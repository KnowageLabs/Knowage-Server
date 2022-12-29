<template>
    <div v-show="!error" :id="chartID" style="width: 100%; height: 100%; margin: 0 auto"></div>
    <!-- <div style="height: 20px"> {{ dataToShow }}</div> -->
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
import mockedData from './mockedData.json' // TODO - Remove

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
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true }
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
        onRefreshChart() {
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.updateChartModel()
        },
        updateChartModel() {
            Highcharts.setOptions({
                lang: {
                    noData: this.chartModel.lang.noData
                }
            })

            // TODO - Uncomment
            // this.widgetModel.settings.chartModel.setData(this.dataToShow, this.widgetModel)
            //this.widgetModel.settings.chartModel.setData(mockedData, this.widgetModel)

            // TODO
            this.widgetModel.settings.chartModel.model.series = [
                {
                    name: 'Speed',
                    data: [
                        {
                            y: 30
                        }
                    ]
                }
            ]

            this.widgetModel.settings.chartModel.updateSeriesAccessibilitySettings(this.widgetModel)
            this.widgetModel.settings.chartModel.updateSeriesLabelSettings(this.widgetModel)
            this.updateDataLabels()
            this.error = this.updateLegendSettings()
            if (this.error) return
            this.error = this.updateTooltipSettings()
            if (this.error) return

            this.widgetModel.settings.chartModel.updateChartColorSettings(this.widgetModel)

            this.setSeriesEvents()

            console.log('>>>>>>> CHART TO RENDER: ', this.getModelForRender())
            this.highchartsInstance = Highcharts.chart(this.chartID, this.getModelForRender() as any)
            // TODO
            this.highchartsInstance = Highcharts.chart(this.chartID, {
                title: '',
                lang: {
                    noData: ''
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
                        align: '',
                        verticalAlign: 'middle'
                    },
                    style: {
                        fontFamily: '',
                        fontSize: '',
                        fontWeight: '',
                        color: '',
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
                        name: 'Speed',
                        data: [
                            {
                                y: 30,
                                dataLabels: {
                                    absolute: false,
                                    backgroundColor: 'rgba(194,194,194, 1)',
                                    enabled: false,
                                    percentage: false,
                                    precision: 2,
                                    prefix: '',
                                    scale: 'empty',
                                    style: {
                                        color: '',
                                        fontFamily: '',
                                        fontSize: '',
                                        fontWeight: ''
                                    },
                                    suffix: '',
                                    position: ''
                                }
                            }
                        ],
                        accessibility: {
                            enabled: false,
                            description: '',
                            exposeAsGroupOnly: false,
                            keyboardNavigation: {
                                enabled: false
                            }
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
                        dataLabels: {
                            backgroundColor: 'rgba(255, 255, 255, 1)',
                            enabled: false,
                            position: '',
                            style: {
                                color: '',
                                fontFamily: '',
                                fontSize: '14px',
                                fontWeight: ''
                            },
                            formatterError: ''
                        },
                        showInLegend: true,
                        colors: ['rgba(4,45,87,1)', 'rgba(7,83,160,1)', 'rgba(10,121,233,1)', 'rgba(72,159,247,1)', 'rgba(145,197,250,1)', 'rgba(248,70,138,1)', 'rgba(121,133,155,1)', 'rgba(165,173,188,1)', 'rgba(209,213,221,1)', 'rgba(59,2,24,1)', 'rgba(133,5,54,1)', 'rgba(207,8,84,1)']
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
                    enabled: true,
                    style: {
                        fontFamily: '',
                        fontSize: '',
                        fontWeight: '',
                        color: ''
                    },
                    backgroundColor: 'rgba(214,214,214,1)',
                    formatterError: '',
                    pointFormatterError: ''
                },
                credits: {
                    enabled: false
                },
                pane: {
                    startAngle: -120,
                    endAngle: 120,
                    center: ['50%', '50%'],
                    background: {
                        backgroundColor: '#EEE',
                        innerRadius: '60%',
                        outerRadius: '100%',
                        shape: 'arc'
                    }
                },
                yAxis: {
                    max: 40,
                    min: 2,
                    minorTickInterval: 'auto',
                    plotBands: [],
                    tickColor: '',
                    tickLength: 0,
                    tickPosition: '',
                    tickWidth: 0
                }
            } as any)
            this.highchartsInstance.reflow()
        },
        updateLegendSettings() {
            if (this.chartModel.plotOptions.pie) this.chartModel.plotOptions.pie.showInLegend = true
            if (this.chartModel.plotOptions.gauge) this.chartModel.plotOptions.gauge.showInLegend = true
            return this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
        },
        updateDataLabels() {
            console.log('>>>>>>> CHART MODEL PLOT OPTIONS: ', this.chartModel.plotOptions)
            console.log('>>>>>>> this.chartModel.chart.type: ', this.chartModel.chart.type)
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
                this.chartModel.plotOptions.series.events = { click: this.setSelection }
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
            const selection = { datasetId: this.widgetModel.dataset as number, datasetLabel: this.getDatasetLabel(this.widgetModel.dataset as number), columnName: attributeColumn?.columnName ?? '', value: value, aggregated: false, timestamp: new Date().getTime() }
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
