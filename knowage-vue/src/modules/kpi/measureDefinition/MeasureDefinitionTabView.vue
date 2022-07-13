<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #start>{{ title }} </template>
            <template #end>
                <Button class="p-button-text p-button-rounded kn-button" :label="$t('kpi.measureDefinition.alias')" @click="aliasesVisible = !aliasesVisible" data-test="submit-button" />
                <Button class="p-button-text p-button-rounded kn-button" :label="$t('kpi.measureDefinition.placeholder')" @click="placeholderVisible = !placeholderVisible" data-test="submit-button" />
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="metadataDisabled" @click="submitConfirm" data-test="submit-button" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <div class="p-d-flex p-flex-row kn-page-content">
            <div class="card kn-flex" v-if="!loading">
                <TabView v-model:activeIndex="activeTab" @tab-change="setTabChanged($event.index)">
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('kpi.measureDefinition.query') }}</span>
                        </template>

                        <MeasureDefinitionQueryCard
                            :rule="rule"
                            :datasourcesList="datasourcesList"
                            :aliases="availableAliasList"
                            :placeholders="placeholdersList"
                            :columns="columns"
                            :rows="rows"
                            :codeInput="codeInput"
                            :preview="preview"
                            @queryChanged="queryChanged = true"
                            @loadPreview="previewQuery(false, true, true)"
                            @closePreview="preview = false"
                        ></MeasureDefinitionQueryCard>
                    </TabPanel>

                    <TabPanel :disabled="metadataDisabled">
                        <template #header>
                            <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                        </template>

                        <MeasureDefinitionMetadataCard :currentRule="rule" :tipologiesType="domainsKpiRuleoutput" :domainsTemporalLevel="domainsTemporalLevel" :categories="domainsKpiMeasures" @touched="setTouched"></MeasureDefinitionMetadataCard>
                    </TabPanel>
                </TabView>
            </div>
            <div v-if="aliasesVisible" class="listbox p-d-flex p-flex-column">
                <MeasureDefinitionFilterList :header="$t('kpi.measureDefinition.alias')" :list="availableAliasList" listType="alias" @selected="setCodeInput($event)"></MeasureDefinitionFilterList>
            </div>
            <div v-if="placeholderVisible" class="listbox p-d-flex p-flex-column">
                <MeasureDefinitionFilterList :header="$t('kpi.measureDefinition.placeholder')" :list="placeholdersList" listType="placeholder" @selected="setCodeInput($event)"></MeasureDefinitionFilterList>
            </div>
        </div>
    </div>

    <MeasureDefinitionSubmitDialog v-if="showSaveDialog" :ruleName="rule.name" :newAlias="newAlias" :reusedAlias="reusedAlias" :newPlaceholder="newPlaceholder" :reusedPlaceholder="reusedPlaceholder" @close="showSaveDialog = false" @save="saveRule($event)"></MeasureDefinitionSubmitDialog>

    <Dialog :autoZIndex="false" :style="metadataDefinitionTabViewDescriptor.errorDialog.style" :modal="true" :visible="errorDialogVisible" :header="errorTitle" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary error-dialog" :closable="false">
        <p>{{ errorMessage }}</p>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeErrorMessageDialog"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDatasource, iMeasure, iRule, iPlaceholder } from './MeasureDefinition'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import MeasureDefinitionFilterList from './MeasureDefinitionFilterList.vue'
import MeasureDefinitionMetadataCard from './card/MeasureDefinitionMetadataCard/MeasureDefinitionMetadataCard.vue'
import metadataDefinitionTabViewDescriptor from './MetadataDefinitionTabViewDescriptor.json'
import MeasureDefinitionQueryCard from './card/MeasureDefinitionQueryCard/MeasureDefinitionQueryCard.vue'
import MeasureDefinitionSubmitDialog from './MeasureDefinitionSubmitDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'measure-definition-detail',
    components: { Dialog, MeasureDefinitionFilterList, MeasureDefinitionMetadataCard, MeasureDefinitionQueryCard, MeasureDefinitionSubmitDialog, TabView, TabPanel },
    props: {
        id: {
            type: String
        },
        ruleVersion: {
            type: String
        },
        clone: {
            type: String
        }
    },
    data() {
        return {
            metadataDefinitionTabViewDescriptor,
            rule: {
                definition: 'SELECT\n\nFROM\n\nWHERE',
                ruleOutputs: [] as iMeasure[],
                placeholders: [] as iPlaceholder[]
            } as iRule,
            datasourcesList: [] as iDatasource[],
            availableAliasList: [],
            notAvailableAliasList: [],
            placeholdersList: [],
            domainsKpiRuleoutput: [],
            domainsTemporalLevel: [],
            domainsKpiMeasures: [],
            newAlias: [] as any[],
            reusedAlias: [] as any[],
            newPlaceholder: [] as any[],
            reusedPlaceholder: [] as any[],
            activeTab: 0,
            columns: [] as any[],
            rows: [],
            errorMessage: null as string | null,
            errorTitle: null as string | null,
            tabChanged: false,
            loading: false,
            touched: false,
            showSaveDialog: false,
            operation: 'create',
            queryChanged: false,
            aliasesVisible: false,
            placeholderVisible: false,
            codeInput: null as string | null,
            preview: false
        }
    },
    computed: {
        title(): string {
            return this.clone || this.id ? this.rule.name : this.$t('kpi.measureDefinition.newMeasure')
        },
        metadataDisabled(): Boolean {
            let disabled = false
            if (!this.rule.dataSource) {
                disabled = true
            }
            if (this.rule.placeholders?.length > 0) {
                this.rule.placeholders.forEach((placeholder) => {
                    if (!placeholder.value) {
                        disabled = true
                    }
                })
            }
            return disabled
        },
        errorDialogVisible(): Boolean {
            return this.errorMessage ? true : false
        }
    },
    async created() {
        this.loading = true
        if (this.id && this.ruleVersion) {
            await this.loadSelectedRule()
        }

        if (this.clone === 'true') {
            this.rule.name = this.$t('common.copyOf') + ' ' + this.rule.name
            delete this.rule.id
        }
        await this.loadDataSources()
        const index = this.datasourcesList.findIndex((datasource: iDatasource) => this.rule.dataSourceId === datasource.DATASOURCE_ID)
        if (index > -1) {
            this.rule.dataSource = this.datasourcesList[index]
        }
        await this.loadAliases()
        await this.loadPlaceholders()
        await this.loadDomainsData()
        this.loading = false
    },
    methods: {
        async loadSelectedRule() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.ruleVersion}/loadRule`).then((response: AxiosResponse<any>) => (this.rule = response.data))
        },
        async loadDataSources() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `datasources/?onlySqlLike=true`).then((response: AxiosResponse<any>) => (this.datasourcesList = response.data.root))
        },
        async loadAliases() {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listAvailableAlias`
            if (this.rule.id) {
                url += `?ruleId=${this.id}&ruleVersion=${this.ruleVersion}`
            }
            await this.$http.get(url).then((response: AxiosResponse<any>) => {
                this.availableAliasList = response.data.available
                this.notAvailableAliasList = response.data.notAvailable
            })
        },
        async loadPlaceholders() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listPlaceholder`).then((response: AxiosResponse<any>) => (this.placeholdersList = response.data))
        },
        async loadDomainsData() {
            await this.loadDomainsByCode('KPI_RULEOUTPUT_TYPE').then((response: AxiosResponse<any>) => (this.domainsKpiRuleoutput = response.data))
            await this.loadDomainsByCode('TEMPORAL_LEVEL').then((response: AxiosResponse<any>) => (this.domainsTemporalLevel = response.data))
            await this.loadCategoriesByCode('KPI_MEASURE_CATEGORY').then((response: AxiosResponse<any>) => (this.domainsKpiMeasures = response.data))
        },
        loadDomainsByCode(code: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${code}`)
        },
        loadCategoriesByCode(code: string) {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `3.0/category/listByCode/${code}`)
        },
        async setTabChanged(tabIndex: any) {
            this.activeTab = tabIndex

            if (this.activeTab !== 0) {
                await this.previewQuery(false, false, true)
            }

            this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                this.setAliasIcon(ruleOutput)
                if (!ruleOutput.category) {
                    ruleOutput.category = { valueCd: '' }
                }
            })

            if (this.errorMessage) {
                this.activeTab = 0
            }
        },
        setNewAliases() {
            this.newAlias = []
            this.reusedAlias = []
            this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                if (this.aliasExists(ruleOutput.alias)) {
                    this.reusedAlias.push(ruleOutput.alias)
                } else {
                    this.newAlias.push(ruleOutput.alias)
                }
            })
            this.newAlias.sort()
            this.reusedAlias.sort()
        },
        setNewPlaceholders() {
            this.newPlaceholder = []
            this.reusedPlaceholder = []
            this.rule.placeholders.forEach((placeholder: any) => {
                if (this.placeholderExists(placeholder.name)) {
                    this.reusedPlaceholder.push(placeholder.name)
                } else {
                    this.newPlaceholder.push(placeholder.name)
                }
            })
            this.newPlaceholder.sort()
            this.reusedPlaceholder.sort()
        },
        placeholderExists(name: string) {
            let exists = false
            this.placeholdersList.forEach((placeholder: any) => {
                if (placeholder.name.toUpperCase() === name.toUpperCase()) {
                    exists = true
                }
            })
            return exists
        },
        setAliasIcon(ruleOutput: any) {
            if (!this.aliasExists(ruleOutput.alias) && !this.aliasUsedByMeasure(ruleOutput.alias)) {
                ruleOutput.aliasIcon = 'fa fa-exclamation-triangle icon-missing'
            }
            if (this.aliasUsedByMeasure(ruleOutput.alias)) {
                ruleOutput.aliasIcon = 'fa fa-exclamation-triangle icon-used'
            }
        },
        aliasExists(name: string) {
            let exists = false
            this.availableAliasList.forEach((alias: any) => {
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    exists = true
                }
            })
            return exists
        },
        aliasUsedByMeasure(name: string) {
            let used = false
            this.notAvailableAliasList.forEach((alias: any) => {
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    used = true
                }
            })
            return used
        },
        async previewQuery(save: boolean, hasPlaceholders: boolean, showDialog: boolean) {
            this.loading = true

            if (this.activeTab === 0 && showDialog) {
                this.preview = true
            }
            const tempRuleOutputs = this.rule.ruleOutputs
            this.rule.ruleOutputs.forEach((ruleOutput) => {
                delete ruleOutput.aliasIcon
                ruleOutput.category = { valueCd: ruleOutput.category?.valueCd as string }
            })
            const tempDatasource = this.rule.dataSource
            if (this.rule.dataSource) {
                this.rule.dataSourceId = this.rule.dataSource.DATASOURCE_ID
            }
            delete this.rule.dataSource
            if (this.rule.definition) {
                this.loadPlaceholder()
            }

            if ((this.rule.placeholders && this.rule.placeholders.length === 0) || hasPlaceholders) {
                const postData = { rule: this.rule, maxItem: 10 }

                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/queryPreview', postData, { headers: { 'X-Disable-Errors': 'true' } })
                    .then((response: AxiosResponse<any>) => {
                        this.columns = response.data.columns
                        this.rows = response.data.rows
                        this.columnToRuleOutputs()
                    })
                    .catch((error) => {
                        this.setPreviewError(error.message)
                    })
            }
            this.setNewAliases()
            this.setNewPlaceholders()
            if (save && !this.errorMessage) {
                await this.preSaveRule()
            }

            this.rule.dataSource = tempDatasource
            this.rule.ruleOutputs = tempRuleOutputs
            this.loading = false
        },
        setPreviewError(error: string) {
            this.errorTitle = this.$t('kpi.measureDefinition.metadataError') + ' ' + this.$t('kpi.measureDefinition.wrongQuery')
            this.errorMessage = error
            this.rows = []
        },
        async preSaveRule() {
            this.loading = true
            const tempDataSource = this.rule.dataSource
            const tempRuleOutputs = this.rule.ruleOutputs
            delete this.rule.dataSource
            this.rule.ruleOutputs.forEach((ruleOutput) => {
                delete ruleOutput.aliasIcon
                ruleOutput.category = { valueCd: ruleOutput.category?.valueCd as string }
            })

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/preSaveRule', this.rule, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    if (this.rule.ruleOutputs.length === 0) {
                        this.errorTitle = this.$t('kpi.measureDefinition.presaveErrors.metadataMissing')
                        this.errorMessage = this.$t('kpi.measureDefinition.presaveErrors.metadataMissingText')
                    }

                    let measurePresent = false
                    this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                        if (ruleOutput.type.valueCd === 'TEMPORAL_ATTRIBUTE' && ruleOutput.hierarchy === null) {
                            this.errorTitle = this.$t('kpi.measureDefinition.presaveErrors.noTemporalattributSet')
                            this.errorMessage = this.$t('kpi.measureDefinition.presaveErrors.missingTemporalattributText')
                        } else if (ruleOutput.type.valueCd === 'MEASURE') {
                            measurePresent = true
                        }
                    })

                    if (!measurePresent) {
                        this.errorTitle = this.$t('kpi.measureDefinition.presaveErrors.noMeasureSet')
                        this.errorMessage = this.$t('kpi.measureDefinition.presaveErrors.metadataMissingText')
                    }

                    if (!this.errorMessage) {
                        this.showSaveDialog = true
                    }
                })
                .catch((error: any) => {
                    this.errorTitle = this.$t('kpi.measureDefinition.metadataError') + ' ' + this.$t('kpi.measureDefinition.wrongQuery')
                    this.errorMessage = error.message
                })

            this.rule.dataSource = tempDataSource
            this.rule.ruleOutputs = tempRuleOutputs
            this.loading = false
        },
        async saveRule(ruleName: string) {
            this.loading = true
            this.rule.name = ruleName
            if (this.rule.id) {
                this.operation = 'update'
            }

            delete this.rule.dataSource
            this.rule.ruleOutputs.forEach((ruleOutput) => {
                delete ruleOutput.aliasIcon
                ruleOutput.category = { valueCd: ruleOutput.category?.valueCd as string }
            })

            delete this.rule.dataSource
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/saveRule', this.rule)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$router.replace('/measure-definition')
                })
                .catch((response: any) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.' + this.operation + 'Title'),
                        msg: response.message
                    })
                })
            this.loading = false
        },
        columnToRuleOutputs() {
            const tempMetadatas = [] as any[]

            for (let index in this.columns) {
                tempMetadatas.push(this.columns[index].label.toUpperCase())
                if (this.ruleOutputIndexOfColumnName(this.columns[index].label) === -1) {
                    let type = this.domainsKpiRuleoutput[1]
                    if (this.columns[index].type === 'int' || this.columns[index].type == 'float') {
                        type = this.domainsKpiRuleoutput[0]
                    }
                    this.rule.ruleOutputs.push({
                        alias: this.columns[index].label,
                        type: type
                    })
                }
            }
            for (let index = 0; index < this.rule.ruleOutputs.length; index++) {
                if (tempMetadatas.indexOf(this.rule.ruleOutputs[index].alias.toUpperCase()) === -1) {
                    this.rule.ruleOutputs.splice(index, 1)
                    index--
                }
            }
        },
        ruleOutputIndexOfColumnName(columnName: string) {
            for (let i = 0; i < this.rule.ruleOutputs.length; i++) {
                if (this.rule.ruleOutputs[i].alias.toUpperCase() === columnName.toUpperCase()) {
                    return i
                }
            }
            return -1
        },
        loadPlaceholder() {
            const placeholder = this.rule.definition.match(/@\w*/g)

            if (placeholder != null) {
                for (let i = 0; i < placeholder.length; i++) {
                    const placeholderName = placeholder[i].substring(1, placeholder[i].length)

                    let tempPlaceholder = this.rule.placeholders.find((tempPlaceholder) => tempPlaceholder.name.toUpperCase() === placeholderName.toUpperCase())

                    if (!tempPlaceholder) {
                        tempPlaceholder = this.placeholdersList.find((placeholder: any) => placeholder.name.toUpperCase() === placeholderName.toUpperCase()) as any
                        if (tempPlaceholder == undefined) {
                            const newPlaceholder = {
                                name: placeholderName,
                                value: ''
                            }
                            this.rule.placeholders.push(newPlaceholder)
                        } else {
                            this.rule.placeholders.push(tempPlaceholder)
                        }
                    }

                    for (let index = 0; index < this.rule.placeholders.length; index++) {
                        if (placeholder.indexOf('@' + this.rule.placeholders[index].name) == -1) {
                            this.rule.placeholders.splice(index, 1)
                            index--
                        }
                    }
                }
            } else {
                this.rule.placeholders = []
            }
        },
        submitConfirm() {
            if (!this.queryChanged) {
                this.previewQuery(true, false, false)
            } else {
                this.$confirm.require({
                    message: this.$t('kpi.measureDefinition.metadataChangedMessage'),
                    header: this.$t('kpi.measureDefinition.metadataChangedTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.queryChanged = false
                        this.previewQuery(true, false, false)
                    }
                })
            }
        },
        closeErrorMessageDialog() {
            this.errorMessage = null
        },
        closeTemplate() {
            const path = '/measure-definition'
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },
        setCodeInput(event: any) {
            if (this.activeTab === 0) {
                this.codeInput = event.type === 'placeholder' ? '@' + event.value : event.value
            }
        },
        sortArray(array: [], type: string) {
            if (this[type] === 'DESC') {
                array = array.sort((a: any, b: any) => (a.name > b.name ? 1 : -1))
                this[type] = 'ASC'
            } else {
                array = array.sort((a: any, b: any) => (a.name < b.name ? 1 : -1))
                this[type] = 'DESC'
            }
        },
        setTouched() {
            this.touched = true
        }
    }
})
</script>

<style lang="scss" scoped>
.error-dialog {
    width: 60vw;
}
.listbox {
    width: 320px;
}
</style>
