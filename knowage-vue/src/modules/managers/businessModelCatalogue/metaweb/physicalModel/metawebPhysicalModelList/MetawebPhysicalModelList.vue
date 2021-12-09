<template>
    <Accordion v-if="meta" :multiple="true" :activeIndex="[0]">
        <AccordionTab v-for="(physicalModel, index) in meta.physicalModels" :key="index">
            <template #header>
                <span>{{ physicalModel.name }}</span>
            </template>

            <Listbox class="metaweb-physical-model-column-listbox" v-model="selectedColumn" :options="physicalModel.columns">
                <template #option="slotProps">
                    <div>
                        <i :class="slotProps.option.primaryKey ? 'fa fa-key gold-key' : 'fa fa-columns'" class="p-mr-2"></i>
                        <span>{{ slotProps.option.name }}</span>
                    </div>
                </template>
            </Listbox>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iColumn } from '../../Metaweb'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'metaweb-physical-model-list',
    components: { Accordion, AccordionTab, Listbox },
    props: { propMeta: { type: Object } },
    data() {
        return {
            meta: null as any,
            selectedColumn: null as iColumn | null
        }
    },
    watch: {
        propMeta() {
            this.loadMeta()
        }
    },
    created() {
        this.loadMeta()
    },
    methods: {
        loadMeta() {
            this.meta = this.propMeta as any
            console.log('LOADED META FOR PHYSICAL MODELS: ', this.propMeta)
        }
    }
})
</script>

<style lang="scss" scoped>
.metaweb-physical-model-column-listbox {
    border: none;
}

.gold-key {
    color: gold;
}
</style>
