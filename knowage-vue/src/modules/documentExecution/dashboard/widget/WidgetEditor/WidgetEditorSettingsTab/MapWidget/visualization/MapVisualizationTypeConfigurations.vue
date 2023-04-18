<template>
    <div class="p-d-flex p-flex-column">
        <MarkersConfig v-if="visType.type === 'markers'" :marker-config-prop="visTypeProp.markerConf" />
        <div v-else-if="visType.type === 'balloons'">balloons component</div>
        <div v-else-if="visType.type === 'pies'">pies component</div>
        <div v-else-if="visType.type === 'clusters'">clusters component</div>
        <MapVisualizationTypeHeatmap v-else-if="visType.type === 'heatmap'" :prop-heatmap-configuration="visType.heatmapConf ?? null"></MapVisualizationTypeHeatmap>
        <MapVisualizationTypeChoropleth v-else-if="visType.type === 'choropleth'" :prop-choropleth-configuration="visType.analysisConf ?? null"></MapVisualizationTypeChoropleth>
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationType } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import descriptor from './MapVisualizationTypeDescriptor.json'
import MapVisualizationTypeHeatmap from './configuration/MapVisualizationTypeHeatmap.vue'
import MapVisualizationTypeChoropleth from './configuration/MapVisualizationTypeChoropleth.vue'

import MarkersConfig from './configurations/MapMarkersConfiguration.vue'

export default defineComponent({
    name: 'map-visualization-type',
    components: { MarkersConfig, MapVisualizationTypeHeatmap, MapVisualizationTypeChoropleth },
    props: { visTypeProp: { type: Object as PropType<IMapWidgetVisualizationType>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            visType: {} as IMapWidgetVisualizationType
        }
    },
    watch: {
        widgetModel() {
            this.loadVisType()
        }
    },
    created() {
        this.loadVisType()
    },
    methods: {
        loadVisType() {
            this.visType = this.visTypeProp as IMapWidgetVisualizationType
        }
    }
})
</script>

<style lang="scss" scoped></style>
