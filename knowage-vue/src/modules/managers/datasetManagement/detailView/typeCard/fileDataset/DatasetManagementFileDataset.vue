<template>
    <Card class="p-m-2">
        <template #content>
            <div class="p-d-flex">
                <div class="p-field" :style="fileDescriptor.style.maxwidth">
                    <span class="p-float-label">
                        <InputText
                            id="fileName"
                            v-model.trim="v$.dataset.fileName.$model"
                            class="kn-material-input"
                            :style="fileDescriptor.style.maxwidth"
                            :class="{
                                'p-invalid': v$.dataset.fileName.$invalid && v$.dataset.fileName.$dirty
                            }"
                            :disabled="true"
                            @blur="v$.dataset.fileName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="fileName" class="kn-material-input-label"> {{ $t('downloadsDialog.columns.fileName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.dataset.fileName" :additional-translate-params="{ fieldName: $t('downloadsDialog.columns.fileName') }" />
                </div>
                <Button icon="fas fa-upload" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                <Button icon="fas fa-download" class="p-button-text y p-button-plain p-ml-2" @click="downloadDatasetFile" />
                <KnInputFile v-if="!uploading" :change-function="uploadDatasetFile" accept=".csv, .xls, .xlsx" :trigger-input="triggerUpload" />
            </div>

            <div v-if="dataset.fileType && dataset.fileType == 'CSV'" class="p-fluid p-formgrid p-grid p-mt-3">
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvDelimiter" v-model="dataset.csvDelimiter" class="kn-material-input" :options="fileDescriptor.csvDelimiterCharacterTypes" option-label="name" option-value="value" @change="getPreviewData(false)" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvDelimiter') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvQuote" v-model="dataset.csvQuote" class="kn-material-input" :options="fileDescriptor.csvQuoteCharacterTypes" option-label="name" option-value="value" @change="getPreviewData(false)" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvQuote') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvEncoding" v-model="dataset.csvEncoding" class="kn-material-input" :options="fileDescriptor.csvEncodingTypes" option-label="name" option-value="value" @change="getPreviewData(false)" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvEncoding') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="dateFormat" v-model="dataset.dateFormat" class="kn-material-input" :options="fileDescriptor.dateFormatTypes" option-label="name" option-value="value" @change="getPreviewData(false)" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="timestampFormat" v-model="dataset.timestampFormat" class="kn-material-input" :options="fileDescriptor.timestampFormatTypes" option-label="name" option-value="value" @change="getPreviewData(false)" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.timestampFormat') }} </label>
                </span>
            </div>
            <div v-if="dataset.fileType == 'XLS' || dataset.fileType == 'XLSX'" class="p-fluid p-formgrid p-grid p-mt-3">
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="skipRows" v-model.trim="dataset.skipRows" class="kn-material-input" type="number" @change="getPreviewData(false)" />
                        <label for="skipRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanSkipRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="limitRows" v-model.trim="dataset.limitRows" class="kn-material-input" type="number" @change="getPreviewData(false)" />
                        <label for="limitRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanLimitRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="sheetnumber" v-model.trim="dataset.xslSheetNumber" class="kn-material-input" type="number" @change="getPreviewData(false)" />
                        <label for="sheetnumber" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanXslSheetNumber') }} </label>
                    </span>
                </div>
            </div>
        </template>
    </Card>

    <div v-if="rows.length > 0" id="preview-container">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
            <template #start>
                <Button v-if="!expandTableCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandTableCard = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandTableCard = false" />
                {{ $t('managers.lovsManagement.preview') }}
            </template>
            <template #end>
                <Button v-tooltip.left="$t('common.refresh')" icon="pi pi-refresh" class="p-button-text p-button-rounded p-button-plain p-ml-auto" @click="getPreviewData"></Button>
            </template>
        </Toolbar>
        <Card v-show="expandTableCard" class="p-m-2">
            <template #content>
                <DataTable :value="rows" class="p-datatable-sm kn-table" :loading="loading" responsive-layout="scroll" :scrollable="true" scroll-direction="both" scroll-height="800px" striped-rows row-hover style="width: 70vw">
                    <Column v-for="col of columns" :key="col.dataIndex" :field="col.name" :header="col.header" class="kn-truncated" style="width: 250px" />
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import fileDescriptor from './DatasetManagementFileDataset.json'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import mainStore from '../../../../../../App.store'

export default defineComponent({
    components: { Card, KnValidationMessages, KnInputFile, Dropdown, DataTable, Column },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any } },
    emits: ['touched', 'fileUploaded', 'checkFormulaForParams'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            v$: useValidate() as any,
            fileDescriptor,
            dataset: {} as any,
            triggerUpload: false,
            uploading: false,
            loading: false,
            expandTableCard: true,
            columns: [] as any,
            rows: [] as any
        }
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.id ? this.getPreviewData() : ''
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.dataset.id ? this.getPreviewData() : ''
    },
    validations() {
        const fileFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'File' || value
        }
        const customValidators: ICustomValidatorMap = { 'file-fields-required': fileFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', fileDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        uploadDatasetFile(event) {
            this.uploading = true
            const uploadedFile = event.target.files[0]
            if (uploadedFile.name.includes(this.dataset.fileName)) {
                this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('common.error.sameFileName') })
                this.triggerUpload = false
            } else {
                this.startUpload(uploadedFile)
            }
            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        async startUpload(uploadedFile) {
            const formData = new FormData()
            formData.append('file', uploadedFile)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `selfservicedatasetupload/fileupload`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryFYwjkDOpT85ZFN3L'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    this.store.setInfo({
                        title: this.$t('common.uploading'),
                        msg: this.$t('importExport.import.successfullyCompleted')
                    })
                    this.dataset.fileType = response.data.fileType
                    this.dataset.fileName = response.data.fileName
                    this.$emit('fileUploaded')
                    this.resetFields()
                    this.getPreviewData()
                })
                .catch()
                .finally(() => {
                    this.triggerUpload = false
                })
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
        },

        async downloadDatasetFile() {
            const encodedLabel = encodeURI(this.dataset.label)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/datasets/download/file?dsLabel=${encodedLabel}&type=${this.dataset.fileType}`, {
                    headers: {
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
                    },
                    responseType: 'blob'
                })
                .then(
                    (response: AxiosResponse<any>) => {
                        if (response.data.errors) {
                            this.store.setError({ title: this.$t('common.error.downloading'), msg: this.$t('common.error.errorCreatingPackage') })
                        } else {
                            this.store.setInfo({ title: this.$t('common.toast.success') })
                            if (response.headers) {
                                downloadDirect(response.data, this.createCompleteFileName(response), response.headers['content-type'])
                            }
                        }
                    },
                    (error) =>
                        this.store.setError({
                            title: this.$t('common.error.downloading'),
                            msg: this.$t(error)
                        })
                )
        },
        createCompleteFileName(response) {
            const contentDisposition = response.headers['content-disposition']
            const fileAndExtension = contentDisposition.match(/filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/i)[1]
            const completeFileName = fileAndExtension.replaceAll('"', '')
            return completeFileName
        },
        async getPreviewData(metadata = true) {
            this.loading = true
            this.dataset.limit = 10
            if (!metadata) delete this.dataset.meta

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/preview`, this.dataset, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8',
                        'X-Disable-Errors': 'true'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    const previewColumns = response.data.metaData.fields
                    previewColumns.forEach((el: any) => {
                        typeof el != 'object' ? '' : this.columns.push(el)
                    })

                    this.rows = response.data.rows
                })
                .catch()
                .finally(() => (this.loading = false))
        }
    }
})
</script>
