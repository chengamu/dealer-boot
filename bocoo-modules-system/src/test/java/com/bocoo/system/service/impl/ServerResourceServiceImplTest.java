package com.bocoo.system.service.impl;

import com.bocoo.system.domain.vo.ServerResourceVo;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerResourceServiceImplTest {

    @Test
    void buildsResourceSnapshotFromOshiMetrics() {
        SystemInfo systemInfo = mock(SystemInfo.class);
        HardwareAbstractionLayer hardware = mock(HardwareAbstractionLayer.class);
        CentralProcessor processor = mock(CentralProcessor.class);
        GlobalMemory memory = mock(GlobalMemory.class);
        OperatingSystem operatingSystem = mock(OperatingSystem.class);
        FileSystem fileSystem = mock(FileSystem.class);
        OSFileStore fileStore = mock(OSFileStore.class);
        OSFileStore dataStore = mock(OSFileStore.class);

        when(systemInfo.getHardware()).thenReturn(hardware);
        when(systemInfo.getOperatingSystem()).thenReturn(operatingSystem);
        when(hardware.getProcessor()).thenReturn(processor);
        when(hardware.getMemory()).thenReturn(memory);
        when(processor.getLogicalProcessorCount()).thenReturn(4);
        when(processor.getSystemCpuLoadTicks()).thenReturn(ticks(0, 0, 0), ticks(20, 10, 70));
        when(memory.getTotal()).thenReturn(1_000L);
        when(memory.getAvailable()).thenReturn(250L);
        when(operatingSystem.getSystemUptime()).thenReturn(3_600L);
        when(operatingSystem.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.getFileStores(true)).thenReturn(List.of(fileStore, dataStore));
        when(fileStore.getName()).thenReturn("disk0");
        when(fileStore.getMount()).thenReturn("/");
        when(fileStore.getType()).thenReturn("ext4");
        when(fileStore.getTotalSpace()).thenReturn(2_000L);
        when(fileStore.getUsableSpace()).thenReturn(500L);
        when(dataStore.getName()).thenReturn("data");
        when(dataStore.getMount()).thenReturn("/data");
        when(dataStore.getType()).thenReturn("ext4");
        when(dataStore.getTotalSpace()).thenReturn(8_000L);
        when(dataStore.getUsableSpace()).thenReturn(1_000L);

        ServerResourceVo snapshot = new ServerResourceServiceImpl(systemInfo, ignored -> { }).snapshot();

        assertThat(snapshot.getCpu().getUsagePercent()).isEqualTo(30D);
        assertThat(snapshot.getMemory().getUsagePercent()).isEqualTo(75D);
        assertThat(snapshot.getStorage().getUsagePercent()).isEqualTo(75D);
        assertThat(snapshot.getDisks()).hasSize(2);
        assertThat(snapshot.getDisks().get(0)).satisfies(disk -> {
            assertThat(disk.getMount()).isEqualTo("/");
            assertThat(disk.getUsedBytes()).isEqualTo(1_500L);
        });
        assertThat(snapshot.getCollectedAt()).isNotNull();
    }

    @Test
    void reusesSnapshotWithinTtl() {
        SystemInfo systemInfo = systemInfo();
        AtomicInteger sleeps = new AtomicInteger();
        AtomicLong clock = new AtomicLong(1_000L);
        ServerResourceServiceImpl service = new ServerResourceServiceImpl(
            systemInfo, ignored -> sleeps.incrementAndGet(), clock::get, 2_000L);

        ServerResourceVo first = service.snapshot();
        ServerResourceVo second = service.snapshot();

        assertThat(second).isSameAs(first);
        assertThat(sleeps).hasValue(1);
        verify(systemInfo.getHardware().getProcessor(), times(2)).getSystemCpuLoadTicks();
    }

    @Test
    void sharesSlowSampleAcrossConcurrentRequests() throws Exception {
        SystemInfo systemInfo = systemInfo();
        AtomicInteger sleeps = new AtomicInteger();
        AtomicLong clock = new AtomicLong(1_000L);
        CountDownLatch sampling = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ServerResourceServiceImpl service = new ServerResourceServiceImpl(systemInfo, ignored -> {
            sleeps.incrementAndGet();
            clock.addAndGet(3_000L);
            sampling.countDown();
            await(release);
        }, clock::get, 2_000L);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ServerResourceVo> first = executor.submit(service::snapshot);
            assertThat(sampling.await(5, TimeUnit.SECONDS)).isTrue();
            Future<ServerResourceVo> second = executor.submit(service::snapshot);
            release.countDown();

            assertThat(second.get(20, TimeUnit.SECONDS)).isSameAs(first.get(20, TimeUnit.SECONDS));
            assertThat(sleeps).hasValue(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private SystemInfo systemInfo() {
        SystemInfo systemInfo = mock(SystemInfo.class);
        HardwareAbstractionLayer hardware = mock(HardwareAbstractionLayer.class);
        CentralProcessor processor = mock(CentralProcessor.class);
        GlobalMemory memory = mock(GlobalMemory.class);
        OperatingSystem operatingSystem = mock(OperatingSystem.class);
        FileSystem fileSystem = mock(FileSystem.class);
        OSFileStore fileStore = mock(OSFileStore.class);
        when(systemInfo.getHardware()).thenReturn(hardware);
        when(systemInfo.getOperatingSystem()).thenReturn(operatingSystem);
        when(hardware.getProcessor()).thenReturn(processor);
        when(hardware.getMemory()).thenReturn(memory);
        when(processor.getSystemCpuLoadTicks()).thenReturn(ticks(0, 0, 0), ticks(20, 10, 70));
        when(memory.getTotal()).thenReturn(1_000L);
        when(memory.getAvailable()).thenReturn(250L);
        when(operatingSystem.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.getFileStores(true)).thenReturn(List.of(fileStore));
        when(fileStore.getMount()).thenReturn("/");
        when(fileStore.getTotalSpace()).thenReturn(1_000L);
        when(fileStore.getUsableSpace()).thenReturn(250L);
        return systemInfo;
    }

    private long[] ticks(long user, long system, long idle) {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        ticks[CentralProcessor.TickType.USER.getIndex()] = user;
        ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = system;
        ticks[CentralProcessor.TickType.IDLE.getIndex()] = idle;
        return ticks;
    }
}
