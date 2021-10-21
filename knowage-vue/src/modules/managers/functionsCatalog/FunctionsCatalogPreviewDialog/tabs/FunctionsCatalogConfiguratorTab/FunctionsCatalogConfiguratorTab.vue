<template>
    <div class="p-d-flex p-flex-row">
        <div class="p-col-4 p-sm-4 p-md-3 p-m-2 p-p-0">
            <FunctionsCatalogDatasetTable :datasets="datasets" @selected="loadSelectedDataset"></FunctionsCatalogDatasetTable>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-m-2 p-p-0">
            <FunctionsCatalogDatasetForm v-if="selectedDataset" :selectedDataset="selectedDataset" :propFunction="propFunction" :pythonEnvironments="pythonEnvironments" :rEnvironments="rEnvironments" @environmentSelected="onEnvironmentSelected" :libraries="libraries"></FunctionsCatalogDatasetForm>
            <div v-else id="no-dataset-selected-info" class="p-d-flex p-flex-row p-jc-center">
                {{ $t('managers.functionsCatalog.noDatasetSelected') }}
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataset, iPythonConfiguration, iLibrary } from '../../../FunctionsCatalog'
import axios from 'axios'
import FunctionsCatalogDatasetTable from './FunctionsCatalogDatasetTable/FunctionsCatalogDatasetTable.vue'
import FunctionsCatalogDatasetForm from './FunctionsCatalogDatasetForm/FunctionsCatalogDatasetForm.vue'

export default defineComponent({
    name: 'function-catalog-configurator-tab',
    components: { FunctionsCatalogDatasetTable, FunctionsCatalogDatasetForm },
    props: { datasets: { type: Array }, propFunction: { type: Object } },
    emits: ['selected', 'loading', 'selectedEnvironment', 'selectedDataset'],
    data() {
        return {
            selectedDataset: null as iDataset | null,
            pythonEnvironments: [] as iPythonConfiguration[],
            rEnvironments: [] as any[],
            libraries: [] as iLibrary[]
        }
    },
    async created() {
        await this.loadEnvironments()
    },
    methods: {
        async loadSelectedDataset(dataset: iDataset) {
            this.$emit('loading', true)
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${dataset.label}`).then((response) => {
                if (response.data) {
                    this.selectedDataset = response.data[0]
                    this.$emit('selectedDataset', this.selectedDataset)
                }
            })
            this.$emit('loading', false)
        },
        async loadEnvironments() {
            this.$emit('loading', true)
            await this.loadEnvironment('PYTHON_CONFIGURATION').then((response) => (this.pythonEnvironments = response.data))
            await this.loadEnvironment('R_CONFIGURATION').then((response) => (this.rEnvironments = response.data))
            this.$emit('loading', false)
        },
        loadEnvironment(type: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/category/${type}`)
        },
        async onEnvironmentSelected(environment: string) {
            this.$emit('selectedEnvironment', environment)
            this.$emit('loading', true)
            if (environment && environment.split('.')[0] === 'python') {
                await this.loadEnvironmentLibraries(`2.0/backendservices/widgets/python/libraries/${environment}`).then((response) => (this.libraries = JSON.parse(response.data.result)))
            }
            this.$emit('loading', false)
        },
        async loadEnvironmentLibraries(url: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `${url}`)
        }
    }
})
</script>

<style lang="scss" scoped>
#no-dataset-selected-info {
    margin: 0 auto;
    border: 1px solid rgba(59, 103, 140, 0.1);
    background-color: #eaf0f6;
    padding: 1rem;
    text-transform: uppercase;
    font-size: 0.8rem;
    width: 80%;
}
</style>
