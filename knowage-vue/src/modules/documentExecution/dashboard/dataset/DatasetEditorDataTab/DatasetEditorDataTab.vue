<template>
    <DataList :dashboardDatasetsProp="dashboardDatasetsProp" :availableDatasetsProp="availableDatasetsProp" :selectedDatasetsProp="selectedDatasets" @addSelectedDatasets="addSelectedDatasets" @datasetSelected="selectDataset" @deleteDataset="$emit('deleteDataset', $event)" />
    <DataDetail :dashboardDatasetsProp="dashboardDatasetsProp" :selectedDatasetProp="selectedDataset" :documentDriversProp="documentDriversProp" :dashboardId="dashboardId" data-test="dataset-detail" />
    <DatasetEditorPreview v-if="selectedDataset.id" id="dataset-editor-preview" :selectedDatasetProp="selectedDataset" data-test="dataset-preview" />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset } from '../../Dashboard'
import DataList from './DatasetEditorDataList/DatasetEditorDataList.vue'
import DataDetail from './DatasetEditorDataDetail/DatasetEditorDataDetail.vue'
import DatasetEditorPreview from '../DatasetEditorPreview.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { DataList, DataDetail, DatasetEditorPreview },
    props: {
        dashboardDatasetsProp: { required: true, type: Array as any },
        availableDatasetsProp: { required: true, type: Array as PropType<IDataset[]> },
        selectedDatasetsProp: { type: Array as any },
        documentDriversProp: { required: true, type: Array as any },
        dashboardId: { type: String, required: true }
    },
    emits: ['addSelectedDatasets', 'deleteDataset'],
    data() {
        return {
            selectedDataset: {} as any,
            selectedDatasets: [] as any[],
            datasetDriversMap: {}
        }
    },
    watch: {
        selectedDatasetsProp() {
            this.loadSelectedDatasets()
        }
    },
    created() {
        this.loadSelectedDatasets()
    },
    methods: {
        selectDataset(datasetId) {
            this.selectedDataset = this.availableDatasetsProp.find((dataset) => dataset.id.dsId === datasetId)
        },
        loadSelectedDatasets() {
            this.selectedDatasets = this.selectedDatasetsProp
        },
        addSelectedDatasets(datasetsToAdd) {
            this.$emit('addSelectedDatasets', datasetsToAdd)
        },
        deleteAndUnselectDataset(event) {
            this.selectedDataset = null
            this.$emit('deleteDataset', event)
        }
    }
})
</script>
<style lang="scss">
@media screen and (max-width: 1199px) {
    #dataset-editor-preview {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 0;
    }
}
@media screen and (min-width: 1200px) {
    #dataset-editor-preview {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 1;
    }
}
</style>
