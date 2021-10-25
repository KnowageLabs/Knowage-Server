<template>
    <div>ITEM: {{ item }} MODE: {{ mode }}</div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-browser-tab',
    components: {},
    props: { item: { type: Object }, mode: { type: String } },
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>

<style>
iframe {
    width: 100vw;
    height: 100vh;
}
</style>
