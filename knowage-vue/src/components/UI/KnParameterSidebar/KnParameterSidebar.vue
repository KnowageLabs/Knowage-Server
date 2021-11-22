<template>
    <div id="kn-parameter-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                <div id="kn-parameter-sidebar-toolbar-icons-container" class="p-d-flex p-flex-row p-jc-around">
                    <i class="fa fa-eraser kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.resetParametersTooltip')"></i>
                    <i class="pi pi-pencil kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.savedParametersTooltip')"></i>
                    <i class="fas fa-save kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.saveParametersFromStateTooltip')"></i>
                </div>
            </template>
        </Toolbar>

        <div class="p-fluid p-m-4">
            <div v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <!-- Manual Text/Number Input -->
                <div class="p-field" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <InputText
                        class="kn-material-input p-inputtext-sm"
                        :type="parameter.type === 'NUM' ? 'number' : 'text'"
                        v-model="parameter.parameterValue[0].value"
                        :class="{
                            'p-invalid': parameter.mandatory && !parameter.parameterValue[0].value
                        }"
                    />
                </div>

                <!-- Date -->
                <div class="p-field" v-if="parameter.type === 'DATE' && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Calendar
                        v-model="parameter.parameterValue[0].value"
                        :showButtonBar="true"
                        :showIcon="true"
                        :manualInput="true"
                        :class="{
                            'p-invalid': parameter.mandatory && !parameter.parameterValue[0].value
                        }"
                    />
                </div>

                <!-- TODO: NEMA! Date Range -->

                <!-- LOV list radio/multiple input -->
                <div class="p-field" v-if="parameter.selectionType == 'LIST' && parameter.showOnPanel == 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-column">
                        <div class="p-field-radiobutton" v-for="(defaultParameter, index) in parameter.defaultValues" :key="index">
                            <RadioButton v-if="!parameter.multivalue && defaultParameter.isEnabled" :value="defaultParameter.value" v-model="parameter.parameterValue[0].value" />
                            <Checkbox v-if="parameter.multivalue && defaultParameter.isEnabled" :value="defaultParameter.value" v-model="selectedParameterCheckbox[parameter.id]" @change="setCheckboxValue(parameter)" />
                            <label>{{ defaultParameter.label }}</label>
                        </div>
                    </div>
                </div>

                <!-- Map input NE RADI!-->
                <!-- <div class="p-field" v-if="parameter.typeCode == 'MAN_IN' && parameter.valueSelection == 'map_in' && parameter.showOnPanel == 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label"><span v-if="!parameter.showMapDriver" v-tooltip.left="$t('documentExecution.main.mapParameterDisabled')"></span>{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-column"></div>
                </div> -->

                <!-- LOV combobox single and multiple input -->
                <div class="p-field" v-if="parameter.selectionType == 'COMBOBOX' && parameter.showOnPanel == 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Dropdown v-if="!parameter.multivalue" class="kn-material-input" v-model="parameter.parameterValue" :options="getParameterDropdownOptions(parameter)" optionLabel="label" />
                    <MultiSelect v-else v-model="parameter.parameterValue" :options="getParameterDropdownOptions(parameter)" optionLabel="label" @change="test(parameter)" />
                </div>
            </div>
        </div>
        <div class="p-fluid p-d-flex p-flex-row p-m-2">
            <Button class="kn-button kn-button--primary" :disabled="buttonsDisabled" @click="$emit('execute')"> {{ $t('common.execute') }}</Button>
            <Button class="kn-button kn-button--primary" icon="fa fa-chevron-down" :disabled="buttonsDisabled" @click="toggle($event)" />
            <Menu ref="menu" :model="executeMenuItems" :popup="true" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'
import MultiSelect from 'primevue/multiselect'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: { Calendar, Checkbox, Dropdown, Menu, MultiSelect, RadioButton },
    props: { filtersData: { type: Object } },
    emits: ['execute', 'exportCSV'],
    data() {
        return {
            parameters: { isReadyForExecution: false, filterStatus: [] } as any,
            executeMenuItems: [] as any[],
            selectedParameterCheckbox: {} as any
        }
    },
    watch: {
        filtersData() {
            this.loadParameters()
        }
    },
    computed: {
        buttonsDisabled(): boolean {
            return this.requiredFiledMissing()
        }
    },
    created() {
        this.loadParameters()
    },
    methods: {
        loadParameters() {
            this.parameters.isReadyForExecution = this.filtersData?.isReadyForExecution
            this.parameters.filterStatus = []

            this.filtersData?.filterStatus.forEach((el: any) => {
                // console.log('LOADEING EL: ', el)
                if (el.selectionType == 'LIST' && el.showOnPanel == 'true' && el.multivalue) {
                    // console.log('LOADED SELECTED EL: ', el)
                    this.selectedParameterCheckbox[el.id] = el.parameterValue?.map((parameterValue: any) => parameterValue.value)
                } else if (el.selectionType == 'COMBOBOX' && el.showOnPanel == 'true') {
                    el.multivalue ? this.setSelectedMultivalueCombobox(el) : this.setSelectedCombobox(el)
                }
                this.parameters.filterStatus.push(el)
            })
            console.log('LOADED PARAMETERS: ', this.parameters)
            // console.log('LOADED SELECTED: ', this.selectedParameterCheckbox)
        },
        setSelectedCombobox(parameter: any) {
            // console.log('SET SELECTED COMBO', parameter)
            const index = parameter.defaultValues.findIndex((el: any) => {
                //console.log(el.value + ' === ' + parameter.parameterValue[0].value)
                return el.value === parameter.parameterValue[0]?.value
            })
            // console.log('INDEX: ', index)
            if (index !== -1) parameter.parameterValue = parameter.defaultValues[index]
        },
        setSelectedMultivalueCombobox(parameter: any) {
            // console.log('SET SELECTED MULTIVALUE COMBO', parameter)
            const formatedValues = [] as any[]
            parameter.parameterValue?.forEach((parameterValue: any) => {
                // console.log('PARAMETER: ', parameterValue)
                const index = parameter.defaultValues?.findIndex((el: any) => {
                    // console.log('EEEEEEEEEL', el)
                    return el.value === parameterValue.value
                })
                if (index !== -1) formatedValues.push(parameter.defaultValues[index])
            })
            // console.log('FORMATED VALUES: ', formatedValues)
            parameter.parameterValue = formatedValues
        },
        resetParameterValue(parameter: any) {
            console.log('RESET PARAMETER VALUE BEFORE: ', parameter)
            if (parameter.selectionType == 'LIST' && parameter.showOnPanel == 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue
                this.selectedParameterCheckbox[parameter.id] = parameter.driverDefaultValue?.map((parameterValue: any) => parameterValue.value)
            } else if (parameter.selectionType == 'COMBOBOX' && parameter.showOnPanel == 'true') {
                parameter.multivalue ? this.resetParameterComboboxMulti(parameter) : this.resetParameterCombobox(parameter)
            } else {
                parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value
            }
            console.log('RESET PARAMETER VALUE AFTER: ', parameter)
        },
        resetParameterCombobox(parameter: any) {
            console.log('RESET COMBOBOX PARAMETER: ', parameter)
            const index = parameter.defaultValues.findIndex((el: any) => {
                console.log(el.value + ' === ' + parameter.driverDefaultValue[0].value)
                return el.value === parameter.driverDefaultValue[0].value
            })
            if (index !== -1) parameter.parameterValue = parameter.defaultValues[index]
        },
        resetParameterComboboxMulti(parameter: any) {
            console.log('RESET COMBOBOX MULTI PARAMETER: ', parameter)
            const formatedValues = [] as any[]
            parameter.driverDefaultValue?.forEach((parameterValue: any) => {
                const index = parameter.defaultValues?.findIndex((el: any) => {
                    return el.value === parameterValue.value
                })
                if (index !== -1) formatedValues.push(parameter.defaultValues[index])
            })
            console.log('FORMATED VALUES RESET: ', formatedValues)
            parameter.parameterValue = formatedValues
        },
        toggle(event: any) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.executeMenuItems = []
            this.executeMenuItems.push({ label: this.$t('common.exportCSV'), command: () => this.$emit('exportCSV') })
        },
        requiredFiledMissing() {
            for (let i = 0; i < this.parameters.filterStatus.length; i++) {
                // console.log('TEST: ', this.parameters.filterStatus[i])
                const parameter = this.parameters.filterStatus[i]
                if (parameter.selectionType === 'LIST' && parameter.showOnPanel == 'true' && parameter.multivalue && parameter.parameterValue.length === 0) {
                    console.log('ENTERED 1', parameter)
                    return true
                } else if (parameter.selectionType !== 'LIST' && parameter.parameterValue && !parameter.parameterValue[0]?.value) {
                    console.log('ENTERED 2', parameter)
                    return true
                }
            }

            return false
        },
        setCheckboxValue(parameter: any) {
            console.log('parameter', parameter)

            // console.log('selectedParameterCheckbox', this.selectedParameterCheckbox)

            parameter.parameterValue = this.selectedParameterCheckbox[parameter.id]

            console.log('parameter after', parameter)
        },
        getParameterDropdownOptions(parameter: any) {
            return parameter.defaultValues.filter((el: any) => el.isEnabled)
        },
        test(parameter: any) {
            console.log('PARAMETER AFTER: ', parameter)
        }
    }
})
</script>

<style lang="scss">
#kn-parameter-sidebar-toolbar .p-toolbar-group-left {
    width: 100%;
}

#kn-parameter-sidebar-toolbar-icons-container {
    width: 100%;
}

#kn-parameter-sidebar {
    z-index: 150;
    background-color: white;
    height: 100%;
    width: 350px;
}

.parameter-clear-icon {
    margin-left: auto;
}
</style>
