<template>
    <div class="widget p-m-2" :style="getWidgetContainerStyle()">
        <div v-if="widget.settings.style.title && widget.settings.style.title.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
            {{ widgetTitle?.text }}
        </div>
        <div class="widget-editor-preview" :style="getWidgetPadding()">
            <TableWidget v-if="widget.type == 'table'" :propWidget="widget" :datasets="datasets" :editorMode="true" />
            <SelectorWidget v-if="widget.type == 'selector'" :propWidget="widget" :dataToShow="mock.selectorMockedResponse" :editorMode="true" />
            <ActiveSelectionsWidget v-if="widget.type == 'selection'" :propWidget="widget" :dataToShow="mock.selectionMockedResponse" :editorMode="true" />
        </div>
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
    created() {},
    methods: {
        click(e) {
            this.$emit('interaction', e, this.widget)
        },
        getWidgetTitleStyle() {
            let widgetTitle = this.widget.settings.style.title
            const styleString = getWidgetStyleByType(this.widget, 'title')
            return styleString + `height: ${widgetTitle.height ?? 25}px;`
        },
        getWidgetContainerStyle() {
            const styleString = getWidgetStyleByType(this.widget, 'borders') + getWidgetStyleByType(this.widget, 'shadows') + getWidgetStyleByType(this.widget, 'background')
            return styleString
        },
        getWidgetPadding() {
            const styleString = getWidgetStyleByType(this.widget, 'padding')
            return styleString
        }
    }
})
</script>
<style lang="scss" scoped>
.widget {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
    flex: 1;
    .widget-editor-preview {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
}
</style>
