<template>
    <div v-if="model?.legend" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.legend.enabled" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.align" :options="descriptor.alignmentOptions" optionValue="value" :disabled="legendDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.alignmentOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.alignHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.verticalAlign') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.verticalAlign" :options="descriptor.verticalAlignmentOptions" optionValue="value" :disabled="legendDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.verticalAlignmentOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.verticalAlignHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.layout') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.layout" :options="descriptor.layoutOptions" optionValue="value" :disabled="legendDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.layoutOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.layoutHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.legendStyleOptions" :propModel="toolbarModel" :disabled="legendDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
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
                            <Textarea class="kn-material-input kn-width-full" rows="2" :autoResize="true" v-model="model.legend.labelFormat" maxlength="250" :disabled="legendDisabled" @change="modelChanged" />
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.formatHint')"></i>
                        </div>
                    </div>
                    <div class="p-col-12">
                        <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.formatter') }}</label>
                        <Message v-if="model.legend.labelFormatterError" class="p-m-2" severity="warn" :closable="false" :style="descriptor.warningMessageStyle">
                            {{ model.legend.labelFormatterError }}
                        </Message>
                        <div class="p-d-flex p-flex-row p-ai-center">
                            <HighchartsFormatterCodeMirror :propCode="model.legend.labelFormatterText" :disabled="legendDisabled" @change="onFormatterChange" @blur="modelChanged"></HighchartsFormatterCodeMirror>
                            <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.formatterHint')"></i>
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
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import InputSwitch from 'primevue/inputswitch'
import Message from 'primevue/message'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'
import Textarea from 'primevue/textarea'
import HighchartsFormatterCodeMirror from '../common/HighchartsFormatterCodeMirror.vue'

export default defineComponent({
    name: 'hihgcharts-legend-settings',
    components: { Dropdown, InputSwitch, InputNumber, Message, WidgetEditorStyleToolbar, Textarea, HighchartsFormatterCodeMirror },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as HighchartsPieChartModel | null,
            toolbarModel: {} as { 'font-family': string; 'font-size': string; 'font-weight': string; 'border-color': string; color: string; 'background-color': string },
            advancedVisible: false,
            getTranslatedLabel
        }
    },
    computed: {
        legendDisabled(): boolean {
            return !this.model || !this.model.legend.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            this.loadToolbarModel()
        },
        loadToolbarModel() {
            if (this.model?.legend) {
                this.toolbarModel = {
                    'font-family': this.model.legend.itemStyle.fontFamily,
                    'font-size': this.model.legend.itemStyle.fontSize,
                    'font-weight': this.model.legend.itemStyle.fontWeight,
                    'border-color': '',
                    color: this.model.legend.itemStyle.color,
                    'background-color': this.model.legend.backgroundColor
                }
            }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.legend) return
            this.toolbarModel = {
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                'border-color': model['border-color'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.model.legend.borderColor = this.toolbarModel['border-color'] ?? ''
            this.model.legend.backgroundColor = this.toolbarModel['background-color'] ?? ''
            this.model.legend.itemStyle = {
                color: this.toolbarModel.color ?? '',
                fontSize: this.toolbarModel['font-size'] ?? '14px',
                fontFamily: this.toolbarModel['font-family'] ?? '',
                fontWeight: this.toolbarModel['font-weight'] ?? ''
            }
            this.modelChanged()
        },
        getAlignValue(toolbarValue: string) {
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
            if (!this.model) return
            this.model.legend.labelFormatterText = newValue
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
