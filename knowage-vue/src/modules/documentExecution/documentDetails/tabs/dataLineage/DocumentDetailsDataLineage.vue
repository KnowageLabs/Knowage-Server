<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.dataLineage.title') }}
                </template>
            </Toolbar>
            <div class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <div id="driver-details-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('managers.datasetManagement.availableTables') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <div class="p-field p-col-12">
                                    <span class="p-float-label">
                                        <Dropdown id="dataSource" class="kn-material-input kn-width-full" v-model="dataSource" :options="metaSourceResource" @change="getTablesBySourceID" optionLabel="name" />
                                        <label for="dataSource" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.dataLineage.selectSource') }} </label>
                                    </span>
                                </div>
                                <div v-if="metaSourceResource.length == 0">
                                    <InlineMessage severity="info" class="kn-width-full">{{ $t('documentExecution.documentDetails.dataLineage.noDatasources') }}</InlineMessage>
                                </div>
                                <div v-if="dataSource && tablesList.length == 0 && metaSourceResource.length != 0">
                                    <InlineMessage severity="info" class="kn-width-full">{{ $t('documentExecution.documentDetails.dataLineage.noTables') }}</InlineMessage>
                                </div>
                                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                                <DataTable
                                    v-if="dataSource && tablesList.length > 0 && !loading"
                                    class="p-datatable-sm kn-table"
                                    :value="tablesList"
                                    v-model:selection="selectedTables"
                                    dataKey="tableId"
                                    responsiveLayout="scroll"
                                    v-model:filters="filters"
                                    :globalFilterFields="globalFilterFields"
                                    @rowSelect="peristTable"
                                    @rowUnselect="deleteTable"
                                >
                                    <template #header>
                                        <div class="table-header p-d-flex p-ai-center">
                                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                                <i class="pi pi-search" />
                                                <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                                            </span>
                                        </div>
                                    </template>
                                    <Column class="lineage-table-header" selectionMode="multiple" :headerStyle="mainDescriptor.style.tableHeader"> </Column>
                                    <Column field="name" :header="$t('managers.datasetManagement.flatTableName')" :sortable="true"></Column>
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
import { iDocument, iMetaSource, iTableSmall } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import InlineMessage from 'primevue/inlinemessage'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'data-lineage',
    components: { Dropdown, DataTable, Column, InlineMessage },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, metaSourceResource: { type: Array as PropType<iMetaSource[]>, required: true }, savedTables: { type: Array as PropType<iTableSmall[]>, required: true } },
    emits: [],
    data() {
        return {
            mainDescriptor,
            dataSource: {} as iMetaSource,
            tablesList: [] as iTableSmall[],
            selectedTables: [] as iTableSmall[],
            loading: false,
            filters: { global: [filterDefault] } as Object,
            globalFilterFields: ['name']
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {},
    methods: {
        async getTablesBySourceID() {
            this.loading = true
            this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/${this.dataSource.sourceId}/metatables`)
                .then((response: AxiosResponse<any>) => {
                    this.tablesList = response.data as iTableSmall[]
                    this.setCheckedTables()
                })
                .finally(() => (this.loading = false))
        },
        setCheckedTables() {
            for (var i = 0; i < this.tablesList.length; i++) {
                for (var j = 0; j < this.savedTables.length; j++) {
                    if (this.tablesList[i].tableId == this.savedTables[j].tableId) {
                        this.selectedTables.push(this.tablesList[i])
                    }
                }
            }
        },
        peristTable(event) {
            this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaDocumetRelationResource/${this.selectedDocument.id}`, event.data, {
                    headers: { 'X-Disable-Errors': 'true' }
                })
                .then(() => this.store.setInfo({ title: this.$t('common.save'), msg: this.$t('documentExecution.documentDetails.dataLineage.persistOk') }))
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.dataLineage.persistError') }))
        },
        deleteTable(event) {
            this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaDocumetRelationResource/${this.selectedDocument.id}/${event.data.tableId}`, {
                    headers: { 'X-Disable-Errors': 'true' }
                })
                .then(() => this.store.setInfo({ title: this.$t('common.save'), msg: this.$t('documentExecution.documentDetails.dataLineage.deleteOk') }))
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.dataLineage.deleteError') }))
        }
    }
})
</script>
<style lang="scss">
.lineage-table-header .p-column-header-content {
    display: none;
}
</style>
