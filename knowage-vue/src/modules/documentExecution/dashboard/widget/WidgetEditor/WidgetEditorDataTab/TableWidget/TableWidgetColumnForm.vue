<template>
    <div v-if="column" class="widget-editor-card p-p-2">
        {{ column }}
        <div>
            <div class="p-d-flex p-flex-row p-ai-center">
                <div class="p-d-flex p-flex-column kn-flex p-m-2">
                    <label class="kn-material-input-label p-mr-2">{{ $t('temp.selectedColumn.alias') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="column.alias" @change="selectedColumnUpdated" />
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
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '../../../../Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from '../TableWidget/TableWidgetDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'table-widget-column-form',
    components: { Dropdown },
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
        },
        selectedColumnUpdated() {
            emitter.emit('selectedColumnUpdated', this.column)
        },
        columnTypeChanged() {
            if (!this.column) return
            this.column.fieldType === 'ATTRIBUTE' ? delete this.column.aggregation : (this.column.aggregation = 'NONE')
            this.selectedColumnUpdated()
        }
    }
})
</script>
