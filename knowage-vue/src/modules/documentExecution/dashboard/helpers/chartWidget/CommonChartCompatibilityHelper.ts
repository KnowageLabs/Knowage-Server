import { IWidget, IWidgetColumn } from '../../Dashboard'
import { getFormattedWidgetColumn } from '../common/WidgetColumnHelper'
import { hexToRgba } from '@/modules/documentExecution/dashboard/helpers/FormattingHelpers'
import useStore from '@/App.store'

interface IOldModelCategory {
    column: string
    groupby: string
    groupbyNames: string
    name: string
    orderColumn: string
    orderType: string
    drillOrder: any
}

const columnNameIdMap = {}
const store = useStore()

export const getFormattedWidgetColumns = (widget: any, chartLibrary: 'chartJS' | 'highcharts' | 'vega') => {
    if (!widget.content || !widget.content.columnSelectedOfDatasetAggregations || !widget.content.chartTemplate || !widget.content.chartTemplate.CHART || !widget.content.chartTemplate.CHART.VALUES) return []
    const chartType = widget.content.chartTemplate.CHART.type
    const widgetColumNameMap = {}
    for (let i = 0; i < widget.content.columnSelectedOfDatasetAggregations.length; i++) {
        if (!widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name]) widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name] = getFormattedWidgetColumn(widget.content.columnSelectedOfDatasetAggregations[i], columnNameIdMap)
    }

    console.log('-------- widgetColumNameMap 1: ', widgetColumNameMap)
    const formattedColumns = [] as IWidgetColumn[]
    const category = widget.content.chartTemplate.CHART.VALUES.CATEGORY
    console.log(' !!!!!!!! CATEGORY: ', category)
    if (category) {
        Array.isArray(category) ? addCategoryColumnsFromArray(category, formattedColumns, widgetColumNameMap, widget, 'vega') : addCategoryColumns(category, formattedColumns, widgetColumNameMap, widget, chartLibrary)
    }



    const index = getMaximumNumberOfSeries(chartLibrary, chartType, widget)
    if (widget.content.chartTemplate.CHART.VALUES.SERIE) {
        const endIndex = index ?? widget.content.chartTemplate.CHART.VALUES.SERIE.length
        for (let i = 0; i < endIndex && i < widget.content.chartTemplate.CHART.VALUES.SERIE.length; i++) addSerieColumn(widget.content.chartTemplate.CHART.VALUES.SERIE[i], widgetColumNameMap, formattedColumns)
    }
    return formattedColumns
}

export const getMaximumNumberOfSeries = (chartLibrary: 'chartJS' | 'highcharts' | 'vega', chartType: string, widget: any) => {
    if (['chartJS', 'vega'].includes(chartLibrary)) return 1
    if (chartLibrary === 'highcharts' && store.user.enterprise && chartType === 'PIE') return 1
    if (chartType === 'GAUGE') {
        const chartSubtype = widget.content.chartTemplate.CHART.subtype
        switch (chartSubtype) {
            case 'activity':
                return 4
            case 'solid':
                return 1
            default:
                return null
        }
    }
    return null
}

const addCategoryColumnsFromArray = (category: IOldModelCategory[], formattedColumns: IWidgetColumn[], widgetColumNameMap: any, widget: IWidget, chartLibrary: 'vega') => {
    console.log('-------- widgetColumNameMap 2: ', widgetColumNameMap)
    category.forEach((category: IOldModelCategory) => addCategoryColumn(category, widgetColumNameMap, formattedColumns, widget, chartLibrary))
    console.log('------------ formattedColumns: ', formattedColumns)
}

export const addCategoryColumns = (category: IOldModelCategory, formattedColumns: IWidgetColumn[], widgetColumNameMap: any, widget: IWidget, chartLibrary: 'chartJS' | 'highcharts' | 'vega') => {
    addCategoryColumn(category, widgetColumNameMap, formattedColumns, widget, chartLibrary)
    if (!chartCanHaveOnlyOneAttribute(widget, chartLibrary) && category.groupbyNames) {
        addDrillColumnsFromCategory(category, widgetColumNameMap, formattedColumns)
    }
}

const addCategoryColumn = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[], widget: IWidget, chartLibrary: 'chartJS' | 'highcharts' | 'vega') => {
    console.log('-------[category.column]: ', category.column)
    console.log('------- widgetColumNameMap 3: ', widgetColumNameMap)
    console.log('------- widgetColumNameMap[category.column]: ', widgetColumNameMap[category.column])
    if (widgetColumNameMap[category.column]) {
        const tempColumn = { ...widgetColumNameMap[category.column] }
        if (chartHasDrilldown(widget, chartLibrary) && category.drillOrder) tempColumn.drillOrder = createDrillOrder(category.drillOrder[category.column].orderColumn, category.drillOrder[category.column].orderType)
        formattedColumns.push(tempColumn)
    }
}

const chartCanHaveOnlyOneAttribute = (widget: any, chartLibrary: 'chartJS' | 'highcharts' | 'vega') => {
    return (chartLibrary === 'chartJS' && widget.content.chartTemplate.CHART.type === 'PIE')
}

const chartHasDrilldown = (widget: any, chartLibrary: 'chartJS' | 'highcharts' | 'vega') => {
    return chartLibrary === 'highcharts' && store.user.enterprise && widget.content.chartTemplate.CHART.type === 'PIE'
}

const addDrillColumnsFromCategory = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    const categoryColumnNames = category.groupbyNames.split(',')
    categoryColumnNames.forEach((columnName: string) => {
        const columnNameTrimmed = columnName.trim()
        if (widgetColumNameMap[columnNameTrimmed]) {
            const tempColumn = { ...widgetColumNameMap[columnNameTrimmed], drillOrder: createDrillOrder(null, '') }
            if (category.drillOrder && category.drillOrder[columnNameTrimmed]) {
                tempColumn.drillOrder = createDrillOrder(category.drillOrder[columnNameTrimmed].orderColumn, category.drillOrder[columnNameTrimmed].orderType)
            }
            formattedColumns.push(tempColumn)
        }
    })
}

const createDrillOrder = (orderColumn: string | null, orderType: string) => {
    return orderColumn ? { orderColumnId: orderColumn ? getColumnId(orderColumn) : '', orderColumn: orderColumn, orderType: orderType ? orderType.toUpperCase() : '' } : { orderColumnId: '', orderColumn: '', orderType: '' }
}

export const addSerieColumn = (serie: any, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    const tempColumn = widgetColumNameMap[serie.column] as IWidgetColumn
    tempColumn.aggregation = serie.groupingFunction
    if (serie.orderType) tempColumn.orderType = serie.orderType.toUpperCase()
    formattedColumns.push(tempColumn)
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

export const getFormattedColorSettings = (widget: any) => {
    let formattedColors = [] as string[]
    if (widget.content.chartTemplate.CHART.COLORPALETTE.COLOR) {
        formattedColors = widget.content.chartTemplate.CHART.COLORPALETTE.COLOR.map((oldColor: any) => hexToRgba(oldColor.value))
    }
    return formattedColors
}
