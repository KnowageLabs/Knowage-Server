<template>
    <div class="p-d-flex p-flex-row">
        <Card id="form-container" class="p-m-2">
            <template #content>
                <div class="p-m-2">
                    <label class="kn-material-input-label"> {{ $t('common.description') }}</label>
                    <p v-html="selectedFunction.description"></p>
                </div>
                <div v-if="selectedFunction.benchmarks" class="p-m-2">
                    <label class="kn-material-input-label"> {{ $t('managers.functionsCatalog.benchmarks') }}</label>
                    <p v-html="selectedFunction.benchmarks"></p>
                </div>
                <div v-if="selectedFunction.inputColumns.length > 0" class="p-m-2">
                    <label class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnsSettings') }}</label>
                    <FunctionCatalogDatasetFormColumnsTable :columns="selectedFunction.inputColumns" :datasetColumns="datasetColumns"></FunctionCatalogDatasetFormColumnsTable>
                </div>
                <div v-if="selectedFunction.inputVariables.length > 0" class="p-mx-2 p-mt-3">
                    <label class="kn-material-input-label"> {{ $t('managers.functionsCatalog.variablesSettings') }}</label>
                    <FunctionCatalogDatasetFormVariablesTable :variables="selectedFunction.inputVariables"></FunctionCatalogDatasetFormVariablesTable>
                </div>
                <div class="p-mx-2 p-mt-3">
                    <label class="kn-material-input-label">{{ $t('managers.functionsCatalog.environment') }}</label>
                    <Dropdown class="kn-material-input" v-model="selectedEnvironment" :options="selectedFunction.language == 'Python' ? pythonEnvironments : rEnvironments" optionLabel="label" optionValue="label" @change="$emit('environmentSelected', selectedEnvironment)" />
                </div>
                <div v-if="selectedEnvironment" class="p-mx-2 p-mt-3">
                    <FunctionCatalogDatasetEnvironmentTable :libraries="libraries"></FunctionCatalogDatasetEnvironmentTable>
                </div>
            </template>
        </Card>
        <FunctionCatalogParametersForm v-if="selectedDataset.pars.length > 0" id="parameters-form" class="p-m-2" :propParameters="selectedDataset.pars"></FunctionCatalogParametersForm>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataset, iFunction, iInputColumn } from '../../../../FunctionsCatalog'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import functionCatalogDatasetFormDescriptor from './FunctionCatalogDatasetFormDescriptor.json'
import FunctionCatalogDatasetFormColumnsTable from './FunctionCatalogDatasetFormColumnsTable.vue'
import FunctionCatalogDatasetFormVariablesTable from './FunctionCatalogDatasetFormVariablesTable.vue'
import FunctionCatalogDatasetEnvironmentTable from './FunctionCatalogDatasetEnvironmentTable.vue'
import FunctionCatalogParametersForm from './FunctionCatalogParametersForm.vue'

export default defineComponent({
    name: 'function-catalog-dateset-form',
    components: { Card, Dropdown, FunctionCatalogDatasetFormColumnsTable, FunctionCatalogDatasetFormVariablesTable, FunctionCatalogDatasetEnvironmentTable, FunctionCatalogParametersForm },
    props: { selectedDataset: { type: Object }, propFunction: { type: Object }, pythonEnvironments: { type: Array }, rEnvironments: { type: Array }, libraries: { type: Array } },
    emits: ['environmentSelected'],
    data() {
        return {
            functionCatalogDatasetFormDescriptor,
            selectedFunction: {} as iFunction,
            dataset: {} as iDataset,
            datasetColumns: [] as any[],
            selectedEnvironment: null
        }
    },
    watch: {
        selectedDataset() {
            this.loadDataset()
        }
    },
    created() {
        this.loadDataset()
        this.loadFunction()
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset as iDataset
            if (this.dataset && this.dataset.meta) {
                this.getDatasetColumns()
            }
            this.clearInputColumnDatasetColumn()
        },
        loadFunction() {
            this.selectedFunction = this.propFunction as iFunction
        },
        getDatasetColumns() {
            this.datasetColumns = []
            for (let i = 2; i < this.dataset.meta.columns.length; i += 3) {
                if (this.dataset.meta.columns[i]) {
                    this.datasetColumns.push(this.dataset.meta.columns[i].pvalue)
                }
            }
            console.log('DATASET COLUMNS: ', this.datasetColumns)
        },
        clearInputColumnDatasetColumn() {
            this.selectedFunction.inputColumns?.forEach((el: iInputColumn) => (el.dsColumn = ''))
        }
    }
})
</script>

<style lang="scss" scoped>
#form-container {
    flex: 3;
}

#parameters-form {
    flex: 1;
}
</style>
