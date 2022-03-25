<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('workspace.gis.title') }}
            </template>
            <template #end>
                <Button class="p-button-text p-button-rounded p-button-plain" :label="$t('workspace.gis.editMap')" @click="logGis" />
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveDialogDisabled" @click="saveDialogVisible = true" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" />
            </template>
        </Toolbar>
        <div class="gis-tabview-container p-d-flex p-flex-column kn-flex">
            <KnOverlaySpinnerPanel :visibility="loading" />
            <TabView v-if="!loading" class="p-d-flex p-flex-column kn-flex">
                <TabPanel>
                    <template #header>
                        <span v-if="$route.path.includes('new')">{{ $t('workspace.gis.datasetLayerTitle') }}</span>
                        <span v-else>{{ $t('managers.layersManagement.layerTitle') }}</span>
                    </template>
                    <DatasetLayerTab :documentDataProp="documentData" :isDatasetChosen="isDatasetChosen" @datasetChanged="onDatasetChange($event)" @layerChanged="onLayerChange($event)" @datasetDeleted="onDatasetDelete" />
                </TabPanel>

                <TabPanel :disabled="!documentData.selectedDataset.length > 0 || !documentData.selectedLayer.length > 0">
                    <template #header>
                        <span>{{ $t('workspace.gis.datasetJoinTitle') }}</span>
                        <Badge v-if="validations.joinsInvalid" value="" class="p-ml-2" severity="danger" />
                    </template>

                    <DatasetJoinTab :documentDataProp="documentData" @joinsValidationChanged="validationChanged" />
                </TabPanel>

                <TabPanel :disabled="!documentData.selectedDataset.length > 0">
                    <template #header>
                        <span>{{ $t('workspace.gis.indicators') }}</span>
                        <Badge v-if="validations.indicatorsInvalid" value="" class="p-ml-2" severity="danger" />
                    </template>

                    <IndicatorsTab :documentDataProp="documentData" @indicatorsValidationChanged="validationChanged" />
                </TabPanel>

                <TabPanel>
                    <template #header>
                        <span>{{ $t('workspace.gis.filtersMenu') }}</span>
                        <Badge v-if="validations.filtersInvalid" value="" class="p-ml-2" severity="danger" />
                    </template>

                    <MenuTab :documentDataProp="documentData" @filtersValidationChanged="validationChanged" />
                </TabPanel>
            </TabView>
        </div>

        <Dialog class="p-fluid kn-dialog--toolbar--primary" :style="descriptor.style.dialogSize" v-if="saveDialogVisible" :visible="saveDialogVisible" :modal="true" :closable="false">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                    <template #start>{{ $t('workspace.gis.save') }}</template>
                </Toolbar>
            </template>
            <div class="p-field p-col-12 p-my-1">
                <span class="p-float-label">
                    <InputText id="label" class="kn-material-input" v-model="documentData.documentLabel" :class="{ 'p-invalid': documentData.documentLabel == null || documentData.documentLabel == '' }" />
                    <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                </span>
                <small for="label" v-if="documentData.documentLabel == null || documentData.documentLabel == ''" class="p-error">Field required *</small>
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <InputText id="desc" class="kn-material-input kn-width-full" v-model="documentData.documentDesc" />
                    <label for="desc" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                </span>
            </div>
            <template #footer>
                <div class="p-d-flex p-flex-row p-jc-end">
                    <Button class="kn-button kn-button--secondary" @click="saveDialogVisible = false"> {{ $t('common.cancel') }}</Button>
                    <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="buildGisTemplate"> {{ $t('common.save') }}</Button>
                </div>
            </template>
        </Dialog>

        <div class="p-d-flex p-flex-row p-jc-end p-mt-auto p-mb-2 p-mr-2">
            <Button class="kn-button kn-button--secondary"> {{ $t('common.back') }}</Button>
            <Button class="kn-button kn-button--primary p-ml-2"> {{ $t('common.next') }}</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import descriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DatasetLayerTab from './tabs/dataset&Layer/GisDocumentDesignerDataset&Layer.vue'
import DatasetJoinTab from './tabs/datasetJoin/GisDocumentDesignerDatasetJoin.vue'
import IndicatorsTab from './tabs/indicator/GisDocumentDesignerIndicator.vue'
import MenuTab from './tabs/menu/GisDocumentDesignerMenu.vue'
import Dialog from 'primevue/dialog'
import Badge from 'primevue/badge'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'gis-document-designer',
    components: { Dialog, TabView, TabPanel, DatasetLayerTab, DatasetJoinTab, IndicatorsTab, MenuTab, KnOverlaySpinnerPanel, Badge },
    emits: [],
    props: {},
    computed: {
        isDatasetChosen(): boolean {
            return this.documentData.datasetLabel != ''
        },
        saveDialogDisabled(): boolean {
            if (this.documentData.selectedDataset.length <= 0 || this.documentData.selectedLayer <= 0 || this.validations.joinsInvalid || this.validations.indicatorsInvalid || this.validations.filtersInvalid) {
                return true
            } else return false
        },
        saveButtonDisabled(): boolean {
            return this.documentData.documentLabel == null || this.documentData.documentLabel == ''
        }
    },
    data() {
        return {
            descriptor,
            loading: false,
            saveDialogVisible: false,
            documentId: 3290 as any,
            templateId: 8067 as any,
            documentTemplate: {} as any,
            documentData: {} as any,
            selectedDocument: {} as any,
            validations: {
                joinsInvalid: false,
                indicatorsInvalid: false,
                filtersInvalid: false
            }
        }
    },
    created() {
        this.loadPage()
    },
    methods: {
        logGis() {
            console.log(this.documentTemplate)
            console.log(this.documentData)
        },
        async loadPage() {
            this.loading = true
            await this.createDocumentData()
            this.loading = false
        },

        async createDocumentData() {
            // http://localhost:3000/knowage-vue/gis/edit?documentId=3290&templateId=8068

            this.documentData = deepcopy(descriptor.newGisTemplate)
            await this.getAllLayers()

            if (this.$route.path.includes('edit')) {
                this.extractParametersFromUrl()
                await this.getSelectedDocument()
                await this.getTemplate() //ako je edit, ucitavamo template, potrebni parametri iz rute: documentId, templateId
                await this.initializeDataset()
                await this.initializeSelectedLayer()
            }
        },

        async getAllLayers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers`).then((response: AxiosResponse<any>) => (this.documentData.allLayers = response.data.root))
        },

        extractParametersFromUrl() {
            this.documentId = this.$route.query.documentId
            this.templateId = this.$route.query.templateId
        },

        async getSelectedDocument() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.documentId}`).then((response: AxiosResponse<any>) => {
                this.selectedDocument = response.data
                this.documentData.documentLabel = response.data.label
                this.documentData.documentDesc = response.data.description
                this.documentData.dataSetId = response.data.dataSetId
            })
        },

        async getTemplate() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.documentId}/templates/selected/${this.templateId}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then(async (response: AxiosResponse<any>) => {
                this.documentTemplate = response.data
                this.documentData.indicators = response.data.indicators
                this.documentData.filters = response.data.filters
                this.documentData.visibilityData = {
                    crossNavigation: response.data.crossNavigation,
                    crossNavigationMultiselect: response.data.crossNavigationMultiselect,
                    visibilityControls: response.data.visibilityControls
                }
            })
        },
        initializeSelectedLayer() {
            if (this.documentTemplate.targetLayerConf) {
                this.documentData.allLayers.forEach((layerFromResponse) => {
                    this.documentTemplate.targetLayerConf.forEach((layerFromTemplate) => {
                        if (layerFromResponse.name === layerFromTemplate.label) {
                            this.documentData.selectedLayer = [layerFromResponse]
                        }
                    })
                })
            }
            this.initializeSelectedJoinColumns()
        },
        async initializeSelectedJoinColumns() {
            if (this.documentTemplate.datasetJoinColumns && this.documentTemplate.layerJoinColumns) {
                var dsJoinCols = this.documentTemplate.datasetJoinColumns.split(',')
                var layerJoinCols = this.documentTemplate.layerJoinColumns.split(',')
                var layerId = this.documentData.selectedLayer[0].layerId

                this.documentData.dsJoins = dsJoinCols.map((x, i) => {
                    return { datasetColumn: x, layerColumn: layerJoinCols[i] }
                })

                this.loadLayerColumns(layerId)
            }
        },
        async loadLayerColumns(layerId) {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers/getFilter?id=${layerId}`).then((response: AxiosResponse<any>) => {
                this.documentData.layerJoinColumns = response.data
            })
        },
        async initializeDataset() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.documentData.dataSetId}`).then((response: AxiosResponse<any>) => {
                this.documentData.selectedDataset = [response.data[0]]
                this.documentData.datasetLabel = response.data[0].label
                this.loadDatasetColumns()
            })
        },
        async loadDatasetColumns() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${this.documentData.datasetLabel}/fields`).then((response: AxiosResponse<any>) => {
                this.documentData.datasetJoinColumns = response.data.results.filter((field) => {
                    return field.nature === 'attribute'
                })
                this.documentData.datasetMeasures = response.data.results.filter((field) => {
                    return field.nature === 'measure'
                })
            })
        },
        validationChanged(property, value) {
            console.log('PROPERTY : ', property, 'VALUE: ', value)
            this.validations[property] = value
        },
        resetAllFields() {
            this.documentData.datasetJoinColumns = []
            this.documentData.datasetMeasures = []
            this.documentData.dsJoins = []
            this.documentData.filters = []
            this.documentData.indicators = []
            this.documentData.selectedLayer = []
            this.documentData.layerJoinColumns = []
        },
        onDatasetChange(dataset) {
            this.documentData.dataSetId = dataset.id
            this.resetAllFields()
            this.initializeDataset()
        },
        onDatasetDelete() {
            this.documentData.datasetLabel = ''
            this.documentData.dataSetId = null
            this.resetAllFields()
        },
        onLayerChange(layer) {
            this.documentData.selectedLayer = layer
            this.isDatasetChosen ? this.loadLayerColumns(layer[0].layerId) : ''
        },
        buildGisTemplate() {
            console.log(this.documentData)
            let template = {} as any

            template.targetLayerConf = [{ label: this.documentData.selectedLayer[0].name }]

            template.datasetJoinColumns = ''
            template.layerJoinColumns = ''
            this.documentData.dsJoins.forEach((join, index) => {
                index != 0 ? (template.datasetJoinColumns += ',') : ''
                template.datasetJoinColumns += join.datasetColumn
            })
            this.documentData.dsJoins.forEach((join, index) => {
                index != 0 ? (template.layerJoinColumns += ',') : ''
                template.layerJoinColumns += join.layerColumn
            })

            template.indicators = this.documentData.indicators
            template.filters = this.documentData.filters
            template.crossNavigation = this.documentData.visibilityData.crossNavigation
            template.crossNavigationMultiselect = this.documentData.visibilityData.crossNavigationMultiselect
            template.visibilityControls = this.documentData.visibilityData.visibilityControls

            console.log(template)
            this.saveGisDocument(template)
        },
        async saveGisDocument(template) {
            if (this.$route.path.includes('new')) {
                let postData = {} as any
                let d = new Date()
                let docLabel = 'geomap_' + (d.getTime() % 10000000)
                postData.action = 'DOC_SAVE'
                postData.customData = { templateContent: template }
                postData.document = { name: this.documentData.documentLabel, description: this.documentData.documentDesc, label: docLabel, type: 'MAP' }
                postData.sourceData = { label: this.documentData.datasetLabel }
                await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/saveDocument/`, postData).then((response: AxiosResponse<any>) => {
                    this.saveDialogVisible = false
                    this.documentData.documentLabel = docLabel
                    this.$store.commit('setInfo', {
                        title: 'Saved',
                        msg: 'SAVED OK'
                    }),
                        console.log(response)
                })
            } else {
                //TODO: Edit Logic
            }
        }
    }
})
</script>
<style lang="scss">
.gis-tabview-container .p-tabview .p-tabview-panel,
.gis-tabview-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
