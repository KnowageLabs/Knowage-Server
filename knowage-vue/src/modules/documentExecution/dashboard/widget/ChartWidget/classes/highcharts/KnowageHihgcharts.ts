import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { IHighchartsChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"
import * as  highchartsDefaultValues from "../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues"

export class KnowageHighcharts {
    model: IHighchartsChartModel
    cardinality: any[]
    range: any[]

    constructor(model: any, widgetModel: IWidget) {
        this.model = this.createNewChartModel()
        this.cardinality = [],
            this.range = []
    }

    initializeEventsDispatcher = () => {
        // TODO - add mitt
        if (this.model.settings.drilldown) this.model.chart.events.drilldown = this.dispatchEvent
    }

    updateCardinality = async (data: any) => {
        let cardinalityObj = {}
        this.model.settings.categories.forEach(category => {
            let tempCategory = data.metaData.fields.filter((i) => i.header === category)
            if (tempCategory.length > 0) {
                cardinalityObj[tempCategory[0].name] = {
                    "category": category,
                    "set": new Set()
                }
            }
        });
        await data.rows.forEach((row: any, index: number) => {
            for (let k in cardinalityObj) {
                if (row[k]) cardinalityObj[k].set.add(row[k])
            }
        })
        this.cardinality = []
        for (let i in cardinalityObj) {
            this.cardinality.push({ [cardinalityObj[i].category]: cardinalityObj[i].set.size })
        }
        return this.cardinality
    }


    public getModel = () => {
        return this.model;
    }

    getCardinality = () => {
        return this.range
    }

    getRange = () => {
        return this.range
    }

    dispatchEvent = (e: any) => {
        var myCustomEvent = new CustomEvent(e.type, { detail: e });
        document.dispatchEvent(myCustomEvent);
    }

    valueFormatter = (value: any, type: string) => {
        console.log(">>>>>>> valueFormatter - value: ", value, ', type: ', type)
        switch (type) {
            case 'float':
                new Intl.NumberFormat('it-IT', { notation: 'compact', minimumFractionDigits: 2, maximumFractionDigits: 2, }).format(value)
        }
        //scale factor
        //number formatter
        //date formatter
    }

    createNewChartModel = () => {
        return {
            title: '',
            lang: { noData: '' },
            chart: {
                options3d: highchartsDefaultValues.getDefault3DOptions(),
                events: {}, // TODO
                type: ''
            },
            noData: highchartsDefaultValues.getDefaultNoDataConfiguration(),
            accessibility: highchartsDefaultValues.getDefaultAccessibilitySettings(),
            series: [],
            settings: {
                drilldown: {}, // TODO
                categories: [] // TODO
            },
            plotOptions: {},
            legend: highchartsDefaultValues.getDefaultLegendSettings(),
            tooltip: highchartsDefaultValues.getDefaultTooltipSettings(),
            credits: { enabled: false }
        }

    }

}