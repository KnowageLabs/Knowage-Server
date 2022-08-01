<template>
    <Teleport to=".dashboard-container">
        <div class="widgetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ widget.type }} Widget Editor </template>
                <template #end>
                    <Button class="kn-button p-button-text" @click="save">{{ $t('common.save') }}</Button>
                    <Button class="kn-button p-button-text" @click="close">{{ $t('common.close') }}</Button>
                </template>
            </Toolbar>
            <div class="widgetEditor-container">
                <WidgetEditorTabs :propWidget="widget" :datasets="datasets" :selectedDatasets="selectedDatasets" @datasetSelected="onDatasetSelected" />
                <WidgetEditorPreview :propWidget="widget" />
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
import { createNewWidget, setWidgetModelTempProperty, setWidgetModelFunctions } from './helpers/WidgetEditorHelpers'
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
            setWidgetModelTempProperty(this.widget)
            setWidgetModelFunctions(this.widget)
            console.log(' ----------------- WIDGET: ', this.widget)
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
            if (this.widget.new) {
                this.dashboardStore.createNewWidget(this.widget)
                this.$emit('widgetSaved')
            } else {
                this.dashboardStore.updateWidget(this.widget)
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
.widgetEditor {
    height: 100vh;
    width: 100%;
    top: 0;
    left: 0;
    background-color: white;
    position: absolute;
    z-index: 999;
    display: flex;
    flex-direction: column;
    .widgetEditor-container {
        flex: 1;
        display: flex;
    }
}
</style>
