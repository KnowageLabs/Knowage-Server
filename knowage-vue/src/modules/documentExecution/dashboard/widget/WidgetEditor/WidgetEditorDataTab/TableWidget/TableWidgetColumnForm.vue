<template>
    <div v-if="column" class="widget-editor-card p-p-2">
        <div class="p-my-2">
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.alias') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="column.alias" @change="onColumnAliasRenamed" />
                </div>
            </div>

            <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.type') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.fieldType" :options="descriptor.columnTypeOptions" optionValue="value" optionLabel="label" @change="columnTypeChanged"> </Dropdown>
                </div>
                <div v-if="column.fieldType === 'MEASURE'" class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.aggregation') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.aggregation" :options="descriptor.columnAggregationOptions" optionValue="value" optionLabel="label" @change="selectedColumnUpdated"> </Dropdown>
                </div>
            </div>
        </div>

        <hr />

        <div v-if="column.filter" class="p-my-2">
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.enableFilter') }}</label>
                    <InputSwitch v-model="column.filter.enabled" @change="selectedColumnUpdated"></InputSwitch>
                </div>
            </div>

            <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
                <div id="filter-operator-dropdown" class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.operator') }}</label>
                    <Dropdown class="kn-material-input" v-model="column.filter.operator" :options="getColumnFilterOptions()" optionValue="value" optionLabel="label" :disabled="!column.filter.enabled" @change="onFilterOperatorChange"> </Dropdown>
                </div>

                <div v-if="['=', '<', '>', '<=', '>=', '!=', 'IN', 'like', 'range'].includes(column.filter.operator)" class="p-d-flex p-flex-column kn-flex-3 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ column.filter.operator === 'range' ? $t('common.from') : $t('common.value') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="column.filter.value" :disabled="!column.filter.enabled" @change="selectedColumnUpdated" />
                </div>

                <div v-if="column.filter.operator === 'range'" class="p-d-flex p-flex-column kn-flex-3 p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.to') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="column.filter.value2" :disabled="!column.filter.enabled" @change="selectedColumnUpdated" />
                </div>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-mr-4" v-tooltip.top="$t('dashboard.widgetEditor.columnFilterHint')"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn, IWidgetColumnFilter } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './TableWidgetDataDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'table-widget-column-form',
    components: { InputSwitch, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedColumn: { type: Object as PropType<IWidgetColumn | null>, required: true } },
    data() {
        return {
            descriptor,
            column: null as IWidgetColumn | null
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
