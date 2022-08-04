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
                <WidgetEditorTabs class="dashboardEditor-tabs" :propWidget="widget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="onDatasetSelected" />
                <WidgetEditorPreview id="widget-editor-preview" :propWidget="widget" />
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing.
 */
import { defineComponent, PropType } from 'vue'
import { IWidgetEditorDataset, IDatasetOptions, IWidget, IDataset, IModelDataset } from '../../Dashboard'
import { AxiosResponse } from 'axios'
import { createNewWidget, setWidgetModelTempProperty, setWidgetModelFunctions, formatWidgetForSave, formatWidgetColumnsForDisplay } from './helpers/WidgetEditorHelpers'
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
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]> } },
    data() {
        return {
            descriptor,
            widget: {} as any,
            previewData: null as any,
            datasetFunctions: {} as { availableFunctions: string[]; nullifFunction: string[] },
            selectedModelDatasets: [] as IModelDataset[],
            selectedDatasets: [] as IDataset[]
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
    },
    methods: {
        loadWidget() {
            if (!this.propWidget) return
            this.widget = this.propWidget.new ? createNewWidget() : deepcopy(this.propWidget)
            formatWidgetColumnsForDisplay(this.widget)
            setWidgetModelTempProperty(this.widget)
            setWidgetModelFunctions(this.widget)
        },
        loadSelectedModelDatasets() {
            // TODO - remove hardcoded dashboard index
            this.selectedModelDatasets = this.dashboardStore.getDashboardSelectedDatastes(1)
        },
        loadSelectedModel() {
            if (!this.datasets) return
            this.selectedDatasets = []
            for (let i = 0; i < this.selectedModelDatasets.length; i++) {
                const tempDataset = this.selectedModelDatasets[i]
                const index = this.datasets.findIndex((dataset: any) => dataset.id?.dsId === tempDataset.id)
                if (index !== -1) this.selectedDatasets.push({ ...this.datasets[index], cache: tempDataset.cache, indexes: tempDataset.indexes, parameters: tempDataset.parameters as any[] })
            }
        },
        onDatasetSelected(dataset: IWidgetEditorDataset) {
            this.loadPreviewData(dataset)
            this.loadAvailableFunctions(dataset)
        },
        async loadPreviewData(dataset: IWidgetEditorDataset) {
            this.store.setLoading(true)
            // TODO - remove hardcoded
            const postData = {
                aggregations: {
                    measures: [],
                    categories: [],
                    dataset: dataset.label
                },
                parameters: {},
                selections: {},
                indexes: []
            } as IDatasetOptions
            // await this.$http
            //     .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/${dataset.label}/data?offset=0&size=10&nearRealtime=true&widgetName=widget_table_1658220241151`, postData)
            //     .then((response: AxiosResponse<any>) => (this.previewData = response.data))
            //     .catch(() => {})
            this.store.setLoading(false)
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
            const tempWidget = formatWidgetForSave(this.widget)
            if (!tempWidget) return

            if (tempWidget.new) {
                delete tempWidget.new
                this.dashboardStore.createNewWidget(tempWidget)
                this.$emit('widgetSaved')
            } else {
                this.dashboardStore.updateWidget(tempWidget)
                this.$emit('widgetUpdated')
            }
        },
        close() {
            this.$emit('close')
        }
    }
})
</script>
<style lang="scss">
@media screen and (max-width: 1199px) {
    #widget-editor-preview {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 0;
    }
}
@media screen and (min-width: 1200px) {
    #widget-editor-preview {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 0.5;
    }
}
</style>
