<template>
    <FunctionCatalogOutputColumnsCard :columns="selectedFunction.outputColumns" :readonly="readonly"></FunctionCatalogOutputColumnsCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import FunctionCatalogOutputColumnsCard from './FunctionCatalogOutputColumnsCard.vue'

export default defineComponent({
    name: 'function-catalog-output-tab',
    components: { FunctionCatalogOutputColumnsCard },
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
