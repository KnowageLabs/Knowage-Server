<template>
    <div v-show="widgetModel">
        <HTMLWidgetSettingsAccordion v-show="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :drivers="drivers" :variables="variables" :dashboardId="dashboardId"></HTMLWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './HTMLWidgetSettingsDescriptor.json'
import HTMLWidgetSettingsAccordion from './HTMLWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'html-widget-settings-container',
    components: { HTMLWidgetSettingsAccordion },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array },
        variables: { type: Array as PropType<IVariable[]> },
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
