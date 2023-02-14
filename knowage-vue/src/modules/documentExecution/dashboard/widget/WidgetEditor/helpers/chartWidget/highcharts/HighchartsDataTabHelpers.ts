import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'

export const addHighchartsColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'pie':
        case 'gauge':
        case 'activitygauge':
        case 'solidgauge':
            addHighchartsColumnToTableRows(tempColumn, rows, chartType, mode, widgetModel)
    }
}

const addHighchartsColumnToTableRows = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, mode: string, widgetModel: IWidget) => {
    if (mode === 'attributesOnly' && rows.length < 4) {
        addAttributeColumnToTableRows(tempColumn, rows)
    } else if (mode === 'measuresOnly') {
        addMeasureColumnToTableRows(tempColumn, rows, chartType, widgetModel)
    }
}

const addAttributeColumnToTableRows = (tempColumn: IWidgetColumn, rows: IWidgetColumn[]) => {
    if (tempColumn.fieldType === 'MEASURE') {
        tempColumn.fieldType = 'ATTRIBUTE'
        tempColumn.aggregation = ''
    }
    tempColumn.drillOrder = {
        "orderColumn": "",
        "orderColumnId": "",
        "orderType": ""
    }
    addColumnToRows(rows, tempColumn)
}

const addMeasureColumnToTableRows = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, widgetModel: IWidget) => {
    const maxValues = getMaxValuesNumber(chartType)
    if (maxValues && maxValues !== 1 && rows.length >= maxValues) return
    convertColumnToMeasure(tempColumn)
    if (rows.length === 1 && maxValues === 1) {
        removeSerieFromWidgetModel(widgetModel, rows[0], chartType)
        updateSerieInWidgetModel(widgetModel, tempColumn, chartType)
        rows[0] = tempColumn
    }
    addColumnToRows(rows, tempColumn)
    widgetModel.settings.chartModel.addSerie(tempColumn)
    emitter.emit('seriesAdded', tempColumn)
}

const getMaxValuesNumber = (chartType: string | undefined) => {
    switch (chartType) {
        case 'pie':
        case 'solidgauge':
            return 1
        case 'activitygauge':
            return 4
        default:
            return null
    }
}

const convertColumnToMeasure = (tempColumn: IWidgetColumn) => {
    if (tempColumn.fieldType === 'ATTRIBUTE') {
        tempColumn.fieldType = 'MEASURE'
        tempColumn.aggregation = 'COUNT'
    }
}

const addColumnToRows = (rows: IWidgetColumn[], tempColumn: IWidgetColumn) => {
    const index = rows.findIndex((column: IWidgetColumn) => column.columnName === tempColumn.columnName)
    if (index === -1) rows.push(tempColumn)
}

const updateSerieInWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    if (chartType === 'pie' || chartType === 'solidgauge') {
        updateFirstSeriesOption(widgetModel.settings.accesssibility.seriesAccesibilitySettings, column)
        updateFirstSeriesOption(widgetModel.settings.series.seriesLabelsSettings, column)
    }
}

const updateFirstSeriesOption = (array: any[], column: IWidgetColumn) => {
    if (array && array[0]) {
        array[0].names[0] = column.columnName
    }
}

export const removeSerieFromWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    widgetModel.settings.chartModel.removeSerie(column)
    const allSeriesOption = chartType !== 'pie' && chartType !== 'solidgauge'
    removeColumnFromSubmodel(column, widgetModel.settings.accesssibility.seriesAccesibilitySettings, allSeriesOption)
    removeColumnFromSubmodel(column, widgetModel.settings.series.seriesLabelsSettings, allSeriesOption)
    emitter.emit('seriesRemoved', column)
}

const removeColumnFromSubmodel = (column: IWidgetColumn, array: any[], allSeriesOption: boolean) => {
    for (let i = array.length - 1; i >= 0; i--) {
        for (let j = array[i].names.length - 1; j >= 0; j--) {
            const serieName = array[i].names[j]
            if (serieName === column.columnName) {
                array[i].names.splice(j, 1)
            }
            if (!allSeriesOption && array[i].names.length === 0) array.splice(i, 1)
        }
    }
}

export const updateWidgetModelColumnsAfterChartTypeChange = (widget: IWidget, chartType: string) => {
    const maxAttributeColumns = chartType === 'pie' ? 4 : 0
    const maxMeasureColumns = getMaxValuesNumber(chartType) ?? widget.columns.length
    const updatedColumns = [] as IWidgetColumn[]
    let attributesAdded = 0
    let measuresAdded = 0
    widget.columns.forEach((column: IWidgetColumn) => {
        if (column.fieldType === 'ATTRIBUTE' && attributesAdded < maxAttributeColumns) {
            updatedColumns.push(column)
            attributesAdded++
        } else if (column.fieldType === 'MEASURE' && measuresAdded < maxMeasureColumns) {
            updatedColumns.push(column)
            measuresAdded++
        }
    })
    widget.columns = updatedColumns
}

