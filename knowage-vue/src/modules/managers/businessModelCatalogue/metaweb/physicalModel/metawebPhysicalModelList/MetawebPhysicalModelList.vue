<template>
    <Accordion v-if="meta" :multiple="true" @tabOpen="setSelected($event.index)" @tabClose="setSelected($event.index)">
        <AccordionTab v-for="(physicalModel, index) in meta.physicalModels" :key="index">
            <template #header>
                <div class="p-d-flex p-flex-row metaweb-physical-model-accordion-header">
                    <span :data-test="'physical-model-tab-' + physicalModel.name ">{{ physicalModel.name }}</span>
                    <span class="p-ml-auto p-mr-2">{{ physicalModel.columns.length + ' ' + $t('common.properties') }}</span>
                </div>
            </template>

            <Listbox class="metaweb-physical-model-column-listbox" v-model="selectedPhysicalModel" :options="physicalModel.columns" @change="emitSelectedItem">
                <template #option="slotProps">
                    <div>
                        <i :class="slotProps.option.primaryKey ? 'fa fa-key gold-key' : 'fa fa-columns'" class="p-mr-2"></i>
                        <span >{{ slotProps.option.name }}</span>
                    </div>
                </template>
            </Listbox>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iColumn, iPhysicalModel } from '../../Metaweb'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'metaweb-physical-model-list',
    components: { Accordion, AccordionTab, Listbox },
    props: { propMeta: { type: Object } },
    emits: ['selected'],
    data() {
        return {
            meta: null as any,
            selectedPhysicalModel: null as iColumn | iPhysicalModel | null
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
        },
        emitSelectedItem() {
            this.$emit('selected', this.selectedPhysicalModel)
        },
        setSelected(index: number) {
            this.selectedPhysicalModel = this.meta.physicalModels[index]
            this.$emit('selected', this.selectedPhysicalModel)
        }
    }
})
</script>

<style lang="scss">
.metaweb-physical-model-column-listbox {
    border: none;
}

.metaweb-physical-model-column-listbox .p-listbox-item {
    padding: 0.2rem !important;
}

.metaweb-physical-model-accordion-header {
    width: 100%;
}

.gold-key {
    color: gold;
}
</style>
