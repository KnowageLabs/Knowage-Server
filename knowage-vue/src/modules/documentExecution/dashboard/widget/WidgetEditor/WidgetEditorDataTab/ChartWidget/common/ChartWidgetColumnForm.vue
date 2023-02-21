<template>
    <div v-if="column" class="widget-editor-card p-p-2">
        <div class="p-my-2">
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.alias') }}</label>
                    <InputText v-model="column.alias" class="kn-material-input p-inputtext-sm" @change="onColumnAliasRenamed" />
                </div>

                <div v-if="column.fieldType === 'MEASURE'" class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                    <Dropdown v-model="column.aggregation" class="kn-material-input" :options="commonDescriptor.columnAggregationOptions" option-value="value" option-label="label" @change="selectedColumnUpdated"> </Dropdown>
                </div>
            </div>

            <div v-if="column.drillOrder" class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingColumn') }}</label>
                    <Dropdown v-model="column.drillOrder.orderColumnId" class="kn-material-input" :options="sortingColumnOptions" option-value="id" option-label="alias" @change="sortingChanged"> </Dropdown>
                </div>
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
                    <Dropdown v-model="column.drillOrder.orderType" class="kn-material-input" :options="commonDescriptor.sortingOrderOptions" option-value="value" @change="selectedColumnUpdated">
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

            <div v-else-if="column.orderType" class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
                    <Dropdown v-model="column.orderType" class="kn-material-input" :options="commonDescriptor.sortingOrderOptions" option-value="value" @change="selectedColumnUpdated">
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
        </div>

        <hr />

        <WidgetEditorFilterForm v-if="column.filter" :prop-column="column"></WidgetEditorFilterForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn, IWidgetColumnFilter } from '../../../../../Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import commonDescriptor from '../../common/WidgetCommonDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorFilterForm from '../../common/WidgetEditorFilterForm.vue'

export default defineComponent({
    name: 'table-widget-column-form',
    components: { Dropdown, WidgetEditorFilterForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedColumn: { type: Object as PropType<IWidgetColumn | null>, required: true } },
    data() {
        return {
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
