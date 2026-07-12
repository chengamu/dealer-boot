package com.bocoo.system.domain.vo;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ServerResourceVo {
    private ServerCpuVo cpu;
    private ServerMemoryVo memory;
    private ServerJvmVo jvm;
    private ServerSystemVo system;
    private ServerStorageVo storage;
    private List<ServerDiskVo> disks;
    private Instant collectedAt;
}
