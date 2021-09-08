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
                <RegistryFilterCard v-for="(filter, index) in filters" :key="index" :propFilter="filter" class="p-m-2"></RegistryFilterCard>
                <div class="p-ai-end">
                    <Button class="kn-button p-button-text">{{ $t('documentExecution.registry.clearFilters') }}</Button>
                    <Button class="kn-button p-button-text">{{ $t('documentExecution.registry.filter') }}</Button>
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
    props: { propFilters: { type: Array } },
    data() {
        return {
            filters: [] as any[],
            filterOptions: {} as any
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
        }
    }
})
</script>
