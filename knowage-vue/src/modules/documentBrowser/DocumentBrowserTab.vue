<template>
    <router-view v-if="item" v-slot="{ Component }" :functionalityId="functionalityId" :item="item" @close="$emit('close', item)" @iframeCreated="onIframeCreated" @closeIframe="$emit('closeIframe')">
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
    emits: ['close', 'iframeCreated', 'closeIframe'],
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
    methods: {
        onIframeCreated(payload: any) {
            this.$emit('iframeCreated', payload)
        }
    }
})
</script>
