<template>
    {{ key }}
    <router-view v-if="item" v-slot="{ Component }" :functionalityId="functionalityId" @close="$emit('close', item)">
        <keep-alive>
            <component :is="Component" :key="key"></component>
        </keep-alive>
    </router-view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-browser-tab',
    components: {},
    emits: ['close'],
    props: { item: { type: Object }, mode: { type: String }, functionalityId: { type: String } },
    data() {
        return {}
    },
    computed: {
        key(): string {
            return this.item?.routerId
        }
    },
    watch: {},
    created() {},
    methods: {}
})
</script>
