<template>
    <div v-if="model?.yAxis" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickPosition') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.yAxis.tickPosition" :options="descriptor.tickPositionOptions" optionValue="value" @change="modelChanged">
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
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickPositionHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-6 p-px-2 p-pt-4">
            <WidgetEditorColorPicker :initialValue="model.yAxis.tickColor" :label="$t('dashboard.widgetEditor.highcharts.tick.tickColor')" @change="onSelectionColorChanged"></WidgetEditorColorPicker>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickLength') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.yAxis.tickLength" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickLengthHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.tickWidth') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber class="kn-material-input p-inputtext-sm" v-model="model.yAxis.tickWidth" @blur="modelChanged" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.tickWidthHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-12 p-lg-4 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.tick.minorTickInterval') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputText class="kn-material-input p-inputtext-sm" v-model="minorTickInterval" @change="onMinorIntervalChange" />
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.tick.minorTickIntervalHint')"></i>
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
