<template>
    <div v-if="valuesManagementModel" class="p-grid p-jc-center p-ai-center kn-flex p-p-4">
        <div class="p-col-12 p-grid p-d-flex p-flex-row p-jc-start p-p-4">
            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="valuesManagementModel.showDataset" @change="valuesManagementChanged"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-10 p-d-flex">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.valuesManagement.showDataset') }}</label>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-mr-4" v-tooltip.top="$t('dashboard.widgetEditor.valuesManagement.showDatasetHint')"></i>
            </div>

            <div class="p-sm-12 p-md-2">
                <InputSwitch v-model="valuesManagementModel.showColumn" @change="valuesManagementChanged"></InputSwitch>
            </div>
            <div class="p-sm-12 p-md-10 p-d-flex">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.valuesManagement.showColumn') }}</label>
                <i class="pi pi-question-circle kn-cursor-pointer p-ml-auto p-mr-4" v-tooltip.top="$t('dashboard.widgetEditor.valuesManagement.showColumnHint')"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { ISelectionsWidgetValuesManagement } from '@/modules/documentExecution/dashboard/interfaces/DashboardSelectionsWidget'
import { emitter } from '../../../../../DashboardHelpers'
import descriptor from '../SelectionsWidgetSettingsDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'selections-widget-values-management',
    components: { InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    data() {
        return {
            descriptor,
            valuesManagementModel: null as ISelectionsWidgetValuesManagement | null
        }
    },
    created() {
        this.loadValuesManagementModel()
    },
    methods: {
        loadValuesManagementModel() {
            if (this.widgetModel.settings?.configuration?.valuesManagement) this.valuesManagementModel = this.widgetModel.settings.configuration.valuesManagement
        },
        valuesManagementChanged() {
            emitter.emit('valuesManagementChanged', this.valuesManagementModel)
            emitter.emit('refreshSelections', this.widgetModel.id)
        }
    }
})
</script>
