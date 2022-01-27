<template>
    <Dialog v-if="!loading" class="full-screen-dialog" :visible="true" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #left>
                    <span>{{ qbe?.label }}</span>
                </template>
                <template #right>
                    <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.filter')" />
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="savingDialogVisible = true" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />
        <div v-if="!loading" class="p-d-flex p-flex-row kn-height-full">
            <div v-show="showEntitiesLists" class="entities-lists">
                <div class="p-d-flex p-flex-column kn-flex kn-overflow-hidden">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0">
                        <template #left>
                            <span>Entities</span>
                        </template>
                        <template #right>
                            <Chip style="background-color:white"> {{ entities.entities.length }} </Chip>
                        </template>
                    </Toolbar>
                    <div class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full olap-scroll-panel">
                            <ExpandableEntity :availableEntities="entities.entities" :query="mainQuery" @showRelationDialog="showRelationDialog" @entityClicked="onDropComplete($event, false)" @entityChildClicked="onDropComplete($event, false)" @openFilterDialog="openFilterDialog" />
                        </ScrollPanel>
                    </div>
                </div>
                <div class="p-d-flex p-flex-column kn-overflow-hidden" :class="{ 'derived-entities-toggle': showDerivedList }">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary kn-flex-0">
                        <template #left>
                            <span>Derived Entities</span>
                        </template>
                        <template #right>
                            <Button v-if="showEntitiesLists" icon="fas fa-plus-circle" class="p-button-text p-button-rounded p-button-plain" v-tooltip.top="$t('common.add')" @click="createSubquery" />
                            <Chip style="background-color:white"> {{ mainQuery.subqueries.length }} </Chip>
                            <Button v-if="showDerivedList" icon="pi pi-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="collapseDerivedList" />
                            <Button v-else icon="pi pi-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="collapseDerivedList" />
                        </template>
                    </Toolbar>
                    <div v-show="showDerivedList" class="kn-flex kn-overflow-hidden">
                        <ScrollPanel class="kn-height-full olap-scroll-panel"> <SubqueryEntity :availableEntities="mainQuery.subqueries" @editSubquery="selectSubquery" @deleteSubquery="deleteSubquery" /> </ScrollPanel>
                    </div>
                </div>
            </div>
            <div class="detail-view p-m-1" v-if="qbe">
                <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                    <template #left>
                        <Button v-if="showEntitiesLists" icon="pi pi-chevron-left" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.hideList')" @click="toggleEntitiesLists" />
                        <Button v-else icon="pi pi-chevron-right" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('qbe.detailView.showList')" @click="toggleEntitiesLists" />
                        <span v-if="selectedQuery.id !== 'q1'">
                            <Button label="MAIN" class="p-button-text p-button-plain kn-uppercase" @click="selectMainQuery" />
                            <Button icon="pi pi-chevron-right" class="p-button-text p-button-plain" />
                            <Button :label="selectedQuery?.name" class="p-button-text p-button-plain kn-uppercase" />
                        </span>
                    </template>
                    <template #right>
                        <i v-if="selectedQuery.fields.length > 0" class="fas fa-eraser kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.deleteAllSelectedFields')" @click="deleteAllSelectedFields"></i>
                        <i v-if="hiddenColumnsExist" class="pi pi-eye kn-cursor-pointer p-mx-2" v-tooltip.top="$t('qbe.viewToolbar.showHiddenColumns')" @click="showHiddenColumns"></i>
                        <span v-if="selectedQuery.filters.length > 0" class="fa-stack p-mx-2">
                            <i class="fas fa-ban fa-stack-2x"></i>
                            <i class="fas fa-filter fa-stack-1x kn-cursor-pointer" v-tooltip.top="$t('qbe.viewToolbar.deleteAllFilters')" @click="deleteAllFilters"></i>
                        </span>
                        <InputSwitch class="p-mr-2" v-model="smartView" />
                        <span>{{ $t('qbe.viewToolbar.smartView') }}</span>
                        <Button icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" @click="showMenu" />
                    </template>
                </Toolbar>
                <div class="kn-flex kn-overflow-y">
                    <QBESimpleTable v-if="!smartView" :query="selectedQuery" @columnVisibilityChanged="checkIfHiddenColumnsExist" @openFilterDialog="openFilterDialog" @openHavingDialog="openHavingDialog" @entityDropped="onDropComplete($event, false)"></QBESimpleTable>
                </div>
            </div>
        </div>

        <QBEFilterDialog :visible="filterDialogVisible" :filterDialogData="filterDialogData" :id="id" :entities="entities?.entities" :propParameters="qbe?.pars" :propExpression="selectedQuery.expression" @close="filterDialogVisible = false" @save="onFiltersSave"></QBEFilterDialog>
        <QBESqlDialog :visible="sqlDialogVisible" :sqlData="sqlData" @close="sqlDialogVisible = false" />
        <QBERelationDialog :visible="relationDialogVisible" :propEntity="relationEntity" @close="relationDialogVisible = false" />
        <QBEParamDialog v-if="paramDialogVisible" :visible="paramDialogVisible" :propDataset="qbe" @close="paramDialogVisible = false" />
        <QBEHavingDialog :visible="havingDialogVisible" :havingDialogData="havingDialogData" @close="havingDialogVisible = false" @save="onHavingsSave"></QBEHavingDialog>
        <QBEAdvancedFilterDialog :visible="advancedFilterDialogVisible" :query="selectedQuery" @close="advancedFilterDialogVisible = false"></QBEAdvancedFilterDialog>
        <QBESavingDialog v-if="savingDialogVisible" :visible="savingDialogVisible" :propDataset="qbe" @close="savingDialogVisible = false" />
        <QBEJoinDefinitionDialog :visible="joinDefinitionDialogVisible" :qbe="qbe" :propEntities="entities?.entities" :id="id" :selectedQuery="selectedQuery" @close="joinDefinitionDialogVisible = false"></QBEJoinDefinitionDialog>
        <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import { iQBE, iQuery, iField, iQueryResult, iFilter } from './QBE'
import { findByName, replace, removeInPlace } from './qbeDialogs/qbeAdvancedFilterDialog/treeService'
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
import ExpandableEntity from '@/modules/qbe/qbeComponents/expandableEntity.vue'
import SubqueryEntity from '@/modules/qbe/qbeComponents/subqueryEntity.vue'
import ScrollPanel from 'primevue/scrollpanel'
import Menu from 'primevue/contextmenu'
import QBEJoinDefinitionDialog from './qbeDialogs/qbeJoinDefinitionDialog/QBEJoinDefinitionDialog.vue'

export default defineComponent({
    name: 'qbe',
    components: { Dialog, Chip, InputSwitch, ScrollPanel, Menu, QBEFilterDialog, QBESavingDialog, QBESqlDialog, QBESimpleTable, QBERelationDialog, QBEParamDialog, ExpandableEntity, SubqueryEntity, QBEHavingDialog, QBEAdvancedFilterDialog, QBEJoinDefinitionDialog },
    props: { visible: { type: Boolean }, id: { type: String } },
    emits: ['close'],
    data() {
        return {
            qbe: null as iQBE | null,
            qbeId: '' as string,
            customizedDatasetFunctions: {} as any,
            exportLimit: null as number | null,
            entities: {} as any,
            queryResult: {} as iQueryResult,
            selectedQuery: {} as any, //editQueryObj u njihovom appu
            mainQuery: {} as any, //scope.query u njihovom appu
            loading: false,
            showEntitiesLists: true,
            smartView: false,
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
            joinDefinitionDialogVisible: false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadDataset()
            await this.loadId()
            await this.loadCustomizedDatasetFunctions()
            await this.loadExportLimit()
            await this.loadEntities()
            await this.executeQBEQuery()
            this.loading = false
        },
        async loadDataset() {
            // HARDCODED Dataset label/name
            // await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Bojan`).then((response: AxiosResponse<any>) => {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Darko%20QBE%20Test`).then((response: AxiosResponse<any>) => {
                this.qbe = response.data[0]
                if (this.qbe) this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            })
            console.log('LOADED QBE Dataset: ', this.qbe)
            console.log('MAIN QUERY q1 : ', this.qbe?.qbeJSONQuery?.catalogue?.queries[0])
            console.log('SUBQUERIES of q1: ', this.qbe?.qbeJSONQuery?.catalogue?.queries[0].subqueries)
            this.mainQuery = this.qbe?.qbeJSONQuery?.catalogue?.queries[0]
            this.selectedQuery = this.qbe?.qbeJSONQuery?.catalogue?.queries[0]
        },
        async loadId() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/qbe-execution-id`).then((response: AxiosResponse<any>) => {
                this.qbeId = response.data
            })
            console.log('LOADED ID: ', this.id)
        },
        async loadCustomizedDatasetFunctions() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${this.qbe?.qbeDataSourceId}`).then((response: AxiosResponse<any>) => (this.customizedDatasetFunctions = response.data))
            // console.log('LOADED CUSTOMIZED DATASET FUNCTIONS: ', this.customizedDatasetFunctions)
        },
        async loadExportLimit() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/EXPORT.LIMITATION`).then((response: AxiosResponse<any>) => (this.exportLimit = response.data))
            // console.log('LOADED EXPORT LIMIT: ', this.exportLimit)
        },
        async loadEntities() {
            // HARDCODED SBI_EXECUTION_ID
            await this.$http
                .get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=${this.id}&datamartName=null`)
                // .get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=${this.qbeId}&datamartName=null`)
                .then((response: AxiosResponse<any>) => (this.entities = response.data))
                .catch((error: any) => console.log('ERROR: ', error))
            console.log('LOADED ENTITIES: ', this.entities)
        },
        async executeQBEQuery() {
            // HARDCODED a lot
            if (!this.qbe) return

            const postData = { catalogue: this.qbe?.qbeJSONQuery.catalogue.queries, meta: this.formatQbeMeta(), pars: this.qbe?.pars, qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(process.env.VUE_APP_QBE_PATH + `qbequery/executeQuery/?SBI_EXECUTION_ID=${this.id}&currentQueryId=q1&start=0&limit=25`, postData)
                .then((response: AxiosResponse<any>) => (this.queryResult = response.data))
                .catch(() => {})
            // console.log('QUERY RESULT : ', this.queryResult)
        },
        formatQbeMeta() {
            const meta = [] as any[]
            this.qbe?.qbeJSONQuery.catalogue.queries?.forEach((query: iQuery) => {
                query.fields?.forEach((field: iField) => {
                    meta.push({ dataType: field.dataType, displayedName: field.alias, fieldType: field.fieldType.toUpperCase(), format: field.format, name: field.alias, type: field.type })
                })
            })
            return meta
        },
        toggleEntitiesLists() {
            this.showEntitiesLists = !this.showEntitiesLists
        },
        collapseDerivedList() {
            this.showDerivedList = !this.showDerivedList
        },
        deleteAllSelectedFields() {
            this.selectedQuery.fields = []
        },
        checkIfHiddenColumnsExist() {
            if (this.qbe) {
                this.hiddenColumnsExist = false
                for (let i = 0; i < this.selectedQuery.fields.length; i++) {
                    console.log(' >>> FIELD: ', this.selectedQuery.fields[i])
                    if (!this.selectedQuery.fields[i].visible) {
                        this.hiddenColumnsExist = true
                        break
                    }
                }
            }
        },
        showHiddenColumns() {
            this.selectedQuery.fields.forEach((field: iField) => (field.visible = true))
            this.hiddenColumnsExist = false
        },
        openFilterDialog(field: iField) {
            console.log('PAYLOAD FOR OPEN FILTER: ', { field: field, query: this.selectedQuery })
            this.filterDialogData = { field: field, query: this.selectedQuery }
            this.filterDialogVisible = true
        },
        openHavingDialog(payload: { field: iField; query: iQuery }) {
            console.log('QBE - PAYLOAD FOR OPEN HAVING DIALOG: ', payload)
            this.havingDialogData = payload
            this.havingDialogVisible = true
        },
        onFiltersSave(filters: iFilter[], field: iField, parameters: any[], expression: any) {
            console.log('ON FILTERS SAVE: ', filters)
            console.log('ON FILTERS SAVE PARAMETERS: ', parameters)
            console.log('QBE QUERY BEFORE FILTERS SAVED: ', this.selectedQuery)
            console.log('FIELD ON FILTER SAVE: ', field)
            if (!this.qbe) return

            for (let i = 0; i < filters.length; i++) {
                const tempFilter = filters[i]
                const index = this.selectedQuery.filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
                console.log('INDEX: ', index)
                if (index !== -1) {
                    this.selectedQuery.filters[index] = tempFilter
                } else {
                    this.selectedQuery.filters.push(tempFilter)
                }
            }

            this.removeDeletedFilters(filters, field, expression)

            // this.selectedQuery.expression = this.createExpression()
            this.refresh(this.selectedQuery.filters, expression)
            this.qbe.pars = parameters ? [...parameters] : []
            this.filterDialogVisible = false
            console.log('QBE QUERY AFTER FILTERS SAVED: ', this.selectedQuery)
        },
        refresh(filters: iFilter[], expression: any) {
            console.log('REFRESH FILTERS: ', filters)
            console.log('REFRESH expression: ', expression)
            if (!this.qbe) return
            for (let filter of filters) {
                // var newConst = new Const('NODE_CONST', filter)
                var newConst = {
                    value: '$F{' + filter.filterId + '}',
                    childNodes: [],
                    details: {
                        leftOperandAlias: filter.leftOperandAlias,
                        operator: filter.operator,
                        entity: filter.entity,
                        rightOperandValue: filter.rightOperandValue.join(', ')
                    }
                }
                var oldConst = findByName(expression, newConst.value)

                replace(expression, newConst, oldConst)
            }
            this.selectedQuery.expression = expression
            console.log('AFTER NEW SAVE: ', this.selectedQuery)
            this.filterDialogVisible = false
        },
        removeDeletedFilters(filters: iFilter[], field: iField, expression: any) {
            if (!this.qbe) return

            // console.log(' >>> BLA: ', this.qbe.qbeJSONQuery.catalogue.queries[0].filters)

            for (let i = this.selectedQuery.filters.length - 1; i >= 0; i--) {
                const tempFilter = this.selectedQuery.filters[i]
                console.log(' >>> TEMP FILTER: ', tempFilter)
                if (tempFilter.leftOperandValue === field.id) {
                    console.log(' >>> FILTER FOR DELETE CHECK: ', tempFilter)
                    const index = filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
                    console.log('  >>> INDEX: ', index)
                    if (index === -1) {
                        this.selectedQuery.filters.splice(i, 1)
                        removeInPlace(expression, '$F{' + tempFilter.filterId + '}')
                    }
                    // this.deleteFilterByProperty('filterId', tempFilter.filterId, this.selectedQuery.filters, expression)
                }
            }
        },
        // deleteFilterByProperty(propertyName: string, propertyValue: string, filters: iFilter[], expression: any) {
        //     for (var i = 0; i < filters.length; i++) {
        //         if (filters[i][propertyName] != undefined && filters[i][propertyName] == propertyValue) {
        //             filters.splice(i, 1)
        //             removeInPlace(expression, '$F{' + propertyValue + '}')
        //             i--
        //         }
        //     }
        // },
        // createExpression() {
        //     const formattedFilters = {}

        //     this.qbe?.qbeJSONQuery.catalogue.queries[0].filters.forEach((filter: iFilter) => {
        //         if (formattedFilters[filter.leftOperandValue]) {
        //             formattedFilters[filter.leftOperandValue].push(filter)
        //         } else {
        //             formattedFilters[filter.leftOperandValue] = [filter]
        //         }
        //     })

        //     console.log('createExpression - FORMATED FILTERS: ', formattedFilters)

        //     let expression = { childNodes: [] } as any
        //     Object.keys(formattedFilters).forEach((key: string) => {
        //         console.log('createExpression - expression: ', this.getExpression(formattedFilters[key]))
        //         expression.childNodes.push(this.getExpression(formattedFilters[key]))
        //     })

        //     if (expression.childNodes.length === 0) {
        //         expression = {}
        //     } else if (expression.childNodes.length > 1) {
        //         expression.value = 'AND'
        //         expression.type = 'NODE_OP'
        //     }
        //     console.log('createExpression - FINAL EXPRESSION: ', expression)
        //     return expression
        // },
        getExpression(filters: iFilter[]) {
            console.log('FILTERS FOR EXPRESISON: ', filters)
            const filtersLength = filters.length

            let expression = {} as any
            if (filtersLength === 0) {
                return expression
            } else if (filters.length === 1) {
                const tempFilter = filters[0]
                expression = {
                    type: 'NODE_CONST',
                    childNodes: [],
                    value: '$F{' + tempFilter.filterId + '}',
                    details: {
                        leftOperandAlias: tempFilter.leftOperandAlias,
                        operator: tempFilter.operator,
                        entity: tempFilter.entity,
                        rightOperandValue: tempFilter.rightOperandValue[0]
                    }
                }
            } else {
                expression = { type: 'NODE_OP', childNodes: [], value: 'AND' }
                filters.forEach((filter: iFilter) =>
                    expression.childNodes.push({
                        type: 'NODE_CONST',
                        childNodes: [],
                        value: '$F{' + filter.filterId + '}',
                        details: {
                            leftOperandAlias: filter.leftOperandAlias,
                            operator: filter.operator,
                            entity: filter.entity,
                            rightOperandValue: filter.rightOperandValue[0]
                        }
                    })
                )
            }

            return expression
        },
        showMenu(event) {
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        showRelationDialog(entity) {
            console.log(entity)
            this.relationEntity = entity
            this.relationDialogVisible = true
        },
        showParamDialog() {
            this.paramDialogVisible = true
        },
        createMenuItems() {
            this.menuButtons = []
            let repetitionIcon = this.discardRepetitions ? 'fas fa-check' : 'fas fa-times'
            this.menuButtons.push(
                { key: '1', label: this.$t('qbe.detailView.toolbarMenu.sql'), command: () => this.showSQLQuery() },
                { key: '2', icon: repetitionIcon, label: this.$t('qbe.detailView.toolbarMenu.repetitions'), command: () => this.toggleDiscardRepetitions() },
                { key: '3', label: this.$t('common.parameters'), command: () => this.showParamDialog() },
                { key: '4', label: this.$t('qbe.advancedFilters.advancedFilterVisualisation'), command: () => this.showAdvancedFilters() },
                { key: '5', label: this.$t('qbe.joinDefinitions.title'), command: () => this.showJoinDefinitions() },
                {
                    key: '6',
                    label: this.$t('qbe.detailView.toolbarMenu.exportTo'),
                    items: [
                        { label: 'CSV', command: () => this.exportQueryResults('csv') },
                        { label: 'XLSX', command: () => this.exportQueryResults('xlsx') }
                    ]
                }
            )
        },
        async exportQueryResults(mimeType) {
            var fileName = ''
            var fileType = ''

            if (mimeType == 'csv') {
                fileName = 'report.csv'
                fileType = 'text/csv'
            } else if (mimeType == 'xlsx') {
                fileName = 'report.xlsx'
                fileType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            } else {
                console.log('Unsupported mime type: ', mimeType, fileName, fileType)
            }
            const postData = { catalogue: this.qbe?.qbeJSONQuery.catalogue.queries, meta: this.formatQbeMeta(), pars: this.qbe?.pars, qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http
                .post(process.env.VUE_APP_QBE_PATH + `qbequery/export/?SBI_EXECUTION_ID=${this.id}&currentQueryId=q2&outputType=${mimeType}`, postData, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    downloadDirect(response.data, fileName, fileType)
                })
                .catch(() => {})
        },
        toggleDiscardRepetitions() {
            this.discardRepetitions = !this.discardRepetitions
            this.qbe ? (this.qbe.qbeJSONQuery.catalogue.queries[0].distinct = this.discardRepetitions) : ''
            if (this.smartView) {
                // odmah pzovi servis i reloaduj prikaz, da bi radio kako treba currentQueryID unutar executeQBEQuery headera, ne sme da bude hardkodovan
                this.executeQBEQuery()
            }
        },
        // #region Havings
        onHavingsSave(havings: iFilter[], field: iField) {
            console.log('QBE - onHavingsSave() - havings: ', havings)
            console.log('QBE - onHavingsSave() - field: ', field)
            console.log('QBE - onHavingsSave() - QBE before havings saved: ', this.selectedQuery)

            if (!this.qbe) return

            for (let i = 0; i < havings.length; i++) {
                const tempFilter = havings[i]
                const index = this.qbe.qbeJSONQuery.catalogue.queries[0].filters.findIndex((el: iFilter) => el.filterId === tempFilter.filterId)
                console.log('QBE - onHavingsSave() - INDEX: ', index)
                if (index !== -1) {
                    this.qbe.qbeJSONQuery.catalogue.queries[0].havings[index] = tempFilter
                } else {
                    this.qbe.qbeJSONQuery.catalogue.queries[0].havings.push(tempFilter)
                }
            }

            this.removeDeletedHavings(havings, field)
            this.havingDialogVisible = false
            console.log('QBE - onHavingsSave() - QBE after havings saved: ', this.selectedQuery)
        },
        removeDeletedHavings(havings: iFilter[], field: iField) {
            if (!this.qbe) return

            console.log(' QBE - removeDeletedHavings() - Query Havings: ', this.qbe.qbeJSONQuery.catalogue.queries[0].havings)

            for (let i = this.qbe.qbeJSONQuery.catalogue.queries[0].havings.length - 1; i >= 0; i--) {
                const tempHaving = this.qbe.qbeJSONQuery.catalogue.queries[0].filters[i]
                console.log(' QBE - removeDeletedHavings() - tempHaving: ', tempHaving)
                if (tempHaving.leftOperandValue === field.id) {
                    console.log(' QBE - removeDeletedHavings() - Having for delete check: ', tempHaving)
                    const index = havings.findIndex((el: iFilter) => el.filterId === tempHaving.filterId)
                    if (index === -1) this.qbe.qbeJSONQuery.catalogue.queries[0].filters.splice(i, 1)
                    console.log(' QBE - removeDeletedHavings() - Having delete index: ', index)
                }
            }
        },
        // #endregion
        // #region Advanced Filters
        showAdvancedFilters() {
            this.advancedFilterDialogVisible = true
        },
        // #endregion
        // #region Join Definitions
        showJoinDefinitions() {
            this.joinDefinitionDialogVisible = true
        },
        // #endregion
        deleteAllFilters() {
            if (this.qbe) {
                this.qbe.qbeJSONQuery.catalogue.queries[0].filters = []
                this.qbe.qbeJSONQuery.catalogue.queries[0].expression = {}
            }
        },
        //#region ===================== TODO: sve sto se tice ovoga mora da se uradi bolje ====================================================
        async showSQLQuery() {
            //TODO: moramo da njih pitamo sta i cemu sluzi ovo je odvratno
            var item = {} as any
            item.catalogue = JSON.stringify(this.qbe?.qbeJSONQuery?.catalogue?.queries)
            item.currentQueryId = 'q1' //hardkoded i kod njih u source dode
            item.ambiguousFieldsPaths = [] //hardkoded i kod njih u source dode
            item.ambiguousRoles = [] //hardkoded i kod njih u source dode
            item.pars = this.qbe?.pars //hardcoded, ovo su dataset parametri VALJDA neam pojma

            console.log('QUERY SEND DATA: ', this.qbe?.qbeJSONQuery?.catalogue?.queries)

            let conf = {} as any
            conf.headers = { 'Content-Type': 'application/x-www-form-urlencoded' } as any
            conf.transformRequest = function(obj) {
                //ne znam sta radi ovo niti cemu sluzi, pitati voju
                var str = [] as any
                for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                return str.join('&')
            }

            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=SET_CATALOGUE_ACTION&SBI_EXECUTION_ID=${this.id}`, item, conf)
                .then((response: AxiosResponse<any>) => {
                    console.log('SET CATALOGUE ACTION - showSQLQuery', response.data)
                    this.getSQL()
                })
                .catch((error) => {
                    this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: error.errors[0].message })
                    console.log('showSQLQuery ---- ERROR', error)
                })
        },
        async getSQL() {
            var item = {} as any
            item.replaceParametersWithQuestion = true
            item.queryId = this.qbe?.qbeJSONQuery?.catalogue?.queries[0]?.id

            let conf = {} as any
            conf.headers = { 'Content-Type': 'application/x-www-form-urlencoded' } as any
            conf.transformRequest = function(obj) {
                //ne znam sta radi ovo niti cemu sluzi, pitati voju
                var str = [] as any
                for (var p in obj) str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]))
                return str.join('&')
            }

            await this.$http
                .post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_SQL_QUERY_ACTION&SBI_EXECUTION_ID=${this.id}`, item, conf)
                .then((response: AxiosResponse<any>) => {
                    console.log('GET_SQL_QUERY_ACTION - getSQL', response.data)
                    this.sqlData = response.data
                    this.sqlDialogVisible = true
                })
                .catch((error) => {
                    this.$store.commit('setError', { title: this.$t('common.toast.error'), msg: error.errors[0].message })
                    console.log('getSQL ---- ERROR', error)
                })
        },
        //#endregion ===============================================================================================

        //#region ===================== Drag&Drop za entitete  ====================================================
        onDropComplete(field) {
            if (field.connector) return
            if (field.children) {
                for (var i in field.children) {
                    this.addEntityToMainQuery(field.children[i])
                }
            } else {
                this.addEntityToMainQuery(field)
            }
        },
        addEntityToMainQuery(field, isCalcField?) {
            //addField kod njih
            let queryModel = this.selectedQuery.fields
            let editQueryObj = this.selectedQuery
            console.log('queryModel--------------', queryModel, 'editQueryObj--------------', editQueryObj)
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
                var newField = {
                    id: field.attributes.type === 'inLineCalculatedField' ? field.attributes.formState : field.id,
                    alias: field.attributes.field,
                    type: field.attributes.type === 'inLineCalculatedField' ? 'inline.calculated.field' : 'datamartField',
                    fieldType: field.attributes.iconCls,
                    entity: field.attributes.entity,
                    field: field.attributes.field,
                    funct: this.getFunct(field),
                    color: field.color,
                    group: this.getGroup(field),
                    order: 'NONE',
                    include: true,
                    // eslint-disable-next-line no-prototype-builtins
                    inUse: field.hasOwnProperty('inUse') ? field.inUse : true,
                    visible: true,
                    iconCls: field.iconCls,
                    dataType: field.dataType,
                    format: field.format,
                    longDescription: field.attributes.longDescription,
                    distinct: editQueryObj.distinct,
                    leaf: field.leaf
                } as any
            }
            // eslint-disable-next-line no-prototype-builtins
            if (!field.hasOwnProperty('id')) {
                newField.id = field.alias
                newField.alias = field.text
                newField.field = field.text
                newField.temporal = field.temporal
            }

            if (!isCalcField) {
                editQueryObj.fields.push(newField)
            }
        },
        getFunct(field) {
            if (this.isColumnType(field, 'measure') && field.aggtype) {
                return field.aggtype
            } else if (this.isColumnType(field, 'measure')) {
                return 'SUM'
            }
            return 'NONE'
        },
        getGroup(field) {
            return this.isColumnType(field, 'attribute') && !this.isDataType(field, 'com.vividsolutions.jts.geom.Geometry')
        },
        isDataType(field, dataType) {
            return field.dataType == dataType
        },
        isColumnType(field, columnType) {
            return field.iconCls == columnType || this.isCalculatedFieldColumnType(field, columnType)
        },
        isCalculatedFieldColumnType(inLineCalculatedField, columnType) {
            return this.isInLineCalculatedField(inLineCalculatedField) && inLineCalculatedField.attributes.formState.nature === columnType
        },
        isInLineCalculatedField(field) {
            return field.attributes.type === 'inLineCalculatedField'
        },
        // #endregion

        //#region ===================== Subquery logic  ====================================================
        selectSubquery(subquery) {
            this.selectedQuery = subquery
        },
        selectMainQuery() {
            console.log(this.selectedQuery)
            if (this.selectedQuery.fields.length < 1) {
                this.$store.commit('setInfo', { title: this.$t('common.toast.error'), msg: 'Sub entities must have one and one only field' })
            } else {
                this.selectedQuery = this.mainQuery
            }
        },
        deleteSubquery(index, subquery) {
            console.log(index, subquery)
            subquery.id === this.selectedQuery.id ? (this.selectedQuery = this.mainQuery) : ''
            this.mainQuery.subqueries.splice(index, 1)
        },
        createSubquery() {
            let newSubquery = { id: 'q' + this.createQueryName(), name: 'subentity-q' + this.createQueryName(), fields: [], distinct: false, filters: [], calendar: {}, expression: {}, isNestedExpression: false, havings: [], graph: [], relationRoles: [], subqueries: [] } as any
            this.mainQuery.subqueries.push(newSubquery)
            this.selectedQuery = newSubquery
        },
        createQueryName() {
            var lastcount = 0
            var lastIndex = this.mainQuery.subqueries.length - 1
            if (lastIndex != -1) {
                var lastQueryId = this.mainQuery.subqueries[lastIndex].id
                lastcount = parseInt(lastQueryId.substr(1))
            } else {
                lastcount = 1
            }
            return lastcount + 1
        }
        // #endregion
    }
})
</script>
<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}

.full-screen-dialog.p-dialog .p-dialog-header,
.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}

.full-screen-dialog.p-dialog .p-dialog-content {
    flex: 1;
}

.entities-lists {
    display: flex;
    flex-direction: column;
    flex: 1;
    border-right: 1px solid #ccc;
    height: 100%;
}

.detail-view {
    display: flex;
    flex-direction: column;
    flex: 3;
}

.derived-entities-toggle {
    height: 25%;
}

.olap-scroll-panel .p-scrollpanel-content {
    padding: 0 !important;
}
.olap-scroll-panel .p-scrollpanel-bar {
    background-color: #43749eb6;
    width: 5px;
}
</style>
