<template>
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" :style="dataViewDescriptor.style.dialog" v-bind:visible="visible" footer="footer" :header="$t('workspace.myData.wizardTitle')" :closable="false" modal>
        <span v-if="wizardStep === 1">
            <StepOne :selectedDataset="dataset" @fileUploaded="onFileUpload" />
        </span>
        <span v-if="wizardStep === 2">
            <StepTwo :selectedDataset="dataset" />
        </span>
        <span v-if="wizardStep === 3">
            <StepThree :gridForPreview="gridForPreview" />
        </span>
        <span v-if="wizardStep === 4">
            <StepFour :selectedDataset="dataset" />
        </span>

        <template #footer>
            <div>
                <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="$emit('closeDialog')" />
                <Button class="kn-button kn-button--secondary" v-if="wizardStep > 1" :label="$t('common.back')" @click="wizardStep--" />
                <Button class="kn-button kn-button--primary" :label="$t('common.next')" :disabled="!fileUploaded" @click="documentFields" />
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import dataViewDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import StepOne from './WorkspaceDatasetWizardStepOne.vue'
import StepTwo from './WorkspaceDatasetWizardStepTwo.vue'
import StepThree from './WorkspaceDatasetWizardStepThree.vue'
import StepFour from './WorkspaceDatasetWizardStepFour.vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    components: { Dialog, StepOne, StepTwo, StepThree, StepFour },
    emits: ['touched', 'fileUploaded', 'closeDialog', 'closeDialogAndReload'],
    props: { selectedDataset: { type: Object as any }, visible: { type: Boolean as any } },
    computed: {
        documentFields(): any {
            switch (this.wizardStep) {
                case 1:
                    return this.submitStepOne
                case 2:
                    return this.submitStepTwo
                case 3:
                    return this.submitStepThree
                case 4:
                    return this.submitStepFour
                default:
                    return this.$emit('closeDialog')
            }
        }
    },
    data() {
        return {
            dataViewDescriptor,
            dataset: {} as any,
            gridForPreview: {} as any,
            datasetColumns: [] as any,
            editingDatasetFile: false,
            newFileUploaded: false,
            fileUploaded: false,
            wizardStep: 1
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.dataset.id ? this.getSelectedDataset() : this.initializeDatasetWizard(undefined, false)
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.id ? this.getSelectedDataset() : this.initializeDatasetWizard(undefined, false)
        }
    },
    methods: {
        onFileUpload() {
            this.fileUploaded = true
            this.newFileUploaded = true
        },
        async getSelectedDataset() {
            this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDataset.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dataset = response.data[0] ? { ...response.data[0] } : {}
                    this.initializeDatasetWizard(this.dataset, true)
                })
                .catch()
        },
        initializeDatasetWizard(dataset, isEdited) {
            this.editingDatasetFile = isEdited
            isEdited ? (this.fileUploaded = true) : (this.fileUploaded = false)
            this.dataset.csvEncoding = dataset != undefined ? dataset.csvEncoding : 'UTF-8'
            this.dataset.csvDelimiter = dataset != undefined ? dataset.csvDelimiter : ','
            this.dataset.csvQuote = dataset != undefined ? dataset.csvQuote : '"'
            this.dataset.skipRows = dataset != undefined ? dataset.skipRows : null
            this.dataset.dateFormat = dataset != undefined && dataset.dateFormat != undefined ? dataset.dateFormat : 'dd/MM/yyyy'
            this.dataset.timestampFormat = dataset != undefined && dataset.timestampFormat != undefined ? dataset.timestampFormat : 'dd/MM/yyyy HH:mm:ss'
            if (dataset != undefined) {
                if (dataset.limitRows != null && dataset.limitRows != '') {
                    this.dataset.limitRows = dataset.limitRows
                } else {
                    this.dataset.limitRows = dataset.limitRows
                }
            } else {
                this.dataset.limitRows = null
            }
            this.dataset.xslSheetNumber = dataset != undefined ? dataset.xslSheetNumber : 1
            this.dataset.catTypeVn = dataset != undefined ? dataset.catTypeVn : null
            this.dataset.catTypeId = dataset != undefined ? dataset.catTypeId : null
            this.dataset.id = dataset != undefined ? dataset.id : ''
            this.dataset.label = dataset != undefined ? dataset.label : ''
            this.dataset.name = dataset != undefined ? dataset.name : ''
            this.dataset.description = dataset != undefined ? dataset.description : ''
            this.dataset.meta = dataset != undefined ? dataset.meta : []
            this.dataset.persist = dataset != undefined && dataset.isPersisted ? dataset.isPersisted : false
            this.dataset.tableName = dataset != undefined && dataset.persistTableName ? dataset.persistTableName : ''
        },
        submitStepOne() {
            let params = {} as any
            params.SBI_EXECUTION_ID = -1
            params.isTech = false
            params.showOnlyOwner = true
            params.showDerivedDataset = false
            this.dataset.type = 'File'
            this.dataset.exportToHdfs = false
            this.dataset.limitPreview = true
            this.dataset.tablePrefix = ''
            this.dataset.persist ? '' : (this.dataset.persist = false)
            this.dataset.tableName ? '' : (this.dataset.tableName = '')
            this.dataset.skipRows == null ? (this.dataset.skipRows = '') : ''
            this.dataset.limitRows == null ? (this.dataset.limitRows = '') : ''
            this.dataset.xslSheetNumber == null ? (this.dataset.xslSheetNumber = '') : ''
            this.dataset.meta = JSON.stringify(this.dataset.meta)
            this.newFileUploaded ? (this.dataset.fileUploaded = true) : ''
            console.log(this.dataset)
            this.$http({
                method: 'POST',
                url: import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + 'selfservicedataset/testDataSet',
                data: this.dataset,
                params: params,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Disable-Errors': 'true' },
                transformRequest: function(obj) {
                    var str = [] as any
                    for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                    return str.join('&')
                }
            })
                .then((response: AxiosResponse<any>) => {
                    if (!response.data.errors) {
                        this.gridForPreview = response.data.gridForPreview
                        this.dataset.meta = response.data.meta
                        this.datasetColumns = response.data.datasetColumns
                        this.wizardStep++
                    } else {
                        this.dataset.meta = []
                    }
                })
                .catch((error: any) => {
                    this.dataset.meta = []
                    error.message == 'error.mesage.description.data.set.parsing.error'
                        ? this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('workspace.myData.parseError') })
                        : this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t(error.message) })
                })
        },
        submitStepTwo() {
            this.dataset.isPublicDS = false
            this.dataset.datasetMetadata = {}
            this.dataset.datasetMetadata.version = 1
            this.dataset.datasetMetadata.dataset = []
            this.dataset.datasetMetadata.columns = []
            this.dataset.datasetMetadata.dataset = [...this.dataset.meta.dataset]
            this.dataset.datasetMetadata.columns = [...this.dataset.meta.columns]
            let c = this.dataset.datasetMetadata.columns
            for (var i = 0; i < c.length; i++) {
                delete c[i].columnView
                delete c[i].pvalueView
                delete c[i].pnameView
                delete c[i].dsMetaValue
            }
            let d = this.dataset.datasetMetadata.dataset
            for (i = 0; i < d.length; i++) {
                delete d[i].pvalueView
                delete d[i].pnameView
            }
            this.dataset.datasetMetadata = JSON.stringify(this.dataset.datasetMetadata)
            this.dataset.limitPreview = true
            this.dataset.page = 1
            this.dataset.start = ''
            this.dataset.page = 10

            if (this.editingDatasetFile == true && this.dataset.fileUploaded == true) {
                this.dataset.label = ''
            }
            var params = {} as any
            params.SBI_EXECUTION_ID = -1
            this.wizardStep++
        },
        submitStepThree() {
            this.wizardStep++
        },
        async submitStepFour() {
            let dsToSend = { ...this.dataset }
            dsToSend.isPublicDS = false
            dsToSend.meta = this.dataset.datasetMetadata
            dsToSend.fileUploaded = this.fileUploaded
            delete dsToSend['datasetMetadata']
            var d = new Date()
            var label = 'ds__' + (d.getTime() % 10000000)
            if (dsToSend.label === '') {
                dsToSend.label = label
            }
            var params = {} as any
            params.showDerivedDataset = false
            params.SBI_EXECUTION_ID = -1
            params.isTech = false
            params.showOnlyOwner = true

            await this.$http({
                method: 'POST',
                url: import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + 'selfservicedataset/save',
                data: dsToSend,
                params: params,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Disable-Errors': 'true' },

                transformRequest: function(obj) {
                    var str = [] as any
                    for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                    return str.join('&')
                }
            })
                .then((response: AxiosResponse<any>) => {
                    if (dsToSend.exportToHdfs) {
                        this.$http.post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/hdfs/${response.data.id}`, { headers: { 'X-Disable-Errors': 'true' } }).catch((responseHDFS: any) => {
                            this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: responseHDFS.data.errors[0].message })
                        })
                    }
                    this.$emit('closeDialogAndReload')
                })
                .catch((response: any) => {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: response.data.errors[0].message })
                })
        }
    }
})
</script>
