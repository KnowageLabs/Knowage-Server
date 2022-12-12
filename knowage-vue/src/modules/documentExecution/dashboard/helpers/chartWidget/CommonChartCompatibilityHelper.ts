import { IWidget, IWidgetColumn } from "../../Dashboard"
import { getFormattedWidgetColumn } from "../common/WidgetColumnHelper"

interface IOldModelCategory {
    column: string
    groupby: string
    groupbyNames: string,
    name: string,
    orderColumn: string,
    orderType: string,
    drillOrder: any
}

const columnNameIdMap = {}

export const getFormattedWidgetColumns = (widget: any, chartLibrary: 'chartJS' | 'highcharts') => {
    if (!widget.content || !widget.content.columnSelectedOfDatasetAggregations || !widget.content.chartTemplate || !widget.content.chartTemplate.CHART || !widget.content.chartTemplate.CHART.VALUES) return []
    const widgetColumNameMap = {}
    for (let i = 0; i < widget.content.columnSelectedOfDatasetAggregations.length; i++) {
        if (!widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name]) widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name] = getFormattedWidgetColumn(widget.content.columnSelectedOfDatasetAggregations[i], columnNameIdMap)
    }

    const formattedColumns = [] as IWidgetColumn[]
    const category = widget.content.chartTemplate.CHART.VALUES.CATEGORY
    const serie = widget.content.chartTemplate.CHART.VALUES.SERIE ? widget.content.chartTemplate.CHART.VALUES.SERIE[0] : null
    if (category) addCategoryColumns(category, formattedColumns, widgetColumNameMap, widget, chartLibrary)
    if (serie) addSerieColumn(serie, widgetColumNameMap, formattedColumns)

    console.log("FORMATTED COLUMNS: ", formattedColumns)
    return formattedColumns
}


export const addCategoryColumns = (category: IOldModelCategory, formattedColumns: IWidgetColumn[], widgetColumNameMap: any, widget: IWidget, chartLibrary: 'chartJS' | 'highcharts') => {
    addCategoryColumn(category, widgetColumNameMap, formattedColumns, widget, chartLibrary)
    if (!chartCanHaveOnlyOneAttribute(widget, chartLibrary) && category.groupbyNames) {
        addDrillColumnsFromCategory(category, widgetColumNameMap, formattedColumns)
    }
}


const addCategoryColumn = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[], widget: IWidget, chartLibrary: 'chartJS' | 'highcharts') => {
    if (widgetColumNameMap[category.column]) {
        const tempColumn = { ...widgetColumNameMap[category.column] }
        if (chartHasDrilldown(widget, chartLibrary) && category.drillOrder) tempColumn.drillOrder = createDrillOrder(category.drillOrder[category.column].orderColumn, category.drillOrder[category.column].orderType)
        console.log(">>>>>>>>> TEMP COLUMN 1: ", tempColumn)
        formattedColumns.push(tempColumn)

    }
}

const chartCanHaveOnlyOneAttribute = (widget: any, chartLibrary: 'chartJS' | 'highcharts') => {
    return chartLibrary === 'chartJS' && widget.content.chartTemplate.CHART.type === 'PIE'
}

const chartHasDrilldown = (widget: any, chartLibrary: 'chartJS' | 'highcharts') => {
    return chartLibrary === 'highcharts' && widget.content.chartTemplate.CHART.type === 'PIE'
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
            console.log(">>>>>>>>> TEMP COLUMN 2: ", tempColumn)
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
    console.log(">>>>>>>>> TEMP COLUMN 3: ", tempColumn)
    formattedColumns.push(tempColumn)
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}


