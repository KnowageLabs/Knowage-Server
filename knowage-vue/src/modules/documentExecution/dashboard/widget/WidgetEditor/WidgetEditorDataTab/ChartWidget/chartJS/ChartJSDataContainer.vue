<template>
    <div v-if="widgetModel">
        <ChartWidgetChartTypeDropdown :widget-model="widgetModel"></ChartWidgetChartTypeDropdown>
        <ChartJSPieChartDataContainer :widget-model="widgetModel" :selected-dataset="selectedDataset"></ChartJSPieChartDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import ChartJSPieChartDataContainer from './ChartJSPieChartDataContainer.vue'
import ChartWidgetChartTypeDropdown from '../common/ChartWidgetChartTypeDropdown.vue'

export default defineComponent({
    name: 'chart-j-s-widget-data-container',
    components: { ChartWidgetChartTypeDropdown, ChartJSPieChartDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
