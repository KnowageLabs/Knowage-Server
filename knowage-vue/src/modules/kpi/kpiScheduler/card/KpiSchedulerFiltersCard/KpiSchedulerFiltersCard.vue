<template>
    <div>
        <Card v-for="(kpiName, index) in kpiNames" :key="index" class="p-mt-2">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ kpiName }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <div class="p-d-flex p-flex-row p-flex-wrap">
                    <KpiSchedulerFilterDetailCard class="p-m-2" v-for="filter in formatedFilters[kpiName]" :key="filter.id" :filter="filter" :placeholderType="placeholderType" :temporalType="temporalType" :lovs="lovs" @touched="$emit('touched')"></KpiSchedulerFilterDetailCard>
                </div>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFilter, iLov } from '../../KpiScheduler'
import Card from 'primevue/card'
import KpiSchedulerFilterDetailCard from './KpiSchedulerFilterDetailCard.vue'

export default defineComponent({
    name: 'kpi-scheduler-filters-card',
    components: { Card, KpiSchedulerFilterDetailCard },
    props: { formatedFilters: { type: Object, required: true }, placeholderType: { type: Array }, temporalType: { type: Array }, lovs: { type: Array, required: true } },
    emits: ['touched'],
    data() {
        return {
            filters: [] as iFilter[],
            kpiNames: [] as string[],
            filteredLovs: [] as iLov[]
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
        }
    }
})
</script>
