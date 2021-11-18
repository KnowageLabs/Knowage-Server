<template>
    <h3>Functionality not in this sprint</h3>
    <h3>Document ID: {{ id }}</h3>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-execution-container',
    components: {},
    props: { id: { type: String } },
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>
