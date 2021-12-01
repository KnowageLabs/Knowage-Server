<template>
    <Dialog class="document-details-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="mainDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('documentExecution.documentDetails.title') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('closeDetails')" />
                </template>
            </Toolbar>
        </template>
        <div class="document-details-tab-container p-d-flex p-flex-column" :style="mainDescriptor.style.flexOne">
            <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
            <TabView class="document-details-tabview" :style="mainDescriptor.style.flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                    </template>
                    <InformationsTab
                        v-if="!loading"
                        :selectedDocument="selectedDocument"
                        :documentTypes="types"
                        :documentEngines="engines"
                        :availableDatasources="dataSources"
                        :availableStates="states"
                        :selectedDataset="selectedDataset"
                        :availableTemplates="templates"
                        :availableAttributes="attributes"
                    />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.drivers.title') }}</span>
                    </template>
                    <DriversTab v-if="!loading" :selectedDocument="selectedDocument" :availableDrivers="drivers" :availableAnalyticalDrivers="analyticalDrivers" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.outputParams.title') }}</span>
                    </template>
                    <OutputParamsTab v-if="!loading" :selectedDocument="selectedDocument" :typeList="parTypes" :dateFormats="dateFormats" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.dataLineage.title') }}</span>
                    </template>

                    <DataLineageTab v-if="!loading" :selectedDocument="selectedDocument" :metaSourceResource="metaSourceResource" :savedTables="savedTables" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.history.title') }}</span>
                    </template>
                    <HistoryTab v-if="!loading" :selectedDocument="selectedDocument" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id && this.selectedDocument?.typeCode == 'REPORT' && this.selectedDocument?.engine == 'knowagejasperreporte'">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.subreports.title') }}</span>
                    </template>
                </TabPanel>
            </TabView>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import mainDescriptor from './DocumentDetailsDescriptor.json'
import InformationsTab from './tabs/informations/DocumentDetailsInformations.vue'
import DriversTab from './tabs/drivers/DocumentDetailsDrivers.vue'
import OutputParamsTab from './tabs/outputParams/DocumentDetailsOutputParameters.vue'
import DataLineageTab from './tabs/dataLineage/DocumentDetailsDataLineage.vue'
import HistoryTab from './tabs/history/DocumentDetailsHistory.vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import { iDocument, iDataSource, iAnalyticalDriver, iDriver, iEngine, iTemplate, iAttribute } from '@/modules/documentExecution/documentDetails/DocumentDetails'

export default defineComponent({
    name: 'document-details',
    components: { InformationsTab, DriversTab, OutputParamsTab, DataLineageTab, HistoryTab, TabView, TabPanel, Dialog },
    props: { selectedDocument: { type: Object, required: true }, visible: { type: Boolean, required: false } },
    emits: ['closeDetails'],
    data() {
        return {
            mainDescriptor,
            loading: false,
            document: {} as iDocument,
            selectedDataset: {} as any,
            dataSources: [] as iDataSource[],
            analyticalDrivers: [] as iAnalyticalDriver[],
            drivers: [] as iDriver[],
            engines: [] as iEngine[],
            templates: [] as iTemplate[],
            attributes: [] as iAttribute[],
            parTypes: [] as any[],
            dateFormats: [] as any[],
            metaSourceResource: [] as any,
            savedTables: [] as any,
            states: mainDescriptor.states,
            types: mainDescriptor.types
        }
    },
    watch: {},
    created() {
        this.getAllPersistentData()
    },
    //document: http://localhost:8080/knowage/restful-services/2.0/documents/${id}
    //datasources: http://localhost:8080/knowage/restful-services/2.0/datasources
    //analyticalDrivers: http://localhost:8080/knowage/restful-services/2.0/analyticalDrivers
    //drivers: http://localhost:8080/knowage/restful-services/2.0/documentdetails/${id}/drivers
    //engines: http://localhost:8080/knowage/restful-services/2.0/engines
    //template: `2.0/documentdetails/${this.selectedDocument?.id}/templates
    //types: ??

    //folderId: ??
    //resourcePath: ??
    //states: ??
    methods: {
        async getAllPersistentData() {
            await this.getAnalyticalDrivers()
            await this.getDatasources()
            await this.getDocumentDrivers()
            await this.getTemplates()
            await this.getEngines()
            await this.getAttributes()
            await this.getParTypes()
            await this.getDateFormats()
            await this.getDataSources()
            await this.getTablesByDocumentID()
            await this.getDataset()
        },
        async getAnalyticalDrivers() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers`).then((response: AxiosResponse<any>) => (this.analyticalDrivers = response.data))
        },
        async getDatasources() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources`).then((response: AxiosResponse<any>) => (this.dataSources = response.data))
        },
        async getDocumentDrivers() {
            if (this.selectedDocument?.id) {
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument?.id}/drivers`).then((response: AxiosResponse<any>) => (this.drivers = response.data))
            }
        },
        async getTemplates() {
            if (this.selectedDocument?.id) {
                this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument?.id}/templates`).then((response: AxiosResponse<any>) => (this.templates = response.data))
            }
        },
        async getEngines() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/engines`).then((response: AxiosResponse<any>) => (this.engines = response.data))
        },
        async getAttributes() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/attributes`).then((response: AxiosResponse<any>) => (this.attributes = response.data))
        },
        async getParTypes() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/PAR_TYPE`).then((response: AxiosResponse<any>) => (this.parTypes = response.data))
        },
        async getDateFormats() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/DATE_FORMAT`).then((response: AxiosResponse<any>) => (this.dateFormats = response.data))
        },
        async getDataSources() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/`).then((response: AxiosResponse<any>) => ((this.metaSourceResource = response.data), (this.metaSourceResource = this.mainDescriptor.metaSourceResource)))
        },
        async getTablesByDocumentID() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaDocumetRelationResource/document/${this.selectedDocument.id}`).then((response: AxiosResponse<any>) => ((this.savedTables = response.data), (this.savedTables = this.mainDescriptor.savedTables)))
        },
        async getDataset() {
            if (this.selectedDocument?.dataSetId) {
                this.loading = true
                this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDocument?.dataSetId}`, { headers: { 'X-Disable-Errors': 'true' } })
                    .then((response: AxiosResponse<any>) => {
                        this.selectedDataset = response.data[0]
                    })
                    //ERROR SE NE VIDI ZBOG TOAST Z-INDEXA OVO MORA DA SE SREDI
                    .catch((error) => {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message })
                    })
                    .finally(() => (this.loading = false))
            }
        }
    }
})
</script>

<style lang="scss">
.right-border {
    border-right: 1px solid #ccc;
}
.document-details-tabview .p-tabview-panels {
    padding: 0 !important;
}
.document-details-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.document-details-tab-container .p-tabview .p-tabview-panel,
.document-details-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
