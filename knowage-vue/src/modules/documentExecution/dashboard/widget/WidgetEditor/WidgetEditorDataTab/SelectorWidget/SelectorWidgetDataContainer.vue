<template>
    <div v-if="widget">
        <div class="p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
            <Dropdown v-model="sortingOrder" class="kn-material-input" :options="commonDescriptor.sortingOrderOptions" option-value="value" @change="sortingChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ slotProps.value }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
        <WidgetEditorColumnTable class="p-m-2" :widget-model="widget" :items="columnTableItems" :settings="commonDescriptor.columnTableSettings" @itemAdded="onColumnAdded" @itemUpdated="onColumnItemUpdate" @itemSelected="setSelectedColumn" @itemDeleted="onColumnDelete"></WidgetEditorColumnTable>
        <WidgetEditorFilterForm v-if="selectedColumn" :prop-column="selectedColumn"></WidgetEditorFilterForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDataset, IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from '../TableWidget/TableWidgetDataDescriptor.json'
import Dropdown from 'primevue/dropdown'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'
import WidgetEditorColumnTable from '../common/WidgetEditorColumnTable.vue'
import WidgetEditorFilterForm from '../common/WidgetEditorFilterForm.vue'

export default defineComponent({
    name: 'selector-widget-data-container',
    components: { Dropdown, WidgetEditorColumnTable, WidgetEditorFilterForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            commonDescriptor,
            widget: {} as IWidget,
            columnTableItems: [] as IWidgetColumn[],
            selectedColumn: null as IWidgetColumn | null,
            sortingOrder: ''
        }
    },
    watch: {
        selectedDataset() {
            this.selectedColumn = null
        }
    },
    async created() {
        this.loadWidget()
        this.$watch('widget.columns', () => this.loadColumnTableItems())
        this.loadColumnTableItems()
        this.loadSortingOrder()
    },
    methods: {
        loadWidget() {
            this.widget = this.widgetModel
        },
        loadColumnTableItems() {
            this.columnTableItems = this.widget.columns ?? []
        },
        loadSortingOrder() {
            this.sortingOrder = this.widget.settings.sortingOrder ?? ''
        },
        onColumnAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[] }) {
            if (this.widget.columns.length > 0) {
                emitter.emit('columnRemoved', this.widget.columns[0])
                this.selectedColumn = null
            }
            this.widget.settings.isDateType = payload.column.type.toLowerCase().includes('date') || payload.column.type.toLowerCase().includes('timestamp')
            this.widget.columns = [payload.column]
            emitter.emit('columnAdded', payload.column)
            emitter.emit('refreshSelector', this.widget.id)
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        onColumnItemUpdate(column: IWidgetColumn) {
            this.widget.columns[0] = { ...column }
            emitter.emit('collumnUpdated', { column: this.widget.columns[0], columnIndex: 0 })
            emitter.emit('refreshSelector', this.widget.id)
            if (this.widget.columns[0].id === this.selectedColumn?.id) this.selectedColumn = { ...this.widget.columns[0] }
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        setSelectedColumn(column: IWidgetColumn) {
            this.selectedColumn = { ...column }
        },
        onColumnDelete(column: IWidgetColumn) {
            this.widget.columns = []
            if (column.id === this.selectedColumn?.id) this.selectedColumn = null
            emitter.emit('columnRemoved', column)
            emitter.emit('refreshSelector', this.widget.id)
            if (this.widget.columns.length == 0) emitter.emit('clearWidgetData', this.widget.id)
            else emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        sortingChanged() {
            this.widget.settings.sortingOrder = this.sortingOrder
            if (this.widget.columns.length > 0) emitter.emit('refreshWidgetWithData', this.widget.id)
            emitter.emit('refreshSelector', this.widget.id)
        }
    }
})
</script>
