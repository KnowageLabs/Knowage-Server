<template>
    <div>
        <div class="p-fluid p-field">
            <div class="p-d-flex">
                <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.selectDataset') }} </label>
            </div>
            <Dropdown class="kn-material-input" v-model="selectedDataset" :options="datasetOptions" optionLabel="label" @change="onDatasetSelected"></Dropdown>
        </div>

        <div class="p-d-flex p-jc-around">
            <label class="kn-material-input-label"> {{ $t('common.columns') }} </label>
            <Button class="kn-button kn-button--primary" @click="showCalculatedFieldDialog"> {{ $t('common.addColumn') }}</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { IWidgetEditorDataset } from '../../../../Dashboard'
import descriptor from './WidgetEditorDataListDescriptor.json'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../../../../../../App.store'

export default defineComponent({
    name: 'widget-editor-data-list',
    components: { Dropdown },
    props: { datasets: { type: Array }, modelDatasets: { type: Array } },
    emits: ['datasetSelected'],
    data() {
        return {
            descriptor,
            datasetOptions: [
                {
                    id: 1,
                    label: 'dew',
                    cache: true,
                    parameters: []
                }
            ] as IWidgetEditorDataset[],
            selectedDataset: null as IWidgetEditorDataset | null,
            selectedDatasetColumns: [] as any[]
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        this.loadDatasets()
    },
    methods: {
        loadDatasets() {
            // TODO - remove mocked
            const mockedDatasets = [
                { dsId: 165, name: 'AirBnB-NY 1', dsLabel: 'AirBnB-NY 1 - GIS Working', useCache: true, frequency: 0, parameters: {} },
                { dsId: 166, name: 'AirBnB-NY 1', dsLabel: 'test', useCache: true, frequency: 0, parameters: {} }
            ]

            this.datasetOptions = mockedDatasets.map((dataset: any) => {
                return {
                    id: dataset.dsId,
                    label: dataset.dsLabel,
                    cache: dataset.useCache,
                    parameters: dataset.parameters
                }
            })
            console.log('loadDatasets() - datasetOptions: ', this.datasetOptions)
        },
        onDatasetSelected() {
            console.log('onDatasetSelected() - selectedDataset: ', this.selectedDataset)
            this.loadDatasetColumns()
            this.$emit('datasetSelected', this.selectedDataset)
            console.log('onDatasetSelected() - allDatasets: ', this.datasets)
        },
        showCalculatedFieldDialog() {
            console.log('showCalculatedFieldDialog() - TODO!')
        },
        async loadDatasetColumns() {
            this.store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDataset?.id}`)
                .then((response: AxiosResponse<any>) => (this.selectedDatasetColumns = response.data))
                .catch(() => {})
            this.store.setLoading(false)
            console.log('loadDatasetColumns() - selectedDatasetColumns: ', this.selectedDatasetColumns)
        }
    }
})
</script>
