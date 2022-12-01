import { IWidget, IWidgetColumn, IWidgetExports, IWidgetInteractions } from "../../../Dashboard"
import { HighchartsChartModel, IHighchartsWidgetConfiguration, IHighchartsWidgetSettings } from "../../../interfaces/highcharts/DashboardHighchartsWidget"
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedWidgetColumn } from "../../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../../common/WidgetInteractionsHelper"
import { getFiltersForColumns } from "../../DashboardBackwardCompatibilityHelper"
import { getFormattedStyle } from "./HighchartsWidgetStyleHelper"

const columnNameIdMap = {}

interface IOldModelCategory {
    column: string
    groupby: string
    groupbyNames: string,
    name: string,
    orderColumn: string,
    orderType: string,
    drillOrder: any
}

export const formatHighchartsWidget = (widget: any) => {
    console.log(">>>>>>>>>>> OLD WIDGET: ", widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget),
        theme: '',
        settings: {} as IHighchartsWidgetSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IHighchartsWidgetSettings
    getFiltersForColumns(formattedWidget, widget)
    console.log(">>>>>>>>>>> FORMATTED WIDGET: ", widget)
    return formattedWidget
}

// TODO - add condition for pie widget, see about the property columnSelectedOfDatasetAggregations
export const getFormattedWidgetColumns = (widget: any) => {
    if (!widget.content || !widget.content.columnSelectedOfDatasetAggregations || !widget.content.chartTemplate || !widget.content.chartTemplate.CHART || !widget.content.chartTemplate.CHART.VALUES) return []
    const widgetColumNameMap = {}
    for (let i = 0; i < widget.content.columnSelectedOfDatasetAggregations.length; i++) {
        if (!widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name]) widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name] = getFormattedWidgetColumn(widget.content.columnSelectedOfDatasetAggregations[i], columnNameIdMap)
    }

    const formattedColumns = [] as IWidgetColumn[]
    const category = widget.content.chartTemplate.CHART.VALUES.CATEGORY
    const serie = widget.content.chartTemplate.CHART.VALUES.SERIE ? widget.content.chartTemplate.CHART.VALUES.SERIE[0] : null
    if (category) addCategoryColumns(category, formattedColumns, widgetColumNameMap)
    if (serie) addSerieColumn(serie, widgetColumNameMap, formattedColumns)
    console.log(">>>>>>>>>>> formattedColumns: ", formattedColumns)
    return formattedColumns
}

const addCategoryColumns = (category: IOldModelCategory, formattedColumns: IWidgetColumn[], widgetColumNameMap: any) => {

    addCategoryColumn(category, widgetColumNameMap, formattedColumns)
    if (category.groupbyNames) {
        addDrillColumnsFromCategory(category, widgetColumNameMap, formattedColumns)
    }
}

const addCategoryColumn = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    if (widgetColumNameMap[category.column]) formattedColumns.push({ ...widgetColumNameMap[category.column], drillOrder: createDrillOrder(category.drillOrder[category.column].orderColumn, category.drillOrder[category.column].orderType) })
}

const addDrillColumnsFromCategory = (category: IOldModelCategory, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    const categoryColumnNames = category.groupbyNames.split(',')
    categoryColumnNames.forEach((columnName: string) => {
        const columnNameTrimmed = columnName.trim()
        if (widgetColumNameMap[columnNameTrimmed]) {
            const tempColumn = { ...widgetColumNameMap[columnNameTrimmed], drillOrder: createDrillOrder(null, '') }
            if (category.drillOrder[columnNameTrimmed]) {
                tempColumn.drillOrder = createDrillOrder(category.drillOrder[columnNameTrimmed].orderColumn, category.drillOrder[columnNameTrimmed].orderType)
            }
            formattedColumns.push(tempColumn)
        }
    })
}

const createDrillOrder = (orderColumn: string | null, orderType: string) => {
    return orderColumn ? { orderColumnId: orderColumn ? getColumnId(orderColumn) : '', orderColumn: orderColumn, orderType: orderType } : { orderColumnId: '', orderColumn: '', orderType: '' }
}


const addSerieColumn = (serie: any, widgetColumNameMap: any, formattedColumns: IWidgetColumn[]) => {
    console.log(">>>>>>>> SERIE: ", serie)
    console.log(">>>>>>>> widgetColumNameMap: ", widgetColumNameMap)
    const tempColumn = widgetColumNameMap[serie.column]
    formattedColumns.push(tempColumn)
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: {} as HighchartsChartModel, // TODO
        configuration: getFormattedConfiguration(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget),
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IHighchartsWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IHighchartsWidgetConfiguration
}


export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}
