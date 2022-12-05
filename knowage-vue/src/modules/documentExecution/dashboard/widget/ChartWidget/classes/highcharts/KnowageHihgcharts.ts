import { IWidget } from "@/modules/documentExecution/dashboard/Dashboard"
import { HighchartsChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"

export class KnowageHighcharts {
    model: HighchartsChartModel
    cardinality: any[]
    range: any[]

    constructor(model: any, widgetModel: IWidget) {
        this.model = this.createNewChartModel()
        this.cardinality = [],
            this.range = []
    }

    loaded = (event: any) => {
        console.log("chart Loaded", event)
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
            lang: { noData: '' },
            noData: {
                position: {
                    align: '',
                    verticalAlign: ''
                },
                style: {
                    fontFamily: '',
                    fontSize: '',
                    fontWeight: '',
                    color: '',
                    backgroundColor: '',
                }
            },
            chart: {
                options3d: {
                    enabled: false,
                    alpha: 0,
                    beta: 0,
                    viewDistance: 25
                },
                events: {},
                plotBackgroundColor: '',
                plotBorderWidth: '',
                plotShadow: false,
                type: ''
            },
            plotOptions: {
                pie: {
                    depth: 0,
                    allowPointSelect: false,
                    cursor: '',
                    dataLabels: {
                        enabled: false,
                        format: ''
                    }
                }, // move to PIE CHART
                series: []
            },
            series: [],
            settings: {},
            credits: {
                enabled: false
            },
            title: {
                text: ''
            },
            tooltip: {
                pointFormat: ''
            },
            accessibility: {
                point: {
                    valueSuffix: ''
                }
            },

            legend: {
                enabled: false,
                align: '',
                verticalAlign: '',
                layout: '',
            }
        }

    }

}