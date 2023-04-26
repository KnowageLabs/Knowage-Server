export interface IGalleryTemplate {
    id: string
    author: string
    label?: string
    name: string
    type: string
    description?: string
    outputType?: string
    code: ICode
    tags?: Array<string>
    image: string | ArrayBuffer
}

export interface ICode {
    html: string
    python: string
    javascript: string
    css: string
}
