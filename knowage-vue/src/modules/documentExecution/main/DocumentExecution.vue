<template>
    <div class="kn-height-full detail-page-container">
        <Toolbar v-if="!embed && !olapDesignerMode" class="kn-toolbar kn-toolbar--primary p-col-12">
            <template #start>
                <span>{{ document?.name }}</span>
            </template>

            <template #end>
                <div class="p-d-flex p-jc-around">
                    <Button icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'VIEW'" v-tooltip.left="$t('documentExecution.main.editCockpit')" @click="editCockpitDocumentConfirm"></Button>
                    <Button icon="fa fa-eye" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'EDIT'" v-tooltip.left="$t('documentExecution.main.viewCockpit')" @click="editCockpitDocumentConfirm"></Button>
                    <Button icon="pi pi-book" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.left="$t('common.onlineHelp')" @click="openHelp"></Button>
                    <Button icon="pi pi-refresh" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.left="$t('common.refresh')" @click="refresh"></Button>
                    <Button icon="fa fa-filter" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-if="isParameterSidebarVisible" v-tooltip.left="$t('common.parameters')" @click="parameterSidebarVisible = !parameterSidebarVisible" data-test="parameter-sidebar-icon"></Button>
                    <Button icon="fa fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.left="$t('common.menu')" @click="toggle"></Button>
                    <TieredMenu ref="menu" :model="toolbarMenuItems" :popup="true" />
                    <Button icon="fa fa-times" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.left="$t('common.close')" @click="closeDocument"></Button>
                </div>
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
        <DocumentExecutionBreadcrumb v-if="breadcrumbs.length > 1" :breadcrumbs="breadcrumbs" @breadcrumbClicked="onBreadcrumbClick"></DocumentExecutionBreadcrumb>

        <div ref="document-execution-view" id="document-execution-view" class="p-d-flex p-flex-row myDivToPrint">
            <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

            <template v-if="filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible">
                <Registry v-if="mode === 'registry'" :id="urlData?.sbiExecutionId" :reloadTrigger="reloadTrigger"></Registry>
                <Dossier v-else-if="mode === 'dossier'" :id="document.id" :reloadTrigger="reloadTrigger" :filterData="filtersData"></Dossier>
                <Olap
                    v-else-if="mode === 'olap'"
                    :id="urlData?.sbiExecutionId"
                    :olapId="document.id"
                    :olapName="document.label"
                    :reloadTrigger="reloadTrigger"
                    :olapCustomViewVisible="olapCustomViewVisible"
                    :hiddenFormDataProp="hiddenFormData"
                    @closeOlapCustomView="olapCustomViewVisible = false"
                    @applyCustomView="executeOlapCustomView"
                    @executeCrossNavigation="executeOLAPCrossNavigation"
                ></Olap>
            </template>

            <iframe
                v-for="(item, index) in breadcrumbs"
                :key="index"
                ref="documentFrame"
                :name="'documentFrame' + index"
                v-show="mode === 'iframe' && filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible && item.label === document.label"
                class="document-execution-iframe"
            ></iframe>

            <DocumentExecutionSchedulationsTable id="document-execution-schedulations-table" v-if="schedulationsTableVisible" :propSchedulations="schedulations" @deleteSchedulation="onDeleteSchedulation" @close="schedulationsTableVisible = false"></DocumentExecutionSchedulationsTable>

            <KnParameterSidebar
                class="document-execution-parameter-sidebar kn-overflow-y"
                v-if="parameterSidebarVisible"
                :filtersData="filtersData"
                :propDocument="document"
                :userRole="userRole"
                :sessionEnabled="sessionEnabled"
                :dateFormat="dateFormat"
                @execute="onExecute"
                @exportCSV="onExportCSV"
                @roleChanged="onRoleChange"
                @parametersChanged="$emit('parametersChanged', $event)"
                data-test="parameter-sidebar"
            ></KnParameterSidebar>

            <DocumentExecutionHelpDialog :visible="helpDialogVisible" :propDocument="document" @close="helpDialogVisible = false"></DocumentExecutionHelpDialog>

            <DocumentExecutionRankDialog :visible="rankDialogVisible" :propDocumentRank="documentRank" @close="rankDialogVisible = false" @saveRank="onSaveRank"></DocumentExecutionRankDialog>
            <DocumentExecutionNotesDialog :visible="notesDialogVisible" :propDocument="document" @close="notesDialogVisible = false"></DocumentExecutionNotesDialog>
            <DocumentExecutionMetadataDialog :visible="metadataDialogVisible" :propDocument="document" :propMetadata="metadata" :propLoading="loading" @close="metadataDialogVisible = false" @saveMetadata="onMetadataSave"></DocumentExecutionMetadataDialog>
            <DocumentExecutionMailDialog :visible="mailDialogVisible" @close="mailDialogVisible = false" @sendMail="onMailSave"></DocumentExecutionMailDialog>
            <DocumentExecutionLinkDialog :visible="linkDialogVisible" :linkInfo="linkInfo" :embedHTML="embedHTML" :propDocument="document" :parameters="linkParameters" @close="linkDialogVisible = false"></DocumentExecutionLinkDialog>
            <DocumentExecutionSelectCrossNavigationDialog :visible="destinationSelectDialogVisible" :crossNavigationDocuments="crossNavigationDocuments" @close="destinationSelectDialogVisible = false" @selected="onCrossNavigationSelected"></DocumentExecutionSelectCrossNavigationDialog>
            <DocumentExecutionCNContainerDialog v-if="crossNavigationContainerData" :visible="crossNavigationContainerVisible" :data="crossNavigationContainerData" @close="onCrossNavigationContainerClose"></DocumentExecutionCNContainerDialog>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iParameter } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { iURLData, iExporter, iSchedulation } from './DocumentExecution'
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
import moment from 'moment'
import DocumentExecutionSelectCrossNavigationDialog from './dialogs/documentExecutionSelectCrossNavigationDialog/DocumentExecutionSelectCrossNavigationDialog.vue'
import DocumentExecutionCNContainerDialog from './dialogs/documentExecutionCNContainerDialog/DocumentExecutionCNContainerDialog.vue'

const deepcopy = require('deepcopy')

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
        DocumentExecutionCNContainerDialog
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
            user: null as any,
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
            crossNavigationContainerData: null as any
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
        sessionRole(): string | null {
            if (!this.user) return null
            return this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null
        },
        url(): string {
            if (this.document) {
                return (
                    process.env.VUE_APP_HOST_URL +
                    `/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=${this.document.id}&OBJECT_LABEL=${this.document.label}&TOOLBAR_VISIBLE=false&MENU_PARAMETERS=%7B%7D&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=${this.sbiExecutionId}&OBJECT_NAME=${this.document.name}&CROSS_PARAMETER=null`
                )
            } else {
                return ''
            }
        },
        isParameterSidebarVisible(): boolean {
            let parameterVisible = false
            for (let i = 0; i < this.filtersData?.filterStatus?.length; i++) {
                const tempFilter = this.filtersData.filterStatus[i]
                if (tempFilter.showOnPanel === 'true' && tempFilter.visible) {
                    parameterVisible = true
                    break
                }
            }

            return parameterVisible || !this.sessionRole
        }
    },
    async created() {
        window.addEventListener('message', this.iframeEventsListener)

        if (this.propMode !== 'document-execution' && !this.$route.path.includes('olap-designer') && this.$route.name !== 'document-execution' && this.$route.name !== 'document-execution-embed' && this.$route.name !== 'document-execution-workspace') return

        await this.loadUserConfig()

        this.isOlapDesignerMode()
        this.setMode()

        this.document = { label: this.id }

        if (!this.document.label) return

        await this.loadDocument()

        this.user = (this.$store.state as any).user
        this.userRole = this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null

        if (this.userRole) {
            await this.loadPage(true)
        } else {
            this.parameterSidebarVisible = true
        }
    },
    methods: {
        iframeEventsListener(event) {
            if (event.data.type === 'crossNavigation') {
                this.executeCrossNavigation(event)
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
            this.toolbarMenuItems = []
            this.toolbarMenuItems.push({
                label: this.$t('common.file'),
                items: [
                    {
                        icon: 'pi pi-print',
                        label: this.$t('common.print'),
                        command: () => this.print()
                    }
                ]
            })

            if (this.exporters && this.exporters.length !== 0) {
                this.toolbarMenuItems.push({
                    label: this.$t('common.export'),
                    items: []
                })
            }

            if (this.user.enterprise) {
                this.toolbarMenuItems.push({
                    label: this.$t('common.info.info'),
                    items: [
                        {
                            icon: 'pi pi-star',
                            label: this.$t('common.rank'),
                            command: () => this.openRank()
                        }
                    ]
                })
            }

            this.toolbarMenuItems.push({
                label: this.$t('common.shortcuts'),
                items: []
            })

            this.exporters?.forEach((exporter: any) =>
                this.toolbarMenuItems[1].items.push({
                    icon: 'fa fa-file-excel',
                    label: exporter.name,
                    command: () => this.export(exporter.name)
                })
            )

            if (this.user.functionalities.includes('SendMailFunctionality') && this.document.typeCode === 'REPORT') {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.info.info'))
                if (index !== -1) {
                    this.toolbarMenuItems[index].items.push({
                        icon: 'pi pi-envelope',
                        label: this.$t('common.sendByEmail'),
                        command: () => this.openMailDialog()
                    })
                } else {
                    this.toolbarMenuItems.push({
                        label: this.$t('common.export'),
                        items: [
                            {
                                icon: 'pi pi-envelope',
                                label: this.$t('common.sendByEmail'),
                                command: () => this.openMailDialog()
                            }
                        ]
                    })
                }
            }

            if (this.user.functionalities.includes('SeeMetadataFunctionality')) {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.info.info'))
                if (index !== -1)
                    this.toolbarMenuItems[index].items.unshift({
                        icon: 'pi pi-info-circle',
                        label: this.$t('common.metadata'),
                        command: () => this.openMetadata()
                    })
            }

            if (this.user.functionalities.includes('SeeNotesFunctionality')) {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.info.info'))
                if (index !== -1)
                    this.toolbarMenuItems[index].items.push({
                        icon: 'pi pi-file',
                        label: this.$t('common.notes'),
                        command: () => this.openNotes()
                    })
            }

            if (this.user.functionalities.includes('SeeSnapshotsFunctionality') && this.user.enterprise) {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.shortcuts'))
                if (index !== -1)
                    this.toolbarMenuItems[index].items.unshift({
                        icon: '',
                        label: this.$t('documentExecution.main.showScheduledExecutions'),
                        command: () => this.showScheduledExecutions()
                    })
            }

            if (this.isOrganizerEnabled()) {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.shortcuts'))
                if (index !== -1)
                    this.toolbarMenuItems[index].items.unshift({
                        icon: 'fa fa-suitcase ',
                        label: this.$t('documentExecution.main.addToWorkspace'),
                        command: () => this.addToWorkspace()
                    })
            }

            if (this.mode === 'olap') {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.shortcuts'))
                if (index !== -1)
                    this.toolbarMenuItems[index].items.unshift({
                        icon: '',
                        label: this.$t('documentExecution.main.showOLAPCustomView'),
                        command: () => this.showOLAPCustomView()
                    })
            }

            if (this.user.functionalities.includes('EnableToCopyAndEmbed')) {
                const index = this.toolbarMenuItems.findIndex((item: any) => item.label === this.$t('common.shortcuts'))
                if (index !== -1) {
                    this.toolbarMenuItems[index].items.push({
                        icon: 'fa fa-share',
                        label: this.$t('documentExecution.main.copyLink'),
                        command: () => this.copyLink(false)
                    })
                    this.toolbarMenuItems[index].items.push({
                        icon: 'fa fa-share',
                        label: this.$t('documentExecution.main.embedInHtml'),
                        command: () => this.copyLink(true)
                    })
                }
            }
        },
        print() {
            window.print()
        },
        export(type: string) {
            if (this.document.typeCode === 'OLAP') {
                this.exportOlap(type)
            } else {
                const tempIndex = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecutionee/${this.document.id}/documentMetadata`).then((response: AxiosResponse<any>) => (this.metadata = response.data))
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentsnapshot/getSnapshots?id=${this.document.id}`).then((response: AxiosResponse<any>) => {
                response.data?.schedulers.forEach((el: any) => this.schedulations.push({ ...el, urlPath: response.data.urlPath }))
            })
            this.loading = false
        },
        async copyLink(embedHTML: boolean) {
            this.loading = true
            this.linkParameters = this.getFormattedParameters()
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/canHavePublicExecutionUrl`, { label: this.document.label })
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
                this.$store.commit('setDocumentExecutionEmbed')
            }

            if (this.$route.path.includes('registry')) {
                this.mode = 'registry'
            } else if (this.$route.path.includes('dossier')) {
                this.mode = 'dossier'
            } else if (this.$route.path.includes('olap')) {
                this.mode = 'olap'
            } else {
                this.mode = 'iframe'
            }
        },
        async loadPage(initialLoading: boolean = false, documentLabel: string | null = null) {
            this.loading = true

            await this.loadFilters(initialLoading)
            if (this.filtersData?.isReadyForExecution) {
                this.parameterSidebarVisible = false
                await this.loadURL(null, documentLabel)
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
            } else {
                this.mode = 'iframe'
            }
        },
        async loadDocument() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.document?.label}`).then((response: AxiosResponse<any>) => (this.document = response.data))

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)

            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                this.breadcrumbs.push({
                    label: this.document.label,
                    document: this.document
                })
            }
        },
        async loadFilters(initialLoading: boolean = false) {
            if (this.parameterValuesMap && this.parameterValuesMap[this.document.label + '-' + this.tabKey] && initialLoading) {
                this.filtersData = this.parameterValuesMap[this.document.label + '-' + this.tabKey]
                this.setFiltersForBreadcrumbItem()
                return
            }

            if (this.sessionEnabled && !this.document.navigationParams) {
                const tempFilters = sessionStorage.getItem(this.document.label)
                if (tempFilters) {
                    this.filtersData = JSON.parse(tempFilters) as {
                        filterStatus: iParameter[]
                        isReadyForExecution: boolean
                    }
                    this.filtersData.filterStatus?.forEach((filter: any) => {
                        if (filter.type === 'DATE' && filter.parameterValue[0].value) {
                            filter.parameterValue[0].value = new Date(filter.parameterValue[0].value)
                        }
                    })
                    this.setFiltersForBreadcrumbItem()
                    return
                }
            }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentexecution/filters`, {
                    label: this.document.label,
                    role: this.userRole,
                    parameters: this.document.navigationParams ?? {}
                })
                .then((response: AxiosResponse<any>) => (this.filtersData = response.data))
                .catch((error: any) => {
                    if (error.response?.status === 500) {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: this.$t('documentExecution.main.userRoleError')
                        })
                    }
                })

            this.filtersData?.filterStatus?.forEach((el: iParameter) => {
                el.parameterValue = el.multivalue ? [] : [{ value: '', description: '' }]
                if (el.driverDefaultValue?.length > 0) {
                    let valueIndex = '_col0'
                    let descriptionIndex = 'col1'
                    if (el.metadata?.colsMap) {
                        valueIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.valueColumn) as any
                        descriptionIndex = Object.keys(el.metadata?.colsMap).find((key: string) => el.metadata.colsMap[key] === el.metadata.descriptionColumn) as any
                    }

                    el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                        return {
                            value: defaultValue.value ?? defaultValue[valueIndex],
                            description: defaultValue.desc ?? defaultValue[descriptionIndex]
                        }
                    })

                    if (el.type === 'DATE' && !el.selectionType && el.valueSelection === 'man_in' && el.showOnPanel === 'true' && el.visible) {
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
                    el.visible = false
                }

                if (!el.parameterValue) {
                    el.parameterValue = [{ value: '', description: '' }]
                }

                if (el.parameterValue[0] && !el.parameterValue[0].description) {
                    el.parameterValue[0].description = el.parameterDescription ? el.parameterDescription[0] : ''
                }
            })

            if (this.document.navigationParams) {
                this.loadNavigationParamsInitialValue()
            }

            this.setFiltersForBreadcrumbItem()
        },
        setFiltersForBreadcrumbItem() {
            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) this.breadcrumbs[index].filtersData = this.filtersData
        },
        loadNavigationParamsInitialValue() {
            Object.keys(this.document.navigationParams).forEach((key: string) => {
                for (let i = 0; i < this.filtersData.filterStatus.length; i++) {
                    const tempParam = this.filtersData.filterStatus[i]
                    if (key === tempParam.urlName || key === tempParam.label) {
                        tempParam.parameterValue[0].value = this.document.navigationParams[key]
                        if (this.document.navigationParams[key + '_field_visible_description']) tempParam.parameterValue[0].description = this.document.navigationParams[key + '_field_visible_description']
                        if (tempParam.selectionType === 'COMBOBOX') this.setCrossNavigationComboParameterDescription(tempParam)
                        if (tempParam.type === 'DATE' && tempParam.parameterValue[0] && tempParam.parameterValue[0].value) {
                            tempParam.parameterValue[0].value = new Date(tempParam.parameterValue[0].value)
                        }
                    }
                }
            })
        },
        setCrossNavigationComboParameterDescription(tempParam: any) {
            if (tempParam.parameterValue[0]) {
                const index = tempParam.data.findIndex((option: any) => option.value === tempParam.parameterValue[0].value)
                if (index !== -1) tempParam.parameterValue[0].description = tempParam.data[index].description
            }
        },
        formatParameterDataOptions(parameter: iParameter, data: any) {
            const valueColumn = parameter.metadata.valueColumn
            const descriptionColumn = parameter.metadata.descriptionColumn
            const valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
            const descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)

            return {
                value: valueIndex ? data[valueIndex] : '',
                description: descriptionIndex ? data[descriptionIndex] : ''
            }
        },
        async loadURL(olapParameters: any, documentLabel: string | null = null) {
            const postData = {
                label: this.document.label,
                role: this.userRole,
                parameters: olapParameters ? olapParameters : this.getFormattedParameters(),
                EDIT_MODE: 'null',
                IS_FOR_EXPORT: true
            } as any

            if (this.sbiExecutionId) {
                postData.SBI_EXECUTION_ID = this.sbiExecutionId
            }

            if (this.document.typeCode === 'MAP') {
                postData.EDIT_MODE = 'edit_map'
            }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.urlData = response.data
                    this.sbiExecutionId = this.urlData?.sbiExecutionId as string
                })
                .catch((response: AxiosResponse<any>) => {
                    this.urlData = response.data
                    this.sbiExecutionId = this.urlData?.sbiExecutionId as string
                })

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.breadcrumbs[index].urlData = this.urlData
                this.sbiExecutionId = this.urlData?.sbiExecutionId as string
            }

            await this.sendForm(documentLabel)
        },
        async loadExporters() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData?.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
        },
        async sendForm(documentLabel: string | null = null) {
            let tempIndex = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label) as any

            const documentUrl = this.urlData?.url + '&timereloadurl=' + new Date().getTime()
            const postObject = {
                params: { document: null } as any,
                url: documentUrl.split('?')[0]
            }
            postObject.params.documentMode = this.documentMode
            this.hiddenFormUrl = postObject.url
            const paramsFromUrl = documentUrl?.split('?')[1]?.split('&')

            for (let i in paramsFromUrl) {
                if (typeof paramsFromUrl !== 'function') {
                    postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1]
                }
            }

            let postForm = document.getElementById('postForm_' + postObject.params.document) as any
            if (!postForm) {
                postForm = document.createElement('form')
            }
            postForm.id = 'postForm_' + postObject.params.document
            postForm.action = process.env.VUE_APP_HOST_URL + postObject.url
            postForm.method = 'post'
            postForm.target = tempIndex !== -1 ? 'documentFrame' + tempIndex : documentLabel
            postForm.acceptCharset = 'UTF-8'
            document.body.appendChild(postForm)

            this.hiddenFormData = new URLSearchParams()

            for (let k in postObject.params) {
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

            if (this.document.typeCode === 'DATAMART' || this.document.typeCode === 'DOSSIER' || this.document.typeCode === 'OLAP') {
                await this.sendHiddenFormData()
            } else {
                postForm.submit()
            }

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/export/cockpitData`, postData)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.exportSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        getFormattedParameters() {
            if (!this.filtersData || !this.filtersData.filterStatus) {
                return {}
            }

            let parameters = {} as any

            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                const parameter = this.filtersData.filterStatus[key]

                if (parameter.parameterValue) {
                    if (parameter.type === 'DATE') {
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value)
                        parameters[parameter.urlName + '_field_visible_description'] = this.getFormattedDate(parameter.parameterValue[0].value, true)
                    } else if (parameter.valueSelection === 'man_in') {
                        parameters[parameter.urlName] = parameter.type === 'NUM' ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.type === 'NUM' ? +parameter.parameterValue[0].description : parameter.parameterValue[0].description
                    } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                        parameters[parameter.urlName] = parameter.parameterValue.map((el: any) => el.value)
                        let tempString = ''
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            tempString += parameter.parameterValue[i].description
                            tempString += i === parameter.parameterValue.length - 1 ? '' : ';'
                        }
                        parameters[parameter.urlName + '_field_visible_description'] = tempString
                    } else {
                        parameters[parameter.urlName] = parameter.parameterValue[0].value
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].description
                    }
                }
            })

            return parameters
        },
        getFormattedParametersForCSVExport() {
            if (!this.filtersData) {
                return {}
            }

            let parameters = {} as any

            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                const parameter = this.filtersData.filterStatus[key]

                if (parameter.parameterValue) {
                    if (parameter.type === 'DATE') {
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value)
                    } else if (parameter.valueSelection === 'man_in' && !parameter.multivalue) {
                        parameters[parameter.urlName] = parameter.type === 'NUM' ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentrating/getvote`, { obj: this.document.id })
                .then((response: AxiosResponse<any>) => (this.documentRank = response.data))
                .catch((error: any) =>
                    this.$store.commit('setError', {
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
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentrating/vote`, { rating: newRank, obj: this.document.id })
                    .then(() =>
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.updateTitle'),
                            msg: this.$t('documentExecution.main.rankSaveSucces')
                        })
                    )
                    .catch((error: any) =>
                        this.$store.commit('setError', {
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecutionee/saveDocumentMetadata`, { id: this.document.id, jsonMeta: jsonMeta })
                .then(() => {
                    this.$store.commit('setInfo', {
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecutionmail/sendMail`, postData)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.sendMailSuccess')
                    })
                    this.mailDialogVisible = false
                })
                .catch((error: any) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                })
            this.loading = false
        },
        async onDeleteSchedulation(schedulation: any) {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentsnapshot/deleteSnapshot`, { SNAPSHOT: '' + schedulation.id })
                .then(async () => {
                    this.removeSchedulation(schedulation)
                    this.$store.commit('setInfo', {
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
            return luxonFormatDate(date, format, useDefaultFormat ? undefined : this.dateFormat)
        },
        onBreadcrumbClick(item: any) {
            this.document = item.document
            this.filtersData = item.filtersData
            this.urlData = item.urlData
            this.hiddenFormData = item.hiddenFormData
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
        async executeCrossNavigation(event: any) {
            this.angularData = event.data
            await this.loadCrossNavigationByDocument(event.data)
        },
        async loadCrossNavigationByDocument(angularData: any) {
            if (!this.document) return

            let temp = {} as any

            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document.label}/loadCrossNavigationByDocument`).then((response: AxiosResponse<any>) => (temp = response.data))
            this.loading = false

            const crossTarget = this.findCrossTargetByCrossName(angularData, temp)

            if (!crossTarget && temp.length > 1) {
                this.crossNavigationDocuments = temp
                this.destinationSelectDialogVisible = true
            } else {
                this.loadCrossNavigation(crossTarget ?? temp[0], angularData)
            }
        },
        findCrossTargetByCrossName(angularData: any, temp: any[]) {
            if (!angularData || !temp) return
            const index = temp.findIndex((el: any) => el.crossName === angularData.targetCrossNavigation.crossName)
            return index !== -1 ? temp[index] : null
        },
        async loadCrossNavigation(crossNavigationDocument: any, angularData: any) {
            this.formatAngularOutputParameters(angularData.otherOutputParameters)
            const navigationParams = this.formatNavigationParams(angularData.otherOutputParameters, crossNavigationDocument ? crossNavigationDocument.navigationParams : [])

            const popupOptions = crossNavigationDocument?.popupOptions ? JSON.parse(crossNavigationDocument.popupOptions) : null

            if (crossNavigationDocument.crossType !== 2) {
                this.document = {
                    ...crossNavigationDocument?.document,
                    navigationParams: navigationParams
                }
            }

            if (crossNavigationDocument.crossType === 2) {
                this.openCrossNavigationInNewWindow(popupOptions, crossNavigationDocument, navigationParams)
            } else if (crossNavigationDocument.crossType === 1) {
                const documentLabel = crossNavigationDocument?.document.label
                this.crossNavigationContainerData = {
                    documentLabel: documentLabel,
                    iFrameName: documentLabel
                }
                this.crossNavigationContainerVisible = true
                await this.loadPage(false, documentLabel)
            } else {
                const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
                if (index !== -1) {
                    this.breadcrumbs[index].document = this.document
                } else {
                    this.breadcrumbs.push({
                        label: this.document.label,
                        document: this.document,
                        crossBreadcrumb: crossNavigationDocument.crossBreadcrumb ?? this.document.name
                    })
                }

                await this.loadPage()
            }
        },
        openCrossNavigationInNewWindow(popupOptions: any, crossNavigationDocument: any, navigationParams: any) {
            if (!crossNavigationDocument || !crossNavigationDocument.document) return
            const parameters = encodeURI(JSON.stringify(navigationParams))
            const url =
                process.env.VUE_APP_HOST_URL +
                `/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=${crossNavigationDocument.document.id}&OBJECT_LABEL=${crossNavigationDocument.document.label}&SELECTED_ROLE=${this.sessionRole}&SBI_EXECUTION_ID=null&OBJECT_NAME=${crossNavigationDocument.document.name}&CROSS_PARAMETER=${parameters}`
            window.open(url, '_blank', `toolbar=0,status=0,menubar=0,width=${popupOptions.width || '800'},height=${popupOptions.height || '600'}`)
        },
        formatAngularOutputParameters(otherOutputParameters: any[]) {
            const startDocumentInputParameters = deepcopy(this.document.drivers)
            const keys = [] as any[]
            otherOutputParameters.forEach((parameter: any) => keys.push(Object.keys(parameter)[0]))

            for (let i = 0; i < startDocumentInputParameters.length; i++) {
                if (!keys.includes(startDocumentInputParameters[i].label)) {
                    const tempObject = {} as any
                    tempObject[startDocumentInputParameters[i].label] = this.getParameterValueForCrossNavigation(startDocumentInputParameters[i].label)
                    otherOutputParameters.push(tempObject)
                }
            }
        },
        getParameterValueForCrossNavigation(parameterLabel: string) {
            const index = this.filtersData.filterStatus?.findIndex((param: any) => param.label === parameterLabel)
            return index !== -1 ? this.filtersData.filterStatus[index].parameterValue[0].value : ''
        },
        formatNavigationParams(otherOutputParameters: any[], navigationParams: any) {
            let formatedParams = {} as any

            otherOutputParameters.forEach((el: any) => {
                let found = false
                let label = ''

                for (let i = 0; i < Object.keys(navigationParams).length; i++) {
                    if (navigationParams[Object.keys(navigationParams)[i]].value.label === Object.keys(el)[0]) {
                        found = true
                        label = Object.keys(navigationParams)[i]
                        break
                    }
                }

                if (found) {
                    formatedParams[label] = el[Object.keys(el)[0]]
                    formatedParams[label + '_field_visible_description'] = el[Object.keys(el)[0]]
                }
            })

            this.setNavigationParametersFromCurrentFilters(formatedParams, navigationParams)

            return formatedParams
        },
        setNavigationParametersFromCurrentFilters(formatedParams: any, navigationParams: any) {
            const navigationParamsKeys = navigationParams ? Object.keys(navigationParams) : []
            const formattedParameters = this.getFormattedParameters()
            const formattedParametersKeys = formattedParameters ? Object.keys(this.getFormattedParameters()) : []
            if (navigationParamsKeys.length > 0 && formattedParametersKeys.length > 0) {
                for (let i = 0; i < navigationParamsKeys.length; i++) {
                    const index = formattedParametersKeys.findIndex((key: string) => key === navigationParamsKeys[i])
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
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document.label}/loadCrossNavigationByDocument`).then((response: AxiosResponse<any>) => (temp = response.data))
            this.loading = false

            if (!temp || temp.length === 0) {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.$t('documentExecution.main.crossNavigationNoTargetError')
                })
                return
            }

            this.document = {
                ...temp[0].document,
                navigationParams: this.formatOLAPNavigationParams(crossNavigationParams, temp[0].navigationParams)
            }

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                this.breadcrumbs.push({
                    label: this.document.label,
                    document: this.document
                })
            }

            await this.loadPage()
            this.reloadTrigger = !this.reloadTrigger
        },
        formatOLAPNavigationParams(crossNavigationParams: any, navigationParams: any) {
            const crossNavigationParamKeys = Object.keys(crossNavigationParams)
            let formattedParams = {} as any

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
            return this.user.isSuperadmin || this.user.functionalities.includes('SaveIntoFolderFunctionality')
        },
        async addToWorkspace() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${this.document.id}`, {}, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: error.message === 'sbi.workspace.organizer.document.addtoorganizer.error.duplicateentry' ? this.$t('documentExecution.main.addToWorkspaceError') : error.message
                    })
                })
            this.loading = false
        },
        async loadUserConfig() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/user-configs`).then((response: AxiosResponse<any>) => {
                if (response.data) {
                    this.sessionEnabled = response.data['SPAGOBI.SESSION_PARAMETERS_MANAGER.enabled'] === 'false' ? false : true
                    this.dateFormat = response.data['SPAGOBI.DATE-FORMAT-SERVER.format'] === '%Y-%m-%d' ? 'dd/MM/yyyy' : response.data['SPAGOBI.DATE-FORMAT-SERVER.format']
                }
            })
        },
        saveParametersInSession() {
            const tempFilters = deepcopy(this.filtersData)
            tempFilters.filterStatus?.forEach((filter: any) => {
                delete filter.dataDependsOnParameters
                delete filter.dataDependentParameters
                delete filter.dependsOnParameters
                delete filter.dependentParameters
                delete filter.lovDependsOnParameters
                delete filter.lovDependentParameters
            })

            sessionStorage.setItem(this.document.label, JSON.stringify(tempFilters))
        },
        async onCrossNavigationSelected(event: any) {
            this.destinationSelectDialogVisible = false
            await this.loadCrossNavigation(event, this.angularData)
        },
        onCrossNavigationContainerClose() {
            this.crossNavigationContainerData = null
            this.crossNavigationContainerVisible = true
            this.onBreadcrumbClick(this.breadcrumbs[0])
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
</style>
