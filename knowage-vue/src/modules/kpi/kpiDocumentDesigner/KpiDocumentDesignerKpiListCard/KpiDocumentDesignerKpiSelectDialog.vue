<template>
    <Dialog id="kpi-edit-kpi-select-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="kpiDocumentDesignerKpiListCardDescriptor.dialog.style" :contentStyle="kpiDocumentDesignerKpiListCardDescriptor.dialog.contentStyle" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('kpi.kpiDocumentDesigner.kpiList') }}
                </template>
            </Toolbar>
        </template>

        <DataTable
            :value="kpiList"
            v-model:selection="selectedKpiAssociations"
            class="p-datatable-sm kn-table"
            dataKey="id"
            v-model:filters="filters"
            :kpiDocumentDesignerKpiListCardDescriptor="kpiDocumentDesignerKpiListCardDescriptor.selectKpiGlobalFilterFields"
            responsiveLayout="stack"
            breakpoint="960px"
            :scrollable="true"
            scrollHeight="80vh"
        >
            <template #header>
                <div class="table-header p-d-flex">
                    <span class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column selectionMode="multiple" headerStyle="width: 3em"></Column>
            <Column class="kn-truncated" field="name" :header="$t('kpi.kpiScheduler.kpiName')" key="name" :sortable="true"> </Column>
            <Column class="kn-truncated" field="category.valueCd" :header="$t('common.category')" key="category.valueCd" :sortable="true"> </Column>
            <Column class="kn-truncated" field="dateCreation" :header="$t('kpi.kpiScheduler.kpiName')" key="dateCreation" :sortable="true">
                <template #body="slotProps">
                    <span>{{ getFormattedDate(slotProps.data.dateCreation) }}</span>
                </template>
            </Column>
            <Column class="kn-truncated" field="author" :header="$t('common.author')" key="author" :sortable="true"> </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--primary" :label="$t('common.close')" @click="closeKpiAssociations"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="addKpiAssociations"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import { iKpi, iKpiListItem } from '../KpiDocumentDesigner'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import kpiDocumentDesignerKpiListCardDescriptor from './KpiDocumentDesignerKpiListCardDescriptor.json'

export default defineComponent({
    name: 'kpi-edit-kpi-select-dialog',
    components: { Column, DataTable, Dialog },
    props: { visible: { type: Boolean }, kpiList: { type: Array as PropType<iKpi[]> }, data: { type: Object as PropType<{ kpi: iKpiListItem[] }>, required: true } },
    emits: ['close', 'kpiSelected'],
    data() {
        return {
            kpiDocumentDesignerKpiListCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedKpiAssociations: [] as iKpi[]
        }
    },
    watch: {
        visible() {
            this.setSelectedKpiAssociations()
        }
    },
    async created() {
        this.setSelectedKpiAssociations()
    },
    methods: {
        getFormattedDate(date: any) {
            return formatDate(date)
        },
        setSelectedKpiAssociations() {
            this.selectedKpiAssociations = []

            if (this.data.kpi) {
                this.kpiList?.forEach((kpi: iKpi) => {
                    const index = this.data.kpi.findIndex((kpiListItem: iKpiListItem) => kpi.name === kpiListItem.name)
                    if (index !== -1) this.selectedKpiAssociations.push(kpi)
                })
            }
        },
        closeKpiAssociations() {
            this.selectedKpiAssociations = []
            this.$emit('close')
        },
        addKpiAssociations() {
            this.$emit('kpiSelected', this.selectedKpiAssociations)
        }
    }
})
</script>

<style lang="scss">
#kpi-edit-kpi-select-dialog .p-dialog-header,
#kpi-edit-kpi-select-dialog .p-dialog-content {
    padding: 0;
}
#kpi-edit-kpi-select-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
