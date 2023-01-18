<template>
    <WidgetEditorDataList :widgetModel="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="setSelectDataset"></WidgetEditorDataList>
    <ChartGallery v-if="chartPickerVisible" :widgetModel="propWidget" @selectedChartTypeChanged="onChartTypeChanged" />
    <div class="p-d-flex kn-flex kn-overflow" v-else-if="propWidget">
        <WidgetEditorHint v-if="!selectedDataset"></WidgetEditorHint>
        <WidgetEditorCommonDataContainer
            v-else-if="propWidget.type === 'table' || propWidget.type === 'html' || propWidget.type === 'text' || propWidget.type === 'discovery'"
            class="kn-flex model-div kn-overflow p-mx-2 p-my-3"
            :widgetModel="propWidget"
            :selectedDataset="selectedDataset"
        ></WidgetEditorCommonDataContainer>
        <SelectorWidgetDataContainer v-else-if="propWidget.type === 'selector'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset"></SelectorWidgetDataContainer>
        <HighchartsDataContainer v-else-if="propWidget.type === 'highcharts'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset" @selectedChartTypeChanged="onChartTypeChanged"></HighchartsDataContainer>
        <ChartJSDataContainer v-else-if="propWidget.type === 'chartJS'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset"></ChartJSDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '../../../Dashboard'
import { createNewHighchartsModel } from '../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsHelpers'
import { createChartJSModel } from '../helpers/chartWidget/chartJS/ChartJSHelpers'
import { updateWidgetModelColumnsAfterChartTypeChange } from '../helpers/chartWidget/highcharts/HighchartsDataTabHelpers'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { mapState } from 'pinia'
import mainStore from '@/App.store'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'
import WidgetEditorCommonDataContainer from './common/WidgetEditorCommonDataContainer.vue'
import SelectorWidgetDataContainer from './SelectorWidget/SelectorWidgetDataContainer.vue'
import HighchartsDataContainer from './ChartWidget/highcharts/HighchartsDataContainer.vue'
import ChartJSDataContainer from './ChartWidget/chartJS/ChartJSDataContainer.vue'
import ChartGallery from '../WidgetEditorDataTab/ChartWidget/common/ChartWidgetGallery.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { WidgetEditorDataList, WidgetEditorHint, WidgetEditorCommonDataContainer, SelectorWidgetDataContainer, HighchartsDataContainer, ChartJSDataContainer, ChartGallery },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]> }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['datasetSelected'],
    data() {
        return {
            selectedDataset: null as IDataset | null
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        }),
        chartPickerVisible() {
            let visible = false
            if (!this.propWidget || !['highcharts', 'chartJS'].includes(this.propWidget.type)) return false
            const model = this.propWidget.settings.chartModel?.model
            visible = !model?.chart?.type
            emitter.emit('chartPickerVisible', visible)
            return visible
        }
    },
    created() {},
    methods: {
        setSelectDataset(dataset: IDataset) {
            this.$emit('datasetSelected', dataset)
            this.selectedDataset = dataset as IDataset
        },
        onChartTypeChanged(chartType: string) {
            if (!this.user) return
            if (this.user.enterprise) updateWidgetModelColumnsAfterChartTypeChange(this.propWidget, chartType)
            // TODO widgetChange
            this.propWidget.settings.chartModel = this.user.enterprise ? createNewHighchartsModel(chartType, this.propWidget.settings.chartModel?.model) : createChartJSModel(chartType)
            // this.propWidget.settings.chartModel = false ? createNewHighchartsModel(chartType) : createChartJSModel(chartType)
            emitter.emit('chartTypeChanged', this.propWidget.id)
            emitter.emit('refreshWidgetWithData', this.propWidget.id)
        }
    }
})
</script>
