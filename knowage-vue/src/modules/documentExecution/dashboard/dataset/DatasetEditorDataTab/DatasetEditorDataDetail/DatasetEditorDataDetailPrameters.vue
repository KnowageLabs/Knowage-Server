<template>
    <Card>
        <template #title> Parameters Card </template>
        <template #content>
            Dashboard -------------
            <br />
            {{ dashboardDatasetsProp }}
            <br />
            Selected -----------------
            <br />
            {{ selectedDatasetProp.parameters }}
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'

export default defineComponent({
    name: 'dataset-editor-data-detail-info',
    components: { Card },
    props: { selectedDatasetProp: { required: true, type: Object }, dashboardDatasetsProp: { required: true, type: Array as any } },
    emits: [],
    data() {
        return {}
    },
    setup() {},
    async created() {},
    methods: {}
})
</script>
