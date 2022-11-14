<template>
    <div class="p-grid">
        <div class="p-col-12 p-text-right">
            <Button class="kn-button kn-button--primary" @click="$emit('addParameter')"> {{ $t('documentExecution.documentDetails.designerDialog.addParameter') }}</Button>
        </div>
        <div v-for="(parameter, index) in parameters" :key="index" class="p-grid p-col-12 p-ai-center p-p-2">
            <div class="p-grid p-ai-center p-col-11">
                <div class="p-sm-12 p-md-3 p-d-flex p-flex-column">
                    <label class="kn-material-input-label">{{ $t('common.parameter') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="parameter.name" :disabled="disabled" />
                </div>
                <div class="p-sm-12 p-md-3 kn-flex p-d-flex p-flex-column p-p-2">
                    <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                    <Dropdown class="kn-material-input" v-model="parameter.type" :options="descriptor.linkParameterTypeOptions" optionValue="value" :disabled="disabled" @change="onParameterTypeChanged(parameter)">
                        <template #value="slotProps">
                            <div>
                                <span>{{ getTranslatedLabel(slotProps.value, descriptor.linkParameterTypeOptions, $t) }}</span>
                            </div>
                        </template>
                        <template #option="slotProps">
                            <div>
                                <span>{{ $t(slotProps.option.label) }}</span>
                            </div>
                        </template>
                    </Dropdown>
                </div>
                <div v-if="parameter.type === 'static'" class="p-sm-11 p-md-5 p-d-flex p-flex-column">
                    <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                    <InputText class="kn-material-input p-inputtext-sm" v-model="parameter.value" :disabled="disabled" @change="parametersChanged" />
                </div>
                <div v-else-if="parameter.type === 'driver'" class="p-sm-11 p-md-5 p-d-flex p-flex-row p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.driver') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.driver" :options="drivers" optionLabel="name" optionValue="name" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                    </div>
                </div>
                <div v-else-if="parameter.type === 'dynamic'" class="p-sm-11 p-md-5 p-d-flex p-flex-row p-ai-center">
                    <div class="p-d-flex p-flex-column kn-flex">
                        <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                        <Dropdown class="kn-material-input" v-model="parameter.column" :options="widgetModel.columns" optionLabel="alias" optionValue="alias" :disabled="disabled" @change="parametersChanged"> </Dropdown>
                    </div>
                </div>
                <div v-else-if="parameter.type === 'selection'" class="p-grid p-sm-11 p-md-5 p-ai-center">
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
            <div class="p-col-1 p-text-center p-pt-2">
                <i :class="'pi pi-trash'" class="kn-cursor-pointer" @click="deleteParameter(index)"></i>
            </div>
            <div v-if="parameter.type === 'json'" class="p-grid p-col-12 p-ai-center">
                <TableWidgetParameterCodeMirror v-if="parameter.type === 'json'" :propParameter="parameter" :visible="parameter.type === 'json'"></TableWidgetParameterCodeMirror>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { IWidgetInteractionParameter, IWidget, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { defineComponent, PropType } from 'vue'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import descriptor from '../WidgetInteractionsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import TableWidgetParameterCodeMirror from './WidgetParameterCodeMirror.vue'

export default defineComponent({
    name: 'table-widget-link-parameters-list',
    components: { Dropdown, InputSwitch, TableWidgetParameterCodeMirror },
    props: {
        widgetModel: { type: Object as PropType<IWidget>, required: true },
        propParameters: { type: Array as PropType<IWidgetInteractionParameter[]>, required: true },
        selectedDatasetsColumnsMap: { type: Object },
        drivers: { type: Array as PropType<IDashboardDriver[]> },
        disabled: { type: Boolean }
    },
    emits: ['change', 'addParameter', 'delete'],
    data() {
        return {
            descriptor,
            parameters: [] as IWidgetInteractionParameter[],
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
        onParameterTypeChanged(parameter: IWidgetInteractionParameter) {
            parameter.value = ''
            switch (parameter.type) {
                case 'static':
                    parameter.value = ''
                    this.deleteFields(['column', 'dataset', 'driver', 'json'], parameter)
                    break
                case 'dynamic':
                    parameter.column = ''
                    this.deleteFields(['value', 'dataset', 'driver', 'json'], parameter)
                    break
                case 'selection':
                    parameter.column = ''
                    parameter.dataset = ''
                    this.deleteFields(['value', 'driver', 'json'], parameter)
                    break
                case 'driver':
                    parameter.driver = ''
                    this.deleteFields(['value', 'dataset', 'column', 'json'], parameter)
                    break
                case 'json':
                    parameter.json = ''
                    this.deleteFields(['value', 'column', 'dataset', 'driver'], parameter)
                    break
                case 'jwt':
                    this.deleteFields(['value', 'column', 'dataset', 'driver', 'json'], parameter)
            }
            this.parametersChanged()
        },
        deleteFields(fields: string[], parameter: IWidgetInteractionParameter) {
            fields.forEach((field: string) => delete parameter[field])
        },
        onDatasetChanged(parameter: IWidgetInteractionParameter) {
            parameter.column = ''
            this.parametersChanged()
        },
        getSelectionDatasetColumnOptions(parameter: IWidgetInteractionParameter) {
            return parameter.dataset && this.selectedDatasetsColumnsMap ? this.selectedDatasetsColumnsMap[parameter.dataset] : []
        },
        deleteParameter(index: number) {
            this.$emit('delete', index)
        }
    }
})
</script>
