<template>
    <div class="widget-container" :style="getWidgetContainerStyle()">
        <div v-if="widget.settings.style.title && widget.settings.style.title.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
            {{ widget.settings.style.title.text }}
        </div>
        <div class="widget-container-renderer" :style="getWidgetPadding()">
            <TableWidget
                v-if="widget.type == 'table'"
                :prop-widget="widget"
                :datasets="datasets"
                :data-to-show="dataToShow"
                :editor-mode="false"
                :prop-active-selections="activeSelections"
                :dashboard-id="dashboardId"
                @pageChanged="$emit('reloadData')"
                @sortingChanged="$emit('reloadData')"
                @launchSelection="$emit('launchSelection', $event)"
            />
            <SelectorWidget
                v-if="widget.type == 'selector'"
                :prop-widget="widget"
                :data-to-show="dataToShow"
                :widget-initial-data="widgetInitialData"
                :prop-active-selections="activeSelections"
                :editor-mode="false"
                :dashboard-id="dashboardId"
                :datasets="datasets"
                :selection-is-locked="selectionIsLocked"
            />
            <ActiveSelectionsWidget v-if="widget.type == 'selection'" :prop-widget="widget" :prop-active-selections="activeSelections" :editor-mode="false" :dashboard-id="dashboardId" />
            <WebComponentContainer v-if="widget.type == 'html' || widget.type == 'text'" :prop-widget="widget" :widget-data="dataToShow" :prop-active-selections="activeSelections" :editor-mode="false" :dashboard-id="dashboardId" :variables="variables"></WebComponentContainer>
            <HighchartsContainer v-if="widget.type === 'highcharts'" :widget-model="widget" :data-to-show="widgetData" :prop-active-selections="activeSelections" :editor-mode="false" :dashboard-id="dashboardId"></HighchartsContainer>
            <ChartJSContainer v-if="widget.type === 'chartJS'" :widget-model="widget" :data-to-show="widgetData" :prop-active-selections="activeSelections" :editor-mode="false" :dashboard-id="dashboardId"></ChartJSContainer>
            <ImageWidget v-if="widget.type === 'image'" :widget-model="widget" :dashboard-id="dashboardId" :editor-mode="false" />
            <CustomChartWidget v-if="widget.type == 'customchart'" :prop-widget="widget" :widget-data="widgetData" :prop-active-selections="activeSelections" :editor-mode="false" :dashboard-id="dashboardId" :variables="variables" @loading="$emit('loading', $event)"></CustomChartWidget>
        </div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of creating the common widget elements to be rendered.
 */
import { defineComponent, PropType } from 'vue'
import { getWidgetStyleByType } from '../widget/TableWidget/TableWidgetHelper'
import { IDashboardDataset, ISelection, IVariable } from '../Dashboard'
import TableWidget from './TableWidget/TableWidget.vue'
import SelectorWidget from './SelectorWidget/SelectorWidget.vue'
import ActiveSelectionsWidget from './ActiveSelectionsWidget/ActiveSelectionsWidget.vue'
import mock from '../dataset/DatasetEditorTestMocks.json'
import WebComponentContainer from './WebComponent/WebComponentContainer.vue'
import HighchartsContainer from '../widget/ChartWidget/Highcharts/HighchartsContainer.vue'
import ChartJSContainer from '../widget/ChartWidget/ChartJS/ChartJSContainer.vue'
import ImageWidget from '../widget/ImageWidget/ImageWidget.vue'
import CustomChartWidget from '../widget/CustomChartWidget/CustomChartWidget.vue'

export default defineComponent({
    name: 'widget-renderer',
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget, WebComponentContainer, HighchartsContainer, ChartJSContainer, ImageWidget, CustomChartWidget },
    props: {
        widget: { required: true, type: Object as any },
        widgetLoading: { required: true, type: Boolean as any },
        widgetData: { required: true, type: Object },
        widgetInitialData: { required: true, type: Object },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        selectionIsLocked: { type: Boolean, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true }
    },
    emits: ['interaction', 'launchSelection', 'reloadData', 'loading'],
    data() {
        return {
            mock,
            dataToShow: {} as any,
            activeSelections: [] as ISelection[],
            htmlContent: '' as string,
            webComponentCss: '' as string,
            textModel: '' as string
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
        async loadDataToShow() {
            this.dataToShow = this.widgetData
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        getWidgetTitleStyle() {
            const widgetTitle = this.widget.settings.style.title
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
