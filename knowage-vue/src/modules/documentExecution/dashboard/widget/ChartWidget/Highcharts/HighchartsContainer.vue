<template>
    <div id="container" style="width:100%; height:400px;"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Highcharts from 'highcharts'
import Highcharts3D from 'highcharts/highcharts-3d'

Highcharts3D(Highcharts)

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
            // highcharts3D(chart)
            // Create the chart
            Highcharts.chart('container', {
                chart: {
                    type: 'pie',
                    options3d: {
                        enabled: true,
                        alpha: 45,
                        beta: 0
                    } // HighchartsOptions3D
                },
                title: {
                    text: 'Global smartphone shipments market share, Q1 2022',
                    align: 'left'
                },
                subtitle: {
                    text: 'Source: ' + '<a href="https://www.counterpointresearch.com/global-smartphone-share/"' + 'target="_blank">Counterpoint Research</a>',
                    align: 'left'
                },
                accessibility: {
                    point: {
                        valueSuffix: '%'
                    }
                },
                tooltip: {
                    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                },
                plotOptions: {
                    pie: {
                        allowPointSelect: true,
                        cursor: 'pointer',
                        depth: 35, // needed for 3D
                        dataLabels: {
                            enabled: true,
                            format: '{point.name}'
                        }
                    }
                },
                series: [
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
            })
        }
    }
})
</script>
