<template>
    <div v-if="widgetModel">
        <TableWidgetDataForm :widgetModel="widgetModel"></TableWidgetDataForm>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import TableWidgetDataForm from './TableWidgetDataForm.vue'

export default defineComponent({
    name: 'table-widget-data-container',
    components: { TableWidgetDataForm },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {}
    },
    async created() {},
    methods: {}
})
</script>
