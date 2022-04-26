<template>
    <DatasetCard v-if="$route.path.includes('new')" :documentDataProp="documentDataProp" :isDatasetChosen="isDatasetChosen" @datasetChanged="$emit('datasetChanged', $event)" @datasetDeleted="$emit('datasetDeleted')" />
    <LayersCard :documentDataProp="documentDataProp" :isDatasetChosen="isDatasetChosen" @layerChanged="$emit('layerChanged', $event)" />
    <LayerFilters v-if="documentDataProp.allDrivers && documentDataProp.allDrivers.length > 0 && !isDatasetChosen" :documentDataProp="documentDataProp" @driverChanged="$emit('driverChanged', $event)" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DatasetCard from './GisDocumentDesignerDataset.vue'
import LayersCard from './GisDocumentDesignerLayer.vue'
import LayerFilters from './GisDocumentDesignerLayerFilters.vue'

export default defineComponent({
    components: {
        DatasetCard,
        LayersCard,
        LayerFilters
    },
    emits: [],
    props: { documentDataProp: { type: Object as any, required: true }, allLayers: Array, isDatasetChosen: Boolean },
    computed: {},
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>
