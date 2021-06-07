<template>
    <Card class="p-m-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.cacheManagement.runtimeInformationTitle') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row">
                <div class="information-container">
                    <p>{{ $t('managers.cacheManagement.cacheEnabled') }} {{ cache.cleaningEnabled }}</p>
                    <p>{{ $t('managers.cacheManagement.totalMemory') }} {{ cache.totalMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemory') }} {{ cache.availableMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.numberOfCachedObjects') }} {{ cache.cachedObjectsCount }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemoryPercentage') }} {{ cache.availableMemoryPercentage }}</p>
                </div>
                <div class="information-container">
                    <Chart type="pie" :data="cacheData" />
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
import runtimeInformationCardDescriptor from './RuntimeInformationCardDescriptor.json'

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
    data() {
        return {
            runtimeInformationCardDescriptor,
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
            console.log('CHART DATA:', this.chartData)
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
                        data: this.chartData as any
                    }
                ]
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.information-container {
    flex: 1;
}
</style>
