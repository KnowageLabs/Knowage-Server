<template>
    <Card>
        <template #title>
            {{ $t('common.filters') }}
        </template>
        <template #content>
            <div class="p-grid p-fluid p-formgrid">
                <div class="p-col-12">
                    <Checkbox v-model="showMissingElements" :binary="true"></Checkbox>
                    <label class="kn-material-input-label"> {{ $t('managers.hierarchyManagement.showMissingElements') }}</label>
                </div>

                <div class="p-col-6" v-for="(filter, index) in filters" :key="index">
                    <span class="p-float-label">
                        <Calendar v-if="filter.TYPE === 'Date'" class="calendar-management-detail-form-calendar-input " v-model="filter.value" :manualInput="true" data-test="calendar-start-date-input"></Calendar>
                        <InputText v-else class="kn-material-input" :type="filter.TYPE === 'number' ? 'number' : 'text'" v-model.trim="filter.value" />
                        <label class="kn-material-input-label"> {{ filter.NAME + ' *' }}</label>
                    </span>
                </div>

                <div class="p-col-12 p-d-flex p-flex-row p-jc-end">
                    <Button icon="pi pi-check" class="p-button-link" @click="applyFilters" />
                    <Button icon="pi pi-trash" class="p-button-link" @click="resetFilters" />
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDimensionFilter } from '../../../HierarchyManagement'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    name: 'hierarchy-management-dimensions-filter-card',
    components: { Card, Calendar, Checkbox },
    props: { dimensionFilters: { type: Array as PropType<iDimensionFilter[]> } },
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
            console.log('LOADED FILTERS: ', this.filters)
        },
        applyFilters() {},
        resetFilters() {
            this.filters?.forEach((filter: iDimensionFilter) => (filter.value = ''))
        }
    }
})
</script>
