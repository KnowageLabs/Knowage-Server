export interface iCache {
    totalMemory: number,
    availableMemory: number,
    availableMemoryPercentage: number,
    cachedObjectsCount: number,
    cleaningEnabled: boolean,
    cleaningQuota: string
}
