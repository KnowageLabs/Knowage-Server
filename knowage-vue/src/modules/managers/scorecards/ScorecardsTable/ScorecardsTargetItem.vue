<template>
    <div v-if="target">
        <div class="p-d-flex p-flex-row p-ai-center p-m-2 scorecards-target-container">
            <div class="kn-flex">
                <Button v-if="!expanded" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = false" />
                <i class="fa fa-bullseye fa-lg p-mr-2" />
                <span>
                    <InputText class="kn-material-input scorecards-target-name-input" v-model="target.name" @input="$emit('touched', false)" />
                </span>
            </div>
            <div class="kn-flex p-d-flex p-flex-row">
                <SelectButton v-model="selectedCriteria" :options="scorecardsTableDescriptor.criteriaOptions" @change="onCriteriaChange">
                    <template #option="slotProps">
                        <span v-tooltip="getSelectedCriteriaTooltip(slotProps.option)" :data-test="'select-button-' +slotProps.option">{{ slotProps.option }}</span>
                    </template>
                </SelectButton>
                <MultiSelect v-if="selectedCriteria !== 'M'" class="p-ml-3 scorecards-criteria-multiselect" v-model="target.options.criterionPriority" :options="target.kpis" optionLabel="name" optionValue="name" @change="onCriterionPriortyChanged"  data-test="criteria-select-input"></MultiSelect>
            </div>

            <div>
                <Button icon="fa-solid fa-square-plus" class="p-button-text p-button-rounded p-button-plain" @click="openKpiDialog" />
                <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteTargetConfirm" />
            </div>
        </div>
        <div v-if="expanded">
            <ScorecardsTableHint v-if="target.kpis.length === 0" class="p-m-4" :hint="'managers.scorecards.addKpiHint'"></ScorecardsTableHint>
            <template v-else>
                <div v-for="(kpi, index) in target.kpis" :key="index" class="scorecards-kpi-container p-d-flex">
                    <div class="scorecards-kpi-info">
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

        <ScorecardsKpiDialog :visible="kpiDialogVisible" :propKpis="kpis" :selectedKpis="target.kpis" @close="kpiDialogVisible = false" @kpiSelected="onKpiSelected"></ScorecardsKpiDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iScorecardTarget, iScorecardCriterion, iKpi } from '../Scorecards'
import MultiSelect from 'primevue/multiselect'
import SelectButton from 'primevue/selectbutton'
import scorecardsTableDescriptor from './ScorecardsTableDescriptor.json'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import ScorecardsKpiDialog from '../ScorecardsKpiDialog/ScorecardsKpiDialog.vue'

export default defineComponent({
    name: 'scorecards-target-item',
    components: { MultiSelect, SelectButton, ScorecardsTableHint, ScorecardsKpiDialog },
    props: { propTarget: { type: Object as PropType<iScorecardTarget> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true }, kpis: { type: Array as PropType<iKpi[]>, required: true } },
    emits: ['deleteTarget', 'openKpiDialog', 'touched'],
    data() {
        return {
            scorecardsTableDescriptor,
            target: null as iScorecardTarget | null,
            expanded: false,
            selectedCriteria: 'M',
            kpiDialogVisible: false
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
            //console.log('>>> LOADED TARGET: ', this.target)
            if (this.target.name === 'New Target') this.expanded = true
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
                this.$emit('touched', false)
            }
        },
        onCriteriaChange() {
            if (!this.target) return
            for (let i = 0; i < this.criterias.length; i++) {
                if ((this.selectedCriteria === 'M' && this.criterias[i].valueCd === 'MAJORITY') || (this.selectedCriteria === 'MP' && this.criterias[i].valueCd === 'MAJORITY_WITH_PRIORITY') || (this.selectedCriteria === 'P' && this.criterias[i].valueCd === 'PRIORITY')) {
                    this.target.criterion = this.criterias[i]
                    this.$emit('touched', true)
                    this.target.updated = true
                    break
                }
            }
        },
        getKpiIconColorClass(kpi: iKpi) {
            //.log('KPI: ', kpi)
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
        openKpiDialog() {
            this.kpiDialogVisible = true
        },
        onKpiSelected(selectedKpis: iKpi[]) {
            //console.log('SELECTYED KPIS: ', selectedKpis)
            if (this.target) {
                this.target.kpis = selectedKpis
                this.$emit('touched', true)
                this.target.updated = true
                this.expanded = true
            }
            this.kpiDialogVisible = false
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
            if (index !== -1) {
                this.target.kpis.splice(index, 1)
                if (this.target.criterion.valueCd !== 'MAJORITY') {
                    const index = this.target.options.criterionPriority.findIndex((criteria: string) => criteria === kpi.name)
                    if (index !== -1) this.target.options.criterionPriority.splice(index, 1)
                }
                this.$emit('touched', true)
                this.target.updated = true
            }
        },
        deleteTargetConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteTarget', this.target)
            })
        },
        getSelectedCriteriaTooltip(option: string) {
            switch (option) {
                case 'M':
                    return this.$t('managers.scorecards.majority')
                case 'MP':
                    return this.$t('managers.scorecards.majorityWithPriority')
                case 'P':
                    return this.$t('managers.scorecards.priority')
                default:
                    return ''
            }
        },
        onCriterionPriortyChanged() {
            this.$emit('touched', true)
            if (this.target) this.target.updated = true
        }
    }
})
</script>

<style lang="scss">
.p-selectbutton > div {
    justify-content: center;
}
</style>

<style lang="scss" scoped>
.scorecards-target-container {
    padding-bottom: 1rem;
    border-bottom: 1px solid #cccccc;
}
.scorecards-item-expand-icon {
    color: white;
    margin-left: 4rem;
}

.scorecards-kpi-container {
    margin: 1rem 1rem 1rem 1rem;
    border-bottom: 1px solid #cccccc;
}

.scorecards-kpi-info {
    margin-left: 11rem;
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

.scorecards-criteria-multiselect {
    width: 50%;
}

.scorecards-target-name-input {
    border: none;
}
</style>
