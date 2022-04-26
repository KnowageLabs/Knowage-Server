<template>
    <div class="kn-page p-d-flex p-flex-row custom-kn-page-width">
        <div id="sideMenu" class="kn-list--column" :style="workspaceDescriptor.style.menuWidth">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('workspace.menuLabels.menuTitle') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <Listbox v-if="displayMenu && storeFunctionalitiesExist" :options="menuItems" data-test="menu-list" class="kn-list">
                <template #option="slotProps">
                    <div v-if="slotProps.option.value !== 'repository'" class="kn-list-item" @click="setActiveView(`/workspace/${slotProps.option.value}`)">
                        <i :class="slotProps.option.icon"></i>
                        <div class="kn-list-item-text">
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </div>
                    <div v-else class="menu-accordion" v-show="showRepository">
                        <Accordion>
                            <AccordionTab>
                                <template #header>
                                    <div class="p-d-flex p-flex-row" @click="accordionIcon = !accordionIcon" data-test="document-accordion">
                                        <i class="fas fa-folder"></i>
                                        <span class="p-ml-2">{{ $t('workspace.menuLabels.myRepository') }}</span>
                                        <i v-if="accordionIcon" class="pi pi-chevron-right menu-accordion-icon"></i>
                                        <i v-if="!accordionIcon" class="pi pi-chevron-down menu-accordion-icon"></i>
                                    </div>
                                </template>
                                <WorkspaceDocumentTree :propFolders="allFolders" mode="select" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder" @delete="deleteFolder" @createFolder="showCreateFolderDialog" data-test="document-tree"></WorkspaceDocumentTree>
                            </AccordionTab>
                        </Accordion>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="p-d-flex p-flex-column" :style="workspaceDescriptor.style.maxWidth">
            <Button id="showSidenavIcon" v-if="$router.currentRoute._rawValue.fullPath === '/workspace'" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="sidebarVisible = true" />
            <router-view
                class="kn-router-view"
                :selectedFolder="selectedFolder"
                :allFolders="allFolders"
                :toggleCardDisplay="toggleCardDisplay"
                :breadcrumbs="breadcrumbs"
                @toggleDisplayView="toggleDisplayView"
                @showMenu="sidebarVisible = true"
                @reloadRepositoryMenu="getAllFolders"
                @breadcrumbClicked="setSelectedBreadcrumb($event)"
                @execute="executeDocument($event)"
                @showQbeDialog="prepareDataForQbe"
            />
        </div>

        <Dialog class="metaweb-dialog remove-padding p-fluid" :contentStyle="workspaceDescriptor.style.flex" :visible="qbeDialogVisible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                    <template #start>
                        {{ qbeDataset.name }}
                    </template>
                    <template #end>
                        <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="parameterSidebarVisible = !parameterSidebarVisible" />
                        <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="saveQbeDataset" />
                        <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="closeQbeIframe" />
                    </template>
                </Toolbar>
            </template>
            <div id="qbe-iframe-container" class="p-d-flex p-flex-row kn-flex">
                <iframe v-if="qbeIframeVisible" id="qbeIframe" ref="qbeIframe" class="kn-width-full kn-height-full" :src="qbeUrl"></iframe>
                <KnParameterSidebar style="position:inherit;margin-left:auto" v-if="parameterSidebarVisible" :filtersData="filtersData" :propDocument="qbeDataset" :userRole="userRole" :propQBEParameters="qbeParameters" :propMode="'qbeView'" @execute="initiateQbeIframe"></KnParameterSidebar>
            </div>
        </Dialog>
    </div>

    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('workspace.menuLabels.menuTitle') }}
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Listbox :options="menuItems">
            <template #option="slotProps">
                <div v-if="slotProps.option.value !== 'repository'" class="kn-list-item p-d-flex p-flex-row" @click="setActiveView(`/workspace/${slotProps.option.value}`)">
                    <i :class="slotProps.option.icon"></i>
                    <div class="kn-list-item-text p-ml-2">
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </div>
                <div v-else v-show="showRepository" class="menu-accordion-sidebar">
                    <Accordion>
                        <AccordionTab>
                            <template #header>
                                <div class="p-d-flex p-flex-row" @click="accordionIcon = !accordionIcon" data-test="document-accordion">
                                    <i class="fas fa-folder"></i>
                                    <span class="p-ml-2">{{ $t('workspace.menuLabels.myRepository') }}</span>
                                </div>
                            </template>
                            <WorkspaceDocumentTree :propFolders="allFolders" mode="select" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder" @delete="deleteFolder" @createFolder="showCreateFolderDialog" data-test="document-tree"></WorkspaceDocumentTree>
                        </AccordionTab>
                    </Accordion>
                </div>
            </template>
        </Listbox>
    </Sidebar>

    <WorkspaceNewFolderDialog :visible="displayCreateFolderDialog" @close="displayCreateFolderDialog = false" @create="createNewFolder"></WorkspaceNewFolderDialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'
import { AxiosResponse } from 'axios'
import Sidebar from 'primevue/sidebar'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Listbox from 'primevue/listbox'
import WorkspaceDocumentTree from './genericComponents/WorkspaceDocumentTree.vue'
import workspaceDescriptor from './WorkspaceDescriptor.json'
import WorkspaceNewFolderDialog from './views/repositoryView/dialogs/WorkspaceNewFolderDialog.vue'
import Dialog from 'primevue/dialog'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
import moment from 'moment'
import { mapState } from 'vuex'

const crypto = require('crypto')

export default defineComponent({
    name: 'dataset-management',
    components: { Sidebar, Listbox, Accordion, AccordionTab, WorkspaceDocumentTree, WorkspaceNewFolderDialog, Dialog, KnParameterSidebar },
    computed: {
        ...mapState({
            user: 'user'
        }),
        showRepository(): any {
            return this.user.functionalities.includes('SaveIntoFolderFunctionality')
        },
        storeFunctionalitiesExist(): any {
            this.createMenuItems()
            return this.user.functionalities.length > 0
        },
        userRole(): any {
            return this.user.sessionRole !== 'No default role selected' ? this.user.sessionRole : null
        }
    },
    data() {
        return {
            workspaceDescriptor,
            sidebarVisible: false,
            toggleCardDisplay: false,
            allFolders: [] as IFolder[],
            selectedFolder: {} as any,
            allDocuments: [] as IDocument[],
            items: [] as IFolder[],
            displayMenu: false,
            displayCreateFolderDialog: false,
            breadcrumbs: [] as any[],
            selectedBreadcrumb: null as any,
            accordionIcon: true,
            loading: false,
            qbeIframeVisible: false,
            qbeDialogVisible: false,
            parameterSidebarVisible: false,
            uniqueID: null as any,
            qbeDataset: null as any,
            datasetDrivers: null as any,
            qbeParameters: [] as any,
            menuItems: [] as any,
            filtersData: null as any,
            qbeUrl: ''
        }
    },
    created() {
        this.uniqueID = crypto.randomBytes(16).toString('hex')
        this.getAllRepositoryData()
    },
    mounted() {
        this.createMenuItems()
    },
    methods: {
        closeSidebar() {
            this.sidebarVisible = false
        },
        async getAllRepositoryData() {
            this.loading = true
            await this.getAllFolders()
            await this.getAllDocuments()
            this.loading = false
        },
        async getAllFolders() {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/folders/`).then((response: AxiosResponse<any>) => {
                this.allFolders = [...response.data]
                this.displayMenu = true
            })
        },
        async getAllDocuments() {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/`).then((response: AxiosResponse<any>) => {
                this.allDocuments = [...response.data]
            })
        },
        setActiveView(route) {
            this.$router.push(route)
            this.closeSidebar()
        },
        toggleDisplayView() {
            this.toggleCardDisplay = this.toggleCardDisplay ? false : true
        },
        setSelectedFolder(folder: any) {
            this.selectedFolder = folder
            this.createBreadcrumbs()
            this.$router.push(`/workspace/repository/${folder.id}`)
        },
        async deleteFolder(folder: any) {
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/foldersee/${folder.id}`, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.getAllRepositoryData()
                    this.$router.push('/workspace')
                })
                .catch((response: any) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response.message === 'sbi.workspace.organizer.folder.error.delete' ? this.$t('workspace.myRepository.folderDeleteError') : response.message
                    })
                })
            this.loading = false
        },
        showCreateFolderDialog(folder: any) {
            this.selectedFolder = folder
            this.displayCreateFolderDialog = true
        },
        async createNewFolder(newFolder: any) {
            newFolder.parentFunct = this.selectedFolder?.id
            newFolder.path = this.selectedFolder?.path + `/` + encodeURIComponent(newFolder.code)
            newFolder.prog = this.selectedFolder?.prog
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/organizer/foldersee/', newFolder, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('workspace.myRepository.folderCreatedMessage') })
                    this.getAllFolders()
                })
                .catch((response: any) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.displayCreateFolderDialog = false))
        },
        createBreadcrumbs() {
            let currentFolder = this.selectedFolder as any
            this.breadcrumbs = [] as any[]
            do {
                this.breadcrumbs.unshift({ label: currentFolder.data.name, node: currentFolder })
                currentFolder = currentFolder.data.parentFolder
            } while (currentFolder)
        },
        async setSelectedBreadcrumb(breadcrumb: any) {
            this.selectedBreadcrumb = breadcrumb
            this.$router.push(`/workspace/repository/${this.selectedBreadcrumb.node.id}`)
        },
        createMenuItems() {
            this.menuItems = []
            this.menuItems.push({ icon: 'fas fa-history', key: '0', label: 'workspace.menuLabels.recent', value: 'recent' }, { icon: 'fas fa-folder', key: '1', label: 'workspace.menuLabels.myRepository', value: 'repository' })
            if (this.user.functionalities.includes('SeeMyData')) {
                this.menuItems.push({ icon: 'fas fa-database', key: '2', label: 'workspace.menuLabels.myData', value: 'data' })
            }
            if (this.user.isSuperadmin || this.user.functionalities.includes('BuildQbeQueriesFunctionality')) {
                this.menuItems.push({ icon: 'fas fa-table', key: '3', label: 'workspace.menuLabels.myModels', value: 'models' })
            }
            if (this.user.functionalities.includes('CreateDocument')) {
                this.menuItems.push({ icon: 'fas fa-th-large', key: '4', label: 'workspace.menuLabels.myAnalysis', value: 'analysis' })
            }
            if (this.user.functionalities.includes('SeeSnapshotsFunctionality') && this.user.functionalities.includes('ViewScheduledWorkspace')) {
                this.menuItems.push({
                    icon: 'fas fa-stopwatch',
                    key: '5',
                    label: 'workspace.menuLabels.schedulation',
                    value: 'schedulation'
                })
            }
            if (this.user.functionalities.includes('DataPreparation')) {
                this.menuItems.push({ icon: 'fas fa-cogs', key: '6', label: 'workspace.menuLabels.advanced', value: 'advanced' })
            }
        },
        executeDocument(document: any) {
            const routeType = this.getRouteDocumentType(document)
            const label = document.label ? document.label : document.documentLabel
            this.$router.push(`/workspace/${routeType}/${label}`)
        },
        getRouteDocumentType(item: any) {
            let routeDocumentType = ''

            const type = item.typeCode ? item.typeCode : item.documentType

            switch (type) {
                case 'DATAMART':
                    routeDocumentType = 'registry'
                    break
                case 'DOCUMENT_COMPOSITE':
                    routeDocumentType = 'document-composite'
                    break
                case 'OFFICE_DOC':
                    routeDocumentType = 'office-doc'
                    break
                case 'OLAP':
                    routeDocumentType = 'olap'
                    break
                case 'MAP':
                    routeDocumentType = 'map'
                    break
                case 'REPORT':
                    routeDocumentType = 'report'
                    break
                case 'KPI':
                    routeDocumentType = 'kpi'
                    break
                case 'DOSSIER':
                    routeDocumentType = 'dossier'
                    break
                case 'ETL':
                    routeDocumentType = 'etl'
                    break
            }

            return routeDocumentType
        },
        async loadQBEDataset(dataset) {
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${dataset.label}`)
                .then((response: AxiosResponse<any>) => {
                    this.qbeDataset = response.data
                })
                .catch(() => {})
        },
        async loadDatasetDrivers(dataset) {
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/businessmodel/${dataset.name}/filters`, { name: dataset.name, role: this.userRole })
                .then((response: AxiosResponse<any>) => {
                    this.filtersData = response.data
                    this.formatDrivers()
                    if (response.data.isReadyForExecution) {
                        this.parameterSidebarVisible = false
                        this.initiateQbeIframe()
                    } else {
                        this.parameterSidebarVisible = true
                    }
                })
                .catch(() => {})
        },
        formatDrivers() {
            this.filtersData?.filterStatus?.forEach((el: any) => {
                el.parameterValue = el.multivalue ? [] : [{ value: '', description: '' }]
                if (el.driverDefaultValue?.length > 0) {
                    let valueIndex = '_col0'
                    let descriptionIndex = 'col1'
                    if (el.metadata?.colsMap) {
                        valueIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.valueColumn) as any
                        descriptionIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.descriptionColumn) as any
                    }

                    el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                        return { value: defaultValue.value ?? defaultValue[valueIndex], description: defaultValue.desc ?? defaultValue[descriptionIndex] }
                    })

                    if (el.type === 'DATE' && !el.selectionType && el.valueSelection === 'man_in' && el.showOnPanel === 'true') {
                        el.parameterValue[0].value = moment(el.parameterValue[0].description?.split('#')[0]).toDate() as any
                    }
                }
                if (el.data) {
                    el.data = el.data.map((data: any) => {
                        return this.formatParameterDataOptions(el, data)
                    })

                    if (el.data.length === 1) {
                        el.parameterValue = [...el.data]
                    }
                }
                if ((el.selectionType === 'COMBOBOX' || el.selectionType === 'LIST') && el.multivalue && el.mandatory && el.data.length === 1) {
                    el.showOnPanel = 'false'
                }

                if (!el.parameterValue) {
                    el.parameterValue = [{ value: '', description: '' }]
                }

                if (el.parameterValue[0] && !el.parameterValue[0].description) {
                    el.parameterValue[0].description = el.parameterDescription ? el.parameterDescription[0] : ''
                }
            })
        },
        formatParameterDataOptions(parameter: any, data: any) {
            const valueColumn = parameter.metadata.valueColumn
            const descriptionColumn = parameter.metadata.descriptionColumn
            const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
            const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)

            return { value: valueIndex ? data[valueIndex] : '', description: descriptionIndex ? data[descriptionIndex] : '' }
        },
        async buildQbeUrl(dataset) {
            let initialUrl = ''
            let language = this.user.locale.split('_')[0]
            let country = this.user.locale.split('_')[1]
            let drivers = encodeURI(JSON.stringify(this.datasetDrivers))
            initialUrl = `/knowageqbeengine/servlet/AdapterHTTP?NEW_SESSION=TRUE&SBI_LANGUAGE=${language}&SBI_SCRIPT=&user_id=${this.user.userUniqueIdentifier}&DEFAULT_DATASOURCE_FOR_WRITING_LABEL=CacheDS&SBI_COUNTRY=${country}&SBI_EXECUTION_ID=${this.uniqueID}&ACTION_NAME=QBE_ENGINE_START_ACTION_FROM_BM&MODEL_NAME=${dataset.name}&DATA_SOURCE_LABEL=${dataset.dataSourceLabel}&DATA_SOURCE_ID=${dataset.dataSourceId}&isTechnicalUser=true&DRIVERS=${drivers}`
            this.qbeUrl = process.env.VUE_APP_HOST_URL + initialUrl
        },
        getFormattedParameters(loadedParameters: { filterStatus: any[]; isReadyForExecution: boolean }) {
            let parameters = {} as any

            Object.keys(loadedParameters.filterStatus).forEach((key: any) => {
                const parameter = loadedParameters.filterStatus[key]

                if (!parameter.multivalue) {
                    parameters[parameter.urlName] = { value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description }
                } else {
                    parameters[parameter.urlName] = { value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription }
                }
            })

            return parameters
        },
        async prepareDataForQbe(dataset) {
            this.qbeDataset = dataset
            this.qbeDialogVisible = true
            await this.loadDatasetDrivers(dataset)
        },
        initiateQbeIframe() {
            this.datasetDrivers = this.getFormattedParameters(this.filtersData)
            this.buildQbeUrl(this.qbeDataset)
            this.qbeIframeVisible = true
            this.parameterSidebarVisible = false
        },
        closeQbeIframe() {
            this.qbeDialogVisible = false
            this.qbeIframeVisible = false
            this.parameterSidebarVisible = false
            this.datasetDrivers = null
            this.qbeUrl = ''
            this.qbeDataset = {}
        },
        saveQbeDataset() {
            let iframe = this.$refs.qbeIframe as any
            iframe.contentWindow.postMessage('saveDS', '*')
        }
    }
})
</script>
<style scoped lang="scss">
#sideMenu {
    width: 33.3333%;
}
.custom-kn-page-width {
    width: calc(100vw - var(--kn-mainmenu-width));
}
@media screen and (max-width: 1024px) {
    #sideMenu {
        -webkit-transition: width 0.3s;
        transition: width 0.3s;
        width: 0%;
    }
    #detailContent {
        width: 100%;
    }
    #showSidenavIcon {
        display: inline;
    }
}
</style>

<style lang="scss">
.mySidebar .p-listbox-list li:nth-child(2),
.mySidebar.p-sidebar .p-sidebar-header,
.mySidebar.p-sidebar .p-sidebar-content,
.menu-accordion .p-accordion-content,
.menu-accordion .p-tree,
.menu-accordion-sidebar .p-accordion-content,
.menu-accordion-sidebar .p-tree {
    padding: 0 !important;
}
.menu-accordion-sidebar .p-accordion-header {
    margin: -7px !important;
}
.menu-accordion .p-accordion-tab-active {
    margin: 0 !important;
    padding: 0 !important;
}
.menu-accordion .p-accordion-header-link {
    padding: 0.75rem 0.75rem !important;
    border-bottom: 1px solid #f2f2f2 !important;
}
.menu-accordion .p-accordion-toggle-icon {
    display: none;
}
.menu-accordion-icon {
    margin-left: auto;
}
.menu-accordion-sidebar .p-accordion-tab-active {
    margin: 0 !important;
}

@media screen and (min-width: 1025px) {
    #sideMenu {
        -webkit-transition: width 0.3s;
        transition: width 0.3s;
        width: 100%;
    }
    #showSidenavIcon {
        display: none;
    }
}

.metaweb-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - var(--kn-mainmenu-width));
    margin: 0;
}
.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
    overflow-x: hidden;
}
</style>
