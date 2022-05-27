<template>
    <DataTable
        class="p-datatable-sm kn-table bml-table kn-flex"
        :value="tableData"
        v-model:selection="selectedRow"
        selectionMode="single"
        dataKey="id"
        responsiveLayout="scroll"
        v-model:filters="filters"
        :globalFilterFields="filterFields"
        :paginator="true"
        :rows="25"
        :loading="loading"
        paginatorTemplate="PrevPageLink PageLinks NextPageLink"
        stripedRows
        :metaKeySelection="false"
        @rowSelect="$emit('rowSelected', $event, dataType)"
        @rowUnselect="$emit('rowUnselected', $event, dataType)"
    >
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ headerTitle }}
                </template>
            </Toolbar>
            <span id="search-container" class="p-input-icon-left p-m-2">
                <i class="pi pi-search" />
                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
            </span>
        </template>
        <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :headerStyle="descriptor.style.uppercase">
            <template #body="slotProps">
                <span class="kn-truncated" v-tooltip.top="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
            </template>
        </Column>
    </DataTable>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import descriptor from './BehaviouralModelLineageDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'behavioural-model-lineage',
    components: { DataTable, Column },
    props: { tableData: { type: Array as any, required: true }, headerTitle: String, dataType: String, loading: Boolean },
    data() {
        return {
            descriptor,
            filterFields: ['name', 'label'],
            columns: [
                { field: 'name', header: 'common.label' },
                { field: 'label', header: 'common.name' }
            ],
            filters: { global: [filterDefault] } as Object,
            selectedRow: {} as any
        }
    },
    created() {},
    methods: {}
})
</script>
<style lang="scss">
.bml-table .p-datatable-header {
    padding: 0 !important;
}
</style>
