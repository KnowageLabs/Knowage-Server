import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard";
import { IChartJSChartModel } from "@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget";

export class KnowageChartJS {
    model: IChartJSChartModel

    constructor(model: any, widgetModel: IWidget) {
        this.model = this.createNewChartModel()
    }

    dispatchEvent = (e: any) => {
        // TODO - add mitt ???
        const myCustomEvent = new CustomEvent(e.type, { detail: e });
        document.dispatchEvent(myCustomEvent);
    }
    getModel = () => {
        return this.model;
    }


    createNewChartModel = () => {
        return {
            chart: { type: '' },
            data: {
                labels: ['VueJs', 'EmberJs', 'ReactJs', 'AngularJs'],
                datasets: [
                    {
                        backgroundColor: ['#41B883', '#E46651', '#00D8FF', '#DD1B16'],
                        data: [40, 20, 80, 10]
                    }
                ]
            },
            options: {
                plugins: {
                    title: { display: false },
                    tooltip: {
                        enabled: true,
                        titleColor: '#245425',
                        titleFont: {
                            family: 'Roboto',
                            size: 14,
                            style: 'italic',
                            weight: 'bold'
                        },
                        backgroundColor: '#ff93ff',
                        titleAlign: 'left'
                    },
                    legend: {
                        display: true,
                        align: 'right',
                        position: 'bottom'
                    }
                }
            }
        }

    }
}
