<template>
    <FunctionsCatalogOutputColumnsCard :columns="selectedFunction.outputColumns" :readonly="readonly"></FunctionsCatalogOutputColumnsCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import FunctionsCatalogOutputColumnsCard from './FunctionsCatalogOutputColumnsCard.vue'

export default defineComponent({
    name: 'function-catalog-output-tab',
    components: { FunctionsCatalogOutputColumnsCard },
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
