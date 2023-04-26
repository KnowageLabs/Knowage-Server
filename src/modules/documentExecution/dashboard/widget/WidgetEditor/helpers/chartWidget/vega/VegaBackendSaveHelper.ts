import { IVegaChartsModel } from './../../../../../interfaces/vega/VegaChartsWidget.d';
import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"

export const formatVegaForSave = (widget: IWidget) => {
    widget.settings.chartModel = widget.settings.chartModel.model
    if (!widget.settings.chartModel) return
    removeChartData(widget.settings.chartModel)

}

const removeChartData = (chartModel: IVegaChartsModel) => {
    if (!chartModel.data[0]) return
    chartModel.data[0].values = []
}
