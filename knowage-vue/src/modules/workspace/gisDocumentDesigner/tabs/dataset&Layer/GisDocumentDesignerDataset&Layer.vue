<template>
    <DatasetCard v-if="$route.path.includes('new')" :documentDataProp="documentDataProp" :isDatasetChosen="isDatasetChosen" @datasetChanged="$emit('datasetChanged', $event)" @datasetDeleted="$emit('datasetDeleted')" />
    <LayersCard :documentDataProp="documentDataProp" :isDatasetChosen="isDatasetChosen" @layerChanged="$emit('layerChanged', $event)" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DatasetCard from './GisDocumentDesignerDataset.vue'
import LayersCard from './GisDocumentDesignerLayer.vue'

export default defineComponent({
    components: {
        DatasetCard,
        LayersCard
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
