<template>
    <div :id="'chartId' + chartID">VEGA CONTAINER</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { mapActions } from 'pinia'
import { IWidget, ISelection } from '../../../Dashboard'
import cryptoRandomString from 'crypto-random-string'
import vegaEmbed from 'vega-embed'
import mainStore from '@/App.store'
import { IVegaChartsModel } from '../../../interfaces/vega/VegaChartsWidget'

export default defineComponent({
    name: 'vega-container',
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
            chartID: cryptoRandomString({ length: 16, type: 'alphanumeric' }),
            chartModel: {} as IVegaChartsModel
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
        ...mapActions(mainStore, ['setError']),
        setEventListeners() {
            emitter.on('refreshChart', this.onRefreshChart)
        },
        removeEventListeners() {
            emitter.off('refreshChart', this.onRefreshChart)
        },
        onRefreshChart(widgetId: any | null = null) {
            if (widgetId && widgetId !== this.widgetModel.id) return
            this.chartModel = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            console.log('---------- onRefreshChart: ', this.chartModel)
            this.updateChartModel()
        },
        updateChartModel() {
            if (!this.chartModel) return

            // TODO - Remove Hardcoded
            this.chartModel = {
                $schema: 'https://vega.github.io/schema/vega/v5.json',
                chart: { type: '' },
                description: 'A word cloud visualization depicting Vega research paper abstracts.',
                padding: 0,
                autosize: {
                    type: 'fit',
                    contains: 'padding'
                },
                signals: [
                    {
                        name: 'width',
                        init: 'containerSize()[0]',
                        on: [
                            {
                                events: 'window:resize',
                                update: 'containerSize()[0]'
                            }
                        ]
                    },
                    {
                        name: 'height',
                        init: 'containerSize()[1]',
                        on: [
                            {
                                events: 'window:resize',
                                update: 'containerSize()[1]'
                            }
                        ]
                    }
                ],
                data: [
                    {
                        name: 'table',
                        values: [
                            {
                                text: 'pre Alcoholic Beverages suf',
                                count: 30
                            },
                            {
                                text: 'Baked Goods',
                                count: 20
                            },
                            {
                                text: 'VEGA',
                                count: 5
                            }
                        ],
                        transform: [
                            {
                                type: 'formula',
                                as: 'angle',
                                expr: '[-45, 0, 45][~~(random() * 3)]'
                            }
                        ]
                    }
                ],
                scales: [
                    {
                        name: 'color',
                        type: 'ordinal',
                        domain: {
                            data: 'table',
                            field: 'text'
                        },
                        range: ['#d5a928', '#652c90', '#939597']
                    }
                ],
                marks: [
                    {
                        type: 'text',
                        from: {
                            data: 'table'
                        },
                        encode: {
                            enter: {
                                text: {
                                    field: 'text'
                                },
                                align: {
                                    value: 'center'
                                },
                                baseline: {
                                    value: 'alphabetic'
                                },
                                fill: {
                                    scale: 'color',
                                    field: 'text'
                                },
                                tooltip: {
                                    signal: "format(datum.count, '($.2f')"
                                }
                            },
                            update: {
                                fillOpacity: {
                                    value: 1
                                }
                            },
                            hover: {
                                fillOpacity: {
                                    value: 0.5
                                }
                            }
                        },
                        transform: [
                            {
                                type: 'wordcloud',
                                text: {
                                    field: 'text'
                                },
                                rotate: {
                                    field: 'datum.angle'
                                },
                                font: 'Helvetica Neue, Arial',
                                fontSize: {
                                    field: 'datum.count'
                                },
                                fontSizeRange: [12, 100],
                                padding: 5
                            }
                        ]
                    }
                ]
            }

            try {
                vegaEmbed('#chartId' + this.chartID, this.chartModel as any)
            } catch (error) {
                this.setError({ title: this.$t('common.toast.errorTitle'), msg: error })
            }
        }
    }
})
</script>
