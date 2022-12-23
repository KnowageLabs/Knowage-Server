<template>
    <div v-if="widgetModel">
        <ChartWidgetChartTypeDropdown :widgetModel="widgetModel"></ChartWidgetChartTypeDropdown>
        <HighchartsCommonDataContainer :widgetModel="widgetModel" :selectedDataset="selectedDataset"></HighchartsCommonDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import HighchartsCommonDataContainer from './HighchartsCommonDataContainer.vue'
import ChartWidgetChartTypeDropdown from '../common/ChartWidgetChartTypeDropdown.vue'

export default defineComponent({
    name: 'highcharts-widget-data-container',
    components: { ChartWidgetChartTypeDropdown, HighchartsCommonDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
