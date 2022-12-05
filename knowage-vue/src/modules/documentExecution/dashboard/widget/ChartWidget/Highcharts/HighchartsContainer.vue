<template>
    <button @click="updateChartModel">Test</button>
    <div id="container" style="width:100%; height:400px;"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '../../../Dashboard'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'

NoDataToDisplay(Highcharts)
Highcharts3D(Highcharts)

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            chartModel: {}
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
            console.log('>>>>>>>>>> refreshChart: ', this.chartModel)
            this.updateChartModel()
        },
        updateChartModel() {
            // highcharts3D(chart)
            // Create the chart
            Highcharts.setOptions({
                lang: {
                    noData: 'No data message'
                }
            })

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
        }
    }
})
</script>
