<template>
    <grid-item class="p-d-flex" :key="item.id" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressSpinner v-if="loading" class="kn-progress-spinner" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />
        <WidgetRenderer :widget="widget" :widgetData="widgetData" :datasets="datasets" v-if="initialized" :dashboardId="dashboardId" :selectionIsLocked="selectionIsLocked" :propActiveSelections="activeSelections" @interaction="manageInteraction"></WidgetRenderer>
        <WidgetButtonBar :playSelectionButtonVisible="playSelectionButtonVisible" @edit-widget="toggleEditMode" @unlockSelection="unlockSelection" @launchSelection="launchSelection"></WidgetButtonBar>
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
import { getSelectorWidgetData } from '../DataProxyHelper'
import { getAssociativeSelections, removeSelectionFromActiveSelections, updateStoreSelections } from './interactionsHelpers/InteractionHelper'
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
                this.widgetData = await getSelectorWidgetData(this.widget, this.datasets, this.$http, false, this.activeSelections)
                this.loading = false
            },
            deep: true
        }
    },
    data() {
        return {
            loading: false,
            initialized: true,
            widgetData: {} as any,
            selectedWidgetId: '' as string,
            selectedDataset: {} as any,
            widgetEditorVisible: false,
            activeSelections: [] as ISelection[]
        }
    },
    async mounted() {
        this.setEventListeners()
        this.loadActiveSelections()
        this.widgetData = await getSelectorWidgetData(this.widget, this.datasets, this.$http, true, this.activeSelections)
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
        ...mapActions(store, ['getDashboard', 'getSelections', 'setSelections']),
        setEventListeners() {
            emitter.on('selectionsChanged', this.loadActiveSelections)
        },
        removeEventListeners() {
            emitter.off('selectionsChanged', this.loadActiveSelections)
        },
        async loadActiveSelections() {
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            await this.reloadWidgetData()
        },
        async reloadWidgetData() {
            if (this.widgetUsesSelections()) this.widgetData = await getSelectorWidgetData(this.widget, this.datasets, this.$http, false, this.activeSelections)
        },
        widgetUsesSelections() {
            let widgetUsesSelection = false
            if (!this.widget.columns) return widgetUsesSelection
            for (let i = 0; i < this.widget.columns.length; i++) {
                const index = this.activeSelections.findIndex((activeSelection: ISelection) => activeSelection.datasetId === this.widget.dataset && activeSelection.columnName === this.widget.columns[i].columnName)
                if (index !== -1) {
                    widgetUsesSelection = true
                    break
                }
            }
            console.log('CAAAAAAAAAAAAAAAAAAAAAAAAALED: ', widgetUsesSelection)

            return widgetUsesSelection
        },
        // TODO
        async test() {
            const dashboardModel = this.getDashboard(this.dashboardId)
            const response = await getAssociativeSelections(dashboardModel, this.datasets, this.$http, this.dashboards)
            console.log('>>>>> RESPONSE: ', response)
        },
        manageInteraction(e, item) {
            console.log('interaction', e, item)
            /**
             * TODO: The interaction manager will find in the widget model the interaction type, and provide the corrent event to be emitted with needed data
             */

            // @ts-ignore
            emitter.emit('interaction', { id: this.dHash, event: e })
        },
        toggleEditMode() {
            emitter.emit('openWidgetEditor', this.widget)
        },
        checkIfSelectionIsLocked() {
            if (this.widget.type !== 'selector') return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.widget.dataset && selection.columnName === this.widget.columns[0].columnName)
            return index !== -1
        },
        unlockSelection() {
            const payload = { datasetId: this.widget.dataset as number, columnName: this.widget.columns[0].columnName }
            removeSelectionFromActiveSelections(payload, this.activeSelections, this.dashboardId, this.setSelections)
        },
        launchSelection() {
            this.setSelections(this.dashboardId, this.activeSelections)
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
