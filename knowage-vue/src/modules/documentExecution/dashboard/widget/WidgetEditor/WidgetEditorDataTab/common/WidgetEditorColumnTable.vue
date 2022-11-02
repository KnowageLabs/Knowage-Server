<template>
    <div class="widget-editor-card p-p-2">
        <label v-if="settings.label" class="kn-material-input-label">{{ $t(settings.label) }}</label>

        <div :class="{ 'dropzone-active': settings.dropIsActive }" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent>
            <div v-if="settings.dropIsActive && rows.length === 0">
                <div id="drag-columns-hint" class="p-d-flex p-flex-row p-jc-center p-ai-center">{{ $t(settings.dragColumnsHint) }}</div>
            </div>
            <DataTable v-else :value="rows" class="p-datatable-sm kn-table table-headers-hidden" :dataKey="settings.dataKey" v-model:filters="filters" :globalFilterFields="settings.globalFilterFields" :responsiveLayout="'stack'" :breakpoint="'600px'" @rowReorder="onRowReorder">
                <template #header>
                    <div v-if="settings.globalFilterFields?.length > 0" class="table-header p-d-flex p-ai-center">
                        <span id="search-container" class="p-input-icon-left p-mr-3">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                        </span>
                    </div>
                </template>
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column v-if="rowReorderEnabled" :rowReorder="rowReorderEnabled" :style="settings.rowReorder.rowReorderColumnStyle" />
                <Column>
                    <template #body="slotProps">
                        <i :class="getIcon(slotProps.data)"></i>
                    </template>
                </Column>
                <Column class="kn-truncated" v-for="column in settings.columns" :key="column.field" :field="column.field" :header="column.header ? $t(column.header) : ''" :sortable="column.sortable">
                    <template #body="slotProps">
                        <div :style="column.style ?? ''">
                            <InputText v-if="column.field === 'alias'" class="kn-material-input" v-model="slotProps.data[column.field]" @change="onColumnAliasRenamed(slotProps.data)" />
                            <Dropdown
                                v-else-if="column.field === 'aggregation' && aggregationDropdownIsVisible(slotProps.data)"
                                class="kn-material-input column-aggregation-dropdown"
                                v-model="slotProps.data[column.field]"
                                :options="descriptor.columnAggregationOptions"
                                optionLabel="label"
                                optionValue="value"
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
                            <Button v-if="slotProps.data.formula" icon="fas fa-calculator" class="p-button-link" v-tooltip.top="$t('common.edit')" @click.stop="openCalculatedFieldDialog(slotProps.data)"></Button>
                            <Button icon="fas fa-cog" class="p-button-link" v-tooltip.top="$t('common.edit')" @click.stop="$emit('itemSelected', slotProps.data)"></Button>
                            <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click.stop="deleteItem(slotProps.data, slotProps.index)"></Button>
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
import { createNewWidgetColumn } from '../../helpers/tableWidget/TableWidgetFunctions'
import { emitter } from '../../../../DashboardHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import deepcopy from 'deepcopy'
import descriptor from '../TableWidget/TableWidgetDataDescriptor.json'

export default defineComponent({
    name: 'widget-editor-column-table',
    components: { Column, DataTable, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, items: { type: Array, required: true }, settings: { type: Object, required: true } },
    emits: ['rowReorder', 'itemUpdated', 'itemSelected', 'itemDeleted', 'itemAdded', 'singleItemReplaced'],
    data() {
        return {
            descriptor,
            rows: [] as IWidgetColumn[],
            filters: {} as any,
            inputValuesMap: {}
        }
    },
    computed: {
        rowReorderEnabled(): boolean {
            return this.widgetModel && ['table', 'html', 'text'].includes(this.widgetModel.type)
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
            this.rows = deepcopy(this.items) as IWidgetColumn[]
        },
        setFilters() {
            if (this.settings.globalFilterFields?.length) this.filters.global = [filterDefault]
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
            const tempColumn = createNewWidgetColumn(eventData)
            if (['table', 'html', 'text'].includes(this.widgetModel.type)) {
                this.rows.push(tempColumn as IWidgetColumn)
            } else {
                this.rows = [tempColumn]
            }
            this.$emit('itemAdded', tempColumn)
        },
        deleteItem(item: IWidgetColumn, index: number) {
            this.rows.splice(index, 1)
            this.$emit('itemDeleted', item)
        },
        aggregationDropdownIsVisible(row: any) {
            return row.fieldType === 'MEASURE'
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
            this.$emit('itemAdded', field)
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
