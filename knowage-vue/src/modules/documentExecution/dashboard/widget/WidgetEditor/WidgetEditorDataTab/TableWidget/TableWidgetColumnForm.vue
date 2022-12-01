<template>
    <div v-if="column" class="widget-editor-card p-p-2">
        {{ column }}
        <div class="p-my-2">
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.alias') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="column.alias" @change="onColumnAliasRenamed" />
                </div>
            </div>

            <div v-if="widgetModel.type === 'chart' && column.drillOrder" class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingColumn') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.drillOrder.orderColumnId" :options="sortingColumnOptions" optionValue="id" optionLabel="alias" @change="sortingChanged"> </Dropdown>
                </div>
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.drillOrder.orderType" :options="commonDescriptor.sortingOrderOptions" optionValue="value" @change="selectedColumnUpdated">
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
            </div>

            <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.type') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.fieldType" :options="descriptor.columnTypeOptions" optionValue="value" optionLabel="label" :disabled="column.formula !== undefined" @change="columnTypeChanged"> </Dropdown>
                </div>
                <div v-if="column.fieldType === 'MEASURE'" class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.aggregation" :options="descriptor.columnAggregationOptions" optionValue="value" optionLabel="label" @change="selectedColumnUpdated"> </Dropdown>
                </div>
            </div>
        </div>

        <hr />

        <WidgetEditorFilterForm v-if="column.filter" :propColumn="column"></WidgetEditorFilterForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn, IWidgetColumnFilter } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './TableWidgetDataDescriptor.json'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'
import WidgetEditorFilterForm from '../common/WidgetEditorFilterForm.vue'

export default defineComponent({
    name: 'table-widget-column-form',
    components: { InputSwitch, Dropdown, WidgetEditorFilterForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedColumn: { type: Object as PropType<IWidgetColumn | null>, required: true } },
    data() {
        return {
            descriptor,
            commonDescriptor,
            column: null as IWidgetColumn | null
        }
    },
    computed: {
        sortingColumnOptions() {
            return this.widgetModel.columns
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
            this.column.aggregation = 'NONE'
            if (this.column.filter) {
                this.column.filter.operator = ''
                this.column.filter.value = ''
            }
            this.selectedColumnUpdated()
        },
        getColumnFilterOptions() {
            return this.column?.fieldType === 'ATTRIBUTE' ? this.descriptor.attributeColumnFilterOperators : this.descriptor.measureColumnFilterOperators
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
        sortingChanged() {
            if (!this.column || !this.column.drillOrder) return
            const index = this.sortingColumnOptions.findIndex((tempColumn: IWidgetColumn) => tempColumn.id === this.column?.drillOrder?.orderColumnId)
            if (index !== -1) this.column.drillOrder.orderColumn = this.sortingColumnOptions[index].columnName
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
