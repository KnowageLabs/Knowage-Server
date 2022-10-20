<template>
    <grid-item class="p-d-flex" :key="item.id" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressSpinner v-if="loading" class="kn-progress-spinner" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />

        <WidgetRenderer
            :widget="widget"
            :widgetData="widgetData"
            :widgetInitialData="widgetInitialData"
            :datasets="datasets"
            v-if="initialized"
            :dashboardId="dashboardId"
            :selectionIsLocked="selectionIsLocked"
            :propActiveSelections="activeSelections"
            @pageChanged="reloadWidgetData"
        ></WidgetRenderer>
        <WidgetButtonBar :playSelectionButtonVisible="playSelectionButtonVisible" :selectionIsLocked="selectionIsLocked" @edit-widget="toggleEditMode" @unlockSelection="unlockSelection" @launchSelection="launchSelection"></WidgetButtonBar>
    </grid-item>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IWidget } from '../Dashboard'
import { emitter } from '../DashboardHelpers'
import { mapState, mapActions } from 'pinia'
import { getWidgetData } from '../DataProxyHelper'
import store from '../Dashboard.store'
import WidgetRenderer from './WidgetRenderer.vue'
import WidgetButtonBar from './WidgetButtonBar.vue'
import Skeleton from 'primevue/skeleton'
import ProgressSpinner from 'primevue/progressspinner'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'widget-manager',
    components: { Skeleton, WidgetButtonBar, WidgetRenderer, ProgressSpinner },
    inject: ['dHash'],
    props: { item: { required: true, type: Object }, activeSheet: { type: Boolean }, widget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]>, required: true }, dashboardId: { type: String, required: true } },
    watch: {
        widget: {
            async handler() {
                this.loading = true
                // console.log('%c --  CALLED FROM WIDGET CONTROLLER watcher!!!!', 'background-color: blue; color: white', this.widget.type)
                // this.loadWidget(this.widget)
                // this.widgetData = await getWidgetData(this.widget, this.datasets, this.$http, false, this.activeSelections)
                this.loading = false
            },
            deep: true
        }
    },
    data() {
        return {
            loading: false,
            initialized: true,
            widgetModel: null as any,
            widgetInitialData: {} as any,
            widgetData: {} as any,
            selectedWidgetId: '' as string,
            selectedDataset: {} as any,
            widgetEditorVisible: false,
            activeSelections: [] as ISelection[],
            pagination: {
                offset: 0,
                itemsNumber: 15,
                totalItems: 0
            }
        }
    },
    async created() {
        this.setEventListeners()
        this.loadWidget(this.widget)
        this.loadInitalData()
    },
    unmounted() {
        this.removeEventListeners()
    },
    computed: {
        ...mapState(store, ['dashboards']),
        selectionIsLocked(): boolean {
            return this.checkIfSelectionIsLocked()
        },
        playSelectionButtonVisible(): boolean {
            if (!this.widget || !this.widget.settings.configuration.selectorType) return false
            return this.widget.type === 'selector' && ['multiValue', 'multiDropdown', 'dateRange'].includes(this.widget.settings.configuration.selectorType.modality)
        }
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections', 'setSelections', 'removeSelection']),
        setEventListeners() {
            emitter.on('selectionsChanged', this.loadActiveSelections)
            emitter.on('selectionsDeleted', this.onSelectionsDeleted)
            emitter.on('widgetUpdatedFromStore', this.onWidgetUpdated)
            emitter.on('associativeSelectionsLoaded', this.onAssociativeSelectionsLoaded)
        },
        removeEventListeners() {
            emitter.off('selectionsChanged', this.loadActiveSelections)
            emitter.off('selectionsDeleted', this.onSelectionsDeleted)
            emitter.off('widgetUpdatedFromStore', this.onWidgetUpdated)
            emitter.off('associativeSelectionsLoaded', this.onAssociativeSelectionsLoaded)
        },
        loadWidget(widget: IWidget) {
            this.widgetModel = widget
        },
        onWidgetUpdated(widget: any) {
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER PROP!!!!', 'background-color: blue; color: white', this.widget.id !== widget.id)
            if (this.widget.id !== widget.id) return
            this.loadWidget(widget)
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER onWidgetUpdated!!!!', 'background-color: blue; color: white', this.widgetModel)
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER onWidgetUpdated!!!!', 'background-color: blue; color: white', widget)
            this.loadInitalData()
        },
        async loadInitalData() {
            if (!this.widgetModel || this.widgetModel.type === 'selection') return
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER loadInitalData!!!!', 'background-color: blue; color: white', this.widgetModel)
            this.loading = true

            this.widgetInitialData = await getWidgetData(this.widgetModel, this.datasets, this.$http, true, this.activeSelections)
            this.widgetData = this.widgetInitialData

            await this.loadActiveSelections()
            this.loading = false
        },
        async loadActiveSelections() {
            // console.log('%c --  loadActiveSelections', 'background-color: blue; color: white', this.widget.type)
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            // console.log('%c --  loadActiveSelections', 'background-color: blue; color: white', this.activeSelections)
            await this.reloadWidgetData(null)
        },
        async onSelectionsDeleted(deletedSelections: any) {
            this.loading = true
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER onSelectionsDeleted!!!!', 'background-color: blue; color: white', this.widgetModel)
            if (this.widgetUsesSelections(deletedSelections)) this.widgetData = await getWidgetData(this.widgetModel, this.datasets, this.$http, false, this.activeSelections)

            this.loading = false
        },
        widgetUsesDeletedSelectionsDataset(deletedSelections: ISelection[]) {
            let widgetUsesSelection = false
            if (!this.widgetModel.dataset) return widgetUsesSelection
            for (let i = 0; i < deletedSelections.length; i++) {
                if (deletedSelections[i].datasetId === this.widgetModel.dataset) {
                    widgetUsesSelection = true
                    break
                }
            }
            return widgetUsesSelection
        },
        async reloadWidgetData(associativeResponseSelections: any) {
            // console.log('%c --  CALLED FROM WIDGET CONTROLLER reloadWidgetData!!!!', 'background-color: blue; color: black', this.widgetModel)
            if (this.widgetUsesSelections(this.activeSelections) || associativeResponseSelections) this.widgetData = await getWidgetData(this.widgetModel, this.datasets, this.$http, false, this.activeSelections, associativeResponseSelections)
        },
        widgetUsesSelections(selections: ISelection[]) {
            let widgetUsesSelection = false
            if (!this.widgetModel.dataset) return widgetUsesSelection
            for (let i = 0; i < selections.length; i++) {
                if (selections[i].datasetId === this.widgetModel.dataset) {
                    widgetUsesSelection = true
                    break
                }
            }
            // console.log('>>>>>>>>>>>>>>>>>>>>> widgetUsesSelections: ', widgetUsesSelection)

            return widgetUsesSelection
        },
        toggleEditMode() {
            emitter.emit('openWidgetEditor', this.widget)
        },
        checkIfSelectionIsLocked() {
            if (this.widgetModel.type !== 'selector') return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.widgetModel.dataset && selection.columnName === this.widgetModel.columns[0].columnName)
            return index !== -1
        },
        unlockSelection() {
            const payload = { datasetId: this.widgetModel.dataset as number, columnName: this.widgetModel.columns[0].columnName }
            emitter.emit('widgetUnlocked', this.widgetModel.id)
            this.removeSelection(payload, this.dashboardId)
        },
        launchSelection() {
            this.setSelections(this.dashboardId, this.activeSelections, this.$http)
        },
        async onAssociativeSelectionsLoaded(response: any) {
            console.log('>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> onAssociativeSelectionsLoaded onAssociativeSelectionsLoaded: ', response)
            if (!response) return
            const datasets = Object.keys(response)
            const dataset = this.datasets.find((dataset: IDataset) => dataset.id.dsId === this.widgetModel.dataset)
            const index = datasets.findIndex((datasetLabel: string) => datasetLabel === dataset?.label)
            console.log('>>>>>>>>>>>>>> INDEX: ', index)
            if (index !== -1) {
                this.loading = true
                console.log('>>>>>>>>>>>>>> EEEEEEEEEEEEEEEEEEEEEEEEEEENTEERD: ', index)
                await this.reloadWidgetData(response)
                this.loading = false
            }
        }
    }
})
</script>
<style lang="scss">
.editorEnter-enter-active,
.editorEnter-leave-active {
    transition: opacity 0.5s ease;
}

.editorEnter-enter-from,
.editorEnter-leave-to {
    opacity: 0;
}

.vue-resizable-handle {
    z-index: 9999;
}
</style>
