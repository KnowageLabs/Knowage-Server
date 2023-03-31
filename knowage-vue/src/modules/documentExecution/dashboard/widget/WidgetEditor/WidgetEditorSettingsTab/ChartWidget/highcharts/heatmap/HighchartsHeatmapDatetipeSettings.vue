<template>
    <div v-if="datetypeSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            {{ datetypeSettings }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartsHeatmapDatetype } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsHeatmapWidget'

export default defineComponent({
    name: 'highcharts-heatmap-datetype-settings',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            datetypeSettings: null as IHighchartsHeatmapDatetype | null
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            if (this.widgetModel.settings?.configuration) this.datetypeSettings = this.widgetModel.settings.configuration.datetypeSettings
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
