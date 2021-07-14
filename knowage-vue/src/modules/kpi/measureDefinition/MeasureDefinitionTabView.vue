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

                <QueryCard :rule="rule" :datasourcesList="datasourcesList" :aliases="availableAliasList" :placeholders="placeholdersList"></QueryCard>
            </TabPanel>

            <TabPanel :disabled="metadataDisabled">
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>

                <MetadataCard
                    :currentRule="rule"
                    :tipologiesType="domainsKpiRuleoutput"
                    :domainsTemporalLevel="domainsTemporalLevel"
                    :categories="domainsKpiMeasures"
                    :availableAliases="availableAliasList"
                    :notAvailableAliasList="notAvailableAliasList"
                    :changed="tabChanged"
                    @close="activeTab = 0"
                ></MetadataCard>
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
            <h2>{{ $t('common.new') }}</h2>
            <Chip v-for="alias in newAlias" :key="alias.id" :label="alias.name"></Chip>
        </div>
        <!--
        <div v-if="reusedAlias.length > 0">
            <h2>{{ $t('common.reused') }}</h2>
            <Chip v-for="alias in reusedAlias" :key="alias.id" :label="alias.name"></Chip>
        </div> -->

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="showSaveDialog = false"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="handleSubmit"></Button>
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
                definition: 'SELECT\n\nFROM\n\nWHERE',
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
            tabChanged: false,
            loading: false,
            touched: false,
            showSaveDialog: false
        }
    },
    computed: {
        metadataDisabled() {
            let disabled = false
            console.log('TEESTSTSTSTS', this.rule.dataSource)
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
            console.log('RULE TEEEEEST', this.rule)
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
        setTabChanged(tabIndex: any) {
            this.activeTab = tabIndex
            if (tabIndex === 1) {
                this.tabChanged = !this.tabChanged
            }
        },
        setNewAliases() {
            console.log('SET NEW ALIASES')
            console.log('RULE ', this.rule)
            this.newAlias = []
            this.rule.ruleOutputs.forEach((ruleOutput: any) => {
                this.availableAliasList.forEach((alias: any) => {
                    console.log('NEW ALIAS: ' + alias.name.toUpperCase() + ' === ' + ruleOutput.toUpperCase())
                    if (alias.name.toUpperCase() === ruleOutput.toUpperCase()) {
                        console.log('NEW ALIAS', alias.name)
                        this.newAlias.push(alias)
                    }
                })
            })
        },
        submitConfirm() {
            if (!this.touched) {
                this.showSaveDialog = true
                this.setNewAliases()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.showSaveDialog = true
                        this.setNewAliases()
                    }
                })
            }
            console.log('caaaaled', this.showSaveDialog)
        },
        handleSubmit() {},
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
