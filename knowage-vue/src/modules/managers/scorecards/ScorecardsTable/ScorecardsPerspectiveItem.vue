<template>
    <div id="perspective" v-if="perspective">
        <div class="p-d-flex p-flex-row p-ai-center">
            <div class="p-d-flex p-ai-center" :style="descriptor.style.inputContainer">
                <Button v-if="!expanded" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" @click="expanded = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="expanded = false" />
                <i class="fa-solid fa-rectangle-list fa-lg p-mr-1 scorecard-blue-icon" />
                <InputText class="kn-material-input scorecards-target-perspective-input" v-model="perspective.name" :maxLength="40" @input="$emit('touched')" />
            </div>
            <div class="p-d-flex p-flex-row p-ai-center kn-flex">
                <SelectButton class="p-mr-1" v-model="selectedCriteria" :options="descriptor.criteriaOptions" @change="onCriteriaChange">
                    <template #option="slotProps">
                        <span v-tooltip="getSelectedCriteriaTooltip(slotProps.option, $t)" :data-test="'select-button-' + slotProps.option">{{ slotProps.option }}</span>
                    </template>
                </SelectButton>
                <MultiSelect v-if="selectedCriteria !== 'M'" :style="descriptor.style.multiselect" v-model="perspective.options.criterionPriority" :options="perspective.targets" optionLabel="name" optionValue="name" @change="onCriterionPriortyChanged" data-test="criteria-select-input" />
            </div>

            <div class="p-d-flex p-ai-center">
                <Button icon="fa-solid fa-square-plus" class="p-button-text p-button-rounded p-button-plain" v-tooltip.top="$t('managers.scorecards.addTarget')" @click="addTarget" />
                <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deletePerspectiveConfirm" />
            </div>
        </div>
        <div v-if="expanded">
            <div v-if="perspective.targets.length === 0" class="p-d-flex p-flex-row p-ai-center scorecards-kpi-container">
                <ScorecardsTableHint :hint="'managers.scorecards.addTargetHint'" data-test="no-targets-hint"></ScorecardsTableHint>
            </div>
            <template v-else>
                <ScorecardsTargetItem v-for="(target, index) in perspective.targets" :key="index" :propTarget="target" :criterias="criterias" :kpis="kpis" @deleteTarget="deleteTarget" @openKpiDialog="$emit('openKpiDialog', $event)" @touched="onTargetTouched"></ScorecardsTargetItem>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPerspective, iScorecardCriterion, iScorecardTarget, iKpi } from '../Scorecards'
import { getSelectedCriteriaTooltip, getDefaultCriterion, getSelectedCriteria } from '../ScorecardsHelpers'
import MultiSelect from 'primevue/multiselect'
import SelectButton from 'primevue/selectbutton'
import descriptor from './ScorecardsTableDescriptor.json'
import ScorecardsTargetItem from './ScorecardsTargetItem.vue'
import ScorecardsTableHint from './ScorecardsTableHint.vue'
import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'scorecards-perspective-item',
    components: { MultiSelect, SelectButton, ScorecardsTargetItem, ScorecardsTableHint },
    props: { propPerspective: { type: Object as PropType<iPerspective> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true }, kpis: { type: Array as PropType<iKpi[]>, required: true } },
    emits: ['deletePerspective', 'openKpiDialog', 'touched'],
    data() {
        return {
            descriptor,
            perspective: null as iPerspective | null,
            expanded: false,
            selectedCriteria: 'M',
            getSelectedCriteriaTooltip
        }
    },
    watch: {
        propPerspective() {
            this.loadPerspective()
        }
    },
    async created() {
        this.loadPerspective()
    },
    methods: {
        loadPerspective() {
            this.perspective = this.propPerspective as iPerspective
            if (this.perspective.name === 'New Perspective') this.expanded = true
            this.selectedCriteria = getSelectedCriteria(this.perspective.criterion?.valueCd)
        },
        addTarget() {
            if (this.perspective) {
                this.perspective.targets.push({ id: cryptoRandomString({ length: 16, type: 'base64' }), name: 'New Target', status: 'GRAY', criterion: getDefaultCriterion(this.criterias), options: { criterionPriority: [] }, kpis: [], groupedKpis: [], new: true })
                this.$emit('touched')
                this.expanded = true
                this.perspective.updated = true
            }
        },
        onCriteriaChange() {
            if (!this.perspective) return

            for (let i = 0; i < this.criterias.length; i++) {
                if ((this.selectedCriteria === 'M' && this.criterias[i].valueCd === 'MAJORITY') || (this.selectedCriteria === 'MP' && this.criterias[i].valueCd === 'MAJORITY_WITH_PRIORITY') || (this.selectedCriteria === 'P' && this.criterias[i].valueCd === 'PRIORITY')) {
                    this.perspective.criterion = this.criterias[i]
                    this.$emit('touched')
                    this.perspective.updated = true
                    break
                }
            }

            this.perspective.options.criterionPriority = []
        },
        deletePerspectiveConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deletePerspective', this.perspective)
            })
        },
        deleteTarget(target: iScorecardTarget) {
            console.log('TARGET: ', target)
            if (!this.perspective) return
            const index = this.perspective.targets.findIndex((tempTarget: iScorecardTarget) => tempTarget.id === target.id)
            if (index !== -1) {
                this.perspective.targets.splice(index, 1)
                if (this.perspective.criterion.valueCd !== 'MAJORITY') {
                    const index = this.perspective.options.criterionPriority.findIndex((criteria: string) => criteria === target.name)
                    if (index !== -1) this.perspective.options.criterionPriority.splice(index, 1)
                }
                this.$emit('touched')
                this.perspective.updated = true
            }
        },
        onTargetTouched(updatePerspective: boolean) {
            this.$emit('touched')
            if (this.perspective && updatePerspective) this.perspective.updated = true
        },
        onCriterionPriortyChanged() {
            this.$emit('touched', true)
            if (this.perspective) this.perspective.updated = true
        }
    }
})
</script>

<style lang="scss">
.p-selectbutton > div {
    justify-content: center;
}
#perspective {
    border-bottom: 1px solid var(--kn-list-border-color);
    border-left: 1px solid var(--kn-list-border-color);
}
</style>

<style lang="scss" scoped>
.scorecards-target-perspective-input {
    border: none;
}
</style>
