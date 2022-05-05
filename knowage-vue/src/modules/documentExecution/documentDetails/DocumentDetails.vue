<template>
    <Dialog class="document-details-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="mainDescriptor.style.flex" :visible="true" :modal="false" :closable="false" :draggable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('documentExecution.documentDetails.title') }}
                </template>
                <template #end>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="saveDocument" :disabled="invalidDrivers > 0 || invalidOutputParams > 0 || v$.$invalid" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="closeDocument" />
                </template>
            </Toolbar>
        </template>
        <ProgressSpinner v-if="loading" class="doc-details-spinner" :style="mainDescriptor.style.spinnerStyle" />

        <div class="document-details-tab-container p-d-flex p-flex-column kn-flex">
            <TabView class="document-details-tabview p-d-flex p-flex-column kn-flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.info.infoTitle') }}</span>
                    </template>
                    <InformationsTab
                        v-if="!loading"
                        :selectedDocument="selectedDocument"
                        :availableFolders="availableFolders"
                        :documentTypes="types"
                        :documentEngines="engines"
                        :availableDatasources="dataSources"
                        :availableStates="states"
                        :selectedDataset="selectedDataset"
                        :availableTemplates="templates"
                        :availableAttributes="attributes"
                        @setTemplateForUpload="setTemplateForUpload"
                        @setImageForUpload="setImageForUpload"
                        @deleteImage="deleteImage"
                    />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span v-bind:class="{ 'details-warning-color': invalidDrivers }">{{ $t('documentExecution.documentDetails.drivers.title') }}</span>
                        <Badge :value="invalidDrivers" class="p-ml-2" severity="danger" v-if="invalidDrivers > 0"></Badge>
                    </template>
                    <DriversTab :selectedDocument="selectedDocument" :availableDrivers="drivers" :availableAnalyticalDrivers="analyticalDrivers" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span v-bind:class="{ 'details-warning-color': invalidOutputParams }">{{ $t('documentExecution.documentDetails.outputParams.title') }}</span>
                        <Badge :value="invalidOutputParams" class="p-ml-2" severity="danger" v-if="invalidOutputParams > 0"></Badge>
                    </template>
                    <OutputParamsTab :selectedDocument="selectedDocument" :typeList="parTypes" :dateFormats="dateFormats" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.dataLineage.title') }}</span>
                    </template>

                    <DataLineageTab :selectedDocument="selectedDocument" :metaSourceResource="metaSourceResource" :savedTables="savedTables" />
                </TabPanel>
                <TabPanel v-if="this.selectedDocument?.id">
                    <template #header>
                        <span>{{ $t('documentExecution.documentDetails.history.title') }}</span>
                    </template>
                    <HistoryTab :selectedDocument="selectedDocument" />
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
import useValidate from '@vuelidate/core'
import mainDescriptor from './DocumentDetailsDescriptor.json'
import InformationsTab from './tabs/informations/DocumentDetailsInformations.vue'
import DriversTab from './tabs/drivers/DocumentDetailsDrivers.vue'
import OutputParamsTab from './tabs/outputParams/DocumentDetailsOutputParameters.vue'
import DataLineageTab from './tabs/dataLineage/DocumentDetailsDataLineage.vue'
import HistoryTab from './tabs/history/DocumentDetailsHistory.vue'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import Badge from 'primevue/badge'
import TabPanel from 'primevue/tabpanel'
import ProgressSpinner from 'primevue/progressspinner'
import { iDataSource, iAnalyticalDriver, iDriver, iEngine, iTemplate, iAttribute, iParType, iDateFormat, iFolder, iTableSmall, iOutputParam, iDocumentType } from '@/modules/documentExecution/documentDetails/DocumentDetails'

export default defineComponent({
    name: 'document-details',
    components: { InformationsTab, DriversTab, OutputParamsTab, DataLineageTab, HistoryTab, TabView, TabPanel, Dialog, Badge, ProgressSpinner },
    props: {},
    emits: ['closeDetails'],
    data() {
        return {
            v$: useValidate() as any,
            mainDescriptor,
            loading: false,
            docId: null as any,
            folderId: null as any,
            templateToUpload: null as any,
            imageToUpload: null as any,
            selectedDataset: {} as any,
            selectedDocument: {} as any,
            dataSources: [] as iDataSource[],
            analyticalDrivers: [] as iAnalyticalDriver[],
            drivers: [] as iDriver[],
            engines: [] as iEngine[],
            templates: [] as iTemplate[],
            attributes: [] as iAttribute[],
            parTypes: [] as iParType[],
            dateFormats: [] as iDateFormat[],
            metaSourceResource: [] as any,
            savedTables: [] as iTableSmall[],
            availableFolders: [] as iFolder[],
            states: mainDescriptor.states,
            types: [] as iDocumentType[]
        }
    },
    computed: {
        invalidOutputParams(): number {
            if (this.selectedDocument && this.selectedDocument.outputParameters) {
                return this.selectedDocument.outputParameters.filter((parameter: any) => parameter.numberOfErrors > 0).length
            }
            return 0
        },
        invalidDrivers(): number {
            if (this.selectedDocument && this.selectedDocument.drivers) {
                return this.selectedDocument.drivers.filter((parameter: any) => parameter.numberOfErrors > 0).length
            }
            return 0
        }
    },
    async created() {
        this.isForEdit()
        await this.loadPage(this.docId)
    },
    methods: {
        isForEdit() {
            this.$route.params.docId ? (this.docId = this.$route.params.docId) : (this.folderId = this.$route.params.folderId)
        },
        //#region ===================== Get Persistent Data ====================================================
        async loadPage(id) {
            this.loading = true
            await Promise.all([
                await this.getSelectedDocumentById(id),
                this.getFunctionalities(),
                this.getAnalyticalDrivers(),
                this.getDatasources(),
                this.getDocumentDrivers(),
                this.getTemplates(),
                this.getTypes(),
                this.getEngines(),
                this.getAttributes(),
                this.getParTypes(),
                this.getDateFormats(),
                this.getSavedTablesByDocumentID(),
                this.getDataset(),
                this.getDataSources()
            ])
            this.loading = false
        },
        async getSelectedDocumentById(id) {
            if (id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${id}`).then((response: AxiosResponse<any>) => (this.selectedDocument = response.data))
            } else {
                this.selectedDocument = { ...this.mainDescriptor.newDocument }
                this.selectedDocument.functionalities = []
            }
        },
        async getFunctionalities() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/folders?includeDocs=false`).then((response: AxiosResponse<any>) => {
                this.availableFolders = response.data
                if (this.$route.params.folderId) {
                    let sourceFolder = this.availableFolders.find((folder) => folder.id == parseInt(this.folderId)) as iFolder
                    this.selectedDocument.functionalities.push(sourceFolder.path)
                }
            })
        },
        async getAnalyticalDrivers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers`).then((response: AxiosResponse<any>) => (this.analyticalDrivers = response.data))
        },
        async getDatasources() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasources`).then((response: AxiosResponse<any>) => (this.dataSources = response.data))
        },
        async getDocumentDrivers() {
            if (this.selectedDocument?.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument?.id}/drivers`).then((response: AxiosResponse<any>) => (this.drivers = response.data))
            }
        },
        async getTemplates() {
            if (this.selectedDocument?.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument?.id}/templates`).then((response: AxiosResponse<any>) => (this.templates = response.data))
            }
        },
        async getTypes() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/document-detail/types`).then((response: AxiosResponse<any>) => (this.types = response.data))
        },
        async getEngines() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/document-detail/engines`).then((response: AxiosResponse<any>) => (this.engines = response.data))
        },
        async getAttributes() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/attributes`).then((response: AxiosResponse<any>) => (this.attributes = response.data))
        },
        async getParTypes() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/PAR_TYPE`).then((response: AxiosResponse<any>) => (this.parTypes = response.data))
        },
        async getDateFormats() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/DATE_FORMAT`).then((response: AxiosResponse<any>) => (this.dateFormats = response.data))
        },
        async getDataSources() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/`).then((response: AxiosResponse<any>) => (this.metaSourceResource = response.data))
        },
        async getSavedTablesByDocumentID() {
            if (this.selectedDocument.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaDocumetRelationResource/document/${this.selectedDocument.id}`).then((response: AxiosResponse<any>) => (this.savedTables = response.data))
            }
        },
        async getDataset() {
            if (this.selectedDocument?.dataSetId) {
                await this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDocument?.dataSetId}`, { headers: { 'X-Disable-Errors': 'true' } })
                    .then((response: AxiosResponse<any>) => {
                        this.selectedDataset = response.data[0]
                    })
                    .catch((error) => {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message })
                    })
            }
        },
        //#endregion ===============================================================================================
        setTemplateForUpload(event) {
            this.templateToUpload = event
        },
        async uploadTemplate(uploadedFile, responseId) {
            if (this.templateToUpload) {
                var formData = new FormData()
                formData.append('file', uploadedFile)
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${responseId}/templates`, formData, {
                        headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' }
                    })
                    .then(() => (this.templateToUpload = null))
                    .catch(() => {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.history.uploadError') })
                    })
            }
        },
        deleteImage() {
            this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/image`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                .then(() => this.loadPage(this.docId))
                .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.info.imageError') }))
        },
        setImageForUpload(event) {
            this.imageToUpload = event
        },
        async uploadImage(uploadedFile, responseId) {
            if (this.imageToUpload) {
                var formData = new FormData()
                formData.append('file', uploadedFile)
                formData.append('fileName', uploadedFile.name)
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${responseId}/image`, formData, {
                        headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' }
                    })
                    .then(() => (this.imageToUpload = null))
                    .catch(() => {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.info.imageUploadError') })
                    })
            }
        },
        async saveOutputParams() {
            if (this.selectedDocument.outputParameters) {
                this.selectedDocument.outputParameters.forEach((parameter: iOutputParam) => {
                    if (!parameter.id) {
                        delete parameter.numberOfErrors
                        delete parameter.tempId
                        delete parameter.isChanged
                        this.$http
                            .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters`, parameter, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                            .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.outputParams.persistError') }))
                    } else if (parameter.isChanged) {
                        delete parameter.numberOfErrors
                        delete parameter.isChanged
                        this.$http
                            .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters/${parameter.id}`, parameter, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                            .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.outputParams.persistError') }))
                    }
                })
            }
        },
        async saveDrivers() {
            if (this.selectedDocument.drivers) {
                this.selectedDocument.drivers.forEach((driver: iDriver) => {
                    driver.modifiable = 0
                    if (!driver.id) {
                        delete driver.numberOfErrors
                        delete driver.isChanged
                        this.$http
                            .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/drivers`, driver, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                            .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.drivers.persistError') }))
                    } else if (driver.isChanged) {
                        delete driver.numberOfErrors
                        delete driver.isChanged
                        this.$http
                            .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/drivers/${driver.id}`, driver, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                            .catch(() => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.drivers.persistError') }))
                    }
                })
            }
        },
        saveRequest(docToSave) {
            if (!this.selectedDocument.id) {
                return this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails`, docToSave, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
            } else {
                return this.$http.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${docToSave.id}`, docToSave, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
            }
        },
        async saveDocument() {
            this.loading = true
            let docToSave = { ...this.selectedDocument }
            delete docToSave.drivers
            delete docToSave.outputParameters
            delete docToSave.dataSetLabel

            await this.saveRequest(docToSave)
                .then(async (response: AxiosResponse<any>) => {
                    await this.saveOutputParams()
                    await this.saveDrivers()
                    await this.uploadTemplate(this.templateToUpload, response.data.id)
                    await this.uploadImage(this.imageToUpload, response.data.id)
                    this.$store.commit('setInfo', { title: this.$t('common.save'), msg: this.$t('common.toast.updateSuccess') })
                    setTimeout(() => {
                        const path = `/document-details/${response.data.id}`
                        !this.selectedDocument.id ? this.$router.push(path) : ''
                        this.loadPage(response.data.id)
                    }, 200)
                })
                .catch((error) => this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message }))
        },
        closeDocument() {
            const path = `/document-browser`
            this.$router.push(path)
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
    width: calc(100vw - var(--kn-mainmenu-width));
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

.details-warning-color {
    color: red;
}

.doc-details-spinner .p-progress-spinner-svg {
    width: 125px;
}
</style>
