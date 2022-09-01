<template>
    <div v-if="widgetModel">
        <TableWidgetRows :widgetModel="widgetModel"></TableWidgetRows>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/Dashboard/Dashboard'
import descriptor from '../TableWidgetSettingsDescriptor.json'
import TableWidgetRows from './TableWidgetRows.vue'

export default defineComponent({
    name: 'table-widget-configuration-container',
    components: { TableWidgetRows },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor
        }
    },
    created() {},
    methods: {}
})
</script>
