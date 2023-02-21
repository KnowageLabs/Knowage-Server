<template>
    <div class="p-d-flex p-flex-row p-jc-center">
        <Message v-if="searchMode" id="documents-found-hint" class="p-m-2" severity="info" :closable="false" :style="documentBrowserTableDescriptor.styles.message">
            {{ documents.length + ' ' + $t('documentBrowser.documentsFound') }}
        </Message>
    </div>
    <div v-if="!searchMode" class="table-header p-d-flex">
        <span class="p-input-icon-left p-mr-3 p-col-12">
            <i class="pi pi-search" />
            <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
        </span>
    </div>
    <div class="kn-overflow-y last-flex-container kn-flex">
        <DataTable
            id="documents-datatable"
            v-model:first="first"
            v-model:filters="filters"
            :value="documents"
            :paginator="documents.length > documentBrowserTableDescriptor.rows"
            paginator-template="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink"
            :current-page-report-template="
                $t('common.table.footer.paginated', {
                    first: '{first}',
                    last: '{last}',
                    totalRecords: '{totalRecords}'
                })
            "
            :rows="documentBrowserTableDescriptor.rows"
            filter-display="menu"
            selection-mode="single"
            class="p-datatable-sm kn-table"
            data-key="id"
            :responsive-layout="documentBrowserTableDescriptor.responsiveLayout"
            :breakpoint="documentBrowserTableDescriptor.breakpoint"
            data-test="documents-datatable"
            style="width: 100%"
            :scrollable="true"
            scroll-height="100%"
            @rowClick="$emit('selected', $event.data)"
        >
            <template #empty>
                <Message class="p-m-2" severity="info" :closable="false" :style="documentBrowserTableDescriptor.styles.message" data-test="no-documents-hint">
                    {{ $t('documentBrowser.noDocumentsHint') }}
                </Message>
            </template>
            <Column v-for="col of documentBrowserTableDescriptor.columns" :key="col.field" class="kn-truncated" :style="col.style" :header="$t(col.header)" :field="col.field" :sort-field="col.field" :sortable="true">
                <template #filter="{ filterModel }">
                    <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
                </template>
                <template #body="slotProps">
                    <span v-tooltip.top="slotProps.data[col.field]" class="kn-truncated">{{ getTranslatedValue(slotProps.data[col.field], col.field) }}</span>
                </template>
            </Column>
            <Column v-if="isAdmin" :header="$t('common.status')" field="stateCodeStr" sort-field="stateCodeStr" :sortable="true" :style="documentBrowserTableDescriptor.table.smallmessage">
                <template #filter="{ filterModel }">
                    <InputText v-model="filterModel.value" type="text" class="p-column-filter"></InputText>
                </template>
                <template #body="slotProps" :style="documentBrowserTableDescriptor.table.iconColumn.smallmessage">
                    <span data-test="document-status"> {{ slotProps.data['stateCodeStr'] }}</span>
                </template></Column
            >
            <Column v-if="isAdmin" :header="$t('common.visible')" field="visible" sort-field="visible" :sortable="true" :style="documentBrowserTableDescriptor.table.iconColumn.style">
                <template #body="slotProps">
                    <span v-tooltip="slotProps.data['visible'] ? $t('common.visible') : $t('common.notVisible')" class="fa-stack">
                        <i class="fa fa-eye fa-stack-1x"></i>
                        <i v-if="!slotProps.data['visible']" class="fa fa-ban fa-stack-2x"></i>
                    </span> </template
            ></Column>
            <Column :style="documentBrowserTableDescriptor.table.iconColumn.style">
                <template #body="slotProps">
                    <Button v-tooltip.left="$t('documentBrowser.executeDocument')" icon="fa fa-play-circle" class="p-button-link" @click.stop="executeDocument(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import documentBrowserTableDescriptor from './DocumentBrowserTableDescriptor.json'
import mainStore from '../../../../App.store'
import { getCorrectRolesForExecution } from '../../../../helpers/commons/roleHelper'

export default defineComponent({
    name: 'document-browser-table',
    components: { Column, DataTable, Message },
    props: { propDocuments: { type: Array }, searchMode: { type: Boolean } },
    emits: ['itemSelected', 'selected'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            documentBrowserTableDescriptor,
            documents: [] as any[],
            filters: {
                global: [filterDefault],
                typeCode: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                creationUser: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                stateCodeStr: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as any,
            user: null as any,
            first: 0
        }
    },
    computed: {
        isAdmin(): boolean {
            return this.user?.functionalities.includes('DocumentManagement') || this.user?.isSuperadmin
        }
    },
    watch: {
        propDocuments() {
            this.loadDocuments()
            this.first = 0
        }
    },
    created() {
        this.loadDocuments()
        this.first = 0
        this.user = (this.store.$state as any).user
    },
    methods: {
        loadDocuments() {
            this.documents = this.propDocuments?.map((el: any) => {
                if (el.field === 'status') el.style = documentBrowserTableDescriptor.table.smallmessage
                return { ...el, stateCodeStr: this.getTranslatedStatus(el.stateCodeStr) }
            }) as any[]
        },
        getTranslatedStatus(status: string) {
            return status ? this.$t(documentBrowserTableDescriptor.status[status] ?? '') : ''
        },
        executeDocument(document: any) {
            getCorrectRolesForExecution(document).then(() => {
                this.$emit('itemSelected', { item: document, mode: 'execute' })
            })
        },
        getTranslatedValue(value: string, fieldType: string) {
            if (fieldType !== 'name' && fieldType !== 'label') return value
            return (this as any).$internationalization(value)
        }
    }
})
</script>

<style lang="scss">
#documents-found-hint {
    flex: 0.5;
}
</style>
