<template>
    <div v-if="tooltipSettings" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-5 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.prefix') }}</label>
            <InputText v-model="tooltipSettings.prefix" class="kn-material-input p-inputtext-sm" @change="modelChanged" />
        </div>
        <div class="p-col-12 p-md-5 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.suffix') }}</label>
            <InputText v-model="tooltipSettings.suffix" class="kn-material-input p-inputtext-sm" @change="modelChanged" />
        </div>

        <div class="p-col-12 p-md-2 p-d-flex p-flex-column">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.precision') }}</label>
            <InputNumber v-model="tooltipSettings.precision" class="kn-material-input p-inputtext-sm" :min="0" @blur="onPrecisionChanged" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVegaChartsTooltipSettings } from '@/modules/documentExecution/dashboard/interfaces/vega/VegaChartsWidget'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import InputNumber from 'primevue/inputnumber'
import descriptor from '../VegaChartsSettingsDescriptor.json'

export default defineComponent({
    name: 'vega-tooltip-configuration',
    components: { InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            tooltipSettings: null as IVegaChartsTooltipSettings | null
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.tooltipSettings = this.widgetModel.settings.tooltip ?? null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        },
        onPrecisionChanged() {
            setTimeout(() => this.modelChanged(), 250)
        }
    }
})
</script>
