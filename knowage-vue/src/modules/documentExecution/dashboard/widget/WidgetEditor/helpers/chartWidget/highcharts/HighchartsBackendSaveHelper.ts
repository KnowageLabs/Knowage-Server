import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"

export const formatHighchartsWidgetForSave = (widget: IWidget) => {
    console.log(">>>>>>>> FOPRMAT WIDGET FOR SAVE: ", widget)
    widget.settings.chartModel = widget.settings.chartModel.getModel()
}