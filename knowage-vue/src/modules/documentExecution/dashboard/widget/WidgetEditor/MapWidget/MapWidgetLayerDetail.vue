<template>
    <div class="p-d-flex p-flex-column kn-flex">
        <MapWidgetLayerInfo :selected-layer="layer"></MapWidgetLayerInfo>
        <MapWidgetMetadata class="p-mt-2" :selected-layer="layer"></MapWidgetMetadata>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IMapWidgetLayer } from '../../../interfaces/mapWidget/DashboardMapWidget'
import MapWidgetLayerInfo from './layerInfo/MapWidgetLayerInfo.vue'
import MapWidgetMetadata from './metadata/MapWidgetMetadata.vue'

export default defineComponent({
    name: 'map-widget-layer-detail',
    components: { MapWidgetLayerInfo, MapWidgetMetadata },
    props: { selectedLayer: { type: Object as PropType<IMapWidgetLayer | null>, required: true } },
    data() {
        return {
            layer: null as IMapWidgetLayer | null
        }
    },
    watch: {
        selectedLayer() {
            this.loadLayer()
        }
    },
    created() {
        this.loadLayer()
    },
    methods: {
        loadLayer() {
            this.layer = this.selectedLayer
        }
    }
})
</script>
