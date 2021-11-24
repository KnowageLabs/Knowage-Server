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

        <div class="p-fluid kn-parameter-sidebar-content">
            <div v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <!-- Manual Text/Number Input -->
                <div class="p-field p-m-4" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
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
                        @input="updateVisualDependency(parameter)"
                    />
                </div>

                <!-- Date -->
                <div class="p-field p-m-4" v-if="parameter.type === 'DATE' && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
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
                        @change="updateVisualDependency(parameter)"
                        @date-select="updateVisualDependency(parameter)"
                    />
                </div>

                <!-- TODO: NEMA! Date Range -->

                <!-- LOV list radio/multiple input -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'LIST' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-parameter-label-error': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0].value) || (parameter.multivalue && parameter.parameterValue.length === 0))
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-column">
                        <div class="p-field-radiobutton" v-for="(defaultParameter, index) in parameter.defaultValues" :key="index">
                            <RadioButton v-if="!parameter.multivalue && defaultParameter.isEnabled" :value="defaultParameter.value" v-model="parameter.parameterValue[0].value" @change="updateVisualDependency(parameter)" />
                            <Checkbox v-if="parameter.multivalue && defaultParameter.isEnabled" :value="defaultParameter.value" v-model="selectedParameterCheckbox[parameter.id]" @change="setCheckboxValue(parameter)" />
                            <label>{{ defaultParameter.label }}</label>
                        </div>
                    </div>
                </div>

                <!-- LOV combobox single and multiple input -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'COMBOBOX' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-parameter-label-error': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue.value) || (parameter.multivalue && parameter.parameterValue.length === 0))
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Dropdown v-if="!parameter.multivalue" class="kn-material-input" v-model="parameter.parameterValue[0]" :options="getParameterDropdownOptions(parameter)" optionLabel="label" @change="updateVisualDependency(parameter)" />
                    <MultiSelect v-else v-model="parameter.parameterValue" :options="getParameterDropdownOptions(parameter)" optionLabel="label" @change="updateVisualDependency(parameter)" />
                </div>

                <!-- POP UP -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-parameter-label-error': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0))
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openPopupDialog(parameter)"></i>
                        <div>
                            <!-- <Chip v-if="!parameter.multivalue">{{ parameter.parameterValue[0].value }}</Chip>
                            <template v-else> -->
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.value }}</Chip>
                            <!-- </template> -->
                        </div>
                    </div>
                </div>

                <!-- Tree -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-parameter-label-error': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0))
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openTreeDialog(parameter)"></i>
                        <div>
                            <!-- <Chip v-if="!parameter.multivalue">{{ parameter.parameterValue[0].value }}</Chip>
                            <template v-else> -->
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.value }}</Chip>
                            <!-- </template> -->
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="p-fluid p-d-flex p-flex-row p-m-5 kn-parameter-sidebar-buttons">
            <Button class="kn-button kn-button--primary" :disabled="buttonsDisabled" @click="$emit('execute')"> {{ $t('common.execute') }}</Button>
            <Button class="kn-button kn-button--primary" icon="fa fa-chevron-down" :disabled="buttonsDisabled" @click="toggle($event)" />
            <Menu ref="menu" :model="executeMenuItems" :popup="true" />
        </div>

        <KnParameterPopupDialog :visible="popupDialogVisible" :selectedParameter="selectedParameter" :propLoading="loading" :parameterPopUpData="parameterPopUpData" @close="popupDialogVisible = false" @save="onPopupSave"></KnParameterPopupDialog>
        <KnParameterTreeDialog :visible="treeDialogVisible" :selectedParameter="selectedParameter" :formatedParameterValues="formatedParameterValues" :document="document" @close="onTreeClose" @save="onTreeSave"></KnParameterTreeDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Calendar from 'primevue/calendar'
import Chip from 'primevue/chip'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import KnParameterPopupDialog from './dialogs/KnParameterPopupDialog.vue'
import KnParameterTreeDialog from './dialogs/KnParameterTreeDialog.vue'
import Menu from 'primevue/menu'
import MultiSelect from 'primevue/multiselect'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: { Calendar, Chip, Checkbox, Dropdown, KnParameterPopupDialog, KnParameterTreeDialog, Menu, MultiSelect, RadioButton },
    props: { filtersData: { type: Object }, propDocument: { type: Object } },
    emits: ['execute', 'exportCSV'],
    data() {
        return {
            document: null as any,
            parameters: { isReadyForExecution: false, filterStatus: [] } as any,
            executeMenuItems: [] as any[],
            selectedParameterCheckbox: {} as any,
            popupDialogVisible: false,
            selectedParameter: null as any,
            parameterPopUpData: null as any,
            treeDialogVisible: false,
            formatedParameterValues: null as any,
            loading: false
        }
    },
    watch: {
        filtersData() {
            this.loadDocument()
            this.loadParameters()
        }
    },
    computed: {
        buttonsDisabled(): boolean {
            return this.requiredFiledMissing()
        }
    },
    created() {
        this.loadDocument()
        this.loadParameters()
    },
    methods: {
        loadDocument() {
            this.document = this.propDocument
        },
        loadParameters() {
            this.parameters.isReadyForExecution = this.filtersData?.isReadyForExecution
            this.parameters.filterStatus = []

            this.filtersData?.filterStatus.forEach((el: any) => {
                if (el.selectionType == 'LIST' && el.showOnPanel == 'true' && el.multivalue) {
                    this.selectedParameterCheckbox[el.id] = el.parameterValue?.map((parameterValue: any) => parameterValue.value)
                } else if (el.selectionType == 'COMBOBOX' && el.showOnPanel == 'true') {
                    el.multivalue ? this.setSelectedMultivalueCombobox(el) : this.setSelectedCombobox(el)
                }
                this.parameters.filterStatus.push(el)
            })

            this.parameters?.filterStatus.forEach((el: any) => this.setVisualDependency(el))
            this.parameters?.filterStatus.forEach((el: any) => this.updateVisualDependency(el))
        },
        setVisualDependency(parameter: any) {
            if (parameter.visualDependencies.length !== 0) {
                parameter.visualDependencies.forEach((dependency: any) => {
                    const index = this.parameters.filterStatus.findIndex((param: any) => {
                        return param.urlName === dependency.parFatherUrlName
                    })
                    if (index !== -1) {
                        const tempParameter = this.parameters.filterStatus[index]
                        parameter.dependensToParameters ? parameter.dependensToParameters.push(tempParameter) : (parameter.dependensToParameters = [tempParameter])
                        tempParameter.dependentParameters ? tempParameter.dependentParameters.push(parameter) : (tempParameter.dependentParameters = [parameter])
                    }
                })
            }
        },
        setSelectedCombobox(parameter: any) {
            const index = parameter.defaultValues.findIndex((el: any) => {
                return el.value === parameter.parameterValue[0]?.value
            })
            if (index !== -1) parameter.parameterValue = [parameter.defaultValues[index]]
        },
        setSelectedMultivalueCombobox(parameter: any) {
            const formatedValues = [] as any[]
            parameter.parameterValue?.forEach((parameterValue: any) => {
                const index = parameter.defaultValues?.findIndex((el: any) => {
                    return el.value === parameterValue.value
                })
                if (index !== -1) formatedValues.push(parameter.defaultValues[index])
            })
            parameter.parameterValue = formatedValues
        },
        resetParameterValue(parameter: any) {
            if (parameter.selectionType === 'LIST' && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue
                this.selectedParameterCheckbox[parameter.id] = parameter.driverDefaultValue?.map((parameterValue: any) => parameterValue.value)
            } else if (parameter.selectionType === 'COMBOBOX' && parameter.showOnPanel === 'true') {
                parameter.multivalue ? this.resetParameterComboboxMulti(parameter) : this.resetParameterCombobox(parameter)
            } else if ((parameter.selectionType === 'LOOKUP' || parameter.selectionType === 'TREE') && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue
            } else {
                if (!parameter.parameterValue[0]) {
                    parameter.parameterValue[0] = { value: '', description: '' }
                }
                parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value
            }
        },
        resetParameterCombobox(parameter: any) {
            const index = parameter.defaultValues.findIndex((el: any) => {
                return el.value === parameter.driverDefaultValue[0].value
            })
            if (index !== -1) parameter.parameterValue = [parameter.defaultValues[index]]
        },
        resetParameterComboboxMulti(parameter: any) {
            const formatedValues = [] as any[]
            parameter.driverDefaultValue?.forEach((parameterValue: any) => {
                const index = parameter.defaultValues?.findIndex((el: any) => {
                    return el.value === parameterValue.value
                })
                if (index !== -1) formatedValues.push(parameter.defaultValues[index])
            })
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
                const parameter = this.parameters.filterStatus[i]

                if (parameter.mandatory && parameter.showOnPanel == 'true') {
                    if (!parameter.parameterValue || parameter.parameterValue.length === 0) {
                        return true
                    } else {
                        parameter.parameterValue.forEach((el: any) => {
                            if (!el.value) return true
                        })
                    }
                }
            }

            return false
        },
        setCheckboxValue(parameter: any) {
            parameter.parameterValue = this.selectedParameterCheckbox[parameter.id].map((el: any) => {
                return { value: el, description: el }
            })
            this.updateVisualDependency(parameter)
        },
        getParameterDropdownOptions(parameter: any) {
            return parameter.defaultValues.filter((el: any) => el.isEnabled)
        },
        openPopupDialog(parameter: any) {
            this.selectedParameter = parameter
            this.getParameterPopupInfo(parameter)
            this.popupDialogVisible = true
        },
        openTreeDialog(parameter: any) {
            this.selectedParameter = parameter
            this.formatedParameterValues = this.getFormatedParameters()
            this.treeDialogVisible = true
        },
        onTreeClose() {
            this.selectedParameter = null
            this.formatedParameterValues = null
            this.treeDialogVisible = false
        },
        async getParameterPopupInfo(parameter: any) {
            this.loading = true
            // TODO: user role? videti, nije odgovorio jos
            const postData = { MODE: 'extra', OBJECT_LABEL: this.document.label, PARAMETERS: this.getFormatedParameters(), PARAMETER_ID: parameter.urlName, ROLE: (this.$store.state as any).user.defaultRole }
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentExeParameters/getParameters`, postData).then((response: AxiosResponse<any>) => (this.parameterPopUpData = response.data))
            this.loading = false
        },
        getFormatedParameters() {
            let PARAMETERS = {}

            Object.keys(this.parameters.filterStatus).forEach((key: any) => {
                const parameter = this.parameters.filterStatus[key]

                // TODO srediti popup-single i tree
                if (parameter.valueSelection === 'man_in') {
                    PARAMETERS[parameter.urlName] = parameter.type === 'NUM' ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                    PARAMETERS[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].description
                } else if (parameter.multivalue) {
                    let tempArrayValue = [] as any[]
                    let tempArrayDescription = [] as any[]
                    for (let i = 0; i < parameter.parameterValue.length; i++) {
                        tempArrayValue.push(parameter.parameterValue[i].value)
                        tempArrayDescription.push(parameter.parameterValue[i].description)
                    }

                    PARAMETERS[parameter.urlName] = tempArrayValue
                    PARAMETERS[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].description
                } else if (parameter.type === 'DATE') {
                    PARAMETERS[parameter.urlName] = parameter.parameterValue[0].value
                    PARAMETERS[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].value
                } else {
                    PARAMETERS[parameter.urlName] = parameter.parameterValue[0] ? parameter.parameterValue[0].value : parameter.parameterValue.value
                    PARAMETERS[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0] ? parameter.parameterValue[0].description : parameter.parameterValue.description
                }
            })

            return PARAMETERS
        },
        onPopupSave(parameter: any) {
            this.updateVisualDependency(parameter)
            this.popupDialogVisible = false
        },
        onTreeSave(parameter: any) {
            this.updateVisualDependency(parameter)
            this.treeDialogVisible = false
        },
        updateVisualDependency(parameter: any) {
            // console.log('PARAMETER FOR VISUAL UPDATE: ', parameter)

            parameter.dependentParameters?.forEach((dependentParameter: any) => {
                // console.log('DEPENEDENT PARAM: ', dependentParameter)

                this.visualDependencyCheck(dependentParameter)

                // console.log('DEPENEDEN AFTER: ', dependentParameter)
            })
        },
        visualDependencyCheck(parameter: any) {
            // console.log(' >>> VISUAL DEP CHECK: ', parameter)

            let showOnPanel = 'true'
            for (let i = 0; i < parameter.visualDependencies.length && showOnPanel === 'true'; i++) {
                showOnPanel = 'false'
                const visualDependency = parameter.visualDependencies[i]

                const index = parameter.dependensToParameters.findIndex((el: any) => el.urlName === visualDependency.parFatherUrlName)
                const parentParameter = parameter.dependensToParameters[index]

                // console.log(' >>>>> INDEX:', index)
                // console.log(' >>>>> PARENT PARAMETER:', parentParameter)

                if (visualDependency.operation === 'contains') {
                    // if (Array.isArray(parentParameter.parameterValue)) {
                    for (let i = 0; i < parentParameter.parameterValue.length; i++) {
                        if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
                            // console.log(' >>>> ENTERED', parentParameter.parameterValue[i].value, ' === ', visualDependency.compareValue)
                            showOnPanel = 'true'
                            break
                        }
                    }
                    // } else {
                    //     console.log(' >>>>>>>> ENTERED FOR SINGLE VALUE', parentParameter.parameterValue.value)
                    //     console.log(' >>>>>>>> ENTERED FOR SINGLE VALUE COMPARE', visualDependency.compareValue)

                    //     if (parentParameter.parameterValue.value === visualDependency.compareValue) {
                    //         console.log(' >>>> ENTERED', parentParameter.parameterValue.value, ' === ', visualDependency.compareValue)
                    //         showOnPanel = 'true'
                    //     }
                    // }
                } else if (visualDependency.operation === 'not contains') {
                    for (let i = 0; i < parentParameter.parameterValue.length; i++) {
                        if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
                            showOnPanel = 'false'
                            break
                        }
                    }
                }

                // console.log('SHOW ON PANEL ITERATION ', i, showOnPanel)
            }

            parameter.showOnPanel = showOnPanel
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
    position: relative;
}

.parameter-clear-icon {
    margin-left: auto;
}

.kn-parameter-sidebar-content {
    height: 80vh;
    overflow: auto;
}

// .kn-parameter-sidebar-buttons {
//     margin-top: auto;
// }

.kn-parameter-label-error {
    color: red !important;
}
</style>
