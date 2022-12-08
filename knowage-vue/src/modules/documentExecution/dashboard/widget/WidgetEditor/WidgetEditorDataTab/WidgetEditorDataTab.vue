<template>
    <WidgetEditorDataList :widgetModel="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="setSelectDataset"></WidgetEditorDataList>
    <div class="p-d-flex kn-flex kn-overflow" v-if="propWidget">
        <WidgetEditorHint v-if="!selectedDataset"></WidgetEditorHint>
        <WidgetEditorCommonDataContainer v-else-if="propWidget.type === 'table' || propWidget.type === 'html' || propWidget.type === 'text'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset"></WidgetEditorCommonDataContainer>
        <SelectorWidgetDataContainer v-else-if="propWidget.type === 'selector'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset"></SelectorWidgetDataContainer>
        <HighchartsWidgetDataContainer v-else-if="propWidget.type === 'highcharts'" class="kn-flex model-div kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedDataset="selectedDataset"></HighchartsWidgetDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '../../../Dashboard'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'
import WidgetEditorCommonDataContainer from './common/WidgetEditorCommonDataContainer.vue'
import SelectorWidgetDataContainer from './SelectorWidget/SelectorWidgetDataContainer.vue'
import HighchartsWidgetDataContainer from './ChartWidget/highcharts/HighchartsDataContainer.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { WidgetEditorDataList, WidgetEditorHint, WidgetEditorCommonDataContainer, SelectorWidgetDataContainer, HighchartsWidgetDataContainer },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]> }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['datasetSelected'],
    data() {
        return {
            selectedDataset: null as IDataset | null
        }
    },
    async created() {},
    methods: {
        setSelectDataset(dataset: IDataset) {
            this.$emit('datasetSelected', dataset)
            this.selectedDataset = dataset as IDataset
        }
    }
})
</script>
