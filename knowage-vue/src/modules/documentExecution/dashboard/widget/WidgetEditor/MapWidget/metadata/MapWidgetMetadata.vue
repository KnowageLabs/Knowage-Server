<template>
    <div v-if="layer">
        <h2 class="kn-material-input-label">{{ $t('common.metadata') }}</h2>
        <MapWidgetMetadataSpatialAttribute :prop-spatial-attribute="spatialAttribute"></MapWidgetMetadataSpatialAttribute>
        <hr />
        <MapWidgetMetadataFields v-if="selectedLayer?.content?.columnSelectedOfDataset" :propFields="selectedLayer.content.columnSelectedOfDataset"></MapWidgetMetadataFields>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import MapWidgetMetadataSpatialAttribute from './MapWidgetMetadataSpatialAttribute.vue'
import MapWidgetMetadataFields from './MapWidgetMetadataFields.vue'

export default defineComponent({
    name: 'map-widget-metadata',
    components: { MapWidgetMetadataSpatialAttribute, MapWidgetMetadataFields },
    props: {
        selectedLayer: { type: Object as PropType<any>, required: true }
    },
    data() {
        return {
            layer: null as any,
            spatialAttribute: null as any,
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
            console.log('-------- LOADED LAYER: ', this.layer)
            this.loadSpatialAttribute()
        },
        loadSpatialAttribute() {
            if (!this.layer || !this.layer.content || !this.layer.content.columnSelectedOfDataset) return
            const index = this.layer.content.columnSelectedOfDataset.findIndex((column: any) => column.fieldType === 'SPATIAL_ATTRIBUTE')
            if (index !== -1) this.spatialAttribute = this.layer.content.columnSelectedOfDataset[index]
        }
    }
})
</script>
