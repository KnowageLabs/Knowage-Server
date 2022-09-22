export const getWidgetStyleByType = (propWidget: any, styleType: string) => {
    const styleSettings = propWidget.settings.style[styleType]
    if (styleSettings.enabled) {
        const styleString = Object.entries(styleSettings.properties ?? styleSettings)
            .map(([k, v]) => `${k}:${v}`)
            .join(';')
        return styleString + ';'
    } else return ''
}

export const getRowStyle = (params: any) => {
    console.log('PARAMS _----------------', params)
    var rowStyles = params.propWidget.settings.style.rows
    var rowIndex = params.node.rowIndex

    if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
        if (rowStyles.alternatedRows.oddBackgroundColor && rowIndex % 2 === 0) {
            return { background: rowStyles.alternatedRows.oddBackgroundColor }
        }
        if (rowStyles.alternatedRows.evenBackgroundColor && rowIndex % 2 != 0) {
            return { background: rowStyles.alternatedRows.evenBackgroundColor }
        }
    } else return false
}

export const getColumnConditionalStyles = (propWidget: any, colId: string, valueToCompare: any, returnString?: boolean) => {
    var conditionalStyles = propWidget.settings.conditionalStyles
    var styleString = false as any

    var columnConditionalStyles = conditionalStyles.conditions.filter((condition) => condition.target.includes(colId))
    if (columnConditionalStyles.length > 0) {
        for (let i = 0; i < columnConditionalStyles.length; i++) {
            if (isConditionMet(columnConditionalStyles[i].condition, valueToCompare)) {
                if (columnConditionalStyles[i].applyToWholeRow && !returnString) {
                    styleString = columnConditionalStyles[i].properties
                } else if (returnString) {
                    styleString = Object.entries(columnConditionalStyles[i].properties)
                        .map(([k, v]) => `${k}:${v}`)
                        .join(';')
                }
                break
            }
        }
    }
    return styleString
}
const isConditionMet = (condition, valueToCompare) => {
    var fullfilledCondition = false
    switch (condition.operator) {
        case '==':
            fullfilledCondition = valueToCompare == condition.value
            break
        case '>=':
            fullfilledCondition = valueToCompare >= condition.value
            break
        case '<=':
            fullfilledCondition = valueToCompare <= condition.value
            break
        case 'IN':
            // fullfilledCondition = condition.value.contains(valueToCompare.toString()) != -1
            fullfilledCondition = condition.value.split(',').indexOf(valueToCompare) != -1
            break
        case '>':
            fullfilledCondition = valueToCompare > condition.value
            break
        case '<':
            fullfilledCondition = valueToCompare < condition.value
            break
        case '!=':
            fullfilledCondition = valueToCompare != condition.value
            break
    }
    return fullfilledCondition
}
