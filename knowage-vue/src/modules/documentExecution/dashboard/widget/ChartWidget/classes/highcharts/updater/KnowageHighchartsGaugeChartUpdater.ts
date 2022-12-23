import { IHighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import { getForamttedLabels, getFormattedLegend, getFormattedNoDataConfiguration, getFormattedSeries, getFormattedTooltipSettings } from './KnowageHighchartsCommonUpdater'

export const updateGaugeChartModel = (oldModel: any, newModel: IHighchartsPieChartModel) => {
    console.log(">>>>>>> OLD MODEL: ", oldModel)
    console.log(">>>>>>> NEW MODEL: ", newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)
    getFormattedLegend(oldModel, newModel)
    getForamttedLabels(oldModel, newModel)
    getFormattedSeries(oldModel, newModel)
    getFormattedTooltipSettings(oldModel, newModel)

    return newModel
}

export const createSerie = (serieName: string, groupingFunction: string) => {
    return {
        name: serieName,
        colorByPoint: true,
        groupingFunction: groupingFunction,
        data: [],
        accessibility: {
            enabled: false,
            description: '',
            exposeAsGroupOnly: false,
            keyboardNavigation: { enabled: false }
        }
    }
}