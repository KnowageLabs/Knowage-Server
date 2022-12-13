import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard";
import { IChartJSChartModel } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget";
import * as  chartJSDefaultValues from "../../../WidgetEditor/helpers/chartWidget/chartJS/ChartJSDefaultValues"

export class KnowageChartJS {
    model: IChartJSChartModel

    constructor(model: any, widgetModel: IWidget) {
        this.model = this.createNewChartModel()
    }

    getModel = () => {
        return this.model;
    }

    createNewChartModel = () => {
        return {
            chart: { type: '' },
            data: {
                labels: [],
                datasets: [{ backgroundColor: [], data: [] }]
            },
            options: {
                plugins: {
                    title: { display: false },
                    tooltip: chartJSDefaultValues.getDefaultTooltipSettings(),
                    legend: chartJSDefaultValues.getDefaultLegendSettings()
                }
            }
        }

    }
}
