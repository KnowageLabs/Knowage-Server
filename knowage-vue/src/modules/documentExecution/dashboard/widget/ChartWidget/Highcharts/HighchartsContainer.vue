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
            Highcharts.setOptions({
                lang: {
                    noData: this.chartModel.lang.noData
                }
            })

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
            modelToRender.series?.forEach((serie: any) => console.log('>>>>>>>>SERIE: ', serie.data[0].dataLabels))
            // const temp = {
            //     title: '',
            //     lang: {
            //         noData: 'No Data message'
            //     },
            //     chart: {
            //         options3d: {
            //             alpha: 0,
            //             beta: 0,
            //             enabled: false,
            //             viewDistance: 25
            //         },
            //         type: 'gauge'
            //     },
            //     noData: {
            //         position: {
            //             align: 'left',
            //             verticalAlign: 'middle'
            //         },
            //         style: {
            //             fontFamily: 'Roboto',
            //             fontSize: '14px',
            //             fontWeight: 'bold',
            //             color: '',
            //             backgroundColor: ''
            //         }
            //     },
            //     accessibility: {
            //         description: '',
            //         enabled: false,
            //         keyboardNavigation: {
            //             enabled: false,
            //             order: []
            //         }
            //     },
            //     series: [
            //         {
            //             name: 'UNITS_ORDERED',
            //             data: [
            //                 {
            //                     name: 'UNITS_ORDERED',
            //                     y: 2330
            //                 }
            //             ],
            //             dataLabels: {
            //                 backgroundColor: 'green',
            //                 distance: 30,
            //                 enabled: true,
            //                 position: '',
            //                 y: 40,
            //                 style: {
            //                     fontFamily: '',
            //                     fontSize: '',
            //                     fontWeight: '',
            //                     color: ''
            //                 }
            //             },
            //             colorByPoint: false,
            //             groupingFunction: 'MIN',
            //             accessibility: {
            //                 enabled: false,
            //                 description: '',
            //                 exposeAsGroupOnly: false,
            //                 keyboardNavigation: {
            //                     enabled: false
            //                 }
            //             },
            //             dial: {
            //                 backgroundColor: 'rgba(194,194,194, 1)',
            //                 baseWidth: 3,
            //                 radius: '80%'
            //             },
            //             pivot: {
            //                 backgroundColor: 'rgba(194,194,194, 1)',
            //                 radius: 5
            //             }
            //         },
            //         {
            //             name: 'UNITS_SHIPPED',
            //             data: [
            //                 {
            //                     name: 'UNITS_SHIPPED',
            //                     y: 239
            //                 }
            //             ],
            //             dataLabels: {
            //                 backgroundColor: 'red',
            //                 distance: 60,
            //                 enabled: true,
            //                 position: '',
            //                 style: {
            //                     fontFamily: '',
            //                     fontSize: '',
            //                     fontWeight: '',
            //                     color: ''
            //                 },
            //                 y: 80
            //             },
            //             colorByPoint: false,
            //             groupingFunction: 'MIN',
            //             accessibility: {
            //                 enabled: false,
            //                 description: '',
            //                 exposeAsGroupOnly: false,
            //                 keyboardNavigation: {
            //                     enabled: false
            //                 }
            //             },
            //             dial: {
            //                 backgroundColor: 'rgba(194,194,194, 1)',
            //                 baseWidth: 3,
            //                 radius: '80%'
            //             },
            //             pivot: {
            //                 backgroundColor: 'rgba(194,194,194, 1)',
            //                 radius: 5
            //             }
            //         }
            //     ],
            //     settings: {
            //         drilldown: {},
            //         categories: []
            //     },
            //     plotOptions: {
            //         series: {
            //             events: {}
            //         },
            //         gauge: {
            //             dataLabels: {
            //                 allowOverlap: false,
            //                 backgroundColor: 'rgba(194,194,194, 1)',
            //                 enabled: true,
            //                 position: '',
            //                 style: {
            //                     color: '',
            //                     fontFamily: '',
            //                     fontSize: '14px',
            //                     fontWeight: ''
            //                 },
            //                 formatterError: ''
            //             },
            //             showInLegend: true
            //         }
            //     },
            //     legend: {
            //         align: 'center',
            //         backgroundColor: '',
            //         borderColor: '',
            //         borderWidth: 1,
            //         enabled: true,
            //         itemStyle: {
            //             color: '',
            //             fontFamily: '',
            //             fontSize: '',
            //             fontWeight: ''
            //         },
            //         layout: 'horizontal',
            //         verticalAlign: 'top',
            //         labelFormatterError: ''
            //     },
            //     tooltip: {
            //         enabled: true,
            //         style: {
            //             fontFamily: 'Roboto',
            //             fontSize: '14px',
            //             fontWeight: 'bold',
            //             color: 'rgba(215,15,230,1)'
            //         },
            //         backgroundColor: 'rgba(160,245,15,1)',
            //         formatterError: '',
            //         pointFormatterError: ''
            //     },
            //     colors: ['rgba(4,45,87,1)', 'rgba(133,5,54,1)', 'rgba(165,173,188,1)', 'rgba(7,83,160,1)', 'rgba(10,121,233,1)', 'rgba(72,159,247,1)', 'rgba(145,197,250,1)', 'rgba(121,133,155,1)', 'rgba(209,213,221,1)', 'rgba(59,2,24,1)', 'rgba(207,8,84,1)', 'rgba(248,70,138,1)'],
            //     credits: {
            //         enabled: false
            //     },
            //     pane: {
            //         startAngle: -120,
            //         endAngle: 120,
            //         center: ['50%', '50%']
            //     },
            //     yAxis: {
            //         max: null,
            //         min: null,
            //         minorTickInterval: 'auto',
            //         plotBands: [],
            //         stops: null,
            //         tickColor: 'rgba(24,223,145,1)',
            //         tickLength: 10,
            //         tickPosition: 'outside',
            //         tickWidth: 5
            //     }
            // }
            this.highchartsInstance = Highcharts.chart(this.chartID, modelToRender as any)
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
