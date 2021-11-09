<template>
    <Card class="p-m-2">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.cacheManagement.runtimeInformationTitle') }}
                </template>
                <template #right>
                    <Button icon="pi pi-refresh" class="p-button-text p-button-rounded p-button-plain" @click="refresh" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-column">
                <div class="kn-flex">
                    dfssdfdfssdfsdfsddfsfd
                    <p>{{ $t('managers.cacheManagement.cacheEnabled') }}: {{ cache.cleaningEnabled }}</p>
                    <p>{{ $t('managers.cacheManagement.totalMemory') }}: {{ totalMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemory') }}: {{ availableMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.numberOfCachedObjects') }}: {{ cache.cachedObjectsCount }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemoryPercentage') }}: {{ cache.availableMemoryPercentage }}%</p>
                </div>
                <div class="kn-flex">
                    <Chart type="pie" :data="cacheData" data-test="chart" />
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCache } from '../../CacheManagement'
import Card from 'primevue/card'
import Chart from 'primevue/chart'

export default defineComponent({
    name: 'runtime-information-card',
    components: {
        Card,
        Chart
    },
    props: {
        item: {
            type: Object,
            required: true
        },
        chartData: {
            type: Object,
            required: true
        }
    },
    emits: ['refresh'],
    data() {
        return {
            cache: {} as iCache,
            cacheData: this.loadChart()
        }
    },
    watch: {
        item() {
            this.loadCache()
        },
        chartData() {
            this.loadChart()
        }
    },
    computed: {
        totalMemory(): string {
            return (this.cache.totalMemory / 1048576).toFixed(2) + ' MB'
        },
        availableMemory(): string {
            return (this.cache.availableMemory / 1048576).toFixed(2) + ' MB'
        }
    },
    created() {
        this.loadCache()
    },
    methods: {
        loadCache() {
            this.cache = { ...this.item } as iCache
        },
        loadChart() {
            this.cacheData = {
                labels: [this.$t('managers.cacheManagement.availableMemory'), this.$t('managers.cacheManagement.usedMemory')],
                datasets: [
                    {
                        backgroundColor: ['#43749e', '#bbd6ed'],
                        data: this.chartData
                    }
                ]
            }
        },
        refresh() {
            this.$emit('refresh')
        }
    }
})
</script>
