<template>
    <div class="p-d-flex p-flex-row">
        <FunctionsCatalogFilterCard v-for="filter in filters" :key="filter.valueId" class="kn-flex p-m-2" :prop-filter="filter" @selected="onSelected"></FunctionsCatalogFilterCard>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunctionType } from './FunctionsCatalog'
import FunctionsCatalogFilterCard from './FunctionsCatalogFilterCard.vue'

export default defineComponent({
    name: 'functions-catalog-filter-cards',
    components: { FunctionsCatalogFilterCard },
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
        },
        onSelected(filter: iFunctionType) {
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
