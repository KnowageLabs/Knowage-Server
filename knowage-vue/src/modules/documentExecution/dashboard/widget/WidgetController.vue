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
            @sortingChanged="reloadWidgetData"
            @launchSelection="launchSelection"
            @mouseover="toggleFocus"
            @mouseleave="startUnfocusTimer(500)"
        ></WidgetRenderer>
        <WidgetButtonBar
            :widget="widget"
            :playSelectionButtonVisible="playSelectionButtonVisible"
            :selectionIsLocked="selectionIsLocked"
            :dashboardId="dashboardId"
            :inFocus="inFocus"
            @edit-widget="toggleEditMode"
            @unlockSelection="unlockSelection"
            @launchSelection="launchSelection"
            @changeFocus="changeFocus"
        ></WidgetButtonBar>
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
import { ISelectorWidgetSettings } from '../interfaces/DashboardSelectorWidget'
import { datasetIsUsedInAssociations } from './interactionsHelpers/DatasetAssociationsHelper'
import { loadAssociativeSelections } from './interactionsHelpers/InteractionHelper'

export default defineComponent({
    name: 'widget-manager',
    components: { Skeleton, WidgetButtonBar, WidgetRenderer, ProgressSpinner },
    inject: ['dHash'],
    props: {
        item: { required: true, type: Object },
        activeSheet: { type: Boolean },
        widget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    watch: {
        widget: {
            async handler() {
                this.loadWidget(this.widget)
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
            },
            inFocus: false,
            selectionIsLocked: false,
            playDisabledButtonTimeout: null as any
        }
    },
    async created() {
        this.setEventListeners()
        this.loadWidget(this.widget)
        this.widget.type !== 'selection' ? this.loadInitalData() : this.loadActiveSelections()
    },
    unmounted() {
        this.removeEventListeners()
    },
    computed: {
        ...mapState(store, ['dashboards']),
        playSelectionButtonVisible(): boolean {
            if (!this.widget || !this.widget.settings.configuration.selectorType) return false
            return this.widget.type === 'selector' && ['multiValue', 'multiDropdown', 'dateRange'].includes(this.widget.settings.configuration.selectorType.modality) && !this.selectionIsLocked
        }
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections', 'setSelections', 'removeSelection']),
        setEventListeners() {
            emitter.on('selectionsChanged', this.loadActiveSelections)
            emitter.on('selectionsDeleted', this.onSelectionsDeleted)
            emitter.on('widgetUpdatedFromStore', this.onWidgetUpdated)
            emitter.on('associativeSelectionsLoaded', this.onAssociativeSelectionsLoaded)
            emitter.on('datasetRefreshed', this.onDatasetRefresh)
            emitter.on('setWidgetLoading', this.setWidgetLoading)
        },
        removeEventListeners() {
            emitter.off('selectionsChanged', this.loadActiveSelections)
            emitter.off('selectionsDeleted', this.onSelectionsDeleted)
            emitter.off('widgetUpdatedFromStore', this.onWidgetUpdated)
            emitter.off('associativeSelectionsLoaded', this.onAssociativeSelectionsLoaded)
            emitter.off('datasetRefreshed', this.onDatasetRefresh)
            emitter.off('setWidgetLoading', this.setWidgetLoading)
        },
        loadWidget(widget: IWidget) {
            this.widgetModel = widget
        },
        setWidgetLoading(loading: any) {
            this.loading = loading
        },
        onWidgetUpdated(widget: any) {
            if (this.widget.id !== widget.id) return
            this.loadWidget(widget)
            this.loadInitalData()
        },
        async loadInitalData() {
            if (!this.widgetModel || this.widgetModel.type === 'selection') return

            this.widgetInitialData = await getWidgetData(this.widgetModel, this.datasets, this.$http, true, this.activeSelections)
            this.widgetData = this.widgetInitialData
            await this.loadActiveSelections()
        },
        async loadActiveSelections() {
            this.getSelectionsFromStore()
            if (this.widgetModel.type === 'selection') return
            if (this.widgetUsesSelections(this.activeSelections)) await this.reloadWidgetData(null)
        },
        getSelectionsFromStore() {
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            this.checkIfSelectionIsLocked()
        },
        async onSelectionsDeleted(deletedSelections: any) {
            const associations = this.dashboards[this.dashboardId]?.configuration.associations ?? []
            this.getSelectionsFromStore()
            if (this.widgetUsesSelections(deletedSelections) || (this.widget.dataset && datasetIsUsedInAssociations(this.widget.dataset, associations))) this.reloadWidgetData(null)
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
            console.log('CAAAAAAAAAAAAAAAAAAAAAAAALED')
            this.loading = true
            this.widgetData = await getWidgetData(this.widgetModel, this.datasets, this.$http, false, this.activeSelections, associativeResponseSelections)
            this.loading = false
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

            return widgetUsesSelection
        },
        toggleEditMode() {
            emitter.emit('openWidgetEditor', this.widget)
        },
        checkIfSelectionIsLocked() {
            if (this.widgetModel.type !== 'selector' || (this.widgetModel.settings as ISelectorWidgetSettings).configuration.valuesManagement.enableAll) return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.widgetModel.dataset && selection.columnName === this.widgetModel.columns[0].columnName)
            this.selectionIsLocked = index !== -1
        },
        unlockSelection() {
            const payload = {
                datasetId: this.widgetModel.dataset as number,
                columnName: this.widgetModel.columns[0].columnName
            }
            emitter.emit('widgetUnlocked', this.widgetModel.id)
            this.removeSelection(payload, this.dashboardId)
        },
        launchSelection() {
            this.setSelections(this.dashboardId, this.activeSelections, this.$http)
        },
        async onAssociativeSelectionsLoaded(response: any) {
            this.getSelectionsFromStore()
            if (!response) return
            const datasets = Object.keys(response)
            const dataset = this.datasets.find((dataset: IDataset) => dataset.id.dsId === this.widgetModel.dataset)
            const index = datasets.findIndex((datasetLabel: string) => datasetLabel === dataset?.label)
            if (index !== -1) await this.reloadWidgetData(response)
        },
        async onDatasetRefresh(modelDatasetId: any) {
            if (this.widgetModel.dataset !== modelDatasetId) return
            if (this.activeSelections.length > 0 && datasetIsUsedInAssociations(modelDatasetId, this.dashboards[this.dashboardId].configuration.associations)) {
                loadAssociativeSelections(this.dashboards[this.dashboardId], this.datasets, this.activeSelections, this.$http)
            } else {
                await this.reloadWidgetData(null)
            }
        },
        startUnfocusTimer(milliseconds: number) {
            this.playDisabledButtonTimeout = setTimeout(() => {
                this.inFocus = false
            }, milliseconds)
        },
        toggleFocus() {
            clearTimeout(this.playDisabledButtonTimeout)
            this.inFocus = true
        },
        changeFocus(value: boolean) {
            clearTimeout(this.playDisabledButtonTimeout)
            if (value) {
                this.inFocus = true
                this.startUnfocusTimer(3000)
            } else {
                this.inFocus = false
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
