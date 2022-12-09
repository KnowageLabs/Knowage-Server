import { IWidget, IWidgetColumn } from "@/modules/documentExecution/dashboard/Dashboard";
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'

export const addHighchartsColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, attributesOnly: boolean, measuresOnly: boolean, widgetModel: IWidget) => {
    let mode = ''
    if (attributesOnly) mode = 'attributesOnly'
    else if (measuresOnly) mode = 'measuresOnly'
    switch (chartType) {
        case 'highchartsPieChart':
            addHighchartsPieChartColumnToTable(tempColumn, rows, chartType, mode, widgetModel)
    }
}

const addHighchartsPieChartColumnToTable = (tempColumn: IWidgetColumn, rows: IWidgetColumn[], chartType: string | undefined, mode: string, widgetModel: IWidget) => {
    console.log('----- add column: ', tempColumn)
    console.log('----- add column mode: ', mode)
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
        updateSerieInWidgetModel(widgetModel, tempColumn, chartType)
        emitter.emit('seriesAdded', tempColumn)
    }
}

const updateSerieInWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    const allSeriesOption = chartType !== 'highchartsPieChart'
    if (widgetModel.settings.accesssibility.seriesAccesibilitySettings && widgetModel.settings.accesssibility.seriesAccesibilitySettings[0]) {
        const firstAccessibilitySettings = widgetModel.settings.accesssibility.seriesAccesibilitySettings[0]
        console.log(">>>>>>>>>> TEEEEEEEEEST: ", firstAccessibilitySettings)

        if (!allSeriesOption) firstAccessibilitySettings.names[0] = column.columnName
    }
}

export const removeSerieFromWidgetModel = (widgetModel: IWidget, column: IWidgetColumn, chartType: string | undefined) => {
    console.log(">>>>>>>> REMOVE FROM WIDGET serie: ", widgetModel)
    console.log(">>>>>>>> REMOVE FROM WIDGET MODEL: ", column)
    const allSeriesOption = chartType !== 'highchartsPieChart'
    removeColumnFromSubmodel(column, widgetModel.settings.accesssibility.seriesAccesibilitySettings, allSeriesOption)
    removeColumnFromSubmodel(column, widgetModel.settings.series.seriesLabelsSettings, allSeriesOption)
    emitter.emit('seriesRemoved', column)
}

const removeColumnFromSubmodel = (column: IWidgetColumn, array: any[], allSeriesOption: boolean) => {
    console.log(">>>>>>>>>>>>>>>>>>>>> TEST: ", array)
    for (let i = array.length - 1; i >= 0; i--) {
        for (let j = array[i].names.length; j >= 0; j--) {
            const serieName = array[i].names[j]
            console.log(serieName + ' === ' + column.columnName)
            if (serieName === column.columnName) {
                console.log('Entered: ', array)
                array[i].names.splice(j, 1)
            }
            if (!allSeriesOption && array[i].names === 0) array.splice(i, 1)
        }
    }
}