export interface iOlapCustomView {
    id: number,
    biobjId: number,
    name: string,
    isPublic: boolean,
    owner: string,
    description: string,
    lastChangeDate: number | Date,
    creationDate: number | Date,
    content: string,
    binaryContentId: number
}