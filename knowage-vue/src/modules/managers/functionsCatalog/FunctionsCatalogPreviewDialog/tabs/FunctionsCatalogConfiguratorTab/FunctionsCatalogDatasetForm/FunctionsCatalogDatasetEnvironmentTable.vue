<template>
    <DataTable
        id="environment-datatable"
        :value="environmentLibraries"
        :paginator="true"
        :rows="functionsCatalogDatasetEnvironmentTableDescriptor.rows"
        class="p-datatable-sm kn-table"
        dataKey="name"
        v-model:filters="filters"
        :globalFilterFields="functionsCatalogDatasetEnvironmentTableDescriptor.globalFilterFields"
        :responsiveLayout="functionsCatalogDatasetEnvironmentTableDescriptor.responsiveLayout"
        :breakpoint="functionsCatalogDatasetEnvironmentTableDescriptor.breakpoint"
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
        <Column class="kn-truncated" :style="col.style" v-for="col of functionsCatalogDatasetEnvironmentTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLibrary } from '../../../../FunctionsCatalog'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogDatasetEnvironmentTableDescriptor from './FunctionsCatalogDatasetEnvironmentTableDescriptor.json'

export default defineComponent({
    name: 'function-catalog-dataset-environment-table',
    components: { Column, DataTable },
    props: { libraries: { type: Array } },
    data() {
        return {
            functionsCatalogDatasetEnvironmentTableDescriptor,
            filters: { global: [filterDefault] } as Object,
            environmentLibraries: [] as iLibrary[]
        }
    },
    watch: {
        libraries() {
            this.loadLibraries()
        }
    },
    created() {
        this.loadLibraries()
    },
    methods: {
        loadLibraries() {
            this.environmentLibraries = this.libraries as iLibrary[]
        }
    }
})
</script>

<style lang="scss">
#environment-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
