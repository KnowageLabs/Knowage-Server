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
import Accessibility from 'highcharts/modules/accessibility'
import NoDataToDisplay from 'highcharts/modules/no-data-to-display'
import SeriesLabel from 'highcharts/modules/series-label'
import cryptoRandomString from 'crypto-random-string'
import store from '../../../Dashboard.store'

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

            this.widgetModel.settings.chartModel.setData(this.dataToShow, this.widgetModel, this.chartModel)

            this.widgetModel.settings.chartModel.updateSeriesAccessibilitySettings(this.widgetModel, this.chartModel)
            this.widgetModel.settings.chartModel.updateSeriesLabelSettings(this.widgetModel, this.chartModel)
            this.error = this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.plotOptions.pie?.dataLabels, 'format', 'formatter', 'formatterText', 'formatterError')
            if (this.error) return
            this.error = this.updateLegendSettings()
            if (this.error) return
            this.error = this.updateTooltipSettings()
            if (this.error) return

            this.widgetModel.settings.chartModel.updateChartColorSettings(this.widgetModel, this.chartModel)

            this.setSeriesEvents()

            console.log('>>>> CHART TO RENDER: ', this.chartModel)
            this.highchartsInstance = Highcharts.chart(this.chartID, this.chartModel as any)
            this.highchartsInstance.reflow()
        },
        updateLegendSettings() {
            if (this.chartModel.plotOptions.pie) this.chartModel.plotOptions.pie.showInLegend = true
            return this.widgetModel.settings.chartModel.updateFormatterSettings(this.chartModel.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
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
        }
    }
})
</script>
