<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" />

        <div id="preview-widget-container" v-if="propWidget.settings && propWidget.type == 'table'" class="p-d-flex p-flex-column p-m-2" style="height: 300px; overflow: hidden" :style="getWidgetContainerStyle()">
            <TableWidget class="kn-flex" :propWidget="propWidget" :datasets="datasets" :editorMode="true" />
        </div>

        <div id="preview-widget-container" v-if="propWidget.settings && propWidget.type == 'selector'" class="p-d-flex p-flex-column p-m-2" style="max-height: 300px; overflow: hidden" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>
            <SelectorWidget :propWidget="propWidget" :dataToShow="mock.selectorMockedResponse" :editorMode="true" />
        </div>

        <div id="preview-widget-container" v-if="propWidget.settings && propWidget.type == 'selection'" class="p-d-flex p-flex-column p-m-2" style="max-height: 300px; overflow: hidden" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>
            <ActiveSelectionsWidget :propWidget="propWidget" :dataToShow="mock.selectionMockedResponse" :editorMode="true" />
            <!-- <ActiveSelectionsWidget :propWidget="propWidget" :dataToShow="[]" :editorMode="true" /> -->
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget } from '../../Dashboard'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import TableWidget from '../TableWidget/TableWidget.vue'
import SelectorWidget from '../SelectorWidget/SelectorWidget.vue'
import ActiveSelectionsWidget from '../ActiveSelectionsWidget/ActiveSelectionsWidget.vue'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget },
    props: {
        propWidget: {
            required: true,
            type: Object as PropType<IWidget>
        },
        datasets: { type: Array as PropType<IDataset[]> }
    },
    data() {
        return {
            descriptor,
            widgetTitle: null as any,
            mock
        }
    },
    created() {
        if (this.propWidget.settings && this.propWidget.type == 'selector') this.getWidgetTitleStyle()
    },
    mounted() {},
    methods: {
        logWidget() {
            console.log('widget ----------------- \n', this.propWidget)
        },
        getWidgetTitleStyle() {
            this.widgetTitle = this.propWidget.settings.style.title
            const styleString = getWidgetStyleByType(this.propWidget, 'title')
            return styleString + `height: ${this.widgetTitle.height}px;`
        },
        getWidgetContainerStyle() {
            const styleString = getWidgetStyleByType(this.propWidget, 'borders') + getWidgetStyleByType(this.propWidget, 'shadows') + getWidgetStyleByType(this.propWidget, 'padding') + getWidgetStyleByType(this.propWidget, 'background')
            return styleString
        }
    }
})
</script>
