// eslint-disable-next-line no-useless-escape
export const extendedAlphanumeric = /^([\p{L}\w\s\-_;\(\)\[\]:!\\\/\?,\.'"\x2F\x5F%])*$/u

// eslint-disable-next-line no-useless-escape
export const extendedAlphanumeric81 = /^([\pL\w\s\-\_\(\)\[\]\;\:\!\?\,\.\'\"\x2F\x5F%])*$/

export const fullnameRegex = /^[\p{L}\w]+([\p{L}\w\s\-,.'\x2F\\@])*$/u

export const alphanumericNoSpaces = /^([a-zA-Z0-9\-_])*$/

export const alphanumeric = /^([a-zA-Z0-9\s\-_])*$/
