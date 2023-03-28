<template>
    <div v-if="model?.accessibility" class="p-grid p-jc-center p-ai-center p-p-4">
        <div class="p-col-12">
            <label class="kn-material-input-label">{{ $t('common.description') }}</label>
            <Textarea v-model="model.accessibility.description" class="kn-material-input kn-width-full" rows="4" :auto-resize="true" maxlength="250" :disabled="accessibilityDisabled" @change="modelChanged" />
        </div>
        <div class="p-col-12 p-grid p-ai-center p-p-4">
            <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.accessibility.enabelKeyboardNavigation') }}</label>
            <InputSwitch v-model="model.accessibility.keyboardNavigation.enabled" :disabled="accessibilityDisabled" @change="modelChanged"></InputSwitch>
        </div>
        <div class="p-col-12 p-d-flex p-flex-column kn-flex p-m-2">
            <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.accessibility.keyboardNavigationOrder') }}</label>
            <div class="p-d-flex p-flex-row p-ai-center">
                <MultiSelect v-model="model.accessibility.keyboardNavigation.order" class="kn-material-input multiselect-keyboardNavigation" :options="descriptor.keyboardNavigationOrderOptions" option-value="value" :disabled="accessibilityDisabled" @change="modelChanged">
                    <template #value="slotProps">
                        <div v-for="value of slotProps.value" :key="value" class="option-item-value">
                            <span> {{ getTranslatedLabel(value, descriptor.keyboardNavigationOrderOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </MultiSelect>
                <i v-tooltip.top="$t('dashboard.widgetEditor.accessibility.keyboardNavigationOrderHint')" class="pi pi-question-circle kn-cursor-pointer p-ml-2"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../../../../Dashboard'
import { emitter } from '@/modules/documentExecution/dashboard/DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { IHighchartsChartModel } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget'
import descriptor from '../HighchartsWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import MultiSelect from 'primevue/multiselect'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'hihgcharts-accessibility-settings',
    components: { InputSwitch, MultiSelect, Textarea },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            model: null as IHighchartsChartModel | null,
            getTranslatedLabel
        }
    },
    computed: {
        accessibilityDisabled(): boolean {
            return !this.model || !this.model.accessibility.enabled
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

<style lang="scss" scoped>
.p-multiselect {
    width: 100%;
}

::v-deep(.multiselect-keyboardNavigation) {
    .p-multiselect-label:not(.p-placeholder) {
        padding-top: 0.25rem;
        padding-bottom: 0.25rem;
    }

    .option-item-value {
        padding: 0.25rem 0.5rem;
        border-radius: 3px;
        display: inline-flex;
        margin-right: 0.5rem;
        background-color: var(--primary-color);
        color: var(--primary-color-text);
    }
}
</style>
