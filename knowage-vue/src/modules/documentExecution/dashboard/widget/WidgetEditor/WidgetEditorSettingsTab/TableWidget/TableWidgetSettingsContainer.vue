<template>
    {{ 'sel' }}
    {{ selectedSetting }}
    <div v-if="widgetModel">
        <TableWidgetConfigurationConatiner v-if="selectedSetting === 'Comfiguration'" :widgetModel="widgetModel"></TableWidgetConfigurationConatiner>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import descriptor from './TableWidgetSettingsDescriptor.json'
import TableWidgetConfigurationConatiner from '../TableWidget/configuration/TableWidgetConfigurationConatiner.vue'

export default defineComponent({
    name: 'table-widget-settings-container',
    components: { TableWidgetConfigurationConatiner },
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
