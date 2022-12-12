<template>
    <div v-if="model?.options?.plugins?.legend" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.enabled') }}</label>
            <InputSwitch v-model="model.options.plugins.legend.display" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.options.plugins.legend.align" :options="descriptor.alignmentOptions" optionValue="value" :disabled="legendDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.alignmentOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.chartJS.legend.alignHint')"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('metaweb.physicalModel.position') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown class="kn-material-input kn-flex" v-model="model.options.plugins.legend.position" :options="descriptor.positionOptions" optionValue="value" :disabled="legendDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.positionOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
                <i class="pi pi-question-circle kn-cursor-pointer  p-ml-2" v-tooltip.top="$t('dashboard.widgetEditor.chartJS.legend.positionHint')"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { IChartJSChartModel } from '@/modules/documentExecution/dashboard/interfaces/chartJS/DashboardChartJSWidget'
import descriptor from '../ChartJSWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'chartJS-legend-settings',
    components: { Dropdown, InputSwitch },
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
            return !this.model || !this.model.options || !this.model.options.plugins.legend.display
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.getModel() : null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
