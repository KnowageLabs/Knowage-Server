<template>
    <div v-if="model?.options?.plugins?.legend" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ model }}
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.options.plugins.legend.display" @change="modelChanged"></InputSwitch>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../ChartJSWidgetSettingsDescriptor.json'

import InputSwitch from 'primevue/inputswitch'
import { IChartJSChartModel } from '@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget'

export default defineComponent({
    name: 'chartJS-legend-settings',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IChartJSChartModel | null,
            getTranslatedLabel
        }
    },
    computed: {
        legendDisabled(): boolean {
            return !this.model
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
            console.log('>>>>>>>> LOADED MODEL: ', this.model)
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
