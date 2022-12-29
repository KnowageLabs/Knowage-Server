import { IChartJSChartModel } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget";
import * as  chartJSDefaultValues from "../../../WidgetEditor/helpers/chartWidget/chartJS/ChartJSDefaultValues"

export class KnowageChartJS {
    model: IChartJSChartModel

    constructor() {
        this.model = this.createNewChartModel()
    }

    createNewChartModel() {
        return {
            chart: { type: '' },
            data: {
                labels: [],
                datasets: [{ backgroundColor: [], data: [] }]
            },
            options: {
                events: [],
                onClick: {},
                plugins: {
                    title: { display: false },
                    tooltip: chartJSDefaultValues.getDefaultTooltipSettings(),
                    legend: chartJSDefaultValues.getDefaultLegendSettings()
                }
            }
        }

    }
}
