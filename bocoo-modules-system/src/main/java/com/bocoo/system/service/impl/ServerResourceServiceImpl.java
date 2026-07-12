package com.bocoo.system.service.impl;

import com.bocoo.system.domain.vo.ServerCpuVo;
import com.bocoo.system.domain.vo.ServerDiskVo;
import com.bocoo.system.domain.vo.ServerJvmVo;
import com.bocoo.system.domain.vo.ServerMemoryVo;
import com.bocoo.system.domain.vo.ServerResourceVo;
import com.bocoo.system.domain.vo.ServerStorageVo;
import com.bocoo.system.domain.vo.ServerSystemVo;
import com.bocoo.system.service.ServerResourceService;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

@Service
public class ServerResourceServiceImpl implements ServerResourceService {

    private static final long CPU_SAMPLE_MILLIS = 500L;
    private static final long SNAPSHOT_TTL_NANOS = 2_000_000_000L;

    private final SystemInfo systemInfo;
    private final LongConsumer sleeper;
    private final LongSupplier nanoTime;
    private final long snapshotTtlNanos;
    private volatile ServerResourceVo cachedSnapshot;
    private volatile long snapshotExpiresAt;

    public ServerResourceServiceImpl() {
        this(new SystemInfo(), Util::sleep);
    }

    ServerResourceServiceImpl(SystemInfo systemInfo, LongConsumer sleeper) {
        this(systemInfo, sleeper, System::nanoTime, SNAPSHOT_TTL_NANOS);
    }

    ServerResourceServiceImpl(SystemInfo systemInfo, LongConsumer sleeper,
                              LongSupplier nanoTime, long snapshotTtlNanos) {
        this.systemInfo = systemInfo;
        this.sleeper = sleeper;
        this.nanoTime = nanoTime;
        this.snapshotTtlNanos = snapshotTtlNanos;
    }

    @Override
    public ServerResourceVo snapshot() {
        long now = nanoTime.getAsLong();
        ServerResourceVo snapshot = cachedSnapshot;
        if (snapshot != null && now < snapshotExpiresAt) return snapshot;
        synchronized (this) {
            now = nanoTime.getAsLong();
            snapshot = cachedSnapshot;
            if (snapshot != null && now < snapshotExpiresAt) return snapshot;
            snapshot = collectSnapshot();
            cachedSnapshot = snapshot;
            snapshotExpiresAt = nanoTime.getAsLong() + snapshotTtlNanos;
            return snapshot;
        }
    }

    private ServerResourceVo collectSnapshot() {
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        List<ServerDiskVo> disks = disks(operatingSystem);
        ServerResourceVo result = new ServerResourceVo();
        result.setCpu(cpu(systemInfo.getHardware().getProcessor()));
        result.setMemory(memory(systemInfo.getHardware().getMemory()));
        result.setJvm(jvm());
        result.setSystem(system(operatingSystem));
        result.setStorage(storage(disks));
        result.setDisks(disks);
        result.setCollectedAt(Instant.now());
        return result;
    }

    private ServerCpuVo cpu(CentralProcessor processor) {
        long[] previous = processor.getSystemCpuLoadTicks();
        sleeper.accept(CPU_SAMPLE_MILLIS);
        long[] current = processor.getSystemCpuLoadTicks();
        long user = delta(previous, current, CentralProcessor.TickType.USER);
        long system = delta(previous, current, CentralProcessor.TickType.SYSTEM);
        long wait = delta(previous, current, CentralProcessor.TickType.IOWAIT);
        long idle = delta(previous, current, CentralProcessor.TickType.IDLE);
        long total = 0L;
        for (CentralProcessor.TickType type : CentralProcessor.TickType.values()) {
            total += delta(previous, current, type);
        }
        ServerCpuVo result = new ServerCpuVo();
        result.setLogicalCores(processor.getLogicalProcessorCount());
        result.setUserPercent(percent(user, total));
        result.setSystemPercent(percent(system, total));
        result.setWaitPercent(percent(wait, total));
        result.setUsagePercent(round(Math.max(0D, 100D - percent(idle, total))));
        return result;
    }

    private ServerMemoryVo memory(GlobalMemory memory) {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = Math.max(0L, total - available);
        ServerMemoryVo result = new ServerMemoryVo();
        result.setTotalBytes(total);
        result.setUsedBytes(used);
        result.setAvailableBytes(available);
        result.setUsagePercent(percent(used, total));
        return result;
    }

    private ServerJvmVo jvm() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long max = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        var runtime = ManagementFactory.getRuntimeMXBean();
        ServerJvmVo result = new ServerJvmVo();
        result.setUsedBytes(heap.getUsed());
        result.setCommittedBytes(heap.getCommitted());
        result.setMaxBytes(max);
        result.setUsagePercent(percent(heap.getUsed(), max));
        result.setVmName(runtime.getVmName());
        result.setJavaVersion(System.getProperty("java.version", "-"));
        result.setStartTime(Instant.ofEpochMilli(runtime.getStartTime()));
        result.setUptimeSeconds(runtime.getUptime() / 1000L);
        result.setThreadCount(ManagementFactory.getThreadMXBean().getThreadCount());
        return result;
    }

    private ServerSystemVo system(OperatingSystem operatingSystem) {
        ServerSystemVo result = new ServerSystemVo();
        result.setHostName(hostName());
        result.setOsName(System.getProperty("os.name", "-"));
        result.setOsVersion(System.getProperty("os.version", "-"));
        result.setArchitecture(System.getProperty("os.arch", "-"));
        result.setUptimeSeconds(operatingSystem.getSystemUptime());
        return result;
    }

    private List<ServerDiskVo> disks(OperatingSystem operatingSystem) {
        List<ServerDiskVo> result = new ArrayList<>();
        for (OSFileStore store : operatingSystem.getFileSystem().getFileStores(true)) {
            if (store.getTotalSpace() <= 0) continue;
            long available = store.getUsableSpace();
            long used = Math.max(0L, store.getTotalSpace() - available);
            ServerDiskVo disk = new ServerDiskVo();
            disk.setName(store.getName());
            disk.setMount(store.getMount());
            disk.setType(store.getType());
            disk.setTotalBytes(store.getTotalSpace());
            disk.setUsedBytes(used);
            disk.setAvailableBytes(available);
            disk.setUsagePercent(percent(used, store.getTotalSpace()));
            result.add(disk);
        }
        return result;
    }

    private ServerStorageVo storage(List<ServerDiskVo> disks) {
        ServerDiskVo primary = disks.stream()
            .filter(disk -> "/".equals(disk.getMount()))
            .findFirst()
            .orElseGet(() -> disks.isEmpty() ? new ServerDiskVo() : disks.get(0));
        long total = primary.getTotalBytes();
        long used = primary.getUsedBytes();
        long available = primary.getAvailableBytes();
        ServerStorageVo result = new ServerStorageVo();
        result.setTotalBytes(total);
        result.setUsedBytes(used);
        result.setAvailableBytes(available);
        result.setUsagePercent(percent(used, total));
        return result;
    }

    private long delta(long[] previous, long[] current, CentralProcessor.TickType type) {
        int index = type.getIndex();
        return Math.max(0L, current[index] - previous[index]);
    }

    private double percent(long part, long total) {
        return total <= 0 ? 0D : round(part * 100D / total);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return "-";
        }
    }
}
