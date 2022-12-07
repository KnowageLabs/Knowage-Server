<template>
    <div v-if="model?.legend" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ model.legend }}
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.legend.enabled" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.align" :options="descriptor.alignmentOptions" optionValue="value" @change="modelChanged">
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
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.alignHint')"></i>
            </div>
        </div>
        <div class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.verticalAlign') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.verticalAlign" :options="descriptor.verticalAlignmentOptions" optionValue="value" @change="modelChanged">
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
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.verticalAlignHint')"></i>
            </div>
        </div>
        <div class="p-col-6 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.layout') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.legend.layout" :options="descriptor.layoutOptions" optionValue="value" @change="modelChanged">
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
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.legend.layoutHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.legendStyleOptions" :propModel="toolbarModel" :disabled="legendDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
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
            toolbarModel: {} as { 'justify-content': string; 'font-family': string; 'font-size': string; 'font-weight': string; color: string; 'background-color': string },
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
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            this.loadToolbarModel()
        },
        loadToolbarModel() {
            if (this.model?.legend) {
                this.toolbarModel = {
                    'justify-content': this.model.legend.align,
                    'font-family': this.model.legend.itemStyle.fontFamily,
                    'font-size': this.model.legend.itemStyle.fontSize,
                    'font-weight': this.model.legend.itemStyle.fontWeight,
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
                'justify-content': model['justify-content'] ?? '',
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.model.legend.align = this.getAlignValue(this.toolbarModel['justify-content'])
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
            // TODO - Put in helper
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
