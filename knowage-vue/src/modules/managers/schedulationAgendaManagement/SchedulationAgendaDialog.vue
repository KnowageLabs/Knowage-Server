<template>
    <Dialog :visible="true" :modal="true" class="kn-dialog--toolbar--primary" :header="title" :closable="false" :style="schedulationAgendaDescriptor.form.style">
        <form class="p-fluid p-m-5">
            <DataTable
                id="dataitem-datatable"
                :value="itemList"
                :rows="10"
                :loading="loading"
                class="p-datatable-sm kn-table"
                dataKey="id"
                v-model:filters="filters"
                v-model:selection="selectedItem"
                :globalFilterFields="schedulationAgendaDescriptor.globalFilterFields"
                :responsiveLayout="schedulationAgendaDescriptor.responsiveLayout"
                :breakpoint="schedulationAgendaDescriptor.breakpoint"
                @rowClick="selectRow"
                :paginator="true"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[10, 15, 20]"
                :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
            >
                <template #empty>
                    <div id="noDataFound">
                        {{ $t('managers.schedulationAgendaManagement.info.noDataFound') }}
                    </div>
                </template>
                <template #loading v-if="loading">
                    {{ $t('managers.schedulationAgendaManagement.info.dataLoading') }}
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3 p-col-12">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column class="kn-truncated" :style="col.style" v-for="col of schedulationAgendaDescriptor.columns" :field="col.field" :header="$t(col.header)" :sortable="false" :key="col.field"></Column>
            </DataTable>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('managers.schedulationAgendaManagement.detail.close')" @click="closeTemplate" />
            <Button class="kn-button kn-button--primary" :label="$t('managers.schedulationAgendaManagement.detail.select')" @click="handleSubmit" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataItem } from './SchedulationAgenda'
import schedulationAgendaDescriptor from './SchedulationAgendaDescriptor.json'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'

export default defineComponent({
    name: 'configuration-management-dialog',
    components: { Dialog, DataTable, Column },
    props: {
        model: Object,
        title: String,
        itemList: Array
    },
    emits: ['close', 'changed'],

    data() {
        return {
            schedulationAgendaDescriptor: schedulationAgendaDescriptor,
            selectedItem: null as iDataItem | null,
            dirty: false,
            options: [true, false],
            loading: false,
            columns: schedulationAgendaDescriptor.columns,
            filters: {
                global: [filterDefault],
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                description: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    mounted() {
        if (this.model) {
            this.selectedItem = { ...this.model } as iDataItem
        }
    },
    computed: {
        buttonDisabled(): any {
            return false
        }
    },
    watch: {
        model() {
            this.selectedItem = { ...this.model } as iDataItem
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        selectRow(event) {
            if (event) {
                this.selectedItem = event.data
            }
        },
        handleSubmit() {
            this.$emit('changed', this.selectedItem)
        }
    }
})
</script>
