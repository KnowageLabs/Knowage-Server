<template>
    <DataList :dashboardDatasetsProp="dashboardDatasetsProp" :availableDatasetsProp="availableDatasetsProp" :selectedDatasetsProp="selectedDatasetsProp" @addSelectedDatasets="addSelectedDatasets" @datasetSelected="selectDataset" @deleteDataset="$emit('deleteDataset', $event)" />
    <DataDetail :dashboardDatasetsProp="dashboardDatasetsProp" :selectedDatasetProp="selectedDataset" :documentDriversProp="documentDriversProp" />
    <DatasetEditorPreview class="kn-flex" :dashboardDatasetsProp="dashboardDatasetsProp" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataList from './DatasetEditorDataList/DatasetEditorDataList.vue'
import DataDetail from './DatasetEditorDataDetail/DatasetEditorDataDetail.vue'
import DatasetEditorPreview from '../DatasetEditorPreview.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { DataList, DataDetail, DatasetEditorPreview },
    props: { dashboardDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as any }, selectedDatasetsProp: { type: Array as any }, documentDriversProp: { required: true, type: Array as any } },
    emits: ['addSelectedDatasets', 'deleteDataset'],
    data() {
        return {
            selectedDataset: {} as any
        }
    },
    async created() {},
    methods: {
        selectDataset(datasetId) {
            this.selectedDataset = this.availableDatasetsProp.find((dataset) => dataset.id.dsId === datasetId)
        },
        addSelectedDatasets(datasetsToAdd) {
            this.$emit('addSelectedDatasets', datasetsToAdd)
        }
    }
})
</script>
