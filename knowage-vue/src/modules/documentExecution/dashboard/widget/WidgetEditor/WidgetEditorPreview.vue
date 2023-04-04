<template>
    <div ref="widgetPreviewContainer" class="widget-editor-preview-container p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow">
        <ProgressBar v-if="loading || customChartLoading" class="p-mx-2" mode="indeterminate" />
        <div class="widget-container p-mx-2" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>

            <div class="widget-container-renderer" :style="getWidgetPadding()">
                <TableWidget v-if="propWidget.type == 'table'" :prop-widget="propWidget" :datasets="datasets" :data-to-show="widgetData" :editor-mode="true" :dashboard-id="dashboardId" :prop-active-selections="activeSelections" :prop-variables="variables" @pageChanged="getWidgetData" />
                <SelectorWidget v-if="propWidget.type == 'selector'" :prop-widget="propWidget" :data-to-show="widgetData" :widget-initial-data="widgetData" :editor-mode="true" :prop-active-selections="activeSelections" :datasets="datasets" :selection-is-locked="false" :dashboard-id="dashboardId" />
                <ActiveSelectionsWidget v-if="propWidget.type == 'selection'" :prop-widget="propWidget" :prop-active-selections="activeSelections" :editor-mode="true" :dashboard-id="dashboardId" />
                <WebComponentContainer
                    v-if="(propWidget.type == 'html' || propWidget.type == 'text') && !loading"
                    :prop-widget="propWidget"
                    :widget-data="widgetData"
                    :prop-active-selections="activeSelections"
                    :editor-mode="true"
                    :dashboard-id="dashboardId"
                    :variables="variables"
                ></WebComponentContainer>
                <HighchartsContainer v-if="propWidget.type === 'highcharts' && !loading && isEnterprise" :widget-model="propWidget" :data-to-show="widgetData" :prop-active-selections="activeSelections" :editor-mode="true" :dashboard-id="dashboardId"></HighchartsContainer>
                <ChartJSContainer v-if="propWidget.type === 'chartJS' && !loading" :widget-model="propWidget" :data-to-show="widgetData" :editor-mode="true" :dashboard-id="dashboardId" :prop-active-selections="activeSelections"></ChartJSContainer>
                <ImageWidget v-if="propWidget.type === 'image'" :widget-model="propWidget" :dashboard-id="dashboardId" :editor-mode="true" />
                <CustomChartWidget
                    v-if="propWidget.type == 'customchart' && !loading"
                    :prop-widget="propWidget"
                    :widget-data="widgetData"
                    :prop-active-selections="activeSelections"
                    :editor-mode="true"
                    :dashboard-id="dashboardId"
                    :variables="variables"
                    @loading="customChartLoading = $event"
                ></CustomChartWidget>
                <DiscoveryWidget v-if="propWidget.type == 'discovery'" :propWidget="propWidget" :datasets="datasets" :dataToShow="widgetData" :editorMode="true" :dashboardId="dashboardId" :propActiveSelections="activeSelections" @pageChanged="getWidgetData" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDataset, ISelection, IVariable, IWidget } from '../../Dashboard'
import { getWidgetStyleByType } from '../TableWidget/TableWidgetHelper'
import { emitter } from '../../DashboardHelpers'
import { getWidgetData } from '../../DataProxyHelper'
import { mapState, mapActions } from 'pinia'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import ProgressBar from 'primevue/progressbar'
import store from '../../Dashboard.store'
import mainStore from '@/App.store'
import deepcopy from 'deepcopy'
import TableWidget from '../TableWidget/TableWidget.vue'
import SelectorWidget from '../SelectorWidget/SelectorWidget.vue'
import ActiveSelectionsWidget from '../ActiveSelectionsWidget/ActiveSelectionsWidget.vue'
import WebComponentContainer from '../WebComponent/WebComponentContainer.vue'
import HighchartsContainer from '../ChartWidget/Highcharts/HighchartsContainer.vue'
import ChartJSContainer from '../ChartWidget/ChartJS/ChartJSContainer.vue'
import ImageWidget from '../ImageWidget/ImageWidget.vue'
import CustomChartWidget from '../CustomChartWidget/CustomChartWidget.vue'
import DiscoveryWidget from '../DiscoveryWidget/DiscoveryWidget.vue'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget, ProgressBar, WebComponentContainer, HighchartsContainer, ChartJSContainer, ImageWidget, CustomChartWidget, DiscoveryWidget },
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
            widgetData: {} as any,
            loading: false,
            activeSelections: [] as ISelection[],
            htmlContent: '',
            webComponentCss: '',
            textModel: '',
            customChartLoading: false
        }
    },
    computed: {
        ...mapState(mainStore, {
            isEnterprise: 'isEnterprise'
        }),
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
            this.widgetData = await getWidgetData(this.dashboardId, this.propWidget, this.datasets, this.$http, false, this.activeSelections)
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
        }
    }
})
</script>
<style lang="scss" scoped>
.widget-editor-preview-container {
    flex: 10000;
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
</style>
