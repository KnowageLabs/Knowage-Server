<template>
    <div v-if="target">
        <div class="p-d-flex p-flex-row p-ai-center scorecards-target-container">
            <div class="p-d-flex p-ai-center p-ml-5">
                <Button v-if="!expanded" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" @click="expanded = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="expanded = false" />
                <i class="fa fa-bullseye fa-lg p-mr-1 scorecard-blue-icon" />
                <InputText class="kn-material-input scorecards-target-name-input" v-model="target.name"  :maxLength="40" @input="$emit('touched', false)" />
            </div>
            <div class="p-d-flex p-flex-row p-ai-center">
                <SelectButton class="p-mr-1" v-model="selectedCriteria" :options="descriptor.criteriaOptions" @change="onCriteriaChange">
                    <template #option="slotProps">
                        <span v-tooltip="getSelectedCriteriaTooltip(slotProps.option, $t)" :data-test="'select-button-' + slotProps.option">{{ slotProps.option }}</span>
                    </template>
                </SelectButton>
                <MultiSelect v-if="selectedCriteria !== 'M'" :style="descriptor.style.multiselect" v-model="target.options.criterionPriority" :options="target.kpis" optionLabel="name" optionValue="name" @change="onCriterionPriortyChanged" data-test="criteria-select-input"></MultiSelect>
            </div>

            <div class="p-ml-auto">
                <Button icon="fa-solid fa-square-plus" class="p-button-text p-button-rounded p-button-plain" v-tooltip.top="$t('managers.scorecards.addKpi')" @click="openKpiDialog" />
                <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deleteTargetConfirm" />
            </div>
        </div>
        <div v-if="expanded">
            <div v-if="target.kpis.length === 0" class="p-d-flex p-flex-row p-ai-center scorecards-kpi-container">
                <ScorecardsTableHint :hint="'managers.scorecards.addKpiHint'"></ScorecardsTableHint>
            </div>
            <template v-else>
                <div v-for="(kpi, index) in target.kpis" :key="index" class="p-d-flex p-flex-row p-ai-center scorecards-kpi-container">
                    <div class="scorecards-kpi-info">
                        <i class="fas fa-square fa-2xl p-mr-2" :class="getKpiIconColorClass(kpi)"></i>
                        <span> {{ kpi.name }}</span>
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
import { getSelectedCriteriaTooltip, getKpiIconColorClass, getSelectedCriteria } from '../ScorecardsHelpers'
import MultiSelect from 'primevue/multiselect'
import SelectButton from 'primevue/selectbutton'
import descriptor from './ScorecardsTableDescriptor.json'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import ScorecardsKpiDialog from '../ScorecardsKpiDialog/ScorecardsKpiDialog.vue'

export default defineComponent({
    name: 'scorecards-target-item',
    components: { MultiSelect, SelectButton, ScorecardsTableHint, ScorecardsKpiDialog },
    props: { propTarget: { type: Object as PropType<iScorecardTarget> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true }, kpis: { type: Array as PropType<iKpi[]>, required: true } },
    emits: ['deleteTarget', 'openKpiDialog', 'touched'],
    data() {
        return {
            descriptor,
            target: null as iScorecardTarget | null,
            expanded: false,
            selectedCriteria: 'M',
            kpiDialogVisible: false,
            getSelectedCriteriaTooltip,
            getKpiIconColorClass
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
            if (this.target.name === 'New Target') this.expanded = true
            this.selectedCriteria = getSelectedCriteria(this.target.criterion?.valueCd)
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
        openKpiDialog() {
            this.kpiDialogVisible = true
        },
        onKpiSelected(selectedKpis: iKpi[]) {
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
        onCriterionPriortyChanged() {
            this.$emit('touched', true)
            if (this.target) this.target.updated = true
        }
    }
})
</script>

<style lang="scss" scoped>
.scorecards-target-container {
    border-top: 1px solid #cccccc;
}

.scorecards-kpi-info {
    margin-left: 6rem;
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
    .p-selectbutton > div {
        justify-content: center;
    }

    .scorecards-kpi-container {
        border-top: 1px solid var(--kn-list-border-color);
    }
    color: #cccccc;
}

.scorecards-criteria-multiselect {
    width: 50%;
}

.scorecards-target-name-input {
    border: none;
}
</style>
