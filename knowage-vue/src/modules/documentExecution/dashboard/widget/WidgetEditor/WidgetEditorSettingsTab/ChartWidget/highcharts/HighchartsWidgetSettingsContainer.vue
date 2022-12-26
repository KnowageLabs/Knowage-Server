<template>
    <div v-show="widgetModel">
        <HighchartsWidgetSettingsAccordion
            v-show="selectedSetting"
            :widgetModel="widgetModel"
            :settings="descriptor?.settings[selectedSetting]"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :variables="variables"
            :dashboardId="dashboardId"
            :descriptor="descriptor"
        ></HighchartsWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './HighchartsWidgetSettingsDescriptor.json'
import HighchartsPieSettingsDescriptor from './descriptors/HighchartsPieSettingsDescriptor.json'
import HighchartsGaugeSettingsDescriptor from './descriptors/HighchartsGaugeSettingsDescriptor.json'
import HighchartsWidgetSettingsAccordion from './HighchartsWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'highcharts-widget-settings-container',
    components: { HighchartsWidgetSettingsAccordion },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {}
    },
    computed: {
        descriptor() {
            switch (this.widgetModel?.settings.chartModel?.model?.chart.type) {
                case 'pie':
                    return HighchartsPieSettingsDescriptor
                case 'gauge':
                    return HighchartsGaugeSettingsDescriptor
            }
        }
    },
    created() {},
    methods: {}
})
</script>
