<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div v-if="!model.yAxis.stops || model.yAxis.stops.length === 0" class="p-grid p-col-12 p-pl-2">
            <Message class="p-col-11" :closable="false">{{ $t('dashboard.widgetEditor.highcharts.stops.stopsHint') }}</Message>
            <div class="p-col-1 p-text-right">
                <i class="pi pi-plus-circle kn-cursor-pointer p-pt-4" @click="addStop()"></i>
            </div>
        </div>

        <template v-else>
            <div v-for="(stop, index) in model.yAxis.stops" :key="index" class="p-grid p-col-12 p-ai-center p-ai-center p-pt-2">
                <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.highcharts.stops.relativePosition') }}</label>
                    <div class="p-d-flex p-flex-row p-ai-center">
                        <InputNumber v-model="stop[0]" class="kn-material-input p-inputtext-sm" mode="decimal" :min="0" :max="1" :min-fraction-digits="2" @blur="onRelativePositionChange" />
                        <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.stops.relativePositionHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
                    </div>
                </div>

                <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-row p-ai-center p-px-2 p-pt-3">
                    <WidgetEditorColorPicker class="kn-flex" :initial-value="stop[1]" :label="$t('common.color')" @change="onSelectionColorChanged($event, stop)"></WidgetEditorColorPicker>
                    <i v-tooltip.top="$t('dashboard.widgetEditor.highcharts.stops.colorHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
                </div>

                <div class="p-col-1 p-d-flex p-flex-row p-jc-center p-ai-center p-pl-2">
                    <i v-if="index === 0" class="pi pi-plus-circle kn-cursor-pointer p-pr-4 p-pt-2" @click="addStop()"></i>
                    <i :class="'pi pi-trash'" class="kn-cursor-pointer p-pt-2" @click="deleteStop(index)"></i>
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../../HighchartsWidgetSettingsDescriptor.json'
import InputNumber from 'primevue/inputnumber'
import Message from 'primevue/message'
import WidgetEditorColorPicker from '../../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'hihgcharts-stops-settings',
    components: { InputNumber, Message, WidgetEditorColorPicker },
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
            this.model.yAxis.stops.splice(index, 1)
            if (this.model.yAxis.stops.length === 0) this.model.yAxis.stops = null
            this.modelChanged()
        }
    }
})
</script>
