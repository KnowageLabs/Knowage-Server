<template>
    <grid-item :key="item.i" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />
        <WidgetRenderer :widget="widget" v-if="initialized"></WidgetRenderer>
    </grid-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import WidgetRenderer from './WidgetRenderer.vue'
import Skeleton from 'primevue/skeleton'

export default defineComponent({
    name: 'widget-manager',
    components: { Skeleton, WidgetRenderer },
    inject: ['dHash'],
    props: {
        item: {
            required: true,
            type: Object
        },
        activeSheet: {
            type: Boolean
        },
        widget: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            loading: true,
            initialized: false
        }
    },
    methods: {
        initializeWidget() {
            setTimeout(() => {
                this.initialized = true
            }, 1000)
        }
    },
    updated() {
        if (!this.initialized && this.activeSheet) {
            this.$nextTick()
            this.initializeWidget()
        }
    }
})
</script>
<style lang="scss"></style>
