<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ rule.id ? rule.name : $t('kpi.measureDefinition.newMeasure') }} </template>
        <template #right>
            <Button class="p-button-text p-button-rounded kn-button" :label="$t('kpi.measureDefinition.alias')" @click="aliasesVisible = !aliasesVisible" data-test="submit-button" />
            <Button class="p-button-text p-button-rounded kn-button" :label="$t('kpi.measureDefinition.placeholder')" @click="placeholderVisible = !placeholderVisible" data-test="submit-button" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="submitConfirm" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex p-flex-row">
        <div class="card kn-flex" v-if="!loading">
            <TabView v-model:activeIndex="activeTab" @tab-change="setTabChanged($event.index)">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.measureDefinition.query') }}</span>
                    </template>

                    <QueryCard :rule="rule" :datasourcesList="datasourcesList" :aliases="availableAliasList" :placeholders="placeholdersList" :columns="columns" :rows="rows" :codeInput="codeInput" @queryChanged="queryChanged = true" @loadPreview="previewQuery(false, true)"></QueryCard>
                </TabPanel>

                <TabPanel :disabled="metadataDisabled">
                    <template #header>
                        <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                    </template>

                    <MetadataCard :currentRule="rule" :tipologiesType="domainsKpiRuleoutput" :domainsTemporalLevel="domainsTemporalLevel" :categories="domainsKpiMeasures"></MetadataCard>
                </TabPanel>
            </TabView>
        </div>
        <div v-if="aliasesVisible" class="listbox">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>{{ $t('kpi.measureDefinition.alias') }}</template>
                <template #right>
                    <Button class="p-button-text p-button-rounded kn-button" :label="$t('common.sort')" @click="sortArray(availableAliasList, aliasSorted)" />
                </template>
            </Toolbar>
            <Listbox
                class="kn-list"
                :options="availableAliasList"
                :listStyle="metadataDefinitionTabViewDescriptor.listBox.style"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="name"
                filterMatchMode="contains"
                :filterFields="metadataDefinitionTabViewDescriptor.aliasFilterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="setCodeInput($event.value.name)"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template></Listbox
            >
        </div>
        <div v-if="placeholderVisible" class="listbox">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>{{ $t('kpi.measureDefinition.placeholder') }}</template>
                <template #right>
                    <Button class="p-button-text p-button-rounded kn-button" :label="$t('common.sort')" @click="sortArray(placeholdersList, placeholderVisible)" />
                </template>
            </Toolbar>

            <Listbox
                class="kn-list"
                :options="placeholdersList"
                listStyle="max-height:calc(100% - 62px)"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="name"
                filterMatchMode="contains"
                :filterFields="metadataDefinitionTabViewDescriptor.placeholderFilterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="setCodeInput($event.value.name)"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template></Listbox
            >
        </div>
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
            <Chip v-for="alias in newAlias" class="p-m-2" :key="alias.id" :label="alias"></Chip>
        </div>

        <div v-if="reusedAlias.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="alias in reusedAlias" class="p-m-2" :key="alias.id" :label="alias"></Chip>
        </div>

        <Toolbar v-if="newPlaceholder.length > 0 || reusedPlaceholder.length > 0" class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('kpi.measureDefinition.placeholder') }}
            </template>
        </Toolbar>

        <div v-if="newPlaceholder.length > 0">
            {{ newPlaceholder }}
            <h4>{{ $t('common.new') }}</h4>
            <Chip v-for="placeholder in newPlaceholder" class="p-m-2" :key="placeholder.id" :label="placeholder"></Chip>
        </div>

        <div v-if="reusedPlaceholder.length > 0">
            {{ reusedPlaceholder }}
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="placeholder in reusedPlaceholder" class="p-m-2" :key="placeholder.id" :label="placeholder"></Chip>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="showSaveDialog = false"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="saveRule" :disabled="saveRuleButtonDisabled"></Button>
        </template>
    </Dialog>

    <Dialog :modal="true" :visible="errorMessage" :header="errorTitle" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary error-dialog" :closable="false">
        <h1>{{ $t('kpi.measureDefinition.metadataError') + ' ' + $t('kpi.measureDefinition.wrongQuery') }}</h1>
        <p>{{ errorMessage }}</p>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeErrorMessageDialog"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDatasource, iMeasure, iRule, iPlaceholder } from './MeasureDefinition'
import axios from 'axios'
import Chip from 'primevue/chip'
import Dialog from 'primevue/dialog'
import Listbox from 'primevue/listbox'
import MetadataCard from './card/MetadataCard/MetadataCard.vue'
import metadataDefinitionTabViewDescriptor from './MetadataDefinitionTabViewDescriptor.json'
import QueryCard from './card/QueryCard/QueryCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

// TODO Fix title

export default defineComponent({
    name: 'measure-definition-detail',
    components: { Chip, Dialog, Listbox, MetadataCard, QueryCard, TabView, TabPanel },
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
                definition: "SELECT * FROM account WHERE account_description = '@Description' AND  account_description = '@Test'",
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
            aliasSorted: 'DESC',
            placeholdersSorted: 'DESC'
        }
    },
    computed: {
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
        saveRuleButtonDisabled(): Boolean {
            return !this.rule.name
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
            await this.previewQuery(false, false)
            console.log('METADATA TAB RULE ', this.rule)

            this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                this.setAliasIcon(ruleOutput)
                if (!ruleOutput.category) {
                    ruleOutput.category = { valueCd: '' }
                }
                // if (!ruleOutput.hierarchy) {
                //     ruleOutput.hierarchy = { valueCd: null }
                // }
            })

            console.log('ERROR MESSAGE: ', this.errorMessage)
            if (this.errorMessage) {
                console.log('STIGAO!!!')
                this.activeTab = 0
            }
        },
        setNewAliases() {
            // console.log('SET NEW ALIASES')
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
            // console.log('NEW ALIASES', this.newAlias)
        },
        setNewPlaceholders() {
            // console.log('SET NEW ALIASES')
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
            // console.log('NEW PLACEHOLDERS', this.newPlaceholder)
        },
        placeholderExists(name: string) {
            console.log('PLACEHOLDER EXISTS: ', this.rule)
            let exists = false
            this.placeholdersList.forEach((placeholder: any) => {
                console.log('Placeholder Exists: ' + placeholder.name.toUpperCase() + ' === ' + name.toUpperCase())
                if (placeholder.name.toUpperCase() === name.toUpperCase()) {
                    // console.log('ALIAS Exists!')
                    exists = true
                }
            })
            return exists
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
            this.setNewPlaceholders()
            console.log('Rule after prepare', this.rule)
            if (save) {
                await this.preSaveRule()
            }

            this.rule.dataSource = tempDatasource
            this.rule.ruleOutputs = tempRuleOutputs
        },
        async preSaveRule() {
            const tempDataSource = this.rule.dataSource
            const tempRuleOutputs = this.rule.ruleOutputs
            delete this.rule.dataSource
            this.rule.ruleOutputs.forEach((ruleOutput) => {
                delete ruleOutput.aliasIcon
                ruleOutput.category = { valueCd: ruleOutput.category?.valueCd as string }
            })

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

            this.rule.dataSource = tempDataSource
            this.rule.ruleOutputs = tempRuleOutputs
        },
        async saveRule() {
            if (this.rule.id) {
                this.operation = 'update'
            }

            delete this.rule.dataSource
            this.rule.ruleOutputs.forEach((ruleOutput) => {
                delete ruleOutput.aliasIcon
                ruleOutput.category = { valueCd: ruleOutput.category?.valueCd as string }
            })

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
            console.log('RULE IN LOAD PLACEHOLDER', this.rule)
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
        setCodeInput(input: string) {
            console.log('setCodeInput: ', input)
            if (this.activeTab === 0) {
                this.codeInput = input
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
