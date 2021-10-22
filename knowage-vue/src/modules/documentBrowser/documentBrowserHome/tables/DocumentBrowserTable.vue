<template>
    <DataTable
        id="documents-datatable"
        :value="documents"
        :paginator="true"
        :rows="documentBrowserTableDescriptor.rows"
        v-model:filters="filters"
        filterDisplay="menu"
        class="p-datatable-sm kn-table"
        dataKey="id"
        :responsiveLayout="documentBrowserTableDescriptor.responsiveLayout"
        :breakpoint="documentBrowserTableDescriptor.breakpoint"
        @rowClick="$emit('selected', $event.data)"
    >
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="documentBrowserTableDescriptor.styles.message">
                {{ $t('documentBrowser.noDocumentsHint') }}
            </Message>
        </template>
        <Column class="kn-truncated" :style="col.style" v-for="col of documentBrowserTableDescriptor.columns" :header="$t(col.header)" :field="col.field" :key="col.field" :sortField="col.field" :sortable="true">
            <template #filter="{filterModel}">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
        </Column>
        <Column v-if="isAdmin" class="kn-truncated" :header="$t('common.status')" field="stateCodeStr" sortField="stateCodeStr" :sortable="true">
            <template #filter="{filterModel}">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
            <template #body="slotProps">
                <span> {{ slotProps.data['stateCodeStr'] }}</span>
            </template></Column
        >
        <Column v-if="isAdmin" :style="documentBrowserTableDescriptor.table.iconColumn.style" :header="$t('common.visible')" field="visible" sortField="visible" :sortable="true">
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
    props: { propDocuments: { type: Array } },
    emits: ['executeDocumentClick'],
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
                }
            } as any
        }
    },
    watch: {
        propDocuments() {
            this.loadDocuments()
        }
    },
    computed: {
        isAdmin(): boolean {
            // TODO Add condition
            return true
        }
    },
    created() {
        this.loadDocuments()
    },
    methods: {
        loadDocuments() {
            this.documents = this.propDocuments?.map((el: any) => {
                return { ...el, stateCodeStr: this.getTranslatedStatus(el.stateCodeStr) }
            }) as any[]

            this.updateFilters()
            // console.log('DOCUMENTS LOADED IN TABLE: ', this.documents)
        },
        updateFilters() {
            if (this.isAdmin) {
                this.filters.stateCodeStr = {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                } as any
            } else {
                delete this.filters.stateCodeStr
            }
            // console.log('FILTERS: ', this.filters)
        },
        getTranslatedStatus(status: string) {
            return status ? this.$t(documentBrowserTableDescriptor.status[status] ?? '') : ''
        },
        executeDocument(document: any) {
            this.$emit('executeDocumentClick', document)
        }
    }
})
</script>

<style lang="scss" scoped>
#documents-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
