import { IHighchartsChartModel, IHighchartsOptions3D } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getFormattedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'


export const updatePieChartModel = (oldModel: any, newModel: IHighchartsChartModel) => {
    getFormatted3DConfiguration(oldModel, newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getFormattedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel, 1)
    getFormattedTooltipSettings(oldModel, newModel)

    return newModel
}

const getFormatted3DConfiguration = (oldModel: any, newModel: IHighchartsChartModel) => {
    if (oldModel.CHART.show3D) {
        if (newModel.plotOptions.pie) newModel.plotOptions.pie.depth = oldModel.CHART.depth
        newModel.chart.options3d = {
            enabled: oldModel.CHART.show3D,
            alpha: oldModel.CHART.alpha,
            beta: oldModel.CHART.beta,
            viewDistance: oldModel.CHART.viewDistance ?? 25
        } as IHighchartsOptions3D
    }
}
