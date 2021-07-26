<template>
    <Card>
        <template #content>
            <DataTable
                v-model:selection="myData.jsonActionParameters"
                :value="files.DOCUMENT_ID"
                :loading="loading"
                class="p-datatable-sm kn-table"
                dataKey="DOCUMENT_ID"
                responsiveLayout="stack"
                v-model:filters="filters"
                filterDisplay="menu"
                :globalFilterFields="addActionDialogDescriptor.documentFilterFields"
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
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import addActionDialogDescriptor from './AddActionDialogDescriptor.json'

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
            type: Array
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
            selectedFile: [],
            myData: null,
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
        }
    }
})
</script>
