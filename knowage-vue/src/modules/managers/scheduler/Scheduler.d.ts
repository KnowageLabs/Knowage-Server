export interface iPackage {
    jobName: string,
    jobGroup: string,
    jobDescription: string,
    jobClass: string,
    jobDurability: boolean,
    jobRequestRecovery: boolean,
    jobMergeAllSnapshots: boolean,
    jobCollateSnapshots: boolean,
    useVolatility: boolean,
    jobParameters: { name: string, value: string }[],
    documents: any[],
    triggers: iTrigger[],
    edit?: boolean
    numberOfDocuments?: number
}


export interface iFile {
    id: number,
    name: string,
    parentId?: number,
    biObjects?: Array,
    exportable?: boolean
}

export interface iNode {
    key: number | string,
    icon: string,
    id: number,
    parentId?: number,
    label: string,
    children?: iNode[],
    selectable?: Boolean,
    data: string | any
    customIcon?: string
}