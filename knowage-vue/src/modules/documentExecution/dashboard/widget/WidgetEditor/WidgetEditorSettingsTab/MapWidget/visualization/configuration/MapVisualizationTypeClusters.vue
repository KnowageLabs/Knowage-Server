<template>
    {{ clusterConfigProp }}
    <MarkersConfiguration :marker-config-prop="markerConfigProp" />
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeCluster, IMapWidgetVisualizationTypeMarker } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import MarkersConfiguration from '../markers/MapVisualizationTypeMarkers.vue'

export default defineComponent({
    name: 'map-visualization-type',
    components: { MarkersConfiguration },
    props: { markerConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeMarker | undefined>, required: true }, clusterConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeCluster | undefined>, required: true } },
    emits: [],
    data() {
        return {}
    },
    watch: {},
    created() {},
    methods: {}
})
</script>

<style lang="scss" scoped></style>
