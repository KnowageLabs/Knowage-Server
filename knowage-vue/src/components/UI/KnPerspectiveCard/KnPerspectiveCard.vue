<template>
    <Card v-if="perspective">
        <template #header>
            <h2 class="p-p-1">
                {{ perspective.name }}
            </h2>
        </template>
        <template #content>
            <div v-for="(target, index) in perspective.targets" :key="index">
                {{ target.name }}
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPerspective } from '@/modules/managers/scorecards/Scorecards'
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
        }
    }
})
</script>
