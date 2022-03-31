<template>
    <Accordion class="p-m-3">
        <AccordionTab :header="$t('common.filters')">
            <div class="p-grid p-fluid p-formgrid">
                <div class="p-col-12">
                    <Checkbox class="p-mr-2" v-model="showMissingElements" :binary="true" :disabled="!selectedHierarchy"></Checkbox>
                    <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.showMissingElements') }}</label>
                </div>

                <div class="p-col-6 p-mt-4" v-for="(filter, index) in filters" :key="index">
                    <span class="p-float-label">
                        <Calendar v-if="filter.TYPE === 'Date'" class="calendar-management-detail-form-calendar-input " v-model="filter.VALUE" :manualInput="true"></Calendar>
                        <InputText v-else class="kn-material-input" :type="filter.TYPE === 'number' ? 'number' : 'text'" v-model.trim="filter.VALUE" />
                        <label class="kn-material-input-label"> {{ filter.NAME }}</label>
                    </span>
                </div>

                <div class="p-col-12 p-d-flex p-flex-row p-jc-end">
                    <Button icon="pi pi-check" class="p-button-link" @click="applyFilters" />
                    <Button icon="pi pi-trash" class="p-button-link" @click="resetFilters" />
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionFilter, iHierarchy } from '../../../HierarchyManagement'
import moment from 'moment'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'hierarchy-management-dimensions-filter-card',
    components: { Accordion, AccordionTab, Calendar, Checkbox },
    props: { dimensionFilters: { type: Array as PropType<iDimensionFilter[]> }, selectedHierarchy: { type: Object as PropType<iHierarchy | null> } },
    emits: ['applyFilters'],
    data() {
        return {
            filters: [] as iDimensionFilter[],
            showMissingElements: false
        }
    },
    watch: {
        dimensionFilters() {
            this.loadFilters()
        }
    },
    async created() {
        this.loadFilters()
    },
    methods: {
        loadFilters() {
            this.filters = this.dimensionFilters as iDimensionFilter[]
        },
        applyFilters() {
            let tempFilters = deepcopy(this.filters)
            tempFilters = tempFilters.filter((filter: iDimensionFilter) => filter.VALUE && filter.VALUE !== '')
            tempFilters.forEach((filter: iDimensionFilter) => {
                if (filter.TYPE === 'Date') {
                    filter.VALUE = moment(filter.VALUE).format('YYYY-MM-DD')
                }
            })
            this.$emit('applyFilters', { filters: tempFilters, showMissingElements: this.showMissingElements })
        },
        resetFilters() {
            this.showMissingElements = false
            this.filters?.forEach((filter: iDimensionFilter) => (filter.VALUE = ''))
            this.$emit('applyFilters', { filters: [], showMissingElements: this.showMissingElements })
        }
    }
})
</script>
