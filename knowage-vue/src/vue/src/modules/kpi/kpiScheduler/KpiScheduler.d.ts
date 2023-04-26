export interface iKpiSchedule {
    id?: number,
    name: string,
    filters: iFilter[],
    delta: Boolean,
    kpiNames?: string,
    kpis?: iKpi[],
    jobStatus?: "ACTIVE" | "EXPIRED" | "SUSPENDED",
    frequency: iFrequency
}

export interface iFrequency {
    cron: any,
    startDate: number | Date,
    endDate: number | Date | null
    startTime: number | Date
    endTime: string | Date | null
}

export interface iFilter {
    executionId?: number,
    kpiId: number,
    kpiName: string,
    kpiVersion: number,
    placeholderName: string,
    value: string | null,
    type: { valueId: number, valueCd: string }
}

export interface iExecution {
    id: number,
    schedulerId: number,
    timeRun: number,
    output: string,
    errorCoun: number,
    successCount: number,
    totalCount: number,
    outputPresent: Boolean
}

export interface iKpi {
    id: number,
    name: string,
    author: string,
    version: number,
    dateCreation: string
}

export interface iLov {
    id: number; name: string; label: string
}