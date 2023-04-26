<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.subreports.title') }}
                </template>
            </Toolbar>
            <div class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <div id="driver-details-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.subreports.tableName') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <InlineMessage severity="info">{{ $t('documentExecution.documentDetails.subreports.info') }}</InlineMessage>

                                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                                <DataTable
                                    v-if="!loading"
                                    v-model:selection="selectedSubreports"
                                    v-model:filters="filters"
                                    class="p-datatable-sm kn-table"
                                    :value="allDocumentDetailsProp"
                                    data-key="id"
                                    responsive-layout="scroll"
                                    :global-filter-fields="globalFilterFields"
                                    :loading="loading"
                                    @rowSelect="peristTable"
                                    @rowUnselect="deleteTable"
                                >
                                    <template #header>
                                        <div class="table-header p-d-flex p-ai-center">
                                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                                <i class="pi pi-search" />
                                                <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                                            </span>
                                        </div>
                                    </template>
                                    <Column class="lineage-table-header" selection-mode="multiple" :header-style="mainDescriptor.style.tableHeader"> </Column>
                                    <Column field="label" :header="$t('common.label')" :sortable="true"></Column>
                                    <Column field="name" :header="$t('common.name')" :sortable="true"></Column>
                                    <Column field="description" :header="$t('common.description')" :sortable="true"></Column>
                                </DataTable>
                            </template>
                        </Card>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InlineMessage from 'primevue/inlinemessage'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'data-lineage',
    components: {
        DataTable,
        Column,
        InlineMessage
    },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, allDocumentDetailsProp: { type: Array as any, required: true } },
    emits: [],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            mainDescriptor,
            savedSubreports: [] as any,
            selectedSubreports: [] as any[],
            loading: false,
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: ['name']
        }
    },
    watch: {
        async allDocumentDetailsProp() {
            await this.getSelectedSubreports()
        }
    },
    async created() {
        await this.getSelectedSubreports()
    },

    methods: {
        async getSelectedSubreports() {
            this.loading = true
            if (this.selectedDocument?.id) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument?.id}/subreports`).then((response: AxiosResponse<any>) => (this.savedSubreports = response.data))
                this.setCheckedTables()
            }
            this.loading = false
        },
        setCheckedTables() {
            for (let i = 0; i < this.allDocumentDetailsProp.length; i++) {
                for (let j = 0; j < this.savedSubreports.length; j++) {
                    if (this.allDocumentDetailsProp[i].id == this.savedSubreports[j].sub_rpt_id) {
                        this.selectedSubreports.push(this.allDocumentDetailsProp[i])
                    }
                }
            }
        },
        peristTable(event) {
            const postData = event.data
            delete postData.dataSetLabel
            delete postData.creationDate
            postData.refreshSeconds = parseInt(postData.refreshSeconds)
            this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/subreports`, postData, {
                    headers: { 'X-Disable-Errors': 'true' }
                })
                .then(() => this.store.setInfo({ title: this.$t('common.save'), msg: this.$t('documentExecution.documentDetails.subreports.persistOk') }))
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.subreports.persistError') }))
        },
        deleteTable(event) {
            this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/subreports/${event.data.id}`, {
                    headers: { 'X-Disable-Errors': 'true' }
                })
                .then(() => this.store.setInfo({ title: this.$t('common.save'), msg: this.$t('documentExecution.documentDetails.subreports.deleteOk') }))
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.subreports.deleteError') }))
        }
    }
})
</script>
<style lang="scss">
.lineage-table-header .p-column-header-content {
    display: none;
}
</style>
