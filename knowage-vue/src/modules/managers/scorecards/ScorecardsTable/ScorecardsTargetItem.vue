<template>
    <div v-if="target">
        <div class="p-d-flex p-flex-row p-ai-center p-m-2">
            <div class="kn-flex">
                <Button v-if="!expanded" icon="fas fa-chevron-right" class="p-ml-5 p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-ml-5 p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = false" />
                <i class="fa-solid fa-bullseye-arrow p-mr-2" />
                <span>
                    {{ target.name }}
                </span>
            </div>
            <div class="kn-flex">
                <SelectButton v-model="selectedCriteria" :options="scorecardsTableDescriptor.criteriaOptions" @change="onCriteriaChange"></SelectButton>
            </div>

            <div class="kn-flex">
                <Button icon="fa-solid fa-square-plus" class="p-button-text p-button-rounded p-button-plain" />
                <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteTargetConfirm" />
            </div>
        </div>
        <div v-if="expanded">
            <ScorecardsTableHint v-if="target.kpis.length === 0" class="p-m-4" :hint="'managers.scorecards.addKpiHint'"></ScorecardsTableHint>
            <template v-else>
                <div v-for="(kpi, index) in target.kpis" :key="index" class="scorecards-kpi-container p-d-flex">
                    <div>
                        <i class="fas fa-square fa-2xl p-mr-2" :class="getKpiIconColorClass(kpi)"></i>
                        <span>
                            {{ kpi.name }}
                        </span>
                    </div>
                    <div class="p-ml-auto">
                        <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteKpiConfirm(kpi)" />
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iScorecardTarget, iScorecardCriterion, iKpi } from '../Scorecards'
import SelectButton from 'primevue/selectbutton'
import scorecardsTableDescriptor from './ScorecardsTableDescriptor.json'
import ScorecardsTableHint from './ScorecardsTableHint.vue'

export default defineComponent({
    name: 'scorecards-target-item',
    components: { SelectButton, ScorecardsTableHint },
    props: { propTarget: { type: Object as PropType<iScorecardTarget> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true } },
    emits: ['deleteTarget'],
    data() {
        return {
            scorecardsTableDescriptor,
            target: null as iScorecardTarget | null,
            expanded: false,
            selectedCriteria: 'M'
        }
    },
    watch: {
        propTarget() {
            this.loadTarget()
        }
    },
    async created() {
        this.loadTarget()
    },
    methods: {
        loadTarget() {
            this.target = this.propTarget as iScorecardTarget
            console.log('>>> LOADED TARGET: ', this.target)
            this.setSelectedCriteria(this.target)
        },
        setSelectedCriteria(target: iScorecardTarget) {
            if (target) {
                switch (target.criterion?.valueCd) {
                    case 'MAJORITY':
                        this.selectedCriteria = 'M'
                        break
                    case 'MAJORITY_WITH_PRIORITY':
                        this.selectedCriteria = 'MP'
                        break
                    case 'PRIORITY':
                        this.selectedCriteria = 'P'
                }
            }
        },
        onCriteriaChange() {
            if (!this.target) return
            for (let i = 0; i < this.criterias.length; i++) {
                if ((this.selectedCriteria === 'M' && this.criterias[i].valueCd === 'MAJORITY') || (this.selectedCriteria === 'MP' && this.criterias[i].valueCd === 'MAJORITY_WITH_PRIORITY') || (this.selectedCriteria === 'P' && this.criterias[i].valueCd === 'PRIORITY')) {
                    this.target.criterion = this.criterias[i]
                    break
                }
            }
        },
        getKpiIconColorClass(kpi: iKpi) {
            console.log('KPI: ', kpi)
            if (kpi.status) {
                switch (kpi.status) {
                    case 'RED':
                        return 'scorecard-kpi-icon-red'
                    case 'YELLOW':
                        return 'scorecard-kpi-icon-yellow'
                    case 'GREEN':
                        return 'scorecard-kpi-icon-green'
                    case 'GREY':
                        return 'scorecard-kpi-icon-grey'
                }
            } else {
                return 'scorecard-kpi-icon-light-grey'
            }
        },
        deleteKpiConfirm(kpi: iKpi) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteKpi(kpi)
            })
        },
        async deleteKpi(kpi: iKpi) {
            if (!this.target) return
            const index = this.target.kpis.findIndex((tempKpi: iKpi) => tempKpi.id === kpi.id)
            if (index !== -1) this.target.kpis.splice(index, 1)
        },
        deleteTargetConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteTarget', this.target)
            })
        }
    }
})
</script>

<style lang="scss">
.scorecards-item-expand-icon {
    color: white;
}

.scorecards-kpi-container {
    margin: 1rem 1rem 1rem 8rem;
}

.scorecard-kpi-icon-red {
    color: #ff5656;
}

.scorecard-kpi-icon-yellow {
    color: #ffee58;
}

.scorecard-kpi-icon-green {
    color: #50c550;
}

.scorecard-kpi-icon-grey {
    color: #b7b7b7;
}

.scorecard-kpi-icon-light-grey {
    color: #cccccc;
}
</style>
