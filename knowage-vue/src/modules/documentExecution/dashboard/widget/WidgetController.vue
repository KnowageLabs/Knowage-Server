<template>
    <grid-item :key="item.i" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" />
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
    props: {
        item: {
            required: true
        },
        sheet: {
            required: true
        },
        widget: {
            required: true
        }
    },
    data() {
        return {
            loading: true,
            initialized: false
        }
    },
    updated() {
        // @ts-ignore
        if (this.sheet === 0 || this.sheet === this.$store.state.dashboard.dashboards['7961fdeb-545f-4d08-9e3c-ba2d48be597f']) {
            setTimeout(() => (this.initialized = true), 1000)
        }
    }
})
</script>
<style lang="scss"></style>
