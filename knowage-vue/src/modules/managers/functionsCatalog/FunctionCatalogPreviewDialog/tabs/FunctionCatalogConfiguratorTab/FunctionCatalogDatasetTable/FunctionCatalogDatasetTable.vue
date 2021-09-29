<template>
    <Card>
        <template #content>
            <DataTable
                id="dataset-datatable"
                :value="datasets"
                :paginator="true"
                :rows="15"
                :loading="loading"
                class="p-datatable-sm kn-table"
                dataKey="id"
                v-model:filters="filters"
                :globalFilterFields="functionCatalogDatasetTableDescriptor.globalFilterFields"
                :responsiveLayout="functionCatalogDatasetTableDescriptor.responsiveLayout"
                :breakpoint="functionCatalogDatasetTableDescriptor.breakpoint"
                @rowClick="$emit('selected', $event.data)"
            >
                <template #empty>
                    <div id="noFunctionsFound">
                        {{ $t('managers.functionsCatalog.noFunctionsFound') }}
                    </div>
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3 p-col-12">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column class="kn-truncated" :style="col.style" v-for="col of functionCatalogDatasetTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionCatalogDatasetTableDescriptor from './FunctionCatalogDatasetTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dateset-table',
    components: { Card, Column, DataTable },
    props: { datasets: { type: Array } },
    emits: ['selected'],
    data() {
        return { functionCatalogDatasetTableDescriptor, filters: { global: [filterDefault] } as Object }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
#dataset-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
