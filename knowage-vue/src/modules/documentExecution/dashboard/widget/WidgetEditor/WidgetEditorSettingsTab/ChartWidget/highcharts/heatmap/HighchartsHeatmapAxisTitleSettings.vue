<template>
    <div v-if="axisModel" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            {{ axisModel?.title }}
        </div>
        <div class="p-col-12 p-md-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.text') }}</label>
            <InputText v-model="axisModel.title.text" class="kn-material-input p-inputtext-sm" :disabled="titleDisabled" @change="modelChanged" />
        </div>
        <div class="p-col-12 p-md-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <Dropdown v-model="axisModel.title.align" class="kn-material-input" :options="descriptor.axisTitleAlignOptions" option-value="value" @change="modelChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.axisTitleAlignOptions, $t) }}</span>
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
            <WidgetEditorStyleToolbar :options="descriptor.styleToolbarSettings" :prop-model="toolbarModel" :disabled="titleDisabled" @change="onStyleToolbarChange"></WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from './HighchartsHeatmapAxisSettingsDescriptor.json'
import settingsDescriptor from '../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'highcharts-heatmap-axis-title-settings',
    components: { Dropdown, WidgetEditorStyleToolbar },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, axis: { type: String, required: true } },
    data() {
        return {
            descriptor,
            settingsDescriptor,
            axisModel: null as any,
            toolbarModel: {} as { 'font-family': string; 'font-size': string; 'font-weight': string; color: string },
            getTranslatedLabel
        }
    },
    computed: {
        titleDisabled() {
            return !this.axisModel || !this.axisModel.title || !this.axisModel.title.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            if (!this.widgetModel.settings.chartModel || !this.widgetModel.settings.chartModel.model) return
            this.axisModel = this.axis === 'x' ? this.widgetModel.settings.chartModel.model.xAxis : this.widgetModel.settings.chartModel.model.yAxis
            this.loadToolbarModel()
        },
        loadToolbarModel() {
            if (this.axisModel && this.axisModel.title) this.toolbarModel = { 'font-family': this.axisModel.title.style.fontFamily, 'font-size': this.axisModel.title.style.fontSize, 'font-weight': this.axisModel.title.style.fontWeight, color: this.axisModel.title.style.color }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.axisModel || !this.axisModel.title) return
            this.toolbarModel = { 'font-family': model['font-family'] ?? '', 'font-size': model['font-size'] ?? '14px', 'font-weight': model['font-weight'] ?? '', color: model.color ?? '' }
            this.axisModel.title.style = { color: this.toolbarModel.color ?? '', fontSize: this.toolbarModel['font-size'] ?? '14px', fontFamily: this.toolbarModel['font-family'] ?? '', fontWeight: this.toolbarModel['font-weight'] ?? '' }
            this.modelChanged()
        }
    }
})
</script>
