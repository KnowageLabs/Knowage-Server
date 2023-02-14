<template>
    <div v-if="activeSelections" class="active-selections-widget p-d-flex p-flex-column kn-flex kn-overflow-y dashboard-scrollbar">
        <div v-if="widgetType === 'list' && activeSelections.length > 0" class="p-d-flex p-flex-row p-flex-wrap kn-flex">
            <ActiveSelectionsList :activeSelections="activeSelections" :propWidget="propWidget" :showDataset="showDataset" :showColumn="showColumn" :editorMode="editorMode" @deleteSelection="onDeleteSelection" />
        </div>

        <div v-if="widgetType === 'chips' && activeSelections.length > 0" class="p-d-flex p-flex-row p-flex-wrap">
            <ActiveSelectionsChips v-for="(activeSelection, index) of activeSelections" :key="index" :activeSelection="activeSelection" :showDataset="showDataset" :showColumn="showColumn" :style="getChipsStyle()" :editorMode="editorMode" @deleteSelection="onDeleteSelection" />
        </div>

        <Message v-if="activeSelections.length == 0 && selectionMessageEnabled" class="p-mx-2" severity="info" :closable="false">{{ noSelectionsMessage }}</Message>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { ISelection, IWidget } from '../../Dashboard'
import { getWidgetStyleByTypeWithoutValidation } from '../TableWidget/TableWidgetHelper'
import { mapActions } from 'pinia'
import ActiveSelectionsChips from './ActiveSelectionsWidgetChips.vue'
import ActiveSelectionsList from './ActiveSelectionsWidgetList.vue'
import Message from 'primevue/message'
import store from '../../Dashboard.store'
import descriptor from '../WidgetEditor/WidgetEditorDescriptor.json'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { ActiveSelectionsChips, ActiveSelectionsList, Message },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, propActiveSelections: { type: Array as PropType<ISelection[]>, required: true }, dashboardId: { type: String, required: true }, editorMode: { type: Boolean } },
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
            else return 'No Active Selections'
        },
        selectionMessageEnabled(): boolean {
            return this.propWidget.settings.configuration.noSelections.enabled
        }
    },
    data() {
        return {
            activeSelections: [] as ISelection[]
        }
    },
    watch: {
        propActiveSelections() {
            this.loadActiveSelections()
        }
    },
    setup() {},
    created() {
        this.loadActiveSelections()
    },
    updated() {},
    unmounted() {},
    methods: {
        ...mapActions(store, ['removeSelection']),
        loadActiveSelections() {
            if (this.editorMode) this.activeSelections = [...descriptor.activeSelectionsEditorMock] as ISelection[]
            else this.activeSelections = this.propActiveSelections
        },
        getChipsStyle() {
            let height = this.propWidget.settings.style.chips.height
            return getWidgetStyleByTypeWithoutValidation(this.propWidget, 'chips') + `height: ${height != 0 ? height : 25}px`
        },
        onDeleteSelection(selection: ISelection) {
            if (this.editorMode) return
            const payload = { datasetId: selection.datasetId, columnName: selection.columnName }
            this.removeSelection(payload, this.dashboardId)
        }
    }
})
</script>
