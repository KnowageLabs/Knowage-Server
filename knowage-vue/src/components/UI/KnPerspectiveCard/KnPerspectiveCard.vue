<template>
    <Card v-if="perspective">
        <template #header>
            <h2 class="p-p-1">
                {{ perspective.name }}
            </h2>
        </template>
        <template #content>
            <div :class="{ 'perspective-target-container': index !== perspective.targets.length - 1 }" v-for="(target, index) in perspective.targets" :key="index">
                <span class="p-mr-2">{{ target.name }}</span>
                <span class="perspective-target-icon">{{ getTargetIconLetter(target) }}</span>
                <i class="fas fa-square fa-2xl p-mr-2"></i>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPerspective, iScorecardTarget } from '@/modules/managers/scorecards/Scorecards'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'

const deepEqual = require('deep-equal')

export default defineComponent({
    name: 'kn-perspective-card',
    components: { Card },
    props: { propPerspective: { type: Object as PropType<iPerspective> } },
    data() {
        return {
            perspective: null as iPerspective | null
        }
    },
    watch: {
        propPerspective() {
            this.loadPerspective()
        }
    },
    created() {
        this.loadPerspective()
    },
    methods: {
        async loadPerspective() {
            this.perspective = this.propPerspective as iPerspective
            if (this.perspective && this.perspective.criterion.valueId) {
                await this.evaluatePerspective()
            }

            console.log('>>> LOADED PERSPECTIVE IN CARD: ', this.perspective)
        },
        getTargetIconLetter(target: iScorecardTarget) {
            console.log(' >>> TARGET: ', target)
            if (target) {
                switch (target.criterion?.valueCd) {
                    case 'MAJORITY':
                        return 'M'

                    case 'MAJORITY_WITH_PRIORITY':
                        return 'MP'

                    case 'PRIORITY':
                        return 'P'
                    default:
                        return ''
                }
            }
        },
        async evaluateCriteria(criterionId: number, statusArray: any[]) {
            this.$store.commit('setLoading', true)
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpiee/${criterionId}/evaluateCriterion`, statusArray)
                .then((response: AxiosResponse<any>) => {
                    console.log('RESPONSE DATA: ', response.data)
                })
                .catch(() => {})
            this.$store.commit('setLoading', false)
        },
        async evaluatePerspective() {
            if (!this.perspective) return

            for (let i = 0; i < this.perspective.targets.length; i++) {
                this.addGroupedKpiItems(this.perspective.targets[i])
            }

            const statusArray = [] as any[]
            for (let i = 0; i < this.perspective.targets.length; i++) {
                statusArray.push({ status: this.perspective.targets[i].status, priority: false })
            }

            for (let i = 0; i < this.perspective.options.criterionPriority.length; i++) {
                for (let j = 0; j < this.perspective.targets.length; j++) {
                    if (this.perspective.options.criterionPriority[i].id === this.perspective.targets[j].id) {
                        statusArray[i].priority = true
                    }
                }
            }

            await this.evaluateCriteria(this.perspective.criterion.valueId, statusArray)
        },
        addGroupedKpiItems(target: iScorecardTarget) {
            console.log(' >>> TARGET: ', target)
            if (!this.perspective || !target.groupedKpis) return

            if (!this.perspective.groupedKpis) {
                this.perspective.groupedKpis = []
            }

            for (let i = 0; i < target.groupedKpis.length; i++) {
                const tempGroupedKpis = target.groupedKpis[i]
                let found = false

                for (let j = 0; j < this.perspective?.groupedKpis?.length; i++) {
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
        }
    }
})
</script>

<style lang="scss">
.perspective-target-icon {
    border-radius: 3px;
    padding: 0 5px;
    text-align: center;
    background-color: #c2c2c2;
}

.perspective-target-container {
    padding: 1rem;
    border-bottom: 1px solid #c2c2c2;
}
</style>
