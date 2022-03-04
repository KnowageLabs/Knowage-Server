<template>
    <DataTable
        class="p-datatable-sm kn-table bml-table kn-flex"
        :value="tableData"
        selectionMode="single"
        dataKey="id"
        responsiveLayout="scroll"
        v-model:filters="filters"
        :globalFilterFields="filterFields"
        :paginator="true"
        :rows="25"
        paginatorTemplate="PrevPageLink PageLinks NextPageLink"
        stripedRows
        @rowSelect="$emit('rowSelected', $event)"
        @rowUnselect="$emit('rowUnselected', $event)"
    >
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
        <Column field="label" :header="$t('common.label')" :sortable="true" :headerStyle="descriptor.style.uppercase" />
        <Column field="name" :header="$t('common.name')" :sortable="true" :headerStyle="descriptor.style.uppercase" />
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
    props: { tableData: { type: Array as any, required: true }, headerTitle: String },
    data() {
        return {
            descriptor,
            loading: false,
            filterFields: ['name', 'label'],
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
