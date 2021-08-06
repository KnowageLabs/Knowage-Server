export interface iAlert {
    id?: number | null
    name?: string
    jsonOptions?: any
    singleExecution?: boolean
    eventBeforeTriggerAction?: number
    alertListener?: iListener
    jobStatus?: string
    frequency?: iFrequency
}
export interface iListener {
    id?: number
    name?: string
    className?: string
    template?: string
}

export interface iFrequency {
    cron: any
    startDate: number | Date
    endDate: number | Date | null
    startTime: number | Date
    endTime: string | Date | null
}

export interface iAction {
    idAction: Number
    jsonActionParameters?: any
    thresholdValues?: any
    thresholdData?: any
}
