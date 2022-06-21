<template>
    <Card class="p-mt-4">
        <template #content>
            <div class="p-d-flex">
                <div class="p-field" :style="dataViewDescriptor.style.maxwidth">
                    <span class="p-float-label">
                        <InputText id="fileName" class="kn-material-input" :style="dataViewDescriptor.style.maxwidth" v-model.trim="dataset.fileName" :disabled="true" @change="$emit('touched')" />
                        <label for="fileName" class="kn-material-input-label"> {{ $t('downloadsDialog.columns.fileName') }} </label>
                    </span>
                </div>
                <Button icon="fas fa-upload fa-2x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                <Button icon="fas fa-download fa-2x" class="p-button-text y p-button-plain p-ml-2" @click="downloadDatasetFile" />
                <KnInputFile v-if="!uploading" :changeFunction="uploadDatasetFile" accept=".csv, .xls, .xlsx" :triggerInput="triggerUpload" />
            </div>

            <div v-if="dataset.fileType && dataset.fileType == 'CSV'" class="p-fluid p-formgrid p-grid p-mt-3">
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvDelimiter" class="kn-material-input workspace-wizard-step-one-input" :options="dataViewDescriptor.csvDelimiterCharacterTypes" optionLabel="name" optionValue="value" v-model="dataset.csvDelimiter" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvDelimiter') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvQuote" class="kn-material-input workspace-wizard-step-one-input" :options="dataViewDescriptor.csvQuoteCharacterTypes" optionLabel="name" optionValue="value" v-model="dataset.csvQuote" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvQuote') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvEncoding" class="kn-material-input workspace-wizard-step-one-input" :options="dataViewDescriptor.csvEncodingTypes" optionLabel="name" optionValue="value" v-model="dataset.csvEncoding" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvEncoding') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="dateFormat" class="kn-material-input workspace-wizard-step-one-input" :options="dataViewDescriptor.dateFormatTypes" optionLabel="name" optionValue="value" v-model="dataset.dateFormat" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="timestampFormat" class="kn-material-input workspace-wizard-step-one-input" :options="dataViewDescriptor.timestampFormatTypes" optionLabel="name" optionValue="value" v-model="dataset.timestampFormat" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.timestampFormat') }} </label>
                </span>
            </div>
            <div v-if="dataset.fileType == 'XLS' || dataset.fileType == 'XLSX'" class="p-fluid p-formgrid p-grid p-mt-3">
                <div class="p-field p-col-12 p-xl-4">
                    <span class="p-float-label">
                        <InputText id="skipRows" class="kn-material-input" type="number" v-model.trim="dataset.skipRows" @change="$emit('touched')" />
                        <label for="skipRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanSkipRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-xl-4">
                    <span class="p-float-label">
                        <InputText id="limitRows" class="kn-material-input" type="number" v-model.trim="dataset.limitRows" @change="$emit('touched')" />
                        <label for="limitRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanLimitRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-xl-4">
                    <span class="p-float-label">
                        <InputText id="sheetnumber" class="kn-material-input" type="number" v-model.trim="dataset.xslSheetNumber" @change="$emit('touched')" />
                        <label for="sheetnumber" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanXslSheetNumber') }} </label>
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import useValidate from '@vuelidate/core'
import dataViewDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import KnInputFile from '@/components/UI/KnInputFile.vue'

export default defineComponent({
    components: { Card, KnInputFile, Dropdown },
    props: { selectedDataset: { type: Object as any } },
    emits: ['touched', 'fileUploaded', 'closeDialog'],
    data() {
        return {
            v$: useValidate() as any,
            dataViewDescriptor,
            dataset: {} as any,
            triggerUpload: false,
            uploading: false,
            loading: false,
            expandTableCard: true,
            columns: [] as any,
            rows: [] as any
        }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    methods: {
        //#region ===================== File Upload/Download ====================================================
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        uploadDatasetFile(event) {
            this.uploading = true
            let uploadedFile = event.target.files[0]
            if (uploadedFile.name.includes(this.dataset.fileName)) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('common.error.sameFileName') })
                this.triggerUpload = false
            } else {
                this.startUpload(uploadedFile)
            }
            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        async startUpload(uploadedFile) {
            var formData = new FormData()
            formData.append('file', uploadedFile)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `/selfservicedatasetupload/fileupload`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryFYwjkDOpT85ZFN3L'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    this.$store.commit('setInfo', { title: this.$t('common.uploading'), msg: this.$t('importExport.import.successfullyCompleted') })
                    this.dataset.fileType = response.data.fileType
                    this.dataset.fileName = response.data.fileName
                    this.$emit('fileUploaded')
                    // this.resetFields()
                })
                .catch()
                .finally(() => {
                    this.triggerUpload = false
                })
        },
        async downloadDatasetFile() {
            var encodedLabel = encodeURI(this.dataset.label)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/download/file?dsLabel=${encodedLabel}&type=${this.dataset.fileType}`, {
                    headers: {
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9',
                        'X-Disable-Errors': 'true'
                    }
                })
                .then(
                    (response: AxiosResponse<any>) => {
                        if (response.data.errors) {
                            this.$store.commit('setError', { title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                        } else {
                            this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                            if (response.headers) {
                                downloadDirect(response.data, this.createCompleteFileName(response), response.headers['content-type'])
                            }
                        }
                    },
                    (error) =>
                        this.$store.commit('setError', {
                            title: this.$t('common.error.downloading'),
                            msg: this.$t(error)
                        })
                )
        },
        createCompleteFileName(response) {
            var contentDisposition = response.headers['content-disposition']
            var fileAndExtension = contentDisposition.match(/filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/i)[1]
            var completeFileName = fileAndExtension.replaceAll('"', '')
            return completeFileName
        },
        resetFields() {
            this.dataset.csvEncoding = 'UTF-8'
            this.dataset.csvDelimiter = ','
            this.dataset.dateFormat = 'dd/MM/yyyy'
            this.dataset.timestampFormat = 'dd/MM/yyyy HH:mm:ss'
            this.dataset.csvQuote = '"'
            this.dataset.skipRows = 0
            this.dataset.limitRows = null
            this.dataset.xslSheetNumber = 1

            if (this.dataset.fileType == 'XLS' || this.dataset.fileType == 'XLSX') {
                this.dataset.limitRows = ''
                this.dataset.csvDelimiter = ''
                this.dataset.dateFormat = ''
                this.dataset.timestampFormat = ''
                this.dataset.csvQuote = ''
            }
        }
        //#endregion ================================================================================================
    }
})
</script>

<style lang="scss">
.workspace-wizard-step-one-input {
    min-width: 100px;
}
</style>
