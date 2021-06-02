export interface iUser {
    id: number | null
    userId: string
    fullName: string
    password: string
    passwordConfirm?: string
    dtPwdBegin: string
    dtPwdEnd: string
    flgPwdBlocked: boolean
    dtLastAccess: number
    isSuperadmin: boolean
    defaultRoleId: number
    failedLoginAttempts: number
    blockedByFailedLoginAttempts: boolean
    sbiExtUserRoleses: Array<iRole>
    sbiUserAttributeses: Object<iAttribute>
}

export interface iRole {
    id: number | null
    name: string
    value: string
}

export interface iAttribute {
    //TODO: Da li je ovo nekada null?
    attributeId: number
    attributeName: string
    attributeDescription: string | null
    allowUser: number | null
    multivalue: number | null
    syntax: number | null
    lovId: number | null
    value: Object | null
}
