<template>
    <div>
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0">
            <FunctionCatalogDatasetTable :datasets="datasets"></FunctionCatalogDatasetTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FunctionCatalogDatasetTable from './FunctionCatalogDatasetTable/FunctionCatalogDatasetTable.vue'

export default defineComponent({
    name: 'function-catalog-configurator-tab',
    components: { FunctionCatalogDatasetTable },
    props: { datasets: { type: Array } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
