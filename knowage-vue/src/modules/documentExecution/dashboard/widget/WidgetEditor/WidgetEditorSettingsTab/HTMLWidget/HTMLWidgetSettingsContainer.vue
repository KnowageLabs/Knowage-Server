<template>
    <div v-show="widgetModel">
        <HTMLWidgetSettingsAccordion
            v-if="selectedSetting != 'Gallery'"
            v-show="selectedSetting"
            :widgetModel="widgetModel"
            :settings="descriptor.settings[selectedSetting]"
            :datasets="datasets"
            :selectedDatasets="selectedDatasets"
            :drivers="drivers"
            :variables="variables"
            :dashboardId="dashboardId"
        ></HTMLWidgetSettingsAccordion>

        <HTMLWidgetSettingsGallery v-if="selectedSetting == 'Gallery'" v-show="selectedSetting" :widgetModel="widgetModel" :htmlGalleryProp="htmlGalleryProp" @galleryItemSelected="$emit('galleryItemSelected')"></HTMLWidgetSettingsGallery>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable, IDashboardDriver, IGalleryItem } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from './HTMLWidgetSettingsDescriptor.json'
import HTMLWidgetSettingsAccordion from './HTMLWidgetSettingsAccordion.vue'
import HTMLWidgetSettingsGallery from './gallery/HTMLWidgetGallery.vue'

export default defineComponent({
    name: 'html-widget-settings-container',
    components: { HTMLWidgetSettingsAccordion, HTMLWidgetSettingsGallery },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        drivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        htmlGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['galleryItemSelected'],
    data() {
        return {
            descriptor,
            setting: ''
        }
    },
    created() {},
    methods: {}
})
</script>
