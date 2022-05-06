<template>
    <div v-if="perspective">
        <div class="p-d-flex p-flex-row p-ai-center p-m-2">
            <div class="kn-flex">
                <Button v-if="!expanded" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = true" />
                <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain scorecards-item-expand-icon" @click="expanded = false" />
                <i class="fa-solid fa-rectangle-list fa-lg p-mr-2" />
                <span>
                    <InputText class="kn-material-input scorecards-target-perspective-input" v-model="perspective.name" />
                </span>
            </div>
            <div class="kn-flex p-d-flex p-flex-row">
                <SelectButton v-model="selectedCriteria" :options="scorecardsTableDescriptor.criteriaOptions" @change="onCriteriaChange"></SelectButton>
                <MultiSelect v-if="selectedCriteria !== 'M'" class="p-ml-3 scorecards-criteria-multiselect" v-model="perspective.options.criterionPriority" :options="perspective.targets" optionLabel="name" optionValue="name"></MultiSelect>
            </div>

            <div>
                <Button icon="fa-solid fa-square-plus" class="p-button-text p-button-rounded p-button-plain" />
                <Button icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click="deletePerspectiveConfirm" />
            </div>
        </div>
        <div v-if="expanded">
            <ScorecardsTableHint v-if="perspective.targets.length === 0" class="p-m-4" :hint="'managers.scorecards.addTargetHint'"></ScorecardsTableHint>
            <template v-else>
                <ScorecardsTargetItem v-for="(target, index) in perspective.targets" :key="index" :propTarget="target" :criterias="criterias" @deleteTarget="deleteTarget"></ScorecardsTargetItem>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPerspective, iScorecardCriterion, iScorecardTarget } from '../Scorecards'
import MultiSelect from 'primevue/multiselect'
import SelectButton from 'primevue/selectbutton'
import scorecardsTableDescriptor from './ScorecardsTableDescriptor.json'
import ScorecardsTargetItem from './ScorecardsTargetItem.vue'
import ScorecardsTableHint from './ScorecardsTableHint.vue'

export default defineComponent({
    name: 'scorecards-perspective-item',
    components: { MultiSelect, SelectButton, ScorecardsTargetItem, ScorecardsTableHint },
    props: { propPerspective: { type: Object as PropType<iPerspective> }, criterias: { type: Array as PropType<iScorecardCriterion[]>, required: true } },
    emits: ['deletePerspective'],
    data() {
        return {
            scorecardsTableDescriptor,
            perspective: null as iPerspective | null,
            expanded: false,
            selectedCriteria: 'M'
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
            console.log('>>> LOADED PERSPECTIVE: ', this.perspective)
            this.setSelectedCriteria(this.perspective)
        },
        setSelectedCriteria(perspective: iPerspective) {
            if (perspective) {
                switch (perspective.criterion?.valueCd) {
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
            if (!this.perspective) return

            for (let i = 0; i < this.criterias.length; i++) {
                if ((this.selectedCriteria === 'M' && this.criterias[i].valueCd === 'MAJORITY') || (this.selectedCriteria === 'MP' && this.criterias[i].valueCd === 'MAJORITY_WITH_PRIORITY') || (this.selectedCriteria === 'P' && this.criterias[i].valueCd === 'PRIORITY')) {
                    this.perspective.criterion = this.criterias[i]
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
            if (!this.perspective) return
            const index = this.perspective.targets.findIndex((tempTarget: iScorecardTarget) => tempTarget.id === target.id)
            if (index !== -1) {
                this.perspective.targets.splice(index, 1)
                if (this.perspective.criterion.valueCd !== 'MAJORITY') {
                    const index = this.perspective.options.criterionPriority.findIndex((criteria: string) => criteria === target.name)
                    if (index !== -1) this.perspective.options.criterionPriority.splice(index, 1)
                }
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.scorecards-item-expand-icon {
    color: white;
}

.scorecards-criteria-multiselect {
    width: 50%;
}

.scorecards-target-perspective-input {
    border: none;
}
</style>
