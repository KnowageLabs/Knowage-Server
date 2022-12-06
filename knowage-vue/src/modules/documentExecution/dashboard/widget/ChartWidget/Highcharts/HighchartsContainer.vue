<template>
    <button @click="updateChartModel">Test</button>
    <div id="container" style="width:100%; height:400px;"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '../../../Dashboard'
import { IHighchartsChartSerie, IHighchartsSerieAccessibility, ISerieAccessibilitySetting } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'

Accessibility(Highcharts)
NoDataToDisplay(Highcharts)
Highcharts3D(Highcharts)

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            chartModel: {} as any
        }
    },
    mounted() {
        this.setEventListeners()
        this.onRefreshChart()
        this.updateChartModel()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('refreshChart', this.onRefreshChart)
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
        },
        onRefreshChart() {
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            this.updateChartModel()
            console.log('>>>>>>>>>> refreshChart: ', this.chartModel)
        },
        updateChartModel() {
            // TODO - remove this
            if (this.widgetModel.type !== 'chart') return

            // highcharts3D(chart)
            // Create the chart
            Highcharts.setOptions({
                lang: {
                    noData: 'No data message'
                }
            })

            // TODO - Remove Hardcoded
            this.chartModel.series = [
                {
                    type: 'pie',
                    name: 'Share',
                    data: [
                        ['Samsung', 23],
                        ['Apple', 18],
                        {
                            name: 'Xiaomi',
                            y: 12,
                            sliced: true,
                            selected: true
                        },
                        ['Oppo*', 9],
                        ['Vivo', 8],
                        ['Others', 30]
                    ]
                }
            ]

            this.updateSeriesAccessibilitySettings()

            Highcharts.chart('container', this.chartModel)

            // Highcharts.chart('container', {
            //     noData: {
            //         position: {
            //             align: 'right',
            //             verticalAlign: 'middle'
            //         },
            //         style: {
            //             backgroundColor: 'rgb(255, 255, 225)',
            //             color: 'rgb(20, 0, 221)',
            //             textAlign: 'start',
            //             fontSize: '12px',
            //             fontFamily: 'roboto',
            //             fontWeight: 'bold'
            //         }
            //     },
            //     chart: {
            //         type: 'pie',
            //         options3d: {
            //             enabled: true,
            //             alpha: 45,
            //             beta: 0
            //         } // HighchartsOptions3D
            //     },
            //     title: {
            //         text: 'Global smartphone shipments market share, Q1 2022',
            //         align: 'left'
            //     },
            //     subtitle: {
            //         text: 'Source: ' + '<a href="https://www.counterpointresearch.com/global-smartphone-share/"' + 'target="_blank">Counterpoint Research</a>',
            //         align: 'left'
            //     },
            //     accessibility: {
            //         point: {
            //             valueSuffix: '%'
            //         }
            //     },
            //     tooltip: {
            //         pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            //     },
            //     plotOptions: {
            //         pie: {
            //             allowPointSelect: true,
            //             cursor: 'pointer',
            //             depth: 35, // needed for 3D
            //             dataLabels: {
            //                 enabled: true,
            //                 format: '{point.name}'
            //             }
            //         }
            //     },
            //     series: [
            //         {
            //             type: 'pie',
            //             name: 'Share',
            //             data: []
            //         }
            //     ]
            // })
        },
        updateSeriesAccessibilitySettings() {
            if (!this.widgetModel || !this.widgetModel.settings.accesssibility || !this.widgetModel.settings.accesssibility.seriesAccesibilitySettings) return

            this.chartModel.series.forEach((serie: IHighchartsChartSerie) => {
                if (this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility.enabled) {
                    serie.accessibility = { ...this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility }
                } else {
                    serie.accessibility = {
                        enabled: false,
                        description: '',
                        exposeAsGroupOnly: false,
                        keyboardNavigation: { enabled: false }
                    }
                }
            })

            for (let i = 1; i < this.widgetModel.settings.accesssibility.seriesAccesibilitySettings.length; i++) {
                const seriesAccesibilitySetting = this.widgetModel.settings.accesssibility.seriesAccesibilitySettings[i] as ISerieAccessibilitySetting
                if (seriesAccesibilitySetting.accessibility.enabled) seriesAccesibilitySetting.names.forEach((serieName: string) => this.updateSerieAccessibilitySettings(serieName, seriesAccesibilitySetting.accessibility))
            }
        },
        updateSerieAccessibilitySettings(serieName: string, accessibility: IHighchartsSerieAccessibility) {
            const index = this.chartModel.series.findIndex((serie: IHighchartsChartSerie) => serie.name === serieName)
            if (index !== -1) this.chartModel.series[index].accessibility = { ...accessibility }
        }
    }
})
</script>
