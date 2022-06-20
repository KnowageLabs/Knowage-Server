<template>
    <Dialog
        :header="$t('managers.crossNavigationManagement.selectDocument')"
        :breakpoints="dialogDescriptor.dialog.breakpoints"
        :style="dialogDescriptor.dialog.style"
        :contentStyle="dialogDescriptor.dialog.contentStyle"
        :visible="dialogVisible"
        :modal="true"
        :closable="false"
        class="p-fluid kn-dialog--toolbar--primary"
    >
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.crossNavigationManagement.selectDocument') }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>
        <DataTable
            :paginator="true"
            :rows="15"
            :rowsPerPageOptions="[10, 15, 20]"
            v-model:selection="selected"
            :value="documents.item"
            class="p-datatable-sm kn-table"
            dataKey="DOCUMENT_ID"
            responsiveLayout="stack"
            selectionMode="single"
            :loading="loading"
            @row-select="hadleSelect"
            v-model:filters="filters"
            filterDisplay="menu"
            :scrollable="true"
            :scrollHeight="dialogDescriptor.dialog.scrollHeight"
            :globalFilterFields="dialogDescriptor.globalFilterFields"
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

            <Column v-for="col of dialogDescriptor.columnsDoc" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" class="kn-truncated">
                <template #body="slotProps">
                    <span>{{ slotProps.data[slotProps.column.props.field] }}</span>
                </template>
            </Column>
        </DataTable>
    </Dialog>
</template>
<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import Dialog from 'primevue/dialog'
    import { filterDefault } from '@/helpers/commons/filterHelper'
    import { FilterOperator } from 'primevue/api'
    import dialogDescriptor from './CrossNavigationManagementDialogDescriptor.json'
    export default defineComponent({
        name: 'doc-dialog',
        components: {
            DataTable,
            Column,
            Dialog
        },
        props: {
            dialogVisible: {
                type: Boolean,
                default: false
            },
            selectedDoc: {
                type: Object,
                required: false
            }
        },
        emits: ['close', 'apply'],
        data() {
            return {
                dialogDescriptor,
                loading: false,
                selected: {} as any,
                documents: [] as any,
                filters: {
                    global: [filterDefault],
                    DOCUMENT_LABEL: {
                        operator: FilterOperator.AND,
                        constraints: [filterDefault]
                    },
                    DOCUMENT_NAME: {
                        operator: FilterOperator.AND,
                        constraints: [filterDefault]
                    }
                } as Object
            }
        },
        watch: {
            async selectedDoc() {
                await this.loadAllDoc()
                this.selected = this.documents.item.find((doc) => doc.DOCUMENT_ID === this.selectedDoc)
            }
        },
        methods: {
            closeDialog() {
                this.$emit('close')
            },
            async loadAllDoc() {
                this.loading = true
                await this.$http
                    .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument')
                    .then((response: AxiosResponse<any>) => (this.documents = response.data))
                    .finally(() => (this.loading = false))
            },
            hadleSelect() {
                this.$emit('apply', this.selected)
                this.selected = null
            }
        }
    })
</script>
