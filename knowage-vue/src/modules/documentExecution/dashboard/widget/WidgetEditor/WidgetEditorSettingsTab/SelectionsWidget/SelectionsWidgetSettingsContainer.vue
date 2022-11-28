<template>
    <div v-show="widgetModel">
        <SelectionsWidgetSettingsAccordion v-show="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :variables="variables"></SelectionsWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IDashboardDriver } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './SelectionsWidgetSettingsDescriptor.json'
import SelectionsWidgetSettingsAccordion from './SelectionsWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'selections-widget-settings-container',
    components: { SelectionsWidgetSettingsAccordion },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array as PropType<IDashboardDriver[]> },
        variables: { type: Array as PropType<IVariable[]> }
    },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>
