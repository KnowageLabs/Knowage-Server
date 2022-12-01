<template>
    <div v-if="widgetModel">
        <HighchartsPieChartDataContainer :widgetModel="widgetModel" :selectedDataset="selectedDataset"></HighchartsPieChartDataContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from '../../TableWidget/TableWidgetDataDescriptor.json'
import HighchartsPieChartDataContainer from './HighchartsPieChartDataContainer.vue'

export default defineComponent({
    name: 'highcharts-widget-data-container',
    components: { HighchartsPieChartDataContainer },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor
        }
    },
    async created() {},
    methods: {}
})
</script>
