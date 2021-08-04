<template>
    <Card class="p-m-2" :header="header">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ toolbarTitle }}
                </template>
                <template #right>
                    <Button class="kn-button-sm p-button-text" :label="$t('managers.lovsManagement.preview')" @click="checkForDependencies(true)" />
                    <Button class="kn-button-sm p-button-text" :label="$t('managers.lovsManagement.test')" @click="onTestButtonClick" />
                    <Button icon="fa fa-info-circle" class="p-button-text p-button-rounded p-button-plain" aria-label="Info" @click="infoDialogVisible = true" />
                    <Button icon="fa fa-users" class="p-button-text p-button-rounded p-button-plain" aria-label="Profiles" @click="profileAttributesDialogVisible = true" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <LovsManagementQuery v-if="lovType === 'QUERY'" :selectedLov="lov" :selectedQuery="selectedQuery" :datasources="datasources" :codeInput="codeInput" @touched="$emit('touched')"></LovsManagementQuery>
            <LovsManagementScript v-else-if="lovType === 'SCRIPT'" :selectedLov="lov" :selectedScript="selectedScript" :listOfScriptTypes="listOfScriptTypes" :codeInput="codeInput" @touched="$emit('touched')"></LovsManagementScript>
            <LovsManagementFixedLovsTable v-else-if="lovType === 'FIX_LOV'" :listForFixLov="listForFixLov" @touched="$emit('touched')"></LovsManagementFixedLovsTable>
            <LovsManagementJavaClassInput v-else-if="lovType === 'JAVA_CLASS'" :selectedJavaClass="selectedJavaClass" @touched="$emit('touched')"></LovsManagementJavaClassInput>
            <LovsManagementDataset v-else-if="lovType === 'DATASET'" :dataset="selectedDataset" />
        </template>
    </Card>
    <LovsManagementInfoDialog v-if="infoDialogVisible" :infoTitle="infoTitle" :lovType="lov.itypeCd" @close="infoDialogVisible = false"></LovsManagementInfoDialog>
    <LovsManagementProfileAttributesList v-if="profileAttributesDialogVisible" :profileAttributes="profileAttributes" @selected="setCodeInput($event)" @close="profileAttributesDialogVisible = false"></LovsManagementProfileAttributesList>
    <LovsManagementParamsDialog v-if="paramsDialogVisible" :dependenciesList="dependenciesList" @preview="previewLov(this.pagination, true, false)" @close="paramsDialogVisible = false"></LovsManagementParamsDialog>
    <LovsManagementPreviewDialog v-if="previewDialogVisible" :dataForPreview="dataForPreview" :pagination="pagination" @close="previewDialogVisible = false" @pageChanged="previewLov($event, false)"></LovsManagementPreviewDialog>
    <LovsManagementTestDialog v-if="testDialogVisible" :selectedLov="lov" :testModel="treeListTypeModel" :testLovModel="testLovModel" :testLovTreeModel="testLovTreeModel" @close="testDialogVisible = false" @save="onTestSave($event)"></LovsManagementTestDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../LovsManagement'
import X2JS from 'x2js'
import axios from 'axios'
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

export enum lovProviderEnum {
    SCRIPT = 'SCRIPTLOV',
    QUERY = 'QUERY',
    FIX_LOV = 'FIXLISTLOV',
    JAVA_CLASS = 'JAVACLASSLOV',
    DATASET = 'DATASET'
}

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
        save: { type: Boolean }
    },
    emits: ['touched', 'save', 'created'],
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
            testLovModel: {} as any,
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
            x2js: new X2JS()
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
            if (this.testValid) {
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
        if (this.lov.id) {
            this.testValid = true;
        }
        this.loadLov()
        this.onLovTypeChanged()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
            // console.log('TEST', this.x2js.js2xml(this.lov.lovProviderJSON))
            // console.log('DEBUG LOV', this.lov)
        },
        onLovTypeChanged() {
            switch (this.lovType) {
                case 'SCRIPT':
                    this.toolbarTitle = this.$t('managers.lovsManagement.scriptWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoSyntax')
                    break

                case 'QUERY':
                    this.toolbarTitle = this.$t('managers.lovsManagement.queryWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoSyntax')
                    break
                case 'FIX_LOV':
                    this.toolbarTitle = this.$t('managers.lovsManagement.fixedListWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoRules')
                    break
                case 'JAVA_CLASS':
                    this.toolbarTitle = this.$t('managers.lovsManagement.javaClassWizard')
                    this.infoTitle = this.$t('managers.lovsManagement.infoRules')
                    break
                case 'DATASET':
                    this.toolbarTitle = this.$t('managers.lovsManagement.datasetWizard')
                    break
            }
        },
        setCodeInput(event: any) {
            // console.log('PROFILE ATTRIBUTE SELECTED', event)
            // this.codeInput = event
            this.codeInput = { code: event, changed: !this.codeInput.changed }
        },

        async checkForDependencies(showPreview: boolean) {
            this.formatForTest()
            this.dependenciesList = []
            let listOfEmptyDependencies = [] as any[]

            // console.log('CHECK DEPENDENCIES LOVPROVIDER', this.lov.lovProviderJSON)

            if (this.lov.lovProviderJSON.QUERY && this.lov.id) {
                this.lov.lovProviderJSON.QUERY.STMT = this.lov.lovProviderJSON.QUERY.decoded_STMT
            }

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/checkdependecies', { provider: this.x2js.js2xml(this.lov.lovProviderJSON) }).then((response) => {
                listOfEmptyDependencies = response.data
                // console.log('listOfEmptyDependencies', listOfEmptyDependencies)
            })

            if (listOfEmptyDependencies.length > 0) {
                for (let i = 0; i < listOfEmptyDependencies.length; i++) {
                    this.dependenciesList.push({
                        name: listOfEmptyDependencies[i].name,
                        type: listOfEmptyDependencies[i].type
                    })
                }
                this.paramsDialogVisible = true
            } else {
                await this.previewLov(this.pagination, false, showPreview)
                this.buildTestTable()

                this.testDialogVisible = !showPreview
            }
        },
        async previewLov(value: any, hasDependencies: boolean, showPreview: boolean) {
            // console.log(value)
            this.pagination = value
            // console.log('PAGINATION', this.pagination)

            const postData = {
                data: {
                    ...this.lov,
                    lovProviderJSON: JSON.stringify(this.lov.lovProviderJSON),
                    lovProvider: this.x2js.js2xml(this.lov.lovProviderJSON)
                },
                pagination: this.pagination
            } as any

            // console.log('hasDependencies', hasDependencies)
            if (hasDependencies) {
                postData.dependencies = this.dependenciesList
            }

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/lovs/preview', postData).then((response) => {
                if (response.status === 204) {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.errorTitle'),
                        msg: this.$t('managers.lovsManagement.syntaxError')
                    })
                } else {
                    this.dataForPreview = response.data
                    this.tableModelForTest = response.data.metaData.fields
                    this.pagination.size = response.data.results

                    this.previewDialogVisible = showPreview
                    this.paramsDialogVisible = hasDependencies
                }
            })

            // console.log('DATA FOR PREVIEW', this.dataForPreview)
            // console.log('TABLE MODEL FOR TEST', this.tableModelForTest)
            console.log('previewDialogVisible', this.previewDialogVisible)
        },
        // TODO REFACTOR THIS
        formatForTest() {
            const propName = this.lov.itypeCd
            const prop = lovProviderEnum[propName]

            if (!this.lov.id) {
                this.lov.lovProviderJSON = {}
                this.lov.lovProviderJSON[prop] = {
                    LOVTYPE: 'simple'
                }

                switch (prop) {
                    case lovProviderEnum.QUERY:
                        this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = ''
                        this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = ''
                        break
                    case lovProviderEnum.SCRIPT:
                        this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = ''
                        this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = ''
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
                        break
                    case lovProviderEnum.DATASET:
                        this.lov.lovProviderJSON[prop]['VISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['INVISIBLE-COLUMNS'] = ''
                        this.lov.lovProviderJSON[prop]['DESCRIPTION-COLUMN'] = ''
                        this.lov.lovProviderJSON[prop]['VALUE-COLUMN'] = ''
                        break
                }
            }

            switch (prop) {
                case lovProviderEnum.QUERY:
                    this.lov.lovProviderJSON[prop].CONNECTION = this.selectedQuery?.datasource
                    this.lov.lovProviderJSON[prop].STMT = this.selectedQuery?.query
                    break
                case lovProviderEnum.SCRIPT:
                    console.log('TEST LOV IN SCRIPT', this.lov)
                    console.log('TEST SCRIPT', this.selectedScript)
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
                    if (this.selectedDataset?.id) {
                        for (var i = 0; i < this.datasources.length; i++) {
                            if ((this.datasources[i] as any).id == this.selectedDataset.id) {
                                this.lov.lovProviderJSON[prop].LABEL = (this.datasources[i] as any).label
                            }
                        }
                    }
                    break
            }
            console.log('LOV AFTER FORMATING FOR TEST', this.lov)
        },
        // TODO REFACTOR THIS MAYBE?
        buildTestTable() {
            if (this.lov != null) {
                const propName = this.lov.itypeCd
                const prop = lovProviderEnum[propName]
                if (this.lov.lovProviderJSON[prop].LOVTYPE == '' || this.lov.lovProviderJSON[prop].LOVTYPE == undefined) {
                    this.lov.lovProviderJSON[prop].LOVTYPE = 'simple'
                }

                this.treeListTypeModel = this.lov.lovProviderJSON[prop]

                if (this.lov.id) {
                    this.formatedVisibleValues = this.treeListTypeModel['VISIBLE-COLUMNS'].split(',')
                    this.formatedInvisibleValues = []
                    if (!this.treeListTypeModel.LOVTYPE || this.treeListTypeModel.LOVTYPE == 'simple') {
                        this.formatedValues = this.treeListTypeModel['VALUE-COLUMN'].split(',')
                        this.formatedDescriptionValues = this.treeListTypeModel['DESCRIPTION-COLUMN'].split(',')
                    } else {
                        this.formatedValues = this.treeListTypeModel['VALUE-COLUMNS'].split(',')
                        this.formatedDescriptionValues = this.treeListTypeModel['DESCRIPTION-COLUMNS'].split(',')
                    }
                } else {
                    this.treeListTypeModel.LOVTYPE = 'simple'
                }
                if (this.treeListTypeModel && this.treeListTypeModel.LOVTYPE != 'simple' && this.treeListTypeModel.LOVTYPE != '') {
                    this.testLovTreeModel = []
                    for (let i = 0; i < this.formatedValues.length; i++) {
                        const defObj = {} as any
                        defObj.level = this.formatedValues[i]
                        defObj.value = this.formatedValues[i]
                        defObj.description = this.formatedDescriptionValues[i]

                        this.testLovTreeModel.push(defObj)
                    }
                    // console.log('TEST LOVE TREE MODEL: ', this.testLovTreeModel)
                }
            }

            this.testLovModel = this.tableModelForTest
            const newformatedVisibleValues = [] as any[]
            for (let i = 0; i < this.formatedVisibleValues.length; i++) {
                for (let j = 0; j < this.testLovModel.length; j++) {
                    if (this.formatedVisibleValues[i] == this.testLovModel[j].name) {
                        newformatedVisibleValues.push(this.testLovModel[j].name)
                    }
                }
            }
            this.formatedVisibleValues = newformatedVisibleValues as any

            // console.log('FORMATED VALUES', this.formatedValues)
            // console.log('FORMATED DESCRIPTION VALUES', this.formatedDescriptionValues)
            // console.log('FORMATED VISIBLE VALUES', this.formatedVisibleValues)
            // console.log('FORMATED INVISIBLE VALUES', this.formatedInvisibleValues)
            // console.log('TEST MODEL LOADED', this.testLovModel)
        },
        async handleSubmit(save: boolean) {
            // console.log('LOV FOR SUBMIT: ', this.selectedLov)
            this.formatForSave()
                        console.log('TEST VALID', this.testValid)
                        if (this.testValid && save) {
                                        await this.saveLov()
                        } else {
                            this.testDialogVisible = false
                        }

            console.log('LOV FOR SUBMIT AFTER FORMATING: ', this.selectedLov)
        },
        formatForSave() {
            // console.log('FORMAT FOR SAVE', this.testLovModel)

            let result = {}
            let propName = this.lov.itypeCd
            let prop = lovProviderEnum[propName]
            let tempObj = this.lov.lovProviderJSON[prop]
            // console.log('TEMP OBJ', tempObj)
            // console.log('TREE LIST TYPE MODEL IN FORMAT SAVE', this.treeListTypeModel)
            if (!this.treeListTypeModel || this.treeListTypeModel.LOVTYPE == 'simple') {
                tempObj['DESCRIPTION-COLUMN'] = this.treeListTypeModel['DESCRIPTION-COLUMN']
                tempObj['VALUE-COLUMN'] = this.treeListTypeModel['VALUE-COLUMN']
                // tempObj['VISIBLE-COLUMNS'] = this.formatedVisibleValues.join()
                tempObj['VISIBLE-COLUMNS'] = this.treeListTypeModel['VISIBLE-COLUMNS']
                for (var i = 0; i < this.testLovModel.length; i++) {
                    if (this.treeListTypeModel['VISIBLE-COLUMNS'].indexOf(this.testLovModel[i].name) === -1) {
                        this.formatedInvisibleValues.push(this.testLovModel[i].name)
                    }
                }
                tempObj['INVISIBLE-COLUMNS'] = this.formatedInvisibleValues.join()
            } else {
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
            }
            tempObj.LOVTYPE = this.treeListTypeModel.LOVTYPE

            this.validateLov(tempObj)
            // console.log('STIGAOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO')

            result[prop] = tempObj
            // console.log('RESULT IN FORMAT', result)
            this.lov.lovProvider = this.x2js.js2xml(result)
            this.lov.itypeId = this.setLovInputTypeId(this.lov.itypeCd) as string
            // delete this.lov.lovProviderJSON

            // console.log('LOV AFTER FORMAT', this.lov)
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
            console.log("CAAAAAAAAAAAAAAAALEEED")
            if (tempObj.LOVTYPE == 'simple' && (tempObj['VALUE-COLUMN'] == '' || tempObj['DESCRIPTION-COLUMN'] == '')) {
                console.log('ERRROR ONE')
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.errorTitle'),
                    msg: this.$t('managers.lovsManagement.emptyField')
                })
                this.testValid = false
            } else if (tempObj.LOVTYPE == 'tree' && (tempObj['VALUE-COLUMNS'] == '' || tempObj['DESCRIPTION-COLUMNS'] == '')) {
                console.log('ERRROR TWO')
                this.$store.commit('setInfo', {
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
            await this.sendRequest(url).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.errorTitle'),
                        msg: response.data.errors[0].message
                    })
                } else {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$emit('created')
                    // console.log('RESPONSE DATA', response.data)
                    this.testDialogVisible = false
                    const id = this.lov.id ? this.lov.id : response.data
                    this.$router.push(`${id}`)
                }
            })
        },
        sendRequest(url: string) {
            if (this.operation === 'create') {
                return axios.post(url, this.lov)
            } else {
                return axios.put(url, this.lov)
            }
        },
        onTestSave(payload: any) {
            this.treeListTypeModel = payload.treeListTypeModel
            this.testLovModel = payload.model
            this.testLovTreeModel = payload.treeModel
            this.handleSubmit(false)
        },
        onTestButtonClick() {
            this.checkForDependencies(false)
        }
    }
})
</script>
