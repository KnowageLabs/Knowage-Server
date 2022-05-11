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
import Card from 'primevue/card'

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
        loadPerspective() {
            this.perspective = this.propPerspective as iPerspective
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
