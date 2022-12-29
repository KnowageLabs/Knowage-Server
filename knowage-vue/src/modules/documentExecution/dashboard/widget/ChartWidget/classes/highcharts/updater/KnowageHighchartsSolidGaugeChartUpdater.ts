import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedTickSettings } from './KnowageHighchartsCommonGaugeUpdater'
import { getFormattedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'

export const updateSolidGaugeChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log(">>>>>>> OLD MODEL: ", oldModel)
    console.log(">>>>>>> NEW MODEL: ", newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel, 4)
    getFormattedTooltipSettings(oldModel, newModel)
    if (oldModel.CHART.AXES_LIST && oldModel.CHART.AXES_LIST.AXIS && oldModel.CHART.AXES_LIST.AXIS[0]) {
        getFormattedTickSettings(oldModel, newModel)
    }

    return newModel
}