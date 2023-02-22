<template>
    <DataTable
        id="preview-datatable"
        v-model:filters="filters"
        :value="rows"
        :paginator="true"
        :rows="functionsCatalogPreviewTableDescriptor.rows"
        class="p-datatable-sm kn-table"
        filter-display="menu"
        :global-filter-fields="globalFilterFields"
        :responsive-layout="functionsCatalogPreviewTableDescriptor.responsiveLayout"
        :breakpoint="functionsCatalogPreviewTableDescriptor.breakpoint"
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
        <Column v-for="col of columns" :key="col.field" class="kn-truncated" :field="col.field" :header="col.header" :sortable="true">
            <template #filter="{filterModel}">
                <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import functionsCatalogPreviewTableDescriptor from './FunctionsCatalogPreviewTableDescriptor.json'
import { FilterOperator } from 'primevue/api'

export default defineComponent({
    name: 'function-catalog-preview-table',
    components: { Column, DataTable },
    props: { previewColumns: { type: Array }, previewRows: { type: Array } },
    data() {
        return {
            functionsCatalogPreviewTableDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: [] as string[]
        }
    },
    watch: {
        previewColumns() {
            this.loadColumns()
        },
        previewRows() {
            this.loadRows()
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
    },
    methods: {
        loadColumns() {
            this.columns = []
            this.previewColumns?.forEach((el: any) => {
                this.columns.push(el)
                this.globalFilterFields.push(el.field)
                this.filters[el.field] = { operator: FilterOperator.AND, constraints: [filterDefault] }
            })
        },
        loadRows() {
            this.rows = this.previewRows as any[]
        }
    }
})
</script>

<style lang="scss">
#preview-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
