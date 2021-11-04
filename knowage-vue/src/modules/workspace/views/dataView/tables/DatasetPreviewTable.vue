<template>
    <h1>IT WORKS</h1>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import datasetPreviewTableDescriptor from './DatasetPreviewTableDescriptor.json'

export default defineComponent({
    name: 'dataset-preview-table',
    components: {},
    data() {
        return { datasetPreviewTableDescriptor }
    },
    async created() {},
    methods: {}
})
</script>
