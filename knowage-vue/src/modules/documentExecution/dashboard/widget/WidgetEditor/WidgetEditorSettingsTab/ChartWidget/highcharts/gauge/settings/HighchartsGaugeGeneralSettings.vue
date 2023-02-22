<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.startAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.pane.startAngle" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.paneSettings.startAngleHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.endAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.pane.endAngle" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.paneSettings.endAngleHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.centralHorizontalPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="centralHorizontalPosition" class="kn-material-input p-inputtext-sm" @blur="onPositionChanged('horizontal')" />
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.centralVerticalPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="centralVerticalPosition" class="kn-material-input p-inputtext-sm" @blur="onPositionChanged('vertical')" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'hihgcharts-gauge-geeneral-settings',
    components: { InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            model: null as IHighchartsChartModel | null,
            centralHorizontalPosition: 0,
            centralVerticalPosition: 0
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.loadHorizontalPosition('horizontal')
            this.loadHorizontalPosition('vertical')
        },
        modelChanged() {
            setTimeout(() => emitter.emit('refreshChart', this.widgetModel.id), 250)
        },
        loadHorizontalPosition(type: 'horizontal' | 'vertical') {
            if (!this.model || !this.model.pane.center || this.model.pane.center.length === 0) return
            const positionAsPercentageString = type === 'horizontal' ? this.model.pane.center[0] : this.model.pane.center[1]
            const formattedPosition = +positionAsPercentageString.trim().replace('%', '')
            type === 'horizontal' ? (this.centralHorizontalPosition = formattedPosition) : (this.centralVerticalPosition = formattedPosition)
        },
        onPositionChanged(type: 'horizontal' | 'vertical') {
            setTimeout(() => {
                if (!this.model) return
                type === 'horizontal' ? (this.model.pane.center[0] = this.centralHorizontalPosition + '%') : (this.model.pane.center[1] = this.centralVerticalPosition + '%')
                this.modelChanged()
            }, 250)
        }
    }
})
</script>
