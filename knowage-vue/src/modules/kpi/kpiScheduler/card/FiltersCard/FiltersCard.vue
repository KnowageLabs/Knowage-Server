<template>
    <Card>
        <template #content>
            {{ filters }}
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'

export default defineComponent({
    name: 'filters-card',
    components: { Card },
    props: {},
    data() {
        return {
            filters: [] as any[]
        }
    },
    watch: {},
    created() {},
    methods: {}
})
</script>
