<template>
    <div v-show="widgetModel">
        <ImageWidgetSettingsAccordion v-if="setting != 'Gallery'" :widgetModel="widgetModel" :settings="descriptor.settings[setting]" :datasets="datasets" :selectedDatasets="selectedDatasets" :variables="variables" :dashboardId="dashboardId"></ImageWidgetSettingsAccordion>

        <ImageWidgetGallery v-if="setting == 'Gallery'" :widgetModel="widgetModel" :imagesListProp="imagesList" @uploadedImage="loadImages"></ImageWidgetGallery>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IVariable } from '@/modules/documentExecution/dashboard/Dashboard'
import { IImage } from '@/modules/documentExecution/dashboard/interfaces/DashboardImageWidget'
import { mapActions } from 'pinia'
import { AxiosResponse } from 'axios'
import appStore from '@/App.store'
import descriptor from './ImageWidgetSettingsDescriptor.json'
import ImageWidgetSettingsAccordion from './ImageWidgetSettingsAccordion.vue'
import ImageWidgetGallery from './gallery/ImageWidgetGallery.vue'

export default defineComponent({
    name: 'image-widget-settings-container',
    components: { ImageWidgetSettingsAccordion, ImageWidgetGallery },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        selectedSetting: { type: String, required: true },
        datasets: { type: Array as PropType<IDataset[]> },
        selectedDatasets: { type: Array as PropType<IDataset[]> },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['settingSelected'],
    data() {
        return {
            descriptor,
            imagesList: [] as IImage[],
            setting: ''
        }
    },
    watch: {
        selectedSetting() {
            this.loadSelectedSetting()
        }
    },
    created() {
        this.loadImages()
        this.loadSelectedSetting()
    },
    methods: {
        ...mapActions(appStore, ['setLoading']),
        loadSelectedSetting() {
            if (!this.selectedSetting) {
                this.setting = 'Gallery'
                this.$emit('settingSelected', this.setting)
            } else {
                this.setting = this.selectedSetting
            }
        },
        async loadImages() {
            this.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/images/listImages`)
                .then((response: AxiosResponse<any>) => (this.imagesList = response.data ? response.data.data : []))
                .catch(() => {})
            this.setLoading(false)
        }
    }
})
</script>
