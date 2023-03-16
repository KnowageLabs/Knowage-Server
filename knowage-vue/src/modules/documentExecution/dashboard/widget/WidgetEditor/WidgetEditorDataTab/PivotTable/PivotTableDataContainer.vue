<template>
    <div v-if="widgetModel" class="p-grid">
        <FieldTable
            v-for="(field, index) in widgetModel.fields"
            :key="index"
            class="p-col-12"
            :field-type="index"
            :widget-model="widgetModel"
            :items="field"
            :settings="descriptor[index]"
            @row-reorder="onFieldsReorder"
            @item-added="onFieldAdded"
            @item-selected="setSelectedField"
            @item-updated="onFieldItemUpdate"
            @item-deleted="onFieldDelete"
        />
        <FieldForm :widget-model="widgetModel" :selected-column="selectedField" />
    </div>
</template>

<script lang="ts">
import descriptor from './PivotTableDataContainerDescriptor.json'
import { defineComponent, PropType } from 'vue'
import { IWidget, IDataset, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import { removeColumnFromPivotTableWidgetModel } from '../../helpers/pivotTableWidget/PivotTableFunctions'
import FieldTable from './PivotTableFieldsTable.vue'
import FieldForm from './PivotTableFieldForm.vue'

export default defineComponent({
    name: 'pivot-table-data-container',
    components: { FieldTable, FieldForm },
    props: { propWidgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
            widgetModel: {} as IWidget,
            columnFields: [] as IWidgetColumn[],
            rowFields: [] as IWidgetColumn[],
            dataFields: [] as IWidgetColumn[],
            filterFields: [] as IWidgetColumn[],
            selectedField: null as IWidgetColumn | null
        }
    },
    computed: {
        widgetType() {
            return this.widgetModel.type
        }
    },
    watch: {
        propWidgetModel() {
            this.loadWidgetModel()
        },
        selectedDataset() {
            this.selectedField = null
        }
    },
    async created() {
        this.loadWidgetModel()
        this.loadColumnTableItems()
    },
    methods: {
        loadWidgetModel() {
            this.widgetModel = this.propWidgetModel
        },
        loadColumnTableItems() {
            this.columnFields = this.widgetModel.fields?.columns ?? []
            this.rowFields = this.widgetModel.fields?.rows ?? []
            this.dataFields = this.widgetModel.fields?.data ?? []
            this.filterFields = this.widgetModel.fields?.filters ?? []
        },
        onFieldsReorder(payload: { fields: IWidgetColumn[]; fieldType: string }) {
            if (this.widgetModel.fields) {
                this.widgetModel.fields[payload.fieldType] = payload.fields
                emitter.emit('columnsReordered', this.widgetModel.columns)
            }
        },
        onFieldAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[]; fieldType: string }) {
            if (this.widgetModel.fields) {
                this.widgetModel.fields[payload.fieldType] = payload.rows
                emitter.emit('columnAdded', payload.column)
            }
        },
        onFieldItemUpdate(field: IWidgetColumn) {
            if (this.selectedField?.id === field.id) this.setSelectedField(field)
        },
        setSelectedField(column: IWidgetColumn) {
            this.selectedField = { ...column }
        },
        onFieldDelete(column: IWidgetColumn) {
            if (column.id === this.selectedField?.id) this.selectedField = null
            this.removeColumnFromModel(column)
            emitter.emit('columnRemoved', column)
        },
        removeColumnFromModel(column: IWidgetColumn) {
            removeColumnFromPivotTableWidgetModel(this.widgetModel, column)
        }
    }
})
</script>
