<template>
    <FunctionsCatalogInputColumnsCard class="p-mt-4" :columns="selectedFunction.inputColumns" :readonly="readonly"></FunctionsCatalogInputColumnsCard>
    <FunctionsCatalogInputVariablesCard class="p-mt-4" :variables="selectedFunction.inputVariables" :readonly="readonly"></FunctionsCatalogInputVariablesCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import FunctionsCatalogInputColumnsCard from './FunctionsCatalogInputColumnsCard.vue'
import FunctionsCatalogInputVariablesCard from './FunctionsCatalogInputVariablesCard.vue'

export default defineComponent({
    name: 'function-catalog-input-tab',
    components: { FunctionsCatalogInputColumnsCard, FunctionsCatalogInputVariablesCard },
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
