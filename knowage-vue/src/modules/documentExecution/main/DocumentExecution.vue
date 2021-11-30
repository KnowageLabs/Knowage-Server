<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
            <template #left>{{ document?.label }} </template>

            <template #right>
                <div class="p-d-flex p-jc-around">
                    <i v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'VIEW'" class="pi pi-pencil kn-cursor-pointer p-mx-4" v-tooltip.left="$t('documentExecution.main.editCockpit')" @click="editCockpitDocument"></i>
                    <i v-if="document?.typeCode === 'DOCUMENT_COMPOSITE' && documentMode === 'EDIT'" class="fa fa-eye kn-cursor-pointer p-mx-4" v-tooltip.left="$t('documentExecution.main.viewCockpit')" @click="editCockpitDocument"></i>
                    <i class="pi pi-book kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.onlineHelp')" @click="openHelp"></i>
                    <i class="pi pi-refresh kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.refresh')" @click="refresh"></i>
                    <i v-if="filtersData?.filterStatus.length > 0" class="fa fa-filter kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.parameters')" @click="parameterSidebarVisible = !parameterSidebarVisible"></i>
                    <i class="fa fa-ellipsis-v kn-cursor-pointer  p-mx-4" v-tooltip.left="$t('common.menu')" @click="toggle"></i>
                    <Menu ref="menu" :model="toolbarMenuItems" :popup="true" />
                    <i class="fa fa-times kn-cursor-pointer p-mx-4" v-tooltip.left="$t('common.close')" @click="closeDocument"></i>
                </div>
            </template>
        </Toolbar>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />

        <div ref="document-execution-view" id="document-execution-view" class="p-d-flex p-flex-row myDivToPrint">
            <div v-if="parameterSidebarVisible" id="document-execution-backdrop" @click="parameterSidebarVisible = false"></div>

            <Registry v-if="mode === 'registry' && filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible" :id="urlData.sbiExecutionId"></Registry>
            <Dossier v-else-if="mode === 'dossier' && filtersData && filtersData.isReadyForExecution && !loading && !schedulationsTableVisible" :id="document.id"></Dossier>

            <!-- <iframe v-else-if="mode === 'iframe'"  id="document-execution-iframe" :src="url"></iframe> -->

            <DocumentExecutionSchedulationsTable id="document-execution-schedulations-table" v-if="schedulationsTableVisible" :propSchedulations="schedulations" @deleteSchedulation="onDeleteSchedulation" @close="schedulationsTableVisible = false"></DocumentExecutionSchedulationsTable>

            <KnParameterSidebar class="document-execution-parameter-sidebar kn-overflow-y" v-if="parameterSidebarVisible" :filtersData="filtersData" :propDocument="document" @execute="onExecute" @exportCSV="onExportCSV"></KnParameterSidebar>

            <DocumentExecutionHelpDialog :visible="helpDialogVisible" :propDocument="document" @close="helpDialogVisible = false"></DocumentExecutionHelpDialog>
            <DocumentExecutionRankDialog :visible="rankDialogVisible" :propDocumentRank="documentRank" @close="rankDialogVisible = false" @saveRank="onSaveRank"></DocumentExecutionRankDialog>
            <DocumentExecutionNotesDialog :visible="notesDialogVisible" :propDocument="document" @close="notesDialogVisible = false"></DocumentExecutionNotesDialog>
            <DocumentExecutionMetadataDialog :visible="metadataDialogVisible" :propDocument="document" :propMetadata="metadata" :propLoading="loading" @close="metadataDialogVisible = false" @saveMetadata="onMetadataSave"></DocumentExecutionMetadataDialog>
            <DocumentExecutionMailDialog :visible="mailDialogVisible" @close="mailDialogVisible = false" @sendMail="onMailSave"></DocumentExecutionMailDialog>
            <DocumentExecutionLinkDialog :visible="linkDialogVisible" :linkInfo="linkInfo" :embedHTML="embedHTML" @close="linkDialogVisible = false"></DocumentExecutionLinkDialog>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
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
    components: { DocumentExecutionHelpDialog, DocumentExecutionRankDialog, DocumentExecutionNotesDialog, DocumentExecutionMetadataDialog, DocumentExecutionMailDialog, DocumentExecutionSchedulationsTable, DocumentExecutionLinkDialog, KnParameterSidebar, Menu, Registry, Dossier },
    props: { id: { type: String } },
    emits: ['close'],
    data() {
        return {
            document: null as any,
            hiddenFormData: {} as any,
            hiddenFormUrl: '' as string,
            documentMode: 'VIEW',
            filtersData: null as any,
            urlData: null as any,
            exporters: null as any,
            mode: null as any,
            parameterSidebarVisible: false,
            toolbarMenuItems: [] as any[],
            helpDialogVisible: false,
            documentRank: null as any,
            rankDialogVisible: false,
            notesDialogVisible: false,
            metadataDialogVisible: false,
            mailDialogVisible: false,
            metadata: null as any,
            schedulationsTableVisible: false,
            schedulations: [] as any[],
            linkDialogVisible: false,
            linkInfo: null as any,
            sbiExecutionId: null as string | null,
            embedHTML: false,
            user: null as any,
            loading: false,
            iframe: {} as any
        }
    },
    computed: {
        sessionRole(): string {
            return this.user.sessionRole !== 'No default role selected' ? this.user.sessionRole : null
        },
        url() {
            return (
                process.env.VUE_APP_HOST_URL +
                '/knowage/restful-services/publish?PUBLISHER=documentExecutionNg&OBJECT_ID=3306&OBJECT_LABEL=DOC_DEFAULT_2&MENU_PARAMETERS=%7B%7D&LIGHT_NAVIGATOR_DISABLED=TRUE&SBI_EXECUTION_ID=null&OBJECT_NAME=DOC_DEFAULT_2&EDIT_MODE=null&TOOLBAR_VISIBLE=null&CAN_RESET_PARAMETERS=null&EXEC_FROM=null&CROSS_PARAMETER=null'
            )
        }
    },
    async created() {
        //console.log('CURRENT ROUTE: ', this.$route)
        this.user = (this.$store.state as any).user

        this.setMode()

        // console.log('MODE: ', this.mode)

        // console.log('ID: ', this.id)

        console.log('LOADED USER: ', this.user)

        if (this.user.sessionRole !== 'No default role selected') {
            await this.loadPage()
        }
    },
    methods: {
        editCockpitDocument() {
            console.log('TODO - EDIT COCKPIT DOCUMENT')
            this.documentMode = this.documentMode === 'EDIT' ? 'VIEW' : 'EDIT'
            this.hiddenFormData.documentMode = this.documentMode
            console.log('TEST', this.hiddenFormData)
            // this.sendHiddenFormData()
        },
        openHelp() {
            this.helpDialogVisible = true
        },
        async refresh() {
            this.parameterSidebarVisible = false
            await this.loadPage()
        },
        toggle(event: any) {
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
                    items: [{ icon: 'pi pi-download', label: this.$t('common.export'), command: () => this.export() }]
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

            this.exporters?.forEach((exporter: any) => this.toolbarMenuItems[1].items.push({ icon: 'fa fa-file-excel', label: exporter.name, command: () => this.export() }))

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
        export() {
            console.log('TODO - EXPORT')
            const url =
                process.env.VUE_APP_HOST_URL +
                `/knowageqbeengine/servlet/AdapterHTTP?&documentName=Registry_Test_1&SBI_EXECUTION_ROLE=%2Fdemo%2Fadmin&SBI_COUNTRY=US&document=3251&SBI_LANGUAGE=en&SBI_SCRIPT=&dateformat=dd%2FMM%2Fyyyy&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&user_id=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZGVtb19hZG1pbiIsImV4cCI6MTYzNzk1MDM4N30.v0om2xcCIWR7lf2_28pKa_zfgjuSYQB-aU8WRVWgpuo&SBI_EXECUTION_ID=190c98b14eac11eca5075bc8d2018299&isFromCross=false&SBI_ENVIRONMENT=DOCBROWSER&outputType=application%2Fvnd.ms-excel&ACTION_NAME=EXPORT_RESULT_ACTION&MIME_TYPE=application%2Fvnd.ms-excel&RESPONSE_TYPE=RESPONSE_TYPE_ATTACHMENT`
            window.open(url, '_blank')

            // http://localhost:8080/knowageqbeengine/servlet/AdapterHTTP?&documentName=Registry_Test_1&SBI_EXECUTION_ROLE=%2Fdemo%2Fadmin&SBI_COUNTRY=US&document=3251&SBI_LANGUAGE=en&SBI_SCRIPT=&dateformat=dd%2FMM%2Fyyyy&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&user_id=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZGVtb19hZG1pbiIsImV4cCI6MTYzNzk1MDM4N30.v0om2xcCIWR7lf2_28pKa_zfgjuSYQB-aU8WRVWgpuo&SBI_EXECUTION_ID=190c98b14eac11eca5075bc8d2018299&isFromCross=false&SBI_ENVIRONMENT=DOCBROWSER&outputType=application%2Fvnd.ms-excel&ACTION_NAME=EXPORT_RESULT_ACTION&MIME_TYPE=application%2Fvnd.ms-excel&RESPONSE_TYPE=RESPONSE_TYPE_ATTACHMENT
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
            this.$router.push('/document-browser')
            this.$emit('close')
        },
        setMode() {
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
            await this.loadDocument()
            await this.loadFilters()
            if (this.filtersData?.isReadyForExecution) {
                await this.loadURL()
                await this.loadExporters()
            } else {
                this.parameterSidebarVisible = true
            }

            this.loading = false

            // TODO LOAD URL HARDCODED
            // await this.loadURL()
        },
        async loadDocument() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.id}`).then((response: AxiosResponse<any>) => (this.document = response.data))
            console.log('LOADED DOCUMENT: ', this.document)
        },
        async loadFilters() {
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentexecution/filters`, { label: this.id, role: this.sessionRole, parameters: {} })
                .then((response: AxiosResponse<any>) => (this.filtersData = response.data))
                .catch((error: any) => console.log('ERROR: ', error))
            console.log('LOADED FILTERS DATA: ', this.filtersData)
        },
        async loadURL() {
            const postData = { label: this.id, role: this.sessionRole, parameters: this.getFormattedParameters(), EDIT_MODE: 'null', IS_FOR_EXPORT: true } as any

            if (this.sbiExecutionId) {
                postData.SBI_EXECUTION_ID = this.sbiExecutionId
            }

            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, postData).then((response: AxiosResponse<any>) => {
                this.urlData = response.data
                this.sbiExecutionId = this.urlData.sbiExecutionId
            })
            await this.sendForm()
        },
        async loadExporters() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
            // console.log('LOADED EXPORTERS: ', this.exporters)
        },
        async sendForm() {
            const documentUrl = this.urlData.url + '&timereloadurl=' + new Date().getTime()
            const postObject = { params: { document: null }, url: documentUrl.split('?')[0] }
            this.hiddenFormUrl = postObject.url
            const paramsFromUrl = documentUrl.split('?')[1].split('&')

            for (let i in paramsFromUrl) {
                if (typeof paramsFromUrl !== 'function') {
                    postObject.params[paramsFromUrl[i].split('=')[0]] = paramsFromUrl[i].split('=')[1]
                }
            }

            let postForm = null as any
            //if (!postForm) {
            postForm = document.createElement('form')
            postForm.id = 'postForm_' + postObject.params.document
            postForm.action = postObject.url
            postForm.method = 'post'
            postForm.target = 'documentFrame'
            // document.body.appendChild(postForm)
            // }

            this.hiddenFormData = new URLSearchParams()

            for (let k in postObject.params) {
                const inputElement = document.getElementById('postForm_' + k) as any
                if (inputElement) {
                    inputElement.value = decodeURIComponent(postObject.params[k])
                    inputElement.value = inputElement.value.replace(/\+/g, ' ')
                } else {
                    const element = document.createElement('input')
                    element.type = 'hidden'
                    element.id = 'postForm_' + k
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

            // TODO: hardkodovano
            this.hiddenFormData.append('documentMode', 'VIEW')

            await this.sendHiddenFormData()
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
                    // this.iframe = response.data
                    // const iFrameDiv = document.getElementById('document-execution-iframe') as any
                    // console.log('CONTENT WINDOW: ', iFrameDiv.contentWindow)
                    // console.log(' >>> iFrameDiv', iFrameDiv)

                    // if (iFrameDiv?.contentWindow) {
                    //     iFrameDiv.contentWindow.open()

                    //     iFrameDiv.contentWindow.document.firstChild.innerHTML = this.iframe
                    //     iFrameDiv.contentWindow.write(this.iframe)
                    //     iFrameDiv.contentWindow.close()
                    // }
                })
                .catch((error: any) => console.log('ERROR: ', error))
        },
        async onExecute() {
            console.log('EXECUTE PARAMS: ', this.filtersData)
            this.loading = true
            this.filtersData.isReadyForExecution = true
            await this.loadURL()
            this.parameterSidebarVisible = false
            this.loading = false
        },
        async onExportCSV() {
            console.log('ON EXPORT CSV CLICKED!', this.document)
            console.log('ON EXPORT CSV CLICKED!', this.filtersData)
            const postData = { documentId: this.document.id, documentLabel: this.document.label, exportType: 'CSV', parameters: this.getFormattedParameters() }
            // Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
            //     console.log('EL: ', this.filtersData.filterStatus[key])
            //     const param = this.filtersData.filterStatus[key]
            //     if (param.multivalue) {
            //         let tempString = ''
            //         for (let i = 0; i < this.filtersData.filterStatus[key].parameterValue.length; i++) {
            //             tempString += this.filtersData.filterStatus[key].parameterValue[i].value
            //             tempString += i === this.filtersData.filterStatus[key].parameterValue.length - 1 ? '' : ','
            //         }

            //         postData.parameters[this.filtersData.filterStatus[key].urlName] = tempString
            //     } else if (param.type === 'NUM' && !param.selectionType) {
            //         postData.parameters[this.filtersData.filterStatus[key].urlName] = +this.filtersData.filterStatus[key].parameterValue[0].value
            //     } else {
            //         postData.parameters[this.filtersData.filterStatus[key].urlName] = this.filtersData.filterStatus[key].parameterValue[0] ? this.filtersData.filterStatus[key].parameterValue[0].value : this.filtersData.filterStatus[key].parameterValue.value
            //     }
            // })
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
            console.log('BLA', postData)
        },
        getFormattedParameters() {
            if (!this.filtersData) {
                return {}
            }

            let parameters = {} as any

            Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
                const parameter = this.filtersData.filterStatus[key]

                console.log('PARAMETER: ', parameter)

                if (parameter.parameterValue) {
                    if (parameter.type === 'DATE') {
                        parameters[parameter.urlName] = parameter.parameterValue[0].value
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].value
                    } else if (parameter.valueSelection === 'man_in' && !parameter.multivalue) {
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
                        parameters[parameter.urlName] = parameter.parameterValue[0] ? parameter.parameterValue[0].value : parameter.parameterValue.value
                        parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0] ? parameter.parameterValue[0].description : parameter.parameterValue.description
                    }
                }
            })

            return parameters

            // const parameters = {} as any
            // Object.keys(this.filtersData.filterStatus).forEach((key: any) => {
            //     console.log('EL: ', this.filtersData.filterStatus[key])
            //     const param = this.filtersData.filterStatus[key]

            //     if (this.filtersData.filterStatus[key].parameterValue) {
            //         if (param.multivalue) {
            //             let tempString = ''
            //             if (this.filtersData.filterStatus[key].parameterValue) {
            //                 for (let i = 0; i < this.filtersData.filterStatus[key].parameterValue.length; i++) {
            //                     tempString += this.filtersData.filterStatus[key].parameterValue[i].value
            //                     tempString += i === this.filtersData.filterStatus[key].parameterValue.length - 1 ? '' : ','
            //                 }
            //             }

            //             parameters[this.filtersData.filterStatus[key].urlName] = tempString
            //         } else if (param.type === 'NUM' && !param.selectionType) {
            //             parameters[this.filtersData.filterStatus[key].urlName] = this.filtersData.filterStatus[key].parameterValue ? +this.filtersData.filterStatus[key].parameterValue[0].value : ''
            //         } else {
            //             parameters[this.filtersData.filterStatus[key].urlName] = this.filtersData.filterStatus[key].parameterValue ? this.filtersData.filterStatus[key].parameterValue[0].value : ''
            //         }
            //     }
            // })

            // return parameters
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
            console.log('LOADED DOCUMENT VOTE MAIN: ', this.documentRank)
        },
        async onSaveRank(newRank: any) {
            console.log('NEW RANK: ', newRank)
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
            console.log('ON METADATA SAVE: ', metadata)
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
                    console.log('ERROR: ', error)
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                })
            this.loading = false
        },
        async onMailSave(mail: any) {
            console.log('MAIL FOR SAVE: ', mail)
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
            // console.log('SCHEDULATION FOR DELETE: ', schedulation)
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
        removeSchedulation(schedulation: any) {
            // console.log('SCHEDULATION FOR REMOVE: ', schedulation)
            const index = this.schedulations.findIndex((el: any) => el.id === schedulation.id)
            if (index !== -1) this.schedulations.splice(index, 1)
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
        padding: 15px;
        font-size: 14px;
        line-height: 18px;
    }
}
</style>
