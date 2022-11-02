<template>
    <div id="variables-container" class="p-grid p-p-4 p-m-0 kn-overflow">
        {{ selectedDatasetOptions }}
        <br />

        <div class="p-col-12">
            <label class="kn-material-input-label"> {{ $t('common.variables') }}</label>
        </div>
        <div class="p-col-6 p-m-auto">
            <KnHint class="p-as-center" :title="'common.variables'" :hint="'dashboard.generalSettings.variablesHint'"></KnHint>
        </div>
        <div class="p-col-11"></div>
        <div class="p-col-1 p-pt-2">
            <KnFabButton icon="fas fa-plus" @click="addNewVariable()"></KnFabButton>
        </div>
        <div v-for="(variable, index) in variables" :key="index" class="p-grid p-col-12 p-ai-center p-p-2">
            <div class="p-col-12">
                {{ variable }}
            </div>
            <div class="p-col-3 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('common.name') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="variable.name" />
            </div>
            <div class="p-col-2 p-d-flex p-flex-column">
                <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                <Dropdown class="kn-material-input" v-model="variable.type" :options="descriptor.variableTypes" optionValue="value" @change="onVariableTypeChange(variable)">
                    <template #value="slotProps">
                        <div>
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.variableTypes, $t) }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>

            <div v-if="variable.type === 'static'" class="p-col-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="variable.value" />
            </div>
            <div v-if="variable.type === 'dataset'" class="p-col-6 p-grid">
                <div class="p-col-6 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                    <Dropdown class="kn-material-input" v-model="variable.dataset" :options="selectedDatasetOptions" optionLabel="label" optionValue="id"> </Dropdown>
                </div>
                <div class="p-col-6 p-d-flex p-flex-column">
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                    <Dropdown class="kn-material-input" v-model="variable.column" :options="getSelectionDatasetColumnOptions(variable)"> </Dropdown>
                    <small>{{ $t('dashboard.generalSettings.variableColumnHint') }}</small>
                </div>
            </div>
            <div v-if="variable.type === 'driver'" class="p-col-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.interactions.analyticalDriver') }}</label>
                <Dropdown class="kn-material-input" v-model="variable.driver" :options="drivers" optionLabel="name" optionValue="urlName" @change="setValueFromAnalyticalDriver(variable)"> </Dropdown>
            </div>
            <div v-if="variable.type === 'profile'" class="p-col-6 p-d-flex p-flex-column">
                <label class="kn-material-input-label">{{ $t('dashboard.generalSettings.attribute') }}</label>
                <Dropdown class="kn-material-input" v-model="variable.attribute" :options="profileAttributes" optionLabel="name" optionValue="name" @change="setValueFromProfileAttribute(variable)"> </Dropdown>
            </div>

            <div class="p-col-1 p-d-flex p-flex-column p-jc-center p-ai-center p-pl-2 p-ml-auto">
                <i :class="'pi pi-trash'" class="kn-cursor-pointer p-ml-2" @click="removeVariable(index)"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IDataset } from '@/modules/documentExecution/dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import KnHint from '@/components/UI/KnHint.vue'
import descriptor from './DashboardGeneralSettingsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'dashboard-variables',
    components: { KnHint, Dropdown, KnFabButton },
    props: {
        propVariables: { type: Array as PropType<IVariable[]>, required: true },
        selectedDatasets: { type: Array as PropType<IDataset[]>, required: true },
        selectedDatasetsColumnsMap: { type: Object, required: true },
        drivers: { type: Array as PropType<any[]>, required: true },
        profileAttributes: { type: Array as PropType<{ name: string; value: string }[]>, required: true }
    },
    data() {
        return {
            descriptor,
            variables: [] as IVariable[],
            selectedDatasetOptions: [] as { id: number; label: string }[],
            getTranslatedLabel
        }
    },
    watch: {
        propVariables() {
            this.loadVariables()
        }
    },
    created() {
        console.log('selectedDatasetOptions: ', this.selectedDatasetOptions)
        this.loadVariables()
        this.loadSelectedDatasetNames()
    },
    methods: {
        loadVariables() {
            this.variables = this.propVariables
        },
        loadSelectedDatasetNames() {
            if (!this.selectedDatasetsColumnsMap) return
            Object.keys(this.selectedDatasetsColumnsMap).forEach((key: string) => this.selectedDatasetOptions.push({ id: +key, label: this.selectedDatasetsColumnsMap[key].name }))
        },
        getSelectionDatasetColumnOptions(variable: IVariable) {
            return variable.dataset && this.selectedDatasetsColumnsMap ? [''].concat(this.selectedDatasetsColumnsMap[variable.dataset].columns) : []
        },
        onVariableTypeChange(variable: IVariable) {
            variable.value = ''
            switch (variable.type) {
                case 'static':
                    this.deleteVariableFields(['dataset', 'column', 'attribute', 'driver'], variable)
                    break
                case 'dataset':
                    this.deleteVariableFields(['dataset', 'column'], variable)
                    break
                case 'driver':
                    this.deleteVariableFields(['dataset', 'column', 'attribute'], variable)
                    break
                case 'profile':
                    this.deleteVariableFields(['dataset', 'column', 'driver'], variable)
            }
        },
        deleteVariableFields(fields: string[], variable: IVariable) {
            fields.forEach((field: string) => delete variable[field])
        },
        setValueFromAnalyticalDriver(variable: IVariable) {
            const index = this.drivers.findIndex((driver: any) => driver.urlName === variable.driver)
            variable.value = index !== -1 ? this.drivers[index].value : ''
        },
        setValueFromProfileAttribute(variable: IVariable) {
            const index = this.profileAttributes.findIndex((profileAttribute: { name: string; value: string }) => profileAttribute.name === variable.attribute)
            variable.value = index !== -1 ? this.profileAttributes[index].value : ''
        },
        addNewVariable() {
            this.variables.push({ name: '', type: '', value: '' })
        },
        removeVariable(index: number) {
            this.variables.splice(index, 1)
        }
    }
})
</script>

<style lang="scss" scoped>
#variables-container {
    box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
}
</style>
