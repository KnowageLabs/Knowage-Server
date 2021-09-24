<template>
    <FunctionCatalogInputColumnsCard :columns="selectedFunction.inputColumns" :readonly="readonly"></FunctionCatalogInputColumnsCard>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction } from '../../FunctionsCatalog'
import FunctionCatalogInputColumnsCard from './FunctionCatalogInputColumnsCard.vue'

export default defineComponent({
    name: 'function-catalog-input-tab',
    components: { FunctionCatalogInputColumnsCard },
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
