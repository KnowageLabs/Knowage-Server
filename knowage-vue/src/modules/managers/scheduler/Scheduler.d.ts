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
    documents: iDocument[],
    triggers: iTrigger[],
    numberOfDocuments: number
}

export interface iDocument {
    name: string,
    nameTitle: string,
    condensedParameters: string,
    parameters: iParameter[]
}

export interface iParameter {
    name: string,
    value: string,
    type: string,
    iterative: boolean
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
}


export interface iTrigger {

}