<template>
    <div v-if="model?.yAxis" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.min') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.yAxis.min" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.scale.minHint')"></i>
                <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="onInputChanged('min')" />
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.max') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.yAxis.max" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.scale.maxHint')"></i>
                <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="onInputChanged('max')" />
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
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'hihgcharts-gauge-scale-settings',
    components: { InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            model: null as IHighchartsChartModel | null
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onInputChanged(type: 'min' | 'max') {
            setTimeout(() => {
                if (!this.model) return
                type === 'min' ? (this.model.yAxis.min = null) : (this.model.yAxis.max = null)
                this.modelChanged()
                console.log('DEEEEEEEEEEP COPY: ', deepcopy(this.model))
            }, 500)
        }
    }
})
</script>
