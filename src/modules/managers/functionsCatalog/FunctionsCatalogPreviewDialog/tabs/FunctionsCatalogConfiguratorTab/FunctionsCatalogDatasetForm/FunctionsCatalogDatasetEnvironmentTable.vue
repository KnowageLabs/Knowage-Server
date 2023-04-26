<template>
    <DataTable
        id="environment-datatable"
        v-model:filters="filters"
        :value="environmentLibraries"
        :paginator="true"
        :rows="functionsCatalogDatasetEnvironmentTableDescriptor.rows"
        class="p-datatable-sm kn-table"
        data-key="name"
        :global-filter-fields="functionsCatalogDatasetEnvironmentTableDescriptor.globalFilterFields"
        :responsive-layout="functionsCatalogDatasetEnvironmentTableDescriptor.responsiveLayout"
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
                    <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                </span>
            </div>
        </template>
        <Column v-for="col of functionsCatalogDatasetEnvironmentTableDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :field="col.field" :header="$t(col.header)" :sortable="true"> </Column>
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
