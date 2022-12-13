<template>
    <div v-if="widgetModel">
        <ChartWidgetChartTypeDropdown :widgetModel="widgetModel"></ChartWidgetChartTypeDropdown>
        <ChartJSPieChartDataContainer :widgetModel="widgetModel" :selectedDataset="selectedDataset"></ChartJSPieChartDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import ChartJSPieChartDataContainer from './ChartJSPieChartDataContainer.vue'
import ChartWidgetChartTypeDropdown from '../common/ChartWidgetChartTypeDropdown.vue'

// TODO - add chart type

export default defineComponent({
    name: 'chartJS-widget-data-container',
    components: { ChartWidgetChartTypeDropdown, ChartJSPieChartDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
