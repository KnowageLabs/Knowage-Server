<template>
    <div class="widget-editor-card p-p-2">
        <div class="p-d-flex p-flex-column">
            <label v-if="settings.label" class="kn-material-input-label">{{ $t(settings.label) }}</label>
            <small v-if="settings.hint"> {{ $t(settings.hint) }}</small>
        </div>

        <div :class="{ 'dropzone-active': settings.dropIsActive }" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent>
            <div v-if="settings.dropIsActive && rows.length === 0">
                <div id="drag-columns-hint" class="p-d-flex p-flex-row p-jc-center p-ai-center">{{ $t(settings.dragColumnsHint) }}</div>
            </div>
            <DataTable v-else v-model:filters="filters" :value="rows" class="p-datatable-sm kn-table table-headers-hidden" :data-key="settings.dataKey" :global-filter-fields="settings.globalFilterFields" :responsive-layout="'stack'" :breakpoint="'600px'" @rowReorder="onRowReorder">
                <template #header>
                    <div v-if="settings.globalFilterFields?.length > 0" class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText v-model="filters['global'].value" class="kn-material-input" :placeholder="$t('common.search')" />
                        </span>
                    </div>
                </template>
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column v-if="rowReorderEnabled" :row-reorder="rowReorderEnabled" :style="settings.rowReorder.rowReorderColumnStyle" />
                <Column v-if="widgetModel.type !== 'highcharts' && widgetModel.type !== 'chartJS'">
                    <template #body="slotProps">
                        <i :class="getIcon(slotProps.data)"></i>
                    </template>
                </Column>
                <Column v-for="column in settings.columns" :key="column.field" class="kn-truncated" :field="column.field" :header="column.header ? $t(column.header) : ''" :sortable="column.sortable">
                    <template #body="slotProps">
                        <div>
                            <InputText v-if="column.field === 'alias'" v-model="slotProps.data[column.field]" class="kn-material-input" @change="onColumnAliasRenamed(slotProps.data)" />
                            <Dropdown
                                v-else-if="column.field === 'aggregation' && aggregationDropdownIsVisible(slotProps.data)"
                                v-model="slotProps.data[column.field]"
                                class="kn-material-input column-aggregation-dropdown"
                                :options="commonDescriptor.columnAggregationOptions"
                                option-label="label"
                                option-value="value"
                                @change="$emit('itemUpdated', slotProps.data)"
                            />
                            <span v-else-if="column.field === 'columnName'" class="kn-truncated">{{ '(' + slotProps.data[column.field] + ')' }}</span>
                            <span v-else class="kn-truncated">{{ slotProps.data[column.field] }}</span>
                        </div>
                    </template>
                </Column>
                <Column :style="settings.buttonColumnStyle">
                    <template #body="slotProps">
                        <div>
                            <Button v-if="slotProps.data.formula" v-tooltip.top="$t('common.edit')" icon="fas fa-calculator" class="p-button-link" @click.stop="openCalculatedFieldDialog(slotProps.data)"></Button>
                            <Button v-tooltip.top="$t('common.edit')" icon="fas fa-cog" class="p-button-link" @click.stop="$emit('itemSelected', slotProps.data)"></Button>
                            <Button v-tooltip.top="$t('common.delete')" icon="pi pi-trash" class="p-button-link" @click.stop="deleteItem(slotProps.data, slotProps.index)"></Button>
                        </div>
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { IWidget, IWidgetColumn } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import { addChartColumnToTable } from '../../helpers/chartWidget/ChartWidgetDataTabHelpers'
import { createNewWidgetColumn } from '../../helpers/WidgetEditorHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'

export default defineComponent({
    name: 'widget-editor-column-table',
    components: { Column, DataTable, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, items: { type: Array, required: true }, settings: { type: Object, required: true }, chartType: { type: String } },
    emits: ['rowReorder', 'itemUpdated', 'itemSelected', 'itemDeleted', 'itemAdded', 'singleItemReplaced'],
    data() {
        return {
            commonDescriptor,
            rows: [] as IWidgetColumn[],
            filters: {} as any,
            inputValuesMap: {}
        }
    },
    computed: {
        widgetType() {
            return this.widgetModel.type
        },
        rowReorderEnabled(): boolean {
            return this.widgetModel && (['table', 'html', 'text', 'discovery', 'customchart'].includes(this.widgetModel.type) || this.chartType !== 'heatmap') && this.rows.length > 1
        }
    },
    watch: {
        items() {
            this.loadItems()
        }
    },
    created() {
        this.setEventListeners()
        this.loadItems()
        this.setFilters()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        setEventListeners() {
            emitter.on('selectedColumnUpdated', this.onSelectedColumnUpdated)
            emitter.on('addNewCalculatedField', this.onCalcFieldAdded)
        },
        removeEventListeners() {
            emitter.off('selectedColumnUpdated', this.onSelectedColumnUpdated)
            emitter.off('addNewCalculatedField', this.onCalcFieldAdded)
        },
        onSelectedColumnUpdated(column: any) {
            this.updateSelectedColumn(column)
        },
        loadItems() {
            this.rows = this.items as IWidgetColumn[]
        },
        setFilters() {
            if (this.settings?.globalFilterFields?.length) this.filters.global = [filterDefault]
        },
        getIcon(item: IWidgetColumn) {
            return item.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'
        },
        onRowReorder(event: any) {
            this.rows = event.value
            this.$emit('rowReorder', event.value)
        },
        onDropComplete(event: any) {
            if (event.dataTransfer.getData('text/plain') === 'b') return
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            const tempColumn = createNewWidgetColumn(eventData, this.widgetType)
            if (['table', 'html', 'text', 'highcharts', 'chartJS', 'discovery', 'customchart', 'vega'].includes(this.widgetModel.type)) {
                if (['chartJS', 'highcharts', 'vega'].includes(this.widgetModel.type)) {
                    addChartColumnToTable(tempColumn, this.rows, this.chartType, this.settings.attributesOnly, this.settings.measuresOnly, this.widgetModel)
                } else if (['table'].includes(this.widgetModel.type) || !this.checkIfColumnIsAlreadyPresent(tempColumn)) this.rows.push(tempColumn as IWidgetColumn)
            } else {
                this.rows = [tempColumn]
            }
            this.$emit('itemAdded', { column: tempColumn, rows: this.rows, settings: this.settings })
        },
        checkIfColumnIsAlreadyPresent(tempColumn: IWidgetColumn) {
            const index = this.rows.findIndex((row: IWidgetColumn) => row.columnName === tempColumn.columnName)
            return index !== -1
        },
        deleteItem(item: IWidgetColumn, index: number) {
            this.rows.splice(index, 1)
            this.$emit('itemDeleted', item)
        },
        aggregationDropdownIsVisible(row: any) {
            return row.fieldType === 'MEASURE' && this.widgetType !== 'discovery'
        },
        updateSelectedColumn(selectedColumn: IWidgetColumn) {
            const index = this.rows.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === selectedColumn.id)
            if (index !== -1) {
                this.rows[index] = { ...selectedColumn }
                this.$emit('itemUpdated', this.rows[index])
            }
        },
        onColumnAliasRenamed(column: IWidgetColumn) {
            emitter.emit('columnAliasRenamed', column as IWidgetColumn)
            this.$emit('itemUpdated', column)
        },
        openCalculatedFieldDialog(column: IWidgetColumn) {
            emitter.emit('editCalculatedField', column)
        },
        onCalcFieldAdded(field) {
            this.rows.push(field as IWidgetColumn)
            this.$emit('itemAdded', { column: field, rows: this.rows, settings: this.settings })
        }
    }
})
</script>

<style lang="scss" scoped>
.table-headers-hidden {
    ::v-deep(.p-datatable-header) {
        display: none;
    }
}

#drag-columns-hint {
    min-height: 200px;
    min-width: 200px;
}

.column-aggregation-dropdown {
    min-width: 200px;
    max-width: 400px;
}
</style>
