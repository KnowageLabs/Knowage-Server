export interface iAlert {
    id: number | null
    name?: string
    jsonOptions?: string
    singleExecution?: boolean
    eventBeforeTriggerAction?: number
    alertListener?: iListener
    jobStatus?: string
    frequency?: iFrequency
}
export interface iListener {
    id: number
    name?: string
    className?: string
    template?: string
}

export interface iFrequency {
    cron?: string
    startDate?: Date
    endDate?: Date
    startTime?: String
    endTime?: String
}
