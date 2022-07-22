<template>
    <Card>
        <template #title> Indexes Card </template>
        <template #content> test </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card },
    props: { selectedDatasetProp: { required: true, type: Object } },
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
