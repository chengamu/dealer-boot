import { requestData } from '@/utils/request'

export interface ServerCpu {
  logicalCores: number
  usagePercent: number
  userPercent: number
  systemPercent: number
  waitPercent: number
}

export interface ServerMemory {
  totalBytes: number
  usedBytes: number
  availableBytes: number
  usagePercent: number
}

export interface ServerJvm {
  usedBytes: number
  committedBytes: number
  maxBytes: number
  usagePercent: number
  vmName: string
  javaVersion: string
  startTime: string
  uptimeSeconds: number
  threadCount: number
}

export interface ServerSystem {
  hostName: string
  osName: string
  osVersion: string
  architecture: string
  uptimeSeconds: number
}

export interface ServerDisk extends ServerMemory {
  name: string
  mount: string
  type: string
}

export interface ServerResource {
  cpu: ServerCpu
  memory: ServerMemory
  jvm: ServerJvm
  system: ServerSystem
  storage: ServerMemory
  disks: ServerDisk[]
  collectedAt: string
}

export function getServerResource() {
  return requestData<ServerResource>({
    url: '/monitor/server',
    method: 'get'
  })
}
