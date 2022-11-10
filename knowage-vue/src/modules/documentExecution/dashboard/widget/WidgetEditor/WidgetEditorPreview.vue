<template>
    <div class="widget-editor-preview-container p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow">
        <!-- <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" /> -->
        <ProgressBar v-if="loading" class="p-mx-2" mode="indeterminate" />
        <!-- TODO - return widget-container class -->
        <div class="p-mx-2" :style="getWidgetContainerStyle()" style="'height: 500px; overflow: auto;'">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>
            <Button v-if="propWidget.type == 'html' || propWidget.type == 'text'" icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" v-tooltip.left="$t('common.todo')" @click="previewHTML">preview HTML</Button>
            <div class="widget-container-renderer" :style="getWidgetPadding()">
                <TableWidget v-if="propWidget.type == 'table'" :propWidget="propWidget" :datasets="datasets" :dataToShow="widgetData" :editorMode="true" :dashboardId="dashboardId" @pageChanged="getWidgetData" />
                <SelectorWidget v-if="propWidget.type == 'selector'" :propWidget="propWidget" :dataToShow="widgetData" :widgetInitialData="widgetData" :editorMode="true" />
                <ActiveSelectionsWidget v-if="propWidget.type == 'selection'" :propWidget="propWidget" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId" />
                <widget-web-component v-if="propWidget.type == 'html'" ref="webComponent"></widget-web-component>
                <!-- <div v-html="textModel"></div> -->
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IVariable, IWidget } from '../../Dashboard'
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
import { parseHtml, parseText } from './helpers/htmlParser/ParserHelper'
import './WidgetEditorSettingsTab/common/webComponent/WidgetWebComponent'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { TableWidget, SelectorWidget, ActiveSelectionsWidget, ProgressBar },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        drivers: { type: Array as PropType<any[]>, required: true },
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
        this.setEventListeners()
        this.getWidgetTitleStyle()
        this.loadWebComponentData()
    },
    mounted() {
        this.getWidgetData()
    },
    unmounted() {
        this.unsetEventListeners()
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections', 'getInternationalization']),
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
            console.log('getting data ------------')
            this.widgetData = await getWidgetData(this.propWidget, this.datasets, this.$http, false, this.activeSelections)
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            this.loading = false
        },
        clearWidgetData() {
            console.log('clearing data ------------')
            this.widgetData = { metaData: {}, rows: [] }
        },
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
            if (this.propWidget.type == 'table') return styleString + 'height: 30%;'
            else return styleString
        },
        getWidgetPadding() {
            const styleString = getWidgetStyleByType(this.propWidget, 'padding')
            return styleString
        },
        previewHTML() {
            let temp = {} as any
            if (this.propWidget.type === 'html') {
                temp = parseHtml(this.propWidget, this.drivers, this.variables, this.getSelections(this.dashboardId), this.getInternationalization())
                this.htmlContent = temp.html
                this.webComponentCss = temp.css
            } else {
                this.textModel = parseText(this.propWidget, this.drivers, this.variables, this.getSelections(this.dashboardId), this.getInternationalization())
            }
            this.$refs.webComponent.htmlContent = this.htmlContent
            this.$refs.webComponent.webComponentCss = this.webComponentCss

            this.$refs.webComponent.addEventListener('selectEvent', this.onSelect)
        },
        onSelect(event: any) {
            console.log('>>>>>>>>>>>>>>>>>>>>> ON SELECT CAAAALED: ', event)
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
        // flex: 1;
        max-height: 35%;
        .widget-container-renderer {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }
    }
}
@media screen and (max-width: 1199px) {
    .widget-editor-preview-container {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 0;
    }
}
@media screen and (min-width: 1200px) {
    .widget-editor-preview-container {
        -webkit-transition: width 0.3s;
        transition: flex 0.3s;
        flex: 0.5;
    }
}
</style>
