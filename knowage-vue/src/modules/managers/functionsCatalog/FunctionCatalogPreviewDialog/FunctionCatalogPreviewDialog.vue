<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="functionCatalogPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.functionsCatalog.previewTitle') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        </template>

        <TabView v-model:activeIndex="active">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.configurator') }}</span>
                </template>

                <FunctionCatalogConfiguratorTab :datasets="datasets" :propFunction="propFunction" @loading="setLoading" @selectedEnvironment="setEnvironment" @selectedDataset="setDataset"></FunctionCatalogConfiguratorTab>
            </TabPanel>
            <TabPanel :disabled="previewInvalid">
                <template #header>
                    <span>{{ $t('managers.functionsCatalog.preview') }}</span>
                </template>

                <FunctionCatalogPreviewTable :previewColumns="previewColumns" :previewRows="previewRows"></FunctionCatalogPreviewTable>
            </TabPanel>
        </TabView>

        <FunctionCatalogPreviewWarningDialog :visible="warningVisible" :warningMessage="warningMessage" @close="warningVisible = false"></FunctionCatalogPreviewWarningDialog>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :icon="active === 0 ? 'pi pi-chevron-right' : 'pi pi-chevron-left'" :iconPos="active === 0 ? 'right' : 'left'" :label="active === 0 ? $t('managers.functionsCatalog.next') : $t('managers.functionsCatalog.back')" @click="changeTab" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDataset } from '../FunctionsCatalog'
import axios from 'axios'
import Dialog from 'primevue/dialog'
import functionCatalogPreviewDialogDescriptor from './FunctionCatalogPreviewDialogDescriptor.json'
import FunctionCatalogConfiguratorTab from './tabs/FunctionCatalogConfiguratorTab/FunctionCatalogConfiguratorTab.vue'
import FunctionCatalogPreviewTable from './tabs/FunctionCatalogPreviewTab/FunctionCatalogPreviewTable.vue'
import FunctionCatalogPreviewWarningDialog from './FunctionCatalogPreviewWarningDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'function-catalog-preview-dialog',
    components: { Dialog, FunctionCatalogConfiguratorTab, FunctionCatalogPreviewTable, FunctionCatalogPreviewWarningDialog, TabView, TabPanel },
    props: { propFunction: { type: Object, required: true }, datasets: { type: Array }, pythonConfigurations: { type: Array } },
    data() {
        return {
            functionCatalogPreviewDialogDescriptor,
            environment: null as string | null,
            selectedDataset: null as iDataset | null,
            previewColumns: [] as any[],
            previewRows: [] as any[],
            active: 0,
            warningVisible: false,
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
            // console.log('LOADING: ', this.loading)
        },
        setEnvironment(environment: string) {
            this.environment = environment
        },
        setDataset(dataset: iDataset) {
            this.selectedDataset = dataset
            console.log('SET DATASET: ', this.selectedDataset)
        },
        changeTab() {
            this.active === 0 ? this.goToPreview() : (this.active = 0)
            // console.log('ACTIVE: ', this.active)
        },
        async goToPreview() {
            console.log('COLUMNS GOOD? : ', this.checkColumnsConfiguration())
            console.log('VARIABLES GOOD? : ', this.checkVariablesConfiguration())
            console.log('ENVIRONMENT GOOD? : ', this.environment ? true : false)
            console.log('ENVIRONMENT: ', this.environment)
            let valid = true
            this.warningMessage = null

            if (!this.checkColumnsConfiguration()) {
                valid = false
                this.warningMessage = this.$t('managers.functionsCatalog.datasetColumnsError')
            } else if (!this.checkColumnsConfiguration()) {
                valid = false
                this.warningMessage = this.$t('managers.functionsCatalog.inputVariablesError')
            } else if (!this.environment) {
                valid = false
                this.warningMessage = this.$t('managers.functionsCatalog.environmentError')
            }

            if (valid) {
                await this.createPreview()
                this.active = 1
            } else {
                this.warningVisible = true
                console.log('MEESSAGE', this.warningMessage)
            }
        },
        checkColumnsConfiguration() {
            // console.log('CHECK COLUMNS: ', this.propFunction.inputColumns)
            for (let i = 0; i < this.propFunction.inputColumns.length; i++) {
                if (!this.propFunction.inputColumns[i].dsColumn) {
                    return false
                }
            }
            return true
        },
        checkVariablesConfiguration() {
            // console.log('CHECK VARIABLES: ', this.propFunction.inputVariables)
            for (let i = 0; i < this.propFunction.inputVariables.length; i++) {
                if (!this.propFunction.inputVariables[i].value) {
                    return false
                }
            }
            return true
        },
        async createPreview() {
            const postBody = { aggregations: this.buildPreviewAggregations(), parameters: this.buildPreviewParameters(), selections: {}, indexes: [] }

            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${this.selectedDataset?.label}/data`, postBody, { headers: { 'X-Disable-Errors': true } })
                .then((response) => {
                    this.setPreviewColumns(response.data)
                    this.previewRows = response.data.rows
                    console.log('LOADED PREVIEW COLUMNS: ', this.previewColumns)
                    console.log('LOADED PREVIEW ROWS: ', this.previewRows)
                })
                .catch((error) => {
                    this.warningVisible = true
                    this.warningMessage = error.message === 'generic.error' ? this.$t('managers.functionsCatalog.genericError') : error.message
                })
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
                console.log('OUTPUT TYPE: ', this.propFunction.outputColumns[i].fieldType)
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
            // console.log('BUILD PAR: ', this.selectedDataset)
            if (this.selectedDataset) {
                for (let i = 0; i < this.selectedDataset.pars.length; i++) {
                    parameters[this.selectedDataset.pars[i].name] = this.selectedDataset.pars[i].value
                }
            }
            // console.log('PARAMTERS: ', parameters)
            return parameters
        }
    }
})
</script>
