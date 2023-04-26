<template>
    <div v-show="widgetModel">
        <TextWidgetSettingsAccordion v-show="selectedSetting" :widget-model="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selected-datasets="selectedDatasets" :variables="variables" :dashboard-id="dashboardId"></TextWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './TextWidgetSettingsDescriptor.json'
import TextWidgetSettingsAccordion from './TextWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'text-widget-settings-container',
    components: { TextWidgetSettingsAccordion },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
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
