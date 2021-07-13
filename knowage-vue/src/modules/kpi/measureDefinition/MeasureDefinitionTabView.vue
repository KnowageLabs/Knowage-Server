<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ rule.id ? rule.name : $t('kpi.measureDefinition.newMeasure') }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card" v-else>
        <TabView>
            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.query') }}</span>
                </template>

                <QueryCard :rule="rule" :datasourcesList="datasourcesList" :aliases="availableAliasList" :placeholders="placeholdersList"></QueryCard>
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.measureDefinition.metadata') }}</span>
                </template>

                <MetadataCard :currentRule="rule" :tipologiesType="domainsKpiRuleoutput" :domainsTemporalLevel="domainsTemporalLevel" :categories="domainsKpiMeasures"></MetadataCard>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDatasource, iRule } from './MeasureDefinition'
import axios from 'axios'
import MetadataCard from './card/MetadataCard/MetadataCard.vue'
import QueryCard from './card/QueryCard/QueryCard.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'rule-definition-detail',
    components: { MetadataCard, QueryCard, TabView, TabPanel },
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
            rule: {} as iRule,
            datasourcesList: [] as iDatasource[],
            availableAliasList: [],
            notAvailableAliasList: [],
            placeholdersList: [],
            domainsKpiRuleoutput: [],
            domainsTemporalLevel: [],
            domainsKpiMeasures: [],
            loading: false,
            touched: false
        }
    },
    computed: {},
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
        // console.log('ALISASES available: ', this.availableAliasList)
        // console.log('ALISASES not available: ', this.notAvailableAliasList)
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
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listAvailableAlias`).then((response) => {
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
