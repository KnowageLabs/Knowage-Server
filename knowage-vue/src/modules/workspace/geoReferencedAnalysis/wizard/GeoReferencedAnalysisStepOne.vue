<template>
    <DataTable class="p-datatable-sm kn-table georef-step1-table p-m-3" :value="selectedDatasetList" dataKey="id" responsiveLayout="stack" breakpoint="600px">
        <template #empty>
            Add a dataset
        </template>

        <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field">
            <template #body="{data}">
                <span class="kn-truncated" v-tooltip.top="data[col.field]">{{ data[col.field] }}</span>
            </template>
        </Column>
        <Column style="text-align:end; width: 15%" @rowClick="false">
            <template #body="">
                <Button icon="pi pi-trash" class="p-button-link" />
            </template>
        </Column>
        <template #footer>
            <Button v-if="selectedDatasetList.length === 0" class="p-button-link" :label="'add dataset'" @click="showDatasetDialog" />
            <Button v-else class="p-button-link" :label="'change dataset'" @click="showDatasetDialog" />
        </template>
    </DataTable>

    <Dialog class="p-fluid kn-dialog--toolbar--primary" style="width:80%;height80%" v-if="datasetDialogVisible" :visible="datasetDialogVisible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start> Dataset list </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <DataTable
            class="p-datatable-sm kn-table kn-width-full"
            :value="availableDatasets"
            dataKey="id"
            v-model:selection="selectedDataset"
            selectionMode="single"
            :scrollable="true"
            scrollHeight="40vh"
            v-model:filters="filters"
            :globalFilterFields="globalFilterFields"
            :loading="loading"
            responsiveLayout="scroll"
        >
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <template #empty>
                No available datasets
            </template>
            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field">
                <template #body="{data}">
                    <span class="kn-truncated" v-tooltip.top="data[col.field]">{{ data[col.field] }}</span>
                </template>
            </Column>
        </DataTable>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--secondary" @click="datasetDialogVisible = false"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="saveDatasetSelection"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'geo-referenced-analysis',
    components: { DataTable, Column, Dialog },
    emits: ['deleteSelectedDataset', 'datasetSelected'],
    props: { allLayers: Array },
    computed: {},
    data() {
        return {
            globalFilterFields: ['name', 'label', 'dsTypeCd'],
            filters: { global: [filterDefault] } as Object,
            datasetDialogVisible: false,
            loading: false,
            selectedDataset: {} as any,
            selectedDatasetList: [
                {
                    label: 'testID',
                    name: 'testNAME',
                    descr: 'testDESC',
                    dsTypeCd: 'testTYPE'
                }
            ] as any,
            columns: [
                {
                    field: 'label',
                    header: 'common.id'
                },
                {
                    field: 'name',
                    header: 'common.name'
                },
                {
                    field: 'descr',
                    header: 'common.description'
                },
                {
                    field: 'dsTypeCd',
                    header: 'common.type'
                }
            ],
            availableDatasets: [] as any
        }
    },
    created() {},
    methods: {
        showDatasetDialog() {
            if (this.selectedDatasetList.length === 0) {
                this.openDatasetDialog()
            } else {
                this.$confirm.require({
                    header: 'Warning',
                    message: 'UPDATE MESSAGE TRASNLATION, DONT FORGET',
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.openDatasetDialog()
                    }
                })
            }
        },
        openDatasetDialog() {
            this.datasetDialogVisible = true
            this.getAllDatasets()
        },
        deleteSelectedDataset() {
            this.selectedDatasetList = []
            this.$emit('deleteSelectedDataset')
        },
        async getAllDatasets() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/mydatanoparams`).then((response: AxiosResponse<any>) => (this.availableDatasets = response.data.root))
            this.loading = false
        },
        saveDatasetSelection() {
            this.selectedDatasetList[0] = deepcopy(this.selectedDataset)
            this.$emit('datasetSelected')
            this.datasetDialogVisible = false
        }
    }
})
</script>
<style lang="scss">
.georef-step1-table .p-datatable-footer {
    text-align: end;
}
</style>
