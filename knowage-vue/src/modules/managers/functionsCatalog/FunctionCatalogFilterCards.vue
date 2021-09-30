<template>
    <div class="p-d-flex p-flex-row">
        <FunctionCatalogFilterCard class="kn-flex p-m-2" v-for="filter in filters" :key="filter.valueId" :propFilter="filter" @selected="onSelected"></FunctionCatalogFilterCard>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunctionType } from './FunctionsCatalog'
import FunctionCatalogFilterCard from './FunctionCatalogFilterCard.vue'

export default defineComponent({
    name: 'functions-catalog-filter-cards',
    components: { FunctionCatalogFilterCard },
    props: { propFilters: { type: Array } },
    data() {
        return {
            filters: [] as iFunctionType[],
            selectedFilter: null as iFunctionType | null
        }
    },
    watch: {
        propFilters() {
            this.loadFilters()
        }
    },
    created() {
        this.loadFilters()
    },
    methods: {
        loadFilters() {
            this.filters = []
            this.propFilters?.forEach((el: any) => {
                if (el.valueCd === 'All') {
                    el.active = true
                    this.selectedFilter = el
                    this.$emit('selected', this.selectedFilter)
                }
                this.filters.push(el)
            })
            console.log('FILTERS: ', this.filters)
        },
        onSelected(filter: iFunctionType) {
            console.log('SELECTED BEFORE', this.selectedFilter)
            if (this.selectedFilter) {
                this.selectedFilter.active = false
            }
            this.selectedFilter = filter
            this.selectedFilter.active = true
            this.$emit('selected', this.selectedFilter)
        }
    }
})
</script>
