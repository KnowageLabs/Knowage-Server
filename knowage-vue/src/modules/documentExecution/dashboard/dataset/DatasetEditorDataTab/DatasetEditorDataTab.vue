<template>
    <div class="p-d-flex">
        <DataList class="kn-flex p-my-2 p-ml-2 p-mr-1" :dashboardDatasetsProp="dashboardDatasetsProp" :availableDatasetsProp="availableDatasetsProp" @datasetSelected="selectDataset" />
        <DataDetail class="kn-flex p-my-2 p-mr-2 p-ml-1" :dashboardDatasetsProp="dashboardDatasetsProp" :selectedDatasetProp="selectedDataset" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataList from './DatasetEditorDataList/DatasetEditorDataList.vue'
import DataDetail from './DatasetEditorDataDetail/DatasetEditorDataDetail.vue'

export default defineComponent({
    name: 'dataset-editor-data-tab',
    components: { DataList, DataDetail },
    props: { dashboardDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as any } },
    emits: [],
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
        }
    }
})
</script>
