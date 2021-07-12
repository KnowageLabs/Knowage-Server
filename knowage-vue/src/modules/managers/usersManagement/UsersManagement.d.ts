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
    sbiUserAttributeses: iAttribute
}

export interface iRole {
    id: number | null
    name: string
    value: string
}

export interface iAttribute {
    attributeId: number
    attributeName: string
    attributeDescription: string | null
    allowUser: Boolean | null
    multivalue: Boolean | null
    syntax: Boolean | null
    lovId: number | null
    value: iValue | null
}

export interface iValue {
    name: string
    type: string
}