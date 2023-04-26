<template>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" :hidden="!load" data-test="related-docs-progress-bar" />

    <DataTable
        v-model:selection="selectedProduct1"
        v-model:filters="filters"
        :value="relatedDocumentsList"
        selection-mode="single"
        :paginator="true"
        :loading="load"
        :rows="20"
        class="p-datatable-sm kn-table"
        data-key="id"
        filter-display="menu"
        :global-filter-fields="menuConfigurationRelatedDocumentsDescriptor.globalFilterFields"
        :rows-per-page-options="[10, 15, 20]"
        responsive-layout="stack"
        breakpoint="960px"
        :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
        data-test="related-documents-list"
        @rowSelect="onDocumentSelect"
    >
        <template #header>
            <div class="table-header">
                <span class="p-input-icon-left">
                    <i class="pi pi-search" />
                    <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>
        <template #loading>
            {{ $t('common.info.dataLoading') }}
        </template>

        <Column v-for="col of menuConfigurationRelatedDocumentsDescriptor.columns" :key="col.field" :field="col.field" :header="$t(col.header)" :style="menuConfigurationRelatedDocumentsDescriptor.table.column.style" :sortable="true" class="kn-truncated">
            <template #filter="{ filterModel }">
                <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
            </template>
            <template #body="slotProps">
                <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import ProgressBar from 'primevue/progressbar'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import MenuConfigurationRelatedDocumentsDescriptor from './MenuManagementRelatedDocumentsDescriptor.json'
import { AxiosResponse } from 'axios'
import { iDocument } from '../MenuManagement'

export default defineComponent({
    name: 'related-documents-list',
    components: {
        DataTable,
        Column,
        ProgressBar
    },
    props: {
        documents: Object,
        loading: Boolean
    },
    emits: ['selectedDocument'],
    data() {
        return {
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            load: false as boolean,
            relatedDocumentsList: [] as iDocument[],
            filters: {
                global: [filterDefault],
                DOCUMENT_NAME: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                DOCUMENT_DESCR: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object,
            selectedDocument: null as iDocument | null,
            menuConfigurationRelatedDocumentsDescriptor: MenuConfigurationRelatedDocumentsDescriptor
        }
    },
    watch: {
        loading: {
            handler: function (l) {
                this.load = l
            }
        }
    },
    async created() {
        await this.loadRelatedDocuments()
    },
    methods: {
        async loadRelatedDocuments() {
            this.load = true
            await this.$http
                .get(this.apiUrl + 'documents/listDocument')
                .then((response: AxiosResponse<any>) => {
                    this.relatedDocumentsList = response.data.item
                })
                .finally(() => (this.load = false))
        },
        onDocumentSelect(event: any) {
            this.$emit('selectedDocument', event.data)
        }
    }
})
</script>
