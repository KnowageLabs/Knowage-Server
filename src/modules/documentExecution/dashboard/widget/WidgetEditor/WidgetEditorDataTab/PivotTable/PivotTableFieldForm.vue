<template>
    <div v-if="column" class="widget-editor-card kn-flex p-m-1">
        <div class="p-fluid p-formgrid p-grid p-m-2">
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <InputText v-model="column.alias" class="kn-material-input" @change="onColumnAliasRenamed" />
                    <label class="kn-material-input-label">{{ $t('common.alias') }}</label>
                </span>
            </div>
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown v-model="column.fieldType" class="kn-material-input" :options="descriptor.columnTypeOptions" option-value="value" option-label="label" :disabled="column.formula !== undefined" @change="columnTypeChanged"> </Dropdown>
                    <label class="kn-material-input-label">{{ $t('common.type') }}</label>
                </span>
            </div>
            <div v-if="!isDataField" class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown v-model="column.sort" class="kn-material-input" :options="descriptor.sortOptions" option-value="value" option-label="label" @change="onFieldSortChanged"> </Dropdown>
                    <label class="kn-material-input-label">{{ $t('common.sort') }}</label>
                </span>
            </div>
            <div v-if="column.fieldType === 'MEASURE'" class="p-field p-col-12">
                <span class="p-float-label">
                    <Dropdown v-model="column.aggregation" class="kn-material-input" :options="commonDescriptor.columnAggregationOptions" option-value="value" option-label="label" @change="selectedColumnUpdated"> </Dropdown>
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                </span>
            </div>

            <WidgetEditorFilterForm v-if="column.filter" class="p-field p-col-12" :prop-column="column"></WidgetEditorFilterForm>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn, IWidgetColumnFilter } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './PivotTableDataContainerDescriptor.json'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorFilterForm from '../common/WidgetEditorFilterForm.vue'

export default defineComponent({
    name: 'table-widget-column-form',
    components: { Dropdown, WidgetEditorFilterForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedColumn: { type: Object as PropType<IWidgetColumn | null>, required: true } },
    data() {
        return {
            descriptor,
            commonDescriptor,
            column: null as IWidgetColumn | null
        }
    },
    computed: {
        widgetType() {
            return this.widgetModel.type
        },
        isDataField() {
            const index = this.widgetModel.fields?.data.findIndex((field: any) => field.columnName === this.selectedColumn?.columnName)
            return index != -1
        }
    },
    watch: {
        selectedColumn() {
            this.loadSelectedColumn()
        }
    },
    created() {
        this.loadSelectedColumn()
    },
    methods: {
        loadSelectedColumn() {
            this.column = this.selectedColumn
            if (this.column && !this.column.filter) this.column.filter = { enabled: false, operator: '', value: '' } as IWidgetColumnFilter
        },
        selectedColumnUpdated() {
            emitter.emit('selectedColumnUpdated', this.column)
        },
        columnTypeChanged() {
            if (!this.column) return

            this.column.aggregation = this.widgetType === 'discovery' ? 'COUNT' : 'NONE'
            if (this.column.filter) {
                this.column.filter.operator = ''
                this.column.filter.value = ''
            }
            this.selectedColumnUpdated()
        },
        getColumnFilterOptions() {
            return this.column?.fieldType === 'ATTRIBUTE' ? this.commonDescriptor.attributeColumnFilterOperators : this.commonDescriptor.measureColumnFilterOperators
        },
        onFilterOperatorChange() {
            if (!this.column || !this.column.filter) return
            if (!['=', '<', '>', '<=', '>=', '!=', 'IN', 'like', 'range'].includes(this.column.filter.operator)) this.column.filter.value = ''
            if (this.column.filter.operator !== 'range') delete this.column.filter.value2
            this.selectedColumnUpdated()
        },
        onColumnAliasRenamed() {
            emitter.emit('columnAliasRenamed', this.column)
            this.selectedColumnUpdated()
        },
        onDiscoveryWidgetColumnAggregationChanged() {
            if (this.column && this.column.aggregation === 'COUNT') this.column.aggregationColumn = ''
            this.selectedColumnUpdated()
        },
        onFieldSortChanged() {
            this.selectedColumnUpdated()
        }
    }
})
</script>

<style lang="scss" scoped>
#filter-operator-dropdown {
    min-width: 300px;
    max-width: 400px;
}
</style>
