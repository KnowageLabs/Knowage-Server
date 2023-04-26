<template>
    <div v-if="model?.yAxis" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown v-model="model.yAxis.tickPosition" class="kn-material-input kn-flex" :options="descriptor.tickPositionOptions" option-value="value" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.tickPositionOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickPositionHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-px-2 p-pt-4">
            <WidgetEditorColorPicker :initial-value="model.yAxis.tickColor" :label="$t('dashboard.widgetEditor.highcharts.tick.tickColor')" @change="onSelectionColorChanged"></WidgetEditorColorPicker>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickLength') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="model.yAxis.tickLength" class="kn-material-input p-inputtext-sm" @blur="onInputNumberChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickLengthHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickWidth') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="model.yAxis.tickWidth" class="kn-material-input p-inputtext-sm" @blur="onInputNumberChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickWidthHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-12 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.minorTickInterval') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputText v-model="minorTickInterval" class="kn-material-input p-inputtext-sm" @change="onMinorIntervalChange" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.minorTickIntervalHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
                <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="removeMinorTickInterval" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../HighchartsWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'hihgcharts-gauge-tick-settings',
    components: { Dropdown, InputNumber, WidgetEditorColorPicker },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            minorTickInterval: null as string | null,
            getTranslatedLabel
        }
    },
    computed: {},
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (this.model?.yAxis.minorTickInterval) this.minorTickInterval = this.model.yAxis.minorTickInterval
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onInputNumberChanged() {
            setTimeout(() => this.modelChanged(), 250)
        },
        onSelectionColorChanged(event: string | null) {
            if (!event || !this.model) return
            this.model.yAxis.tickColor = event
            this.modelChanged()
        },
        onMinorIntervalChange() {
            if (!this.model) return
            if (this.minorTickInterval === 'auto' || this.minorTickInterval === null) this.model.yAxis.minorTickInterval = this.minorTickInterval
            else this.model.yAxis.minorTickInterval = +this.minorTickInterval
            this.modelChanged()
        },
        removeMinorTickInterval() {
            if (!this.model) return
            this.minorTickInterval = null
            this.model.yAxis.minorTickInterval = null
            this.modelChanged()
        }
    }
})
</script>
