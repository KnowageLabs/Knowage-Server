import { IWidgetCrossNavigation, IWidgetInteractionParameter } from "../../../Dashboard";
import { IChartInteractionValues } from "../../../interfaces/chartJS/DashboardChartJSWidget";

export const formatForCrossNavigation = (chartEvent: any, crossNavigationOptions: IWidgetCrossNavigation, dataToShow: any) => {
    const formattedChartValues = getFormattedChartValues(chartEvent, dataToShow)
    const formattedOutputParameters = getFormattedOutputParameters(formattedChartValues, crossNavigationOptions.parameters)
    return formattedOutputParameters

}

const getFormattedChartValues = (chartEvent: any, dataToShow: any) => {
    const categoryName = dataToShow?.metaData?.fields[1] ? dataToShow.metaData.fields[1].header : ''
    const chartPoint = chartEvent.point
    return { serieName: chartPoint.series.name, serieValue: chartPoint.options.y, categoryName: categoryName, categoryValue: chartPoint.options.name }
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
    let value = ''
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