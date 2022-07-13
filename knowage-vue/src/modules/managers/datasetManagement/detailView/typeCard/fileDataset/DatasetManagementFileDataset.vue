<template>
    <Card class="p-m-2">
        <template #content>
            <div class="p-d-flex">
                <div class="p-field" :style="fileDescriptor.style.maxwidth">
                    <span class="p-float-label">
                        <InputText
                            id="fileName"
                            class="kn-material-input"
                            :style="fileDescriptor.style.maxwidth"
                            v-model.trim="v$.dataset.fileName.$model"
                            :class="{
                                'p-invalid': v$.dataset.fileName.$invalid && v$.dataset.fileName.$dirty
                            }"
                            :disabled="true"
                            @blur="v$.dataset.fileName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="fileName" class="kn-material-input-label"> {{ $t('downloadsDialog.columns.fileName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.fileName" :additionalTranslateParams="{ fieldName: $t('downloadsDialog.columns.fileName') }" />
                </div>
                <Button icon="fas fa-upload" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                <Button icon="fas fa-download" class="p-button-text y p-button-plain p-ml-2" @click="downloadDatasetFile" />
                <KnInputFile v-if="!uploading" :changeFunction="uploadDatasetFile" accept=".csv, .xls, .xlsx" :triggerInput="triggerUpload" />
            </div>

            <div v-if="dataset.fileType && dataset.fileType == 'CSV'" class="p-fluid p-formgrid p-grid p-mt-3">
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvDelimiter" class="kn-material-input" :options="fileDescriptor.csvDelimiterCharacterTypes" optionLabel="name" optionValue="value" v-model="dataset.csvDelimiter" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvDelimiter') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvQuote" class="kn-material-input" :options="fileDescriptor.csvQuoteCharacterTypes" optionLabel="name" optionValue="value" v-model="dataset.csvQuote" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvQuote') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="csvEncoding" class="kn-material-input" :options="fileDescriptor.csvEncodingTypes" optionLabel="name" optionValue="value" v-model="dataset.csvEncoding" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvEncoding') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="dateFormat" class="kn-material-input" :options="fileDescriptor.dateFormatTypes" optionLabel="name" optionValue="value" v-model="dataset.dateFormat" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                </span>
                <span class="p-field p-float-label p-col">
                    <Dropdown id="timestampFormat" class="kn-material-input" :options="fileDescriptor.timestampFormatTypes" optionLabel="name" optionValue="value" v-model="dataset.timestampFormat" />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.timestampFormat') }} </label>
                </span>
            </div>
            <div v-if="dataset.fileType == 'XLS' || dataset.fileType == 'XLSX'" class="p-fluid p-formgrid p-grid p-mt-3">
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="skipRows" class="kn-material-input" type="number" v-model.trim="dataset.skipRows" @change="$emit('touched')" />
                        <label for="skipRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanSkipRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="limitRows" class="kn-material-input" type="number" v-model.trim="dataset.limitRows" @change="$emit('touched')" />
                        <label for="limitRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanLimitRows') }} </label>
                    </span>
                </div>
                <div class="p-field p-col">
                    <span class="p-float-label">
                        <InputText id="sheetnumber" class="kn-material-input" type="number" v-model.trim="dataset.xslSheetNumber" @change="$emit('touched')" />
                        <label for="sheetnumber" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanXslSheetNumber') }} </label>
                    </span>
                </div>
            </div>
        </template>
    </Card>

    <div id="preview-container" v-if="rows.length > 0">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
            <template #start>
                <Button v-if="!expandTableCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = false" />
                {{ $t('managers.lovsManagement.preview') }}
            </template>
        </Toolbar>
        <Card class="p-m-2" v-show="expandTableCard">
            <template #content>
                <DataTable :value="rows" class="p-datatable-sm kn-table" :loading="loading" responsiveLayout="scroll" :scrollable="true" scrollDirection="both" scrollHeight="800px" stripedRows rowHover style="width:70vw">
                    <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" style="width:250px" />
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

export default defineComponent({
    components: { Card, KnValidationMessages, KnInputFile, Dropdown, DataTable, Column },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any } },
    emits: ['touched', 'fileUploaded'],
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
    created() {
        this.dataset = this.selectedDataset
        this.dataset.id ? this.getPreviewData() : ''
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.id ? this.getPreviewData() : ''
        }
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
            let uploadedFile = event.target.files[0]

            this.startUpload(uploadedFile)

            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        async startUpload(uploadedFile) {
            var formData = new FormData()
            formData.append('file', uploadedFile)
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `selfservicedatasetupload/fileupload`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryFYwjkDOpT85ZFN3L'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    this.$store.commit('setInfo', {
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
            var encodedLabel = encodeURI(this.dataset.label)
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/download/file?dsLabel=${encodedLabel}&type=${this.dataset.fileType}`, {
                    headers: {
                        Accept: 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9'
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
        async getPreviewData() {
            this.loading = true
            this.dataset.limit = 10
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/preview`, this.dataset, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8',
                        'X-Disable-Errors': 'true'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    this.columns = []
                    let previewColumns = response.data.metaData.fields
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
