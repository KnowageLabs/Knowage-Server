<template>
    <Accordion class="p-m-3">
        <AccordionTab :header="$t('common.filters')">
            <div class="p-grid p-fluid p-formgrid">
                <div class="p-col-12">
                    <Checkbox class="p-mr-2" v-model="showMissingElements" :binary="true" :disabled="!selectedHierarchy"></Checkbox>
                    <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.showMissingElements') }}</label>
                </div>

                <div class="p-col-6 p-mt-4">
                    <span class="p-float-label">
                        <Calendar v-model="afterDate" :manualInput="true" :disabled="!selectedHierarchy"></Calendar>
                        <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.afterDate') }}</label>
                    </span>
                </div>
                <div class="p-col-6"></div>

                <div class="p-col-12 p-d-flex p-flex-row p-jc-end">
                    <Button icon="pi pi-check" class="p-button-link" :disabled="!selectedHierarchy" @click="applyFilters" />
                    <Button icon="pi pi-trash" class="p-button-link" :disabled="!selectedHierarchy" @click="resetFilters" />
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iHierarchy } from '../../../HierarchyManagement'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-filter-card',
    components: { Accordion, AccordionTab, Calendar, Checkbox },
    props: { selectedHierarchy: { type: Object as PropType<iHierarchy | null> } },
    emits: ['applyFilters'],
    data() {
        return {
            showMissingElements: false,
            afterDate: null as Date | null
        }
    },
    created() {},
    methods: {
        applyFilters() {
            this.$emit('applyFilters', { showMissingElements: this.showMissingElements, afterDate: this.afterDate })
        },
        resetFilters() {
            this.showMissingElements = false
            this.afterDate = null
            this.$emit('applyFilters', null)
        }
    }
})
</script>
