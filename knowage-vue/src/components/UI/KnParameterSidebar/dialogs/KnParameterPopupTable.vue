<template>
    <DataTable
        :value="rows"
        class="p-datatable-sm kn-table"
        v-model:selection="selectedRow"
        :selectionMode="multivalue ? false : 'single'"
        v-model:filters="filters"
        :globalFilterFields="globalFilterFields"
        :paginator="rows.length > 20"
        :rows="20"
        responsiveLayout="stack"
        breakpoint="600px"
        :scrollable="true"
        :scrollHeight="knParameterPopupDialogDescriptor.dialog.scrollHeight"
        @row-select="$emit('selected', selectedRow)"
        @row-unselect="$emit('selected', selectedRow)"
        @row-select-all="$emit('selected', selectedRow)"
        @row-unselect-all="$emit('selected', selectedRow)"
    >
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="knParameterPopupDialogDescriptor.styles.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
        </template>

        <template #header>
            <div class="table-header p-d-flex p-ai-center">
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                </span>
            </div>
        </template>

        <Column v-if="multivalue" selectionMode="multiple" :headerStyle="knParameterPopupDialogDescriptor.styles.checkboxColumn"></Column>
        <Column class="kn-truncated" v-for="col of columns" :field="col.field" :header="col.header" :key="col.field" :sortable="true"> </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import knParameterPopupDialogDescriptor from './KnParameterPopupDialogDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'kn-parameter-popup-dialog',
    components: { Column, DataTable, Message },
    props: { parameterPopUpData: { type: Object }, multivalue: { type: Boolean }, multipleSelectedRows: { type: Array } },
    data() {
        return {
            knParameterPopupDialogDescriptor,
            rows: [] as any[],
            columns: [] as { header: string; field: string }[],
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: [] as string[],
            selectedRow: null as any
        }
    },
    watch: {
        parameterPopUpData() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.rows = this.parameterPopUpData?.result.data

            this.columns = []
            Object.keys(this.parameterPopUpData?.result.metadata.colsMap).forEach((key: string) => {
                this.columns.push({ header: this.parameterPopUpData?.result.metadata.colsMap[key], field: key })
            })

            this.columns.forEach((el: any) => this.globalFilterFields.push(el.field))

            this.selectedRow = this.multipleSelectedRows
        }
    }
})
</script>
