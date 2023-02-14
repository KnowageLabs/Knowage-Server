import { formatNumber } from "./qbeHelpers"


export function setInputDataType(columnType: string) {
    switch (columnType) {
        case 'int':
        case 'float':
        case 'decimal':
        case 'long':
            return 'number'
        case 'date':
            return 'date'
        default:
            return 'text'
    }
}

export function getInputStep(dataType: string) {
    if (dataType === 'float') {
        return '.01'
    } else if (dataType === 'int') {
        return '1'
    } else {
        return 'any'
    }
}

export const numberFormatRegex = '^(####|#\.###|#\,###){1}([,.]?)(#*)$' //eslint-disable-line no-useless-escape 

export const formatRegistryNumber = (column: any) => {
    if (column.columnInfo?.type === 'int') return { useGrouping: true, minFractionDigits: 0, maxFractionDigits: 0 }
    if (!column.format) return null

    const result = column.format.trim().match(numberFormatRegex)
    if (!result) return null

    const useGrouping = result[1].includes('.') || result[1].includes(',')
    const maxFractionDigits = result[3].length
    const configuration = { useGrouping: useGrouping, minFractionDigits: maxFractionDigits, maxFractionDigits: maxFractionDigits }
    if (!isFrontendFormatCompatibleWithBackendFormat(column, configuration)) return null

    return configuration

}

const isFrontendFormatCompatibleWithBackendFormat = (column: any, frontendConfiguration: { useGrouping: boolean, minFractionDigits: number, maxFractionDigits: number }) => {
    const backendNumberConfiguration = formatNumber(column.columnInfo)
    return frontendConfiguration.maxFractionDigits <= backendNumberConfiguration?.minFractionDigits
}
