<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ rule.id ? rule.name : $t('kpi.measureDefinition.newMeasure') }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="submitConfirm" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card" v-else>
        <TabView v-model:activeIndex="activeTab" @tab-change="setTabChanged($event.index)">
            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.query') }}</span>
                </template>

                <QueryCard :rule="rule" :datasourcesList="datasourcesList" :aliases="availableAliasList" :placeholders="placeholdersList" :columns="columns" :rows="rows" @queryChanged="queryChanged = true" @loadPreview="previewQuery(false, true)"></QueryCard>
            </TabPanel>

            <TabPanel :disabled="metadataDisabled">
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>

                <MetadataCard :currentRule="rule" :tipologiesType="domainsKpiRuleoutput" :domainsTemporalLevel="domainsTemporalLevel" :categories="domainsKpiMeasures"></MetadataCard>
            </TabPanel>
        </TabView>
    </div>

    <Dialog :contentStyle="metadataDefinitionTabViewDescriptor.dialog.style" :header="$t('kpi.measureDefinition.saveInProgress')" :visible="showSaveDialog" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-field p-m-2">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model.trim="rule.name" />
                <label class="kn-material-input-label"> {{ $t('common.name') }} </label>
            </span>
        </div>
        <Toolbar v-if="newAlias.length > 0 || reusedAlias.length > 0" class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('kpi.measureDefinition.alias') }}
            </template>
        </Toolbar>
        <div v-if="newAlias.length > 0">
            <h4>{{ $t('common.new') }}</h4>
            <Chip v-for="alias in newAlias" :key="alias.id" :label="alias"></Chip>
        </div>

        <div v-if="reusedAlias.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="alias in reusedAlias" :key="alias.id" :label="alias"></Chip>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="showSaveDialog = false"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="saveRule"></Button>
        </template>
    </Dialog>

    <Dialog :modal="true" :visible="errorMessage" :header="errorTitle" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary error-dialog" :closable="false">
        <h1>{{ $t('kpi.measureDefinition.metadataError') + ' ' + $t('kpi.measureDefinition.wrongQuery') }}</h1>
        <p>{{ errorMessage }}</p>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeerrorMessageDialog"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDatasource, iMeasure, iRule } from './MeasureDefinition'
import axios from 'axios'
import Chip from 'primevue/chip'
import Dialog from 'primevue/dialog'
import MetadataCard from './card/MetadataCard/MetadataCard.vue'
import metadataDefinitionTabViewDescriptor from './MetadataDefinitionTabViewDescriptor.json'
import QueryCard from './card/QueryCard/QueryCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

// TODO Fix title

export default defineComponent({
    name: 'rule-definition-detail',
    components: { Chip, Dialog, MetadataCard, QueryCard, TabView, TabPanel },
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
                // definition: 'SELECT\n\nFROM\n\nWHERE',
                definition: "SELECT account_id as bla FROM account WHERE account_description = 'Assets'",
                ruleOutputs: [] as iMeasure[]
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
            newPlaceholder: [],
            reusedPlaceholder: [],
            activeTab: 0,
            columns: [] as any[],
            rows: [],
            errorMessage: null as null | string,
            errorTitle: null as null | string,
            tabChanged: false,
            loading: false,
            touched: false,
            showSaveDialog: false,
            operation: 'create',
            queryChanged: false
        }
    },
    computed: {
        metadataDisabled() {
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
        }
    },

    async created() {
        // console.log('ID: ', this.id)
        // console.log('Rule Version: ', this.ruleVersion)
        // console.log('Clone: ', this.clone)
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
        // console.log('ALISASES FOR RULE available: ', this.availableAliasesForRule)
        // console.log('ALISASESFOR RULE  not available: ', this.notAvailableAliasesForRule)
        // console.log('PLACEHOLDERS: ', this.placeholdersList)
        console.log('RULE BOJAN', this.rule)
    },
    methods: {
        async loadSelectedRule() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.ruleVersion}/loadRule`).then((response) => (this.rule = response.data))
        },
        async loadDataSources() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `datasources/?onlySqlLike=true`).then((response) => (this.datasourcesList = response.data.root))
        },
        async loadAliases() {
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listAvailableAlias`
            // console.log('RULE TEEEEEST', this.rule)
            if (this.rule.id) {
                url += `?ruleId=${this.id}&ruleVersion=${this.ruleVersion}`
            }
            await axios.get(url).then((response) => {
                this.availableAliasList = response.data.available
                this.notAvailableAliasList = response.data.notAvailable
            })
        },
        async loadPlaceholders() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listPlaceholder`).then((response) => (this.placeholdersList = response.data))
        },
        async loadDomainsData() {
            await this.loadDomainsByCode('KPI_RULEOUTPUT_TYPE').then((response) => (this.domainsKpiRuleoutput = response.data))
            await this.loadDomainsByCode('TEMPORAL_LEVEL').then((response) => (this.domainsTemporalLevel = response.data))
            await this.loadDomainsByCode('KPI_MEASURE_CATEGORY').then((response) => (this.domainsKpiMeasures = response.data))
        },
        loadDomainsByCode(code: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${code}`)
        },
        async setTabChanged(tabIndex: any) {
            this.activeTab = tabIndex
            if (tabIndex === 1) {
                await this.previewQuery(false, false)

                this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                    this.setAliasIcon(ruleOutput)
                    if (!ruleOutput.category) {
                        ruleOutput.category = { valueCd: '' }
                    }
                    if (!ruleOutput.hierarchy) {
                        ruleOutput.hierarchy = { valueCd: '' }
                    }
                })

                console.log('ERROR MESSAGE: ', this.errorMessage)
                if (this.errorMessage) {
                    console.log('STIGAO!!!')
                    this.activeTab = 0
                }
            }
        },
        setNewAliases() {
            console.log('SET NEW ALIASES')
            this.newAlias = []
            this.reusedAlias = []
            this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                if (this.aliasExists(ruleOutput.alias)) {
                    this.reusedAlias.push(ruleOutput.alias)
                } else {
                    this.newAlias.push(ruleOutput.alias)
                }
            })
            console.log('NEW ALIASES', this.newAlias)
        },
        setAliasIcon(ruleOutput: any) {
            console.log('SET ALIAS ICON: ', ruleOutput.alias)
            if (!this.aliasExists(ruleOutput.alias) && !this.aliasUsedByMeasure(ruleOutput.alias)) {
                console.log('ALIAS doesnt Exist!')
                ruleOutput.aliasIcon = 'fa fa-exclamation-triangle icon-missing'
            }
            if (this.aliasUsedByMeasure(ruleOutput.alias)) {
                ruleOutput.aliasIcon = 'fa fa-exclamation-triangle icon-used'
            }
        },
        aliasExists(name: string) {
            let exists = false
            this.availableAliasList.forEach((alias: any) => {
                // console.log('Exists: ' + alias.name.toUpperCase() + ' === ' + name.toUpperCase())
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    // console.log('ALIAS Exists!')
                    exists = true
                }
            })
            return exists
        },
        aliasUsedByMeasure(name: string) {
            let used = false
            this.notAvailableAliasList.forEach((alias: any) => {
                //console.log('Used: ' + alias.name.toUpperCase() + ' === ' + name.toUpperCase())
                if (alias.name.toUpperCase() === name.toUpperCase()) {
                    //console.log('ALIAS USED!')
                    used = true
                }
            })
            return used
        },
        async previewQuery(save: boolean, hasPlaceholders: boolean) {
            // console.log('RULE: ', this.rule)
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
                await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/queryPreview', postData).then((response) => {
                    if (response.data.errors) {
                        this.errorMessage = response.data.errors[0].message
                    } else {
                        this.columns = response.data.columns
                        this.rows = response.data.rows
                        this.columnToRuleOutputs()
                    }
                })
            }
            // console.log('Submit error', this.errorMessage)
            this.setNewAliases()
            console.log('Rule after prepare', this.rule)
            if (save) {
                await this.preSaveRule()
            }

            this.rule.dataSource = tempDatasource
        },
        async preSaveRule() {
            delete this.rule.dataSource

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/preSaveRule', this.rule).then((response) => {
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

                console.log('PRESAVE RESPONSE: ', response)
                if (response.data.errors) {
                    this.errorTitle = null
                    this.errorMessage = response.data.errors[0]
                }

                if (!this.errorMessage) {
                    this.showSaveDialog = true
                }
            })
        },
        async saveRule() {
            if (this.rule.id) {
                this.operation = 'update'
            }

            delete this.rule.dataSource
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/saveRule', this.rule).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.' + this.operation + 'Title'),
                    msg: this.$t('common.toast.success')
                })
                this.$router.replace('/measure-definition')
            })
        },
        columnToRuleOutputs() {
            const tempMetadatas = [] as any[]
            // console.log('METADATA COLUMNS: ', this.columns)
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
            // console.log('RULE AFTER METHOD!!!', this.rule)
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
            // console.log('PLACEHOLDER ', placeholder)
            if (placeholder != null) {
                for (let i = 0; i < placeholder.length; i++) {
                    const placeholderName = placeholder[i].substring(1, placeholder[i].length)
                    let tempPlaceholder = this.rule.placeholders.find((tempPlaceholder) => {
                        // console.log(tempPlaceholder.name + ' === ' + placeholderName)
                        return tempPlaceholder.name === placeholderName
                    })
                    // console.log('TEMP PLACEHOLDER', tempPlaceholder)
                    if (!tempPlaceholder) {
                        tempPlaceholder = this.placeholdersList.find((placeholder: any) => placeholder.name === tempPlaceholder?.name) as any
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
                    // console.log('RULE PLACEHOLDERS: ', this.rule.placeholders)
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
                this.previewQuery(true, false)
            } else {
                this.$confirm.require({
                    message: this.$t('kpi.measureDefinition.metadataChangedMessage'),
                    header: this.$t('kpi.measureDefinition.metadataChangedTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.queryChanged = false
                        this.previewQuery(true, false)
                    }
                })
            }
            // console.log('caaaaled', this.showSaveDialog)
        },
        closeerrorMessageDialog() {
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
        }
    }
})
</script>

<style lang="scss" scoped>
.error-dialog {
    width: 60vw;
}
</style>
