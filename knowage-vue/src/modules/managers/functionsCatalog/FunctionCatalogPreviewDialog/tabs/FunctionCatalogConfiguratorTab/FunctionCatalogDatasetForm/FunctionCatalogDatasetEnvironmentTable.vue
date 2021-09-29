<template>
    <DataTable
        id="environment-datatable"
        :value="datasets"
        :paginator="true"
        :rows="15"
        class="p-datatable-sm kn-table"
        dataKey="id"
        v-model:filters="filters"
        :globalFilterFields="functionCatalogDatasetEnvironmentTableDescriptor.globalFilterFields"
        :responsiveLayout="functionCatalogDatasetEnvironmentTableDescriptor.responsiveLayout"
        :breakpoint="functionCatalogDatasetEnvironmentTableDescriptor.breakpoint"
        @rowClick="$emit('selected', $event.data)"
    >
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
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
        <Column class="kn-truncated" :style="col.style" v-for="col of functionCatalogDatasetEnvironmentTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionCatalogDatasetEnvironmentTableDescriptor from './FunctionCatalogDatasetEnvironmentTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dataset-environment-table',
    components: { Column, DataTable },
    data() {
        return {
            functionCatalogDatasetEnvironmentTableDescriptor,
            filters: { global: [filterDefault] } as Object
        }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss">
#environment-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
