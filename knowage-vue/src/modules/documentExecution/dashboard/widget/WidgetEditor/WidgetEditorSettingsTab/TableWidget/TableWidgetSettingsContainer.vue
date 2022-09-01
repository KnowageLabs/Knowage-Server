<template>
    <div v-if="widgetModel">
        <TableWidgetSettingsAccordion v-if="selectedSetting" :widgetModel="widgetModel" :settings="descriptor.settings[selectedSetting]"></TableWidgetSettingsAccordion>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetSettingsAccordion from '../TableWidget/configuration/TableWidgetSettingsAccordion.vue'

export default defineComponent({
    name: 'table-widget-settings-container',
    components: { TableWidgetSettingsAccordion },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, selectedSetting: { type: String, required: true } },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>
