<template>
    <WidgetEditorDataList :widget-model="widget" :datasets="datasets" :selected-datasets="selectedDatasets" @datasetSelected="setSelectDataset"></WidgetEditorDataList>
    <ChartGallery v-if="chartPickerVisible" :widget-model="widget" @selectedChartTypeChanged="onChartTypeChanged" />
    <div v-else-if="widget" class="p-d-flex kn-flex kn-overflow">
        <WidgetEditorHint v-if="!selectedDataset"></WidgetEditorHint>
        <WidgetEditorCommonDataContainer v-else-if="['table', 'html', 'text', 'discovery', 'customchart'].includes(widget.type)" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :prop-widget-model="widget" :selected-dataset="selectedDataset"></WidgetEditorCommonDataContainer>
        <SelectorWidgetDataContainer v-else-if="widget.type === 'selector'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widget-model="propWidget" :selected-dataset="selectedDataset"></SelectorWidgetDataContainer>
        <HighchartsDataContainer v-else-if="widget.type === 'highcharts' && isEnterprise" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widget-model="propWidget" :selected-dataset="selectedDataset" @selectedChartTypeChanged="onChartTypeChanged"></HighchartsDataContainer>
        <ChartJSDataContainer v-else-if="widget.type === 'chartJS'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widget-model="propWidget" :selected-dataset="selectedDataset" @selectedChartTypeChanged="onChartTypeChanged"></ChartJSDataContainer>
        <VegaDataContainer v-else-if="widget.type === 'vega'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widget-model="propWidget" :selected-dataset="selectedDataset" @selectedChartTypeChanged="onChartTypeChanged"></VegaDataContainer>
        <PivotTableDataContainer v-else-if="widget.type === 'static-pivot-table'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :prop-widget-model="propWidget" :selected-dataset="selectedDataset"></PivotTableDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '../../../Dashboard'
import { createNewHighchartsModel, createNewHighchartsSettings } from '../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsHelpers'
import { createChartJSModel, createNewChartJSSettings } from '../helpers/chartWidget/chartJS/ChartJSHelpers'
import { updateWidgetModelColumnsAfterChartTypeChange } from '../helpers/chartWidget/highcharts/HighchartsDataTabHelpers'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { mapState } from 'pinia'
import { IHighchartsWidgetSettings } from '../../../interfaces/highcharts/DashboardHighchartsWidget'
import { IChartJSWidgetSettings } from '../../../interfaces/chartJS/DashboardChartJSWidget'
import { createVegaModel, createNewVegaSettings } from '../helpers/chartWidget/vega/VegaHelpers'
import { IVegaChartsSettings } from '../../../interfaces/vega/VegaChartsWidget'
import mainStore from '@/App.store'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'
import WidgetEditorCommonDataContainer from './common/WidgetEditorCommonDataContainer.vue'
import SelectorWidgetDataContainer from './SelectorWidget/SelectorWidgetDataContainer.vue'
import HighchartsDataContainer from './ChartWidget/highcharts/HighchartsDataContainer.vue'
import PivotTableDataContainer from './PivotTable/PivotTableDataContainer.vue'
import ChartJSDataContainer from './ChartWidget/chartJS/ChartJSDataContainer.vue'
import ChartGallery from '../WidgetEditorDataTab/ChartWidget/common/ChartWidgetGallery.vue'
import VegaDataContainer from './ChartWidget/vega/VegaDataContainer.vue'
import { updateVegaModelColumnsAfterChartTypeChange } from '../helpers/chartWidget/vega/VegaDataTabHelpers'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { WidgetEditorDataList, WidgetEditorHint, SelectorWidgetDataContainer, HighchartsDataContainer, WidgetEditorCommonDataContainer, ChartJSDataContainer, ChartGallery, PivotTableDataContainer, VegaDataContainer },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]> }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['datasetSelected'],
    data() {
        return {
            selectedDataset: null as IDataset | null,
            widget: {} as IWidget
        }
    },
    computed: {
        ...mapState(mainStore, {
            isEnterprise: 'isEnterprise'
        }),
        chartPickerVisible() {
            let visible = false
            if (!this.widget || !['highcharts', 'chartJS', 'vega'].includes(this.widget.type)) return false
            const model = (this.widget.settings as IHighchartsWidgetSettings | IChartJSWidgetSettings | IVegaChartsSettings).chartModel?.model
            visible = !model?.chart?.type
            emitter.emit('chartPickerVisible', visible)
            return visible
        }
    },
    created() {
        this.loadWidget()
    },
    methods: {
        loadWidget() {
            this.widget = this.propWidget
        },
        setSelectDataset(dataset: IDataset) {
            this.$emit('datasetSelected', dataset)
            this.selectedDataset = dataset as IDataset
        },
        onChartTypeChanged(chartType: string) {
            if (!this.widget) return

            // TODO widgetChange
            if (chartType === 'wordcloud') {
                this.widget.type = 'vega'
                this.widget.settings = createNewVegaSettings()
                updateVegaModelColumnsAfterChartTypeChange(this.widget, chartType)
                this.widget.settings.chartModel = createVegaModel(this.widget, chartType)
            } else if (this.isEnterprise) {
                const oldChartModel = this.widget.settings.chartModel?.model
                this.widget.type = 'highcharts'
                this.widget.settings = createNewHighchartsSettings()
                updateWidgetModelColumnsAfterChartTypeChange(this.widget, chartType)
                this.widget.settings.chartModel = createNewHighchartsModel(chartType, oldChartModel)
            } else {
                this.widget.type = 'chartJS'
                this.widget.settings = createNewChartJSSettings()
                this.widget.settings.chartModel = createChartJSModel(chartType)
            }
            emitter.emit('chartTypeChanged', this.widget.id)
            emitter.emit('refreshWidgetWithData', this.widget.id)
        }
    }
})
</script>
