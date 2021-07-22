<template>
    <div>
        <Card v-for="(kpiName, index) in kpiNames" :key="index" class="p-mt-2">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ kpiName }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <div class="p-d-flex p-flex-row p-flex-wrap">
                    <KpiSchedulerFilterDetailCard class="p-m-2" v-for="filter in formatedFilters[kpiName]" :key="filter.id" :filter="filter" :placeholderType="placeholderType" :temporalType="temporalType" :lovs="lovs"></KpiSchedulerFilterDetailCard>
                </div>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import KpiSchedulerFilterDetailCard from './KpiSchedulerFilterDetailCard.vue'

export default defineComponent({
    name: 'filters-card',
    components: { Card, KpiSchedulerFilterDetailCard },
    props: { formatedFilters: { type: Object, required: true }, placeholderType: { type: Array }, temporalType: { type: Array }, lovs: { type: Array, required: true } },
    data() {
        return {
            filters: [] as any[],
            kpiNames: [] as any[],
            filteredLovs: [] as any[]
        }
    },
    watch: {
        formatedFilters() {
            this.loadFilters()
        }
    },
    created() {
        this.loadFilters()
    },
    methods: {
        loadFilters() {
            this.filters = this.formatedFilters as any[]
            if (this.filters) {
                this.kpiNames = Object.keys(this.filters)
            }
            console.log('FC - Loaded filters', this.filters)
        }
    }
})
</script>
