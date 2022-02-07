<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedDataset.label }}</template>
        <template #right>
            <Button :label="$t('managers.lovsManagement.preview')" class="p-button-text p-button-rounded p-button-plain" @click="showPreviewDialog = true" :disabled="buttonDisabled" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="checkFormulaForParams" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="$emit('close')" />
        </template>
    </Toolbar>
    <div class="datasetDetail">
        <TabView class="tabview-custom" v-model:activeIndex="activeTab" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('managers.mondrianSchemasManagement.detail.title') }}</span>
                </template>
                <DetailCard
                    :scopeTypes="scopeTypes"
                    :categoryTypes="categoryTypes"
                    :selectedDataset="selectedDataset"
                    :selectedDatasetVersions="selectedDatasetVersions"
                    :availableTags="availableTags"
                    :loading="loading"
                    @reloadVersions="getSelectedDatasetVersions"
                    @loadingOlderVersion="$emit('loadingOlderVersion')"
                    @olderVersionLoaded="onOlderVersionLoaded"
                    @touched="$emit('touched')"
                />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.alert.type') }}</span>
                </template>
                <TypeCard
                    :selectedDataset="selectedDataset"
                    :datasetTypes="datasetTypes"
                    :dataSources="dataSources"
                    :businessModels="businessModels"
                    :scriptTypes="scriptTypes"
                    :parentValid="v$.$invalid"
                    :pythonEnvironments="pythonEnvironments"
                    :rEnvironments="rEnvironments"
                    @fileUploaded="selectedDataset.fileUploaded = true"
                    @touched="$emit('touched')"
                />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>
                <MetadataCard :selectedDataset="selectedDataset" @touched="$emit('touched')" />
            </TabPanel>

            <TabPanel v-if="selectedDataset.dsTypeCd == 'Query'">
                <template #header>
                    <span>{{ $t('managers.glossary.glossaryUsage.link') }}</span>
                </template>
                <LinkCard :selectedDataset="selectedDataset" :metaSourceResource="metaSourceResource" :activeTab="activeTab" @addTables="onAddLinkedTables" @removeTables="onRemoveLinkedTables" />
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('cron.advanced') }}</span>
                </template>
                <AdvancedCard :selectedDataset="selectedDataset" :transformationDataset="transformationDataset" :schedulingData="scheduling" @touched="$emit('touched')" />
            </TabPanel>
        </TabView>

        <WorkspaceDataPreviewDialog :visible="showPreviewDialog" :propDataset="selectedDataset" @close="showPreviewDialog = false" :previewType="'dataset'"></WorkspaceDataPreviewDialog>
    </div>
</template>

<script lang="ts">
import useValidate from '@vuelidate/core'
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import detailViewDescriptor from './DatasetManagementDetailViewDescriptor.json'
import DetailCard from './detailCard/DatasetManagementDetailCard.vue'
import TypeCard from './typeCard/DatasetManagementTypeCard.vue'
import AdvancedCard from './advancedCard/DatasetManagementAdvancedCard.vue'
import LinkCard from './linkCard/DatasetManagementLinkCard.vue'
import MetadataCard from './metadataCard/DatasetManagementMetadataCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import WorkspaceDataPreviewDialog from '@/modules/workspace/views/dataView/dialogs/WorkspaceDataPreviewDialog.vue'

export default defineComponent({
    components: { TabView, TabPanel, DetailCard, AdvancedCard, LinkCard, TypeCard, MetadataCard, WorkspaceDataPreviewDialog },
    props: {
        id: { type: String, required: false },
        scopeTypes: { type: Array as any, required: true },
        categoryTypes: { type: Array as any, required: true },
        datasetTypes: { type: Array as any, required: true },
        transformationDataset: { type: Object as any, required: true },
        scriptTypes: { type: Array as any, required: true },
        dataSources: { type: Array as any, required: true },
        businessModels: { type: Array as any, required: true },
        pythonEnvironments: { type: Array as any, required: true },
        rEnvironments: { type: Array as any, required: true },
        metaSourceResource: { type: Array as any, required: true },
        availableTags: { type: Array as any, required: true },
        datasetToCloneId: { type: Number as any }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    emits: ['close', 'touched', 'loadingOlderVersion', 'olderVersionLoaded', 'updated', 'created', 'showSavingSpinner', 'hideSavingSpinner'],
    data() {
        return {
            detailViewDescriptor,
            v$: useValidate() as any,
            tablesToAdd: [] as any,
            tablesToRemove: [] as any,
            selectedDataset: {} as any,
            selectedDatasetVersions: [] as any,
            scheduling: {
                repeatInterval: null as String | null
            } as any,
            touched: false,
            loading: false,
            loadingVersion: false,
            showPreviewDialog: false,
            activeTab: 0
        }
    },
    created() {
        this.getAllDatasetData()
    },
    watch: {
        id() {
            this.getAllDatasetData()
            this.activeTab = 0
        },
        datasetToCloneId() {
            this.cloneDatasetConfirm(this.datasetToCloneId)
        }
    },
    validations() {},
    methods: {
        //#region ===================== Get All Data ====================================================
        async getSelectedDataset() {
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${this.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedDataset = response.data[0] ? { ...response.data[0] } : {}
                    this.selectedDataset.pythonEnvironment ? (this.selectedDataset.pythonEnvironment = JSON.parse(this.selectedDataset.pythonEnvironment ? this.selectedDataset.pythonEnvironment : '{}')) : ''
                })
                .catch()
        },
        async getSelectedDatasetVersions() {
            this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/olderversions/${this.id}`)
                .then((response: AxiosResponse<any>) => {
                    response.data.root ? (this.selectedDatasetVersions = response.data.root) : (this.selectedDatasetVersions = [])
                })
                .catch()
                .finally(() => (this.loading = false))
        },
        async getAllDatasetData() {
            if (this.id) {
                this.loading = true
                await this.getSelectedDataset()
                await this.getSelectedDatasetVersions()
            } else {
                this.selectedDataset = { ...detailViewDescriptor.newDataset }
                this.selectedDatasetVersions = []
            }
        },
        //#endregion ===============================================================================================

        //#region ===================== Clone Functionality ====================================================
        cloneDatasetConfirm(datasetId) {
            this.$confirm.require({
                icon: 'pi pi-exclamation-triangle',
                message: this.$t('kpi.kpiDefinition.confirmClone'),
                header: this.$t(' '),
                datasetId,
                accept: () => this.cloneDataset(datasetId)
            })
        },
        async cloneDataset(datasetId) {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/dataset/id/${datasetId}`).then((response: AxiosResponse<any>) => {
                delete response.data[0].id
                response.data[0].label = '...'
                response.data[0].dsVersions = []
                response.data[0].usedByNDocs = 0

                this.selectedDataset = { ...response.data[0] }
            })
        },
        //#endregion ===============================================================================================

        //#region ===================== Save/Update Dataset & Tags =================================================
        async saveDataset() {
            this.$emit('showSavingSpinner')
            let dsToSave = { ...this.selectedDataset } as any
            let restRequestHeadersTemp = {}
            if (dsToSave.dsTypeCd.toLowerCase() == 'rest' || dsToSave.dsTypeCd.toLowerCase() == 'solr') {
                for (let i = 0; i < dsToSave.restRequestHeaders.length; i++) {
                    restRequestHeadersTemp[dsToSave.restRequestHeaders[i]['name']] = dsToSave.restRequestHeaders[i]['value']
                }
            }
            dsToSave['restRequestHeaders'] && dsToSave['restRequestHeaders'].length > 0 ? (dsToSave.restRequestHeaders = JSON.stringify(restRequestHeadersTemp)) : (dsToSave.restRequestHeaders = '')
            dsToSave['restJsonPathAttributes'] && dsToSave['restJsonPathAttributes'].length > 0 ? (dsToSave.restJsonPathAttributes = JSON.stringify(dsToSave.restJsonPathAttributes)) : (dsToSave.restJsonPathAttributes = '')
            dsToSave.pars ? '' : (dsToSave.pars = [])
            dsToSave.pythonEnvironment ? (dsToSave.pythonEnvironment = JSON.stringify(dsToSave.pythonEnvironment)) : ''
            dsToSave.meta ? (dsToSave.meta = await this.manageDatasetFieldMetadata(dsToSave.meta)) : (dsToSave.meta = [])
            dsToSave.recalculateMetadata = true

            dsToSave.isScheduled ? (dsToSave.schedulingCronLine = await this.formatCronForSave()) : ''

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/`, dsToSave, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then(async (response: AxiosResponse<any>) => {
                    this.touched = false
                    this.$store.commit('setInfo', { title: this.$t('common.toast.createTitle'), msg: this.$t('common.toast.success') })
                    this.selectedDataset.id ? this.$emit('updated') : this.$emit('created', response)
                    await this.saveTags(dsToSave, response.data.id)
                    await this.saveSchedulation(dsToSave, response.data.id)
                    await this.saveLinks(response.data.id)
                    await this.removeLinks(response.data.id)
                    await this.getSelectedDataset()
                })
                .catch()
                .finally(() => this.$emit('hideSavingSpinner'))
        },
        async saveTags(dsToSave, id) {
            let tags = {} as any
            tags.versNum = dsToSave.versNum + 1
            tags.tagsToAdd = dsToSave.tags

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${id}/dstags/`, tags, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .catch()
        },
        async saveSchedulation(dsToSave, id) {
            if (dsToSave.isScheduled) {
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/persistence/dataset/id/${id}`, dsToSave, {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/json;charset=UTF-8'
                        }
                    })
                    .catch()
            } else {
                await this.$http.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/persistence/dataset/label/${dsToSave.label}`).catch()
            }
        },
        async saveLinks(id) {
            if (this.tablesToAdd.length > 0) {
                this.tablesToAdd.forEach(async (link) => {
                    if (link.added === true) {
                        delete link.added
                        await this.$http
                            .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaDsRelationResource/${id}`, link, {
                                headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' }
                            })
                            .catch()
                    }
                })
            }
        },
        async removeLinks(id) {
            if (this.tablesToRemove.length > 0) {
                this.tablesToRemove.forEach(async (link) => {
                    if (link.deleted === true) {
                        await this.$http
                            .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaDsRelationResource/${id}/${link.tableId}`, {
                                headers: { Accept: 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8' }
                            })
                            .catch()
                    }
                })
            }
        },
        async manageDatasetFieldMetadata(fieldsColumns) {
            //Temporary workaround because fieldsColumns is now an object with a new structure after changing DataSetJSONSerializer
            if (fieldsColumns.columns != undefined && fieldsColumns.columns != null) {
                var columnsArray = new Array()

                var columnsNames = new Array()
                //create columns list
                for (var i = 0; i < fieldsColumns.columns.length; i++) {
                    var element = fieldsColumns.columns[i]
                    columnsNames.push(element.column)
                }

                columnsNames = this.removeDuplicates(columnsNames)

                for (i = 0; i < columnsNames.length; i++) {
                    var columnObject = { displayedName: '', name: '', fieldType: '', type: '' }
                    var currentColumnName = columnsNames[i]
                    //this will remove the part before the double dot if the column is in the format ex: it.eng.spagobi.Customer:customerId
                    if (currentColumnName.indexOf(':') != -1) {
                        var arr = currentColumnName.split(':')
                        columnObject.displayedName = arr[1]
                    } else {
                        columnObject.displayedName = currentColumnName
                    }

                    columnObject.name = currentColumnName
                    for (var j = 0; j < fieldsColumns.columns.length; j++) {
                        element = fieldsColumns.columns[j]
                        if (element.column == currentColumnName) {
                            if (element.pname.toUpperCase() == 'type'.toUpperCase()) {
                                columnObject.type = element.pvalue
                            } else if (element.pname.toUpperCase() == 'fieldType'.toUpperCase()) {
                                columnObject.fieldType = element.pvalue
                            }
                        }
                    }
                    columnsArray.push(columnObject)
                }

                return columnsArray
                // end workaround ---------------------------------------------------
            }
        },
        checkFormulaForParams() {
            if (this.selectedDataset?.query?.includes('${') && this.selectedDataset?.isPersisted) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.datasetManagement.formulaParamError') })
            } else this.saveDataset()
        },
        removeDuplicates(array) {
            var index = {}
            for (var i = array.length - 1; i >= 0; i--) {
                if (array[i] in index) {
                    array.splice(i, 1)
                } else {
                    index[array[i]] = true
                }
            }
            return array
        },
        async formatCronForSave() {
            if (this.selectedDataset.isScheduled) {
                if (this.selectedDataset.startDate == null) {
                    this.selectedDataset.startDate = new Date()
                }
                var repeatInterval = this.scheduling.repeatInterval
                var finalCronString = ''
                var secondsForCron = 0
                var minutesForCron = this.stringifySchedulingValues(this.scheduling.minutesSelected && this.scheduling.minutesSelected.length != 0, 'minutesSelected')
                var hoursForCron = this.stringifySchedulingValues(repeatInterval != 'minute' && this.scheduling.hoursSelected && this.scheduling.hoursSelected.length != 0, 'hoursSelected')
                var daysForCron = this.stringifySchedulingValues((repeatInterval === 'day' || repeatInterval === 'month') && this.scheduling.daysSelected && this.scheduling.daysSelected.length != 0, 'daysSelected')
                var monthsForCron = this.stringifySchedulingValues(repeatInterval === 'month' && this.scheduling.monthsSelected && this.scheduling.monthsSelected.length != 0, 'monthsSelected')
                var weekdaysForCron = this.stringifySchedulingValues(repeatInterval === 'week' && this.scheduling.weekdaysSelected && this.scheduling.weekdaysSelected.length != 0, 'weekdaysSelected')

                if (daysForCron == '*' && weekdaysForCron != '*') {
                    daysForCron = '?'
                } else {
                    weekdaysForCron = '?'
                }
                finalCronString = minutesForCron + ' ' + hoursForCron + ' ' + daysForCron + ' ' + monthsForCron + ' ' + weekdaysForCron

                return secondsForCron + ' ' + finalCronString
            }
        },
        stringifySchedulingValues(condition, selectedValue) {
            var stringValue = ''
            if (condition) {
                for (var i = 0; i < this.scheduling[selectedValue].length; i++) {
                    stringValue += '' + this.scheduling[selectedValue][i]

                    if (i < this.scheduling[selectedValue].length - 1) {
                        stringValue += ','
                    }
                }
                return stringValue
            } else {
                stringValue = '*'
                return stringValue
            }
        },
        //#endregion ===============================================================================================

        onAddLinkedTables(event) {
            this.tablesToAdd = event
            this.$emit('touched')
        },
        onRemoveLinkedTables(event) {
            this.tablesToRemove = event
            this.$emit('touched')
        },
        onOlderVersionLoaded(event) {
            this.$emit('olderVersionLoaded')
            this.selectedDataset = { ...event }
        }
    }
})
</script>
<style lang="scss" scoped>
.datasetDetail {
    overflow: auto;
    flex: 1;
    display: flex;
    flex-direction: column;
}
</style>
