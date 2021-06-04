<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.cacheManagement.runtimeInformationTitle') }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div>
                <div>
                    <p>{{ $t('managers.cacheManagement.cacheEnabled') }} {{ cache.cleaningEnabled }}</p>
                    <p>{{ $t('managers.cacheManagement.totalMemory') }} {{ cache.totalMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemory') }} {{ cache.availableMemory }}</p>
                    <p>{{ $t('managers.cacheManagement.numberOfCachedObjects') }} {{ cache.cachedObjectsCount }}</p>
                    <p>{{ $t('managers.cacheManagement.availableMemoryPercentage') }} {{ cache.availableMemoryPercentage }}</p>
                </div>
                <div>
                    <Chart type="pie" :data="chartData" />
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
        }
    },
    data() {
        return {
            runtimeInformationCardDescriptor,
            cache: {} as iCache,
            chartData: {
                labels: [this.$t('managers.cacheManagement.availableMemory'), this.$t('managers.cacheManagement.usedMemory')],
                datasets: [
                    {
                        label: 'My First dataset',
                        backgroundColor: '#42A5F5',
                        data: [100, 1 - 99]
                    }
                ]
            }
        }
    },
    watch: {
        item() {
            this.loadCache()
        }
    },
    created() {
        this.loadCache()
    },
    methods: {
        loadCache() {
            this.cache = { ...this.item } as iCache
        }
    }
})
</script>
