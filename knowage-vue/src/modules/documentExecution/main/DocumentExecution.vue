<template>
    <div class="kn-height-full detail-page-container">
        <Toolbar v-if="!embed && !olapDesignerMode && !managementOpened && !dashboardGeneralSettingsOpened" class="kn-toolbar kn-toolbar--primary p-col-12">
            <template #start>
                <DocumentExecutionBreadcrumb v-if="breadcrumbs.length > 1" :breadcrumbs="breadcrumbs" @breadcrumbClicked="onBreadcrumbClick"></DocumentExecutionBreadcrumb>
                <span v-else>{{ document?.name }}</span>
            </template>
            <template #end>
                <div class="p-d-flex p-jc-around">
                    <Button v-if="mode == 'dashboard'" v-tooltip.left="$t('common.datasets')" icon="fas fa-database" class="p-button-text p-button-rounded p-button-plain p-mx-2" :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }" @click="openDashboardDatasetManagement"></Button>
                    <Button v-if="mode == 'dashboard'" v-tooltip.left="$t('common.save')" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain p-mx-2" :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }" @click="saveDashboard"></Button>
                    <Button v-if="mode !== 'dashboard' && canEditCockpit && documentMode === 'VIEW'" v-tooltip.left="$t('documentExecution.main.editCockpit')" icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain p-mx-2" @click="editCockpitDocumentConfirm"></Button>
                    <Button v-if="mode !== 'dashboard' && canEditCockpit && documentMode === 'EDIT'" v-tooltip.left="$t('documentExecution.main.viewCockpit')" icon="fa fa-eye" class="p-button-text p-button-rounded p-button-plain p-mx-2" @click="editCockpitDocumentConfirm"></Button>
                    <Button v-if="mode !== 'dashboard'" v-tooltip.left="$t('common.onlineHelp')" icon="pi pi-book" class="p-button-text p-button-rounded p-button-plain p-mx-2" @click="openHelp"></Button>
                    <Button v-if="!newDashboardMode" v-tooltip.left="$t('common.refresh')" icon="pi pi-refresh" class="p-button-text p-button-rounded p-button-plain p-mx-2" :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }" @click="refresh"></Button>
                    <Button
                        v-if="isParameterSidebarVisible && !newDashboardMode"
                        v-tooltip.left="$t('common.parameters')"
                        icon="fa fa-filter"
                        class="p-button-text p-button-rounded p-button-plain p-mx-2"
                        :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }"
                        data-test="parameter-sidebar-icon"
                        @click="parameterSidebarVisible = !parameterSidebarVisible"
                    ></Button>
                    <Button v-tooltip.left="$t('common.menu')" icon="fa fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain p-mx-2" :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }" @click="toggle"></Button>
                    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
                    <Button v-if="mode == 'dashboard'" id="add-widget-button" class="p-button-sm" :label="$t('dashboard.widgetEditor.addWidget')" icon="pi pi-plus-circle" @click="addWidget" />
                    <Button v-tooltip.left="$t('common.close')" icon="fa fa-times" class="p-button-text p-button-rounded p-button-plain p-mx-2" :class="{ 'dashboard-toolbar-icon': mode === 'dashboard' }" @click="closeDocument"></Button>
                </div>
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />

        <div id="document-execution-view" ref="document-execution-view" class="p-d-flex p-flex-row myDivToPrint">
            <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

            <template v-if="(filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible) || newDashboardMode">
                <Registry v-if="mode === 'registry'" :id="urlData?.sbiExecutionId" :reload-trigger="reloadTrigger"></Registry>
                <Dossier v-else-if="mode === 'dossier'" :id="document.id" :reload-trigger="reloadTrigger" :filter-data="filtersData"></Dossier>
                <Olap
                    v-else-if="mode === 'olap'"
                    :id="urlData?.sbiExecutionId"
                    :olap-id="document.id"
                    :olap-name="document.label"
                    :reload-trigger="reloadTrigger"
                    :olap-custom-view-visible="olapCustomViewVisible"
                    :hidden-form-data-prop="hiddenFormData"
                    @closeOlapCustomView="olapCustomViewVisible = false"
                    @applyCustomView="executeOlapCustomView"
                    @executeCrossNavigation="executeOLAPCrossNavigation"
                ></Olap>
                <template v-else-if="mode === 'dashboard' || newDashboardMode">
                    <DashboardController
                        v-for="(item, index) in breadcrumbs"
                        :key="index"
                        :sbiExecutionId="urlData?.sbiExecutionId"
                        :document="item.document"
                        :reloadTrigger="reloadTrigger"
                        :hiddenFormData="item.hiddenFormData"
                        :filtersData="item.filtersData"
                        :newDashboardMode="newDashboardMode"
                        @newDashboardSaved="onNewDashboardSaved"
                        @executeCrossNavigation="onExecuteCrossNavigation"
                    ></DashboardController>
                </template>
            </template>
            <iframe
                v-for="(item, index) in breadcrumbs"
                v-show="mode === 'iframe' && filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible && (item.label === document.name || (crossNavigationContainerData && index === breadcrumbs.length - 1))"
                :key="index"
                ref="documentFrame"
                :name="'documentFrame' + index"
                class="document-execution-iframe"
            ></iframe>

            <DocumentExecutionSchedulationsTable v-if="schedulationsTableVisible" id="document-execution-schedulations-table" :prop-schedulations="schedulations" @deleteSchedulation="onDeleteSchedulation" @close="schedulationsTableVisible = false"></DocumentExecutionSchedulationsTable>

            <KnParameterSidebar
                v-if="parameterSidebarVisible"
                class="document-execution-parameter-sidebar kn-overflow-y"
                :filters-data="filtersData"
                :prop-document="document"
                :user-role="userRole"
                :session-enabled="sessionEnabled"
                :date-format="dateFormat"
                data-test="parameter-sidebar"
                @execute="onExecute"
                @exportCSV="onExportCSV"
                @roleChanged="onRoleChange"
                @parametersChanged="$emit('parametersChanged', $event)"
            ></KnParameterSidebar>

            <DocumentExecutionHelpDialog :visible="helpDialogVisible" :prop-document="document" @close="helpDialogVisible = false"></DocumentExecutionHelpDialog>

            <DocumentExecutionRankDialog :visible="rankDialogVisible" :prop-document-rank="documentRank" @close="rankDialogVisible = false" @saveRank="onSaveRank"></DocumentExecutionRankDialog>
            <DocumentExecutionNotesDialog :visible="notesDialogVisible" :prop-document="document" @close="notesDialogVisible = false"></DocumentExecutionNotesDialog>
            <DocumentExecutionMetadataDialog :visible="metadataDialogVisible" :prop-document="document" :prop-metadata="metadata" :prop-loading="loading" @close="metadataDialogVisible = false" @saveMetadata="onMetadataSave"></DocumentExecutionMetadataDialog>
            <DocumentExecutionMailDialog :visible="mailDialogVisible" @close="mailDialogVisible = false" @sendMail="onMailSave"></DocumentExecutionMailDialog>
            <DocumentExecutionLinkDialog :visible="linkDialogVisible" :link-info="linkInfo" :embed-h-t-m-l="embedHTML" :prop-document="document" :parameters="linkParameters" @close="linkDialogVisible = false"></DocumentExecutionLinkDialog>
            <DocumentExecutionSelectCrossNavigationDialog :visible="destinationSelectDialogVisible" :cross-navigation-documents="crossNavigationDocuments" @close="destinationSelectDialogVisible = false" @selected="onCrossNavigationSelected"></DocumentExecutionSelectCrossNavigationDialog>
            <DocumentExecutionCNContainerDialog v-if="crossNavigationContainerData" :visible="crossNavigationContainerVisible" :data="crossNavigationContainerData" @close="onCrossNavigationContainerClose"></DocumentExecutionCNContainerDialog>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { iURLData, iExporter, iSchedulation } from './DocumentExecution'
import { createToolbarMenuItems } from './DocumentExecutionHelpers'
import { emitter } from '../dashboard/DashboardHelpers'
import DocumentExecutionBreadcrumb from './breadcrumbs/DocumentExecutionBreadcrumb.vue'
import DocumentExecutionHelpDialog from './dialogs/documentExecutionHelpDialog/DocumentExecutionHelpDialog.vue'
import DocumentExecutionRankDialog from './dialogs/documentExecutionRankDialog/DocumentExecutionRankDialog.vue'
import DocumentExecutionNotesDialog from './dialogs/documentExecutionNotesDialog/DocumentExecutionNotesDialog.vue'
import DocumentExecutionMetadataDialog from './dialogs/documentExecutionMetadataDialog/DocumentExecutionMetadataDialog.vue'
import DocumentExecutionMailDialog from './dialogs/documentExecutionMailDialog/DocumentExecutionMailDialog.vue'
import DocumentExecutionSchedulationsTable from './tables/documentExecutionSchedulationsTable/DocumentExecutionSchedulationsTable.vue'
import DocumentExecutionLinkDialog from './dialogs/documentExecutionLinkDialog/DocumentExecutionLinkDialog.vue'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
import { luxonFormatDate } from '@/helpers/commons/localeHelper'
import TieredMenu from 'primevue/tieredmenu'
import Registry from '../registry/Registry.vue'
import Dossier from '../dossier/Dossier.vue'
import Olap from '../olap/Olap.vue'
import DocumentExecutionSelectCrossNavigationDialog from './dialogs/documentExecutionSelectCrossNavigationDialog/DocumentExecutionSelectCrossNavigationDialog.vue'
import DocumentExecutionCNContainerDialog from './dialogs/documentExecutionCNContainerDialog/DocumentExecutionCNContainerDialog.vue'
import mainStore from '../../../App.store'
import { mapState, mapActions } from 'pinia'
import deepcopy from 'deepcopy'
import DashboardController from '../dashboard/DashboardController.vue'
import { getCorrectRolesForExecution } from '../../../helpers/commons/roleHelper'
import { executeAngularCrossNavigation, loadCrossNavigation } from './DocumentExecutionAngularCrossNavigationHelper'
import { getDocumentForCrossNavigation } from './DocumentExecutionCrossNavigationHelper'
import { loadFilters } from './DocumentExecutionDirverHelpers'

// @ts-ignore
// eslint-disable-next-line
window.execExternalCrossNavigation = function(outputParameters, otherOutputParameters, crossNavigationLabel) {
    postMessage(
        {
            type: 'crossNavigation',
            outputParameters: outputParameters,
            inputParameters: {},
            targetCrossNavigation: crossNavigationLabel,
            docLabel: null,
            otherOutputParameters: otherOutputParameters ? [otherOutputParameters] : []
        },
        '*'
    )
}

export default defineComponent({
    name: 'document-execution',
    components: {
        DocumentExecutionBreadcrumb,
        DocumentExecutionHelpDialog,
        DocumentExecutionRankDialog,
        DocumentExecutionNotesDialog,
        DocumentExecutionMetadataDialog,
        DocumentExecutionMailDialog,
        DocumentExecutionSchedulationsTable,
        DocumentExecutionLinkDialog,
        KnParameterSidebar,
        TieredMenu,
        Registry,
        Dossier,
        Olap,
        DocumentExecutionSelectCrossNavigationDialog,
        DocumentExecutionCNContainerDialog,
        DashboardController
    },
    props: {
        id: { type: String },
        parameterValuesMap: { type: Object },
        tabKey: { type: String },
        propMode: { type: String },
        selectedMenuItem: { type: Object },
        menuItemClickedTrigger: { type: Boolean }
    },
    emits: ['close', 'updateDocumentName', 'parametersChanged'],
    data() {
        return {
            managementOpened: false,
            document: null as any,
            hiddenFormData: {} as any,
            hiddenFormUrl: '' as string,
            documentMode: 'VIEW',
            filtersData: {} as {
                filterStatus: iParameter[]
                isReadyForExecution: boolean
            },
            urlData: null as iURLData | null,
            exporters: null as iExporter[] | null,
            mode: null as string | null,
            parameterSidebarVisible: false,
            toolbarMenuItems: [] as any[],
            helpDialogVisible: false,
            documentRank: null as string | null,
            rankDialogVisible: false,
            notesDialogVisible: false,
            metadataDialogVisible: false,
            mailDialogVisible: false,
            metadata: {} as any,
            schedulationsTableVisible: false,
            schedulations: [] as any[],
            linkDialogVisible: false,
            linkInfo: null as {
                isPublic: boolean
                noPublicRoleError: boolean
            } | null,
            sbiExecutionId: null as string | null,
            embedHTML: false,
            reloadTrigger: false,
            breadcrumbs: [] as any[],
            linkParameters: [],
            embed: false,
            olapCustomViewVisible: false,
            userRole: null,
            loading: false,
            olapDesignerMode: false,
            sessionEnabled: false,
            dateFormat: '' as string,
            destinationSelectDialogVisible: false,
            crossNavigationDocuments: [] as any[],
            angularData: null as any,
            crossNavigationContainerVisible: false,
            crossNavigationContainerData: null as any,
            newDashboardMode: false,
            dashboardGeneralSettingsOpened: false
        }
    },
    watch: {
        async menuItemClickedTrigger() {
            if (!this.selectedMenuItem) return
            const routes = ['registry', 'document-composite', 'report', 'office-doc', 'olap', 'map', 'report', 'kpi', 'dossier', 'etl']
            const test = routes.some((el) => this.selectedMenuItem?.to.includes(el))
            if (!test) return
            const label = this.selectedMenuItem.to.substring(this.selectedMenuItem.to.lastIndexOf('/') + 1)
            this.document = { label: label }
            if (!this.document.label) return
            this.breadcrumbs = []
            this.filtersData = {} as any
            await this.loadDocument()

            if (this.userRole) {
                await this.loadPage(true)
            } else {
                this.parameterSidebarVisible = true
            }
        }
    },
    async activated() {
        if (this.mode === 'iframe' && this.$route.name !== 'new-dashboard') {
            if (this.userRole) {
                await this.loadPage(true)
            } else {
                this.parameterSidebarVisible = true
            }
        }
    },
    deactivated() {
        this.parameterSidebarVisible = false
        window.removeEventListener('message', this.iframeEventsListener)
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user',
            configurations: 'configurations'
        }),
        canEditCockpit(): boolean {
            if (!this.user || !this.document) return false
            return (this.document.engine?.toLowerCase() === 'knowagecockpitengine' || this.document.engine?.toLowerCase() === 'knowagedashboardengine') && (this.user.functionalities?.includes('DocumentAdminManagement') || this.document.creationUser === this.user.userId)
        },
        sessionRole(): string | null {
            if (!this.user) return null
            return this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null
        },
        url(): string {
            if (this.document) {
                return (
                    import.meta.env.VITE_HOST_URL +
                    `/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=${this.document.id}&OBJECT_LABEL=${this.document.label}&TOOLBAR_VISIBLE=false&MENU_PARAMETERS=%7B%7D&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=${this.sbiExecutionId}&OBJECT_NAME=${this.document.name}&CROSS_PARAMETER=null`
                )
            } else {
                return ''
            }
        },
        isParameterSidebarVisible(): boolean {
            if (!this.userRole) return false

            let parameterVisible = false
            for (let i = 0; i < this.filtersData?.filterStatus?.length; i++) {
                const tempFilter = this.filtersData.filterStatus[i]
                if (tempFilter.showOnPanel === 'true' && tempFilter.visible) {
                    parameterVisible = true
                    break
                }
            }

            return parameterVisible
        }
    },
    async created() {
        this.setEventListeners()

        window.addEventListener('message', this.iframeEventsListener)

        if (this.propMode !== 'document-execution' && !this.$route.path.includes('olap-designer') && this.$route.name !== 'document-execution' && this.$route.name !== 'document-execution-embed' && this.$route.name !== 'document-execution-workspace') return

        if (this.$route.name === 'new-dashboard') {
            this.newDashboardMode = true
        }
        await this.loadUserConfig()

        this.isOlapDesignerMode()
        this.setMode()

        this.document = { label: this.id }

        if (!this.document.label) return

        this.userRole = this.user?.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user?.sessionRole : null

        if (this.document.label === 'new-dashboard') {
            this.newDashboardMode = true
            return
        }

        await this.loadDocument()

        let invalidRole = false
        getCorrectRolesForExecution(this.document).then(async (response: any) => {
            const correctRolesForExecution = response

            if (!this.userRole) {
                if (correctRolesForExecution.length == 1) {
                    this.userRole = correctRolesForExecution[0]
                } else {
                    this.parameterSidebarVisible = true
                }
            } else if (this.userRole) {
                if (correctRolesForExecution.length == 1) {
                    const correctRole = correctRolesForExecution[0]
                    if (this.userRole !== correctRole) {
                        this.setError({
                            title: this.$t('common.error.generic'),
                            msg: this.$t('documentExecution.main.userRoleError')
                        })
                        invalidRole = true
                    }
                }
            }
            if (!invalidRole) {
                if (this.userRole) {
                    await this.loadPage(true)
                } else {
                    this.parameterSidebarVisible = true
                }
            }
        })
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(mainStore, ['setInfo', 'setError', 'setDocumentExecutionEmbed']),
        iframeEventsListener(event) {
            if (event.data.type === 'crossNavigation') {
                executeAngularCrossNavigation(this, event, this.$http)
            } else if (event.data.type === 'cockpitExecuted') {
                this.loading = false
            }
        },
        editCockpitDocumentConfirm() {
            if (this.documentMode === 'EDIT') {
                this.$confirm.require({
                    message: this.$t('documentExecution.main.editModeConfirm'),
                    header: this.$t('documentExecution.main.editCockpit'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.editCockpitDocument()
                })
            } else {
                this.editCockpitDocument()
            }
        },
        async editCockpitDocument() {
            this.loading = true
            this.documentMode = this.documentMode === 'EDIT' ? 'VIEW' : 'EDIT'
            this.hiddenFormData.set('documentMode', this.documentMode)
            await this.loadURL(null)
        },
        openHelp() {
            this.helpDialogVisible = true
        },
        async refresh() {
            this.parameterSidebarVisible = false
            await this.loadURL(null)
            this.reloadTrigger = !this.reloadTrigger
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems = createToolbarMenuItems(
                this.document,
                {
                    print: this.print,
                    openRank: this.openRank,
                    export: this.export,
                    openMailDialog: this.openMailDialog,
                    openMetadata: this.openMetadata,
                    openNotes: this.openNotes,
                    showScheduledExecutions: this.showScheduledExecutions,
                    addToWorkspace: this.addToWorkspace,
                    showOLAPCustomView: this.showOLAPCustomView,
                    copyLink: this.copyLink,
                    openDashboardGeneralSettings: this.openDashboardGeneralSettings
                },
                this.exporters,
                this.user,
                this.isOrganizerEnabled(),
                this.mode,
                this.$t,
                this.newDashboardMode
            )
        },
        print() {
            window.print()
        },
        export(type: string) {
            if (this.document.typeCode === 'OLAP') {
                this.exportOlap(type)
            } else if (this.document.typeCode === 'REPORT') {
                window.open(this.urlData?.url + '&outputType=' + type, 'name', 'resizable=1,height=750,width=1000')
            } else {
                const tempIndex = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name)
                let tempFrame = window.frames[tempIndex]
                while (tempFrame && tempFrame.name !== 'documentFrame' + tempIndex) {
                    tempFrame = tempFrame[0].frames
                }
                tempFrame.postMessage({ type: 'export', format: type.toLowerCase() }, '*')
            }
        },
        exportOlap(type: string) {
            const url = type === 'PDF' ? `/knowagewhatifengine/restful-services/1.0/model/export/pdf?SBI_EXECUTION_ID=${this.sbiExecutionId}` : `/knowagewhatifengine/restful-services/1.0/model/export/excel?SBI_EXECUTION_ID=${this.sbiExecutionId}`
            window.open(url)
        },
        openMailDialog() {
            this.mailDialogVisible = true
        },
        async openMetadata() {
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentexecutionee/${this.document.id}/documentMetadata`).then((response: AxiosResponse<any>) => (this.metadata = response.data))
            this.metadataDialogVisible = true
            this.loading = false
        },
        async openRank() {
            await this.getRank()
            this.rankDialogVisible = true
        },
        openNotes() {
            this.notesDialogVisible = true
        },
        async showScheduledExecutions() {
            this.loading = true
            this.parameterSidebarVisible = false
            this.schedulationsTableVisible = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentsnapshot/getSnapshots?id=${this.document.id}`).then((response: AxiosResponse<any>) => {
                response.data?.schedulers.forEach((el: any) => this.schedulations.push({ ...el, urlPath: response.data.urlPath }))
            })
            this.loading = false
        },
        async copyLink(embedHTML: boolean) {
            this.loading = true
            this.linkParameters = this.getFormattedParameters()
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentexecution/canHavePublicExecutionUrl`, { label: this.document.label })
                .then((response: AxiosResponse<any>) => {
                    this.embedHTML = embedHTML
                    this.linkInfo = response.data
                    this.linkDialogVisible = true
                })
                .catch(() => {})
            this.loading = false
        },
        closeDocument() {
            const link = this.$route.path.includes('workspace') ? '/workspace' : '/document-browser'
            this.$router.push(link)
            this.breadcrumbs = []
            this.$emit('close')
        },
        setMode() {
            this.embed = this.$route.path.includes('embed')
            if (this.embed) {
                this.setDocumentExecutionEmbed()
            }

            if (this.$route.path.includes('registry')) {
                this.mode = 'registry'
            } else if (this.$route.path.includes('dossier')) {
                this.mode = 'dossier'
            } else if (this.$route.path.includes('olap')) {
                this.mode = 'olap'
            } else if (this.$route.path.includes('dashboard')) {
                this.mode = 'dashboard'
            } else {
                this.mode = 'iframe'
            }
        },
        async loadPage(initialLoading = false, documentLabel: string | null = null, crossNavigationPopupMode = false) {
            this.loading = true
            this.filtersData = await loadFilters(initialLoading, this.filtersData, this.document, this.breadcrumbs, this.userRole, this.parameterValuesMap, this.tabKey as string, this.sessionEnabled, this.$http, this.dateFormat, this)
            if (this.filtersData?.isReadyForExecution) {
                this.parameterSidebarVisible = false
                await this.loadURL(null, documentLabel, crossNavigationPopupMode)
                await this.loadExporters()
            } else if (this.filtersData?.filterStatus) {
                this.parameterSidebarVisible = true
            }

            this.updateMode()
            this.loading = false
        },
        updateMode() {
            if (this.document.typeCode === 'DATAMART') {
                this.mode = 'registry'
            } else if (this.document.typeCode === 'DOSSIER') {
                this.mode = 'dossier'
            } else if (this.document.typeCode === 'OLAP') {
                this.mode = 'olap'
            } else if (this.document.typeCode === 'DOCUMENT_COMPOSITE' && this.$route.path.includes('dashboard')) {
                this.mode = 'dashboard'
            } else {
                this.mode = 'iframe'
            }
        },
        async loadDocument() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documents/${this.document?.label}`).then((response: AxiosResponse<any>) => (this.document = response.data))

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name)
            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                this.breadcrumbs.push({
                    label: this.document.name,
                    document: this.document
                })
            }
        },
        async loadURL(olapParameters: any, documentLabel: string | null = null, crossNavigationPopupMode = false) {
            let error = false
            const postData = {
                label: this.document.label,
                role: this.userRole,
                parameters: olapParameters ? olapParameters : this.getFormattedParameters(),
                EDIT_MODE: 'null',
                IS_FOR_EXPORT: true,
                SBI_EXECUTION_ID: ''
            } as any

            if (this.sbiExecutionId) {
                postData.SBI_EXECUTION_ID = this.sbiExecutionId
            }

            if (this.document.typeCode === 'MAP') {
                postData.EDIT_MODE = 'edit_map'
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, postData)
                .then((response: AxiosResponse<any>) => {
                    error = false
                    this.urlData = response.data
                    this.sbiExecutionId = this.urlData?.sbiExecutionId as string
                })
                .catch((response: AxiosResponse<any>) => {
                    error = true
                    this.urlData = response.data
                    this.sbiExecutionId = this.urlData?.sbiExecutionId as string
                })

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name)
            if (index !== -1) {
                this.breadcrumbs[index].urlData = this.urlData
                this.sbiExecutionId = this.urlData?.sbiExecutionId as string
            }

            if (error) return

            await this.sendForm(documentLabel, crossNavigationPopupMode)
        },
        async loadExporters() {
            if (!this.urlData || !this.urlData.engineLabel) return
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
        },
        async sendForm(documentLabel: string | null = null, crossNavigationPopupMode = false) {
            const tempIndex = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name) as any

            const documentUrl = this.urlData?.url + '&timereloadurl=' + new Date().getTime()
            const postObject = {
                params: { document: null } as any,
                url: documentUrl.split('?')[0]
            }
            if (this.$route.query.documentMode === 'edit') this.documentMode = 'EDIT'
            postObject.params.documentMode = this.documentMode
            this.hiddenFormUrl = postObject.url
            const paramsFromUrl = documentUrl?.split('?')[1]?.split('&')

            for (const i in paramsFromUrl) {
                if (typeof paramsFromUrl !== 'function') {
                    postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1]
                }
            }

            let postForm = document.getElementById('postForm_' + postObject.params.document) as any
            if (!postForm) {
                postForm = document.createElement('form')
            }
            postForm.id = 'postForm_' + postObject.params.document
            postForm.action = import.meta.env.VITE_HOST_URL + postObject.url
            postForm.method = 'post'
            const iframeName = crossNavigationPopupMode ? 'documentFramePopup' : 'documentFrame'
            postForm.target = tempIndex !== -1 ? iframeName + tempIndex : documentLabel
            postForm.acceptCharset = 'UTF-8'
            document.body.appendChild(postForm)

            this.hiddenFormData = new URLSearchParams()

            for (const k in postObject.params) {
                const inputElement = document.getElementById('postForm_' + postObject.params.document + k) as any
                if (inputElement) {
                    inputElement.value = decodeURIComponent(postObject.params[k])
                    inputElement.value = inputElement.value.replace(/\+/g, ' ')

                    this.hiddenFormData.set(k, decodeURIComponent(postObject.params[k]).replace(/\+/g, ' '))
                } else {
                    const element = document.createElement('input')
                    element.type = 'hidden'
                    element.id = 'postForm_' + postObject.params.document + k
                    element.name = k
                    element.value = decodeURIComponent(postObject.params[k])
                    element.value = element.value.replace(/\+/g, ' ')

                    postForm.appendChild(element)
                    this.hiddenFormData.append(k, decodeURIComponent(postObject.params[k]).replace(/\+/g, ' '))
                }
            }

            for (let i = postForm.elements.length - 1; i >= 0; i--) {
                const postFormElement = postForm.elements[i].id.replace('postForm_' + postObject.params.document, '')

                if (!(postFormElement in postObject.params)) {
                    postForm.removeChild(postForm.elements[i])
                    this.hiddenFormData.delete(postFormElement)
                }
            }

            this.hiddenFormData.append('documentMode', this.documentMode)

            if (this.document.typeCode === 'DATAMART' || this.document.typeCode === 'DOSSIER' || this.document.typeCode === 'OLAP' || (this.document.typeCode === 'DOCUMENT_COMPOSITE' && this.mode === 'dashboard')) {
                await this.sendHiddenFormData()
            } else {
                postForm.submit()
            }

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name)
            if (index !== -1) this.breadcrumbs[index].hiddenFormData = this.hiddenFormData
        },
        async sendHiddenFormData() {
            await this.$http
                .post(this.hiddenFormUrl, this.hiddenFormData, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                    }
                })
                .then(() => {})
                .catch(() => {})
        },
        async onExecute() {
            this.loading = true
            this.filtersData.isReadyForExecution = true
            await this.loadURL(null)
            this.parameterSidebarVisible = false
            this.reloadTrigger = !this.reloadTrigger

            if (!this.exporters || this.exporters.length === 0) {
                await this.loadExporters()
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.export'))
                if (index === -1) {
                    this.toolbarMenuItems.splice(1, 0, {
                        label: this.$t('common.export'),
                        items: []
                    })
                } else {
                    this.exporters?.forEach((exporter: any) =>
                        this.toolbarMenuItems[index].items.push({
                            icon: 'fa fa-file-excel',
                            label: exporter.name,
                            command: () => this.export(exporter.name)
                        })
                    )
                }
            }

            if (this.sessionEnabled) {
                this.saveParametersInSession()
            }
            this.loading = false
        },
        async onExportCSV() {
            const postData = {
                documentId: this.document.id,
                documentLabel: this.document.label,
                exportType: 'CSV',
                parameters: this.getFormattedParametersForCSVExport()
            }
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/export/cockpitData`, postData)
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.exportSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        getFormattedParameters() {
            if (!this.filtersData || !this.filtersData.filterStatus) return {}

            const parameters = {} as any

            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                const parameter = this.filtersData.filterStatus[key]

                if (parameter.parameterValue) {
                    if (parameter.type === 'DATE' && parameter.parameterValue[0] && parameter.parameterValue[0].value) {
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value)
                        parameters[parameter.urlName + '_field_visible_description'] = this.getFormattedDate(parameter.parameterValue[0].value, true)
                    } else if (parameter.valueSelection === 'man_in') {
                        if (!parameter.parameterValue[0]) parameter.parameterValue[0] = { value: '', description: '' }
                        parameters[parameter.urlName] = parameter.type === 'NUM' && parameter.parameterValue[0].value ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.type === 'NUM' && parameter.parameterValue[0].description ? +parameter.parameterValue[0].description : parameter.parameterValue[0].description
                    } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                        parameters[parameter.urlName] = parameter.parameterValue.map((el: any) => el.value)
                        let tempString = ''
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            tempString += parameter.parameterValue[i].description
                            tempString += i === parameter.parameterValue.length - 1 ? '' : ';'
                        }
                        parameters[parameter.urlName + '_field_visible_description'] = tempString
                    } else {
                        parameters[parameter.urlName] = parameter.parameterValue[0] ? parameter.parameterValue[0].value : ''
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0] ? parameter.parameterValue[0].description : ''
                    }
                }
            })

            return parameters
        },
        getFormattedParametersForCSVExport() {
            if (!this.filtersData) return {}

            const parameters = {} as any

            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                const parameter = this.filtersData.filterStatus[key]

                if (parameter.parameterValue) {
                    if (parameter.type === 'DATE') {
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value)
                    } else if (parameter.valueSelection === 'man_in' && !parameter.multivalue) {
                        parameters[parameter.urlName] = parameter.type === 'NUM' && parameter.parameterValue[0].value ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                    } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                        let tempString = ''
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            tempString += parameter.parameterValue[i].value
                            tempString += i === parameter.parameterValue.length - 1 ? '' : ','
                        }
                        parameters[parameter.urlName] = tempString
                    } else {
                        parameters[parameter.urlName] = parameter.parameterValue[0].value
                    }
                }
            })

            return parameters
        },
        async getRank() {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documentrating/getvote`, { obj: this.document.id })
                .then((response: AxiosResponse<any>) => (this.documentRank = response.data))
                .catch((error: any) =>
                    this.setError({
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        },
        async onSaveRank(newRank: any) {
            if (newRank) {
                this.loading = true
                await this.$http
                    .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documentrating/vote`, { rating: newRank, obj: this.document.id })
                    .then(() =>
                        this.setInfo({
                            title: this.$t('common.toast.updateTitle'),
                            msg: this.$t('documentExecution.main.rankSaveSucces')
                        })
                    )
                    .catch((error: any) =>
                        this.setError({
                            title: this.$t('common.error.generic'),
                            msg: error
                        })
                    )
                this.loading = false
            }
            this.rankDialogVisible = false
        },
        async onMetadataSave(metadata: any) {
            this.loading = true
            const jsonMeta = [] as any[]
            const properties = ['shortText', 'longText', 'file']
            properties.forEach((property: string) =>
                metadata[property].forEach((el: any) => {
                    if (el.value || (property === 'file' && el.fileToSave)) {
                        if (property === 'file') el.value = JSON.stringify(el.value)
                        jsonMeta.push(el)
                    }
                })
            )

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentexecutionee/saveDocumentMetadata`, { id: this.document.id, jsonMeta: jsonMeta })
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.metadataDialogVisible = false
                })
                .catch(() => {})
            this.loading = false
        },
        async onMailSave(mail: any) {
            this.loading = true
            const postData = {
                ...mail,
                label: this.document.label,
                docId: this.document.id,
                userId: this.user.userId,
                parameters: this.getFormattedParameters()
            }
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentexecutionmail/sendMail`, postData)
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.sendMailSuccess')
                    })
                    this.mailDialogVisible = false
                })
                .catch((error: any) => {
                    this.setError({
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                })
            this.loading = false
        },
        async onDeleteSchedulation(schedulation: any) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentsnapshot/deleteSnapshot`, { SNAPSHOT: '' + schedulation.id })
                .then(async () => {
                    this.removeSchedulation(schedulation)
                    this.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        removeSchedulation(schedulation: iSchedulation) {
            const index = this.schedulations.findIndex((el: any) => el.id === schedulation.id)
            if (index !== -1) this.schedulations.splice(index, 1)
        },
        getFormattedDate(date: any, useDefaultFormat?: boolean) {
            const format = date instanceof Date ? undefined : 'dd/MM/yyyy'
            return luxonFormatDate(date, format, useDefaultFormat ? undefined : this.configurations['SPAGOBI.DATE-FORMAT-SERVER.format'])
        },
        async onBreadcrumbClick(item: any) {
            this.document = item.document
            this.filtersData = item.filtersData
            this.urlData = item.urlData
            this.hiddenFormData = item.hiddenFormData
            this.documentMode = 'VIEW'
            this.updateMode()
        },
        async onRoleChange(role: string) {
            this.userRole = role as any
            this.filtersData = {} as {
                filterStatus: iParameter[]
                isReadyForExecution: boolean
            }
            this.urlData = null
            this.exporters = null
            await this.loadPage()
        },
        setNavigationParametersFromCurrentFilters(formatedParams: any, navigationParams: any) {
            const navigationParamsKeys = navigationParams ? Object.keys(navigationParams) : []
            const formattedParameters = this.getFormattedParameters()
            const formattedParametersKeys = formattedParameters ? Object.keys(formattedParameters) : []
            if (navigationParamsKeys.length > 0 && formattedParametersKeys.length > 0) {
                for (let i = 0; i < navigationParamsKeys.length; i++) {
                    const index = formattedParametersKeys.findIndex((key: string) => key === navigationParams[navigationParamsKeys[i]].value.label && navigationParams[navigationParamsKeys[i]].value.isInput)
                    if (index !== -1) {
                        formatedParams[navigationParamsKeys[i]] = formattedParameters[formattedParametersKeys[index]]
                        formatedParams[navigationParamsKeys[i] + '_field_visible_description'] = formattedParameters[formattedParametersKeys[index] + '_field_visible_description'] ? formattedParameters[formattedParametersKeys[index] + '_field_visible_description'] : ''
                    }
                }
            }
        },
        showOLAPCustomView() {
            this.olapCustomViewVisible = true
        },
        async executeOlapCustomView(payload: any) {
            this.loading = true
            this.olapCustomViewVisible = false
            await this.loadURL(payload)
            this.reloadTrigger = !this.reloadTrigger
            this.loading = false
        },
        async executeOLAPCrossNavigation(crossNavigationParams: any) {
            let temp = {} as any
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document.label}/loadCrossNavigationByDocument`).then((response: AxiosResponse<any>) => (temp = response.data))
            this.loading = false

            if (!temp || temp.length === 0) {
                this.setError({
                    title: this.$t('common.error.generic'),
                    msg: this.$t('documentExecution.main.crossNavigationNoTargetError')
                })
                return
            }

            this.document = {
                ...temp[0].document,
                navigationParams: this.formatOLAPNavigationParams(crossNavigationParams, temp[0].navigationParams)
            }

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.name)
            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                this.breadcrumbs.push({
                    label: this.document.name,
                    document: this.document
                })
            }

            await this.loadPage()
            this.reloadTrigger = !this.reloadTrigger
        },
        formatOLAPNavigationParams(crossNavigationParams: any, navigationParams: any) {
            const crossNavigationParamKeys = Object.keys(crossNavigationParams)
            const formattedParams = {} as any

            Object.keys(navigationParams).forEach((key: string) => {
                const index = crossNavigationParamKeys.findIndex((el: string) => el === navigationParams[key].value.label)
                if (index !== -1) {
                    formattedParams[key] = crossNavigationParams[crossNavigationParamKeys[index]]
                }
            })

            return formattedParams
        },
        isOlapDesignerMode() {
            if (this.$route.name === 'olap-designer') {
                this.olapDesignerMode = true
            }
        },
        isOrganizerEnabled() {
            return this.user.isSuperadmin || this.user.functionalities.includes('DocumentAdminManagement') || this.user.functionalities.includes('SaveIntoFolderFunctionality')
        },
        async addToWorkspace() {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${this.document.id}`, {}, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                })
                .catch((error) => {
                    this.setError({
                        title: this.$t('common.toast.updateTitle'),
                        msg: error.message === 'sbi.workspace.organizer.document.addtoorganizer.error.duplicateentry' ? this.$t('documentExecution.main.addToWorkspaceError') : error.message
                    })
                })
            this.loading = false
        },
        async loadUserConfig() {
            this.sessionEnabled = this.configurations['SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled'] === 'false' ? false : true
            this.dateFormat = this.configurations['SPAGOBI.DATE-FORMAT-SERVER.format'] === '%Y-%m-%d' ? 'dd/MM/yyyy' : this.configurations['SPAGOBI.DATE-FORMAT-SERVER.format']
        },
        saveParametersInSession() {
            const tempFilters = deepcopy(this.filtersData)
            tempFilters.filterStatus?.forEach((filter: any) => ['dataDependsOnParameters', 'dataDependentParameters', 'dependsOnParameters', 'dependentParameters', 'lovDependsOnParameters', 'lovDependentParameters'].forEach((field: string) => delete filter[field]))
            sessionStorage.setItem(this.document.label, JSON.stringify(tempFilters))
        },
        async onCrossNavigationSelected(event: any) {
            this.destinationSelectDialogVisible = false
            await loadCrossNavigation(this, event, this.angularData)
        },
        onCrossNavigationContainerClose() {
            this.crossNavigationContainerData = null
            this.crossNavigationContainerVisible = true
            this.onBreadcrumbClick(this.breadcrumbs[0])
        },
        addWidget() {
            emitter.emit('openNewWidgetPicker')
        },
        openDashboardDatasetManagement() {
            this.managementOpened = true
            emitter.emit('openDatasetManagement')
        },
        setEventListeners() {
            emitter.on('datasetManagementClosed', this.onDatasetManagementClosed)
            emitter.on('widgetEditorOpened', this.onWidgetEditorOpened)
            emitter.on('widgetEditorClosed', this.onWidgetEditorClosed)
            emitter.on('dashboardGeneralSettingsClosed', this.onDashboardGeneralSettingsClosed)
        },
        removeEventListeners() {
            emitter.off('datasetManagementClosed', this.onDatasetManagementClosed)
            emitter.off('widgetEditorOpened', this.onWidgetEditorOpened)
            emitter.off('widgetEditorClosed', this.onWidgetEditorClosed)
            emitter.off('dashboardGeneralSettingsClosed', this.onDashboardGeneralSettingsClosed)
        },
        onDatasetManagementClosed() {
            this.managementOpened = false
        },
        onWidgetEditorOpened() {
            this.managementOpened = true
        },
        onWidgetEditorClosed() {
            this.managementOpened = false
        },
        onDashboardGeneralSettingsClosed() {
            this.dashboardGeneralSettingsOpened = false
        },
        openDashboardGeneralSettings() {
            this.dashboardGeneralSettingsOpened = true
            emitter.emit('openDashboardGeneralSettings')
        },
        saveDashboard() {
            emitter.emit('saveDashboard')
        },
        async onNewDashboardSaved(document: { name: string; label: string }) {
            this.document.label = document.label
            await this.loadDocument()

            this.userRole = this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null

            if (this.userRole) {
                await this.loadPage(true)
            } else {
                this.parameterSidebarVisible = true
            }
            this.newDashboardMode = false
        },
        async onExecuteCrossNavigation(payload: any) {
            this.document = getDocumentForCrossNavigation(payload, this.document)
            console.log('!!!!!!!!! DOCUMENT: ', this.document)
            this.filtersData = await loadFilters(false, this.filtersData, this.document, this.breadcrumbs, this.userRole, this.parameterValuesMap, this.tabKey as string, this.sessionEnabled, this.$http, this.dateFormat, this)
            console.log('!!!!!!! FORMATTED FILTERS DATA: ', this.filtersData)
        }
    }
})
</script>

<style lang="scss">
#document-execution-view {
    position: relative;
    height: 100%;
    width: 100%;
}

#document-execution-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.document-execution-parameter-sidebar {
    margin-left: auto;
}

.document-execution-iframe {
    width: 100%;
    height: 100%;
}

.document-execution-parameter-sidebar {
    height: 60vh;
}

#document-execution-schedulations-table {
    position: relative;
    z-index: 100;
    width: 100%;
    height: 90%;
}

.document-execution-iframe {
    border: 0;
}

.detail-page-container {
    display: flex;
    flex-direction: column;
}
@media print {
    body * {
        visibility: hidden;
    }
    #document-execution-view,
    #document-execution-view * {
        visibility: visible;
    }

    #document-execution-view .document-execution-parameter-sidebar,
    #document-execution-view .document-execution-parameter-sidebar *,
    #document-execution-view #document-execution-backdrop {
        visibility: hidden;
    }

    #document-execution-view {
        background-color: white;
        height: 100%;
        width: 100%;
        position: fixed;
        top: 0;
        left: 0;
        margin: 0;
    }
}

.p-tieredmenu .p-menuitem-active > .p-submenu-list {
    left: unset !important;
}

.p-submenu-list {
    right: 100% !important;
}

.dashboard-toolbar-icon {
    border: 1px solid white !important;
    padding: 1.5px !important;
    border-radius: 5px !important;
}

#add-widget-button {
    background-color: var(--kn-color-fab);
    min-width: 120px;
}
</style>
