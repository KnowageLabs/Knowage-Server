<template>
    <div v-if="heatmapConfiguration" class="p-formgrid p-grid p-p-2">
        <div class="p-col-12">
            {{ heatmapConfiguration }}
        </div>
        <div class="p-float-label p-col-12 p-lg-6 p-fluid p-p-2">
            <InputNumber v-model="heatmapConfiguration.radius" class="kn-material-input" />
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.radius') }}</label>
        </div>
        <div class="p-float-label p-col-12 p-lg-6 p-fluid p-p-2">
            <InputNumber v-model="heatmapConfiguration.blur" class="kn-material-input" />
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.blur') }}</label>
        </div>
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeHeatmap } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'
import descriptor from '../MapVisualizationTypeDescriptor.json'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'map-visualization-type-heatmap',
    components: { InputNumber },
    props: { propHeatmapConfiguration: { type: Object as PropType<IMapWidgetVisualizationTypeHeatmap | null>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            heatmapConfiguration: null as IMapWidgetVisualizationTypeHeatmap | null
        }
    },
    watch: {
        propHeatmapConfiguration() {
            this.loadHeatmapConfiguration()
        }
    },
    created() {
        this.loadHeatmapConfiguration()
    },
    methods: {
        loadHeatmapConfiguration() {
            this.heatmapConfiguration = this.propHeatmapConfiguration
        }
    }
})
</script>
