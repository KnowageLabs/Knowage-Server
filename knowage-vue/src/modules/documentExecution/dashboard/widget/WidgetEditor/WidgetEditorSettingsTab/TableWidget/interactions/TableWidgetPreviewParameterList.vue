<template>
    <div>
        {{ 'DATASET PARAMETERS: ' }}
        {{ datasetParameters }}
        <br />
        {{ 'MODEL PARAMETERS: ' }}
        {{ propParameters }}
    </div>
</template>

<script lang="ts">
import { ITableWidgetParameter, IWidget, IDatasetParameter } from '@/modules/documentExecution/Dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-preview-parameters-list',
    components: { Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, propParameters: { type: Array, required: true }, datasetParameters: { type: Array as PropType<IDatasetParameter[]>, required: true } },
    emits: ['change'],
    data() {
        return {
            descriptor,
            parameters: [] as any[],
            getTranslatedLabel
        }
    },
    watch: {
        propParameters() {
            this.loadParameters()
        }
    },
    created() {
        this.loadParameters()
    },
    methods: {
        loadParameters() {
            this.parameters = this.propParameters
        }
    }
})
</script>
