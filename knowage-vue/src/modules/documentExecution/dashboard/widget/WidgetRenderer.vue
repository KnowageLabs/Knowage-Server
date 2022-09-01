<template>
    <div class="widget-renderer">
        <TableWidget v-if="widget.type === 'table'" />
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

export default defineComponent({
    name: 'widget-renderer',
    emits: ['interaction'],
    components: { TableWidget },
    props: {
        widget: {
            required: true,
            type: Object
        },
        data: {
            required: true,
            type: Object
        }
    },
    methods: {
        click(e) {
            this.$emit('interaction', e, this.widget)
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
