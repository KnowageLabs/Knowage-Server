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

export const formatNumber = (column: any, row: any) => {
    const number = row[column.field]
    switch (column.format) {
        case '####,##':
            return { useGrouping: false, locale: 'it-IT', minFractionDigits: 2, maxFractionDigits: 2 }
        case '####,###':
            return { useGrouping: false, locale: 'it-IT', minFractionDigits: 3, maxFractionDigits: 3 }
        case '#.###,##':
            return { useGrouping: true, locale: 'it-IT', minFractionDigits: 2, maxFractionDigits: 2 }
        case '####':
            return { useGrouping: false, locale: '', minFractionDigits: 0, maxFractionDigits: 0 }
        case '####.##':
            return { useGrouping: false, locale: 'en-US', minFractionDigits: 2, maxFractionDigits: 2 }
        case '#,###.##':
            return { useGrouping: true, locale: 'en-US', minFractionDigits: 2, maxFractionDigits: 2 }
        default:
            return null
    }
}