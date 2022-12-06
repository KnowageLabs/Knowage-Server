<template>
    <div v-if="model?.plotOptions?.pie?.dataLabels" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ model.plotOptions.pie.dataLabels }}
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.plotOptions.pie.dataLabels.enabled" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcarts.labels.distance') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.plotOptions.pie.dataLabels.distance" :disabled="labelsConfigurationDisabled" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcarts.labels.distanceHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.labelsStyleOptions" :propModel="toolbarModel" :disabled="labelsConfigurationDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
        <div class="p-col-12 p-py-4">
            <div class="p-d-flex p-flex-row p-jc-center">
                <label class="kn-material-input-label kn-cursor-pointer" @click="advancedVisible = !advancedVisible">{{ $t('common.advanced') }}<i :class="advancedVisible ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" class="p-ml-2"></i></label>
            </div>
            <Transition>
                <div v-if="advancedVisible" class="p-d-flex p-flex-column">
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.format') }}</label>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <Textarea class="kn-material-input kn-width-full" rows="2" :autoResize="true" v-model="model.plotOptions.pie.dataLabels.format" maxlength="250" :disabled="labelsConfigurationDisabled" @change="modelChanged" />
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcarts.labels.formatHint')"></i>
                        </div>
                    </div>
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.formatter') }}</label>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <Textarea class="kn-material-input kn-width-full" rows="20" :autoResize="true" v-model="model.plotOptions.pie.dataLabels.formatter" maxlength="1000" :disabled="labelsConfigurationDisabled" @change="modelChanged" />
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcarts.labels.formatterHint')"></i>
                        </div>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { HighchartsPieChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'hihgcharts-labels-settings',
    components: { InputSwitch, InputNumber, WidgetEditorStyleToolbar, Textarea },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as HighchartsPieChartModel | null,
            toolbarModel: {} as { 'justify-content': string; 'font-family': string; 'font-size': string; 'font-weight': string; color: string; 'background-color': string },
            advancedVisible: false
        }
    },
    computed: {
        labelsConfigurationDisabled(): boolean {
            return !this.model || !this.model.plotOptions.pie.dataLabels.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            if (this.model?.plotOptions?.pie?.dataLabels)
                this.toolbarModel = {
                    'justify-content': this.model.plotOptions.pie.dataLabels.style.textAlign,
                    'font-family': this.model.plotOptions.pie.dataLabels.style.fontFamily,
                    'font-size': this.model.plotOptions.pie.dataLabels.style.fontSize,
                    'font-weight': this.model.plotOptions.pie.dataLabels.style.fontWeight,
                    color: this.model.plotOptions.pie.dataLabels.style.color,
                    'background-color': this.model.plotOptions.pie.dataLabels.style.backgroundColor
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.plotOptions.pie.dataLabels) return
            this.toolbarModel = {
                'justify-content': model['justify-content'] ?? '',
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.model.plotOptions.pie.dataLabels.style = {
                textAlign: this.getTextAlignValue(this.toolbarModel['justify-content']),
                backgroundColor: 'black',
                color: this.toolbarModel.color ?? '',
                fontSize: this.toolbarModel['font-size'] ?? '14px',
                fontFamily: this.toolbarModel['font-family'] ?? '',
                fontWeight: this.toolbarModel['font-weight'] ?? ''
            }

            this.modelChanged()
        },
        getTextAlignValue(toolbarValue: string) {
            switch (toolbarValue) {
                case 'flex-start':
                    return 'left'
                case 'flex-end':
                    return 'right'
                default:
                    return 'center'
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.v-enter-active,
.v-leave-active {
    transition: opacity 0.3s ease;
}

.v-enter-from,
.v-leave-to {
    opacity: 0;
}
</style>
