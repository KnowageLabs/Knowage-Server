<template>
    <div v-show="!error" :id="chartID" style="width: 100%; height: 100%; margin: 0 auto"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { ISelection, IWidget, IWidgetColumn } from '../../../Dashboard'
import { IHighchartsChartModel } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import { mapActions } from 'pinia'
import { updateStoreSelections } from '../../interactionsHelpers/InteractionHelper'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import HighchartsMore from 'highcharts/highcharts-more'
import HighchartsSolidGauge from 'highcharts/modules/solid-gauge'
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'
import SeriesLabel from 'highcharts/modules/series-label'
import cryptoRandomString from 'crypto-random-string'
import store from '../../../Dashboard.store'
import deepcopy from 'deepcopy'

HighchartsMore(Highcharts)
HighchartsSolidGauge(Highcharts)
Accessibility(Highcharts)
NoDataToDisplay(Highcharts)
SeriesLabel(Highcharts)
Highcharts3D(Highcharts)

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Object as any, required: true },
        dashboardId: { type: String, required: true },
        editorMode: { type: Boolean },
        propActiveSelections: {
            type: Array as PropType<ISelection[]>,
            required: true
        }
    },
    data() {
        return {
            chartID: cryptoRandomString({ length: 16, type: 'base64' }),
            chartModel: {} as IHighchartsChartModel,
            error: false,
            highchartsInstance: {} as any
        }
    },
    watch: {
        dataToShow() {
            this.onRefreshChart()
        }
    },
    mounted() {
        this.setEventListeners()
        this.onRefreshChart()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(store, ['setSelections', 'getDatasetLabel']),
        setEventListeners() {
            emitter.on('refreshChart', this.onRefreshChart)
            emitter.on('chartWidgetResized', this.resizeChart)
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
            emitter.off('chartWidgetResized', this.resizeChart)
        },
        onRefreshChart(widgetId: any | null = null) {
            if (widgetId && widgetId !== this.widgetModel.id) return
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.updateChartModel()
        },
        updateChartModel() {
            Highcharts.setOptions({ lang: { noData: this.chartModel.lang.noData } })

            this.widgetModel.settings.chartModel.setData(this.dataToShow, this.widgetModel)

            this.widgetModel.settings.chartModel.updateSeriesAccessibilitySettings(this.widgetModel)
            this.widgetModel.settings.chartModel.updateSeriesLabelSettings(this.widgetModel)
            this.updateDataLabels()
            this.error = this.updateLegendSettings()
            if (this.error) return
            this.error = this.updateTooltipSettings()
            if (this.error) return

            this.widgetModel.settings.chartModel.updateChartColorSettings(this.widgetModel)

            this.setSeriesEvents()

            const modelToRender = this.getModelForRender()
            console.log('>>>>>>> CHART TO RENDER: ', modelToRender)
            const temp = {
                chart: {
                    additionalData: {
                        categoriesCardinality: [],
                        range: [
                            {
                                serie: 'units_ordered',
                                min: 441,
                                max: 441
                            },
                            {
                                serie: 'units_shipped',
                                min: 239,
                                max: 239
                            },
                            {
                                serie: 'supply_time',
                                min: 1163.9615,
                                max: 1163.9615
                            }
                        ]
                    },
                    type: 'solidgauge',
                    subtype: 'activity',
                    alignTicks: false,
                    backgroundColor: '#FFFFFF',
                    heightDimType: 'pixels',
                    widthDimType: 'pixels',
                    plotBackgroundColor: null,
                    plotBorderWidth: 0,
                    plotShadow: false
                },
                title: {
                    text: '',
                    style: {
                        align: '',
                        color: '',
                        fontFamily: '',
                        fontWeight: '',
                        fontSize: ''
                    }
                },
                pane: {
                    startAngle: -90,
                    endAngle: 90,
                    background: [
                        {
                            outerRadius: '100%',
                            innerRadius: '75%',
                            backgroundColor: 'rgba(4,45,87,0.3)'
                        },
                        {
                            outerRadius: '74%',
                            innerRadius: '49%',
                            backgroundColor: 'rgba(7,83,160,0.3)'
                        },
                        {
                            outerRadius: '48%',
                            innerRadius: '23%',
                            backgroundColor: 'rgba(10,121,233,0.3)'
                        }
                    ]
                },
                yAxis: [
                    {
                        min: 2,
                        max: 500,
                        title: {
                            text: '',
                            style: {
                                color: '#1430CB',
                                fontFamily: 'Roboto',
                                fontWeight: 'bold',
                                fontSize: '18px'
                            },
                            align: 'high'
                        },
                        lineColor: '#19C7B4',
                        tickPosition: 'inside',
                        tickColor: '#723BE2',
                        minorTickColor: '#0D8849',
                        offset: 0,
                        lineWidth: 3,
                        minorTickLength: 5,
                        minorTickWidth: 3,
                        tickWidth: 4,
                        tickLength: 5,
                        endOnTick: true,
                        minorTickInterval: 4,
                        minorTickPosition: 'outside',
                        tickPixelInterval: 2,
                        labels: {
                            distance: 4,
                            rotation: 30,
                            style: {
                                rotate: '24',
                                color: '#890ED8',
                                fontFamily: 'Arial',
                                fontWeight: 'bold',
                                fontSize: '14px'
                            },
                            align: 'right'
                        },
                        plotBands: [
                            {
                                from: 20,
                                to: 40,
                                color: '#D3148C'
                            },
                            {
                                from: 40,
                                to: 60,
                                color: '#35C693'
                            }
                        ],
                        plotLines: [
                            {
                                color: '#5DE1D8',
                                dashStyle: 'Solid',
                                value: '3',
                                width: 4
                            }
                        ]
                    }
                ],
                series: [
                    {
                        name: 'UNITS_ORDERED',
                        color: '#042d57',
                        yAxis: 0,
                        data: [
                            {
                                color: '#042d57',
                                radius: '100%',
                                innerRadius: '75%',
                                y: 441
                            }
                        ],
                        dataLabels: {
                            enabled: true,
                            color: '#042d57',
                            y: 40
                        },
                        tooltip: {
                            valuePrefix: 'Prefix',
                            valueSuffix: 'Suffix',
                            valueScaleFactor: 'empty',
                            valueDecimals: '3',
                            ttBackColor: '#D6D6D6'
                        },
                        dial: {
                            backgroundColor: '#042d57',
                            radius: '100%'
                        }
                    },
                    {
                        name: 'UNITS_SHIPPED',
                        color: '#0753a0',
                        yAxis: 0,
                        data: [
                            {
                                color: '#0753a0',
                                radius: '74%',
                                innerRadius: '49%',
                                y: 239
                            }
                        ],
                        dataLabels: {
                            enabled: true,
                            color: '#0753a0',
                            y: 80
                        },
                        tooltip: {
                            valuePrefix: 'Prefix',
                            valueSuffix: 'Suffix',
                            valueScaleFactor: 'empty',
                            valueDecimals: '4',
                            ttFontWeight: 'bold',
                            ttBackColor: '#649D29',
                            ttColor: '#31DDD1',
                            ttAlign: 'center',
                            ttFont: 'Arial',
                            ttFontSize: '18px'
                        },
                        dial: {
                            backgroundColor: '#0753a0',
                            radius: '100%'
                        }
                    },
                    {
                        name: 'SUPPLY_TIME',
                        color: '#0a79e9',
                        yAxis: 0,
                        data: [
                            {
                                color: '#0a79e9',
                                radius: '48%',
                                innerRadius: '23%',
                                y: 1163.9615
                            }
                        ],
                        dataLabels: {
                            enabled: true,
                            color: '#0a79e9',
                            y: 120
                        },
                        tooltip: {
                            valuePrefix: '',
                            valueSuffix: '',
                            valueScaleFactor: 'empty',
                            valueDecimals: '0',
                            ttFontWeight: 'bold',
                            ttBackColor: '#DD9595',
                            ttColor: '#E1A216',
                            ttAlign: 'right',
                            ttFont: 'Times New Roman',
                            ttFontSize: '12px'
                        },
                        dial: {
                            backgroundColor: '#0a79e9',
                            radius: '100%'
                        }
                    }
                ],
                tooltip: {
                    borderWidth: 0,
                    borderRadius: 0,
                    useHTML: true,
                    backgroundColor: null,
                    style: {
                        padding: 0
                    }
                },
                plotOptions: {
                    solidgauge: {
                        dataLabels: {
                            enabled: false
                        },
                        rounded: true
                    },
                    series: {
                        turboThreshold: 0
                    }
                },
                credits: {
                    enabled: false
                }
            }

            const myTemp = {
                title: '',
                lang: {
                    noData: 'No data message'
                },
                chart: {
                    options3d: {
                        alpha: 0,
                        beta: 0,
                        enabled: false,
                        viewDistance: 25
                    },
                    type: 'solidgauge'
                },
                noData: {
                    position: {
                        align: 'right',
                        verticalAlign: 'middle'
                    },
                    style: {
                        fontFamily: 'Roboto',
                        fontSize: '10px',
                        fontWeight: 'bold',
                        color: 'rgba(189,168,242,1)',
                        backgroundColor: ''
                    }
                },
                accessibility: {
                    description: '',
                    enabled: false,
                    keyboardNavigation: {
                        enabled: false,
                        order: []
                    }
                },
                series: [
                    {
                        name: 'UNITS_ORDERED',
                        data: [
                            {
                                name: 'UNITS_ORDERED',
                                y: 441,
                                radius: '112%',
                                innerRadius: '88%'
                            }
                        ],
                        colorByPoint: false,
                        groupingFunction: 'COUNT',
                        accessibility: {
                            enabled: false,
                            description: '',
                            exposeAsGroupOnly: false,
                            keyboardNavigation: {
                                enabled: false
                            }
                        },
                        dial: {
                            backgroundColor: 'rgba(4,45,87,1)',
                            baseWidth: 3,
                            radius: '80%'
                        },
                        pivot: {
                            backgroundColor: 'rgba(4,45,87,1)',
                            radius: 5
                        }
                    },
                    {
                        name: 'UNITS_SHIPPED',
                        data: [
                            {
                                name: 'UNITS_SHIPPED',
                                y: 239,
                                radius: '87%',
                                innerRadius: '63%'
                            }
                        ],
                        colorByPoint: false,
                        groupingFunction: 'MIN',
                        accessibility: {
                            enabled: false,
                            description: '',
                            exposeAsGroupOnly: false,
                            keyboardNavigation: {
                                enabled: false
                            }
                        },
                        dial: {
                            backgroundColor: 'rgba(7,83,160,1)',
                            baseWidth: 3,
                            radius: '80%'
                        },
                        pivot: {
                            backgroundColor: 'rgba(7,83,160,1)',
                            radius: 5
                        }
                    },
                    {
                        name: 'SUPPLY_TIME',
                        data: [
                            {
                                name: 'SUPPLY_TIME',
                                y: 1163.9615,
                                radius: '62%',
                                innerRadius: '38%'
                            }
                        ],
                        colorByPoint: false,
                        groupingFunction: 'SUM',
                        accessibility: {
                            enabled: false,
                            description: '',
                            exposeAsGroupOnly: false,
                            keyboardNavigation: {
                                enabled: false
                            }
                        },
                        dial: {
                            backgroundColor: 'rgba(10,121,233,1)',
                            baseWidth: 3,
                            radius: '80%'
                        },
                        pivot: {
                            backgroundColor: 'rgba(10,121,233,1)',
                            radius: 5
                        }
                    }
                ],
                settings: {
                    drilldown: {},
                    categories: []
                },
                plotOptions: {
                    series: {
                        events: {}
                    },
                    solidgauge: {
                        allowPointSelect: false,
                        colors: [],
                        cursor: '',
                        dataLabels: {
                            backgroundColor: 'rgba(194,194,194, 1)',
                            distance: 30,
                            enabled: false,
                            position: '',
                            style: {
                                color: '',
                                fontFamily: '',
                                fontSize: '14px',
                                fontWeight: ''
                            }
                        },
                        depth: 100,
                        linecap: 'round',
                        rounded: true,
                        showInLegend: true,
                        stickyTracking: false
                    }
                },
                legend: {
                    align: 'center',
                    backgroundColor: '',
                    borderColor: '',
                    borderWidth: 1,
                    enabled: true,
                    itemStyle: {
                        color: '',
                        fontFamily: '',
                        fontSize: '',
                        fontWeight: ''
                    },
                    layout: 'horizontal',
                    verticalAlign: 'top',
                    labelFormatterError: ''
                },
                tooltip: {
                    backgroundColor: 'none',
                    borderWidth: 0,
                    pointFormat: '{series.name}<br><span style="font-size:1em; color: {point.color}; font-weight: bold;  text-align: center;">{point.y}</span>',
                    shadow: false,
                    style: {
                        fontSize: '16px'
                    },
                    valueSuffix: '',
                    formatterError: '',
                    pointFormatterError: ''
                },
                colors: ['rgba(4,45,87,1)', 'rgba(7,83,160,1)', 'rgba(10,121,233,1)', 'rgba(72,159,247,1)', 'rgba(145,197,250,1)', 'rgba(248,70,138,1)', 'rgba(121,133,155,1)', 'rgba(165,173,188,1)', 'rgba(209,213,221,1)', 'rgba(59,2,24,1)', 'rgba(133,5,54,1)', 'rgba(207,8,84,1)'],
                credits: {
                    enabled: false
                },
                pane: {
                    center: ['50%', '50%'],
                    endAngle: 360,
                    startAngle: 0
                },
                yAxis: {
                    lineWidth: 0,
                    max: null,
                    min: null,
                    minorTickInterval: null,
                    plotBands: [],
                    tickColor: '',
                    tickLength: 0,
                    tickPosition: '',
                    tickWidth: 0
                }
            }
            this.highchartsInstance = Highcharts.chart(this.chartID, modelToRender as any)
            // this.highchartsInstance = Highcharts.chart(this.chartID, myTemp as any)
            this.highchartsInstance.reflow()
        },
        updateLegendSettings() {
            if (this.chartModel.plotOptions.pie) this.chartModel.plotOptions.pie.showInLegend = true
            if (this.chartModel.plotOptions.gauge) this.chartModel.plotOptions.gauge.showInLegend = true
            return this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
        },
        updateDataLabels() {
            const dataLabels = this.chartModel.plotOptions && this.chartModel.plotOptions[this.chartModel.chart.type] ? this.chartModel.plotOptions[this.chartModel.chart.type].dataLabels : null
            if (dataLabels) {
                this.error = this.widgetModel.settings.chartModel.updateFormatterSettings(dataLabels, 'format', 'formatter', 'formatterText', 'formatterError')
                if (this.error) return
            }
        },
        updateTooltipSettings() {
            let hasError = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.tooltip, null, 'formatter', 'formatterText', 'formatterError')
            if (hasError) return hasError
            hasError = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.tooltip, null, 'pointFormatter', 'pointFormatterText', 'pointFormatterError')
            return hasError
        },
        setSeriesEvents() {
            if (this.chartModel.plotOptions.series) {
                this.chartModel.plotOptions.series.events = {
                    click: this.setSelection
                }
            } else
                this.chartModel.plotOptions.series = {
                    events: { click: this.setSelection }
                }
        },
        setSelection(event: any) {
            if (this.editorMode || !this.widgetModel.settings.interactions.selection || !this.widgetModel.settings.interactions.selection.enabled) return
            const serieClicked = event.point?.options
            if (!serieClicked || !serieClicked.name) return
            updateStoreSelections(this.createNewSelection([serieClicked.name]), this.propActiveSelections, this.dashboardId, this.setSelections, this.$http)
        },
        createNewSelection(value: (string | number)[]) {
            const attributeColumn = this.widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'ATTRIBUTE')
            const selection = {
                datasetId: this.widgetModel.dataset as number,
                datasetLabel: this.getDatasetLabel(this.widgetModel.dataset as number),
                columnName: attributeColumn?.columnName ?? '',
                value: value,
                aggregated: false,
                timestamp: new Date().getTime()
            }
            return selection
        },
        resizeChart() {
            setTimeout(() => {
                this.highchartsInstance.reflow()
            }, 100)
        },
        getModelForRender() {
            const formattedChartModel = deepcopy(this.chartModel)
            if (formattedChartModel.chart.type === 'activitygauge') formattedChartModel.chart.type = 'solidgauge'

            return formattedChartModel
        }
    }
})
</script>
