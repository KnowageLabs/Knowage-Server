<template>
    <div v-show="widgetModel">
        <HighchartsWidgetSettingsAccordion
            v-show="selectedSetting"
            :widgetModel="widgetModel"
            :settings="descriptor.settings[selectedSetting]"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :drivers="drivers"
            :variables="variables"
            :dashboardId="dashboardId"
        ></HighchartsWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './HighchartsWidgetSettingsDescriptor.json'
import HighchartsWidgetSettingsAccordion from './HighchartsWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'highcharts-widget-settings-container',
    components: { HighchartsWidgetSettingsAccordion },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
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
