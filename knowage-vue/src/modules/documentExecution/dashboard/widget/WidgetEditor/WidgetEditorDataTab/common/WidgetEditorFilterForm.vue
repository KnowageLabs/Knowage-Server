<template>
    <div v-if="column">
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
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import commonDescriptor from './WidgetCommonDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-filter-form',
    components: { InputSwitch, Dropdown },
    props: { propColumn: { type: Object as PropType<IWidgetColumn | null>, required: true } },
    data() {
        return {
            commonDescriptor,
            column: null as IWidgetColumn | null
        }
    },
    watch: {
        propColumn() {
            this.loadColumn()
        }
    },
    created() {
        this.loadColumn()
    },
    methods: {
        loadColumn() {
            this.column = this.propColumn
        },
        selectedColumnUpdated() {
            emitter.emit('selectedColumnUpdated', this.column)
        },
        getColumnFilterOptions() {
            return this.column?.fieldType === 'ATTRIBUTE' ? this.commonDescriptor.attributeColumnFilterOperators : this.commonDescriptor.measureColumnFilterOperators
        },
        onFilterOperatorChange() {
            if (!this.column || !this.column.filter) return
            if (!['=', '<', '>', '<=', '>=', '!=', 'IN', 'like', 'range'].includes(this.column.filter.operator)) this.column.filter.value = ''
            if (this.column.filter.operator !== 'range') delete this.column.filter.value2
            this.selectedColumnUpdated()
        }
    }
})
</script>
