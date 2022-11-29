<template>
    <div v-if="defaultValuesModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-grid">
            <div class="p-col-3 p-sm-12 p-md-3">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.defaultValues.enableDefaultValues') }}</label>
            </div>
            <div class="p-col-9 p-sm-12 p-md-9">
                <InputSwitch v-model="defaultValuesModel.enabled" @change="defaultValuesChanged"></InputSwitch>
            </div>
        </div>

        <div class="p-col-12 p-grid p-ai-center">
            <div v-if="isDateType" class="p-col-10 p-lg-11 p-grid">
                <div class="p-col-12 p-lg-6 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('cron.startDate') }}</label>
                    <Calendar v-model="(defaultValuesModel.startDate as Date)" :manualInput="true" :disabled="defaultModelDisabled" @input="defaultValuesChanged" @dateSelect="defaultValuesChanged"></Calendar>
                </div>

                <div class="p-col-12 p-lg-6 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('cron.endDate') }}</label>
                    <Calendar v-model="(defaultValuesModel.endDate as Date)" :manualInput="true" :disabled="defaultModelDisabled" @input="defaultValuesChanged" @dateSelect="defaultValuesChanged"></Calendar>
                </div>
            </div>
            <div v-else class="p-col-10 p-lg-11 p-grid">
                <div class="p-col-12 p-lg-9 p-fluid p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.defaultValues.selectDafaultValue') }}</label>
                    <Dropdown class="kn-material-input" v-model="defaultValuesModel.valueType" :options="descriptor.defaultValuesTypes" optionValue="value" :disabled="defaultModelDisabled" @change="onDefaultValuesTypeChanged">
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

                <div v-if="defaultValuesModel.valueType === 'STATIC'" class="p-col-12 p-lg-3 p-d-flex p-flex-column">
                    <label class="kn-material-input-label p-mr-2">{{ $t('common.value') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm kn-flex" v-model="defaultValuesModel.value" :disabled="defaultModelDisabled" @change="defaultValuesChanged" />
                </div>
            </div>
            <div class="p-col-2 p-lg-1 p-d-flex p-jc-center">
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-mr-4" v-tooltip.top="$t('dashboard.widgetEditor.defaultValues.hint')"></i>
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
import Calendar from 'primevue/calendar'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'selector-widget-default-values',
    components: { Calendar, InputSwitch, Dropdown },
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
        },
        isDateType() {
            return this.widgetModel?.settings?.isDateType
        }
    },
    watch: {
        isDateType() {
            this.onDefaultValuesTypeChanged()
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
            emitter.emit('defaultValuesChanged', this.widgetModel.id)
            emitter.emit('refreshSelector', this.widgetModel.id)
        },
        onDefaultValuesTypeChanged() {
            if (!this.defaultValuesModel) return
            if (this.isDateType) {
                delete this.defaultValuesModel.value
                delete this.defaultValuesModel.valueType
            } else {
                delete this.defaultValuesModel.startDate
                delete this.defaultValuesModel.endDate
            }
            if (this.defaultValuesModel.valueType !== 'STATIC') delete this.defaultValuesModel.value
            this.defaultValuesChanged()
        }
    }
})
</script>
