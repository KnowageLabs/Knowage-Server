<template>
    <div v-if="drillDownModel" class="p-grid p-jc-center p-ai-center p-p-4"></div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { IHighchartsDrilldown } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'hihgcharts-drilldown-settings',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            drillDownModel: null as IHighchartsDrilldown | null
        }
    },
    computed: {},
    created() {
        this.loadDrillDownModel()
    },
    methods: {
        loadDrillDownModel() {
            if (this.widgetModel?.settings?.interactions?.drilldown) this.drillDownModel = this.widgetModel.settings.interactions.drilldown
        }
    }
})
</script>
