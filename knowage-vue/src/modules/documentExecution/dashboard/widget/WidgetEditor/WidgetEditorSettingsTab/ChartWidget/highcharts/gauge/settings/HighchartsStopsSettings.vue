<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-text-right">
            <Button class="kn-button kn-button--primary" @click="addStop"> {{ $t('common.add') }}</Button>
        </div>
        <div v-for="(stop, index) in model.yAxis.stops" :key="index" class="p-grid p-col-12 p-ai-center p-ai-center p-pt-2">
            <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.stops.relativePosition') }}</label>
                <div class="p-d-flex p-flex-row p-ai-center">
                    <InputNumber class="kn-material-input p-inputtext-sm" v-model="stop[0]" mode="decimal" :min="0" :max="1" :minFractionDigits="2" @blur="onRelativePositionChange" />
                    <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.stops.relativePositionHint')"></i>
                </div>
            </div>

            <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-row p-ai-center p-px-2 p-pt-2">
                <WidgetEditorColorPicker class="kn-flex" :initialValue="stop[1]" :label="$t('common.color')" @change="onSelectionColorChanged($event, stop)"></WidgetEditorColorPicker>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.highcharts.stops.colorHint')"></i>
            </div>

            <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2">
                <i :class="'pi pi-trash'" class="kn-cursor-pointer" @click="deleteStop(index)"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../../HighchartsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import WidgetEditorColorPicker from '../../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'hihgcharts-stops-settings',
    components: { InputNumber, WidgetEditorColorPicker },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
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
        onRelativePositionChange() {
            setTimeout(() => this.modelChanged(), 250)
        },
        onSelectionColorChanged(event: string | null, stop: [number, string]) {
            if (!event) return
            stop[1] = event
            this.modelChanged()
        },
        addStop() {
            if (!this.model) return
            if (!this.model.yAxis.stops) this.model.yAxis.stops = []
            this.model.yAxis.stops.push([0, 'rgba(0, 0, 0, 1)'])
        },
        deleteStop(index: number) {
            if (!this.model) return
            this.model.yAxis.stops.splice(1, index)
            if (this.model.yAxis.stops.length === 0) this.model.yAxis.stops = null
            this.modelChanged()
        }
    }
})
</script>
