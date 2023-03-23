<template>
    <div :id="'chartId' + chartID" class="kn-flex">VEGA CONTAINER</div>
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
                chart: {
                    type: 'wordcloud'
                },
                description: 'A word cloud visualization depicting Vega research paper abstracts.',

                padding: 0,
                autosize: {
                    contains: 'padding',
                    type: 'fit'
                },
                signals: [
                    {
                        init: 'containerSize()[0]',
                        name: 'width',
                        on: [
                            {
                                events: 'window:resize',
                                update: 'containerSize()[0]'
                            }
                        ]
                    },
                    {
                        init: 'containerSize()[1]',
                        name: 'height',
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
                        transform: [],
                        values: [
                            {
                                text: 'pre Alcoholic Beverages suf',
                                count: 34
                            },
                            {
                                text: 'Baked Goods',
                                count: 39
                            },
                            {
                                text: 'VEGA',
                                count: 100
                            }
                        ]
                    }
                ],
                scales: [
                    {
                        domain: {
                            data: 'table',
                            field: 'text'
                        },
                        name: 'color',
                        range: ['#d5a928', '#652c90', '#939597'],
                        type: 'ordinal'
                    }
                ],
                marks: [
                    {
                        encode: {
                            enter: {
                                align: {
                                    value: 'center'
                                },
                                baseline: {
                                    value: 'alphabetic'
                                },
                                fill: {
                                    field: 'text',
                                    scale: 'color'
                                },
                                text: {
                                    field: 'text'
                                },
                                tooltip: {
                                    signal: "format(datum.count, '($.2f')"
                                }
                            },
                            hover: {
                                fillOpacity: {
                                    value: 0.5
                                }
                            },
                            update: {
                                fillOpacity: {
                                    value: 1
                                }
                            }
                        },
                        from: {
                            data: 'table'
                        },
                        transform: [
                            {
                                font: 'Helvetica Neue, Arial',
                                fontSize: {
                                    field: 'datum.count'
                                },
                                fontSizeRange: [12, 100],
                                padding: 5,
                                rotate: {
                                    field: 'datum.angle'
                                },
                                text: {
                                    field: 'text'
                                },
                                type: 'wordcloud'
                            }
                        ],
                        type: 'text'
                    }
                ]
            }

            console.log('-------- CHART MODEL TO RENDER: ', this.chartModel)

            try {
                vegaEmbed('#chartId' + this.chartID, this.chartModel as any)
            } catch (error) {
                this.setError({ title: this.$t('common.toast.errorTitle'), msg: error })
            }
        }
    }
})
</script>
