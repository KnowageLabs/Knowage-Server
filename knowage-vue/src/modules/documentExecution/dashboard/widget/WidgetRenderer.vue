<template>
    <div class="widget-renderer" :style="getWidgetStyleString()">
        <TableWidget v-if="widget.type === 'table'" :propWidget="widget" :datasets="datasets" :editorMode="false" />
        <SelectorWidget v-if="widget.type === 'selector'" :propWidget="widget" :datasets="datasets" :dataToShow="mock.selectorMockedResponse" :editorMode="false" style="flex: 1" />
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 * TODO: the switch between different widget types will be added here
 */
import { defineComponent } from 'vue'
import { getWidgetStyleByType } from '../widget/TableWidget/TableWidgetHelper'
import TableWidget from './TableWidget/TableWidget.vue'
import SelectorWidget from './SelectorWidget/SelectorWidget.vue'
import mock from '../dataset/DatasetEditorTestMocks.json'

export default defineComponent({
    name: 'widget-renderer',
    emits: ['interaction'],
    components: { TableWidget, SelectorWidget },
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
    data() {
        return {
            mock
        }
    },
    created() {
        this.getWidgetStyleString()
    },
    methods: {
        click(e) {
            this.$emit('interaction', e, this.widget)
        },
        getWidgetStyleString() {
            const styleString = getWidgetStyleByType(this.widget, 'shadows') + getWidgetStyleByType(this.widget, 'padding') + getWidgetStyleByType(this.widget, 'borders') + getWidgetStyleByType(this.widget, 'background')
            return styleString
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-renderer {
    width: 100%;
    height: 100%;
    background-color: #fff;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}
</style>
