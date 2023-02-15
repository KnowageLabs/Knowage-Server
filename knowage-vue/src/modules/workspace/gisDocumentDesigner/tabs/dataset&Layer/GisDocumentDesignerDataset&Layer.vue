<template>
    <DatasetCard v-if="$route.path.includes('new')" :document-data-prop="documentDataProp" :is-dataset-chosen="isDatasetChosen" @datasetChanged="$emit('datasetChanged', $event)" @datasetDeleted="$emit('datasetDeleted')" />
    <LayersCard :document-data-prop="documentDataProp" :is-dataset-chosen="isDatasetChosen" @layerChanged="$emit('layerChanged', $event)" />
    <LayerFilters v-if="documentDataProp.allDrivers && documentDataProp.allDrivers.length > 0 && !isDatasetChosen" :document-data-prop="documentDataProp" @driverChanged="$emit('driverChanged', $event)" />
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
    props: { documentDataProp: { type: Object as any, required: true }, allLayers: Array, isDatasetChosen: Boolean },
    emits: [],
    data() {
        return {}
    },
    computed: {},
    created() {},
    methods: {}
})
</script>
