import { IWidget, IWidgetColumn, IWidgetCrossNavigation, IWidgetInteractionParameter } from "../../../Dashboard"
import { IChartInteractionValues, IChartJSData } from "../../../interfaces/chartJS/DashboardChartJSWidget"


export const formatForCrossNavigation = (chartSelectionEvent: any, widgetModel: IWidget, chartData: IChartJSData, dataToShow: any) => {
    const crossNavigationOptions = widgetModel.settings.interactions.crossNavigation as IWidgetCrossNavigation
    const datasetIndex = chartSelectionEvent.datasetIndex
    const index = chartSelectionEvent.index
    const categoryValue = chartData.labels[index]
    const serieValue = chartData.datasets[datasetIndex].data[index]
    const serieName = getSerieName(widgetModel)
    const formattedChartValues = getFormattedChartValues('' + serieValue, categoryValue, serieName, dataToShow)
    const formattedOutputParameters = getFormattedOutputParameters(formattedChartValues, crossNavigationOptions.parameters)
    return formattedOutputParameters
}

const getSerieName = (widgetModel: IWidget) => {
    const index = widgetModel.columns.findIndex((column: IWidgetColumn) => column.fieldType === "MEASURE")
    return index !== -1 ? widgetModel.columns[index].columnName : ''
}

const getFormattedChartValues = (serieValue: string, categoryValue: string, serieName: string, dataToShow: any) => {
    const dataToShowFields = dataToShow?.metaData?.fields ?? []
    const categoryName = dataToShowFields[1] ? dataToShowFields[1].header : ''
    return { serieName: serieName, serieValue: serieValue, categoryName: categoryName, categoryValue: categoryValue }
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
    }
    return { ...outputParameter, value: value }
}