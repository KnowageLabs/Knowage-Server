import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedBandsSettings, getFormattedPaneSettings, getFormattedScaleSettings, getFormattedTickSettings } from './KnowageHighchartsCommonGaugeUpdater'
import { getFormattedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'

export const updateGaugeChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    console.log(">>>>>>> OLD MODEL: ", oldModel)
    console.log(">>>>>>> NEW MODEL: ", newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel, null)
    getFormattedTooltipSettings(oldModel, newModel)
    getFormattedPaneSettings(oldModel, newModel, 'gauge')
    if (oldModel.CHART.AXES_LIST && oldModel.CHART.AXES_LIST.AXIS && oldModel.CHART.AXES_LIST.AXIS[0]) {
        getFormattedScaleSettings(oldModel, newModel)
        getFormattedTickSettings(oldModel, newModel)
        getFormattedBandsSettings(oldModel, newModel)
    }

    return newModel
}
