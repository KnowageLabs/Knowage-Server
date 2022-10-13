<template>
    <div class="widget-editor-preview-container p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow">
        <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" />

        <div class="widget p-m-2" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>
            <div class="widget-editor-preview" :style="getWidgetPadding()">
                <TableWidget v-if="propWidget.type == 'table'" :propWidget="propWidget" :datasets="datasets" :editorMode="true" />
                <SelectorWidget v-if="propWidget.type == 'selector'" :propWidget="propWidget" :dataToShow="mock.selectorMockedResponse" :editorMode="true" />
                <ActiveSelectionsWidget v-if="propWidget.type == 'selection'" :propWidget="propWidget" :dataToShow="mock.selectionMockedResponse" :editorMode="true" />
                <!-- <ActiveSelectionsWidget :propWidget="propWidget" :dataToShow="[]" :editorMode="true" /> -->
            </div>
        </div>
    </div>
</template>

<script lang="ts">
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
        this.getWidgetTitleStyle()
    },
    mounted() {},
    methods: {
        logWidget() {
            console.log('widget ----------------- \n', this.propWidget)
        },
        getWidgetTitleStyle() {
            this.widgetTitle = this.propWidget.settings.style.title
            const styleString = getWidgetStyleByType(this.propWidget, 'title')
            return styleString + `height: ${this.widgetTitle.height ?? 25}px;`
        },
        getWidgetContainerStyle() {
            const styleString = getWidgetStyleByType(this.propWidget, 'borders') + getWidgetStyleByType(this.propWidget, 'shadows') + getWidgetStyleByType(this.propWidget, 'background')
            return styleString
        },
        getWidgetPadding() {
            const styleString = getWidgetStyleByType(this.propWidget, 'padding')
            return styleString
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-editor-preview-container {
    flex: 0.5;
    border-left: 1px solid #ccc;
    .widget {
        display: flex;
        flex-direction: column;
        overflow: hidden;
        flex: 1;
        max-height: 35%;
        .widget-editor-preview {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
    }
}
// @media screen and (max-width: 1199px) {
//     .widget-editor-preview-container {
//         -webkit-transition: width 0.3s;
//         transition: flex 0.3s;
//         flex: 0;
//     }
// }
// @media screen and (min-width: 1200px) {
//     .widget-editor-preview-container {
//         -webkit-transition: width 0.3s;
//         transition: flex 0.3s;
//         flex: 0.5;
//     }
// }
</style>
