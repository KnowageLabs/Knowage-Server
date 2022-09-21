export const getWidgetStyleByType = (propWidget: any, styleType: string) => {
    const styleSettings = propWidget.settings.style[styleType]
    if (styleSettings.enabled) {
        const styleString = Object.entries(styleSettings.properties ?? styleSettings)
            .map(([k, v]) => `${k}:${v}`)
            .join(';')
        return styleString + ';'
    } else return ''
}
