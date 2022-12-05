<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ model }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'

export default defineComponent({
    name: 'hihgcharts-no-data-message-configuration',
    components: { InputSwitch, InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            model: null as HighchartsPieChartModel | null
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            console.log('>>>>>> LOADED model: ', this.model)
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
