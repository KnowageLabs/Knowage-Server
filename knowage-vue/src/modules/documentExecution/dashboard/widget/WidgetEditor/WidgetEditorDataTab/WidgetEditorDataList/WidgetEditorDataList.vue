<template>
    <div>
        <div>
                <label class="kn-material-input-label"> {{ $t('dashboard.filters.targetType') }} </label>
            <Dropdown class="kn-material-input" v-model="selectedDataset" :options="datasetOptions" optionValue="label" @change="onDatasetSelected"></Dropdown>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import {IWidgetEditorDataset} from '../../../../Dashboard'
import descriptor from './WidgetEditorDataListDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-data-list',
    components: {Dropdown},
    props: {datasets: {type: Array} },
    data() {
        return {
            descriptor,
            datasetOptions: [] as IWidgetEditorDataset[],
            selectedDataset: null as IWidgetEditorDataset | null
        }
    },
    async created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            // TODO - remove mocked
            const mockedDatasets = [ { "dsId": 165,
                "name": "AirBnB-NY 1",
                "dsLabel": "AirBnB-NY 1 - GIS Working",
                "useCache": true,
                "frequency": 0,
                "parameters": {}}]

            this.datasetOptions = mockedDatasets.map((dataset: any) => {return {
                id: dataset.dsId,
                label: dataset.dsLabel,
                cache: dataset.useCache,
                parameters: dataset.parameters,
            }})
            console.log("loadDatasets() - datasetOptions: ", this.datasetOptions)
        },
        onDatasetSelected() {
            console.log("onDatasetSelected() - selectedDataset: ", this.selectedDataset)
        }
    }
})
</script>