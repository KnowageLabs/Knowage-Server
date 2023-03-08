<template>
    <div v-if="responsiveModel" class="p-grid p-jc-center p-ai-center p-p-4">Potential CR Changes?</div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { IPivotFieldPanel } from '@/modules/documentExecution/dashboard/interfaces/pivotTable/DashboardPivotTableWidget'
import descriptor from './PivotTableConfigDescriptor.json'

export default defineComponent({
    name: 'widget-responsive',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            responsiveModel: null as IPivotFieldPanel | null
        }
    },
    created() {
        this.loadResponsiveModel()
    },
    methods: {
        loadResponsiveModel() {
            if (this.widgetModel.settings.configuration.fieldPicker) this.responsiveModel = this.widgetModel.settings.configuration.fieldPicker
        }
    }
})
</script>
