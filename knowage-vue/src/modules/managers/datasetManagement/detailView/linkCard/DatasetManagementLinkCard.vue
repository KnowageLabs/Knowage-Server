<template>
    <Card>
        <template #content>
            test
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card },
    props: {
        selectedDataset: { type: Object as any }
    },
    computed: {},
    emits: [],
    data() {
        return {}
    },
    created() {},
    watch: {},

    methods: {}
})
</script>
