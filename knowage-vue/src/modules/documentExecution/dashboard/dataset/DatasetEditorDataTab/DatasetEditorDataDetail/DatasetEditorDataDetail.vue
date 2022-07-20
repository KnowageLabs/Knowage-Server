<template>
    <div id="dataset-editor-data-detail-container">
        <Card>
            <template #title> Advanced Card </template>
            <template #content> {{ dashboardDatasetsProp }}</template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    name: 'dataset-editor-data-detail',
    components: { Card },
    props: { dashboardDatasetsProp: { required: true, type: Array } },
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
