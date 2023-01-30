<template>
    <div ref="widgetPreviewContainer" class="widget-editor-preview-container p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow">
        <div class="preview-buttons-container p-d-flex" style="position: absolute; top: 38px; right: 10px">
            <Button icon="fas fa-terminal" class="p-button-rounded p-button-text p-button-plain expand-button" @click="logStuff" />
            <Button icon="fas fa-maximize" class="p-button-rounded p-button-text p-button-plain expand-button" @click="toggleExpandPreview" />
            <Button icon="fas fa-rotate-right" class="p-button-rounded p-button-text p-button-plain" @click="getWidgetData" />
        </div>

        <ProgressBar v-if="loading" class="p-mx-2" mode="indeterminate" />
        <div class="widget-container p-mx-2" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>

            <div class="widget-container-renderer" :style="getWidgetPadding()">
                <TableWidget v-if="propWidget.type == 'table'" :propWidget="propWidget" :datasets="datasets" :dataToShow="widgetData" :editorMode="true" :dashboardId="dashboardId" :propActiveSelections="activeSelections" @pageChanged="getWidgetData" />
                <SelectorWidget v-if="propWidget.type == 'selector'" :propWidget="propWidget" :dataToShow="widgetData" :widgetInitialData="widgetData" :editorMode="true" :propActiveSelections="activeSelections" :datasets="datasets" :selectionIsLocked="false" :dashboardId="dashboardId" />
                <ActiveSelectionsWidget v-if="propWidget.type == 'selection'" :propWidget="propWidget" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId" />
                <WebComponentContainer v-if="(propWidget.type == 'html' || propWidget.type == 'text') && !loading" :propWidget="propWidget" :widgetData="widgetData" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId" :variables="variables"></WebComponentContainer>
                <HighchartsContainer v-if="propWidget.type === 'highcharts' && !loading" :widgetModel="propWidget" :dataToShow="widgetData" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId"></HighchartsContainer>
                <ChartJSContainer v-if="propWidget.type === 'chartJS' && !loading" :widgetModel="propWidget" :dataToShow="widgetData" :editorMode="true" :dashboardId="dashboardId" :propActiveSelections="activeSelections"></ChartJSContainer>
                <ImageWidget v-if="propWidget.type === 'image'" :widgetModel="propWidget" :dashboardId="dashboardId" :editorMode="true" />
                <CustomChartWidget v-if="propWidget.type == 'customchart' && !loading" :propWidget="propWidget" :widgetData="widgetData" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId" :variables="variables"></CustomChartWidget>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDataset, ISelection, IVariable, IWidget } from '../../Dashboard'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import TableWidget from '../TableWidget/TableWidget.vue'
import SelectorWidget from '../SelectorWidget/SelectorWidget.vue'
import ActiveSelectionsWidget from '../ActiveSelectionsWidget/ActiveSelectionsWidget.vue'
import { emitter } from '../../DashboardHelpers'
import { getWidgetData } from '../../DataProxyHelper'
import ProgressBar from 'primevue/progressbar'
import { mapState, mapActions } from 'pinia'
import store from '../../Dashboard.store'
import deepcopy from 'deepcopy'
import WebComponentContainer from '../WebComponent/WebComponentContainer.vue'
import HighchartsContainer from '../ChartWidget/Highcharts/HighchartsContainer.vue'
import ChartJSContainer from '../ChartWidget/ChartJS/ChartJSContainer.vue'
import ImageWidget from '../ImageWidget/ImageWidget.vue'
import CustomChartWidget from '../CustomChartWidget/CustomChartWidget.vue'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget, ProgressBar, WebComponentContainer, HighchartsContainer, ChartJSContainer, ImageWidget, CustomChartWidget },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true }
    },
    data() {
        return {
            descriptor,
            widgetTitle: null as any,
            mock,
            widgetData: {} as any,
            loading: false,
            activeSelections: [] as ISelection[],
            htmlContent: '',
            webComponentCss: '',
            textModel: ''
        }
    },
    computed: {
        ...mapState(store, ['dashboards'])
    },
    created() {
        this.getWidgetData()
        this.setEventListeners()
        this.getWidgetTitleStyle()
        this.loadWebComponentData()
    },
    unmounted() {
        this.unsetEventListeners()
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections']),
        setEventListeners() {
            emitter.on('clearWidgetData', this.clearWidgetData)
            emitter.on('refreshWidgetWithData', this.getWidgetData)
        },
        unsetEventListeners() {
            emitter.off('clearWidgetData', this.clearWidgetData)
            emitter.off('refreshWidgetWithData', this.getWidgetData)
        },
        loadWebComponentData() {},
        async getWidgetData() {
            this.loading = true
            this.widgetData = await getWidgetData(this.propWidget, this.datasets, this.$http, false, this.activeSelections)
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            this.loading = false
        },
        clearWidgetData() {
            this.widgetData = { metaData: {}, rows: [] }
        },
        getWidgetTitleStyle() {
            this.widgetTitle = this.propWidget.settings.style.title
            const styleString = getWidgetStyleByType(this.propWidget, 'title')
            return styleString + `height: ${this.widgetTitle.height ?? 25}px;`
        },
        getWidgetContainerStyle() {
            const styleString = getWidgetStyleByType(this.propWidget, 'borders') + getWidgetStyleByType(this.propWidget, 'shadows') + getWidgetStyleByType(this.propWidget, 'background')
            if (this.propWidget.type == 'table' || this.propWidget.type == 'html' || this.propWidget.type == 'text') return styleString + 'height: 30%;'
            else return styleString
        },
        getWidgetPadding() {
            const styleString = getWidgetStyleByType(this.propWidget, 'padding')
            return styleString
        },
        toggleExpandPreview() {
            const widgetPreviewContainerRef = this.$refs.widgetPreviewContainer as any
            widgetPreviewContainerRef.classList.toggle('expand')
            setTimeout(() => {
                emitter.emit('chartWidgetResized', this.propWidget)
            }, 250)
        },
        logStuff() {
            console.group('facet selected ------------------------------------')
            console.log('WIDGET', this.propWidget)
            console.log('DATA', this.widgetData)
            console.groupEnd()
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-editor-preview-container {
    flex: 0.5;
    border-left: 1px solid #ccc;
    .widget-container {
        display: flex;
        flex-direction: column;
        overflow: hidden;
        flex: 1;
        max-height: 50%;
        .widget-container-renderer {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
    }
}
.widget-editor-preview-container.expand {
    flex: 10000;
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
