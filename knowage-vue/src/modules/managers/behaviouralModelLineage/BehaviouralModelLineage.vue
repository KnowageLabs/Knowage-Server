<template>
    <div class="kn-page">
        <KnOverlaySpinnerPanel :visibility="loading" />
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.bml.title') }}
            </template>
            <template #end>
                <Button icon="fa-solid fa-arrows-rotate" class="p-button-text p-button-rounded p-button-plain" @click="loadAllData" />
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />

        <div id="table-container" class="p-d-flex p-flex-row kn-height-full">
            <BMLTable :tableData="allLovs" :headerTitle="$t('managers.bml.lovsTitle')" @rowSelected="logEvent" @rowUnselected="logEvent" />
            <BMLTable :tableData="allDrivers" :headerTitle="$t('managers.bml.drivers')" @rowSelected="logEvent" @rowUnselected="logEvent" />
            <BMLTable :tableData="allDocuments" :headerTitle="$t('managers.datasetManagement.documents')" @rowSelected="logEvent" @rowUnselected="logEvent" />
            <!-- <DataTable
                class="p-datatable-sm kn-table bml-table"
                :value="allLovs"
                selectionMode="single"
                v-model:selection="selectedLov"
                dataKey="id"
                responsiveLayout="stack"
                breakpoint="360px"
                v-model:filters="filters"
                :globalFilterFields="filterFields"
                stripedRows
                @rowSelect="logEvent"
                @rowUnselect="logEvent"
            >
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('managers.bml.lovsTitle') }}
                        </template>
                    </Toolbar>
                    <span id="search-container" class="p-input-icon-left p-m-2">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="filterInput" />
                    </span>
                </template>
                <Column field="label" :header="$t('common.label')" :sortable="true" :headerStyle="descriptor.style.uppercase" />
                <Column field="name" :header="$t('common.name')" :sortable="true" :headerStyle="descriptor.style.uppercase" />
            </DataTable> -->
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iLov, iAnalyticalDriver, iDocument } from './BehaviouralModelLineage'
import descriptor from './BehaviouralModelLineageDescriptor.json'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import ProgressBar from 'primevue/progressbar'
// import DataTable from 'primevue/datatable'
// import Column from 'primevue/column'
import BMLTable from './BehaviouralModelLineageTable.vue'

export default defineComponent({
    name: 'behavioural-model-lineage',
    components: {
        KnOverlaySpinnerPanel,
        ProgressBar,
        // DataTable,
        // Column
        BMLTable
    },
    data() {
        return {
            descriptor,
            loading: false,
            filterFields: ['name', 'label'],
            filters: { global: [filterDefault] } as Object,
            allLovs: [] as iLov[],
            selectedLov: {} as iLov,
            allDrivers: [] as iAnalyticalDriver[],
            allDocuments: [] as iDocument[]
        }
    },
    created() {
        this.loadAllData()
    },
    methods: {
        async loadAllData() {
            this.loading = true
            await Promise.all([this.loadAllLovs(), this.loadAllDrivers(), this.loadAllDocuments()]).then(() => (this.loading = false))
        },
        async loadAllLovs() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/get/all/').then((response: AxiosResponse<any>) => {
                this.allLovs = response.data
            })
        },
        async loadAllDrivers() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/analyticalDrivers').then((response: AxiosResponse<any>) => {
                this.allDrivers = response.data
            })
        },
        async loadAllDocuments() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents').then((response: AxiosResponse<any>) => {
                this.allDocuments = response.data
            })
        },
        logEvent(data) {
            console.log('logme', data)
        }
    }
})
</script>
<style lang="scss">
.bml-table .p-datatable-header {
    padding: 0 !important;
}
</style>
