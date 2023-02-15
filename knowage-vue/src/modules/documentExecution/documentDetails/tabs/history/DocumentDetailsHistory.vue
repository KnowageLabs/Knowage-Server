<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.history.listTitle') }}
                </template>
                <template #end>
                    <Button :label="$t('common.add')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="setUploadType" />
                    <KnInputFile v-if="!uploading" label="" :change-function="startTemplateUpload" :trigger-input="triggerUpload" />
                </template>
            </Toolbar>
            <div id="drivers-list-container" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
                    <KnListBox
                        v-if="!loading"
                        class="kn-height-full"
                        :options="listOfTemplates"
                        :settings="historyDescriptor.knListSettings"
                        @click="selectTemplate($event.item)"
                        @setActive.stop="setActiveTemplate($event.item)"
                        @download.stop="downloadTemplate($event.item)"
                        @delete.stop="deleteTemplateConfirm($event.item)"
                    ></KnListBox>
                </div>
            </div>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :style="mainDescriptor.style.driverDetailsContainer">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.history.template') }}
                </template>
                <template #end>
                    <Button v-if="designerButtonVisible" :label="$t('documentExecution.olap.openDesigner')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="openDesignerConfirm" />
                </template>
            </Toolbar>
            <div id="driver-details-container" class="kn-flex kn-relative">
                <div id="codemirror-container" :style="mainDescriptor.style.absoluteScroll">
                    {{ scriptOptions.mode }}
                    <VCodeMirror v-if="showTemplateContent" ref="codeMirrorScriptType" v-model:value="selectedTemplateContent" class="kn-height-full" :options="scriptOptions" @keyup="$emit('touched')" />
                    <div v-else>
                        <InlineMessage severity="info" class="p-m-2 kn-width-full"> {{ $t('documentExecution.documentDetails.history.templateHint') }}</InlineMessage>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { iTemplate } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import VCodeMirror from 'codemirror-editor-vue3'
import { mapState } from 'pinia'
import { startOlap } from '../../dialogs/olapDesignerDialog/DocumentDetailOlapHelpers'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from '@/modules/documentExecution/documentDetails/tabs/drivers/DocumentDetailsDriversDescriptor.json'
import historyDescriptor from './DocumentDetailsHistory.json'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import InlineMessage from 'primevue/inlinemessage'
import mainStore from '../../../../../App.store'

import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'document-drivers',
    components: { KnListBox, KnInputFile, VCodeMirror, InlineMessage },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true } },
    computed: {
        showTemplateContent(): any {
            switch (this.selectedTemplateFileType) {
                case 'xml':
                    return true
                case 'xls':
                    return true
                case 'rptdesign':
                    return true
                case 'sbicockpit':
                    return true
                case 'json':
                    return true
                case 'sbigeoreport':
                    return true
                default:
                    return false
            }
        },
        designerButtonVisible(): boolean {
            return this.selectedDocument.typeCode == 'OLAP' || this.selectedDocument.typeCode == 'KPI' || this.selectedDocument.engine == 'knowagegisengine'
        },
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            historyDescriptor,
            selectedTemplate: {} as iTemplate,
            listOfTemplates: [] as iTemplate[],
            selectedTemplateFileType: null as any,
            selectedTemplateContent: '' as any,
            loading: false,
            triggerUpload: false,
            uploading: false,
            codeMirrorScriptType: {} as any,
            scriptOptions: {
                readOnly: true,
                mode: '',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            }
        }
    },
    created() {
        const interval = setInterval(() => {
            if (!this.$refs.codeMirrorScriptType) return
            this.codeMirrorScriptType = (this.$refs.codeMirrorScriptType as any).cminstance as any
            clearInterval(interval)
        }, 200)
        this.getAllTemplates()
    },
    methods: {
        async getAllTemplates() {
            this.loading = true
            this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates`)
                .then((response: AxiosResponse<any>) => (this.listOfTemplates = response.data as iTemplate[]))
                .finally(() => (this.loading = false))
        },
        async getSelectedTemplate(templateId) {
            this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/selected/${templateId}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
                this.selectedTemplateFileType == 'sbicockpit' || this.selectedTemplateFileType == 'json' || this.selectedTemplateFileType == 'sbigeoreport' ? (this.selectedTemplateContent = JSON.stringify(response.data, null, 4)) : (this.selectedTemplateContent = response.data)
            })
        },
        changeCodemirrorMode() {
            let mode = ''
            switch (this.selectedTemplateFileType) {
                case 'xml':
                    mode = 'text/html'
                    break
                case 'xls':
                    mode = 'text/html'
                    break
                case 'rptdesign':
                    mode = 'text/html'
                    break
                case 'sbicockpit':
                    mode = 'text/javascript'
                    break
                case 'json':
                    mode = 'text/javascript'
                    break
                case 'sbigeoreport':
                    mode = 'text/javascript'
            }
            setTimeout(() => {
                this.codeMirrorScriptType.setOption('mode', mode)
            }, 250)
        },
        setFileType(template) {
            if (template && template.name) {
                const fileType = template.name.split('.')
                this.selectedTemplateFileType = fileType[fileType.length - 1]
            }
        },
        selectTemplate(event) {
            this.selectedTemplate = event as iTemplate
            this.setFileType(event)
            this.changeCodemirrorMode()
            this.getSelectedTemplate(event.id)
        },
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        startTemplateUpload(event) {
            this.uploading = true
            this.uploadTemplate(event.target.files[0])
            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        async uploadTemplate(uploadedFile) {
            const formData = new FormData()
            formData.append('file', uploadedFile)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates`, formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                .then(async () => {
                    this.store.setInfo({ title: this.$t('common.toast.success'), msg: this.$t('common.toast.uploadSuccess') })
                    await this.getAllTemplates()
                    this.selectTemplate(this.listOfTemplates[0])
                })
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.history.uploadError') }))
                .finally(() => (this.triggerUpload = false))
        },
        setActiveTemplate(template) {
            this.$http
                .put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}`, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.store.setInfo({ title: this.$t('common.toast.success'), msg: this.$t('documentExecution.documentDetails.history.activeOk') })
                    this.getAllTemplates()
                })
                .catch(() => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.documentDetails.history.activeError') }))
        },
        async downloadTemplate(template) {
            const fileType = template.name.split('.')
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}/${fileType[fileType.length - 1]}/file`, {
                    headers: { Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9', 'X-Disable-Errors': 'true' }
                })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.errors) {
                        this.store.setError({ title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                    } else {
                        this.store.setInfo({ title: this.$t('common.toast.success') })
                        if (response.headers) {
                            const contentDisposition = response.headers['content-disposition']
                            const contentDispositionMatcher = contentDisposition.match(/filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/i)
                            if (contentDispositionMatcher && contentDispositionMatcher.length > 1) {
                                const fileAndExtension = contentDispositionMatcher[1]
                                const completeFileName = fileAndExtension.replaceAll('"', '')
                                if (fileType[fileType.length - 1] == 'json' || fileType[fileType.length - 1] == 'sbicockpit') {
                                    downloadDirect(JSON.stringify(response.data), completeFileName, 'text/html; charset=UTF-8')
                                } else {
                                    downloadDirect(response.data, completeFileName, 'text/html; charset=UTF-8')
                                }
                            }
                        }
                    }
                })
                .catch((error) => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: error.message }))
        },
        deleteTemplateConfirm(template) {
            this.$confirm.require({
                header: this.$t('common.toast.deleteConfirmTitle'),
                message: this.$t('common.toast.deleteMessage'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteTemplate(template)
            })
        },
        async deleteTemplate(template) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/templates/${template.id}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.store.setInfo({ title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                    this.getAllTemplates()
                })
                .catch((error) => this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: error.message }))
        },
        openDesignerConfirm() {
            this.$confirm.require({
                header: this.$t('common.toast.warning'),
                message: this.$t('documentExecution.olap.openDesignerMsg'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    switch (this.selectedDocument.typeCode) {
                        case 'KPI':
                            this.openKpiDocumentDesigner()
                            break
                        case 'MAP': {
                            this.openGis()
                            break
                        }
                        default:
                            this.openDesigner()
                    }
                }
            })
        },
        async openDesigner() {
            if (this.listOfTemplates.length === 0) {
                this.$emit('openDesignerDialog')
            } else {
                const activeTemplate = this.findActiveTemplate()
                const sbiExecutionId = cryptoRandomString({ length: 16, type: 'base64' })
                await startOlap(this.$http, this.user, sbiExecutionId, this.selectedDocument, activeTemplate, this.$router)
            }
        },
        findActiveTemplate() {
            let activeTemplate = null as any
            for (let i = 0; i < this.listOfTemplates.length; i++) {
                if (this.listOfTemplates[i].active) {
                    activeTemplate = this.listOfTemplates[i]
                    break
                }
            }
            return activeTemplate
        },
        openGis() {
            this.$router.push(`/gis/edit?documentId=${this.selectedDocument.id}`)
        },
        openKpiDocumentDesigner() {
            this.$router.push(`/kpi-edit/${this.selectedDocument.id}?from=documentDetail`)
        }
    }
})
</script>
<style lang="scss">
#codemirror-container {
    overflow: hidden !important;
}
</style>
