<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('workspace.gis.title') }}
            </template>
            <template #end>
                <Button class="p-button-text p-button-rounded p-button-plain" :label="$t('workspace.gis.editMap')" @click="logGis" />
                <!-- <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="saveDialogVisible = true" /> -->
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="buildGisTemplate" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" />
            </template>
        </Toolbar>
        <div class="gis-tabview-container p-d-flex p-flex-column kn-flex">
            <TabView v-if="!loading" class="p-d-flex p-flex-column kn-flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('workspace.gis.datasetLayerTitle') }}</span>
                    </template>
                    <DatasetLayerTab :documentDataProp="documentData" :isDatasetChosen="isDatasetChosen" @datasetChanged="onDatasetChange($event)" @layerChanged="onLayerChange($event)" @datasetDeleted="onDatasetDelete" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('workspace.gis.datasetJoinTitle') }}</span>
                    </template>
                    <DatasetJoinTab :documentDataProp="documentData" @joinsValidationChanged="onJoinValidationChange" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('workspace.gis.indicators') }}</span>
                    </template>

                    <IndicatorsTab :documentDataProp="documentData" @indicatorsValidationChanged="onIndicatorsValidationChanged" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('workspace.gis.filtersMenu') }}</span>
                    </template>

                    <MenuTab :documentDataProp="documentData" />
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
                    <Button class="kn-button kn-button--primary" @click="buildGisTemplate"> {{ $t('common.save') }}</Button>
                </div>
            </template>
        </Dialog>

        <div class="p-d-flex p-flex-row p-jc-end p-mt-auto p-mb-2 p-mr-2">
            <Button class="kn-button kn-button--secondary"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary p-ml-2"> {{ $t('common.save') }}</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import descriptor from '@/modules/workspace/gisDocumentDesigner/GisDocumentDesignerDescriptor.json'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import DatasetLayerTab from './tabs/dataset&Layer/GisDocumentDesignerDataset&Layer.vue'
import DatasetJoinTab from './tabs/datasetJoin/GisDocumentDesignerDatasetJoin.vue'
import IndicatorsTab from './tabs/indicator/GisDocumentDesignerIndicator.vue'
import MenuTab from './tabs/menu/GisDocumentDesignerMenu.vue'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'gis-document-designer',
    components: { Dialog, TabView, TabPanel, DatasetLayerTab, DatasetJoinTab, IndicatorsTab, MenuTab },
    emits: [],
    props: {},
    computed: {
        isDatasetChosen(): boolean {
            return this.documentData.datasetLabel != ''
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
            joinsInvalid: false,
            indicatorsInvalid: false
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
            // await Promise.all([await this.getAllLayers(), await this.initializeDataset(), await this.getTemplate()])
            this.loading = false
        },
        async getAllLayers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers`).then((response: AxiosResponse<any>) => (this.documentData.allLayers = response.data.root))
        },
        async getSelectedDocument() {
            if (this.$route.query.documentId) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${this.$route.query.documentId}`).then((response: AxiosResponse<any>) => {
                    this.selectedDocument = response.data
                    this.documentData.documentLabel = response.data.label
                    this.documentData.documentDesc = response.data.description
                })
            }
        },
        async createDocumentData() {
            // http://localhost:3000/knowage-vue/gis/edit?documentId=3290&templateId=8068&datasetLabel=BIG_TEST_GIS&documentLabel=Hope-GIS
            console.log('ROUTE PARAMETERS: ', this.$route)
            await this.getSelectedDocument()
            await this.getAllLayers() // uvek se ucitavaju layeri
            if (this.$route.path.includes('edit')) {
                this.assignParameterValues()
                await this.getTemplate() //ako je edit, ucitavamo template, potrebni parametri iz rute: documentId, templateId
                await this.initializeDataset() //ucitaj sve podatke koje dataset nosi, dropdown etc
            } else {
                //TODO: Proveriti dal je uopste moguca kreacija samo sa layerima bez dataseta?????
            }
        },

        assignParameterValues() {
            this.documentId = this.$route.query.documentId
            this.templateId = this.$route.query.templateId
            this.documentData.datasetLabel = this.$route.query.datasetLabel
            this.documentData.documentLabel = this.$route.query.documentLabel
        },

        async getTemplate() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.documentId}/templates/selected/${this.templateId}`, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then((response: AxiosResponse<any>) => {
                this.documentTemplate = response.data
                this.documentData.indicators = response.data.indicators
                this.documentData.filters = response.data.filters
                this.documentData.visibilityData = {
                    crossNavigation: response.data.crossNavigation,
                    crossNavigationMultiselect: response.data.crossNavigationMultiselect,
                    visibilityControls: response.data.visibilityControls
                }
                this.initializeSelectedLayer()
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
            this.isDatasetChosen ? this.initializeSelectedJoinColumns() : ''
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
            if (this.isDatasetChosen) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/${this.documentData.datasetLabel}`).then((response: AxiosResponse<any>) => {
                    this.documentData.selectedDataset = [response.data[0]]
                    this.loadDatasetColumns()
                })
            }
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
        onJoinValidationChange(event) {
            console.log('JOIN VALIDAITON CHANGED: ', event)
            this.joinsInvalid = event
        },
        onIndicatorsValidationChanged(event) {
            console.log('INDICATOR VALIDAITON CHANGED: ', event)
            this.indicatorsInvalid = event
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
            this.documentData.datasetLabel = dataset.label
            this.resetAllFields()
            this.initializeDataset()
        },
        onDatasetDelete() {
            this.documentData.datasetLabel = ''
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
