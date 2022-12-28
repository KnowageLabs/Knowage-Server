import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedBandsSettings, getFormattedPaneSettings, getFormattedScaleSettings, getFormattedTickSettings } from './KnowageHighchartsCommonGaugeUpdater'
import { getForamttedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'

export const updateActivityGaugeChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log(">>>>>>> OLD MODEL: ", oldModel)
    console.log(">>>>>>> NEW MODEL: ", newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getForamttedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel)
    getFormattedTooltipSettings(oldModel, newModel)
    getFormattedPaneSettings(oldModel, newModel)
    if (oldModel.CHART.AXES_LIST && oldModel.CHART.AXES_LIST.AXIS && oldModel.CHART.AXES_LIST.AXIS[0]) {
        getFormattedTickSettings(oldModel, newModel)
    }

    return newModel
}