<template>
    <Teleport to=".dashboard-container">
        <div class="widgetEditor">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ widget.type }} Widget Editor </template>
                <template #end> <Button @click="close">Close</Button> </template>
            </Toolbar>
            <div class="widgetEditor-container">
                <WidgetEditorTabs :datasets="datasets" @datasetSelected="onDatasetSelected" />
                <WidgetEditorPreview :widget="widget" />
            </div>
        </div>
    </Teleport>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing.
 */
import { defineComponent } from 'vue'
import { IWidgetEditorDataset, IDatasetOptions } from '../../Dashboard'
import { AxiosResponse } from 'axios'
import WidgetEditorPreview from './WidgetEditorPreview.vue'
import WidgetEditorTabs from './WidgetEditorTabs.vue'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'widget-editor',
    components: { WidgetEditorPreview, WidgetEditorTabs },
    emits: ['close'],
    props: { widget: { required: true, type: Object }, datasets: { type: Array } },
    data() {
        return {
            previewData: null as any,
            datasetFunctions: {} as { availableFunctions: string[]; nullifFunction: string[] }
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    methods: {
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
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/${dataset.label}/data?offset=0&size=10&nearRealtime=true&widgetName=widget_table_1658220241151`, postData)
                .then((response: AxiosResponse<any>) => (this.previewData = response.data))
                .catch(() => {})
            this.store.setLoading(false)
            console.log('loadPreviewData() - previewData: ', this.previewData)
        },
        async loadAvailableFunctions(dataset: IWidgetEditorDataset) {
            this.store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/availableFunctions/${dataset.id}?useCache=false`)
                .then((response: AxiosResponse<any>) => (this.datasetFunctions = response.data))
                .catch(() => {})
            this.store.setLoading(false)
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
