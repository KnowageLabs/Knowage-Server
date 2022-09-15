<template>
    <div v-show="widgetModel">
        <TableWidgetSettingsAccordion v-show="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :variables="variables"></TableWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetSettingsAccordion from './TableWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'table-widget-settings-container',
    components: { TableWidgetSettingsAccordion },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedSetting: { type: String, required: true }, datasets: { type: Array as PropType<IDataset[]> }, selectedDatasets: { type: Array as PropType<IDataset[]> }, drivers: { type: Array }, variables: { type: Array } },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>
