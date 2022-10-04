<template>
    <div class="widget-renderer" :style="getWidgetStyleString()">
        {{ widget.id }}
        <TableWidget v-if="widget.type === 'table'" :propWidget="widget" :datasets="datasets" :editorMode="false" style="flex: 1" />
        <!-- <div class="drag-handle">{{ widget.id }} {{ widget.type }}</div> -->
        <!-- <div style="width: 100%; height: 100%; display: flex; justify-content: center; align-items: center">
            <div>{{ data }}</div>
            <button @click="click">CLICKME</button>
        </div> -->
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 * TODO: the switch between different widget types will be added here
 */
import { defineComponent } from 'vue'
import TableWidget from './TableWidget/TableWidget.vue'
import { getWidgetStyleByType } from '../widget/TableWidget/TableWidgetHelper'

export default defineComponent({
    name: 'widget-renderer',
    emits: ['interaction'],
    components: { TableWidget },
    props: {
        widget: {
            required: true,
            type: Object as any
        },
        data: {
            required: true,
            type: Object
        },
        datasets: { type: Array }
    },
    created() {
        this.getWidgetStyleString()
    },
    methods: {
        click(e) {
            this.$emit('interaction', e, this.widget)
        },
        getWidgetStyleString() {
            const styleString = getWidgetStyleByType(this.widget, 'shadows') + getWidgetStyleByType(this.widget, 'padding') + getWidgetStyleByType(this.widget, 'borders')
            return styleString
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-renderer {
    width: 100%;
    height: 100%;
    background-color: #ccc;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
</style>
