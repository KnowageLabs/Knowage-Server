export interface iCache {
    totalMemory: number,
    availableMemory: number,
    availableMemoryPercentage: number,
    cachedObjectsCount: number,
    cleaningEnabled: boolean,
    cleaningQuota: string
}

export interface iMeta {
    name: string,
    signature: string,
    table: string,
    dimension: number
}

export interface iSettings {
    prefixForCacheTablesName: string,
    spaceAvailable: number,
    limitForClean: number,
    schedulingFullClean: { label: string, value: string },
    lastAccessTtl: number,
    createAndPersistTimeout: number,
    cacheLimitForStore: number,
    sqldbCacheTimeout: number,
    hazelcastTimeout: number,
    hazelcastLeaseTime: number
}
