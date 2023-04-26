<template>
    <div class="p-grid">
        <div v-for="(parameter, index) in parameters" :key="index" class="p-grid p-col-12 p-ai-center p-p-2">
            <div class="p-sm-6 p-md-1 p-ai-center">
                <div class="p-mr-2">
                    <InputSwitch v-model="parameter.enabled" :disabled="disabled" @change="parametersChanged"></InputSwitch>
                </div>
            </div>
            <div class="p-sm-6 p-md-2 p-d-flex p-flex-column kn-flex">
                <label class="kn-material-input-label">{{ $t('common.parameter') }}</label>
                <InputText v-model="parameter.name" class="kn-material-input p-inputtext-sm" :disabled="true" />
            </div>
            <div class="p-sm-6 p-md-2 p-d-flex p-flex-column kn-flex p-p-2 value-type-dropdown">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown v-model="parameter.type" class="kn-material-input" :options="descriptor.previewParameterTypeOptions" option-value="value" :disabled="disabled" @change="onParameterTypeChanged(parameter)">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.previewParameterTypeOptions, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
            <div v-if="parameter.type === 'static'" class="p-sm-12 p-md-7 p-d-flex p-flex-column kn-flex">
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                <InputText v-model="parameter.value" class="kn-material-input p-inputtext-sm" :disabled="disabled" @change="parametersChanged" />
            </div>
            <div v-else-if="parameter.type === 'driver'" class="p-sm-12 p-md-7 p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown v-model="parameter.driver" class="kn-material-input" :options="drivers" option-label="name" option-value="name" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                </div>
            </div>
            <div v-else-if="parameter.type === 'dynamic'" class="p-sm-12 p-md-7 p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown v-model="parameter.column" class="kn-material-input" :options="widgetModel.columns" option-label="alias" option-value="alias" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                </div>
            </div>
            <div v-else-if="parameter.type === 'selection'" class="p-grid p-sm-12 p-md-7 p-ai-center kn-flex">
                <div class="p-sm-12 p-md-6 p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                        <Dropdown v-model="parameter.dataset" class="kn-material-input" :options="selectedDatasetNames" :disabled="disabled" @change="onDatasetChanged(parameter)"> </Dropdown>
                    </div>
                </div>
                <div class="p-sm-12 p-md-6 p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                        <Dropdown v-model="parameter.column" class="kn-material-input" :options="getSelectionDatasetColumnOptions(parameter)" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { IWidgetInteractionParameter, IWidget, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { mapActions } from 'pinia'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
import descriptor from '../WidgetInteractionsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-preview-parameters-list',
    components: { Dropdown, InputSwitch },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        propParameters: { type: Array as PropType<IWidgetInteractionParameter[]>, required: true },
        selectedDatasetsColumnsMap: { type: Object },
        dashboardId: { type: String, required: true },
        disabled: { type: Boolean }
    },
    emits: ['change'],
    data() {
        return {
            descriptor,
            parameters: [] as IWidgetInteractionParameter[],
            selectedDatasetNames: [] as string[],
            drivers: [] as IDashboardDriver[],
            getTranslatedLabel
        }
    },
    watch: {
        propParameters() {
            this.loadParameters()
        }
    },
    created() {
        this.loadParameters()
        this.loadSelectedDatasetNames()
    },
    methods: {
        ...mapActions(dashboardStore, ['getDashboardDrivers']),
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId)
        },
        loadParameters() {
            this.parameters = this.propParameters
        },
        loadSelectedDatasetNames() {
            if (!this.selectedDatasetsColumnsMap) return
            Object.keys(this.selectedDatasetsColumnsMap).forEach((key: string) => this.selectedDatasetNames.push(key))
        },
        parametersChanged() {
            this.$emit('change', this.parameters)
        },
        onParameterTypeChanged(parameter: IWidgetInteractionParameter) {
            parameter.value = ''
            switch (parameter.type) {
                case 'static':
                    delete parameter.column
                    delete parameter.dataset
                    delete parameter.driver
                    break
                case 'dynamic':
                    parameter.column = ''
                    delete parameter.dataset
                    delete parameter.driver
                    break
                case 'selection':
                    parameter.column = ''
                    delete parameter.driver
                    break
                case 'driver':
                    parameter.column = ''
                    delete parameter.column
                    delete parameter.dataset
            }
            this.parametersChanged()
        },
        onDatasetChanged(parameter: IWidgetInteractionParameter) {
            parameter.column = ''
            this.parametersChanged()
        },
        getSelectionDatasetColumnOptions(parameter: IWidgetInteractionParameter) {
            return parameter.dataset && this.selectedDatasetsColumnsMap ? this.selectedDatasetsColumnsMap[parameter.dataset] : []
        }
    }
})
</script>
