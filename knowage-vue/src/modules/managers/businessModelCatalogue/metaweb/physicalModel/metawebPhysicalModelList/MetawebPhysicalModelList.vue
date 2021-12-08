<template>
    <Accordion :multiple="true" :activeIndex="[0]">
        <AccordionTab v-for="(physicalModel, index) in physicalModels" :key="index">
            <template #header>
                <span>{{ physicalModel.name }}</span>
            </template>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iPhysicalModel } from '../../Metaweb'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'

export default defineComponent({
    name: 'metaweb-physical-model-list',
    components: { Accordion, AccordionTab },
    props: { propPhysicalModels: { type: Array as PropType<iPhysicalModel[]> } },
    data() {
        return {
            physicalModels: [] as iPhysicalModel[]
        }
    },
    watch: {
        propPhysicalModels() {
            this.loadPhysicalModels()
        }
    },
    created() {
        this.loadPhysicalModels()
    },
    methods: {
        loadPhysicalModels() {
            this.physicalModels = this.propPhysicalModels as iPhysicalModel[]
            console.log('LOADED PHYSICAL MODELS: ', this.physicalModels)
        }
    }
})
</script>
