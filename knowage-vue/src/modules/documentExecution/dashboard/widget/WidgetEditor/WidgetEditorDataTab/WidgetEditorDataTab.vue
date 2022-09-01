<template>
    <WidgetEditorDataList :widgetModel="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="setSelectDataset"></WidgetEditorDataList>
    <div class="p-d-flex kn-flex kn-overflow" v-if="propWidget">
        <WidgetEditorHint v-if="!selectedDataset"></WidgetEditorHint>
        <!-- <WidgetEditorGeneric v-else id="model-div" class="kn-flex kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :propDescriptor="dataDescriptor"></WidgetEditorGeneric> -->
        <TableWidgetDataContainer v-else id="model-div" class="kn-flex kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget"></TableWidgetDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '../../../Dashboard'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'
import TableWidgetDataContainer from './TableWidget/TableWidgetDataContainer.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { WidgetEditorDataList, WidgetEditorGeneric, WidgetEditorHint, TableWidgetDataContainer },
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
