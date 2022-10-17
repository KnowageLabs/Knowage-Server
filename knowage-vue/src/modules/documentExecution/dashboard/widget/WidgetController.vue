<template>
    <grid-item :key="item.id" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressBar mode="indeterminate" v-if="loading" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />

        {{ 'TODO - REMOVE THIS' }}
        {{ selectionIsLocked }}
        <br />
        <button class="p-mr-2" :style="'max-width: 100px;'" @click="unlockSelection">UNLOCK SELECTION</button>
        <button v-if="playSelectionButtonVisible" :style="'max-width: 100px;'" @click="launchSelection">LAUNCH SELECTION</button>
        <!-- <button @click="test">CLICK ME FOR TEST</button> -->
        <WidgetRenderer :widget="widget" :widgetData="widgetData" :datasets="datasets" v-if="initialized" :dashboardId="dashboardId" :selectionIsLocked="selectionIsLocked" :activeSelections="activeSelections" @interaction="manageInteraction"></WidgetRenderer>
        <WidgetButtonBar @edit-widget="toggleEditMode"></WidgetButtonBar>
    </grid-item>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget behaviour related to data and interactions, not related to view elements.
 */
import { defineComponent, PropType } from 'vue'
import { getData } from '../DataProxyHelper'
import { IDataset, ISelection, IWidget } from '../Dashboard'
import { emitter } from '../DashboardHelpers'
import { mapState, mapActions } from 'pinia'
import { getSelectorWidgetData } from '../DataProxyHelper'
import { getAssociativeSelections, removeSelectionFromActiveSelections, updateStoreSelections } from './interactionsHelpers/InteractionHelper'
import store from '../Dashboard.store'
import WidgetRenderer from './WidgetRenderer.vue'
import WidgetButtonBar from './WidgetButtonBar.vue'
import Skeleton from 'primevue/skeleton'
import ProgressBar from 'primevue/progressbar'

export default defineComponent({
    name: 'widget-manager',
    components: { ProgressBar, Skeleton, WidgetButtonBar, WidgetRenderer },
    inject: ['dHash'],
    props: { item: { required: true, type: Object }, activeSheet: { type: Boolean }, widget: { type: Object as PropType<IWidget>, required: true }, datasets: { type: Array as PropType<IDataset[]>, required: true }, dashboardId: { type: String, required: true } },
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
        this.widgetData = await getSelectorWidgetData(this.widget, this.datasets, this.$http)
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
            console.log('TEEEEEEST: ', this.widget.settings.configuration.selectorType.modality)
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
        loadActiveSelections() {
            this.activeSelections = this.getSelections(this.dashboardId)
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
