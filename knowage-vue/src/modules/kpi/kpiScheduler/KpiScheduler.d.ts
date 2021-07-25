export interface iKpiSchedule {
    id: number,
    name: string,
    filters: [],
    delta: Boolean,
    kpiNames: string,
    jobStatus: "ACTIVE" | "EXPIRED" | "SUSPENDED",
    frequency: iFrequency
}

export interface iFrequency {
    cron: any,
    startDate: number | Date,
    endDate: number | Date
    startTime: string | Date
    endTime: string | Date
}

export interface iFilter {
    kpiName: string,
    kpiVersion: number,
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
    dateCreation: string
}

export interface iLov {
    id: number; name: string; label: string
}