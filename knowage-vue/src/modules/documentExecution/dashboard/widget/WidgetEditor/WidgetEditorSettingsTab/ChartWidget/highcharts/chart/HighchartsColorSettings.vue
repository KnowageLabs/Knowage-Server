<template>
    <div class="p-grid p-jc-center p-ai-center p-p-4">
        {{ 'COLOR SETTINGS' }}
        {{ colors }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartColor } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'

export default defineComponent({
    name: 'hihgcharts-color-settings',
    components: {},
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            colors: [] as IHighchartColor[]
        }
    },
    computed: {},
    created() {
        this.loadColorSettings()
    },
    methods: {
        loadColorSettings() {
            if (this.widgetModel.settings.chart.colors) this.colors = this.widgetModel.settings.chart.colors
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
