export interface iHost {
    hostName: string,
    hardwareId: string
}

export interface iLicense {
    licenseId: number,
    product: string,
    status: string,
    expiration_date: string,
    expiration_date_format: string
}