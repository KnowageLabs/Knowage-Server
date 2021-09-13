<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.registry.filters') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="filter-container" :style="registryDescriptor.styles.filterContainer">
                <div class="fields-container" :style="registryDescriptor.styles.fieldsContainer">
                    <form class="p-fluid p-formgrid p-grid">
                        <template v-for="(filter, index) in filters" :key="index">
                            <RegistryFilterCard v-if="filter.visible" :id="id" :propFilter="filter" :entity="entity" :clearTrigger="clearFiltersTrigger" @changed="setFilterValue($event, index)"></RegistryFilterCard>
                        </template>
                    </form>
                </div>

                <div class="button-container" :style="registryDescriptor.styles.buttonsContainer">
                    <Button class="p-button kn-button--primary p-mx-1" :style="registryDescriptor.styles.filtersButton" @click="clearAllFilters">{{ $t('documentExecution.registry.clearFilters') }}</Button>
                    <Button class="p-button kn-button--primary p-mx-1" :style="registryDescriptor.styles.filtersButton" @click="filterRegistry" data-test="filter-button">{{ $t('documentExecution.registry.filter') }}</Button>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import RegistryFilterCard from './RegistryFilterCard.vue'
import registryDescriptor from './RegistryDescriptor.json'

export default defineComponent({
    name: 'registry-filters-card',
    components: { Card, RegistryFilterCard },
    props: {
        propFilters: { type: Array },
        entity: { type: String },
        id: { type: String }
    },
    emits: ['filter'],
    data() {
        return {
            registryDescriptor,
            filters: [] as any[],
            clearFiltersTrigger: false
        }
    },
    watch: {
        propFilters() {
            console.log('Entity: ', this.entity)
            this.loadFilters()
        }
    },
    async created() {
        this.loadFilters()
    },
    methods: {
        loadFilters() {
            this.filters = [...(this.propFilters as any[])]

            // TODO delete this, just for showing
            this.filters.forEach((el: any) => (el.visible = true))
        },
        setFilterValue(value: string, index: number) {
            this.filters[index].filterValue = value
            // console.log('FILTER UPDATED: ', this.filters)
        },
        clearAllFilters() {
            this.filters.forEach((el: any) => (el.filterValue = ''))
            this.clearFiltersTrigger = !this.clearFiltersTrigger
            this.$emit('filter', this.filters)
        },
        filterRegistry() {
            this.$emit('filter', this.filters)
        }
    }
})
</script>
