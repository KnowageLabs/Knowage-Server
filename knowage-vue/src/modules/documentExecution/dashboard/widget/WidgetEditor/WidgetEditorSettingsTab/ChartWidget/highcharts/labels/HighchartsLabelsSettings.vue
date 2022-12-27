<template>
    {{ dataLabelsModel }}
    <div v-if="dataLabelsModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="dataLabelsModel.enabled" @change="modelChanged"></InputSwitch>
        </div>
        <div v-if="dataLabelsModel.distance" class="p-col-12 p-md-6 p-lg-3 p-d-flex p-flex-column kn-flex">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.labels.distance') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="dataLabelsModel.distance" :disabled="labelsConfigurationDisabled" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.labels.distanceHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.labelsStyleOptions" :propModel="toolbarModel" :disabled="labelsConfigurationDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
        <div class="p-col-12 p-py-4">
            <div class="p-d-flex p-flex-row p-jc-center">
                <label class="kn-material-input-label kn-cursor-pointer" @click="advancedVisible = !advancedVisible">{{ $t('common.advanced') }}<i :class="advancedVisible ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" class="p-ml-2"></i></label>
                <i class=""></i>
            </div>
            <Transition>
                <div v-if="advancedVisible" class="p-d-flex p-flex-column">
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.format') }}</label>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <Textarea class="kn-material-input kn-width-full" rows="2" :autoResize="true" v-model="dataLabelsModel.format" maxlength="250" :disabled="labelsConfigurationDisabled" @change="modelChanged" />
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.labels.formatHint')"></i>
                        </div>
                    </div>
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.formatter') }}</label>
                        <Message v-if="dataLabelsModel.formatterError" class="p-m-2" severity="warn" :closable="false" :style="descriptor.warningMessageStyle">
                            {{ dataLabelsModel.formatterError }}
                        </Message>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <HighchartsFormatterCodeMirror :propCode="dataLabelsModel.formatterText" @change="onFormatterChange" @blur="modelChanged"></HighchartsFormatterCodeMirror>
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.labels.formatterHint')"></i>
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
import { IHighchartsChartModel, IHighchartsChartDataLabels } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import Message from 'primevue/message'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import Textarea from 'primevue/textarea'
import HighchartsFormatterCodeMirror from '../common/HighchartsFormatterCodeMirror.vue'

export default defineComponent({
    name: 'hihgcharts-labels-settings',
    components: {
        InputSwitch,
        InputNumber,
        Message,
        WidgetEditorStyleToolbar,
        Textarea,
        HighchartsFormatterCodeMirror
    },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            dataLabelsModel: null as IHighchartsChartDataLabels | null,
            toolbarModel: {} as {
                'justify-content': string
                'font-family': string
                'font-size': string
                'font-weight': string
                color: string
                'background-color': string
            },
            advancedVisible: false
        }
    },
    computed: {
        labelsConfigurationDisabled(): boolean {
            return !this.dataLabelsModel || !this.dataLabelsModel.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.loadDataLabelsModel()
            this.loadToolbarModel()
        },
        loadDataLabelsModel() {
            switch (this.model?.chart.type) {
                case 'pie':
                    this.dataLabelsModel = this.model.plotOptions.pie?.dataLabels ?? null
                    break
                case 'gauge':
                    this.dataLabelsModel = this.model.plotOptions.gauge?.dataLabels ?? null
                    break
                case 'activitygauge':
                    this.dataLabelsModel = this.model.plotOptions.soldgauge?.dataLabels ?? null
                    break
            }
        },
        loadToolbarModel() {
            if (this.dataLabelsModel)
                this.toolbarModel = {
                    'justify-content': this.dataLabelsModel.position,
                    'font-family': this.dataLabelsModel.style.fontFamily,
                    'font-size': this.dataLabelsModel.style.fontSize,
                    'font-weight': this.dataLabelsModel.style.fontWeight,
                    color: this.dataLabelsModel.style.color,
                    'background-color': this.dataLabelsModel.backgroundColor
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.dataLabelsModel) return
            this.toolbarModel = {
                'justify-content': model['justify-content'] ?? '',
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            ;(this.dataLabelsModel.position = this.getTextAlignValue(this.toolbarModel['justify-content'])), (this.dataLabelsModel.backgroundColor = this.toolbarModel['background-color'] ?? '')
            this.dataLabelsModel.style = {
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
        },
        onFormatterChange(newValue: string) {
            if (!this.model || !this.dataLabelsModel) return
            this.dataLabelsModel.formatterText = newValue
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
