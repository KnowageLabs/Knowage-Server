<template>
    <Toolbar v-if="!embed" class="kn-toolbar kn-toolbar--primary p-col-12">
        <template #left>
            <span>{{ document?.label }}</span>
        </template>

        <template #right>
            <div class="p-d-flex p-jc-around">
                <i v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'VIEW'" class="pi pi-pencil kn-cursor-pointer p-mx-4" v-tooltip.left="$t('documentExecution.main.editCockpit')" @click="editCockpitDocument"></i>
                <i v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'EDIT'" class="fa fa-eye kn-cursor-pointer p-mx-4" v-tooltip.left="$t('documentExecution.main.viewCockpit')" @click="editCockpitDocument"></i>
                <i class="pi pi-book kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.onlineHelp')" @click="openHelp"></i>
                <i class="pi pi-refresh kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.refresh')" @click="refresh"></i>
                <i v-if="filtersData?.filterStatus?.length > 0 || !sessionRole" class="fa fa-filter kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.parameters')" @click="parameterSidebarVisible = !parameterSidebarVisible" data-test="parameter-sidebar-icon"></i>
                <i class="fa fa-ellipsis-v kn-cursor-pointer  p-mx-4" v-tooltip.left="$t('common.menu')" @click="toggle"></i>
                <Menu ref="menu" :model="toolbarMenuItems" :popup="true" />
                <i class="fa fa-times kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.close')" @click="closeDocument"></i>
            </div>
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
    <DocumentExecutionBreadcrumb v-if="breadcrumbs.length > 1" :breadcrumbs="breadcrumbs" @breadcrumbClicked="onBreadcrumbClick"></DocumentExecutionBreadcrumb>

    <div ref="document-execution-view" id="document-execution-view" class="p-d-flex p-flex-row myDivToPrint">
        <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

        <template v-if="filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible">
            <Registry v-if="mode === 'registry'" :id="urlData.sbiExecutionId" :reloadTrigger="reloadTrigger"></Registry>
            <Dossier v-else-if="mode === 'dossier'" :id="document.id" :reloadTrigger="reloadTrigger"></Dossier>
        </template>

        <!-- <iframe id="documentFrame" name="documentFrame" v-else-if="mode === 'iframe'" class="document-execution-iframe" :src="url"></iframe> -->
        <iframe id="documentFrame" name="documentFrame" v-show="mode === 'iframe' && filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible" class="document-execution-iframe"></iframe>

        <DocumentExecutionSchedulationsTable id="document-execution-schedulations-table" v-if="schedulationsTableVisible" :propSchedulations="schedulations" @deleteSchedulation="onDeleteSchedulation" @close="schedulationsTableVisible = false"></DocumentExecutionSchedulationsTable>

        <KnParameterSidebar
            class="document-execution-parameter-sidebar kn-overflow-y"
            v-if="parameterSidebarVisible"
            :filtersData="filtersData"
            :propDocument="document"
            :userRole="userRole"
            @execute="onExecute"
            @exportCSV="onExportCSV"
            @roleChanged="onRoleChange"
            data-test="parameter-sidebar"
        ></KnParameterSidebar>

        <DocumentExecutionHelpDialog :visible="helpDialogVisible" :propDocument="document" @close="helpDialogVisible = false"></DocumentExecutionHelpDialog>
        <DocumentExecutionRankDialog :visible="rankDialogVisible" :propDocumentRank="documentRank" @close="rankDialogVisible = false" @saveRank="onSaveRank"></DocumentExecutionRankDialog>
        <DocumentExecutionNotesDialog :visible="notesDialogVisible" :propDocument="document" @close="notesDialogVisible = false"></DocumentExecutionNotesDialog>
        <DocumentExecutionMetadataDialog :visible="metadataDialogVisible" :propDocument="document" :propMetadata="metadata" :propLoading="loading" @close="metadataDialogVisible = false" @saveMetadata="onMetadataSave"></DocumentExecutionMetadataDialog>
        <DocumentExecutionMailDialog :visible="mailDialogVisible" @close="mailDialogVisible = false" @sendMail="onMailSave"></DocumentExecutionMailDialog>
        <DocumentExecutionLinkDialog :visible="linkDialogVisible" :linkInfo="linkInfo" :embedHTML="embedHTML" @close="linkDialogVisible = false"></DocumentExecutionLinkDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { formatDate } from '@/helpers/commons/localeHelper'
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
import Menu from 'primevue/menu'
import Registry from '../registry/Registry.vue'
import Dossier from '../dossier/Dossier.vue'

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
        Menu,
        Registry,
        Dossier
    },
    props: { id: { type: String } },
    emits: ['close'],
    data() {
        return {
            document: null as any,
            hiddenFormData: {} as any,
            hiddenFormUrl: '' as string,
            documentMode: 'VIEW',
            filtersData: {} as { filterStatus: iParameter[]; isReadyForExecution: boolean },
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
            linkInfo: null as { isPublic: boolean; noPublicRoleError: boolean } | null,
            sbiExecutionId: null as string | null,
            embedHTML: false,
            user: null as any,
            reloadTrigger: false,
            breadcrumbs: [] as any[],
            embed: false,
            userRole: null,
            loading: false
        }
    },
    computed: {
        sessionRole(): string {
            return this.user.sessionRole !== 'No default role selected' ? this.user.sessionRole : null
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
        }
    },
    async created() {
        window.addEventListener('message', (event) => {
            // console.log('EVENT: ', event)
            if (event.data.type === 'crossNavigation') {
                this.executeCrossNavigation(event)
            }
        })

        // console.log('DOCUMENT EXECUTION CREATED!!!!!!!!!1')

        //console.log('CURRENT ROUTE: ', this.$route)
        this.user = (this.$store.state as any).user
        this.userRole = this.user.sessionRole !== 'No default role selected' ? this.user.sessionRole : null

        this.setMode()

        // console.log('MODE: ', this.mode)

        // console.log('ID: ', this.id)

        console.log('LOADED USER: ', this.user)

        this.document = { label: this.id }

        await this.loadDocument()

        if (this.userRole) {
            await this.loadPage()
        } else {
            this.parameterSidebarVisible = true
        }
    },
    methods: {
        editCockpitDocument() {
            console.log('TODO - EDIT COCKPIT DOCUMENT')
            this.documentMode = this.documentMode === 'EDIT' ? 'VIEW' : 'EDIT'
            this.hiddenFormData.set('documentMode', this.documentMode)
            console.log('TEST', this.hiddenFormData)
            // window.frames[0].postMessage({ type: 'changeMode', mode: this.documentMode }, '*')
            this.loadURL()
        },
        openHelp() {
            this.helpDialogVisible = true
        },
        async refresh() {
            this.parameterSidebarVisible = false
            await this.loadURL()
            this.reloadTrigger = !this.reloadTrigger
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.toolbarMenuItems = []
            this.toolbarMenuItems.push(
                {
                    label: this.$t('common.file'),
                    items: [{ icon: 'pi pi-print', label: this.$t('common.print'), command: () => this.print() }]
                },
                {
                    label: this.$t('common.export'),
                    items: []
                },
                {
                    label: this.$t('common.info.info'),
                    items: [{ icon: 'pi pi-star', label: this.$t('common.rank'), command: () => this.openRank() }]
                },
                {
                    label: this.$t('common.shortcuts'),
                    items: []
                }
            )

            this.exporters?.forEach((exporter: any) => this.toolbarMenuItems[1].items.push({ icon: 'fa fa-file-excel', label: exporter.name, command: () => this.export(exporter.name) }))

            if (this.user.functionalities.includes('SendMailFunctionality') && this.document.typeCode === 'REPORT') {
                this.toolbarMenuItems[1].items.push({ icon: 'pi pi-envelope', label: this.$t('common.sendByEmail'), command: () => this.openMailDialog() })
            }

            if (this.user.functionalities.includes('SeeMetadataFunctionality')) {
                this.toolbarMenuItems[2].items.unshift({ icon: 'pi pi-info-circle', label: this.$t('common.metadata'), command: () => this.openMetadata() })
            }

            if (this.user.functionalities.includes('SeeNotesFunctionality')) {
                this.toolbarMenuItems[2].items.push({ icon: 'pi pi-file', label: this.$t('common.notes'), command: () => this.openNotes() })
            }

            if (this.user.functionalities.includes('SeeSnapshotsFunctionality')) {
                this.toolbarMenuItems[3].items.unshift({ icon: '', label: this.$t('documentExecution.main.showScheduledExecutions'), command: () => this.showScheduledExecutions() })
            }

            if (this.user.functionalities.includes('EnableToCopyAndEmbed')) {
                this.toolbarMenuItems[3].items.push({ icon: 'fa fa-share', label: this.$t('documentExecution.main.copyLink'), command: () => this.copyLink(false) })
                this.toolbarMenuItems[3].items.push({ icon: 'fa fa-share', label: this.$t('documentExecution.main.embedInHtml'), command: () => this.copyLink(true) })
            }
        },
        print() {
            window.print()
        },
        export(type: string) {
            window.frames[0].frames[0].frames.postMessage({ type: 'export', format: type.toLowerCase() }, '*')
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
            if (this.document.typeCode === 'DATAMART' || this.document.typeCode === 'DOSSIER') {
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/canHavePublicExecutionUrl`, { label: this.document.label })
                    .then((response: AxiosResponse<any>) => {
                        this.embedHTML = embedHTML
                        this.linkInfo = response.data
                        this.linkDialogVisible = true
                    })
                    .catch(() => {})
            } else {
                window.frames[0].postMessage({ type: 'htmlLink', embedHTML: embedHTML }, '*')
            }
            this.loading = false
        },
        closeDocument() {
            const link = this.$route.path.includes('workspace') ? '/workspace' : '/document-browser'
            this.$router.push(link)
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
            } else {
                this.mode = 'iframe'
            }
        },
        async loadPage() {
            this.loading = true

            await this.loadFilters()
            if (this.filtersData?.isReadyForExecution) {
                await this.loadURL()
                await this.loadExporters()
            } else if (this.filtersData?.filterStatus) {
                this.parameterSidebarVisible = true
            }

            this.loading = false
        },
        async loadDocument() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.document?.label}`).then((response: AxiosResponse<any>) => (this.document = response.data))

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)

            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                this.breadcrumbs.push({ label: this.document.label, document: this.document })
            }
            // console.log('LOADED DOCUMENT: ', this.document)
            // console.log('BREADCRUMBS AFTER LOADED DOCUMENT: ', this.breadcrumbs)
        },
        async loadFilters() {
            console.log(' >>>>>>>>>>>>>>>>>>>> LOADING FILTERS FOR DOCUMENT: ', this.document)
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentexecution/filters`, { label: this.id, role: this.userRole, parameters: this.document.navigationParams ?? {} })
                .then((response: AxiosResponse<any>) => (this.filtersData = response.data))
                .catch((error: any) => {
                    if (error.response.status === 500) {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: this.$t('documentExecution.main.userRoleError')
                        })
                    }
                })

            this.filtersData?.filterStatus?.forEach((el: iParameter) => {
                el.parameterValue = el.multivalue ? [] : [{ value: '', description: '' }]
                if (el.driverDefaultValue?.length > 0) {
                    el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                        return { value: defaultValue.value ?? defaultValue._col0, description: defaultValue.desc ?? defaultValue._col1 }
                    })
                }

                if (el.data) {
                    el.data = el.data.map((data: any) => {
                        return { value: data._col0, description: data._col1 }
                    })
                }

                if ((el.selectionType === 'COMBOBOX' || el.selectionType === 'LIST') && el.multivalue && el.mandatory && el.data.length === 1) {
                    el.showOnPanel = 'false'
                }
            })

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) this.breadcrumbs[index].filtersData = this.filtersData
            console.log('LOADED FILTERS DATA: ', this.filtersData)
            // console.log('BREADCRUMBS AFTER LOADED FILTERS DATA: ', this.breadcrumbs)
        },
        async loadURL() {
            console.log('LOADING URL FROM VUE APP!')

            const postData = { label: this.id, role: this.userRole, parameters: this.getFormattedParameters(), EDIT_MODE: 'null', IS_FOR_EXPORT: true } as any

            if (this.sbiExecutionId) {
                postData.SBI_EXECUTION_ID = this.sbiExecutionId
            }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.urlData = response.data
                    this.sbiExecutionId = this.urlData?.sbiExecutionId as string
                })
                .catch((error: string) => {
                    console.log('ERROR: ', error)
                })

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.breadcrumbs[index].urlData = this.urlData
                this.sbiExecutionId = this.urlData?.sbiExecutionId as string
            }
            console.log('LOADED URL DATA: ', this.urlData)
            // console.log('BREADCRUMBS AFTER LOADED URL DATA: ', this.breadcrumbs)
            await this.sendForm()
        },
        async loadExporters() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData?.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
            // console.log('LOADED EXPORTERS: ', this.exporters)
        },
        async sendForm() {
            const documentUrl = this.urlData?.url + '&timereloadurl=' + new Date().getTime()
            const postObject = { params: { document: null }, url: documentUrl.split('?')[0] }
            this.hiddenFormUrl = postObject.url
            const paramsFromUrl = documentUrl.split('?')[1].split('&')

            for (let i in paramsFromUrl) {
                if (typeof paramsFromUrl !== 'function') {
                    postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1]
                }
            }

            let postForm = document.getElementById('postForm_' + postObject.params.document) as any
            if (!postForm) {
                postForm = document.createElement('form')
                postForm.id = 'postForm_' + postObject.params.document
                postForm.action = 'http://localhost:8080' + postObject.url
                postForm.method = 'post'
                postForm.target = 'documentFrame'
                document.body.appendChild(postForm)
            }

            this.hiddenFormData = new URLSearchParams()

            for (let k in postObject.params) {
                // console.log('>>>>> K: ', k)
                const inputElement = document.getElementById('postForm_' + postObject.params.document + k) as any
                if (inputElement) {
                    //  console.log('>>>>> K FOUND: ', k)
                    inputElement.value = decodeURIComponent(postObject.params[k])
                    inputElement.value = inputElement.value.replace(/\+/g, ' ')
                    this.hiddenFormData.set(k, decodeURIComponent(postObject.params[k]).replace(/\+/g, ' '))
                } else {
                    //  console.log('>>>>> K NEW: ', k)

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
            // encodeURIComponent
            for (let i = postForm.elements.length - 1; i >= 0; i--) {
                const postFormElement = postForm.elements[i].id.replace('postForm_', '')

                if (!(postFormElement in postObject.params)) {
                    this.hiddenFormData.delete(postFormElement)
                }
            }

            this.hiddenFormData.append('documentMode', this.documentMode)

            console.log('SENDING FORM FROM VUE!!!!!!!!!!!!!!!!!!!')

            if (this.document.typeCode === 'DATAMART' || this.document.typeCode === 'DOSSIER') {
                await this.sendHiddenFormData()
            } else {
                postForm.submit()
            }

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) this.breadcrumbs[index].hiddenFormData = this.hiddenFormData

            // console.log('BREADCRUMBS AFTER HIDDEN FORM DATA: ', this.breadcrumbs)
        },
        async sendHiddenFormData() {
            await this.$http
                .post(this.hiddenFormUrl, this.hiddenFormData, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                    }
                })
                .then((response) => {
                    console.log('HIDDEN FORM RESPONSE: ', response)
                })
                .catch((error: any) => console.log('ERROR: ', error))
        },
        async onExecute() {
            this.loading = true
            this.filtersData.isReadyForExecution = true
            await this.loadURL()
            this.parameterSidebarVisible = false
            this.reloadTrigger = !this.reloadTrigger
            this.loading = false
        },
        async onExportCSV() {
            const postData = { documentId: this.document.id, documentLabel: this.document.label, exportType: 'CSV', parameters: this.getFormattedParametersForCSVExport() }
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
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value, 'MM/DD/YYYY')
                        parameters[parameter.urlName + '_field_visible_description'] = this.getFormattedDate(parameter.parameterValue[0].value, 'MM/DD/YYYY')
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
                        parameters[parameter.urlName] = this.getFormattedDate(parameter.parameterValue[0].value, 'MM/DD/YYYY')
                    } else if (parameter.valueSelection === 'man_in' && !parameter.multivalue) {
                        parameters[parameter.urlName] = parameter.type === 'NUM' ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                    } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                        let tempString = ''
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            tempString += parameter.parameterValue[i].value
                            tempString += i === parameter.parameterValue.length - 1 ? '' : ','
                        }
                        parameters[parameter.urlName] = tempString
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
            const properties = ['shortText', 'longText']
            properties.forEach((property: string) =>
                metadata[property].forEach((el: any) => {
                    if (el.value) {
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
                .catch((error: any) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                })
            this.loading = false
        },
        async onMailSave(mail: any) {
            this.loading = true
            const postData = { ...mail, label: this.document.label, docId: this.document.id, userId: this.user.userId, parameters: this.getFormattedParameters() }
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
                    console.log('ERROR: ', error)
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
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        },
        onBreadcrumbClick(item: any) {
            this.document = item.document
            this.filtersData = item.filtersData
            this.urlData = item.urlData
            this.hiddenFormData = item.hiddenFormData
        },
        async onRoleChange(role: string) {
            this.userRole = role as any
            this.filtersData = {} as { filterStatus: iParameter[]; isReadyForExecution: boolean }
            this.urlData = null
            this.exporters = null
            await this.loadPage()
        },
        async executeCrossNavigation(event: any) {
            console.log('EVENT DATA: ', event.data)

            await this.loadCrossNavigationByDocument()
        },
        async loadCrossNavigationByDocument() {
            let temp = {} as any

            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/crossNavigation/${this.document.label}/loadCrossNavigationByDocument`).then((response: AxiosResponse<any>) => (temp = response.data))
            this.loading = false

            console.log('DATA FROM ANGULAR: ', temp)

            this.document = { ...temp[0].document, navigationParams: temp[0].navigationParams }
            console.log('NEW DOCUMENT: ', this.document)

            const index = this.breadcrumbs.findIndex((el: any) => el.label === this.document.label)
            // console.log('INDEX: ', index)
            if (index !== -1) {
                this.breadcrumbs[index].document = this.document
            } else {
                // console.log('CAAAAAAAAAAAAAAALED FOR ', this.document.label)
                this.breadcrumbs.push({ label: this.document.label, document: this.document })
            }

            await this.loadPage()
        }
    }
})
</script>

<style lang="scss">
#document-execution-view {
    position: relative;
    height: 100vh;
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
}

@media print {
    body * {
        visibility: hidden;
    }
    #document-execution-view,
    #document-execution-view * {
        visibility: visible;
    }

    .document-execution-parameter-sidebar {
        visibility: hidden;
    }

    #document-execution-view {
        position: absolute;
        left: 0;
        top: 0;
        background-color: white;
        height: 100%;
        width: 100%;
        position: fixed;
        top: 0;
        left: 0;
        margin: 0;
    }
}
</style>
