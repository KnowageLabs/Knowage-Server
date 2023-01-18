<template>
    <Dialog class="full-screen-dialog" :visible="true" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span v-if="qbe">{{ qbe.label ? qbe.label : qbe.qbeDatamarts }}</span>
                </template>
                <template #end>
                    <Button v-if="isParameterSidebarVisible" icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.filter')" @click="parameterSidebarVisible = !parameterSidebarVisible" />
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="openSavingDialog" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <ProgressSpinner class="kn-progress-spinner" v-if="loading" />
        <div v-if="!qbePreviewDialogVisible" class="kn-relative p-d-flex p-flex-row kn-height-full kn-width-full">
            <div v-if="parameterSidebarVisible" :style="qbeDescriptor.style.backdrop" @click="parameterSidebarVisible = false"></div>
            <div v-if="showEntitiesLists && qbeLoaded" :style="qbeDescriptor.style.entitiesLists">
                <div class="p-d-flex p-flex-column kn-flex kn-overflow-hidden">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0">
                        <template #start>
                            <span>{{ $t('qbe.entities.title') }}</span>
                        </template>
                        <template #end>
                            <Chip style="background-color: white"> {{ entities?.entities?.length }} </Chip>
                        </template>
                    </Toolbar>
                    <div class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full qbe-scroll-panel">
                            <ExpandableEntity :availableEntities="entities.entities" :query="mainQuery" @showRelationDialog="showRelationDialog" @entityClicked="onDropComplete($event, false)" @entityChildClicked="onDropComplete($event, false)" @openFilterDialog="openFilterDialog" />
                        </ScrollPanel>
                    </div>
                </div>
                <div class="p-d-flex p-flex-column kn-overflow-hidden" :class="{ 'derived-entities-toggle': showDerivedList }">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0">
                        <template #start>
                            <span>{{ $t('qbe.entities.derived') }}</span>
                        </template>
                        <template #end>
                            <Button v-if="showEntitiesLists" icon="fas fa-plus-circle" class="p-button-text p-button-rounded p-button-plain" v-tooltip.top="$t('common.add')" @click="createSubquery" />
                            <Chip style="background-color: white"> {{ mainQuery?.subqueries?.length }} </Chip>
                            <Button v-if="showDerivedList" icon="pi pi-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="collapseDerivedList" />
                            <Button v-else icon="pi pi-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="collapseDerivedList" />
                        </template>
                    </Toolbar>
                    <div v-show="showDerivedList" class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full qbe-scroll-panel">
                            <SubqueryEntity :availableEntities="mainQuery?.subqueries" @editSubquery="selectSubquery" @deleteSubquery="deleteSubquery" />
                        </ScrollPanel>
                    </div>
                </div>
            </div>
            <div class="qbe-detail-view p-m-1" v-if="qbe && qbeLoaded">
                <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                    <template #start>
                        <Button v-if="showEntitiesLists" icon="pi pi-chevron-left" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.hideList')" @click="toggleEntitiesLists" />
                        <Button v-else icon="pi pi-chevron-right" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.showList')" @click="toggleEntitiesLists" />
                        <span v-if="selectedQuery.id !== 'q1'">
                            <Button label="MAIN" class="p-button-text p-button-plain kn-uppercase" @click="selectMainQuery" />
                            <Button icon="pi pi-chevron-right" class="p-button-text p-button-plain" />
                            <Button :label="selectedQuery?.name" class="p-button-text p-button-plain kn-uppercase" />
                        </span>
                    </template>
                    <template #end>
                        <i v-if="selectedQuery.fields.length > 0" class="fas fa-eraser kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.deleteAllSelectedFields')" @click="deleteAllFieldsFromQuery"></i>
                        <i v-if="hiddenColumnsExist" class="pi pi-eye kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.showHiddenColumns')" @click="showHiddenColumns"></i>
                        <span v-if="selectedQuery.filters.length > 0" class="fa-stack p-mx-2">
                            <i class="fas fa-ban fa-stack-2x"></i>
                            <i class="fas fa-filter fa-stack-1x kn-cursor-pointer" v-tooltip.top="$t('qbe.viewToolbar.deleteAllFilters')" @click="deleteAllFilters"></i>
                        </span>
                        <InputSwitch class="p-mr-2" v-model="smartView" @change="updateSmartView" />
                        <span>{{ $t('qbe.viewToolbar.smartView') }}</span>
                        <i v-show="!smartView" class="fas fa-play p-m-2 kn-cursor-pointer" @click="openPreviewDialog"></i>
                        <Button icon="fas fa-ellipsis-v kn-cursor-pointer" class="p-button-text p-button-rounded p-button-plain" @click="showMenu" />
                    </template>
                </Toolbar>
                <div class="kn-relative kn-flex p-mt-2">
                    <div class="kn-height-full kn-width-full kn-absolute">
                        <QBESimpleTable
                            v-if="!smartView"
                            :query="selectedQuery"
                            @columnVisibilityChanged="checkIfHiddenColumnsExist"
                            @openFilterDialog="openFilterDialog"
                            @openHavingDialog="openHavingDialog"
                            @entityDropped="onDropComplete($event, false)"
                            @groupingChanged="onGroupingChanged"
                            @openCalculatedFieldDialog="editCalcField"
                            @fieldDeleted="onFieldDeleted"
                            @fieldAliasChanged="onFieldAliasChange"
                        />
                        <QBESmartTable
                            v-else
                            :query="selectedQuery"
                            :previewData="queryPreviewData"
                            :pagination="pagination"
                            @removeFieldFromQuery="deleteFieldFromQuery"
                            @orderChanged="updateSmartView"
                            @fieldHidden="smartViewFieldHidden"
                            @fieldGrouped="updateSmartView"
                            @fieldAggregated="updateSmartView"
                            @aliasChanged="onFieldAliasChange"
                            @reordered="smartViewReorder"
                            @entityDropped="onDropComplete($event, false)"
                            @pageChanged="updatePagination($event)"
                            @openFilterDialog="openFilterDialog"
                        />
                    </div>
                </div>
            </div>
            <KnParameterSidebar v-if="parameterSidebarVisible" :filtersData="filtersData" :propDocument="dataset" :userRole="userRole" :propMode="'qbeView'" :propQBEParameters="qbe?.pars" @execute="onExecute" @roleChanged="onRoleChange"></KnParameterSidebar>
        </div>

        <QBEPreviewDialog v-show="!loading && qbePreviewDialogVisible" :id="uniqueID" :queryPreviewData="queryPreviewData" :pagination="pagination" :entities="entities?.entities" :selectedQuery="selectedQuery" @close="closePreview" @pageChanged="updatePagination($event)"></QBEPreviewDialog>
        <QBEFilterDialog :visible="filterDialogVisible" :filterDialogData="filterDialogData" :id="uniqueID" :entities="entities?.entities" :propParameters="qbe?.pars" :propExpression="selectedQuery?.expression" @close="filterDialogVisible = false" @save="onFiltersSave"></QBEFilterDialog>
        <QBESqlDialog :visible="sqlDialogVisible" :sqlData="sqlData" @close="sqlDialogVisible = false" />
        <QBERelationDialog :visible="relationDialogVisible" :propEntity="relationEntity" @close="relationDialogVisible = false" />
        <QBEParamDialog v-if="paramDialogVisible" :visible="paramDialogVisible" :propDataset="qbe" @close="paramDialogVisible = false" @save="onParametersSave" />
        <QBEHavingDialog :visible="havingDialogVisible" :havingDialogData="havingDialogData" :entities="selectedQuery?.fields" @close="havingDialogVisible = false" @save="onHavingsSave"></QBEHavingDialog>
        <QBEAdvancedFilterDialog :visible="advancedFilterDialogVisible" :query="selectedQuery" @close="advancedFilterDialogVisible = false" @save="onAdvancedFiltersSave"></QBEAdvancedFilterDialog>
        <QBESavingDialog :visible="savingDialogVisible" :propDataset="qbe" :propMetadata="qbeMetadata" @close="savingDialogVisible = false" @datasetSaved="$emit('datasetSaved')" />
        <QBEJoinDefinitionDialog v-if="joinDefinitionDialogVisible" :visible="joinDefinitionDialogVisible" :qbe="qbe" :propEntities="entities?.entities" :id="uniqueID" :selectedQuery="selectedQuery" @close="onJoinDefinitionDialogClose"></QBEJoinDefinitionDialog>

        <KnCalculatedField
            v-if="calcFieldDialogVisible"
            v-model:template="selectedCalcField"
            v-model:visibility="calcFieldDialogVisible"
            :fields="calcFieldColumns"
            :descriptor="calcFieldDescriptor"
            :propCalcFieldFunctions="calcFieldFunctionsToShow"
            :readOnly="false"
            :valid="true"
            source="QBE"
            @save="onCalcFieldSave"
            @cancel="calcFieldDialogVisible = false"
        >
            <template #additionalInputs>
                <div class="p-field" :class="[selectedCalcField.type === 'DATE' ? 'p-col-3' : 'p-col-4']">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" v-model="selectedCalcField.type" :options="qbeDescriptor.types" optionLabel="label" optionValue="name" />
                        <label for="type" class="kn-material-input-label"> {{ $t('components.knCalculatedField.type') }} </label>
                    </span>
                </div>
                <div v-if="selectedCalcField.type === 'DATE'" class="p-field p-col-3">
                    <span class="p-float-label">
                        <Dropdown id="type" class="kn-material-input" v-model="selectedCalcField.format" :options="qbeDescriptor.admissibleDateFormats">
                            <template #value>
                                <span>{{ selectedCalcField.format ? moment().format(selectedCalcField.format) : '' }}</span>
                            </template>
                            <template #option="slotProps">
                                <span>{{ moment().format(slotProps.option) }}</span>
                            </template>
                        </Dropdown>
                        <label for="type" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                    </span>
                </div>
                <div class="p-field" :class="[selectedCalcField.type === 'DATE' ? 'p-col-3' : 'p-col-4']">
                    <span class="p-float-label">
                        <Dropdown id="columnType" class="kn-material-input" v-model="selectedCalcField.nature" :options="qbeDescriptor.columnTypes" optionLabel="label" optionValue="name" />
                        <label for="columnType" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnType') }} </label>
                    </span>
                </div>
            </template>
        </KnCalculatedField>

        <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import { iQBE, iQuery, iField, iQueryResult, iFilter } from './QBE'
import { onFiltersSaveCallback } from './QBEFilterService'
import { formatDrivers } from './QBEDriversService'
import { onHavingsSaveCallback } from './QBEHavingsService'
import { createNewField, creatNewMetadataFromField } from '@/helpers/commons/qbeHelpers'
import { buildCalculatedField, updateCalculatedField } from '@/helpers/commons/buildQbeCalculatedField'
import { removeInPlace } from './qbeDialogs/qbeAdvancedFilterDialog/treeService'
import moment from 'moment'
import Dialog from 'primevue/dialog'
import Chip from 'primevue/chip'
import InputSwitch from 'primevue/inputswitch'
import QBEAdvancedFilterDialog from './qbeDialogs/qbeAdvancedFilterDialog/QBEAdvancedFilterDialog.vue'
import QBEFilterDialog from './qbeDialogs/qbeFilterDialog/QBEFilterDialog.vue'
import QBEHavingDialog from './qbeDialogs/qbeHavingDialog/QBEHavingDialog.vue'
import QBESimpleTable from './qbeTables/qbeSimpleTable/QBESimpleTable.vue'
import QBESqlDialog from './qbeDialogs/QBESqlDialog.vue'
import QBERelationDialog from './qbeDialogs/QBEEntityRelationDialog.vue'
import QBEParamDialog from './qbeDialogs/QBEParameterDialog.vue'
import QBESavingDialog from './qbeDialogs/qbeSavingDialog/QBESavingDialog.vue'
import QBESmartTable from './qbeTables/qbeSmartTable/QBESmartTable.vue'
import ExpandableEntity from '@/modules/qbe/qbeComponents/QBEExpandableEntity.vue'
import SubqueryEntity from '@/modules/qbe/qbeComponents/QBESubqueryEntity.vue'
import ScrollPanel from 'primevue/scrollpanel'
import Menu from 'primevue/contextmenu'
import QBEJoinDefinitionDialog from './qbeDialogs/qbeJoinDefinitionDialog/QBEJoinDefinitionDialog.vue'
import KnParameterSidebar from '@/components/UI/KnParameterSidebar/KnParameterSidebar.vue'
import QBEPreviewDialog from './qbeDialogs/qbePreviewDialog/QBEPreviewDialog.vue'
import qbeDescriptor from './QBEDescriptor.json'
import ProgressSpinner from 'primevue/progressspinner'
import calcFieldDescriptor from './QBECalcFieldDescriptor.json'
import KnCalculatedField from '@/components/functionalities/KnCalculatedField/KnCalculatedField.vue'
import Dropdown from 'primevue/dropdown'
import mainStore from '../../App.store'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'
import { getCorrectRolesForExecution } from '@/helpers/commons/roleHelper'

export default defineComponent({
    name: 'qbe',
    components: {
        Dialog,
        Chip,
        InputSwitch,
        ScrollPanel,
        Menu,
        QBEFilterDialog,
        QBESavingDialog,
        QBESqlDialog,
        QBESimpleTable,
        QBERelationDialog,
        QBEParamDialog,
        ExpandableEntity,
        SubqueryEntity,
        QBEHavingDialog,
        QBEAdvancedFilterDialog,
        QBEJoinDefinitionDialog,
        KnParameterSidebar,
        QBEPreviewDialog,
        QBESmartTable,
        ProgressSpinner,
        KnCalculatedField,
        Dropdown
    },
    props: { visible: { type: Boolean }, dataset: { type: Object }, returnQueryMode: { type: Boolean }, getQueryFromDatasetProp: { type: Boolean } },
    emits: ['close', 'querySaved'],
    data() {
        return {
            moment,
            qbeDescriptor,
            calcFieldDescriptor,
            qbe: null as iQBE | null,
            customizedDatasetFunctions: {} as any,
            entities: {} as any,
            queryPreviewData: {} as iQueryResult,
            selectedQuery: {} as any,
            mainQuery: {} as any,
            loading: false,
            showEntitiesLists: true,
            smartView: true,
            hiddenColumnsExist: false,
            filterDialogVisible: false,
            sqlDialogVisible: false,
            paramDialogVisible: false,
            relationDialogVisible: false,
            savingDialogVisible: false,
            filterDialogData: {} as { field: iField; query: iQuery },
            showDerivedList: true,
            discardRepetitions: false,
            sqlData: {} as any,
            menuButtons: [] as any,
            relationEntity: {} as any,
            havingDialogVisible: false,
            havingDialogData: {} as { field: iField; query: iQuery },
            advancedFilterDialogVisible: false,
            joinDefinitionDialogVisible: false,
            parameterSidebarVisible: false,
            user: null as any,
            userRole: null,
            qbePreviewDialogVisible: false,
            pagination: { start: 0, limit: 25 } as any,
            uniqueID: null as string | null,
            filtersData: {} as any,
            qbeLoaded: false,
            calcFieldDialogVisible: false,
            calcFieldColumns: [] as any,
            selectedCalcField: null as any,
            calcFieldFunctions: [] as any,
            calcFieldFunctionsToShow: [] as any,
            qbeMetadata: [] as any,
            colors: ['#D7263D', '#F46036', '#2E294E', '#1B998B', '#C5D86D', '#3F51B5', '#8BC34A', '#009688', '#F44336']
        }
    },
    computed: {
        isParameterSidebarVisible(): boolean {
            let parameterVisible = false
            for (let i = 0; i < this.filtersData?.filterStatus?.length; i++) {
                const tempFilter = this.filtersData.filterStatus[i]
                if (tempFilter.showOnPanel === 'true') {
                    parameterVisible = true
                    break
                }
            }
            return parameterVisible || this.qbe?.pars?.length !== 0
        }
    },
    watch: {
        async dataset() {
            await this.loadPage()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        this.uniqueID = cryptoRandomString({ length: 16, type: 'base64' })
        this.user = (this.store.$state as any).user
        this.userRole = this.user.sessionRole && this.user.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.user.sessionRole : null

        let invalidRole = false
        getCorrectRolesForExecution(null, this.dataset).then(async (response: any) => {
            let correctRolesForExecution = response

            if (!this.userRole) {
                if (correctRolesForExecution.length == 1) {
                    this.userRole = correctRolesForExecution[0]
                } else {
                    this.parameterSidebarVisible = true
                }
            } else if (this.userRole) {
                if (correctRolesForExecution.length == 1) {
                    let correctRole = correctRolesForExecution[0]
                    if (this.userRole !== correctRole) {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: this.$t('documentExecution.main.userRoleError')
                        })
                        invalidRole = true
                    }
                }
            }
            if (!invalidRole) {
                if (this.userRole) {
                    await this.loadPage()
                } else {
                    this.parameterSidebarVisible = true
                }
            }
        })
    },
    methods: {
        //#region ===================== Load QBE and format data ====================================================
        async loadPage() {
            this.loading = true

            if (this.dataset && !this.dataset.dataSourceId && !this.dataset.federation_id) {
                await this.loadDataset()
            } else {
                this.qbe = this.getQBEFromModel()
            }
            this.loadQuery()
            this.qbeMetadata = this.extractFieldsMetadata(this.qbe?.meta.columns)
            this.generateFieldsAndMetadataId()
            if (!this.dataset?.federation_id) await this.loadDatasetDrivers()
            if (this.qbe?.pars?.length === 0 && (this.filtersData?.isReadyForExecution || this.dataset?.federation_id)) {
                await this.loadQBE()
            } else if (this.qbe?.pars?.length !== 0 || !this.filtersData?.isReadyForExecution) {
                this.parameterSidebarVisible = true
            }
            this.loading = false
        },
        async loadDataset() {
            if (!this.dataset) {
                return
            }
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/datasets/${this.dataset.label}`).then((response: AxiosResponse<any>) => {
                this.qbe = response.data[0]
                if (this.qbe && this.qbe.qbeJSONQuery) this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            })

            if (this.qbe && this.getQueryFromDatasetProp) {
                this.qbe = deepcopy(this.dataset) as any
                if (this.qbe && this.qbe.qbeJSONQuery && typeof this.qbe.qbeJSONQuery === 'string') this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            }

            if (this.qbe && this.dataset) {
                this.qbe.pars = this.dataset.pars ?? []
            }
        },
        getQBEFromModel() {
            if (!this.dataset) return {}
            this.smartView = this.dataset.smartView
            return {
                dsTypeCd: 'Qbe',
                qbeDatamarts: this.dataset.name,
                qbeDataSource: this.dataset.dataSourceLabel,
                qbeJSONQuery: this.getQueryFromDatasetProp
                    ? this.dataset.qbeJSONQuery
                    : {
                          catalogue: {
                              queries: [{ id: 'q1', name: 'Main', fields: [], distinct: false, filters: [], calendar: {}, expression: {}, isNestedExpression: false, havings: [], graph: [], relationRoles: [], subqueries: [] }]
                          }
                      },
                meta: [],
                pars: this.dataset.pars ?? [],
                scopeId: null,
                scopeCd: '',
                label: '',
                name: ''
            } as any
        },
        loadQuery() {
            this.mainQuery = this.qbe?.qbeJSONQuery?.catalogue?.queries[0]
            this.selectedQuery = this.qbe?.qbeJSONQuery?.catalogue?.queries[0]
        },
        extractFieldsMetadata(array) {
            if (array && array.length > 0) {
                var object = {}
                for (var item in array) {
                    var element = object[array[item].column]
                    if (!element) {
                        element = {}
                        object[array[item].column] = element
                        element['column'] = array[item].column
                    }
                    element[array[item].pname] = array[item].pvalue
                }
                var fieldsMetadata = new Array()
                for (item in object) {
                    fieldsMetadata.push(object[item])
                }
                return fieldsMetadata
            } else return []
        },
        generateFieldsAndMetadataId() {
            this.selectedQuery.fields.forEach((field) => {
                field.uniqueID = cryptoRandomString({ length: 4, type: 'base64' })
                this.qbeMetadata.find((metadata) => {
                    field.alias === metadata.column ? (metadata.uniqueID = field.uniqueID) : ''
                })
            })
        },
        async loadDatasetDrivers() {
            if (!this.qbe) return
            const label = this.qbe.label ? this.qbe.label : this.qbe.qbeDatamarts
            const url = this.qbe.label ? `3.0/datasets/${label}/filters` : `1.0/businessmodel/${this.qbe.qbeDatamarts}/filters`

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, { role: this.userRole })
                .then((response: AxiosResponse<any>) => {
                    this.filtersData = response.data
                    if (this.filtersData.filterStatus) {
                        this.filtersData.filterStatus = this.filtersData.filterStatus.filter((filter: any) => filter.id)
                    }
                })
                .catch(() => {})

            formatDrivers(this.filtersData)
        },
        async loadQBE() {
            this.loadCalcFieldFunctions()
            await this.initializeQBE()
            await this.loadCustomizedDatasetFunctions()
            await this.loadEntities()

            if (!this.dataset?.dataSourceLabel) {
                await this.executeQBEQuery(false)
            }
        },
        loadCalcFieldFunctions() {
            this.calcFieldFunctions = calcFieldDescriptor.availableFunctions
        },
        async initializeQBE() {
            const label = this.dataset?.dataSourceLabel ? this.dataset.dataSourceLabel : this.qbe?.qbeDataSource
            const datamart = this.dataset?.dataSourceLabel ? this.dataset.name : this.qbe?.qbeDatamarts
            const temp = this.getFormattedParameters(this.filtersData)
            const drivers = encodeURI(JSON.stringify(temp))
            const url = this.dataset?.federation_id
                ? `start-federation?federationId=${this.dataset.federation_id}&user_id=${this.user?.userUniqueIdentifier}&SBI_EXECUTION_ID=${this.uniqueID}&drivers=%7B%7D`
                : `start-qbe?datamart=${datamart}&user_id=${this.user?.userUniqueIdentifier}&SBI_EXECUTION_ID=${this.uniqueID}&DATA_SOURCE_LABEL=${label}&drivers=${drivers}`
            if (this.dataset) {
                await this.$http
                    .get(import.meta.env.VITE_QBE_PATH + url)
                    .then(() => {
                        this.qbeLoaded = true
                    })
                    .catch(() => {})
            }
        },
        getFormattedParameters(loadedParameters: { filterStatus: any[]; isReadyForExecution: boolean }) {
            let parameters = {} as any
            if (!loadedParameters.filterStatus) return parameters
            Object.keys(loadedParameters.filterStatus).forEach((key: any) => {
                const parameter = loadedParameters.filterStatus[key]
                if (!parameter.multivalue) {
                    parameters[parameter.urlName] = [{ value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description }]
                } else {
                    parameters[parameter.urlName] = [{ value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription }]
                }
            })
            return parameters
        },
        async loadCustomizedDatasetFunctions() {
            if (this.dataset && this.dataset.federation_id) return
            const id = this.dataset?.dataSourceId ? this.dataset.dataSourceId : this.qbe?.qbeDataSourceId
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${id}`).then((response: AxiosResponse<any>) => {
                this.customizedDatasetFunctions = response.data
                if (response.data.data && response.data.data.length > 0) {
                    let customFunctions = response.data.data.map((funct) => ({ category: 'CUSTOM', formula: funct.value, label: funct.label, name: funct.name, help: 'dataPreparation.custom' }))
                    customFunctions.forEach((funct) => {
                        this.calcFieldFunctions.push(funct)
                    })
                }
            })
            this.hideSpatialFunct()
        },
        hideSpatialFunct() {
            const isSpatial = (field) => field.isSpatial
            if (!this.selectedQuery.fields.some(isSpatial)) {
                let tempFunctions = deepcopy(this.calcFieldFunctions)
                this.calcFieldFunctionsToShow = tempFunctions.filter((funct) => {
                    return funct.category !== 'SPATIAL'
                })
            } else this.calcFieldFunctionsToShow = deepcopy(this.calcFieldFunctions)
        },
        async loadEntities() {
            let datamartName = this.dataset?.dataSourceId ? this.dataset.name : this.qbe?.qbeDatamarts
            if (this.dataset?.type === 'federatedDataset') {
                datamartName = null
            }
            await this.$http
                .get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=${this.uniqueID}&datamartName=${datamartName}`)
                .then(async (response: AxiosResponse<any>) => {
                    this.addExpandedProperty(response.data.entities)
                    await this.addSpatialProperty(response.data.entities)
                    this.entities = response.data
                })
                .catch((error: any) => console.log('ERROR: ', error))
        },
        addExpandedProperty(entities) {
            entities.forEach((entity) => {
                entity.expanded = false
            })
        },
        async addSpatialProperty(entities) {
            await entities.forEach((entity) => {
                if (entity.iconCls == 'geographic_dimension') {
                    entity.children.forEach((child) => {
                        child.isSpatial = true
                    })
                }
            })
        },
        async executeQBEQuery(showPreview: boolean) {
            if (!this.qbe) return
            if (this.qbe.qbeJSONQuery && this.qbe.qbeJSONQuery.catalogue.queries[0].fields.length == 0) return

            this.loading = true
            const postData = { catalogue: this.qbe?.qbeJSONQuery?.catalogue.queries, meta: this.formatQbeMeta(), pars: this.qbe?.pars, qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(import.meta.env.VITE_QBE_PATH + `qbequery/executeQuery/?SBI_EXECUTION_ID=${this.uniqueID}&currentQueryId=${this.selectedQuery.id}&start=${this.pagination.start}&limit=${this.pagination.limit}`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.queryPreviewData = response.data
                    this.pagination.size = response.data.results
                    if (showPreview) this.qbePreviewDialogVisible = true
                })
                .catch(() => {
                    if (showPreview) this.qbePreviewDialogVisible = false
                })
            this.loading = false
        },
        formatQbeMeta() {
            const meta = [] as any[]
            this.qbe?.qbeJSONQuery?.catalogue.queries?.forEach((query: iQuery) => {
                query.fields?.forEach((field: iField) => {
                    meta.push({ dataType: field.dataType, displayedName: field.alias, fieldType: field.fieldType.toUpperCase(), format: field.format, name: field.alias, type: field.type })
                })
            })
            return meta
        },
        //#endregion ================================================================================================

        //#region ===================== Manage Fields Logic =========================================================
        onDropComplete(field) {
            if (field.connector) return
            if (field.children) {
                for (var i in field.children) {
                    this.addEntityToMainQuery(field.children[i])
                }
            } else {
                this.addEntityToMainQuery(field)
            }
            this.updateSmartView()
        },
        addEntityToMainQuery(field, isCalcField?) {
            let queryModel = this.selectedQuery.fields
            let editQueryObj = this.selectedQuery
            for (var i = 0; i < queryModel.length; i++) {
                if (queryModel != undefined && !this.smartView && queryModel.length > 0) {
                    editQueryObj.fields[i].group = queryModel[i].group
                    editQueryObj.fields[i].funct = queryModel[i].funct
                    editQueryObj.fields[i].visible = queryModel[i].visible
                    editQueryObj.fields[i].distinct = queryModel[i].distinct
                    editQueryObj.fields[i].iconCls = queryModel[i].visible
                    editQueryObj.fields[i].inUse = queryModel[i].inUse
                }
                editQueryObj.fields[i].alias = queryModel[i].alias
            }

            if (!isCalcField) {
                var newField = createNewField(editQueryObj, field)
                var newMetadata = creatNewMetadataFromField(newField)

                editQueryObj.fields.push(newField)
                this.qbeMetadata.push(newMetadata)
            }
            // eslint-disable-next-line no-prototype-builtins
            this.hideSpatialFunct()
        },
        deleteAllFieldsFromQuery() {
            this.selectedQuery.fields = []
            this.selectedQuery.havings = []
            this.qbeMetadata = []
            this.updateSmartView()
        },
        deleteFieldFromQuery(fieldID) {
            let indexOfFieldToDelete = this.selectedQuery.fields.findIndex((field) => {
                return field.uniqueID === fieldID
            })
            this.onFieldDeleted({ ...this.selectedQuery.fields[indexOfFieldToDelete] })
            this.selectedQuery.fields.splice(indexOfFieldToDelete, 1)
            this.updateSmartView()
        },
        onFieldDeleted(field: any) {
            this.deleteFieldMetadata(field.uniqueID)
            for (let i = this.selectedQuery.havings.length - 1; i >= 0; i--) {
                if (this.selectedQuery.havings[i].leftOperandValue === field.id) {
                    this.selectedQuery.havings.splice(i, 1)
                }
            }
            setTimeout(() => {
                this.hideSpatialFunct()
            }, 0)
        },
        deleteFieldMetadata(fieldID) {
            let indexOfFieldMetadataToDelete = this.qbeMetadata.findIndex((metadata) => {
                return metadata.uniqueID === fieldID
            })
            this.qbeMetadata.splice(indexOfFieldMetadataToDelete, 1)
        },
        //#endregion ================================================================================================

        //#region ===================== Calc Fields Logic ===========================================================
        createNewCalcField() {
            this.createCalcFieldColumns()
            this.selectedCalcField = { alias: '', expression: '', format: undefined, nature: 'ATTRIBUTE', type: 'STRING' } as any
            this.calcFieldDialogVisible = true
        },
        editCalcField(calcField) {
            this.createCalcFieldColumns()
            this.selectedCalcField = this.formatCalcFieldForComponent(calcField)
            this.calcFieldDialogVisible = true
        },
        formatCalcFieldForComponent(calcField) {
            let formatField = {
                uniqueID: calcField.uniqueID,
                alias: calcField.id.alias,
                type: calcField.id.type,
                expression: calcField.id.expressionSimple,
                nature: calcField.id.nature,
                format: calcField.id.format
            } as any
            return formatField
        },
        createCalcFieldColumns() {
            this.calcFieldColumns = []
            this.selectedQuery.fields.forEach((field) => {
                field.type != 'inline.calculated.field' ? this.calcFieldColumns.push({ fieldAlias: `$F{${field.alias}}`, fieldLabel: field.alias }) : ''
            })
        },
        onCalcFieldSave(calcFieldOutput) {
            var calculatedField = {} as any
            this.selectedCalcField.alias = calcFieldOutput.colName
            this.selectedCalcField.expression = calcFieldOutput.formula

            if (this.selectedCalcField.uniqueID) {
                this.updateExistingCalculatedField(this.selectedCalcField)
            } else {
                calculatedField = buildCalculatedField(this.selectedCalcField, this.selectedQuery.fields)
                calculatedField.uniqueID = cryptoRandomString({ length: 4, type: 'base64' })
                this.selectedQuery.fields.push(calculatedField)
                this.addEntityToMainQuery(calculatedField, true)
                this.addCalculatedFieldMetadata(calculatedField)
            }

            this.calcFieldDialogVisible = false
        },
        addCalculatedFieldMetadata(calculatedField) {
            var newMetadata = {
                uniqueID: calculatedField.uniqueID,
                column: calculatedField.alias,
                fieldAlias: calculatedField.field,
                fieldType: calculatedField.id.nature.toUpperCase(),
                decrypt: false,
                personal: false,
                subjectId: false
            } as any

            switch (calculatedField.id.type) {
                case 'STRING':
                    newMetadata.Type = 'java.lang.String'
                    break
                case 'NUMBER':
                    newMetadata.Type = 'java.lang.Long'
                    break
                case 'DATE':
                    newMetadata.Type = 'java.sql.Date'
                    break
            }
            this.qbeMetadata.push(newMetadata)
        },
        updateExistingCalculatedField(calculatedField) {
            let fieldToEditIndex = this.selectedQuery.fields.findIndex((field) => field.uniqueID == calculatedField.uniqueID)
            this.selectedQuery.fields[fieldToEditIndex] = updateCalculatedField(this.selectedQuery.fields[fieldToEditIndex], this.selectedCalcField, this.selectedQuery.fields)
            this.updateCalculatedFieldMetadata(this.selectedQuery.fields[fieldToEditIndex])
        },
        updateCalculatedFieldMetadata(calculatedField) {
            let metaToEditIndex = this.qbeMetadata.findIndex((meta) => meta.uniqueID == calculatedField.uniqueID)

            this.qbeMetadata[metaToEditIndex].alias = calculatedField.alias
            this.qbeMetadata[metaToEditIndex].column = calculatedField.alias
            this.qbeMetadata[metaToEditIndex].fieldAlias = calculatedField.field
            this.qbeMetadata[metaToEditIndex].fieldType = calculatedField.id.nature.toUpperCase()
            this.qbeMetadata[metaToEditIndex].format = calculatedField.format
            switch (calculatedField.id.type) {
                case 'STRING':
                    this.qbeMetadata[metaToEditIndex].Type = 'java.lang.String'
                    break
                case 'NUMBER':
                    this.qbeMetadata[metaToEditIndex].Type = 'java.lang.Long'
                    break
                case 'DATE':
                    this.qbeMetadata[metaToEditIndex].Type = 'java.sql.Date'
                    break
            }
        },
        //#endregion ================================================================================================

        //#region ===================== Mini Menu Handler ==========================================================
        showMenu(event) {
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        createMenuItems() {
            this.menuButtons = []
            let repetitionIcon = this.discardRepetitions ? 'fas fa-check' : 'fas fa-times'
            this.menuButtons.push(
                { key: '1', label: this.$t('qbe.detailView.toolbarMenu.sql'), command: () => this.showSQLQuery() },
                { key: '2', icon: repetitionIcon, label: this.$t('qbe.detailView.toolbarMenu.repetitions'), command: () => this.toggleDiscardRepetitions() },
                { key: '3', label: this.$t('common.parameters'), command: () => this.showParamDialog() },
                { key: '4', label: this.$t('components.knCalculatedField.title'), visible: !this.smartView && this.selectedQuery.fields.length > 0, command: () => this.createNewCalcField() },
                { key: '5', label: this.$t('qbe.advancedFilters.advancedFilterVisualisation'), command: () => this.showAdvancedFilters() },
                { key: '6', label: this.$t('qbe.joinDefinitions.title'), command: () => this.showJoinDefinitions() },
                {
                    key: '7',
                    label: this.$t('qbe.detailView.toolbarMenu.exportTo'),
                    items: [
                        { label: 'CSV', command: () => this.exportQueryResults('csv') },
                        { label: 'XLSX', command: () => this.exportQueryResults('xlsx') }
                    ]
                }
            )
        },
        async showSQLQuery() {
            var item = {} as any
            item.catalogue = JSON.stringify(this.qbe?.qbeJSONQuery?.catalogue?.queries)
            item.currentQueryId = 'q1'
            item.ambiguousFieldsPaths = []
            item.ambiguousRoles = []
            item.pars = this.qbe?.pars

            let conf = {} as any
            conf.headers = { 'Content-Type': 'application/x-www-form-urlencoded' } as any
            conf.transformRequest = function (obj) {
                var str = [] as any
                for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                return str.join('&')
            }

            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=SET_CATALOGUE_ACTION&SBI_EXECUTION_ID=${this.uniqueID}`, item, conf)
                .then(() => {
                    this.getSQL()
                })
                .catch((error) => {
                    this.store.setError({ title: this.$t('common.toast.error'), msg: error.errors[0].message })
                })
        },
        async getSQL() {
            var item = {} as any
            item.replaceParametersWithQuestion = true
            item.queryId = this.selectedQuery.id

            let conf = {} as any
            conf.headers = { 'Content-Type': 'application/x-www-form-urlencoded' } as any
            conf.transformRequest = function (obj) {
                var str = [] as any
                for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                return str.join('&')
            }

            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_SQL_QUERY_ACTION&SBI_EXECUTION_ID=${this.uniqueID}`, item, conf)
                .then((response: AxiosResponse<any>) => {
                    this.sqlData = response.data
                    this.sqlDialogVisible = true
                })
                .catch((error) => {
                    this.store.setError({ title: this.$t('common.toast.error'), msg: error.errors[0].message })
                })
        },
        toggleDiscardRepetitions() {
            this.discardRepetitions = !this.discardRepetitions
            this.qbe ? (this.qbe.qbeJSONQuery.catalogue.queries[0].distinct = this.discardRepetitions) : ''
            if (this.smartView) {
                this.executeQBEQuery(false)
            }
        },
        showParamDialog() {
            this.paramDialogVisible = true
        },
        showAdvancedFilters() {
            this.advancedFilterDialogVisible = true
        },
        showJoinDefinitions() {
            this.joinDefinitionDialogVisible = true
        },
        async exportQueryResults(mimeType) {
            var fileName = ''
            mimeType == 'csv' ? (fileName = 'report.csv') : (fileName = 'report.xlsx')

            const postData = { catalogue: this.qbe?.qbeJSONQuery?.catalogue.queries, meta: this.formatQbeMeta(), pars: this.qbe?.pars, qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(import.meta.env.VITE_QBE_PATH + `qbequery/export/?SBI_EXECUTION_ID=${this.uniqueID}&currentQueryId=${this.selectedQuery.id}&outputType=${mimeType}`, postData, { headers: { Accept: 'application/json, text/plain, */*' }, responseType: 'blob' })
                .then((response: AxiosResponse<any>) => {
                    downloadDirect(response.data, fileName, response.headers['content-type'])
                })
                .catch(() => {})
        },
        //#endregion ================================================================================================

        //#region ===================== Event Handlers ==============================================================
        async updatePagination(lazyParams: any) {
            this.pagination.start = lazyParams.paginationStart
            this.pagination.limit = lazyParams.paginationLimit
            await this.executeQBEQuery(false)
        },
        toggleEntitiesLists() {
            this.showEntitiesLists = !this.showEntitiesLists
        },
        collapseDerivedList() {
            this.showDerivedList = !this.showDerivedList
        },
        checkIfHiddenColumnsExist() {
            if (this.qbe) {
                this.hiddenColumnsExist = false
                for (let i = 0; i < this.selectedQuery.fields.length; i++) {
                    if (!this.selectedQuery.fields[i].visible) {
                        this.hiddenColumnsExist = true
                        break
                    }
                }
            }
        },
        showHiddenColumns() {
            this.selectedQuery.fields.forEach((field: iField) => (field.visible = true))
            if (this.smartView) {
                this.updateSmartView()
            }
            this.hiddenColumnsExist = false
        },
        openFilterDialog(field: iField) {
            this.filterDialogData = { field: field, query: this.selectedQuery }
            this.filterDialogVisible = true
        },
        openHavingDialog(payload: { field: iField; query: iQuery }) {
            this.havingDialogData = payload
            this.havingDialogVisible = true
        },
        onFiltersSave(filters: iFilter[], field: iField, parameters: any[], expression: any) {
            onFiltersSaveCallback(filters, field, parameters, expression, this.qbe, this.selectedQuery, this.smartView, this.executeQBEQuery)
            this.filterDialogVisible = false
        },
        onAdvancedFiltersSave(expression: any) {
            this.selectedQuery.expression = expression
            this.advancedFilterDialogVisible = false
            this.updateSmartView()
        },

        showRelationDialog(entity) {
            this.relationEntity = entity
            this.relationDialogVisible = true
        },
        onHavingsSave(havings: iFilter[], field: iField) {
            onHavingsSaveCallback(havings, this.qbe, this.selectedQuery, field)
            this.havingDialogVisible = false
        },
        onGroupingChanged(field: iField) {
            if (field.group && this.selectedQuery) {
                this.selectedQuery.havings = this.selectedQuery.havings.filter((having: any) => having.letOperandValue !== field.id)
            }
        },

        onJoinDefinitionDialogClose() {
            this.joinDefinitionDialogVisible = false
            if (this.smartView) {
                this.executeQBEQuery(false)
            }
        },
        deleteAllFilters() {
            if (this.qbe) {
                this.selectedQuery.filters = []
                this.selectedQuery.havings = []
                this.selectedQuery.expression = {}
                if (this.smartView) this.executeQBEQuery(false)
            }
        },

        selectSubquery(subquery) {
            this.selectedQuery = subquery
            this.updateSmartView()
        },
        selectMainQuery() {
            if (this.selectedQuery.fields.length < 1) {
                this.store.setInfo({ title: this.$t('common.toast.error'), msg: 'Sub entities must have one and one only field' })
            } else {
                this.selectedQuery = this.mainQuery
            }
            this.updateSmartView()
        },
        deleteSubquery(index, subquery) {
            subquery.id === this.selectedQuery.id ? (this.selectedQuery = this.mainQuery) : ''
            this.mainQuery.subqueries.splice(index, 1)
            this.updateSmartView()
        },
        createSubquery() {
            let newSubquery = { id: 'q' + this.createQueryName(), name: 'subentity-q' + this.createQueryName(), fields: [], distinct: false, filters: [], calendar: {}, expression: {}, isNestedExpression: false, havings: [], graph: [], relationRoles: [], subqueries: [] } as any
            this.mainQuery.subqueries.push(newSubquery)
            this.selectedQuery = newSubquery
            this.queryPreviewData = null as any
        },
        createQueryName() {
            var lastcount = 0
            var lastIndex = this.mainQuery.subqueries?.length - 1
            if (lastIndex != -1 && this.mainQuery.subqueries) {
                var lastQueryId = this.mainQuery.subqueries[lastIndex].id
                lastcount = parseInt(lastQueryId.substr(1))
            } else {
                lastcount = 1
            }
            return lastcount + 1
        },
        async onExecute(qbeParameters: any[]) {
            if (this.qbe) {
                this.qbe.pars = [...qbeParameters]
                if (this.dataset && !this.dataset.dataSourceId && !this.dataset.federation_id) {
                    await this.loadDataset()
                } else {
                    this.qbe = this.getQBEFromModel()
                }
                await this.loadQBE()
                this.loadQuery()
                this.qbeMetadata = this.extractFieldsMetadata(this.qbe?.meta?.columns)
                this.generateFieldsAndMetadataId()
                this.parameterSidebarVisible = false
            }
        },
        async openPreviewDialog() {
            this.pagination.limit = 20
            await this.executeQBEQuery(true)
        },
        updateSmartView() {
            this.smartView && this.selectedQuery.fields.length > 0 ? this.executeQBEQuery(false) : this.resetQueryPreviewAndPagination()
        },
        resetQueryPreviewAndPagination() {
            this.pagination = { start: 0, limit: 25, size: 0 }
            this.queryPreviewData = {} as any
        },
        smartViewFieldHidden() {
            this.checkIfHiddenColumnsExist()
            this.updateSmartView()
        },
        smartViewReorder(event) {
            var temp = this.selectedQuery.fields[event.dragIndex]
            this.selectedQuery.fields[event.dragIndex] = this.selectedQuery.fields[event.dropIndex]
            this.selectedQuery.fields[event.dropIndex] = temp
            this.updateSmartView()
        },
        closePreview() {
            this.qbePreviewDialogVisible = false
            this.pagination = { start: 0, limit: 25 }
        },
        onFieldAliasChange(field) {
            this.qbeMetadata.findIndex((metadata) => {
                if (metadata.uniqueID === field.uniqueID) metadata.fieldAlias = field.alias
            })
            this.updateSmartView()
        },
        onParametersSave() {
            this.paramDialogVisible = false

            for (let i = this.selectedQuery.filters.length - 1; i >= 0; i--) {
                if (this.selectedQuery.filters[i].rightType === 'parameter') {
                    const index = this.qbe?.pars.findIndex((parameter: any) => parameter.name === this.selectedQuery.filters[i].paramName)
                    if (index === -1) {
                        removeInPlace(this.selectedQuery.expression, '$F{' + this.selectedQuery.filters[i].filterId + '}')
                        this.selectedQuery.filters.splice(index, 1)
                    }
                }
            }

            if (this.selectedQuery.expression.childNodes?.length === 0) this.selectedQuery.expression = {}
            this.updateSmartView()
        },
        async onRoleChange(role: string) {
            this.userRole = role as any
            this.filtersData = {}
            if (this.dataset && !this.dataset.dataSourceId && !this.dataset.federation_id) {
                await this.loadDataset()
            } else {
                this.qbe = this.getQBEFromModel()
            }
            this.loadQuery()
            if (!this.dataset?.federation_id) await this.loadDatasetDrivers()
        },
        //#endregion ================================================================================================
        openSavingDialog() {
            if (this.returnQueryMode) {
                this.$emit('querySaved', this.qbe?.qbeJSONQuery)
            } else {
                this.savingDialogVisible = true
            }
        }
    }
})
</script>
<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{54px});
    margin: 0;
    .p-dialog-content {
        padding: 0;
        margin: 0;
        flex: 1;
        overflow: hidden;
    }
    .p-dialog-header {
        padding: 0;
        margin: 0;
    }
}
.qbe-detail-view {
    display: flex;
    flex-direction: column;
    flex: 3;
}
.derived-entities-toggle {
    height: 25%;
}
.qbe-scroll-panel .p-scrollpanel-content {
    padding: 0 !important;
}
.qbe-scroll-panel .p-scrollpanel-bar {
    background-color: #43749eb6;
    width: 5px;
}
</style>
