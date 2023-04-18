<template>
    {{ clusterConfig }}
    <div class="p-d-flex p-flex-column">
        <div class="p-d-flex p-flex-row">
            <span class="p-float-label kn-flex">
                <InputNumber v-model="clusterConfig.radiusSize" class="kn-material-input kn-width-full" :max="500" />
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.map.markerTypes.scale%') }}</label>
            </span>
        </div>
        <MarkersConfiguration :marker-config-prop="markerConfigProp" />
    </div>
</template>

<script lang="ts">
import { IMapWidgetVisualizationTypeCluster, IMapWidgetVisualizationTypeMarker } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import { defineComponent, PropType } from 'vue'

import MarkersConfiguration from '../markers/MapVisualizationTypeMarkers.vue'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'map-visualization-type',
    components: { MarkersConfiguration, InputNumber },
    props: { markerConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeMarker | undefined>, required: true }, clusterConfigProp: { type: Object as PropType<IMapWidgetVisualizationTypeCluster | undefined>, required: true } },
    emits: [],
    data() {
        return {
            clusterConfig: {} as IMapWidgetVisualizationTypeCluster
        }
    },
    watch: {
        clusterConfigProp() {
            this.loadClusterConfig()
        }
    },
    created() {
        this.loadClusterConfig()
    },
    methods: {
        loadClusterConfig() {
            this.clusterConfig = this.clusterConfigProp as IMapWidgetVisualizationTypeCluster
        }
    }
})
</script>

<style lang="scss" scoped></style>
