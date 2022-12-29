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
    } else if (mode === 'measuresOnly') {
        const maxValues = getMaxValuesNumber(chartType)
        if (maxValues && maxValues !== 1 && rows.length >= maxValues) return
        convertColumnToMeasure(tempColumn)
        if (rows.length === 1 && maxValues === 1) {
            removeSerieFromWidgetModel(widgetModel, rows[0], chartType)
            rows[0] = tempColumn
        }
        addColumnToRows(rows, tempColumn)
        updateSerieInWidgetModel(widgetModel, tempColumn, chartType)

    }
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
        tempColumn.aggregation = 'SUM'
    }
}

const addColumnToRows = (rows: IWidgetColumn[], tempColumn: IWidgetColumn) => {
    const index = rows.findIndex((column: IWidgetColumn) => column.columnName === tempColumn.columnName)
    if (index === -1) rows.push(tempColumn)
}

// TODO
const updateSerieInWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    if (chartType === 'pie')
        updateFirstSeriesOption(widgetModel.settings.accesssibility.seriesAccesibilitySettings, column)
    updateFirstSeriesOption(widgetModel.settings.series.seriesLabelsSettings, column)
    emitter.emit('seriesAdded', column)
}

const updateFirstSeriesOption = (array: any[], column: IWidgetColumn) => {
    if (array && array[0]) {
        array[0].names[0] = column.columnName
    }
}

export const removeSerieFromWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    const allSeriesOption = chartType !== 'pie'
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
            if (!allSeriesOption && array[i].names === 0) array.splice(i, 1)
        }
    }
}