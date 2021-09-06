<template>
    <DataTable
        v-model:selection="selectedFiles"
        :value="files"
        :loading="loading"
        class="p-datatable-sm kn-table"
        dataKey="DOCUMENT_ID"
        responsiveLayout="stack"
        v-model:filters="filters"
        filterDisplay="menu"
        :globalFilterFields="addActionDialogDescriptor.documentFilterFields"
        @row-select="fileSelected"
        @row-unselect="fileSelected"
        @row-select-all="fileSelected"
        @row-unselect-all="fileSelected"
    >
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #loading>
            {{ $t('common.info.dataLoading') }}
        </template>

        <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
        <Column v-for="col of addActionDialogDescriptor.columnsDocument" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" class="kn-truncated">
            <template #body="slotProps">
                <span>{{ slotProps.data[slotProps.column.props.field] }}</span>
            </template>
        </Column>
    </DataTable>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import addActionDialogDescriptor from './AlertDefinitionActionDialogDescriptor.json'

export default defineComponent({
    name: 'etl-card',
    components: {
        DataTable,
        Column
    },
    props: {
        loading: {
            type: Boolean
        },
        files: {
            type: Array as any
        },
        data: {
            type: Object
        }
    },
    created() {
        this.loadData()
    },
    data() {
        return {
            addActionDialogDescriptor,
            selectedFiles: [] as any,
            myData: {} as any,
            filters: {
                global: [filterDefault],
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    methods: {
        loadData() {
            this.myData = this.data as any
            this.selectedFiles = this.myData?.jsonActionParameters?.listDocIdSelected ? [...this.myData.jsonActionParameters.listDocIdSelected] : []
        },
        fileSelected() {
            this.myData.jsonActionParameters.listDocIdSelected = this.selectedFiles.map((doc: any) => {
                return { DOCUMENT_ID: doc.DOCUMENT_ID }
            })
        }
    }
})
</script>
