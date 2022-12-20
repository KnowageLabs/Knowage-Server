<template>
    <div v-if="widgetModel">
        <ChartWidgetChartTypeDropdown :widgetModel="widgetModel"></ChartWidgetChartTypeDropdown>
        <IHighchartsPieChartDataContainer :widgetModel="widgetModel" :selectedDataset="selectedDataset"></IHighchartsPieChartDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import IHighchartsPieChartDataContainer from './IHighchartsPieChartDataContainer.vue'
import ChartWidgetChartTypeDropdown from '../common/ChartWidgetChartTypeDropdown.vue'

export default defineComponent({
    name: 'highcharts-widget-data-container',
    components: { ChartWidgetChartTypeDropdown, IHighchartsPieChartDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
