<template>
    <div v-if="axisModel" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ axisModel }}
        <div class="p-col-12 p-md-3 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.min') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="axisModel.min" class="kn-material-input p-inputtext-sm" @blur="onInputNumberChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.heatmap.axisMinHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-3 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.max') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="axisModel.max" class="kn-material-input p-inputtext-sm" @blur="onInputNumberChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.heatmap.axisMaxHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-3 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.heatmap.labelRotation') }}</label>
            <InputNumber v-model="axisModel.labels.rotation" class="kn-material-input p-inputtext-sm" @blur="onInputNumberChanged" />
        </div>
        <div class="p-col-12 p-md-3 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <Dropdown v-model="axisModel.labels.align" class="kn-material-input" :options="settingsDescriptor.alignmentOptions" option-value="value" @change="modelChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, settingsDescriptor.alignmentOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>
        <div class="p-col-12 p-px-2 p-pt-4">
            <WidgetEditorStyleToolbar :options="descriptor.styleToolbarSettings" :prop-model="toolbarModel" @change="onStyleToolbarChange"></WidgetEditorStyleToolbar>
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
                            <Textarea v-model="axisModel.labels.format" class="kn-material-input kn-width-full" rows="2" :auto-resize="true" maxlength="250" @change="modelChanged" />
                            <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.labels.formatHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
                        </div>
                    </div>
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.formatter') }}</label>
                        <Message v-if="axisModel.labels.formatterError" class="p-m-2" severity="warn" :closable="false" :style="settingsDescriptor.warningMessageStyle">
                            {{ axisModel.labels.formatterError }}
                        </Message>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <HighchartsFormatterCodeMirror :prop-code="axisModel.labels.formatterText" @change="onFormatterChange" @blur="modelChanged"></HighchartsFormatterCodeMirror>
                            <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.labels.formatterHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
                        </div>
                    </div>
                </div>
            </Transition>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsHeatmapAxis } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsHeatmapWidget'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from './HighchartsHeatmapAxisSettingsDescriptor.json'
import settingsDescriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import HighchartsFormatterCodeMirror from '../common/HighchartsFormatterCodeMirror.vue'
import Textarea from 'primevue/textarea'
import Message from 'primevue/message'

export default defineComponent({
    name: 'highcharts-heatmap-axis-settings',
    components: { Dropdown, InputNumber, WidgetEditorStyleToolbar, HighchartsFormatterCodeMirror, Textarea, Message },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, axis: { type: String, required: true } },
    data() {
        return {
            descriptor,
            settingsDescriptor,
            axisModel: null as IHighchartsHeatmapAxis | null,
            toolbarModel: {} as { 'font-family': string; 'font-size': string; 'font-weight': string; color: string },
            advancedVisible: false,
            getTranslatedLabel
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            if (!this.widgetModel.settings.chartModel || !this.widgetModel.settings.chartModel.model) return
            this.axisModel = this.axis === 'x' ? this.widgetModel.settings.chartModel.model.xAxis : this.widgetModel.settings.chartModel.model.yAxis
        },
        loadToolbarModel() {
            if (this.axisModel && this.axisModel.labels)
                this.toolbarModel = {
                    'font-family': this.axisModel.labels.style.fontFamily,
                    'font-size': this.axisModel.labels.style.fontSize,
                    'font-weight': this.axisModel.labels.style.fontWeight,
                    color: this.axisModel.labels.style.color
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onInputNumberChanged() {
            setTimeout(() => this.modelChanged(), 250)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.axisModel || !this.axisModel.labels || !this.axisModel.labels) return
            this.toolbarModel = { 'font-family': model['font-family'] ?? '', 'font-size': model['font-size'] ?? '14px', 'font-weight': model['font-weight'] ?? '', color: model.color ?? '' }
            this.axisModel.labels.style = { color: this.toolbarModel.color ?? '', fontSize: this.toolbarModel['font-size'] ?? '14px', fontFamily: this.toolbarModel['font-family'] ?? '', fontWeight: this.toolbarModel['font-weight'] ?? '' }
            this.modelChanged()
        },
        onFormatterChange(newValue: string) {
            if (!this.axisModel) return
            this.axisModel.labels.formatterText = newValue
        }
    }
})
</script>
