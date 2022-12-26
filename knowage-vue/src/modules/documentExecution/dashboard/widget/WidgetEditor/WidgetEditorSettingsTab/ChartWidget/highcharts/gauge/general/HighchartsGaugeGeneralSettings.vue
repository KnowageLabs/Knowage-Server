<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.startAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.pane.startAngle" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.paneSettings.startAngleHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.endAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.pane.endAngle" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.paneSettings.endAngleHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.centralHorizontalPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="centralHorizontalPosition" @blur="onPositionChanged('horizontal')" />
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.paneSettings.centralVerticalPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="centralVerticalPosition" @blur="onPositionChanged('vertical')" />
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
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'hihgcharts-gauge-geeneral-settings',
    components: { InputSwitch, InputNumber },
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
            emitter.emit('refreshChart', this.widgetModel.id)
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
