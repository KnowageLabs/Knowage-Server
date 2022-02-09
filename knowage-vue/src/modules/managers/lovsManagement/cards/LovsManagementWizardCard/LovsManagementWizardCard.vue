<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ toolbarTitle }}
                </template>
                <template #right>
                    <Button class="kn-button-sm p-button-text" :label="$t('managers.lovsManagement.preview')" :disabled="previewDisabled" @click="checkForDependencies(true)" />
                    <Button class="kn-button-sm p-button-text" :label="$t('managers.lovsManagement.test')" :disabled="previewDisabled" @click="onTestButtonClick" />
                    <Button v-if="lovType !== 'DATASET'" icon="fa fa-info-circle" class="p-button-text p-button-rounded p-button-plain" aria-label="Info" @click="infoDialogVisible = true" />
                    <Button icon="fa fa-users" class="p-button-text p-button-rounded p-button-plain" aria-label="Profiles" @click="profileAttributesDialogVisible = true" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <LovsManagementQuery v-if="lovType === 'QUERY'" :selectedLov="lov" :selectedQuery="selectedQuery" :datasources="datasources" :codeInput="codeInput" @touched="onTouched"></LovsManagementQuery>
            <LovsManagementScript v-else-if="lovType === 'SCRIPT'" :selectedLov="lov" :selectedScript="selectedScript" :listOfScriptTypes="listOfScriptTypes" @touched="onTouched"></LovsManagementScript>
            <LovsManagementFixedLovsTable v-else-if="lovType === 'FIX_LOV'" :listForFixLov="listForFixLov" @touched="$emit('touched')" @sorted="$emit('sorted', $event)"></LovsManagementFixedLovsTable>
            <LovsManagementJavaClassInput v-else-if="lovType === 'JAVA_CLASS'" :selectedJavaClass="selectedJavaClass" @touched="onTouched"></LovsManagementJavaClassInput>
            <LovsManagementDataset v-else-if="lovType === 'DATASET'" :dataset="selectedDataset" @selected="$emit('selectedDataset', $event)" />
        </template>
    </Card>
    <LovsManagementInfoDialog v-show="infoDialogVisible" :visible="infoDialogVisible" :infoTitle="infoTitle" :lovType="lov.itypeCd" @close="infoDialogVisible = false"></LovsManagementInfoDialog>
    <LovsManagementProfileAttributesList v-show="profileAttributesDialogVisible" :visible="profileAttributesDialogVisible" :profileAttributes="profileAttributes" @selected="setCodeInput($event)" @close="profileAttributesDialogVisible = false"></LovsManagementProfileAttributesList>
    <LovsManagementParamsDialog v-show="paramsDialogVisible" :visible="paramsDialogVisible" :dependenciesList="dependenciesList" :mode="paramsDialogMode" @preview="onPreview" @close="onParamsDialogClose" @test="onTest"></LovsManagementParamsDialog>
    <LovsManagementPreviewDialog v-show="previewDialogVisible" :visible="previewDialogVisible" :dataForPreview="dataForPreview" :pagination="pagination" @close="onPreviewClose" @pageChanged="previewLov($event, false, true)"></LovsManagementPreviewDialog>
    <LovsManagementTestDialog v-show="testDialogVisible" :visible="testDialogVisible" :selectedLov="lov" :testModel="treeListTypeModel" :testLovModel="testLovModel" :testLovTreeModel="testLovTreeModel" @close="testDialogVisible = false" @save="onTestSave($event)"></LovsManagementTestDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../LovsManagement'
import { lovProviderEnum } from '../../LovsManagementDetail.vue'
import X2JS from 'x2js'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import lovsManagementWizardCardDescriptor from './LovsManagementWizardCardDescriptor.json'
import LovsManagementQuery from './LovsManagementQuery/LovsManagementQuery.vue'
import LovsManagementScript from './LovsManagementScript/LovsManagementScript.vue'
import LovsManagementFixedLovsTable from './LovsManagementFixedLovsTable/LovsManagementFixedLovsTable.vue'
import LovsManagementJavaClassInput from './LovsManagementJavaClassInput/LovsManagementJavaClassInput.vue'
import LovsManagementInfoDialog from './LovsManagementInfoDialog/LovsManagementInfoDialog.vue'
import LovsManagementPreviewDialog from './LovsManagementPreviewDialog/LovsManagementPreviewDialog.vue'
import LovsManagementProfileAttributesList from './LovsManagementProfileAttributesList/LovsManagementProfileAttributesList.vue'
import LovsManagementTestDialog from './LovsManagementTestDialog/LovsManagementTestDialog.vue'
import LovsManagementParamsDialog from './LovsManagementParamsDialog/LovsManagementParamsDialog.vue'
import LovsManagementDataset from './LovsManagementDataset/LovsManagementDataset.vue'

export default defineComponent({
    name: 'lovs-management-wizard-card',
    components: {
        Card,
        LovsManagementParamsDialog,
        LovsManagementInfoDialog,
        LovsManagementQuery,
        LovsManagementScript,
        LovsManagementFixedLovsTable,
        LovsManagementJavaClassInput,
        LovsManagementDataset,
        LovsManagementPreviewDialog,
        LovsManagementProfileAttributesList,
        LovsManagementTestDialog
    },
    props: {
        selectedLov: { type: Object, required: true },
        selectedQuery: { type: Object },
        datasources: { type: Array, required: true },
        selectedScript: { type: Object },
        listOfScriptTypes: { type: Array },
        listForFixLov: { type: Array },
        selectedJavaClass: { type: Object },
        selectedDataset: { type: Object },
        profileAttributes: { type: Array },
        save: { type: Boolean },
        previewDisabled: { type: Boolean }
    },
    emits: ['touched', 'save', 'created', 'selectedDataset'],
    data() {
        return {
            lovsManagementWizardCardDescriptor,
            lov: {} as iLov,
            toolbarTitle: '',
            infoTitle: '',
            previewDialogVisible: false,
            infoDialogVisible: false,
            profileAttributesDialogVisible: false,
            codeInput: { code: null, changed: false } as any,
            dependenciesList: [] as any[],
            pagination: lovsManagementWizardCardDescriptor.defaultPagination as any,
            dataForPreview: {} as any,
            tableModelForTest: {} as any,
            testDialogVisible: false,
            testLovModel: [] as any,
            treeListTypeModel: {} as any,
            formatedValues: [],
            formatedDescriptionValues: [],
            formatedVisibleValues: [] as any[],
            formatedInvisibleValues: [] as any[],
            testLovTreeModel: [] as any[],
            paramsDialogVisible: false,
            operation: 'create',
            testValid: false,
            sendSave: false,
            dependenciesReady: false,
            touchedForTest: false,
            x2js: new X2JS(),
            paramsDialogMode: 'preview'
        }
    },
    watch: {
        lovType() {
            this.onLovTypeChanged()
        },
        selectedLov() {
            this.loadLov()
            this.onLovTypeChanged()
        },
        async save() {
            this.sendSave = true

            if (!this.touchedForTest) {
                this.buildTestTable()
                this.formatForTest()
            }
            if (this.testValid && !this.touchedForTest) {
                await this.handleSubmit(true)
            } else {
                await this.checkForDependencies(false)
            }
        }
    },
    computed: {
        lovType(): string {
            return this.selectedLov.itypeCd
        }
    },
    async created() {
        this.loadLov()
        this.onLovTypeChanged()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
            if (this.lov.id) {
                this.testValid = true
            }
        },
        onLovTypeChanged() {
            switch (this.lovType) {
                case 'SCRIPT':
                    this.toolbarTitle = this.$t('managers.lovsManagement.scriptWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoSyntax')
                    if (!this.lov.lovProviderJSON.SCRIPTLOV) {
                        this.lov.lovProviderJSON = {
                            SCRIPTLOV: {
                                'DESCRIPTION-COLUMN': '',
                                'INVISIBLE-COLUMNS': '',
                                LANGUAGE: '',
                                LOVTYPE: 'simple',
                                SCRIPT: '',
                                'TREE-LEVELS-COLUMNS': '',
                                'VALUE-COLUMN': '',
                                'VISIBLE-COLUMNS': ''
                            }
                        }
                    }
                    break
                case 'QUERY':
                    this.toolbarTitle = this.$t('managers.lovsManagement.queryWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoSyntax')
                    if (!this.lov.lovProviderJSON.QUERY) {
                        this.lov.lovProviderJSON = {
                            QUERY: {
                                CONNECTION: '',
                                'DESCRIPTION-COLUMN': '',
                                'INVISIBLE-COLUMNS': '',
                                STMT: '',
                                LOVTYPE: 'simple',
                                'VALUE-COLUMN': '',
                                'VISIBLE-COLUMNS': '',
                                decoded_STMT: ''
                            }
                        }
                    }
                    break
                case 'FIX_LOV':
                    this.toolbarTitle = this.$t('managers.lovsManagement.fixedListWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoRules')
                    if (!this.lov.lovProviderJSON.FIXLISTLOV) {
                        this.lov.lovProviderJSON = {
                            FIXLISTLOV: {
                                'DESCRIPTION-COLUMN': '',
                                ROWS: {},
                                'INVISIBLE-COLUMNS': '',
                                LABEL: '',
                                LOVTYPE: 'simple',
                                'VALUE-COLUMN': '',
                                'VISIBLE-COLUMNS': ''
                            }
                        }
                    }
                    break
                case 'JAVA_CLASS':
                    this.toolbarTitle = this.$t('managers.lovsManagement.javaClassWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoRules')
                    if (!this.lov.lovProviderJSON.JAVACLASSLOV) {
                        this.lov.lovProviderJSON = {
                            JAVACLASSLOV: {
                                'DESCRIPTION-COLUMN': '',
                                ROWS: {},
                                'INVISIBLE-COLUMNS': '',
                                LABEL: '',
                                LOVTYPE: 'simple',
                                'VALUE-COLUMN': '',
                                'VISIBLE-COLUMNS': ''
                            }
                        }
                    }
                    break
                case 'DATASET':
                    this.toolbarTitle = this.$t('managers.lovsManagement.datasetWizard')
                    if (!this.lov.lovProviderJSON.DATASET) {
                        this.lov.lovProviderJSON = {
                            DATASET: {
                                'DESCRIPTION-COLUMN': '',
                                ID: '',
                                'INVISIBLE-COLUMNS': '',
                                LABEL: '',
                                LOVTYPE: 'simple',
                                'VALUE-COLUMN': '',
                                'VISIBLE-COLUMNS': ''
                            }
                        }
                    }
            }
        },
        setCodeInput(event: any) {
            this.codeInput = { code: event, changed: !this.codeInput.changed }
        },
        async checkForDependencies(showPreview: boolean) {
            this.formatForTest()
            let listOfEmptyDependencies = [] as any[]

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/checkdependecies', { provider: this.x2js.js2xml(this.lov.lovProviderJSON) })
                .then((response: AxiosResponse<any>) => {
                    listOfEmptyDependencies = response.data
                })
                .catch((response: AxiosResponse<any>) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.errorTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.touchedForTest = false))

            if (listOfEmptyDependencies.length > 0 && !this.dependenciesReady) {
                this.dependenciesList = []
                for (let i = 0; i < listOfEmptyDependencies.length; i++) {
                    this.dependenciesList.push({
                        name: listOfEmptyDependencies[i].name,
                        type: listOfEmptyDependencies[i].type
                    })
                }
                this.paramsDialogMode = showPreview ? 'preview' : 'test'
                this.paramsDialogVisible = true
            } else {
                await this.previewLov(this.pagination, false, showPreview)
                this.buildTestTable()
            }
        },
        async previewLov(value: any, hasDependencies: boolean, showPreview: boolean) {
            this.pagination = value
            const postData = {
                data: {
                    ...this.lov,
                    lovProviderJSON: JSON.stringify(this.lov.lovProviderJSON),
                    lovProvider: this.x2js.js2xml(this.lov.lovProviderJSON)
                },
                pagination: this.pagination
            } as any

            if (hasDependencies || this.dependenciesReady) {
                postData.dependencies = this.dependenciesList
            }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/preview', postData)
                .then((response: AxiosResponse<any>) => {
                    if (response.status === 204) {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.errorTitle'),
                            msg: this.$t('managers.lovsManagement.syntaxError')
                        })
                        this.tableModelForTest = []
                    } else {
                        this.dataForPreview = response.data
                        this.tableModelForTest = response.data.metaData.fields
                        this.pagination.size = response.data.results
                        this.previewDialogVisible = showPreview
                        this.testDialogVisible = !showPreview
                        this.paramsDialogVisible = hasDependencies
                    }
                })
                .catch((response: AxiosResponse<any>) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.errorTitle'),
                        msg: response
                    })
                })
        },
        formatForTest() {
            const propName = this.lov.itypeCd
            const prop = lovProviderEnum[propName]

            if (!this.lov.lovProviderJSON[prop].LOVTYPE) {
                this.lov.lovProviderJSON[prop].LOVTYPE = 'simple'
            }

            if (!this.lov.id) {
                this.setLovProviderJsonValues(prop)
            }

            switch (prop) {
                case lovProviderEnum.QUERY:
                    this.lov.lovProviderJSON[prop].CONNECTION = this.selectedQuery?.datasource
                    this.lov.lovProviderJSON[prop].STMT = this.selectedQuery?.query
                    break
                case lovProviderEnum.SCRIPT:
                    this.lov.lovProviderJSON[prop].LANGUAGE = this.selectedScript?.language
                    this.lov.lovProviderJSON[prop].SCRIPT = this.selectedScript?.text
                    break
                case lovProviderEnum.FIX_LOV:
                    if (this.listForFixLov && this.listForFixLov.length > 1) {
                        this.lov.lovProviderJSON[prop].ROWS.ROW = this.listForFixLov
                    } else if (this.listForFixLov != null && this.listForFixLov.length == 1) {
                        this.lov.lovProviderJSON[prop].ROWS.ROW = this.listForFixLov[0]
                    }
                    break
                case lovProviderEnum.JAVA_CLASS:
                    this.lov.lovProviderJSON[prop].JAVA_CLASS_NAME = this.selectedJavaClass?.name
                    break
                case lovProviderEnum.DATASET:
                    this.lov.lovProviderJSON[prop].ID = this.selectedDataset?.id
                    this.lov.lovProviderJSON[prop].LABEL = this.selectedDataset?.label
                    if (this.selectedDataset?.id) {
                        for (var i = 0; i < this.datasources.length; i++) {
                            if ((this.datasources[i] as any).id == this.selectedDataset.id) {
                                this.lov.lovProviderJSON[prop].LABEL = (this.datasources[i] as any).label
                            }
                        }
                    }
            }
        },
        setLovProviderJsonValues(prop: string) {
            this.lov.lovProviderJSON = {}
            this.lov.lovProviderJSON[prop] = {
                LOVTYPE: 'simple'
            }

            switch (prop) {
                case lovProviderEnum.QUERY:
                case lovProviderEnum.DATASET:
                    this.setEmptyLovProviderJSONColumns(prop)
                    break
                case lovProviderEnum.SCRIPT:
                    this.setEmptyLovProviderJSONColumns(prop)
                    this.lov.lovProviderJSON[prop]['TREE-LEVELS-COLUMNS'] = ''
                    break
                case lovProviderEnum.FIX_LOV:
                    this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = 'DESCRIPTION'
                    this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = 'DESCRIPTION'
                    this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['TREE-LEVELS-COLUMNS'] = ''
                    this.lov.lovProviderJSON[prop].ROWS = {}
                    break
                case lovProviderEnum.JAVA_CLASS:
                    this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = 'VALUE'
                    this.lov.lovProviderJSON[prop]['TREE-LEVELS-COLUMNS'] = ''
            }
        },
        setEmptyLovProviderJSONColumns(prop: string) {
            this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = ''
            this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = ''
            this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = ''
            this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = ''
        },
        buildTestTable() {
            if (this.lov) {
                const propName = this.lov.itypeCd
                const prop = lovProviderEnum[propName]

                if (!this.lov.lovProviderJSON[prop].LOVTYPE) {
                    this.lov.lovProviderJSON[prop].LOVTYPE = 'simple'
                }

                this.treeListTypeModel = this.lov.lovProviderJSON[prop]
                this.setColumnValues()

                if (this.treeListTypeModel && this.treeListTypeModel.LOVTYPE != 'simple' && this.treeListTypeModel.LOVTYPE != '') {
                    this.setTreeLovModel()
                }
            }

            this.testLovModel = Array.isArray(this.tableModelForTest) ? this.tableModelForTest : []
            this.setFormatedVisibleValues()
        },
        setColumnValues() {
            if (this.lov.id) {
                this.formatedVisibleValues = this.treeListTypeModel['VISIBLE-COLUMNS']?.length > 0 ? this.treeListTypeModel['VISIBLE-COLUMNS'].split(',') : []
                this.formatedInvisibleValues = []
                if (!this.treeListTypeModel.LOVTYPE || this.treeListTypeModel.LOVTYPE == 'simple') {
                    this.formatedValues = this.treeListTypeModel['VALUE-COLUMN']?.length > 0 ? this.treeListTypeModel['VALUE-COLUMN']?.split(',') : []
                    this.formatedDescriptionValues = this.treeListTypeModel['DESCRIPTION-COLUMN']?.length > 0 ? this.treeListTypeModel['DESCRIPTION-COLUMN']?.split(',') : []
                } else {
                    this.formatedValues = this.treeListTypeModel['VALUE-COLUMNS']?.length > 0 ? this.treeListTypeModel['VALUE-COLUMNS'].split(',') : []
                    this.formatedDescriptionValues = this.treeListTypeModel['DESCRIPTION-COLUMNS']?.length > 0 ? this.treeListTypeModel['DESCRIPTION-COLUMNS']?.split(',') : []
                }
            } else {
                this.treeListTypeModel.LOVTYPE = 'simple'
            }
        },
        setTreeLovModel() {
            this.testLovTreeModel = []
            for (let i = 0; i < this.formatedValues.length; i++) {
                this.testLovTreeModel.push({ level: this.formatedValues[i], value: this.formatedValues[i], description: this.formatedDescriptionValues[i] })
            }
        },
        setFormatedVisibleValues() {
            const newFormatedVisibleValues = [] as any[]
            for (let i = 0; i < this.formatedVisibleValues.length; i++) {
                for (let j = 0; j < this.testLovModel.length; j++) {
                    if (this.formatedVisibleValues[i] == this.testLovModel[j].name) {
                        newFormatedVisibleValues.push(this.testLovModel[j].name)
                    }
                }
            }
            this.formatedVisibleValues = newFormatedVisibleValues as any
        },
        async handleSubmit(save: boolean) {
            this.formatForSave()

            if (this.testValid && save) {
                await this.saveLov()
            }
        },
        formatForSave() {
            let result = {}
            let propName = this.lov.itypeCd
            let prop = lovProviderEnum[propName]
            let tempObj = this.lov.lovProviderJSON[prop]

            if (!this.treeListTypeModel || this.treeListTypeModel.LOVTYPE == 'simple') {
                this.formatSimpleTestTree(tempObj)
            } else {
                this.formatAdvancedTestTree(tempObj)
            }
            tempObj.LOVTYPE = this.treeListTypeModel.LOVTYPE

            this.validateLov(tempObj)

            result[prop] = tempObj
            this.lov.lovProvider = this.x2js.js2xml(result)
            this.lov.itypeId = this.setLovInputTypeId(this.lov.itypeCd) as string
        },
        formatSimpleTestTree(tempObj: any) {
            tempObj['DESCRIPTION-COLUMN'] = this.treeListTypeModel['DESCRIPTION-COLUMN']
            tempObj['VALUE-COLUMN'] = this.treeListTypeModel['VALUE-COLUMN']
            tempObj['VISIBLE-COLUMNS'] = this.treeListTypeModel['VISIBLE-COLUMNS']

            for (let i = 0; i < this.testLovModel.length; i++) {
                if (this.treeListTypeModel['VISIBLE-COLUMNS'].indexOf(this.testLovModel[i].name) === -1) {
                    this.formatedInvisibleValues.push(this.testLovModel[i].name)
                }
            }
            tempObj['INVISIBLE-COLUMNS'] = this.formatedInvisibleValues.join()
        },
        formatAdvancedTestTree(tempObj: any) {
            delete tempObj['DESCRIPTION-COLUMN']
            delete tempObj['VALUE-COLUMN']
            const formatedDescriptionColumns = [] as any[]
            const formatedValueColumns = [] as any[]
            for (let i = 0; i < this.testLovTreeModel.length; i++) {
                formatedDescriptionColumns.push(this.testLovTreeModel[i].description)
            }
            tempObj['DESCRIPTION-COLUMNS'] = formatedDescriptionColumns.join()
            for (let i = 0; i < this.testLovTreeModel.length; i++) {
                formatedValueColumns.push(this.testLovTreeModel[i].value)
            }
            tempObj['VALUE-COLUMNS'] = formatedValueColumns.join()
            for (let i = 0; i < this.testLovModel.length; i++) {
                if (formatedValueColumns.indexOf(this.testLovModel[i].name) === -1) {
                    this.formatedInvisibleValues.push(this.testLovModel[i].name)
                }
            }
            tempObj['INVISIBLE-COLUMNS'] = this.formatedInvisibleValues.join()

            tempObj['VISIBLE-COLUMNS'] = this.treeListTypeModel['VISIBLE-COLUMNS']
        },
        setLovInputTypeId(inputType: string) {
            switch (inputType) {
                case 'QUERY':
                    return '1'
                case 'SCRIPT':
                    return '2'
                case 'FIX_LOV':
                    return '3'
                case 'JAVA_CLASS':
                    return '4'
                case 'DATASET':
                    return '5'
            }
        },
        validateLov(tempObj: any) {
            if (tempObj.LOVTYPE == 'simple' && (!tempObj['VALUE-COLUMN'] || !tempObj['DESCRIPTION-COLUMN'])) {
                this.$store.commit('setError', {
                    title: this.$t('common.toast.errorTitle'),
                    msg: this.$t('managers.lovsManagement.emptyField')
                })
                this.testValid = false
            } else if (tempObj.LOVTYPE == 'tree' && (!tempObj['VALUE-COLUMNS'] || !tempObj['DESCRIPTION-COLUMNS'])) {
                this.$store.commit('setError', {
                    title: this.$t('common.toast.errorTitle'),
                    msg: this.$t('managers.lovsManagement.treeNotDefined')
                })
                this.testValid = false
            } else {
                this.testValid = true
            }
        },
        async saveLov() {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/save'

            if (this.lov.id) {
                this.operation = 'update'
                url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/'
            }

            await this.sendRequest(url)
                .then((response: AxiosResponse<any>) => {
                    if (response.status == 409) {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.errorTitle'),
                            msg: this.$t('managers.lovsManagement.sameLabelError')
                        })
                    } else {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.' + this.operation + 'Title'),
                            msg: this.$t('common.toast.success')
                        })
                        this.$emit('created')
                        this.testDialogVisible = false
                        const id = this.lov.id ? this.lov.id : response.data
                        this.$router.push(`${id}`)
                    }
                })
                .catch((response: AxiosResponse<any>) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: response
                    })
                })
        },
        sendRequest(url: string) {
            if (this.operation === 'create') {
                return this.$http.post(url, this.lov)
            } else {
                return this.$http.put(url, this.lov)
            }
        },
        onTestSave(payload: any) {
            this.treeListTypeModel = payload.treeListTypeModel
            this.testLovModel = payload.model
            this.testLovTreeModel = payload.treeModel

            this.treeListTypeModel['VISIBLE-COLUMNS'] = ''
            for (let i = 0; i < this.testLovModel.length; i++) {
                this.treeListTypeModel['VISIBLE-COLUMNS'] += this.testLovModel[i].name
                this.treeListTypeModel['VISIBLE-COLUMNS'] += i === this.testLovModel.length - 1 ? '' : ','
            }

            this.handleSubmit(this.sendSave)
            this.testDialogVisible = false
        },
        onTestButtonClick() {
            this.sendSave = false
            this.checkForDependencies(false)
        },
        async onPreview() {
            await this.previewLov(lovsManagementWizardCardDescriptor.defaultPagination, true, true)
            this.dependenciesReady = this.dependenciesSet()
        },
        dependenciesSet() {
            let ready = true
            this.dependenciesList.forEach((el: any) => {
                if (!el.value) {
                    ready = false
                }
            })
            return ready
        },
        onTouched() {
            this.touchedForTest = true
            this.$emit('touched')
        },
        onPreviewClose() {
            this.previewDialogVisible = false
        },
        onParamsDialogClose() {
            this.paramsDialogVisible = false
            this.dependenciesList = []
            this.dependenciesReady = false
        },
        async onTest() {
            this.dependenciesReady = true
            await this.previewLov(this.pagination, false, false)
            this.buildTestTable()
        }
    }
})
</script>
