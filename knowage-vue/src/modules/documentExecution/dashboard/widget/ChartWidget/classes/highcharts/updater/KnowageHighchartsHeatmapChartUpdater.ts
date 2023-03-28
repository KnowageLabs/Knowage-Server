import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'

export const updateHeatmapChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log('-------- OLD CHART MODEL: ', oldModel)
    //getFormattedNoDataConfiguration(oldModel, newModel)
    //getFormattedLegend(oldModel, newModel)
    //getFormattedLabels(oldModel, newModel)
    //getFormattedSeries(oldModel, newModel, 1)
    //getFormattedTooltipSettings(oldModel, newModel)
    console.log('-------- NEW CHART MODEL: ', newModel)

    return newModel
}
