<template>
    <router-view v-if="!loading" v-slot="{ Component }" @close="$emit('close', item)">
        <keep-alive>
            <component :is="Component" :key="$route.fullPath"></component>
        </keep-alive>
    </router-view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-browser-tab',
    components: {},
    emits: ['close'],
    props: { item: { type: Object }, mode: { type: String } },
    data() {
        return {}
    },
    created() {},
    methods: {}
})
</script>
