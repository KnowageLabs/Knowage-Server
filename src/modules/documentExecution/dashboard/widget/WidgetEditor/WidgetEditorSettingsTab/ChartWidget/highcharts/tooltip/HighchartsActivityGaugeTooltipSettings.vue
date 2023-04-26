<template>
    <div v-if="model" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-py-4">
            <WidgetEditorStyleToolbar :options="descriptor.activityGaugeTooltipStyleOptions" :prop-model="toolbarModel" :disabled="tooltipDisabled" @change="onStyleToolbarChange"> </WidgetEditorStyleToolbar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetStyleToolbarModel } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import WidgetEditorStyleToolbar from '../../../common/styleToolbar/WidgetEditorStyleToolbar.vue'

export default defineComponent({
    name: 'hihgcharts-activity-gauge-tooltip-settings',
    components: {
        WidgetEditorStyleToolbar
    },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            toolbarModel: {} as {
                'font-family': string
                'font-size': string
                'font-weight': string
                'background-color': string
            },
            advancedVisible: false
        }
    },
    computed: {
        tooltipDisabled(): boolean {
            return !this.model || !this.model.tooltip.enabled
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
            if (this.model?.tooltip)
                this.toolbarModel = {
                    'font-family': this.model.tooltip.style.fontFamily,
                    'font-size': this.model.tooltip.style.fontSize,
                    'font-weight': this.model.tooltip.style.fontWeight,
                    'background-color': this.model.tooltip.backgroundColor
                }
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onStyleToolbarChange(model: IWidgetStyleToolbarModel) {
            if (!this.model || !this.model.tooltip) return
            this.toolbarModel = {
                'font-family': model['font-family'] ?? '',
                'font-size': model['font-size'] ?? '',
                'font-weight': model['font-weight'] ?? '',
                'background-color': model['background-color'] ?? ''
            }
            ;(this.model.tooltip.backgroundColor = this.toolbarModel['background-color'] ?? ''),
                (this.model.tooltip.style = {
                    fontFamily: this.toolbarModel['font-family'] ?? '',
                    fontSize: this.toolbarModel['font-size'] ?? '14px',
                    fontWeight: this.toolbarModel['font-weight'] ?? ''
                })
            this.modelChanged()
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
