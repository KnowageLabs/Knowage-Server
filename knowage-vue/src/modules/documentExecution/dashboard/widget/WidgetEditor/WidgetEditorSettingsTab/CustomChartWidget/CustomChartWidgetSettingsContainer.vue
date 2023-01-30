<template>
    <div v-show="widgetModel">
        <CustomChartWidgetSettingsAccordion v-show="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :variables="variables" :dashboardId="dashboardId"></CustomChartWidgetSettingsAccordion>
        <CustomChartWidgetSettingsGallery v-if="selectedSetting == 'Gallery'" v-show="selectedSetting" :widgetModel="widgetModel" :customChartGalleryProp="customChartGalleryProp" @galleryItemSelected="$emit('galleryItemSelected')"></CustomChartWidgetSettingsGallery>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IGalleryItem } from '@/modules/documentExecution/dashboard/Dashboard'
import descriptor from './CustomChartWidgetSettingsDescriptor.json'
import CustomChartWidgetSettingsAccordion from './CustomChartWidgetSettingsAccordion.vue'
import CustomChartWidgetSettingsGallery from './gallery/CustomChartWidgetGallery.vue'

export default defineComponent({
    name: 'custom-chart-widget-settings-container',
    components: { CustomChartWidgetSettingsAccordion, CustomChartWidgetSettingsGallery },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true },
        customChartGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true }
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
