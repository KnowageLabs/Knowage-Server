<template>
    <Dialog class="document-details-dialog remove-padding" :content-style="mainDescriptor.style.flex" position="right" :visible="visible" :modal="false" :closable="false" :base-z-index="10" :auto-z-index="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary kn-width-full">
                <template #start>
                    {{ $t('documentExecution.documentDetails.info.datasetDialogTitle') }}
                </template>
            </Toolbar>
        </template>
        <DataTable v-model:filters="filters" v-model:selection="selectedDataset" class="p-datatable-sm kn-table p-m-2" :value="datasets" data-key="id" responsive-layout="stack" :global-filter-fields="infoDescriptor.globalFilterFields" :loading="loading" selection-mode="single" striped-rows>
            <template #header>
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                </span>
            </template>
            <template #empty>{{ $t('common.info.noDataFound') }}</template>
            <Column class="kn-truncated" field="label" :header="$t('common.label')" :sortable="true" />
            <Column class="kn-truncated" field="name" :header="$t('common.name')" :sortable="true" />
            <Column class="kn-truncated" field="description" :header="$t('common.description')" :sortable="true" />
            <Column class="kn-truncated" field="owner" :header="$t('common.owner')" :sortable="true" />
            <Column class="kn-truncated" field="scope" :header="$t('managers.datasetManagement.scope')" :sortable="true" />
        </DataTable>
        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="$emit('closeDialog')" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="$emit('saveSelectedDataset', selectedDataset), $emit('closeDialog')" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import mainDescriptor from '../../DocumentDetailsDescriptor.json'
import infoDescriptor from './DocumentDetailsInformationsDescriptor.json'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'dataset-dialog',
    components: { Dialog, DataTable, Column },
    props: { visible: { type: Boolean, required: false } },
    emits: ['closeDialog', 'saveSelectedDataset'],
    data() {
        return {
            mainDescriptor,
            infoDescriptor,
            loading: false,
            filters: { global: [filterDefault] } as Object,
            datasets: [] as any,
            selectedDataset: {} as any
        }
    },
    created() {
        this.getAllDatasets()
    },
    methods: {
        async getAllDatasets() {
            this.loading = true
            this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/basicinfo/all/`)
                .then((response: AxiosResponse<any>) => (this.datasets = response.data))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
