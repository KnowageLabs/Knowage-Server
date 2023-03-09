<template>
    <div v-if="widgetModel" class="p-grid">
        {{ widgetModel.fields }}
        <!-- TODO: removed events: onFieldAdded, onFieldItemUpdate, onFieldDelete - check if everything is ok with model  -->
        <!-- we dont need to emit to reload widget because it wont be updated in runtime -->
        <FieldTable v-for="(field, index) in widgetModel.fields" :key="index" class="p-col-12" :field-type="index" :widget-model="widgetModel" :items="field" :settings="descriptor[index]" @row-reorder="onFieldsReorder" @item-selected="setSelectedField" />
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
    name: 'widget-editor-common-data-container',
    components: { FieldTable, FieldForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedDataset: { type: Object as PropType<IDataset | null> } },
    data() {
        return {
            descriptor,
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
        selectedDataset() {
            this.selectedField = null
        }
    },
    async created() {
        this.loadColumnTableItems()
        console.log('widget model', this.widgetModel, this.widgetModel.columns)
    },
    methods: {
        loadColumnTableItems() {
            this.columnFields = this.widgetModel.fields?.columns ?? []
            this.rowFields = this.widgetModel.fields?.rows ?? []
            this.dataFields = this.widgetModel.fields?.data ?? []
            this.filterFields = this.widgetModel.fields?.filters ?? []
        },
        onFieldsReorder(payload: { fields: IWidgetColumn[]; fieldType: string }) {
            if (this.widgetModel.fields) {
                console.log('BEFORE', payload, this.widgetModel.fields?.[payload.fieldType])
                // eslint-disable-next-line vue/no-mutating-props
                this.widgetModel.fields[payload.fieldType] = payload.fields
                emitter.emit('columnsReordered', this.widgetModel.columns)
                console.log('AFTER', this.widgetModel.fields[payload.fieldType])
            }
        },
        onFieldAdded(payload: { column: IWidgetColumn; rows: IWidgetColumn[]; fieldType: string }) {
            if (this.widgetModel.fields) {
                console.log('BEFORE', payload, this.widgetModel.fields?.[payload.fieldType])
                // eslint-disable-next-line vue/no-mutating-props
                this.widgetModel.fields[payload.fieldType] = payload.rows
                emitter.emit('columnAdded', payload.column)
                console.log('AFTER', this.widgetModel.fields[payload.fieldType])
            }
        },
        onFieldItemUpdate(column: IWidgetColumn) {
            const index = this.widgetModel.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === column.id)
            if (index !== -1) {
                // eslint-disable-next-line vue/no-mutating-props
                this.widgetModel.columns[index] = { ...column }
                emitter.emit('collumnUpdated', { column: this.widgetModel.columns[index], columnIndex: index })
                if (this.widgetModel.columns[index].id === this.selectedField?.id) this.selectedField = { ...this.widgetModel.columns[index] }
            }
            this.loadColumnTableItems()
        },
        setSelectedField(column: IWidgetColumn) {
            console.log('selected field', column)
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
