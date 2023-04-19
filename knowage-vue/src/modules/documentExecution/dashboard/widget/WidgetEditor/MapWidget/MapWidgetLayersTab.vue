<template>
    <LayersList :widget-model="widget" :datasets="datasets" :selected-datasets="selectedDatasets" @layerSelected="setSelectedLayer"></LayersList>
    <MapWidgetLayerDetail id="map-widget-layer-detail" class="p-d-flex kn-flex kn-overflow p-p-3" :selected-layer="selectedLayer"></MapWidgetLayerDetail>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IDataset, IWidget } from '../../../Dashboard'
import { mapState } from 'pinia'
import { IMapWidgetLayer } from '../../../interfaces/mapWidget/DashboardMapWidget'
import mainStore from '@/App.store'
import LayersList from './MapWidgetLayersTabList.vue'
import MapWidgetLayerDetail from './MapWidgetLayerDetail.vue'

export default defineComponent({
    name: 'map-widget-layers-tab',
    components: { LayersList, MapWidgetLayerDetail },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: {
            type: Array as PropType<IDataset[]>,
            default: function () {
                return []
            }
        },
        selectedDatasets: {
            type: Array as PropType<IDataset[]>,
            default: function () {
                return []
            }
        }
    },
    data() {
        return {
            selectedDataset: null as IDataset | null,
            widget: {} as IWidget,
            selectedLayer: null as IMapWidgetLayer | null
        }
    },
    computed: {
        ...mapState(mainStore, {
            isEnterprise: 'isEnterprise'
        })
    },
    created() {
        this.loadWidget()
    },
    methods: {
        loadWidget() {
            this.widget = this.propWidget
        },
        setSelectedLayer(layer: any) {
            this.selectedLayer = layer
        }
    }
})
</script>

<style lang="scss" scoped>
#map-widget-layer-detail {
    overflow: auto;
}
</style>
