<template>
    <div v-if="model?.options?.plugins?.legend" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('common.align') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown v-model="model.options.plugins.legend.align" class="kn-material-input kn-flex" :options="descriptor.alignmentOptions" option-value="value" :disabled="legendDisabled" @change="modelChanged">
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
                <i v-tooltip.top="$t('dashboard.widgetEditor.chartJS.legend.alignHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-lg-4 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label p-mr-2">{{ $t('metaweb.physicalModel.position') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <Dropdown v-model="model.options.plugins.legend.position" class="kn-material-input kn-flex" :options="descriptor.positionOptions" option-value="value" :disabled="legendDisabled" @change="modelChanged">
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
                <i v-tooltip.top="$t('dashboard.widgetEditor.chartJS.legend.positionHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
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

export default defineComponent({
    name: 'chart-j-s-legend-settings',
    components: { Dropdown },
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
            this.model = this.widgetModel.settings.chartModel ? this.widgetModel.settings.chartModel.model : null
        },
        modelChanged() {
            emitter.emit('refreshChart', this.widgetModel.id)
        }
    }
})
</script>
