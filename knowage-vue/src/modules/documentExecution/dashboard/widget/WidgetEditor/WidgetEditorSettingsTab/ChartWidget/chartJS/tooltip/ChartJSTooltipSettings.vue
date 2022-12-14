<template>
    <div v-if="model?.options?.plugins?.tooltip" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.options.plugins.tooltip.enabled" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.tooltipStyleOptions" :propModel="toolbarModel" :disabled="tooltipDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { IChartJSChartModel } from '@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget'
import descriptor from '../ChartJSWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'chartJS-tooltip-settings',
    components: { Dropdown, InputSwitch, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IChartJSChartModel | null,
            toolbarModel: {} as { 'font-family': string; 'font-style': string; 'font-size': string; 'font-weight': string; color: string; 'background-color': string },
            getTranslatedLabel
        }
    },
    computed: {
        tooltipDisabled(): boolean {
            return !this.model || !this.model.options || !this.model.options.plugins.tooltip.enabled
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
            if (this.model?.options.plugins.tooltip) {
                this.toolbarModel = {
                    'font-family': this.model.options.plugins.tooltip.bodyFont.family ?? '',
                    'font-style': this.model.options.plugins.tooltip.bodyFont.style ?? 'normal',
                    'font-size': this.model.options.plugins.tooltip.bodyFont.size + 'px',
                    'font-weight': this.model.options.plugins.tooltip.bodyFont.weight,
                    color: this.model.options.plugins.tooltip.bodyColor,
                    'background-color': this.model.options.plugins.tooltip.backgroundColor
                }
            }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.options.plugins.tooltip) return
            console.log('>>>>> MODEL: ', model)
            this.toolbarModel = {
                'font-family': model['font-family'] ?? '',
                'font-style': model['font-style'] ?? 'normal',
                'font-size': model['font-size'] ?? '14px',
                'font-weight': model['font-weight'] ?? '',
                color: model.color ?? '',
                'background-color': model['background-color'] ?? ''
            }
            this.model.options.plugins.tooltip.backgroundColor = this.toolbarModel['background-color'] ?? ''
            this.model.options.plugins.tooltip.bodyColor = this.toolbarModel.color ?? ''
            this.model.options.plugins.tooltip.bodyFont = {
                family: this.toolbarModel['font-family'] ?? '',
                style: this.toolbarModel['font-style'] ?? 'normal',
                size: this.getFormattedFontSize(this.toolbarModel['font-size']),
                weight: this.toolbarModel['font-weight'] ?? ''
            }
            this.modelChanged()
        },
        getFormattedFontSize(fontSize: string) {
            if (!fontSize) return 0
            const formattedFontSize = fontSize.substring(0, fontSize.lastIndexOf('p'))
            return formattedFontSize ? +formattedFontSize : 0
        }
    }
})
</script>
