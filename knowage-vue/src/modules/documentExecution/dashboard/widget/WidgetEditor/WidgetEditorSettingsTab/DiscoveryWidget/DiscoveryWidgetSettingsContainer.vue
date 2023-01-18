<template>
    <div v-show="widgetModel">
        <DiscoveryWidgetSettingsAccordion v-show="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :variables="variables" :dashboardId="dashboardId"></DiscoveryWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './DiscoveryWidgetSettingsDescriptor.json'
import DiscoveryWidgetSettingsAccordion from './DiscoveryWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'table-widget-settings-container',
    components: { DiscoveryWidgetSettingsAccordion },
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
