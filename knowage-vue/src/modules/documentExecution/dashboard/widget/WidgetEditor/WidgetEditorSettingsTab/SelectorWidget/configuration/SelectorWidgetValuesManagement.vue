<template>
    <div v-if="valuesManagementModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12">
            {{ valuesManagementModel }}
        </div>
        <div class="p-col-12 p-grid p-d-flex p-flex-row p-jc-start p-p-4">
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="valuesManagementModel.hideDisabled" @change="onHideDisabledChanged"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-5">
                <label class="kn-material-input-label p-ml-auto">{{ $t('dashboard.widgetEditor.valuesManagement.hideDisabledValues') }}</label>
            </div>
            <div class="p-md-5"></div>
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="valuesManagementModel.enableAll" @change="onEnableAllChange"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-5">
                <label class="kn-material-input-label p-ml-auto">{{ $t('dashboard.widgetEditor.valuesManagement.alwaysEnableValues') }}</label>
            </div>
            <div class="p-md-5"></div>
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="valuesManagementModel.wrapText" @change="valuesManagementChanged"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-5">
                <label class="kn-material-input-label p-ml-auto">{{ $t('dashboard.widgetEditor.valuesManagement.wrapText') }}</label>
            </div>
            <div class="p-md-5"></div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectorWidgetValuesManagement } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectorWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectorWidgetSettingsDescriptor.json'
import Calendar from 'primevue/calendar'
import InputSwitch from 'primevue/inputswitch'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'selector-widget-values-management',
    components: { Calendar, InputSwitch, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            valuesManagementModel: null as ISelectorWidgetValuesManagement | null
        }
    },
    created() {
        this.loadDefaultValuesModel()
    },
    methods: {
        loadDefaultValuesModel() {
            if (this.widgetModel.settings?.configuration?.valuesManagement) this.valuesManagementModel = this.widgetModel.settings.configuration.valuesManagement
        },
        valuesManagementChanged() {
            emitter.emit('valuesManagementChanged', this.valuesManagementModel)
            emitter.emit('refreshSelector', this.widgetModel.id)
        },
        onHideDisabledChanged() {
            if (!this.valuesManagementModel) return
            if (this.valuesManagementModel.hideDisabled) this.valuesManagementModel.enableAll = false
            this.valuesManagementChanged()
        },
        onEnableAllChange() {
            if (!this.valuesManagementModel) return
            if (this.valuesManagementModel.enableAll) this.valuesManagementModel.hideDisabled = false
            this.valuesManagementChanged()
        }
    }
})
</script>
