<template>
    <div>
        <MapWidgetMetadata :selectedLayer="layer"></MapWidgetMetadata>
    </div>
</template>

<script lang="ts">
import { PropType, defineComponent } from 'vue'
import MapWidgetMetadata from './metadata/MapWidgetMetadata.vue'

export default defineComponent({
    name: 'map-widget-layer-detail',
    components: { MapWidgetMetadata },
    props: { selectedLayer: { type: Object as PropType<any>, required: true } },
    data() {
        return {
            layer: null as any
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
            console.log('---- CAAAAAALED', this.layer)
        }
    }
})
</script>
