<template>
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" :style="dataViewDescriptor.style.dialog" v-bind:visible="visible" footer="footer" :header="$t('datasetWizard')" :closable="false" modal>
        <span v-if="wizardStep === 1">
            <Step1 :selectedDataset="dataset" />
        </span>
        <span v-if="wizardStep === 2">Step 2</span>
        <span v-if="wizardStep === 3">Step 3</span>
        <span v-if="wizardStep === 4">Step 4</span>

        <template #footer>
            <div>
                <Button class="kn-button kn-button--primary" label="LOG DATASET" @click="logDataset" />
                <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="$emit('closeDialog')" />
                <Button v-if="wizardStep > 1" class="kn-button kn-button--secondary" :label="$t('common.back')" @click="wizardStep--" />
                <Button class="kn-button kn-button--primary" :label="$t('common.next')" @click="submitStep1" />
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import dataViewDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import Step1 from './WorkspaceDatasetWizardStep1.vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    components: { Dialog, Step1 },
    props: { selectedDataset: { type: Object as any }, visible: { type: Boolean as any } },
    emits: ['touched', 'fileUploaded', 'closeDialog'],
    data() {
        return {
            dataViewDescriptor,
            dataset: {} as any,
            gridForPreview: {} as any,
            datasetColumns: [] as any,
            wizardStep: 1
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.dataset.id ? this.getSelectedDataset() : this.initializeDatasetWizard(undefined)
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.dataset.id ? this.getSelectedDataset() : this.initializeDatasetWizard(undefined)
        }
    },
    methods: {
        async getSelectedDataset() {
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.selectedDataset.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.dataset = response.data[0] ? { ...response.data[0] } : {}
                    this.initializeDatasetWizard(this.dataset)
                })
                .catch()
        },
        logDataset() {
            console.log(this.dataset)
        },
        initializeDatasetWizard(dataset) {
            this.dataset.csvEncoding = dataset != undefined ? dataset.csvEncoding : 'UTF-8'
            this.dataset.csvDelimiter = dataset != undefined ? dataset.csvDelimiter : ','
            this.dataset.csvQuote = dataset != undefined ? dataset.csvQuote : '"'
            this.dataset.skipRows = dataset != undefined ? Number(dataset.skipRows) : Number(null)
            this.dataset.dateFormat = dataset != undefined && dataset.dateFormat != undefined ? dataset.dateFormat : 'dd/MM/yyyy'
            this.dataset.timestampFormat = dataset != undefined && dataset.timestampFormat != undefined ? dataset.timestampFormat : 'dd/MM/yyyy HH:mm:ss'
            if (dataset != undefined) {
                if (dataset.limitRows != null && dataset.limitRows != '') {
                    this.dataset.limitRows = Number(dataset.limitRows)
                } else {
                    this.dataset.limitRows = dataset.limitRows
                }
            } else {
                this.dataset.limitRows = null
            }
            this.dataset.xslSheetNumber = dataset != undefined ? Number(dataset.xslSheetNumber) : Number(1)
            this.dataset.catTypeVn = dataset != undefined ? dataset.catTypeVn : ''
            this.dataset.catTypeId = dataset != undefined ? Number(dataset.catTypeId) : null
            this.dataset.id = dataset != undefined ? dataset.id : ''
            this.dataset.label = dataset != undefined ? dataset.label : ''
            this.dataset.name = dataset != undefined ? dataset.name : ''
            this.dataset.description = dataset != undefined ? dataset.description : ''
            this.dataset.meta = dataset != undefined ? dataset.meta : []
            this.dataset.persist = dataset != undefined && dataset.isPersisted ? dataset.isPersisted : false
            this.dataset.tableName = dataset != undefined && dataset.persistTableName ? dataset.persistTableName : ''
            this.dataset.fileUploaded = false
        },
        submitStep1() {
            let params = {} as any
            params.SBI_EXECUTION_ID = -1
            params.isTech = false
            params.showOnlyOwner = true
            params.showDerivedDataset = false
            this.dataset.type = 'dada'
            this.dataset.exportToHdfs = false
            // this.dataset.tablePrefix = datasetParameters.TABLE_NAME_PREFIX + (this.$store.state as any).user.fullName + '_'
            this.dataset.tablePrefix = 'D_' + (this.$store.state as any).user.fullName + '_'
            this.dataset.persist ? '' : (this.dataset.persist = false)
            this.dataset.tableName ? '' : (this.dataset.tableName = '')
            this.dataset.skipRows == null ? (this.dataset.skipRows = '') : ''
            this.dataset.limitRows == null ? (this.dataset.limitRows = '') : ''
            this.dataset.xslSheetNumber == null ? (this.dataset.xslSheetNumber = '') : ''
            this.dataset.meta = JSON.stringify(this.dataset.meta)
            this.$http({
                method: 'POST',
                url: process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'selfservicedataset/testDataSet',
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
                        console.info('[SUCCESS]: The Step 1 form is submitted successfully.')
                        this.gridForPreview = response.data.gridForPreview
                        this.wizardStep++
                        this.dataset.meta = {}
                        this.dataset.meta = response.data.meta
                        this.datasetColumns = response.data.datasetColumns
                        // this.prepareMetaForView()
                        // this.prepareDatasetForView()
                    } else {
                        console.log(response.data.errors)
                        this.dataset.meta = []
                    }
                })
                .catch((error: any) => {
                    console.log(error)
                })
        }
    }
})
</script>
