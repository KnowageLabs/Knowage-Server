<template>
    <Toolbar class="kn-toolbar kn-toolbar--default">
        <template #start>
            <Button v-if="!expandFilterCard" icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" @click="expandFilterCard = true" />
            <Button v-else icon="fas fa-chevron-up" class="p-button-text p-button-rounded p-button-plain" @click="expandFilterCard = false" />
            <span>{{ $t('common.filters') }}</span>
        </template>
        <template #end>
            <Button v-if="expandFilterCard" icon="pi pi-check" class="p-button-text p-button-rounded p-button-plain" @click="applyFilters" />
            <Button v-if="expandFilterCard" icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="resetFilters" />
        </template>
    </Toolbar>
    <Card v-show="expandFilterCard">
        <template #content>
            <div class="p-grid p-fluid p-formgrid">
                <div class="p-field-checkbox p-col-12">
                    <Checkbox v-model="showMissingElements" class="p-mr-2" :binary="true" :disabled="!selectedHierarchy" />
                    <label class="kn-material-input-label p-as-center"> {{ $t('managers.hierarchyManagement.showMissingElements') }}</label>
                </div>

                <div v-for="(filter, index) in filters" :key="index" class="p-field p-col-12 p-lg-6">
                    <span class="p-float-label">
                        <Calendar v-if="filter.TYPE === 'Date'" v-model="filter.VALUE" class="kn-material-input" :manual-input="true" :show-icon="true" />
                        <InputText v-else v-model.trim="filter.VALUE" class="kn-material-input" :type="filter.TYPE === 'number' ? 'number' : 'text'" />
                        <label class="kn-material-input-label"> {{ filter.NAME }}</label>
                    </span>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionFilter, iHierarchy } from '../../../HierarchyManagement'
import moment from 'moment'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Card from 'primevue/card'

import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'hierarchy-management-dimensions-filter-card',
    components: { Calendar, Checkbox, Card },
    props: { dimensionFilters: { type: Array as PropType<iDimensionFilter[]> }, selectedHierarchy: { type: Object as PropType<iHierarchy | null> } },
    emits: ['applyFilters'],
    data() {
        return {
            filters: [] as iDimensionFilter[],
            showMissingElements: false,
            expandFilterCard: false
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
