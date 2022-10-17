<template>
    <grid-item :key="item.id" :x="item.x" :y="item.y" :w="item.w" :h="item.h" :i="item.i" drag-allow-from=".drag-handle">
        <div v-if="initialized" class="drag-handle"></div>
        <ProgressBar mode="indeterminate" v-if="loading" />
        <Skeleton shape="rectangle" v-if="!initialized" height="100%" border-radius="0" />

        {{ 'TODO - REMOVE THIS' }}
        {{ selectionIsLocked }}
        <br />
        <button @click="unlockSelection">UNLOCK SELECTION</button>
        <!-- <button @click="test">CLICK ME FOR TEST</button> -->
        <WidgetRenderer :widget="widget" :widgetData="widgetData" :datasets="datasets" v-if="initialized" :dashboardId="dashboardId" :selectionIsLocked="selectionIsLocked" @interaction="manageInteraction"></WidgetRenderer>
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
import { AxiosResponse } from 'axios'
import { getAssociativeSelections, removeSelectionFromActiveSelections } from './interactionsHelpers/InteractionHelper'
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
    mounted() {
        this.setEventListeners()
        this.getWidgetData()
        this.loadActiveSelections()
    },
    computed: {
        ...mapState(store, ['dashboards']),
        selectionIsLocked(): boolean {
            return this.checkIfSelectionIsLocked()
        }
    },
    methods: {
        // TODO
        ...mapActions(store, ['getDashboard', 'getSelections']),
        loadActiveSelections() {
            this.activeSelections = this.getSelections(this.dashboardId)
        },
        async test() {
            const dashboardModel = this.getDashboard(this.dashboardId)
            const response = await getAssociativeSelections(dashboardModel, this.datasets, this.$http, this.dashboards)
            console.log('>>>>> RESPONSE: ', response)
        },
        setEventListeners() {
            emitter.on('interaction', async (event) => {
                /**
                 * ! this is just an example of a possible interaction.
                 * TODO: after getting the informations related to what the needed data will be, the dataProxyHelper should take care of getting the updated data.
                 */

                this.loading = true
                this.loading = false
            })
        },
        async initializeWidget() {
            this.initialized = true
            this.loading = false
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
        formatModelForGet(propWidget: IWidget, datasetLabel) {
            //TODO: strong type this, and create a default object
            var dataToSend = {
                aggregations: {
                    measures: [],
                    categories: [],
                    dataset: ''
                },
                parameters: {},
                selections: {},
                indexes: []
            } as any

            dataToSend.aggregations.dataset = datasetLabel

            propWidget.columns.forEach((column) => {
                if (column.fieldType === 'MEASURE') {
                    let measureToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, funct: column.aggregation, orderColumn: column.alias } as any
                    column.formula ? (measureToPush.formula = column.formula) : ''
                    dataToSend.aggregations.measures.push(measureToPush)
                } else {
                    let attributeToPush = { id: column.alias, alias: column.alias, columnName: column.columnName, orderType: '', funct: 'NONE' } as any
                    column.id === propWidget.settings.sortingColumn ? (attributeToPush.orderType = propWidget.settings.sortingOrder) : ''
                    dataToSend.aggregations.categories.push(attributeToPush)
                }
            })

            return dataToSend
        },
        async getWidgetData() {
            let datasetIndex = this.datasets.findIndex((dataset: any) => this.widget.dataset === dataset.id.dsId)
            this.selectedDataset = this.datasets[datasetIndex]

            if (this.selectedDataset) {
                // let url = createGetUrl(this.widget, this.selectedDataset.label)
                var url = `2.0/datasets/${this.selectedDataset.label}/data?offset=-1&size=-1&nearRealtime=true`

                let postData = this.formatModelForGet(this.widget, this.selectedDataset.label)

                await this.$http
                    .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
                    .then((response: AxiosResponse<any>) => {
                        console.log('WIDGET DATA FROM CONTROLLER ---------------------', response.data)
                        this.widgetData = response.data
                    })
                    .catch(() => {})
            }
        },
        checkIfSelectionIsLocked() {
            if (this.widget.type !== 'selector') return false
            const index = this.activeSelections.findIndex((selection: ISelection) => selection.datasetId === this.widget.dataset && selection.columnName === this.widget.columns[0].columnName)
            return index !== -1
        },
        unlockSelection() {
            const payload = { datasetId: this.widget.dataset as number, columnName: this.widget.columns[0].columnName }
            removeSelectionFromActiveSelections(payload, this.activeSelections, this.dashboardId, this.setSelections)
        }
    },
    updated() {
        if (!this.initialized && this.activeSheet) {
            this.$nextTick()
            this.initializeWidget()
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
