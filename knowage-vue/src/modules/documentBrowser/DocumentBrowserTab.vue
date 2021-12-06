<template>
    <router-view></router-view>
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
