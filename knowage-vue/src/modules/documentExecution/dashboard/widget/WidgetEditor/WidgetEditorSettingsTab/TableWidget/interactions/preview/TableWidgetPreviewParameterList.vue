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
                <InputText class="kn-material-input p-inputtext-sm" v-model="parameter.name" :disabled="true" />
            </div>
            <div class="p-sm-6 p-md-2 p-d-flex p-flex-column kn-flex p-p-2 value-type-dropdown">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="parameter.type" :options="descriptor.previewParameterTypeOptions" optionValue="value" :disabled="disabled" @change="onParameterTypeChanged(parameter)">
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
                <InputText class="kn-material-input p-inputtext-sm" v-model="parameter.value" :disabled="disabled" @change="parametersChanged" />
            </div>
            <div v-else-if="parameter.type === 'driver'" class="p-sm-12 p-md-7 p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="parameter.driver" :options="drivers" optionLabel="name" optionValue="name" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                </div>
            </div>
            <div v-else-if="parameter.type === 'dynamic'" class="p-sm-12 p-md-7 p-d-flex p-flex-row p-ai-center kn-flex">
                <div class="p-d-flex p-flex-column kn-flex">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="parameter.column" :options="widgetModel.columns" optionLabel="alias" optionValue="alias" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                </div>
            </div>
            <div v-else-if="parameter.type === 'selection'" class="p-grid p-sm-12 p-md-7 p-ai-center kn-flex">
                <div class="p-sm-12 p-md-6 p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.dataset" :options="selectedDatasetNames" :disabled="disabled" @change="onDatasetChanged(parameter)"> </Dropdown>
                    </div>
                </div>
                <div class="p-sm-12 p-md-6 p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.column" :options="getSelectionDatasetColumnOptions(parameter)" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { ITableWidgetParameter, IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../../TableWidgetSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-preview-parameters-list',
    components: { Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, propParameters: { type: Array as PropType<ITableWidgetParameter[]>, required: true }, selectedDatasetsColumnsMap: { type: Object }, drivers: { type: Array }, disabled: { type: Boolean } },
    emits: ['change'],
    data() {
        return {
            descriptor,
            parameters: [] as ITableWidgetParameter[],
            selectedDatasetNames: [] as string[],
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
        onParameterTypeChanged(parameter: ITableWidgetParameter) {
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
        onDatasetChanged(parameter: ITableWidgetParameter) {
            parameter.column = ''
            this.parametersChanged()
        },
        getSelectionDatasetColumnOptions(parameter: ITableWidgetParameter) {
            return parameter.dataset && this.selectedDatasetsColumnsMap ? this.selectedDatasetsColumnsMap[parameter.dataset] : []
        }
    }
})
</script>
