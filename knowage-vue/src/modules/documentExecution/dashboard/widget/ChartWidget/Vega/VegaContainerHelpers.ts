import { IWidget, IWidgetCrossNavigation, IWidgetColumn, IWidgetInteractionParameter } from './../../../Dashboard.d';
import { IChartInteractionValues } from './../../../interfaces/vega/VegaChartsWidget.d';

export const formatForCrossNavigation = (event: any, widgetModel: IWidget) => {
    const crossNavigationOptions = widgetModel.settings.interactions.crossNavigation as IWidgetCrossNavigation
    const formattedChartValues = { serieName: '', serieValue: event.count, categoryName: '', categoryValue: event.textValue, categoryId: '' }
    const category = getColumnFromWidgetModel(widgetModel, 'ATTRIBUTE')
    if (category) {
        formattedChartValues.categoryId = category.id ?? ''
        formattedChartValues.categoryName = category.columnName
    }
    const serie = getColumnFromWidgetModel(widgetModel, 'MEASURE')
    if (serie) formattedChartValues.serieName = serie.columnName

    const formattedOutputParameters = getFormattedOutputParameters(formattedChartValues, crossNavigationOptions.parameters)
    return formattedOutputParameters
}

const getColumnFromWidgetModel = (widgetModel: IWidget, columnType: 'ATTRIBUTE' | 'MEASURE') => {
    const index = widgetModel.columns.findIndex((column: IWidgetColumn) => column.fieldType === columnType)
    return index !== -1 ? widgetModel.columns[index] : ''
}

const getFormattedOutputParameters = (formattedChartValues: IChartInteractionValues, outputParameters: IWidgetInteractionParameter[]) => {
    const formattedOutputParameters = [] as IWidgetInteractionParameter[]
    outputParameters.forEach((outputParameter: IWidgetInteractionParameter) => {
        if (outputParameter.type === 'dynamic') {
            formattedOutputParameters.push(getFormattedDynamicOutputParameter(formattedChartValues, outputParameter))
        } else {
            formattedOutputParameters.push(outputParameter)
        }
    })
    return formattedOutputParameters
}

const getFormattedDynamicOutputParameter = (formattedChartValues: IChartInteractionValues, outputParameter: IWidgetInteractionParameter) => {
    let value = '' as string
    switch (outputParameter.column) {
        case "SERIE_NAME":
            value = formattedChartValues.serieName;
            break
        case "SERIE_VALUE":
            value = formattedChartValues.serieValue;
            break
        case "CATEGORY_NAME":
            value = formattedChartValues.categoryName;
            break
        case "CATEGORY_VALUE":
            value = formattedChartValues.categoryValue;
            break
        case "CATEGORY_ID":
            value = formattedChartValues.categoryId;
            break
    }
    return { ...outputParameter, value: value }
}