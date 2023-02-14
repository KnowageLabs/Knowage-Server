import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"

export const formatHighchartsWidgetForSave = (widget: IWidget) => {
    widget.settings.chartModel = widget.settings.chartModel.model
    if (!widget.settings.chartModel) return
    removeChartData(widget.settings.chartModel)
    formatPiePlotOptions(widget.settings.chartModel)
    formatLegendSettings(widget.settings.chartModel)
    formatTooltipSettings(widget.settings.chartModel)
}

const removeChartData = (chartModel: IHighchartsChartModel) => {
    chartModel.series = []
}

const formatPiePlotOptions = (chartModel: IHighchartsChartModel) => {
    if (!chartModel.plotOptions.pie) return
    delete chartModel.plotOptions.pie.dataLabels.formatterError
}

const formatLegendSettings = (chartModel: IHighchartsChartModel) => {
    delete chartModel.legend.labelFormatterError
}

const formatTooltipSettings = (chartModel: IHighchartsChartModel) => {
    delete chartModel.tooltip.formatterError
    delete chartModel.tooltip.pointFormatterError
}