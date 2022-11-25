<template>
    <div id="container" style="width:100%; height:400px;"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Highcharts from 'highcharts'

export default defineComponent({
    name: 'highcharts-container',
    components: {},
    data() {
        return {}
    },
    mounted() {
        this.createChart()
    },
    methods: {
        createChart() {
            // Create the chart
            Highcharts.chart('container', {
                chart: {
                    plotBackgroundColor: '',
                    plotBorderWidth: null,
                    plotShadow: false,
                    type: 'pie'
                },
                title: {
                    text: 'Browser market shares in May, 2020'
                },
                tooltip: {
                    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                },
                accessibility: {
                    point: {
                        valueSuffix: '%'
                    }
                },
                credits: false,
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        dataLabels: {
                            enabled: true,
                            format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                        }
                    }
                },
                series: [
                    {
                        name: 'Brands',
                        colorByPoint: true,
                        data: [
                            {
                                name: 'Chrome',
                                y: 70.67,
                                sliced: true,
                                selected: true
                            },
                            {
                                name: 'Edge',
                                y: 14.77
                            },
                            {
                                name: 'Firefox',
                                y: 4.86
                            },
                            {
                                name: 'Safari',
                                y: 2.63
                            },
                            {
                                name: 'Internet Explorer',
                                y: 1.53
                            },
                            {
                                name: 'Opera',
                                y: 1.4
                            },
                            {
                                name: 'Sogou Explorer',
                                y: 0.84
                            },
                            {
                                name: 'QQ',
                                y: 0.51
                            },
                            {
                                name: 'Other',
                                y: 2.6
                            }
                        ]
                    }
                ]
            } as any)
        }
    }
})
</script>
