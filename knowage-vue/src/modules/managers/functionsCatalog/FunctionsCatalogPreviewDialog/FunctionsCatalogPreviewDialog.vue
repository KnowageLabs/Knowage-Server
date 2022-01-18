<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="functionsCatalogPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.functionsCatalog.previewTitle') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <TabView v-model:activeIndex="active">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.configurator') }}</span>
                </template>

                <FunctionsCatalogConfiguratorTab :datasets="datasets" :propFunction="propFunction" @loading="setLoading" @selectedEnvironment="setEnvironment" @selectedDataset="setDataset"></FunctionsCatalogConfiguratorTab>
            </TabPanel>
            <TabPanel :disabled="previewInvalid">
                <template #header>
                    <span>{{ $t('common.preview') }}</span>
                </template>

                <FunctionsCatalogPreviewTable :previewColumns="previewColumns" :previewRows="previewRows"></FunctionsCatalogPreviewTable>
            </TabPanel>
        </TabView>

        <FunctionsCatalogPreviewWarningDialog :visible="warningVisible" :warningTitle="warningTitle" :warningMessage="warningMessage" @close="warningVisible = false"></FunctionsCatalogPreviewWarningDialog>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :icon="active === 0 ? 'pi pi-chevron-right' : 'pi pi-chevron-left'" :iconPos="active === 0 ? 'right' : 'left'" :label="active === 0 ? $t('common.next') : $t('common.back')" @click="changeTab" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataset } from '../FunctionsCatalog'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import functionsCatalogPreviewDialogDescriptor from './FunctionsCatalogPreviewDialogDescriptor.json'
import FunctionsCatalogConfiguratorTab from './tabs/FunctionsCatalogConfiguratorTab/FunctionsCatalogConfiguratorTab.vue'
import FunctionsCatalogPreviewTable from './tabs/FunctionsCatalogPreviewTab/FunctionsCatalogPreviewTable.vue'
import FunctionsCatalogPreviewWarningDialog from './FunctionsCatalogPreviewWarningDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'function-catalog-preview-dialog',
    components: { Dialog, FunctionsCatalogConfiguratorTab, FunctionsCatalogPreviewTable, FunctionsCatalogPreviewWarningDialog, TabView, TabPanel },
    props: { propFunction: { type: Object, required: true }, datasets: { type: Array }, pythonConfigurations: { type: Array }, visible: { type: Boolean } },
    data() {
        return {
            functionsCatalogPreviewDialogDescriptor,
            environment: null as string | null,
            selectedDataset: null as iDataset | null,
            previewColumns: [] as any[],
            previewRows: [] as any[],
            active: 0,
            warningVisible: false,
            warningTitle: null as string | null,
            warningMessage: null as string | null,
            loading: false
        }
    },
    computed: {
        previewInvalid(): boolean {
            return !this.checkColumnsConfiguration() || !this.checkVariablesConfiguration() || !this.environment
        }
    },
    created() {},
    methods: {
        setLoading(value: boolean) {
            this.loading = value
        },
        setEnvironment(environment: string) {
            this.environment = environment
        },
        setDataset(dataset: iDataset) {
            this.selectedDataset = dataset
        },
        changeTab() {
            this.active === 0 ? this.goToPreview() : (this.active = 0)
        },
        async goToPreview() {
            let valid = true
            this.warningMessage = null

            if (!this.checkColumnsConfiguration()) {
                valid = this.setWarningMessage(this.$t('managers.functionsCatalog.datasetColumnsError'))
            } else if (!this.checkVariablesConfiguration()) {
                valid = this.setWarningMessage(this.$t('managers.functionsCatalog.inputVariablesError'))
            } else if (!this.environment) {
                valid = this.setWarningMessage(this.$t('managers.functionsCatalog.environmentError'))
            }

            await this.openPreview(valid)
        },
        async openPreview(valid: boolean) {
            if (valid) {
                await this.createPreview()
                this.active = 1
            } else {
                this.warningTitle = this.$t('managers.functionsCatalog.warningTitle')
                this.warningVisible = true
            }
        },
        setWarningMessage(message: string) {
            this.warningMessage = message
            return false
        },
        checkColumnsConfiguration() {
            for (let i = 0; i < this.propFunction.inputColumns.length; i++) {
                if (!this.propFunction.inputColumns[i].dsColumn) {
                    return false
                }
            }
            return true
        },
        checkVariablesConfiguration() {
            for (let i = 0; i < this.propFunction.inputVariables.length; i++) {
                if (!this.propFunction.inputVariables[i].value) {
                    return false
                }
            }
            return true
        },
        async createPreview() {
            this.loading = true
            const postBody = { aggregations: this.buildPreviewAggregations(), parameters: this.buildPreviewParameters(), selections: {}, indexes: [] }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${this.selectedDataset?.label}/data`, postBody, { headers: { 'X-Disable-Errors': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    this.setPreviewColumns(response.data)
                    this.previewRows = response.data.rows
                })
                .catch((response: any) => {
                    this.warningVisible = true
                    this.warningTitle = this.$t('managers.functionsCatalog.dataServiceErrorTitle')
                    this.warningMessage = response.message === '100' ? this.$t('managers.functionsCatalog.dataServiceErrorMessage') : response.message
                })
            this.loading = false
        },
        setPreviewColumns(data: any) {
            this.previewColumns = []
            for (let i = 1; i < data.metaData.fields.length; i++) {
                this.previewColumns.push({ header: data.metaData.fields[i].header, field: data.metaData.fields[i].name })
            }
        },
        buildPreviewAggregations() {
            const measures = [] as any[]
            const categories = [] as any[]

            this.formatDatasetMetaColumns(measures, categories)
            this.formatFunctionConfig(measures, categories)

            return { measures: measures, categories: categories, dataset: this.selectedDataset?.label }
        },

        formatDatasetMetaColumns(measures: any[], categories: any[]) {
            const datasetMetaColumns = this.selectedDataset?.meta.columns
            for (let i = 0; i < datasetMetaColumns.length; i += 3) {
                const name = datasetMetaColumns[i].column
                const object = { id: name, alias: datasetMetaColumns[i + 2].pvalue, columnName: name, funct: 'NONE' } as any
                this.addObjectToMeasuresOrCategories(datasetMetaColumns[i + 1].pvalue, object, measures, categories, name)
            }
        },
        formatFunctionConfig(measures: any[], categories: any[]) {
            const functionConfig = { inputColumns: this.propFunction.inputColumns, inputVariables: this.propFunction.inputVariables, outputColumns: this.propFunction.outputColumns, environment: this.environment }
            for (let i = 0; i < this.propFunction.outputColumns.length; i++) {
                const name = this.propFunction.outputColumns[i].name
                const object = { id: name, alias: name, catalogFunctionId: this.propFunction.id, catalogFunctionConfig: functionConfig, columnName: name, funct: 'NONE' } as any
                this.addObjectToMeasuresOrCategories(this.propFunction.outputColumns[i].fieldType, object, measures, categories, name)
            }
        },
        addObjectToMeasuresOrCategories(fieldType: string, object: any, measures: any[], categories: any[], name: string) {
            if (fieldType === 'MEASURE') {
                object.orderColumn = name
                measures.push(object)
            } else {
                object.orderType = ''
                categories.push(object)
            }
        },
        buildPreviewParameters() {
            const parameters = {}
            if (this.selectedDataset) {
                for (let i = 0; i < this.selectedDataset.pars.length; i++) {
                    parameters[this.selectedDataset.pars[i].name] = this.selectedDataset.pars[i].value
                }
            }
            return parameters
        },
        closeDialog() {
            this.active = 0
            this.selectedDataset = null
            this.previewColumns = []
            this.previewRows = []
            this.environment = null
            this.$emit('close')
        }
    }
})
</script>
