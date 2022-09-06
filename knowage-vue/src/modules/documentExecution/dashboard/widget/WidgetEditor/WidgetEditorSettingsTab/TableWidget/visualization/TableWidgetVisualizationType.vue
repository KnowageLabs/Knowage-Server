<template>
    <div>
        {{ visualizationTypes }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, ITableWidgetVisualization, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'table-widget-visualization-type',
    components: { InputSwitch, MultiSelect },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            visualizationTypes: [] as ITableWidgetVisualization[]
        }
    },
    created() {
        this.setEventListeners()
        this.loadVisualizationTypes()
    },
    methods: {
        setEventListeners() {
            emitter.on('collumnRemoved', (column) => this.onColumnRemoved(column))
        },
        loadVisualizationTypes() {
            console.log(' ----- loadVisualizationTypes - model: ', this.widgetModel)
        },
        onColumnRemoved(column: IWidgetColumn) {
            console.log('ON COLUMN REMOVED: ', column)
        }
    }
})
</script>
