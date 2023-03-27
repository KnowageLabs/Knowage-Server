<template>
    <VegaContainerNoData v-if="showNoData" :widget-model="widgetModel"></VegaContainerNoData>
    <div v-else :id="'chartId' + chartID" class="kn-flex"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { mapActions } from 'pinia'
import { IWidget, ISelection, IWidgetColumn } from '../../../Dashboard'
import { IVegaChartsModel, IVegaChartsTextConfiguration, IVegaChartsTooltipSettings } from '../../../interfaces/vega/VegaChartsWidget'
import { executeChartCrossNavigation, updateStoreSelections } from '../../interactionsHelpers/InteractionHelper'
import { formatForCrossNavigation } from './VegaContainerHelpers'
import VegaContainerNoData from './VegaContainerNoData.vue'
import cryptoRandomString from 'crypto-random-string'
import vegaEmbed from 'vega-embed'
import mainStore from '@/App.store'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'

export default defineComponent({
    name: 'vega-container',
    components: { VegaContainerNoData },
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
    computed: {
        showNoData() {
            return this.widgetModel && this.dataToShow && this.dataToShow.rows.length == 0
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
        ...mapActions(dashboardStore, ['setSelections', 'getDatasetLabel']),
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
            //    console.log('---------- onRefreshChart: ', this.chartModel)
            this.updateChartModel()
        },
        async updateChartModel() {
            if (!this.chartModel) return

            // TODO - REMOVE
            // const mockedDataToShow = {
            //     metaData: {
            //         totalProperty: 'results',
            //         root: 'rows',
            //         id: 'id',
            //         fields: [
            //             'recNo',
            //             {
            //                 name: 'column_1',
            //                 header: 'PRODUCT_FAMILY',
            //                 dataIndex: 'column_1',
            //                 type: 'string',
            //                 multiValue: false
            //             },
            //             {
            //                 name: 'column_2',
            //                 header: 'UNIT_SALES_SUM',
            //                 dataIndex: 'column_2',
            //                 type: 'float',
            //                 precision: 54,
            //                 scale: 4,
            //                 multiValue: false
            //             }
            //         ],
            //         cacheDate: '2023-03-23 16:38:56.806'
            //     },
            //     results: 4,
            //     rows: [
            //         {
            //             id: 1,
            //             column_1: 'Food',
            //             column_2: 244181.8227
            //         },
            //         {
            //             id: 2,
            //             column_1: 'Drink',
            //             column_2: 37519.9496
            //         },
            //         {
            //             id: 3,
            //             column_1: 'Non-Consumable',
            //             column_2: 142034.041
            //         },
            //         {
            //             id: 4,
            //             column_1: 'Car',
            //             column_2: 1590.4262
            //         }
            //     ],
            //     initialCall: false
            // }
            const mockedDataToShow = this.dataToShow
            // TODO
            this.widgetModel.settings.chartModel.setData(mockedDataToShow, this.widgetModel)

            this.setTextConfiguration()
            this.setTooltipConfiguration()
            this.setChartColors()

            console.log('-------- CHART MODEL TO RENDER: ', this.chartModel)

            try {
                if (!this.showNoData)
                    await vegaEmbed('#chartId' + this.chartID, this.chartModel as any).then((res: any) => {
                        const view = res.view
                        view.addEventListener('click', (event: any, item: any) => {
                            if (item && item.datum) this.executeInteractions(item.datum)
                            view.finalize()
                        })
                    })
            } catch (error) {
                this.setError({ title: this.$t('common.toast.errorTitle'), msg: error })
            }
        },
        setTextConfiguration() {
            if (!this.chartModel || !this.chartModel.marks || !this.chartModel.marks[0] || !this.chartModel.marks[0].transform || !this.chartModel.marks[0].transform[0] || !this.widgetModel.settings.configuration.textConfiguration) return
            const widgetTextConfiguration = this.widgetModel.settings.configuration.textConfiguration as IVegaChartsTextConfiguration
            const transform = this.chartModel.marks[0].transform[0]
            transform.font = widgetTextConfiguration.font
            transform.rotate = widgetTextConfiguration.wordAngle
            transform.padding = widgetTextConfiguration.wordPadding
            transform.fontSizeRange[0] = widgetTextConfiguration.minimumFontSize
            transform.fontSizeRange[1] = widgetTextConfiguration.maximumFontSize
        },
        setTooltipConfiguration() {
            if (!this.chartModel || !this.chartModel.marks || !this.chartModel.marks[0] || !this.chartModel.marks[0].encode || !this.chartModel.marks[0].encode.enter || !this.chartModel.marks[0].encode.enter.tooltip || !this.widgetModel.settings.tooltip) return
            const tooltipSettings = this.widgetModel.settings.tooltip as IVegaChartsTooltipSettings
            const tooltip = this.chartModel.marks[0].encode.enter.tooltip
            tooltip.signal = `'${tooltipSettings.prefix}' + format(datum.count, '.${tooltipSettings.precision}f') + '${tooltipSettings.suffix}' `
        },
        setChartColors() {
            if (!this.chartModel || !this.chartModel.scales || !this.chartModel.scales[0] || !this.chartModel.data[0] || !this.widgetModel.settings.chart) return
            const colors = this.widgetModel.settings.chart.colors
            const numberOfValues = this.chartModel.data[0].values.length
            const scale = this.chartModel.scales[0]
            scale.range = []
            let i = 0
            let j = 0
            while (i < numberOfValues) {
                scale.range.push(colors[j])
                if (j === colors.length - 1) j = 0
                j++
                i++
            }
        },
        executeInteractions(event: any) {
            if (this.editorMode) return
            if (this.widgetModel.settings.interactions.crossNavigation.enabled) {
                const formattedOutputParameters = formatForCrossNavigation(event, this.widgetModel)
                executeChartCrossNavigation(formattedOutputParameters, this.widgetModel.settings.interactions.crossNavigation, this.dashboardId)
            } else {
                this.setSelection(event)
            }
        },
        setSelection(event: any) {
            if (this.editorMode || !event || !this.widgetModel.settings.interactions.selection || !this.widgetModel.settings.interactions.selection.enabled) return
            updateStoreSelections(this.createNewSelection([event.text]), this.propActiveSelections, this.dashboardId, this.setSelections, this.$http)
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
        }
    }
})
</script>
