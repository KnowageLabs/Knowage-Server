<template>
    <div v-if="layer" class="widget-editor-card p-p-2">
        <h3 class="p-ml-3">{{ $t('common.metadata') }}</h3>
        <MapWidgetMetadataSpatialAttribute :prop-spatial-attribute="spatialAttribute"></MapWidgetMetadataSpatialAttribute>
        <hr />
        <MapWidgetMetadataFields v-if="selectedLayer?.content?.columnSelectedOfDataset" :propFields="selectedLayer.content.columnSelectedOfDataset"></MapWidgetMetadataFields>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import { IMapWidgetLayer, IWidgetMapLayerColumn } from '@/modules/documentExecution/dashboard/interfaces/mapWidget/DashboardMapWidget'
import MapWidgetMetadataSpatialAttribute from './MapWidgetMetadataSpatialAttribute.vue'
import MapWidgetMetadataFields from './MapWidgetMetadataFields.vue'

export default defineComponent({
    name: 'map-widget-metadata',
    components: { MapWidgetMetadataSpatialAttribute, MapWidgetMetadataFields },
    props: {
        selectedLayer: { type: Object as PropType<IMapWidgetLayer | null>, required: true }
    },
    data() {
        return {
            layer: null as IMapWidgetLayer | null,
            spatialAttribute: null as IWidgetMapLayerColumn | null,
            fields: [] as any[]
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
            this.loadSpatialAttribute()
        },
        loadSpatialAttribute() {
            if (!this.layer || !this.layer.content || !this.layer.content.columnSelectedOfDataset) return
            const index = this.layer.content.columnSelectedOfDataset.findIndex((column: IWidgetMapLayerColumn) => column.fieldType === 'SPATIAL_ATTRIBUTE')
            if (index !== -1) this.spatialAttribute = this.layer.content.columnSelectedOfDataset[index]
        }
    }
})
</script>
