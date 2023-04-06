import { IWidgetCrossNavigation, IWidgetInteractionParameter } from "../../../Dashboard";
import { IChartInteractionValues } from "../../../interfaces/chartJS/DashboardChartJSWidget";

export const formatForCrossNavigation = (chartEvent: any, crossNavigationOptions: IWidgetCrossNavigation, dataToShow: any, chartType: string) => {
    console.log('--------- CHART EVENT: ', chartEvent)
    console.log('--------- chartType: ', chartType)
    console.log('--------- crossNavigationOptions: ', crossNavigationOptions)
    const formattedChartValues = getFormattedChartValues(chartEvent, dataToShow, chartType)

    const formattedOutputParameters = getFormattedOutputParameters(formattedChartValues, crossNavigationOptions.parameters)
    return formattedOutputParameters

}

const getFormattedChartValues = (chartEvent: any, dataToShow: any, chartType: string) => {
    const categoryName = dataToShow?.metaData?.fields[1] ? dataToShow.metaData.fields[1].header : ''
    const chartPoint = chartEvent.point
    const formattedChartValues = { serieName: chartPoint.series.name, serieValue: chartType === 'pie' ? chartPoint.options.y : chartPoint.options.value, categoryName: categoryName, categoryValue: chartPoint.options.name } as IChartInteractionValues
    if (chartType === 'heatmap') {
        const groupingName = dataToShow?.metaData?.fields[2] ? dataToShow.metaData.fields[2].header : ''
        formattedChartValues.groupingName = groupingName
        formattedChartValues.groupingValue = chartPoint.options.groupingValue
    }
    console.log('--------- formattedChartValues: ', formattedChartValues)
    return formattedChartValues
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
        case "GROUPING_NAME":
            value = formattedChartValues.groupingName as string;
            break
        case "GROUPING_VALUE":
            value = formattedChartValues.groupingValue as string;
            break
    }
    return { ...outputParameter, value: value }
}