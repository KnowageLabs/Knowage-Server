import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import deepcopy from "deepcopy";

export const addHighchartsColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'IHighchartsPieChart':
            addIHighchartsPieChartColumnToTable(tempColumn, rows, chartType, mode, widgetModel)
    }
}

const addIHighchartsPieChartColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, mode: string, widgetModel: IWidget) => {
    if (mode === 'attributesOnly' && rows.length < 4) {
        const index = rows.findIndex((column: IWidgetColumn) => column.columnName === tempColumn.columnName)
        if (tempColumn.fieldType === 'MEASURE') {
            tempColumn.fieldType = 'ATTRIBUTE'
            tempColumn.aggregation = ''
        }
        if (index === -1) rows.push(tempColumn)
    } else if (mode === 'measuresOnly' && rows.length <= 1) {
        if (tempColumn.fieldType === 'ATTRIBUTE') {
            tempColumn.fieldType = 'MEASURE'
            tempColumn.aggregation = 'SUM'
        }
        if (rows.length === 1) {
            removeSerieFromWidgetModel(widgetModel, rows[0], chartType)
        }
        rows[0] = tempColumn
        if (chartType === 'IHighchartsPieChart') updateSerieInWidgetModel(widgetModel, tempColumn)

    }
}

const updateSerieInWidgetModel = (widgetModel: IWidget, column: IWidgetColumn) => {
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
    const allSeriesOption = chartType !== 'IHighchartsPieChart'
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