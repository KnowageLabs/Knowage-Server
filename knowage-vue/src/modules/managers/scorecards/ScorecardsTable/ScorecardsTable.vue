<template>
    <div class=" p-d-flex p-flex-column kn-flex kn-overflow" v-if="scorecard">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('managers.scorecards.perspectives') }}
            </template>
            <template #end>
                <Button :label="$t('managers.scorecards.addPerspective')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="addPerspective" data-test="add-perspective-button" />
            </template>
        </Toolbar>

        <ScorecardsTableHint v-if="scorecard.perspectives.length === 0" class="p-my-2" :hint="'managers.scorecards.addPerspectiveHint'" data-test="no-perspective-hint"></ScorecardsTableHint>
        <div v-else class="kn-flex kn-overflow">
            <ScorecardsPerspectiveItem v-for="(perspective, index) in scorecard.perspectives" :key="index" :propPerspective="perspective" :criterias="criterias" :kpis="kpis" @deletePerspective="deletePerspective" @touched="$emit('touched')"></ScorecardsPerspectiveItem>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iScorecard, iScorecardCriterion, iPerspective, iKpi } from '../Scorecards'
import { getDefaultCriterion } from '../ScorecardsHelpers'
import ScorecardsPerspectiveItem from './ScorecardsPerspectiveItem.vue'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'scorecards-table',
    components: { ScorecardsPerspectiveItem, ScorecardsTableHint },
    props: { propScorecard: { type: Object as PropType<iScorecard> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true }, kpis: { type: Array as PropType<iKpi[]>, required: true } },
    emits: ['touched'],
    data() {
        return {
            scorecard: null as iScorecard | null
        }
    },
    watch: {
        propScorecard() {
            this.loadScorecard()
        }
    },
    created() {
        this.loadScorecard()
    },
    methods: {
        loadScorecard() {
            this.scorecard = this.propScorecard as iScorecard
        },
        addPerspective() {
            if (this.scorecard) {
                this.scorecard.perspectives.push({ id: +cryptoRandomString({ length: 16, type: 'base64' }), name: 'New Perspective', status: 'GRAY', criterion: getDefaultCriterion(this.criterias), options: { criterionPriority: [] }, targets: [], groupedKpis: [] })
                this.$emit('touched')
            }
        },
        deletePerspective(perspective: iPerspective) {
            if (!this.scorecard) return
            const index = this.scorecard.perspectives.findIndex((tempPerspective: iPerspective) => tempPerspective.id === perspective.id)
            if (index !== -1) {
                this.scorecard.perspectives.splice(index, 1)
                this.$emit('touched')
            }
        }
    }
})
</script>
