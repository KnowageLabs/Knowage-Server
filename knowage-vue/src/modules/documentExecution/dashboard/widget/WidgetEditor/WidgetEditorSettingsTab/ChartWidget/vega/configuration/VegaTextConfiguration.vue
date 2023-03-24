<template>
    <div v-if="textConfiguration" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12 p-md-6 p-lg-6 p-d-flex p-flex-column">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.textConfiguration.font') }}</label>
            <Dropdown v-model="textConfiguration.font" class="kn-material-input" :options="descriptor.fontOptions" option-value="value" @change="modelChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.fontOptions, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>

        <div class="p-col-12 p-md-3 p-lg-3 p-d-flex p-flex-column">
            <label v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.minimumFontSize')" class="kn-material-input-label p-mr-2 kn-truncated">{{ $t('dashboard.widgetEditor.textConfiguration.minimumFontSize') }}</label>
            <InputNumber v-model="textConfiguration.minimumFontSize" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
        </div>

        <div class="p-col-12 p-md-3 p-lg-3 p-d-flex p-flex-column">
            <label v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.maximumFontSize')" class="kn-material-input-label p-mr-2 kn-truncated">{{ $t('dashboard.widgetEditor.textConfiguration.maximumFontSize') }}</label>
            <InputNumber v-model="textConfiguration.maximumFontSize" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
        </div>

        <div class="p-col-12 p-md-4 p-lg-4 p-d-flex p-flex-column">
            <label v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.wordPadding')" class="kn-material-input-label p-mr-2 kn-truncated">{{ $t('dashboard.widgetEditor.textConfiguration.wordPadding') }}</label>
            <InputNumber v-model="textConfiguration.wordPadding" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
        </div>

        <div class="p-col-12 p-md-4 p-lg-4 p-d-flex p-flex-column">
            <label v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.maximumWords')" class="kn-material-input-label p-mr-2 kn-truncated">{{ $t('dashboard.widgetEditor.textConfiguration.maximumWords') }}</label>
            <InputNumber v-model="textConfiguration.maxNumberOfWords" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
        </div>

        <div class="p-col-12 p-md-4 p-lg-4 p-d-flex p-flex-column">
            <label v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.wordAngle')" class="kn-material-input-label p-mr-2 kn-truncated">{{ $t('dashboard.widgetEditor.textConfiguration.wordAngle') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center p-fluid">
                <InputNumber v-model="textConfiguration.wordAngle" class="kn-material-input p-inputtext-sm" @blur="modelChanged" />
                <i v-tooltip.top="$t('dashboard.widgetEditor.textConfiguration.wordAngleHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVegaChartsTextConfiguration } from '@/modules/documentExecution/dashboard/interfaces/vega/VegaChartsWidget'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import descriptor from '../VegaChartsSettingsDescriptor.json'

export default defineComponent({
    name: 'vega-text-configuration',
    components: { Dropdown, InputNumber },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            textConfiguration: null as IVegaChartsTextConfiguration | null,
            getTranslatedLabel
        }
    },
    created() {
        this.loadModel()
    },
    methods: {
        loadModel() {
            this.textConfiguration = this.widgetModel.settings.configuration ? this.widgetModel.settings.configuration.textConfiguration : null
        },
        modelChanged() {
            setTimeout(() => emitter.emit('refreshChart', this.widgetModel.id), 250)
        }
    }
})
</script>
