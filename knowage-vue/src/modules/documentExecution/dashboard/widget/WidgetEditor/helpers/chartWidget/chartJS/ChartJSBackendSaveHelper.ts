import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { IChartJSChartModel } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget"

export const formatChartJSForSave = (widget: IWidget) => {
    widget.settings.chartModel = widget.settings.chartModel.model
    if (!widget.settings.chartModel) return
    removeChartData(widget.settings.chartModel)

}

const removeChartData = (chartModel: IChartJSChartModel) => {
    chartModel.data = { datasets: [], labels: [] }
}
