<template>
    <DataTable :value="kpisList" class="p-datatable-sm kn-table" dataKey="id" v-model:filters="filters" :globalFilterFields="kpiCardDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="960px">
        <template #header>
            <div class="table-header p-d-flex">
                <span class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                </span>
            </div>
        </template>
        <Column class="kn-truncated" v-for="col of kpiCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"> </Column>
        <Column :style="kpiCardDescriptor.table.iconColumn.style">
            <template #body="slotProps">
                <Button icon="pi pi-copy" class="p-button-link" @click="cloneKpiConfirm(slotProps.data)" data-test="clone-button" />
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteMeasureConfirm(slotProps.data)" :data-test="'delete-button-' + slotProps.data.id" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import kpiCardDescriptor from './KpiCardDescriptor.json'

export default defineComponent({
    name: 'kpi-card',
    components: { Column, DataTable },
    props: { kpis: { type: Array } },
    emits: ['touched'],
    data() {
        return {
            kpiCardDescriptor,
            kpisList: [] as any[],
            filters: { global: [filterDefault] } as Object
        }
    },
    created() {
        this.loadKpis()
    },
    methods: {
        loadKpis() {
            this.kpisList = this.kpis as any[]
            console.log('KpiCard KPI: ', this.kpisList)
        }
    }
})
</script>
