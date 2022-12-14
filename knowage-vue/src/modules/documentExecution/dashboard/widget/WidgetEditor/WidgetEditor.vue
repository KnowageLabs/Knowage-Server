<template>
    <Teleport to=".dashboard-container">
        <div class="dashboardEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ widget.type }} Widget Editor </template>
                <template #end>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="save" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="close" />
                </template>
            </Toolbar>
            <div class="datasetEditor-container kn-overflow">
                <WidgetEditorTabs
                    class="dashboardEditor-tabs"
                    :propWidget="widget"
                    :datasets="datasets"
                    :selectedDatasets="selectedDatasets"
                    :drivers="drivers"
                    :variables="variables"
                    :dashboardId="dashboardId"
                    :selectedSettingProp="selectedSetting"
                    :htmlGalleryProp="htmlGalleryProp"
                    @settingChanged="onSettingChanged"
                    @datasetSelected="onDatasetSelected"
                />
                <WidgetEditorPreview v-if="selectedSetting != 'Gallery'" :propWidget="widget" :dashboardId="dashboardId" :datasets="selectedModelDatasets" :drivers="documentDrivers" :variables="variables" />
                <!-- <WidgetEditorPreview v-if="selectedSetting != 'Gallery' && false" :propWidget="widget" :dashboardId="dashboardId" :datasets="selectedModelDatasets" :drivers="documentDrivers" :variables="variables" /> -->
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing.
 */
import { defineComponent, PropType } from 'vue'
import { IWidgetEditorDataset, IWidget, IDataset, IModelDataset, IVariable, IDashboardDriver, IGalleryItem } from '../../Dashboard'
import { AxiosResponse } from 'axios'
import { createNewWidget, formatWidgetForSave } from './helpers/WidgetEditorHelpers'
import WidgetEditorPreview from './WidgetEditorPreview.vue'
import WidgetEditorTabs from './WidgetEditorTabs.vue'
import mainStore from '../../../../../App.store'
import descriptor from './WidgetEditorDescriptor.json'
import dashStore from '../../Dashboard.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'widget-editor',
    components: { WidgetEditorPreview, WidgetEditorTabs },
    emits: ['close', 'widgetUpdated', 'widgetSaved'],
    props: {
        dashboardId: { type: String, required: true },
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        documentDrivers: { type: Array as PropType<IDashboardDriver[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true },
        htmlGalleryProp: { type: Array as PropType<IGalleryItem[]>, required: true }
    },
    data() {
        return {
            descriptor,
            widget: {} as any,
            previewData: null as any,
            datasetFunctions: {} as {
                availableFunctions: string[]
                nullifFunction: string[]
            },
            selectedModelDatasets: [] as IModelDataset[],
            selectedDatasets: [] as IDataset[],
            drivers: [] as any[],
            selectedSetting: ''
        }
    },
    watch: {
        propWidget() {
            this.loadWidget()
        }
    },
    setup() {
        const store = mainStore()
        const dashboardStore = dashStore()
        return { store, dashboardStore }
    },
    created() {
        this.loadWidget()
        this.loadSelectedModelDatasets()
        this.loadSelectedModel()
        this.loadDrivers()
    },
    methods: {
        loadWidget() {
            if (!this.propWidget) return
            this.widget = this.propWidget.new ? createNewWidget(this.propWidget.type) : deepcopy(this.propWidget)
        },
        loadSelectedModelDatasets() {
            this.selectedModelDatasets = this.dashboardId ? this.dashboardStore.getDashboardSelectedDatasets(this.dashboardId) : {}
        },
        loadSelectedModel() {
            if (!this.datasets) return
            this.selectedDatasets = [] as IDataset[]
            for (let i = 0; i < this.selectedModelDatasets.length; i++) {
                const tempDataset = this.selectedModelDatasets[i]
                const index = this.datasets.findIndex((dataset: any) => dataset.id.dsId === tempDataset.id)
                if (index !== -1)
                    this.selectedDatasets.push({
                        ...this.datasets[index],
                        cache: tempDataset.cache,
                        indexes: tempDataset.indexes ?? [],
                        parameters: tempDataset.parameters as any[],
                        drivers: tempDataset.drivers ?? []
                    })
            }
        },
        loadDrivers() {
            this.drivers = this.documentDrivers as any[]
        },
        onDatasetSelected(dataset: IWidgetEditorDataset) {
            this.loadAvailableFunctions(dataset)
        },
        async loadAvailableFunctions(dataset: IWidgetEditorDataset) {
            this.store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/availableFunctions/${dataset.id}?useCache=false`)
                .then((response: AxiosResponse<any>) => (this.datasetFunctions = response.data))
                .catch(() => {})
            this.store.setLoading(false)
        },
        save() {
            const tempWidget = deepcopy(this.widget)
            if (!tempWidget) return

            if (tempWidget.new) {
                delete tempWidget.new
                this.dashboardStore.createNewWidget(this.dashboardId, tempWidget)
                console.log('>>>>>>>>>>>>>>> TEMP WIDGET: ', tempWidget)
                this.$emit('widgetSaved')
            } else {
                this.dashboardStore.updateWidget(this.dashboardId, tempWidget)
                console.log('>>>>>>>>>>>>>>> TEMP WIDGET: ', tempWidget)
                this.$emit('widgetUpdated')
            }
        },
        close() {
            this.$emit('close')
        },
        onSettingChanged(setting: string) {
            this.selectedSetting = setting
        }
    }
})
</script>
<style lang="scss">
.widget-editor-card {
    color: rgba(0, 0, 0, 0.87);
    box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
}

.icon-disabled {
    color: #c2c2c2;
}
</style>
