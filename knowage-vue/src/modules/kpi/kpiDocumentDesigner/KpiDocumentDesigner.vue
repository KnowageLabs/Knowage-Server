<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0 p-p-0">
            <div class="kn-page-content p-grid p-m-0">
                <div class="p-col-12 p-p-0">
                    <Toolbar class="kn-toolbar kn-toolbar--primary">
                        <template #start>
                            {{ $t('kpi.kpiDocumentDesigner.title') }}
                        </template>
                        <template #end>
                            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="saveButtonDisabled" @click="saveKpi" />
                            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeKpi" />
                        </template>
                    </Toolbar>
                    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />

                    <div v-if="kpiDesigner" class="p-d-flex p-flex-column p-m-0">
                        <KpiDocumentDesignerTypeCard v-if="showScorecards" class="kn-flex" :chartType="kpiDesigner.chart.type" @typeChanged="onTypeChanged"></KpiDocumentDesignerTypeCard>
                        <KpiDocumentDesignerDocumentTypeCard v-if="kpiDesigner.chart.type === 'kpi'" class="kn-flex" :propChart="kpiDesigner.chart"></KpiDocumentDesignerDocumentTypeCard>
                        <KpiDocumentDesignerKpiListCard v-if="kpiDesigner.chart.type === 'kpi'" class="kn-flex" :propData="kpiDesigner.chart.data" :kpiList="kpiList" :documentType="kpiDesigner.chart.model"></KpiDocumentDesignerKpiListCard>
                        <KpiDocumentDesignerScorecardsListCard v-else class="kn-flex" :propData="kpiDesigner.chart.data" :scorecardList="scorecards" @scorecardChanged="onScorecardChanged"></KpiDocumentDesignerScorecardsListCard>
                        <div class="p-d-flex p-flex-row kn-flex p-p-0 p-m-0">
                            <KpiDocumentDesignerStyleCard class="kn-flex" :propStyle="kpiDesigner.chart.style"></KpiDocumentDesignerStyleCard>
                            <KpiDocumentDesignerOptionsCard v-if="kpiDesigner.chart.type === 'kpi'" class="kn-flex" :propOptions="kpiDesigner.chart.options"></KpiDocumentDesignerOptionsCard>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <KpiDocumentDesignerSaveDialog :visible="saveDialogVisible" @close="saveDialogVisible = false" @saveKpi="onKpiSave"></KpiDocumentDesignerSaveDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iKpi, iScorecard, iKpiDesigner } from './KpiDocumentDesigner'
import KpiDocumentDesignerDocumentTypeCard from './KpiDocumentDesignerDocumentTypeCard/KpiDocumentDesignerDocumentTypeCard.vue'
import KpiDocumentDesignerKpiListCard from './KpiDocumentDesignerKpiListCard/KpiDocumentDesignerKpiListCard.vue'
import KpiDocumentDesignerOptionsCard from './KpiDocumentDesignerOptionsCard/KpiDocumentDesignerOptionsCard.vue'
import KpiDocumentDesignerStyleCard from './KpiDocumentDesignerStyleCard/KpiDocumentDesignerStyleCard.vue'
import KpiDocumentDesignerTypeCard from './KpiDocumentDesignerTypeCard/KpiDocumentDesignerTypeCard.vue'
import KpiDocumentDesignerSaveDialog from './KpiDocumentDesignerSaveDialog/KpiDocumentDesignerSaveDialog.vue'
import KpiDocumentDesignerScorecardsListCard from './KpiDocumentDesignerScorecardsListCard/KpiDocumentDesignerScorecardsListCard.vue'
import { mapState } from 'vite'
import mainStore from '../../../App.store'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'kpi-document-designer',
    components: { KpiDocumentDesignerDocumentTypeCard, KpiDocumentDesignerKpiListCard, KpiDocumentDesignerOptionsCard, KpiDocumentDesignerStyleCard, KpiDocumentDesignerTypeCard, KpiDocumentDesignerSaveDialog, KpiDocumentDesignerScorecardsListCard },
    props: { id: { type: String } },
    data() {
        return {
            kpiDesigner: null as iKpiDesigner | null,
            kpiList: [] as iKpi[],
            scorecards: [] as iScorecard[],
            saveDialogVisible: false,
            loading: false
        }
    },
    watch: {
        async id() {
            await this.loadKpi()
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        }),
        showScorecards(): boolean {
            return (this.store.$state as any).user.functionalities.includes('ScorecardsManagement')
        },
        saveButtonDisabled(): boolean {
            return this.kpiDesigner !== null && ((this.kpiDesigner.chart.type === 'kpi' && this.kpiTypeInvalid()) || (this.kpiDesigner.chart.type === 'scorecard' && this.scorecardTypeInvalid()))
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            let config = {
                headers: { Accept: 'application/json, text/plain, */*' }
            }

            let language = this.user.locale.split('_')[0]
            let country = this.user.locale.split('_')[1]
            await this.$http.get(process.env.VUE_APP_KPI_ENGINE_API_URL + `1.0/pages/edit?SBI_LANGUAGE=${language}&SBI_COUNTRY=${country}&user_id=${this.user.userUniqueIdentifier}&document=${this.id}`, config).then(() => {})

            await this.loadKpi()
            await this.loadKpiList()
            await this.loadScorecards()
            this.loading = false
        },
        async loadKpiList() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/listKpi`).then((response: AxiosResponse<any>) => (this.kpiList = response.data))
        },
        async loadScorecards() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpiee/listScorecard`).then((response: AxiosResponse<any>) => (this.scorecards = response.data))
        },
        async loadKpi() {
            this.loading = true
            if (this.id) {
                await this.$http
                    .post(process.env.VUE_APP_KPI_ENGINE_API_URL + `1.0/kpisTemplate/getKpiTemplate`, { id: this.id })
                    .then((response: AxiosResponse<any>) => {
                        this.kpiDesigner = response.data.templateContent ? JSON.parse(response.data.templateContent) : response.data

                        if (this.kpiDesigner && !this.kpiDesigner.chart) this.kpiDesigner = this.initializeKpiDesigner()
                    })
                    .catch(() => {})
            } else {
                this.kpiDesigner = this.initializeKpiDesigner()
            }
            this.loading = false
        },
        initializeKpiDesigner() {
            return {
                chart: {
                    type: 'kpi',
                    model: 'widget',
                    data: { kpi: [] },
                    style: { font: { color: 'rgb(14,13,13)', fontFamily: 'roboto', fontWeight: 'normal', size: '8px' } },
                    options: {
                        showtarget: true,
                        showtargetpercentage: false,
                        showthreshold: true,
                        showvalue: true,
                        vieweas: '',
                        history: {
                            size: 1,
                            units: 'month'
                        }
                    }
                }
            } as iKpiDesigner
        },
        onTypeChanged(value: string) {
            if (this.kpiDesigner) this.kpiDesigner.chart.type = value
        },
        kpiTypeInvalid() {
            return this.kpiDesigner && (this.kpiDesigner.chart.data.kpi?.length === 0 || this.kpiMissingRequiredField())
        },
        kpiMissingRequiredField(): boolean {
            if (!this.kpiDesigner || !this.kpiDesigner.chart.data.kpi) return false

            let missingField = false
            for (let i = 0; i < this.kpiDesigner.chart.data.kpi.length; i++) {
                const tempKpi = this.kpiDesigner.chart.data.kpi[i]
                if (!tempKpi.rangeMinValue || !tempKpi.rangeMaxValue) {
                    missingField = true
                    break
                }
            }

            return missingField
        },
        scorecardTypeInvalid() {
            return !this.kpiDesigner || !this.kpiDesigner.chart.data.scorecard?.name
        },
        onScorecardChanged(scorecard: iScorecard | null) {
            if (!this.kpiDesigner) return

            if (scorecard) {
                this.kpiDesigner.chart.data.scorecard = { name: scorecard.name }
            } else {
                this.kpiDesigner.chart.data.scorecard = { name: '' }
            }
        },
        saveKpi() {
            if (!this.id) {
                this.saveDialogVisible = true
            } else {
                this.onKpiSave('')
            }
        },
        async onKpiSave(kpiName: string) {
            this.loading = true

            if (this.id) {
                await this.updateKpi()
            } else {
                await this.saveNewKpi(kpiName)
            }

            this.loading = false
        },
        async saveNewKpi(kpiName: string) {
            const postData = {
                document: {
                    name: kpiName,
                    label: kpiName,
                    description: '',
                    type: 'KPI'
                },
                customData: {
                    templateContent: this.getFormattedKpiDesigner()
                },
                action: 'DOC_SAVE'
            }
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/saveDocument`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.store.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$router.push(`/kpi-edit/${response.data.id}?from=${this.$route.query.from}`)
                })
                .catch(() => {})
                .finally(() => (this.saveDialogVisible = false))
            this.loading = false
        },
        async updateKpi() {
            const postData = new URLSearchParams()
            postData.append('docLabel', this.id as string)
            postData.append('jsonTemplate', JSON.stringify(this.getFormattedKpiDesigner()))

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/saveKpiTemplate`, postData, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        Accept: 'application/json, text/plain, */*'
                    }
                })
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                })
                .catch(() => {})
        },
        getFormattedKpiDesigner() {
            const tempDesigner = deepcopy(this.kpiDesigner)

            if (tempDesigner.chart.type === 'kpi') {
                delete tempDesigner.chart.data.scorecard
            } else {
                delete tempDesigner.chart.data.kpi
                delete tempDesigner.chart.options
            }

            return tempDesigner
        },
        closeKpi() {
            const url = this.$route.query.from === 'documentDetail' ? `/document-details/${this.id}` : '/workspace/analysis'
            this.$router.push(url)
        }
    }
})
</script>
