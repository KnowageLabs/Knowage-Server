<template>
    <div v-show="widgetModel">
        <CustomChartWidgetSettingsAccordion v-show="selectedSetting" :widget-model="widgetModel" :settings="descriptor.settings[selectedSetting]" :datasets="datasets" :selected-datasets="selectedDatasets" :variables="variables" :dashboard-id="dashboardId"></CustomChartWidgetSettingsAccordion>
        <CustomChartWidgetSettingsGallery v-if="selectedSetting == 'Gallery'" v-show="selectedSetting" :widget-model="widgetModel" :custom-chart-gallery-prop="customChartGalleryProp" @galleryItemSelected="$emit('galleryItemSelected')"></CustomChartWidgetSettingsGallery>
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
    emits: ['galleryItemSelected'],
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>
