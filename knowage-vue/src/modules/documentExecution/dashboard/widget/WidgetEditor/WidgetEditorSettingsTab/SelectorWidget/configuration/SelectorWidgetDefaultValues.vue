<template>
    <div v-if="defaultValuesModel" class="p-grid p-jc-center p-ai-center p-p-4">
        {{ defaultValuesModel }}
        <div id="index-column-switch" class="p-col-12 p-grid p-p-3">
            <div class="p-col-3 p-sm-12 p-md-3">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.defaultValues.enableDefaultValues') }}</label>
            </div>
            <div class="p-col-9 p-sm-12 p-md-9">
                <InputSwitch v-model="defaultValuesModel.enabled" @change="defaultValuesChanged"></InputSwitch>
            </div>
        </div>

        <div class="p-col-12 p-fluid p-d-flex p-flex-column p-px-4 p-py-2">
            <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.defaultValues.selectDafaultValue') }}</label>
            <Dropdown class="kn-material-input" v-model="defaultValuesModel.valueType" :options="descriptor.defaultValuesTypes" optionValue="value" :disabled="defaultModelDisabled" @change="defaultValuesChanged">
                <template #value="slotProps">
                    <div>
                        <span>{{ getTranslatedLabel(slotProps.value, descriptor.defaultValuesTypes, $t) }}</span>
                    </div>
                </template>
                <template #option="slotProps">
                    <div>
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </template>
            </Dropdown>
        </div>

        <div class="p-col-12 p-grid p-px-3 p-py-4">
            <div class="p-col-3 p-sm-12 p-md-3">
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
            </div>
            <div class="p-col-9 p-sm-12 p-md-9">
                <InputSwitch v-model="defaultValuesModel.value" @change="defaultValuesChanged"></InputSwitch>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectorWidgetDefaultValues } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
import { emitter } from '../../../../../DashboardHelpers'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../SelectorWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'table-widget-rows',
    components: { InputSwitch, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            defaultValuesModel: null as ISelectorWidgetDefaultValues | null,
            getTranslatedLabel
        }
    },
    computed: {
        defaultModelDisabled() {
            return !this.defaultValuesModel || !this.defaultValuesModel.enabled
        }
    },
    created() {
        this.loadDefaultValuesModel()
    },
    methods: {
        loadDefaultValuesModel() {
            if (this.widgetModel.settings?.configuration?.defaultValues) this.defaultValuesModel = this.widgetModel.settings.configuration.defaultValues
        },
        defaultValuesChanged() {
            emitter.emit('defaultValuesChanged', this.defaultValuesModel)
            emitter.emit('refreshSelector', this.widgetModel.id)
        }
    }
})
</script>
