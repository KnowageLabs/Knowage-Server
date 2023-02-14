<template>
    <div class="p-d-flex kn-flex" :style="descriptor.style.mainDialog">
        <div class="kn-list--column" :style="descriptor.style.designer">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.scorecards.scorecardDesigner') }}
                </template>
                <template #end>
                    <Button class="kn-button p-button-text" :disabled="saveButtonDisabled" @click="saveScorecard">{{ $t('common.save') }}</Button>
                    <Button class="kn-button p-button-text" @click="close">{{ $t('common.close') }}</Button>
                </template>
            </Toolbar>

            <Card v-if="scorecard" class="p-m-2">
                <template #content>
                    <div class="p-fluid p-formgrid p-grid">
                        <div class="p-field p-col-6">
                            <span class="p-float-label">
                                <InputText
                                    class="kn-material-input"
                                    v-model="scorecard.name"
                                    :class="{
                                        'p-invalid': !scorecard.name && nameTouched
                                    }"
                                    @input="touched = true"
                                />
                                <label class="kn-material-input-label"> {{ $t('common.name') + ' *' }}</label>
                            </span>
                            <div v-if="!scorecard.name && nameTouched" class="p-error">
                                <small class="p-col-12"> {{ $t('common.validation.required', { fieldName: $t('documentExecution.olap.crossNavigationDefinition.parameterName') }) }} </small>
                            </div>
                        </div>

                        <div class="p-field p-col-6">
                            <span class="p-float-label">
                                <InputText class="kn-material-input" v-model="scorecard.description" @input="touched = true" />
                                <label class="kn-material-input-label"> {{ $t('common.description') }}</label>
                            </span>
                        </div>
                    </div>
                </template>
            </Card>

            <ScorecardsTable v-if="scorecard" :propScorecard="scorecard" :criterias="criterias" :kpis="kpis" @touched="touched = true"></ScorecardsTable>
        </div>

        <div v-if="scorecard" id="sideMenu" class="kn-overflow" :style="descriptor.style.perspective">
            <KnPerspectiveCard class="p-m-4" v-for="(perspective, index) in scorecard.perspectives" :key="index" :propPerspective="perspective" :data-test="'perspective-' + perspective.name"></KnPerspectiveCard>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iScorecard, iScorecardCriterion, iKpi, iPerspective, iScorecardTarget } from './Scorecards'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import ScorecardsTable from './ScorecardsTable/ScorecardsTable.vue'
import KnPerspectiveCard from '@/components/UI/KnPerspectiveCard/KnPerspectiveCard.vue'
import descriptor from './ScorecardsDescriptor.json'
import mainStore from '../../../App.store'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'scorecards-designer',
    components: { Card, KnPerspectiveCard, ScorecardsTable },
    props: { id: { type: String } },
    data() {
        return {
            descriptor,
            scorecard: null as iScorecard | null,
            nameTouched: false,
            criterias: [] as iScorecardCriterion[],
            kpis: [] as iKpi[],
            touched: false
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return !this.scorecard || !this.scorecard.name || this.scorecard.perspectives.length === 0
        }
    },
    watch: {
        async id() {
            await this.loadScorecard()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        await this.loadScorecard()
        await this.loadCriterias()
        await this.loadKpis()
    },
    methods: {
        async loadScorecard() {
            this.store.setLoading(true)
            if (this.id) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/${this.id}/loadScorecard`).then((response: AxiosResponse<any>) => (this.scorecard = response.data))
            } else {
                this.scorecard = { name: '', description: '', perspectives: [] }
            }
            this.store.setLoading(false)
        },
        async loadCriterias() {
            this.store.setLoading(true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/KPI_SCORECARD_CRITE`).then((response: AxiosResponse<any>) => (this.criterias = response.data))
            this.store.setLoading(false)
        },
        async loadKpis() {
            this.store.setLoading(true)
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/listKpiWithResult`).then((response: AxiosResponse<any>) => (this.kpis = response.data))
            this.store.setLoading(false)
        },
        async saveScorecard() {
            const tempScorecard = this.getFormattedScorecard()

            const operation = tempScorecard && tempScorecard.id ? 'update' : 'create'
            this.store.setLoading(true)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/saveScorecard`, tempScorecard)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.id && this.scorecard) {
                        this.store.setInfo({
                            title: this.$t('common.toast.' + operation + 'Title'),
                            msg: this.$t('common.toast.success')
                        })
                        this.scorecard.id = response.data.id
                        if (operation === 'create') this.$router.push(`/scorecards/${this.scorecard.id}`)
                        this.touched = false
                    }
                })
                .catch(() => {})
            this.store.setLoading(false)
        },
        getFormattedScorecard() {
            const tempScorecard = deepcopy(this.scorecard)
            if (!tempScorecard) return
            // TODO - BE needs to be changed for description
            delete tempScorecard.description

            const fieldToDelete = ['groupedKpis', 'statusColor', 'updated']
            tempScorecard.perspectives?.forEach((perspective: iPerspective) => {
                if (perspective.new) {
                    delete perspective.id
                    delete perspective.new
                }
                fieldToDelete.forEach((field: string) => delete perspective[field])
                perspective.targets?.forEach((target: iScorecardTarget) => {
                    if (target.new) {
                        delete target.id
                        delete target.new
                    }
                    fieldToDelete.forEach((field: string) => delete target[field])
                })
            })

            return tempScorecard
        },
        close() {
            if (!this.touched) {
                this.$router.push(`/scorecards`)
                this.scorecard = null
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(`/scorecards`)
                        this.scorecard = null
                    }
                })
            }
        }
    }
})
</script>
<style lang="scss">
@media screen and (max-width: 1024px) {
    #sideMenu {
        display: none;
    }
}
.scorecard-blue-icon {
    color: rgb(67, 116, 158);
}
</style>
