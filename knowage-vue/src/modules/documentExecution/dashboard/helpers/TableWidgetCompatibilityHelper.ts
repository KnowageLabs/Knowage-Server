import { IWidget, IWidgetColumn, IWidgetColumnFilter, ITableWidgetSettings, ITableWidgetPagination, ITableWidgetRows, ITableWidgetSummaryRows, ITableWidgetColumnGroup, ITableWidgetColumnGroups, ITableWidgetVisualization, ITableWidgetVisualizationType, ITableWidgetVisibilityCondition, ITableWidgetColumnStyle, ITableWidgetRowsStyle, ITableWidgetBordersStyle, ITableWidgetPaddingStyle, ITableWidgetShadowsStyle, ITableWidgetConditionalStyle, ITableWidgetTooltipStyle } from '../Dashboard'
import cryptoRandomString from 'crypto-random-string'

export const formatTableWidget = (widget: any) => {
    console.log("TableWidgetCompatibilityHelper - formatTableWidget called for: ", widget)
    const formattedWidget = {
        id: widget.id, dataset: widget.dataset.dsId, type: widget.type, columns: getFormattedWidgetColumns(widget), conditionalStyles: [], interactions: [], theme: '', style: {}, settings: {}
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(formattedWidget, widget)
    getFiltersForColumns(formattedWidget, widget)
    getSettingsFromWidgetColumns(formattedWidget, widget)

    console.log("TableWidgetCompatibilityHelper - FORMATTED WIDGET: ", formattedWidget)
    return formattedWidget
}

const getFormattedWidgetColumns = (widget: any) => {
    if (!widget?.content?.columnSelectedOfDataset) return
    const formattedColumns = [] as IWidgetColumn[]
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        formattedColumns.push(getFormattedWidgetColumn(widget.content.columnSelectedOfDataset[i]))
    }
    return formattedColumns
}

const getFormattedWidgetColumn = (widgetColumn: any) => {
    const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widgetColumn.name, alias: widgetColumn.alias, type: widgetColumn.type, fieldType: widgetColumn.fieldType, multiValue: widgetColumn.multiValue, filter: {} } as IWidgetColumn
    if (widgetColumn.aggregationSelected) formattedColumn.aggregation = widgetColumn.aggregationSelected
    return formattedColumn
}

const getColumnId = (formattedWidget: IWidget, widgetColumnName: string) => {
    const index = formattedWidget.columns.findIndex((modelColumn: IWidgetColumn) => widgetColumnName === modelColumn.columnName)
    return (index !== -1) ? formattedWidget.columns[index].id as string : ''
}

// SETTINGS !!!
const getFormattedWidgetSettings = (formattedWidget: IWidget, widget: any) => {
    const formattedSettings = { sortingColumn: getColumnId(formattedWidget, widget.settings?.sortingColumn), sortingOrder: widget.settings?.sortingOrder, updatable: widget.updateble, clickable: widget.cliccable, conditionalStyles: getFormattedConditionalStyles(formattedWidget, widget), configuration: getFormattedConfiguration(formattedWidget, widget) as any, interactions: getFormattedInteractions(widget) as any, pagination: getFormattedPaginations(widget), style: getFormattedStyle(widget) as any, tooltips: getFormattedTooltips() as ITableWidgetTooltipStyle[], visualization: getFormattedVisualizations(widget), responsive: getFormattedResponsivnes(widget) as any } as ITableWidgetSettings
    return formattedSettings
}
const getFormattedConditionalStyles = (formattedWidget: IWidget, widget: any) => {
    const formattedStyles = [] as ITableWidgetConditionalStyle[]
    if (widget.settings.rowThresholds?.enabled) {
        widget.settings.rowThresholds.list.forEach((rowThreshold: any) => {
            formattedStyles.push(createConditionFromRowThreshold(formattedWidget, rowThreshold))
        })
    }

    return formattedStyles
}

const createConditionFromRowThreshold = (formattedWidget: IWidget, rowThreshold: any) => {
    const conditionStyle = {
        target: getColumnId(formattedWidget, rowThreshold.column), applyToWholeRow: false, condition: { type: rowThreshold.compareValueType, operator: rowThreshold.condition, value: '' },
        properties: {
            "justify-content": '',
            "font-family": '',
            "font-size": '',
            "font-style": '',
            "font-weight": '',
            color: '',
            "background-color": '',
            icon: ''
        }
    } as ITableWidgetConditionalStyle
    switch (conditionStyle.condition.type) {
        case 'static':
            conditionStyle.condition.value = rowThreshold.compareValue
            break;
        case 'parameter':
            conditionStyle.condition.value = getParameterValue(rowThreshold.compareValue)
            conditionStyle.condition.parameter = rowThreshold.compareValue
            break
        case 'variable':
            conditionStyle.condition.value = getVariableValue(rowThreshold.compareValue)
            conditionStyle.condition.variable = rowThreshold.compareValue
    }

    if (rowThreshold.style) {
        delete rowThreshold.style['border-top-color']
        delete rowThreshold.style['border-bottom-color']
        conditionStyle.properties = { ...rowThreshold.style, icon: '' }
    }

    return conditionStyle
}

// TODO
const getFormattedConfiguration = (formattedWidget: IWidget, widget: any) => {
    return { columnGroups: getFormattedColumnGroups(widget), exports: getFormattedExport(widget), headers: getHeadersConfiguration(widget), rows: getFormattedRows(widget), summaryRows: getFormattedSummaryRows(widget) }
}

const getFormattedColumnGroups = (widget: any) => {
    if (!widget.groups) return []
    const formattedColumnGroups = [] as ITableWidgetColumnGroup[]
    widget.groups.forEach((group: { id: string, name: string }) => formattedColumnGroups.push({ id: group.id, label: group.name, columns: [] }))
    return { enabled: true, groups: formattedColumnGroups } as ITableWidgetColumnGroups

}

const getFormattedExport = (widget: any) => {
    const formattedExport = {
        pdf: {
            enabled: false,
            custom: {
                height: 0,
                width: 0,
                enabled: false
            },
            a4landscape: false,
            a4portrait: false
        },
        showExcelExport: false,
        showScreenshot: false
    }
    if (widget.settings.exportpdf) formattedExport.pdf = widget.settings.exportpdf
    if (widget.style) {
        formattedExport.showExcelExport = widget.style.showExcelExport ?? false
        formattedExport.showScreenshot = widget.style.showScreenshot ?? false
    }
    return formattedExport

}

const getHeadersConfiguration = (widget: any) => {
    return { enabled: widget.style?.th?.enabled ?? false, enabledMultiline: widget.style?.th?.multiline ?? false, custom: { enabled: false, rules: [] } }
}

const getFormattedRows = (widget: any) => {
    return { indexColumn: widget.settings?.indexColumn, rowSpan: { enabled: false, column: '' } } as ITableWidgetRows
}

// IMPORTANT
const getSettingsFromWidgetColumns = (formattedWidget: IWidget, widget: any) => {
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        const tempColumn = widget.content.columnSelectedOfDataset[i]
        getRowConfigurationFromWidgetColumn(formattedWidget, tempColumn)
        getHeaderConfigurationFromWidgetColumn(formattedWidget, tempColumn)
        if (tempColumn.group) addColumnToColumnGroup(formattedWidget, tempColumn)
        getVisualizationTypeConfigurationsFromColumn(formattedWidget, tempColumn)
        getVisibilityConditionsFromColumn(formattedWidget, tempColumn)
        getStyleFromColumn(formattedWidget, tempColumn)
        getConditionalStyleFromColumn(formattedWidget, tempColumn)
        getTooltipFromColumn(formattedWidget, tempColumn)
    }

}

const addColumnToColumnGroup = (formattedWidget: IWidget, tempColumn: any) => {
    const columnGroups = formattedWidget.settings.configuration.columnGroups.groups
    const index = columnGroups.findIndex((columnGroup: ITableWidgetColumnGroup) => columnGroup.id === tempColumn.group)
    if (index !== -1) columnGroups[index].columns.push(getColumnId(formattedWidget, tempColumn.name))
}

const getVisualizationTypeConfigurationsFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    if (tempColumn.fieldType === "ATTRIBUTE" && tempColumn.precision !== 0 || tempColumn.style?.prefix || tempColumn.style?.suffix || tempColumn.pinned) {
        addVisualisationTypeAttributeColumn(formattedWidget, tempColumn)
    } else if (tempColumn.fieldType === 'MEASURE' && tempColumn.visType) {
        addVisualisationTypeMeasureColumn(formattedWidget, tempColumn)
    }
}

const getVisibilityConditionsFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    if (tempColumn.style && tempColumn.style.hasOwnProperty('hiddenColumn') || tempColumn.style.hasOwnProperty('hideFromPdf')) {
        const tempVisibiilityCondition = {
            target: [getColumnId(formattedWidget, tempColumn.name)],
            hide: tempColumn.style.hiddenColumn ?? false, hidePdf: tempColumn.style.hideFromPdf ?? false, condition: {
                type: 'always'
            }
        } as ITableWidgetVisibilityCondition
        if (tempColumn.variables) {
            getVisibilityConditionVariable(formattedWidget, tempColumn.variables, tempVisibiilityCondition)
        } else {
            formattedWidget.settings.visualization.visibilityConditions.push(tempVisibiilityCondition)
        }
    }
}

const getStyleFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    if (!tempColumn.style) return
    let hasStyle = false
    let fields = ['background-color', 'color', "justify-content", "font-size", "font-family", "font-style", "font-weight"]
    for (let i = 0; i < fields.length; i++) {
        if (tempColumn.style.hasOwnProperty(fields[i])) {
            hasStyle = true;
            break;
        }
    }

    if (hasStyle) formattedWidget.settings.style.columns.push({
        target: [getColumnId(formattedWidget, tempColumn.name)], properties: {
            "background-color": tempColumn.style['background-color'] ?? "rgb(0, 0, 0)",
            color: tempColumn.style.color ?? 'rgb(255, 255, 255)',
            "justify-content": tempColumn.style['justify-content'] ?? '',
            "font-size": tempColumn.style['font-size'] ?? "",
            "font-family": tempColumn.style['font-family'] ?? '',
            "font-style": tempColumn.style['font-style'] ?? '',
            "font-weight": tempColumn.style['font-weight'] ?? '',
        }
    })

}

const getConditionalStyleFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    if (!tempColumn.ranges || tempColumn.ranges.length === 0) return
    const columnId = getColumnId(formattedWidget, tempColumn.name)
    tempColumn.ranges.forEach((range: any) => {
        const tempConditionalStyle = {
            target: columnId,
            applyToWholeRow: true,
            condition: {
                type: 'static',
                operator: range.operator,
                value: range.value
            },
            properties: {
                "justify-content": '',
                "font-family": '',
                "font-size": '',
                "font-style": '',
                "font-weight": '',
                color: range.color ?? '',
                "background-color": range['background-color'] ?? '',
                icon: range.icon ?? ''
            }
        } as ITableWidgetConditionalStyle
        formattedWidget.settings.conditionalStyles.push(tempConditionalStyle)
    })
}

const getTooltipFromColumn = (formattedWidget: IWidget, tempColumn: any) => {
    console.log("getTooltipFromColumn - formattedWidget ", formattedWidget)
    console.log("getTooltipFromColumn - tempColumn ", tempColumn)
    if (tempColumn.hasOwnProperty('hideTooltip') || tempColumn.style.hasOwnProperty('tooltip')) {
        const tempTooltipStyle = {
            target: [getColumnId(formattedWidget, tempColumn.name)],
            enabled: !tempColumn.hideTooltip,
            prefix: tempColumn.style?.tooltip?.prefix ?? '',
            suffix: tempColumn.style?.tooltip?.suffix ?? '',
            precision: tempColumn.style?.tooltip?.precision ?? 0,
            header: {
                enabled: tempColumn.style?.enableCustomHeaderTooltip ?? false,
                text: tempColumn.style?.customHeaderTooltip ?? ''
            }
        }
        formattedWidget.settings.tooltips.push(tempTooltipStyle)
    }
}

const getVisibilityConditionVariable = (formattedWidget: IWidget, variables: { action: string, variable: string, condition: string, value: string }[], tempVisibiilityCondition: ITableWidgetVisibilityCondition) => {
    variables.forEach((variable: { action: string, variable: string, condition: string, value: string }) => {
        if (variable.action === 'hide') {
            tempVisibiilityCondition.condition = {
                type: 'variable',
                variable: variable.variable,
                variableValue: 'MOCK',
                operator: variable.condition,
                value: variable.value,
            }
            formattedWidget.settings.visualization.visibilityConditions.push(tempVisibiilityCondition)
        }
    })
}

const addVisualisationTypeAttributeColumn = (formattedWidget: IWidget, tempColumn: any) => {
    formattedWidget.settings.visualization.types.push({ target: [getColumnId(formattedWidget, tempColumn.name)], type: 'Text', prefix: tempColumn.style?.prefix ?? '', suffix: tempColumn.style?.suffix ?? '', pinned: tempColumn.pinned ?? '' })
}

const addVisualisationTypeMeasureColumn = (formattedWidget: IWidget, tempColumn: any) => {
    const tempVisualizationType = { target: [getColumnId(formattedWidget, tempColumn.name)], type: formatColumnVisualizationTypeFromOldModel(tempColumn.visType), precision: tempColumn.precision, prefix: tempColumn.style?.prefix ?? '', suffix: tempColumn.style?.suffix, pinned: tempColumn.pinned ?? '' } as ITableWidgetVisualizationType
    if ((tempColumn.visType === 'Chart' || tempColumn.visType === 'Text & Chart') && tempColumn.barchart) {
        tempVisualizationType.min = tempColumn.barchart.minValue ?? 0
        tempVisualizationType.max = tempColumn.barchart.maxValue ?? 0
        tempVisualizationType.alignment = tempColumn.barchart.style ? tempColumn.barchart.style['justify-content'] ?? '' : ''
        tempVisualizationType.color = tempColumn.barchart.style ? hexToRgb(tempColumn.barchart.style.color) : ''
        tempVisualizationType['background-color'] = tempColumn.barchart.style ? hexToRgb(tempColumn.barchart.style['background-color']) ?? '' : ''
    }
    formattedWidget.settings.visualization.types.push(tempVisualizationType)
}

const hexToRgb = (hex: string) => {
    if (!hex.startsWith('#')) return 'rgb(0, 0, 0)'
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? "rgb(" + parseInt(result[1], 16) + ', ' + parseInt(result[2], 16) + ', ' + parseInt(result[3], 16) + ')' : '';
}


const formatColumnVisualizationTypeFromOldModel = (visType: string) => {
    switch (visType) {
        case 'Text & Chart':
            return 'Bar'
        case 'Chart':
            return 'Bar'
        case 'Icon only':
            return 'Icon'
        default:
            return 'Text'
    }
}

const getRowConfigurationFromWidgetColumn = (formattedWidget: IWidget, column: any) => {
    if (column.rowSpan) {
        formattedWidget.settings.configuration.rows.rowSpan.enabled = true;
        formattedWidget.settings.configuration.rows.rowSpan.column = getColumnId(formattedWidget, column.name)
    }
}

const getHeaderConfigurationFromWidgetColumn = (formattedWidget: IWidget, column: any) => {
    if (column.style && column.style.hasOwnProperty('hideHeader')) {
        formattedWidget.settings.configuration.headers.custom.enabled = true;
        formattedWidget.settings.configuration.headers.custom.rules.push({ target: [getColumnId(formattedWidget, column.name)], action: 'hide' })

    }

}

const getFormattedSummaryRows = (widget: any) => {
    let formattedSummaryRows = {} as ITableWidgetSummaryRows
    if (widget.settings.summary) formattedSummaryRows = widget.settings.summary
    if (formattedSummaryRows.list && formattedSummaryRows.list[0]) formattedSummaryRows.list[0].aggregation = 'Columns Default Aggregation'
    return formattedSummaryRows
}

// TODO
const getFormattedInteractions = (widget: any) => {
    return {}
}


const getFormattedPaginations = (widget: any) => {
    if (!widget.settings?.pagination) return { enabled: false, itemsNumber: 0 }
    return { enabled: widget.settings.pagination.enabled, itemsNumber: widget.settings.pagination.itemsNumber } as ITableWidgetPagination
}

// STYLE!!!

const getFormattedStyle = (widget: any) => {
    return {
        borders: getFormattedBorderStyle(widget),
        columns: [],
        columnGroups: getFormattedColumnGroupsStyle(widget),
        headers: getFormattedHeadersStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        rows: getFormattedRowsStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        summary: getFormattedSummaryStyle(widget)
    }
}

const getFormattedTooltips = () => {
    const allTooltip = {
        target: 'all',
        enabled: false,
        prefix: '',
        suffix: '',
        precision: 0,
        header: {
            enabled: false,
            text: ''
        }
    }
    return [allTooltip] as ITableWidgetTooltipStyle[]
}

const getFormattedBorderStyle = (widget: any) => {
    if (!widget.style || !widget.style.border) return {
        enabled: false,
        properties: {
            "border-bottom-left-radius": "",
            "border-bottom-right-radius": "",
            "border-style": "",
            "border-top-left-radius": "",
            "border-top-right-radius": "",
            "border-width": "",
            "border-color": "rgb(212, 212, 212)"
        }
    } as ITableWidgetBordersStyle

    return { enabled: true, properties: { ...widget.style.border, 'border-color': hexToRgb(widget.style.border['border-color']) } } as ITableWidgetBordersStyle
}

const getFormattedColumnGroupsStyle = (widget: any) => {
    const formattedColumnGroupsStyles = [] as ITableWidgetColumnStyle[]
    if (!widget.groups) return formattedColumnGroupsStyles
    let fields = ['background-color', 'color', "justify-content", "font-size", "font-family", "font-style", "font-weight"]
    for (let i = 0; i < widget.groups.length; i++) {
        const tempGroup = widget.groups[i]
        let hasStyle = false;
        for (let j = 0; j < fields.length; j++) {
            if (tempGroup.hasOwnProperty(fields[j])) {
                hasStyle = true;
                break
            }
        }
        if (hasStyle) formattedColumnGroupsStyles.push({
            target: [tempGroup.id], properties: {
                "background-color": tempGroup['background-color'] ?? "rgb(0, 0, 0)",
                color: tempGroup.color ?? 'rgb(255, 255, 255)',
                "justify-content": tempGroup['justify-content'] ?? '',
                "font-size": tempGroup['font-size'] ?? "",
                "font-family": tempGroup['font-family'] ?? '',
                "font-style": tempGroup['font-style'] ?? '',
                "font-weight": tempGroup['font-weight'] ?? '',
            }
        })
    }
    return formattedColumnGroupsStyles
}

const getFormattedHeadersStyle = (widget: any) => {
    if (!widget.style?.th) return {
        height: 25,
        properties: {
            "background-color": "rgb(137, 158, 175)",
            color: 'rgb(255, 255, 255)',
            "justify-content": 'center',
            "font-size": "14px",
            "font-family": "",
            "font-style": "normal",
            "font-weight": "",
        }
    }


    return {
        height: widget.style.th.height,
        properties: {
            "background-color": widget.style.th['background-color'] ?? "rgb(137, 158, 175)",
            color: widget.style.th.color ?? 'rgb(255, 255, 255)',
            "justify-content": widget.style.th['justify-content'] ?? 'center',
            "font-size": widget.style.th['font-size'] ?? "14px",
            "font-family": widget.style.th['font-family'] ?? '',
            "font-style": widget.style.th['font-style'] ?? 'normal',
            "font-weight": widget.style.th['font-weight'] ?? '',
        }
    }
}

const getFormattedPaddingStyle = (widget: any) => {
    if (!widget.style || !widget.style.padding) return {
        enabled: false,
        properties: {
            "padding-top": '',
            "padding-left": '',
            "padding-bottom": '',
            "padding-right": '',
            unlinked: false
        }
    } as ITableWidgetPaddingStyle

    return {
        enabled: widget.style.padding.enabled,
        properties: {
            "padding-top": widget.style.padding['padding-top'],
            "padding-left": widget.style.padding['padding-left'],
            "padding-bottom": widget.style.padding['padding-bottom'],
            "padding-right": widget.style.padding['padding-right'],
            unlinked: widget.style.padding.unlinked
        }
    } as ITableWidgetPaddingStyle
}

const getFormattedRowsStyle = (widget: any) => {
    const formattedRowsStyle = {
        height: widget.style.tr?.height ?? 0,
        multiselectable: widget.settings.multiselectable ?? false,
        selectionColor: widget.settings.multiselectablecolor ?? '',
        alternatedRows: {
            enabled: widget.settings.alternateRows.enabled ?? false,
            evenBackgroundColor: widget.settings.alternateRows.evenRowsColor ?? 'rgb(228, 232, 236)',
            oddBackgroundColor: widget.settings.alternateRows.oddRowsColor ?? ''

        }
    }
    return formattedRowsStyle as ITableWidgetRowsStyle
}

const getFormattedShadowsStyle = (widget: any) => {
    if (!widget.style || !widget.style.shadow) return {
        enabled: false,
        properties: {
            "box-shadow": '',
            "backgroundColor": ''
        }
    } as ITableWidgetShadowsStyle

    return {
        enabled: true,
        properties: {
            "box-shadow": widget.style.shadow["box-shadow"],
            "backgroundColor": hexToRgb(widget.style.backgroundColor)
        }
    } as ITableWidgetShadowsStyle
}


const getFormattedSummaryStyle = (widget: any) => {
    if (!widget.settings.summary || !widget.settings.summary.style) return {
        "background-color": "",
        "color": "",
        "font-family": "",
        "font-size": "",
        "font-style": "",
        "font-weight": "",
        "justify-content": ""
    }

    return {
        "background-color": convertColorFromHSLtoRGB(widget.settings.summary.style['background-color']),
        "color": convertColorFromHSLtoRGB(widget.settings.summary.style.color),
        "font-family": widget.settings.summary.style['font-family'] ?? '',
        "font-size": widget.settings.summary.style['font-size'] ?? '',
        "font-style": widget.settings.summary.style['font-style'] ?? '',
        "font-weight": widget.settings.summary.style['font-weight'] ?? '',
        "justify-content": ""
    }
}

const convertColorFromHSLtoRGB = (hslColor: string | null) => {
    if (!hslColor) return 'rgb(0, 0, 0)'
    const temp = hslColor
        ?.trim()
        ?.substring(4, hslColor.length - 1)
        ?.split(',')

    const h = temp[0] ? +temp[0] : 0
    let s = temp[1] ? +(temp[1].replace('%', '').trim()) : 0
    let l = temp[2] ? +(temp[2].replace('%', '').trim()) : 0


    s /= 100;
    l /= 100;
    const k = n => (n + h / 30) % 12;
    const a = s * Math.min(l, 1 - l);
    const f = (n: number) =>
        l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
    const tempResult = [Math.round(255 * f(0)), Math.round(255 * f(8)), Math.round(255 * f(4))]
    return 'rgb(' + tempResult[0] + ', ' + tempResult[1] + ', ' + tempResult[2] + ')'
}


const getFormattedVisualizations = (widget: any) => {
    return { types: [], visibilityConditions: [] }
}

const getFormattedResponsivnes = (widget: any) => {
    return { xs: true, sm: true, md: true, lg: true, xl: true }
}

const getFiltersForColumns = (formattedWidget: IWidget, oldWidget: any) => {
    if (!oldWidget.filters || oldWidget.filters.length === 0) return
    for (let i = 0; i < oldWidget.filters.length; i++) {
        const tempFilter = oldWidget.filters[i]
        const index = formattedWidget.columns?.findIndex((column: IWidgetColumn) => column.columnName === tempFilter.colName)
        if (index !== -1) {
            formattedWidget.columns[index].filter = { enabled: true, operator: tempFilter.filterOperator, value: tempFilter.filterVal1 }
            if (tempFilter.filterVal2 && formattedWidget.columns[index].filter) (formattedWidget.columns[index].filter as IWidgetColumnFilter).value2 = tempFilter.filterVal2
        }
    }
}

export const getColumnById = (formattedWidget: IWidget, columnId: string) => {
    const index = formattedWidget.columns.findIndex((column: IWidgetColumn) => column.id === columnId)
    return index !== -1 ? formattedWidget.columns[index] : null
}


// TODO - PARAMETER VALUE
const getParameterValue = (parameterName: string) => {
    return 'MOCKED PARAMETER VALUE';
}

// TODO - VARIABLE VALUE
const getVariableValue = (variable: string) => {
    return 'MOCKED VARIABLE VALUE';
}