<template>
    <div v-if="model?.chart?.options3d" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.configurationOf3D.alphaAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.chart.options3d.alpha" class="kn-material-input p-inputtext-sm" :disabled="configurationDisabled" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.configurationOf3D.alphaAngleHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.configurationOf3D.betaAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.chart.options3d.beta" class="kn-material-input p-inputtext-sm" :disabled="configurationDisabled" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.configurationOf3D.betaAngleHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div v-if="model.chart.type !== 'pie'" class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.configurationOf3D.viewDistance') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.chart.options3d.viewDistance" class="kn-material-input p-inputtext-sm" :disabled="configurationDisabled" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.configurationOf3D.viewDistanceHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div v-if="model.plotOptions?.pie" class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.configurationOf3D.totalDepth') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber v-model="model.plotOptions.pie.depth" class="kn-material-input p-inputtext-sm" :disabled="configurationDisabled" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.configurationOf3D.totalDepthHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import InputNumber from 'primevue/inputnumber'

export default defineComponent({
    name: 'hihgcharts-3d-configuration',
    components: { InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            model: null as IHighchartsChartModel | null
        }
    },
    computed: {
        configurationDisabled(): boolean {
            return !this.model || !this.model.chart.options3d.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
