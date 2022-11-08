<template>
    <div class="widget-editor-preview-container p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow">
        <!-- <Button icon="fas fa-square-check" class="p-button-rounded p-button-text p-button-plain" @click="logWidget" /> -->
        <ProgressBar v-if="loading" class="p-mx-2" mode="indeterminate" />
        <div class="widget-container p-mx-2" :style="getWidgetContainerStyle()">
            <div v-if="widgetTitle && widgetTitle.enabled" class="p-d-flex p-ai-center" style="border-radius: 0px" :style="getWidgetTitleStyle()">
                {{ widgetTitle?.text }}
            </div>
            <Button v-if="propWidget.type == 'html'" icon="fas fa-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" v-tooltip.left="$t('common.todo')" @click="test">TODO</Button>
            <div class="widget-container-renderer" :style="getWidgetPadding()">
                <TableWidget v-if="propWidget.type == 'table'" :propWidget="propWidget" :datasets="datasets" :dataToShow="widgetData" :editorMode="true" @pageChanged="getWidgetData" />
                <SelectorWidget v-if="propWidget.type == 'selector'" :propWidget="propWidget" :dataToShow="widgetData" :widgetInitialData="widgetData" :editorMode="true" />
                <ActiveSelectionsWidget v-if="propWidget.type == 'selection'" :propWidget="propWidget" :propActiveSelections="activeSelections" :editorMode="true" :dashboardId="dashboardId" />
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
import { parseHtml } from './helpers/htmlParser/ParserHelper'

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
            activeSelections: [] as ISelection[]
        }
    },
    computed: {
        ...mapState(store, ['dashboards'])
    },
    created() {
        this.setEventListeners()
        this.getWidgetTitleStyle()
    },
    mounted() {
        this.getWidgetData()
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
        test() {
            // TODO
            parseHtml(this.propWidget, this.drivers, this.variables, this.getSelections(this.dashboardId))
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
