<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-8 p-md-6 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.scorecards.scorecardDesigner') }}
                    </template>
                </Toolbar>

                <Card v-if="scorecard" class="p-m-3">
                    <template #content>
                        <div class="p-fluid p-formgrid p-grid p-m-2">
                            <div class="p-field p-col-6">
                                <span class="p-float-label">
                                    <InputText
                                        class="kn-material-input"
                                        v-model="scorecard.name"
                                        :class="{
                                            'p-invalid': !scorecard.name && nameTouched
                                        }"
                                    />
                                    <label class="kn-material-input-label"> {{ $t('common.name') }}</label>
                                </span>
                                <div v-if="!scorecard.name && nameTouched" class="p-error">
                                    <small class="p-col-12"> {{ $t('common.validation.required', { fieldName: $t('documentExecution.olap.crossNavigationDefinition.parameterName') }) }} </small>
                                </div>
                            </div>

                            <div class="p-field p-col-6">
                                <span class="p-float-label">
                                    <InputText class="kn-material-input" v-model="scorecard.description" />
                                    <label class="kn-material-input-label"> {{ $t('common.description') }}</label>
                                </span>
                            </div>
                        </div>
                    </template>
                </Card>

                <ScorecardsTable v-if="scorecard" :propScorecard="scorecard" :criterias="criterias"></ScorecardsTable>
            </div>

            <div class="p-col-4 p-sm-4 p-md-6 p-p-0 p-m-0">
                PERSPECTIVES LIST
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iScorecard, iScorecardCriterion } from './Scorecards'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import ScorecardsTable from './ScorecardsTable/ScorecardsTable.vue'

export default defineComponent({
    name: 'scorecards-designer',
    components: { Card, ScorecardsTable },
    props: { id: { type: String } },
    data() {
        return {
            scorecard: null as iScorecard | null,
            nameTouched: false,
            criterias: [] as iScorecardCriterion[]
        }
    },
    watch: {
        async id() {
            await this.loadScorecard()
        }
    },
    async created() {
        await this.loadScorecard()
        await this.loadCriterias()
    },
    methods: {
        async loadScorecard() {
            this.$store.commit('setLoading', true)
            if (this.id) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpiee/${this.id}/loadScorecard`).then((response: AxiosResponse<any>) => (this.scorecard = response.data))
            } else {
                this.scorecard = { name: '', description: '', perspectives: [] }
            }
            this.$store.commit('setLoading', false)
            console.log('LOADED SCORECARD: ', this.scorecard)
        },
        async loadCriterias() {
            this.$store.commit('setLoading', true)
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/KPI_SCORECARD_CRITE`).then((response: AxiosResponse<any>) => (this.criterias = response.data))
            this.$store.commit('setLoading', false)

            console.log('LOADED CRITERIA: ', this.criterias)
        }
    }
})
</script>
