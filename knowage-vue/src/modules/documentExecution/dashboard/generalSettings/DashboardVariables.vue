<template>
    <div class="p-d-flex p-flex-column kn-flex p-mr-3 p-my-3 dashboard-card-shadow kn-overflow dashboard-scrollbar">
        <KnFabButton icon="fas fa-plus" class="p-as-end" style="position: absolute; right: 10px" @click="addNewVariable()"></KnFabButton>
        <label class="kn-material-input-label p-m-3"> {{ $t('common.variables') }}</label>

        <KnHint v-if="variables.length == 0" class="p-as-center" :title="'common.variables'" :hint="'dashboard.generalSettings.variablesHint'"></KnHint>

        <div v-for="(variable, index) in variables" :key="index" class="p-fluid p-formgrid p-grid p-m-3" style="gap: 5px">
            <div class="p-field kn-flex">
                <span class="p-float-label">
                    <InputText v-model="variable.name" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('common.name') }}</label>
                </span>
            </div>

            <div class="p-field kn-flex">
                <span class="p-float-label">
                    <Dropdown v-model="variable.type" class="kn-material-input" :options="descriptor.variableTypes" option-value="value" @change="onVariableTypeChange(variable)">
                        <template #value="slotProps">
                            <span>{{ getTranslatedLabel(slotProps.value, descriptor.variableTypes, $t) }}</span>
                        </template>
                        <template #option="slotProps">
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </template>
                    </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                </span>
            </div>

            <div v-if="variable.type === 'static'" class="p-field kn-flex">
                <span class="p-float-label">
                    <InputText v-model="variable.value" class="kn-material-input" />
                    <label class="kn-material-input-label">{{ $t('common.value') }}</label>
                </span>
            </div>

            <div v-if="variable.type === 'dataset'" class="p-field kn-flex">
                <span class="p-float-label">
                    <Dropdown v-model="variable.dataset" class="kn-material-input" :options="selectedDatasetOptions" option-label="label" option-value="id"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.dataset') }}</label>
                </span>
            </div>
            <div v-if="variable.type === 'dataset'" class="p-field kn-flex">
                <span class="p-float-label">
                    <Dropdown v-model="variable.column" class="kn-material-input" :options="getSelectionDatasetColumnOptions(variable)"> </Dropdown>
                    <label class="kn-material-input-label"> {{ $t('common.column') }}</label>
                </span>
                <small>{{ $t('dashboard.generalSettings.variableColumnHint') }}</small>
            </div>

            <div v-if="variable.type === 'driver'" class="p-field kn-flex">
                <span class="p-float-label">
                    <Dropdown v-model="variable.driver" class="kn-material-input" :options="drivers" option-label="name" option-value="urlName" @change="setValueFromAnalyticalDriver(variable)"> </Dropdown>
                    <label class="kn-material-input-label">{{ $t('dashboard.widgetEditor.interactions.analyticalDriver') }}</label>
                </span>
            </div>

            <div v-if="variable.type === 'profile'" class="p-field kn-flex">
                <span class="p-float-label">
                    <Dropdown v-model="variable.attribute" class="kn-material-input" :options="profileAttributes" option-label="name" option-value="name" @change="setValueFromProfileAttribute(variable)"> </Dropdown>
                    <label class="kn-material-input-label">{{ $t('dashboard.generalSettings.attribute') }}</label>
                </span>
            </div>

            <Button v-tooltip.left="$t('common.delete')" icon="fas fa-trash-alt" class="p-button-text p-button-rounded p-button-plain p-mt-1" @click="removeVariable(index)" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IVariable, IDataset, IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { getTranslatedLabel } from '@/helpers/commons/dropdownHelper'
import { mapActions } from 'pinia'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
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
        profileAttributes: { type: Array as PropType<{ name: string; value: string }[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    data() {
        return {
            descriptor,
            variables: [] as IVariable[],
            selectedDatasetOptions: [] as { id: number; label: string }[],
            drivers: [] as IDashboardDriver[],
            getTranslatedLabel
        }
    },
    watch: {
        propVariables() {
            this.loadVariables()
        }
    },
    created() {
        this.loadVariables()
        this.loadSelectedDatasetNames()
    },
    methods: {
        ...mapActions(dashboardStore, ['getDashboardDrivers']),
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId)
        },
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
