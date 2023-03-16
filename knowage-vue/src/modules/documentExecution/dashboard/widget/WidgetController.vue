<template>
    <grid-item :key="item.id" class="p-d-flex widget-grid-item" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle" @resized="resizedEvent" :class="{ canEdit: canEditDashboard(document) }">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressSpinner v-if="loading || customChartLoading" class="kn-progress-spinner" />
        <Skeleton v-if="!initialized" shape="rectangle" height="100%" border-radius="0" />
        <WidgetRenderer
            v-if="!loading"
            :widget="widget"
            :widget-data="widgetData"
            :widget-initial-data="widgetInitialData"
            :datasets="datasets"
            :dashboard-id="dashboardId"
            :selection-is-locked="selectionIsLocked"
            :prop-active-selections="activeSelections"
            :variables="variables"
            :widget-loading="widgetLoading"
            @reloadData="reloadWidgetData"
            @launchSelection="launchSelection"
            @mouseover="toggleFocus"
            @mouseleave="startUnfocusTimer(500)"
            @loading="customChartLoading = $event"
            @contextmenu="onWidgetRightClick"
        ></WidgetRenderer>
        <WidgetButtonBar
            :widget="widget"
            :play-selection-button-visible="playSelectionButtonVisible"
            :selection-is-locked="selectionIsLocked"
            :dashboard-id="dashboardId"
            :in-focus="inFocus"
            :menu-items="items"
            @edit-widget="toggleEditMode"
            @unlockSelection="unlockSelection"
            @launchSelection="launchSelection"
            @changeFocus="changeFocus"
        ></WidgetButtonBar>
        <ContextMenu v-if="canEditDashboard(document)" ref="contextMenu" :model="items" />
    </grid-item>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent, PropType } from 'vue'
import { IDataset, ISelection, IVariable, IWidget } from '../Dashboard'
import { emitter, canEditDashboard } from '../DashboardHelpers'
import { mapState, mapActions } from 'pinia'
import { getWidgetData } from '../DataProxyHelper'
import store from '../Dashboard.store'
import mainStore from '@/App.store'
import WidgetRenderer from './WidgetRenderer.vue'
import WidgetButtonBar from './WidgetButtonBar.vue'
import Skeleton from 'primevue/skeleton'
import ProgressSpinner from 'primevue/progressspinner'
import deepcopy from 'deepcopy'
import { ISelectorWidgetSettings } from '../interfaces/DashboardSelectorWidget'
import { datasetIsUsedInAssociations } from './interactionsHelpers/DatasetAssociationsHelper'
import { loadAssociativeSelections } from './interactionsHelpers/InteractionHelper'
import ContextMenu from 'primevue/contextmenu'

export default defineComponent({
    name: 'widget-manager',
    components: { ContextMenu, Skeleton, WidgetButtonBar, WidgetRenderer, ProgressSpinner },
    inject: ['dHash'],
    props: {
        model: { type: Object },
        item: { required: true, type: Object },
        activeSheet: { type: Boolean },
        document: { type: Object },
        widget: { type: Object as PropType<IWidget>, required: true },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dashboardId: { type: String, required: true },
        variables: { type: Array as PropType<IVariable[]>, required: true }
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
            playDisabledButtonTimeout: null as any,
            widgetLoading: false,
            customChartLoading: false,
            canEditDashboard,
            items: [
                {
                    label: 'Edit Widget',
                    icon: 'fa-solid fa-pen-to-square',
                    command: () => this.toggleEditMode()
                },
                {
                    label: 'Delete Widget',
                    icon: 'fa-solid fa-trash',
                    command: () => this.deleteWidget(this.dashboardId, this.widget)
                }
            ]
        }
    },
    watch: {
        widget: {
            async handler() {
                this.loadWidget(this.widget)
            },
            deep: true
        }
    },
    async created() {
        this.setWidgetLoading(true)

        this.setEventListeners()
        this.loadWidget(this.widget)
        this.widget.type !== 'selection' ? await this.loadInitalData() : await this.loadActiveSelections()

        this.setWidgetLoading(false)
    },
    unmounted() {
        this.removeEventListeners()
    },
    computed: {
        ...mapState(store, ['dashboards']),
        ...mapState(mainStore, ['user']),
        playSelectionButtonVisible(): boolean {
            if (!this.widget || !this.widget.settings.configuration || !this.widget.settings.configuration.selectorType) return false
            return this.widget.type === 'selector' && ['multiValue', 'multiDropdown', 'dateRange'].includes(this.widget.settings.configuration.selectorType.modality) && !this.selectionIsLocked
        }
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections', 'setSelections', 'removeSelection', 'deleteWidget']),
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
        onWidgetRightClick(event) {
            const contextMenu = this.$refs.contextMenu as any
            contextMenu?.show(event)
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

            this.setWidgetLoading(true)

            this.widgetInitialData = await getWidgetData(this.dashboardId, this.widgetModel, this.model?.configuration?.datasets, this.$http, true, this.activeSelections)
            this.widgetData = this.widgetInitialData
            await this.loadActiveSelections()

            this.setWidgetLoading(false)
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
            this.widgetLoading = true
            this.widgetData = await getWidgetData(this.dashboardId, this.widgetModel, this.model?.configuration?.datasets, this.$http, false, this.activeSelections, associativeResponseSelections)
            this.widgetLoading = false
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
        },
        resizedEvent: function (newHPx) {
            emitter.emit('widgetResized', newHPx)
        }
    }
})
</script>
<style lang="scss">
.widget-grid-item {
    &.vue-grid-item > .vue-resizable-handle {
        display: none;
    }
    &:hover {
        &.vue-grid-item > .vue-resizable-handle {
            display: block;
        }
        &.canEdit {
            outline: 1px solid var(--kn-color-borders);
            .drag-widget-icon {
                display: block;
            }
        }
        .widgetButtonBarContainer {
            display: block;
        }
    }
}

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
