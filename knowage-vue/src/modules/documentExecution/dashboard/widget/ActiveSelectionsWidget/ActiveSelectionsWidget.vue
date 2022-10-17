<template>
    <div class="active-selections-widget p-d-flex p-flex-column kn-flex kn-overflow-y">
        <div v-if="widgetType === 'list' && activeSelections?.length > 0" class="p-d-flex p-flex-row p-flex-wrap kn-flex">
            list
            <ActiveSelectionsList :activeSelections="activeSelections" :propWidget="propWidget" :showDataset="showDataset" :showColumn="showColumn" @deleteSelection="onDeleteSelection" />
        </div>

        <div v-if="widgetType === 'chips' && activeSelections?.length > 0" class="p-d-flex p-flex-row p-flex-wrap">
            chip
            <ActiveSelectionsChips v-for="(activeSelection, index) of activeSelections" :key="index" :activeSelection="activeSelection" :showDataset="showDataset" :showColumn="showColumn" :style="getChipsStyle()" @deleteSelection="onDeleteSelection" />
        </div>

        <Message v-if="activeSelections?.length == 0" class="p-mx-2" severity="info" :closable="false">{{ noSelectionsMessage }}</Message>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ISelection, IWidget } from '../../Dashboard'
import { getWidgetStyleByTypeWithoutValidation } from '../TableWidget/TableWidgetHelper'
import { mapActions } from 'pinia'
import { emitter } from '../../DashboardHelpers'
import { removeSelectionFromActiveSelections } from '../dataProxyHelper/DataProxyHelper'
import ActiveSelectionsChips from './ActiveSelectionsWidgetChips.vue'
import ActiveSelectionsList from './ActiveSelectionsWidgetList.vue'
import Message from 'primevue/message'
import store from '../../Dashboard.store'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { ActiveSelectionsChips, ActiveSelectionsList, Message },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, dataToShow: { type: Array as any, required: true }, dashboardId: { type: String, required: true } },
    emits: ['close'],
    computed: {
        widgetType(): string {
            return this.propWidget.settings.configuration.type || null
        },
        showDataset(): boolean {
            return this.propWidget.settings.configuration.valuesManagement.showDataset || false
        },
        showColumn(): boolean {
            return this.propWidget.settings.configuration.valuesManagement.showColumn || false
        },
        noSelectionsMessage(): string {
            let noSelections = this.propWidget.settings.configuration.noSelections
            if (noSelections.enabled) return noSelections.customText
            else return 'TODO: PUT THIS INTO EN_US No Active Selections'
        }
    },
    data() {
        return {
            activeSelections: [] as ISelection[]
        }
    },
    setup() {},
    created() {
        this.setEventListeners()
        this.loadActiveSelections()
    },
    updated() {},
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        ...mapActions(store, ['getSelections', 'setSelections']),
        setEventListeners() {
            emitter.on('selectionsChanged', this.loadActiveSelections)
        },
        removeEventListeners() {
            emitter.off('selectionsChanged', this.loadActiveSelections)
        },
        loadActiveSelections() {
            this.activeSelections = this.getSelections(this.dashboardId)
        },
        getChipsStyle() {
            let height = this.propWidget.settings.style.chips.height
            return getWidgetStyleByTypeWithoutValidation(this.propWidget, 'chips') + `height: ${height != 0 ? height : 25}px`
        },
        onDeleteSelection(selection: ISelection) {
            const payload = { datasetId: selection.datasetId, columnName: selection.columnName }
            removeSelectionFromActiveSelections(payload, this.activeSelections, this.dashboardId, this.setSelections)
        }
    }
})
</script>
<style scoped>
::-webkit-scrollbar {
    width: 5px;
}
::-webkit-scrollbar-track {
    background: #f1f1f1;
}
::-webkit-scrollbar-thumb {
    background: #888;
}
::-webkit-scrollbar-thumb:hover {
    background: #555;
}
</style>
