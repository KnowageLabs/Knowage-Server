<template>
    <div class="p-d-flex p-flex-row p-jc-center">
        <Message v-if="searchMode" id="documents-found-hint" class="p-m-2" severity="info" :closable="false" :style="documentBrowserTableDescriptor.styles.message">
            {{ documents.length + ' ' + $t('documentBrowser.documentsFound') }}
        </Message>
    </div>
    <div class="table-header p-d-flex" v-if="!searchMode">
        <span class="p-input-icon-left p-mr-3 p-col-12">
            <i class="pi pi-search" />
            <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
        </span>
    </div>
    <div class="kn-overflow-y last-flex-container kn-flex">
        <DataTable
            id="documents-datatable"
            v-model:first="first"
            :value="documents"
            :paginator="documents.length > documentBrowserTableDescriptor.rows"
            :rows="documentBrowserTableDescriptor.rows"
            v-model:filters="filters"
            filterDisplay="menu"
            selectionMode="single"
            class="p-datatable-sm"
            dataKey="id"
            :responsiveLayout="documentBrowserTableDescriptor.responsiveLayout"
            :breakpoint="documentBrowserTableDescriptor.breakpoint"
            @rowClick="$emit('selected', $event.data)"
            data-test="documents-datatable"
            style="width:100%;"
            :scrollable="true"
            scrollHeight="70vh"
        >
            <template #empty>
                <Message class="p-m-2" severity="info" :closable="false" :style="documentBrowserTableDescriptor.styles.message" data-test="no-documents-hint">
                    {{ $t('documentBrowser.noDocumentsHint') }}
                </Message>
            </template>
            <Column class="kn-truncated" :style="col.style" v-for="col of documentBrowserTableDescriptor.columns" :header="$t(col.header)" :field="col.field" :key="col.field" :sortField="col.field" :sortable="true">
                <template #filter="{filterModel}">
                    <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                </template>
            </Column>
            <Column v-if="isSuperAdmin" class="kn-truncated" :header="$t('common.status')" field="stateCodeStr" sortField="stateCodeStr" :sortable="true">
                <template #filter="{filterModel}">
                    <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                </template>
                <template #body="slotProps">
                    <span data-test="document-status"> {{ slotProps.data['stateCodeStr'] }}</span>
                </template></Column
            >
            <Column v-if="isSuperAdmin" :style="documentBrowserTableDescriptor.table.iconColumn.style" :header="$t('common.visible')" field="visible" sortField="visible" :sortable="true">
                <template #body="slotProps">
                    <span class="fa-stack">
                        <i class="fa fa-eye fa-stack-1x"></i>
                        <i v-if="!slotProps.data['visible']" class="fa fa-ban fa-stack-2x"></i>
                    </span> </template
            ></Column>
            <Column :style="documentBrowserTableDescriptor.table.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="fa fa-play-circle" class="p-button-link" @click.stop="executeDocument(slotProps.data)" />
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

export default defineComponent({
    name: 'document-browser-table',
    components: { Column, DataTable, Message },
    props: { propDocuments: { type: Array }, searchMode: { type: Boolean } },
    emits: ['itemSelected', 'selected'],
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
    watch: {
        propDocuments() {
            this.loadDocuments()
            this.first = 0
        }
    },
    computed: {
        isSuperAdmin(): boolean {
            return this.user?.isSuperadmin
        }
    },
    created() {
        this.loadDocuments()
        this.first = 0
        this.user = (this.$store.state as any).user
    },
    methods: {
        loadDocuments() {
            this.documents = this.propDocuments?.map((el: any) => {
                return { ...el, stateCodeStr: this.getTranslatedStatus(el.stateCodeStr) }
            }) as any[]
        },
        getTranslatedStatus(status: string) {
            return status ? this.$t(documentBrowserTableDescriptor.status[status] ?? '') : ''
        },
        executeDocument(document: any) {
            this.$emit('itemSelected', { item: document, mode: 'execute' })
        }
    }
})
</script>

<style lang="scss">
#documents-found-hint {
    flex: 0.5;
}
</style>
