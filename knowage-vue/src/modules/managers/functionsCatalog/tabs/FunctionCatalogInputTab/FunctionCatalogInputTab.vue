<template>
    <FunctionCatalogInputColumnsCard :columns="selectedFunction.inputColumns" :readonly="readonly"></FunctionCatalogInputColumnsCard>
    <FunctionCatalogInputVariablesCard :variables="selectedFunction.inputVariables" :readonly="readonly"></FunctionCatalogInputVariablesCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import FunctionCatalogInputColumnsCard from './FunctionCatalogInputColumnsCard.vue'
import FunctionCatalogInputVariablesCard from './FunctionCatalogInputVariablesCard.vue'

export default defineComponent({
    name: 'function-catalog-input-tab',
    components: { FunctionCatalogInputColumnsCard, FunctionCatalogInputVariablesCard },
    props: { propFunction: { type: Object }, readonly: { type: Boolean } },
    data() {
        return {
            selectedFunction: {} as iFunction
        }
    },
    created() {
        this.loadFunction()
    },
    methods: {
        loadFunction() {
            this.selectedFunction = this.propFunction as iFunction
        }
    }
})
</script>
