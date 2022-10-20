<template>
    <div class="widget-container" :style="getWidgetContainerStyle()">
        <div v-if="widget.settings.style.title && widget.settings.style.title.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
            {{ widget.settings.style.title.text }}
        </div>
        <div class="widget-container-renderer" :style="getWidgetPadding()">
            <TableWidget v-if="widget.type == 'table'" :propWidget="widget" :datasets="datasets" :dataToShow="dataToShow" :editorMode="false" @pageChanged="$emit('pageChanged')" />
            <!-- <SelectorWidget v-if="widget.type == 'selector'" :propWidget="widget" :dataToShow="mock.selectorMockedResponse" :editorMode="true" :dashboardId="dashboardId" :datasets="datasets" :selectionIsLocked="selectionIsLocked" /> -->
            <SelectorWidget v-if="widget.type == 'selector'" :propWidget="widget" :dataToShow="dataToShow" :widgetInitialData="widgetInitialData" :propActiveSelections="activeSelections" :editorMode="false" :dashboardId="dashboardId" :datasets="datasets" :selectionIsLocked="selectionIsLocked" />
            <ActiveSelectionsWidget v-if="widget.type == 'selection'" :propWidget="widget" :propActiveSelections="activeSelections" :editorMode="false" :dashboardId="dashboardId" />
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 * TODO: the switch between different widget types will be added here
 */
import { defineComponent, PropType } from 'vue'
import { getWidgetStyleByType } from '../widget/TableWidget/TableWidgetHelper'
import TableWidget from './TableWidget/TableWidget.vue'
import SelectorWidget from './SelectorWidget/SelectorWidget.vue'
import ActiveSelectionsWidget from './ActiveSelectionsWidget/ActiveSelectionsWidget.vue'
import mock from '../dataset/DatasetEditorTestMocks.json'
import { IDataset, ISelection } from '../Dashboard'

export default defineComponent({
    name: 'widget-renderer',
    emits: ['interaction', 'pageChanged'],
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget },
    props: {
        widget: { required: true, type: Object as any },
        widgetData: { required: true, type: Object },
        widgetInitialData: { required: true, type: Object },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        selectionIsLocked: { type: Boolean, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true }
    },
    data() {
        return {
            mock,
            dataToShow: {} as any,
            activeSelections: [] as ISelection[]
        }
    },
    watch: {
        widgetData() {
            this.loadDataToShow()
        },
        propActiveSelections() {
            this.loadActiveSelections()
        }
    },
    created() {
        this.loadActiveSelections()
        this.loadDataToShow()
    },
    methods: {
        loadDataToShow() {
            this.dataToShow = this.widgetData
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
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
.widget-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
    flex: 1;
    .widget-container-renderer {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }
}
</style>
