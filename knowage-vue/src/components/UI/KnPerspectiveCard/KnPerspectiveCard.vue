<template>
    <Card class="perspective-card" v-if="perspective">
        <template #header>
            <div class="perspective-header p-d-flex p-flex-row p-ai-center" :class="toolbarBorderClass">
                <h2 class="p-m-0 p-p-2">{{ perspective.name }}</h2>
                <span v-tooltip="getSelectedCriteriaTooltip(perspective.criterion?.valueCd)" class="perspective-target-icon kn-cursor-pointer">{{ getTargetIconLetter(perspective.criterion?.valueCd) }}</span>
                <div v-if="perspective.criterion?.valueCd !== 'MAJORITY'" class="p-ml-2 kn-truncated priority-items-container">{{ '(' + getPriorityItems(perspective) + ')' }}</div>
            </div>
        </template>
        <template #content>
            <div class="target-row p-d-flex p-flex-row p-ai-center p-p-3 p-my-2" :class="{ 'perspective-target-container': index !== perspective.targets.length - 1 }" v-for="(target, index) in perspective.targets" :key="index">
                <div class="p-d-flex p-flex-row">
                    <span class="p-mr-2 kn-flex">{{ target.name }}</span>
                    <span v-tooltip="getSelectedCriteriaTooltip(target.criterion?.valueCd)" class="p-ml-auto perspective-target-icon kn-cursor-pointer">{{ getTargetIconLetter(target.criterion?.valueCd) }}</span>
                    <div v-if="target.criterion?.valueCd !== 'MAJORITY'" class="p-ml-2 kn-truncated priority-items-container">{{ '(' + getPriorityItems(target) + ')' }}</div>
                </div>
                <div class="p-ml-auto">
                    <i class="fas fa-square fa-2xl p-mr-2" :class="getTargetStatusIconColor(target)"></i>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPerspective, iScorecardTarget } from '@/modules/managers/scorecards/Scorecards'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import mainStore from '../../../App.store'
import deepEqual from 'deep-equal'

export default defineComponent({
    name: 'kn-perspective-card',
    components: { Card },
    props: { propPerspective: { type: Object as PropType<iPerspective> } },
    data() {
        return {
            perspective: null as iPerspective | null,
            toolbarBorderClass: 'perspective-toolbar-light-grey'
        }
    },
    computed: {
        perspectiveUpdated(): boolean | undefined {
            return this.perspective?.updated
        }
    },
    watch: {
        propPerspective() {
            this.loadPerspective()
        },
        async perspectiveUpdated(value: boolean) {
            if (value && this.perspective) {
                await this.evaluatePerspective()
                this.evaluatePerspectiveTargets()
                this.perspective.updated = false
            }
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadPerspective()
    },
    methods: {
        async loadPerspective() {
            this.perspective = this.propPerspective ? this.propPerspective : ({} as iPerspective)

            if (this.perspective && this.perspective.criterion.valueId) {
                await this.evaluatePerspective()
                this.evaluatePerspectiveTargets()
            }
        },
        getTargetIconLetter(criterionValue: string | null) {
            if (!criterionValue) criterionValue = ''

            switch (criterionValue) {
                case 'MAJORITY':
                    return 'M'
                case 'MAJORITY_WITH_PRIORITY':
                    return 'MP'
                case 'PRIORITY':
                    return 'P'
                default:
                    return ''
            }
        },
        async evaluateCriteria(criterionId: number, statusArray: any[], target: iScorecardTarget | null) {
            this.store.setLoading(true)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpiee/${criterionId}/evaluateCriterion`, statusArray)
                .then((response: AxiosResponse<any>) => {
                    if (!target && this.perspective) {
                        this.perspective.statusColor = response.data ? response.data.status : null
                        this.setPerspectiveToolbarClass()
                    } else if (target) {
                        target.statusColor = response.data ? response.data.status : null
                    }
                })
                .catch(() => {})
            this.store.setLoading(false)
        },
        async evaluatePerspective() {
            if (!this.perspective) return

            for (let i = 0; i < this.perspective.targets.length; i++) {
                this.addGroupedTargets(this.perspective.targets[i].status)
                this.addGroupedKpiItems(this.perspective.targets[i])
            }

            const statusArray = [] as any[]
            for (let i = 0; i < this.perspective.targets.length; i++) {
                statusArray.push({ status: this.perspective.targets[i].status, priority: false })
            }

            for (let i = 0; i < this.perspective.options.criterionPriority.length; i++) {
                for (let j = 0; j < this.perspective.targets.length; j++) {
                    if (this.perspective.options.criterionPriority[i] === this.perspective.targets[j].name) {
                        statusArray[j].priority = true
                    }
                }
            }

            await this.evaluateCriteria(this.perspective.criterion.valueId, statusArray, null)
        },
        evaluatePerspectiveTargets() {
            if (!this.perspective) return
            this.perspective.targets?.forEach((target: iScorecardTarget) => {
                if (target.updated || target.updated === undefined) {
                    this.evaluateTarget(target)
                    target.updated = false
                }
            })
        },
        async evaluateTarget(target: iScorecardTarget) {
            const statusArray = [] as any[]
            for (let i = 0; i < target.kpis.length; i++) {
                statusArray.push({ status: target.kpis[i].status, priority: false })
            }

            for (let i = 0; i < target.options.criterionPriority.length; i++) {
                for (let j = 0; j < target.kpis.length; j++) {
                    if (target.options.criterionPriority[i] === target.kpis[j].name) {
                        statusArray[j].priority = true
                    }
                }
            }

            await this.evaluateCriteria(target.criterion.valueId, statusArray, target)
        },
        addGroupedTargets(type: any) {
            if (!this.perspective || !this.perspective.groupedTargets) return

            for (let i = 0; i < this.perspective.groupedTargets?.length; i++) {
                if (deepEqual(this.perspective.groupedTargets[i].status, type)) {
                    this.perspective.groupedTargets[i].count++
                    break
                }
            }
            this.perspective?.groupedTargets?.push({ status: type, count: 1 })
        },
        addGroupedKpiItems(target: iScorecardTarget) {
            if (!this.perspective || !target.groupedKpis) return

            if (!this.perspective.groupedKpis) {
                this.perspective.groupedKpis = []
            }

            for (let i = 0; i < target.groupedKpis.length; i++) {
                const tempGroupedKpis = target.groupedKpis[i]
                let found = false

                for (let j = 0; j < this.perspective?.groupedKpis?.length; j++) {
                    if (deepEqual(this.perspective.groupedKpis[j].status, tempGroupedKpis.status)) {
                        this.perspective.groupedKpis[j].count += tempGroupedKpis.count
                        found = true
                        break
                    }
                }
                if (!found) {
                    this.perspective.groupedKpis?.push({ status: tempGroupedKpis.status, count: tempGroupedKpis.count })
                }
            }
        },
        setPerspectiveToolbarClass() {
            if (this.perspective?.statusColor) {
                switch (this.perspective.statusColor) {
                    case 'RED':
                        this.toolbarBorderClass = 'perspective-toolbar-red'
                        break
                    case 'YELLOW':
                        this.toolbarBorderClass = 'perspective-toolbar-yellow'
                        break
                    case 'GREEN':
                        this.toolbarBorderClass = 'perspective-toolbar-green'
                        break
                    case 'GREY':
                        this.toolbarBorderClass = 'perspective-toolbar-grey'
                }
            } else {
                this.toolbarBorderClass = 'perspective-toolbar-light-grey'
            }
        },
        getTargetStatusIconColor(target: iScorecardTarget) {
            if (target?.statusColor) {
                switch (target.statusColor) {
                    case 'RED':
                        return 'scorecard-icon-red'
                    case 'YELLOW':
                        return 'scorecard-icon-yellow'
                    case 'GREEN':
                        return 'scorecard-icon-green'
                    case 'GREY':
                        return 'scorecard-icon-grey'
                }
            } else {
                return 'scorecard-icon-light-grey'
            }
        },
        getSelectedCriteriaTooltip(criterionValue: string | null) {
            switch (criterionValue) {
                case 'MAJORITY':
                    return this.$t('managers.scorecards.majority')
                case 'MAJORITY_WITH_PRIORITY':
                    return this.$t('managers.scorecards.majorityWithPriority')
                case 'PRIORITY':
                    return this.$t('managers.scorecards.priority')
                default:
                    return ''
            }
        },
        getPriorityItems(item: iPerspective | iScorecardTarget) {
            let targets = ''
            if (item && item.options) {
                for (let i = 0; i < item.options.criterionPriority.length; i++) {
                    targets += item.options.criterionPriority[i]
                    targets += i === item.options.criterionPriority.length - 1 ? ' ' : ', '
                }
            }

            return targets
        }
    }
})
</script>

<style lang="scss">
.perspective-header {
    border-bottom: 1px solid #c2c2c2;
}

.perspective-card .p-card-body,
.perspective-card .p-card-body .p-card-content {
    padding: 0;
}

.target-row {
    min-height: 25px;
}

.perspective-target-icon {
    border-radius: 3px;
    padding: 4px 8px;
    min-height: 25px;
    text-align: center;
    background-color: #c2c2c2;
}

.perspective-target-container {
    border-bottom: 1px solid #c2c2c2;
}

.perspective-toolbar-red {
    border-top: 8px solid #ff5656;
}

.perspective-toolbar-yellow {
    border-top: 8px solid #ffee58;
}

.perspective-toolbar-green {
    border-top: 8px solid #50c550;
}

.perspective-toolbar-grey {
    border-top: 8px solid #b7b7b7;
}

.perspective-toolbar-light-grey {
    border-top: 8px solid #cccccc;
}

.scorecard-icon-red {
    color: #ff5656;
}

.scorecard-icon-yellow {
    color: #ffee58;
}

.scorecard-icon-green {
    color: #50c550;
}

.scorecard-icon-grey {
    color: #b7b7b7;
}

.scorecard-icon-light-grey {
    color: #cccccc;
}

.priority-items-container {
    width: 100%;
    max-width: 250px;
}
</style>
