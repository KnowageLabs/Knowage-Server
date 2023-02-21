<template>
    <Dialog :visible="true" :modal="true" class="kn-dialog--toolbar--primary" :header="title" :closable="false" :style="schedulationAgendaDescriptor.form.style">
        <form class="p-fluid p-m-5">
            <DataTable
                id="dataitem-datatable"
                v-model:filters="filters"
                v-model:selection="selectedItem"
                :value="itemList"
                :rows="10"
                :loading="loading"
                class="p-datatable-sm kn-table"
                data-key="id"
                :global-filter-fields="schedulationAgendaDescriptor.globalFilterFields"
                :responsive-layout="schedulationAgendaDescriptor.responsiveLayout"
                :breakpoint="schedulationAgendaDescriptor.breakpoint"
                :paginator="true"
                paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rows-per-page-options="[10, 15, 20]"
                :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                @rowClick="selectRow"
            >
                <template #empty>
                    <div id="noDataFound">
                        {{ $t('managers.schedulationAgendaManagement.info.noDataFound') }}
                    </div>
                </template>
                <template v-if="loading" #loading>
                    {{ $t('managers.schedulationAgendaManagement.info.dataLoading') }}
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3 p-col-12">
                            <i class="pi pi-search" />
                            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                        </span>
                    </div>
                </template>
                <Column v-for="col of schedulationAgendaDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :field="col.field" :header="$t(col.header)" :sortable="false"></Column>
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
    mounted() {
        if (this.model) {
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
