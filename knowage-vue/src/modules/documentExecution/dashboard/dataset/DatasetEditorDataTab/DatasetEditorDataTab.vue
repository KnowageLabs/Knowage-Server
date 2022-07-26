<template>
    <DataList class="" :dashboardDatasetsProp="dashboardDatasetsProp" :availableDatasetsProp="availableDatasetsProp" :selectedDatasetsProp="selectedDatasetsProp" @addSelectedDatasets="addSelectedDatasets" @datasetSelected="selectDataset" />
    <DataDetail class="kn-flex" :dashboardDatasetsProp="dashboardDatasetsProp" :selectedDatasetProp="selectedDataset" />
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
    props: { dashboardDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as any }, selectedDatasetsProp: { required: true, type: Array as any } },
    emits: ['addSelectedDatasets'],
    data() {
        return {
            selectedDataset: {} as any
        }
    },
    async created() {},
    methods: {
        selectDataset(datasetId) {
            this.selectedDataset = this.availableDatasetsProp.find((dataset) => dataset.id.dsId === datasetId)
            console.log('selectedDataset', this.selectedDataset)
        },
        addSelectedDatasets(datasetsToAdd) {
            this.$emit('addSelectedDatasets', datasetsToAdd)
        }
    }
})
</script>
