<template>
    <div v-if="widgetModel">
        <ChartJSPieChartDataContainer :widgetModel="widgetModel" :selectedDataset="selectedDataset"></ChartJSPieChartDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import ChartJSPieChartDataContainer from './ChartJSPieChartDataContainer.vue'

// TODO - add chart type

export default defineComponent({
    name: 'chartJS-widget-data-container',
    components: { ChartJSPieChartDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
