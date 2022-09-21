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
