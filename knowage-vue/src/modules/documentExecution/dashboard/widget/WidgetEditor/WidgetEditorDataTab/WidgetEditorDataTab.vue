<template>
    <div class="p-d-flex">
        <Card class="kn-flex p-m-2 widget-editor-data-list-card">
            <template #content>
                <WidgetEditorDataList :widgetModel="propWidget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="setSelectDataset"></WidgetEditorDataList>
            </template>
        </Card>
        <div v-if="propWidget">
            <WidgetEditorHint v-if="!selectedDataset && propWidget.columns.length === 0"></WidgetEditorHint>
            <WidgetEditorGeneric v-else class="kn-flex p-m-2" :widgetModel="propWidget" :propDescriptor="dataDescriptor"></WidgetEditorGeneric>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '../../../Dashboard'
import Card from 'primevue/card'
import dataDescriptor from './WidgetEditorGenericDescriptor.json'
import WidgetEditorDataList from './WidgetEditorDataList/WidgetEditorDataList.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'
import WidgetEditorHint from '../WidgetEditorHint.vue'

export default defineComponent({
    name: 'widget-editor-data-tab',
    components: { Card, WidgetEditorDataList, WidgetEditorGeneric, WidgetEditorHint },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]> }, selectedDatasets: { type: Array as PropType<IDataset[]> } },
    emits: ['datasetSelected'],
    data() {
        return {
            dataDescriptor,
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

<style lang="scss" scoped>
.widget-editor-data-list-card {
    min-width: 250px;
    max-width: 300px;
}
</style>
