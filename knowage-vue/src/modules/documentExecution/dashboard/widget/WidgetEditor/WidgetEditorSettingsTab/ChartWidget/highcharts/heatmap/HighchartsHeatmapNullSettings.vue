<template>
    <div v-if="heatmapPlotOptions" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            {{ heatmapPlotOptions }}
        </div>
        <div class="p-col-3 p-md-4">
            <InputSwitch v-model="heatmapPlotOptions.connectNulls" @change="modelChanged"></InputSwitch>
            <label class="kn-material-input-label p-m-2">{{ $t('dashboard.widgetEditor.highcharts.heatmap.nullValues.connectNulls') }}</label>
        </div>
        <div class="p-col-9 p-md-8 p-d-flex p-flex-row">
            <WidgetEditorColorPicker class="kn-flex" :initial-value="heatmapPlotOptions.nullColor" :label="$t('dashboard.widgetEditor.highcharts.heatmap.nullValues.nullColor')" @change="onSelectionColorChanged"></WidgetEditorColorPicker>
            <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain" @click="removeNullColor" />
        </div>
    </div>
</template>

<script lang="ts">
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartPlotOptions } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { defineComponent, PropType } from 'vue'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import InputSwitch from 'primevue/inputswitch'
import WidgetEditorColorPicker from '../../../common/WidgetEditorColorPicker.vue'

export default defineComponent({
    name: 'highcharts-heatmap-null-settings',
    components: { InputSwitch, WidgetEditorColorPicker },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            heatmapPlotOptions: null as IHighchartsChartPlotOptions | null
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            if (this.widgetModel.settings?.chartModel?.model?.plotOptions) this.heatmapPlotOptions = this.widgetModel.settings.chartModel.model.plotOptions.heatmap
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onSelectionColorChanged(event: string | null) {
            if (!event || !this.heatmapPlotOptions) return
            this.heatmapPlotOptions.nullColor = event
            this.modelChanged()
        },
        removeNullColor() {
            delete this.heatmapPlotOptions?.nullColor
        }
    }
})
</script>
