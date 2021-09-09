<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('documentExecution.registry.filters') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row p-ai-center">
                <RegistryFilterCard class="p-m-2" v-for="(filter, index) in filters" :key="index" :propFilter="filter" :entity="entity" :clearTrigger="clearFiltersTrigger" @changed="setFilterValue($event, index)"></RegistryFilterCard>
                <div class="p-ai-end">
                    <Button class="kn-button p-button-text" @click="clearAllFilters">{{ $t('documentExecution.registry.clearFilters') }}</Button>
                    <Button class="kn-button p-button-text" @click="filterRegistry">{{ $t('documentExecution.registry.filter') }}</Button>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import RegistryFilterCard from './RegistryFilterCard.vue'

export default defineComponent({
    name: 'registry-filters-card',
    components: { Card, RegistryFilterCard },
    props: { propFilters: { type: Array }, entity: { type: String } },
    emits: ['filter'],
    data() {
        return {
            filters: [] as any[],
            clearFiltersTrigger: false
        }
    },
    watch: {
        propFilters() {
            this.loadFilters()
        }
    },
    async created() {
        this.loadFilters()
    },
    methods: {
        loadFilters() {
            this.filters = [...(this.propFilters as any[])]
        },
        setFilterValue(value: string, index: number) {
            this.filters[index].filterValue = value
            // console.log('FILTER UPDATED: ', this.filters)
        },
        clearAllFilters() {
            this.filters.forEach((el: any) => (el.filterValue = ''))
            this.clearFiltersTrigger = !this.clearFiltersTrigger
        },
        filterRegistry() {
            this.$emit('filter', this.filters)
        }
    }
})
</script>
