<template>
    <div id="kn-parameter-sidebar" :class="positionClass">
        <Toolbar v-if="mode !== 'workspaceView' && mode !== 'qbeView' && mode !== 'datasetManagement'" id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                <div id="kn-parameter-sidebar-toolbar-icons-container" class="p-d-flex p-flex-row p-jc-around">
                    <Button icon="fa fa-eraser" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.top="$t('documentExecution.main.resetParametersTooltip')" @click="resetAllParameters"></Button>
                    <Button icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.top="$t('documentExecution.main.savedParametersTooltip')" @click="openSavedParametersDialog"></Button>
                    <Button icon="fas fa-save" class="p-button-text p-button-rounded p-button-plain p-mx-2" v-tooltip.top="$t('documentExecution.main.saveParametersFromStateTooltip')" @click="openSaveParameterDialog"></Button>
                </div>
            </template>
        </Toolbar>

        <div class="p-fluid kn-parameter-sidebar-content kn-alternated-rows">
            <div class="p-field p-my-1 p-p-2" v-if="user && (!sessionRole || sessionRole === $t('role.defaultRolePlaceholder')) && (mode === 'execution' || dataset?.drivers.length > 0)">
                <div class="p-d-flex">
                    <label class="kn-material-input-label">{{ $t('common.roles') }}</label>
                </div>
                <Dropdown class="kn-material-input" v-model="role" :options="user.roles" @change="setNewSessionRole" />
            </div>

            <template v-if="mode === 'qbeView' || mode === 'workspaceView' || mode === 'datasetManagement'">
                <div v-for="(qbeParameter, index) in qbeParameters" :key="index">
                    <div class="p-field p-m-4">
                        <div class="p-d-flex">
                            <label class="kn-material-input-label">{{ qbeParameter.name }} <span v-if="mode !== 'datasetManagement'"> *</span> </label>
                            <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="qbeParameter.value = qbeParameter.defaultValue"></i>
                        </div>
                        <Chips v-if="qbeParameter.multiValue" v-model="qbeParameter.value" />
                        <InputText
                            v-else
                            class="kn-material-input p-inputtext-sm"
                            v-model="qbeParameter.value"
                            :class="{
                                'p-invalid': !qbeParameter.value && mode !== 'datasetManagement'
                            }"
                        />
                    </div>
                </div>
            </template>

            <template v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <div class="p-field p-my-1 p-p-2" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters }" :data-test="'parameter-input-label-' + parameter.id">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)" :data-test="'parameter-input-clear-' + parameter.id"></i>
                    </div>
                    <InputText
                        v-if="parameter.parameterValue"
                        class="kn-material-input p-inputtext-sm"
                        :type="parameter.type === 'NUM' ? 'number' : 'text'"
                        v-model="parameter.parameterValue[0].value"
                        :class="{
                            'p-invalid': parameter.mandatory && parameter.parameterValue && !parameter.parameterValue[0]?.value
                        }"
                        @input="updateDependency(parameter)"
                        :data-test="'parameter-input-' + parameter.id"
                    />
                </div>
                <div class="p-field p-my-1 p-p-2" v-if="parameter.type === 'DATE' && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters }" :data-test="'parameter-date-label-' + parameter.id">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)" :data-test="'parameter-date-clear-' + parameter.id"></i>
                    </div>
                    <Calendar
                        v-if="parameter.parameterValue"
                        v-model="parameter.parameterValue[0].value"
                        :showButtonBar="true"
                        :showIcon="true"
                        :manualInput="true"
                        class="kn-material-input custom-timepicker"
                        :class="{ 'p-invalid': parameter.mandatory && parameter.parameterValue && !parameter.parameterValue[0]?.value }"
                        @change="updateDependency(parameter)"
                        @date-select="updateDependency(parameter)"
                        :data-test="'parameter-date-input-' + parameter.id"
                    />
                </div>
                <div class="p-field p-my-1 p-p-2" v-if="parameter.selectionType === 'LIST' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters
                            }"
                            :data-test="'parameter-checkbox-label-' + parameter.id"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)" :data-test="'parameter-checkbox-clear-' + parameter.id"></i>
                    </div>
                    <ScrollPanel class="inputScrollPanel">
                        <div class="p-field-radiobutton" v-for="(option, index) in parameter.data" :key="index" :data-test="'parameter-list-' + parameter.id">
                            <RadioButton v-if="!parameter.multivalue && parameter.parameterValue" :value="option.value" v-model="parameter.parameterValue[0].value" @change="setRadioButtonValue(parameter)" />
                            <Checkbox v-if="parameter.multivalue && parameter.parameterValue" :value="option.value" v-model="selectedParameterCheckbox[parameter.id]" @change="setCheckboxValue(parameter)" />
                            <label>{{ option.description }}</label>
                        </div>
                    </ScrollPanel>
                </div>
                <div class="p-field p-my-1 p-p-2" v-if="parameter.selectionType === 'COMBOBOX' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Dropdown v-if="!parameter.multivalue && parameter.parameterValue" class="kn-material-input" v-model="parameter.parameterValue[0]" :options="parameter.data" optionLabel="description" @change="updateDependency(parameter)" />
                    <MultiSelect v-else v-model="parameter.parameterValue" :options="parameter.data" optionLabel="description" @change="updateDependency(parameter)" />
                </div>
                <div class="p-field p-my-1 p-p-2" v-if="parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openPopupDialog(parameter)"></i>
                        <ScrollPanel class="lookupScrollPanel">
                            <Chip class="parameterValueChip" v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.description ?? parameterValue.value }}</Chip>
                        </ScrollPanel>
                    </div>
                </div>
                <div class="p-field p-my-1 p-p-2" v-if="parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters || parameter.lovDependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openTreeDialog(parameter)"></i>
                        <div>
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.description ?? parameterValue.value }}</Chip>
                        </div>
                    </div>
                </div>
            </template>
        </div>
        <div v-if="(parameters && parameters.filterStatus.length > 0) || mode === 'qbeView' || mode === 'workspaceView' || mode === 'datasetManagement'" class="p-fluid p-d-flex p-flex-row p-m-2 kn-parameter-sidebar-buttons">
            <Button class="kn-button kn-button--primary" :disabled="buttonsDisabled" @click="$emit('execute', qbeParameters)"> {{ $t('common.execute') }}</Button>
            <Button v-if="mode !== 'qbeView' && mode !== 'workspaceView'" class="kn-button kn-button--primary p-ml-1" icon="fa fa-chevron-down" :disabled="buttonsDisabled" @click="toggle($event)" />
            <Menu ref="executeButtonMenu" :model="executeMenuItems" :popup="true" />
        </div>
        <KnParameterPopupDialog v-if="popupDialogVisible" :visible="popupDialogVisible" :selectedParameter="selectedParameter" :propLoading="loading" :parameterPopUpData="parameterPopUpData" @close="popupDialogVisible = false" @save="onPopupSave"></KnParameterPopupDialog>
        <KnParameterTreeDialog v-if="treeDialogVisible" :visible="treeDialogVisible" :selectedParameter="selectedParameter" :formatedParameterValues="formatedParameterValues" :document="document" :mode="mode" :selectedRole="role" @close="onTreeClose" @save="onTreeSave"></KnParameterTreeDialog>
        <KnParameterSaveDialog :visible="parameterSaveDialogVisible" :propLoading="loading" @close="parameterSaveDialogVisible = false" @saveViewpoint="saveViewpoint"></KnParameterSaveDialog>
        <KnParameterSavedParametersDialog :visible="savedParametersDialogVisible" :propViewpoints="viewpoints" @close="savedParametersDialogVisible = false" @fillForm="fillParameterForm" @executeViewpoint="executeViewpoint" @deleteViewpoint="deleteViewpoint"></KnParameterSavedParametersDialog>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { formatDate } from '@/helpers/commons/localeHelper'
import { iDocument, iParameter, iAdmissibleValues } from './KnParameterSidebar'
import { setVisualDependency, updateVisualDependency } from './KnParameterSidebarVisualDependency'
import { setDataDependency, updateDataDependency } from './KnParameterSidebarDataDependency'
import { setLovsDependency, updateLovDependency } from './KnParameterSidebarLovsDependency'
import Calendar from 'primevue/calendar'
import Chip from 'primevue/chip'
import Chips from 'primevue/chips'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import KnParameterPopupDialog from './dialogs/KnParameterPopupDialog.vue'
import KnParameterTreeDialog from './dialogs/KnParameterTreeDialog.vue'
import KnParameterSaveDialog from './dialogs/KnParameterSaveDialog.vue'
import KnParameterSavedParametersDialog from './dialogs/KnParameterSavedParametersDialog.vue'
import Menu from 'primevue/menu'
import MultiSelect from 'primevue/multiselect'
import RadioButton from 'primevue/radiobutton'
import ScrollPanel from 'primevue/scrollpanel'
import moment from 'moment'

export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: { Calendar, Chip, Chips, Checkbox, Dropdown, KnParameterPopupDialog, KnParameterTreeDialog, KnParameterSaveDialog, KnParameterSavedParametersDialog, Menu, MultiSelect, RadioButton, ScrollPanel },
    props: { filtersData: { type: Object }, propDocument: { type: Object }, userRole: { type: Object as PropType<String | null> }, propMode: { type: String }, propQBEParameters: { type: Array }, dateFormat: { type: String }, dataset: { type: Object } },
    emits: ['execute', 'exportCSV', 'roleChanged', 'parametersChanged'],
    data() {
        return {
            document: null as iDocument | null,
            parameters: { isReadyForExecution: false, filterStatus: [] } as { filterStatus: iParameter[]; isReadyForExecution: boolean },
            executeMenuItems: [] as { label: string; command: Function }[],
            selectedParameterCheckbox: {} as any,
            popupDialogVisible: false,
            selectedParameter: null as iParameter | null,
            parameterPopUpData: null as iAdmissibleValues | null,
            treeDialogVisible: false,
            formatedParameterValues: null as any,
            parameterSaveDialogVisible: false,
            savedParametersDialogVisible: false,
            viewpoints: [],
            user: null as any,
            role: null as string | null,
            loading: false,
            updateVisualDependency,
            mode: 'execution',
            qbeParameters: [] as any,
            primary: true,
            userDateFormat: '' as string
        }
    },
    watch: {
        sessionRole() {
            this.role = ''
            this.parameters = { isReadyForExecution: false, filterStatus: [] }
        },
        filtersData() {
            this.loadDocument()
            this.loadParameters()
        },
        userRole() {
            this.role = this.userRole as string
        },
        propMode() {
            this.loadMode()
        },
        propQBEParameters() {
            this.loadQBEParameters()
        },
        dateFormat() {
            this.userDateFormat = this.dateFormat as string
        }
    },
    computed: {
        sessionRole(): string {
            return (this.$store.state as any).user.sessionRole
        },
        buttonsDisabled(): boolean {
            return this.requiredFiledMissing()
        },
        positionClass(): string {
            return this.document?.parametersRegion ? 'kn-parameter-sidebar-' + this.document.parametersRegion : 'kn-parameter-sidebar'
        }
    },
    created() {
        this.loadMode()
        if (this.mode === 'qbeView' || this.mode === 'workspaceView' || this.mode === 'datasetManagement') this.loadQBEParameters()

        this.user = (this.$store.state as any).user
        this.role = this.userRole as string
        this.loadDocument()
        this.loadParameters()
        this.userDateFormat = this.dateFormat as string
    },
    methods: {
        applyFieldClass(cssClass: string): string {
            let cssCompleteClass = this.primary ? cssClass + ' fieldBackgroundColorPrimary' : cssClass + ' fieldBackgroundColorSecondary'
            this.primary = !this.primary
            return cssCompleteClass
        },
        setNewSessionRole() {
            this.$emit('roleChanged', this.role)
            this.parameters = { isReadyForExecution: false, filterStatus: [] }
        },
        loadDocument() {
            this.document = this.propDocument as iDocument
        },
        loadParameters() {
            this.parameters.isReadyForExecution = this.filtersData?.isReadyForExecution
            this.parameters.filterStatus = []
            this.filtersData?.filterStatus?.forEach((el: iParameter) => {
                if (el.selectionType == 'LIST' && el.showOnPanel == 'true' && el.multivalue) {
                    this.selectedParameterCheckbox[el.id] = el.parameterValue?.map((parameterValue: any) => parameterValue.value)
                }
                this.parameters.filterStatus.push(el)
            })
            this.parameters?.filterStatus.forEach((el: any) => setVisualDependency(this.parameters, el))
            this.parameters?.filterStatus.forEach((el: any) => setDataDependency(this.parameters, el))
            this.parameters?.filterStatus.forEach((el: any) => setLovsDependency(this.parameters, el))
            this.parameters?.filterStatus.forEach((el: any) => this.updateVisualDependency(el))
        },
        setDataDependency(parameter: iParameter) {
            if (parameter.dependencies.data.length !== 0) {
                parameter.dependencies.data.forEach((dependency: any) => {
                    const index = this.parameters.filterStatus.findIndex((param: any) => {
                        return param.urlName === dependency.parFatherUrlName
                    })
                    if (index !== -1) {
                        const tempParameter = this.parameters.filterStatus[index]
                        parameter.dataDependsOnParameters ? parameter.dataDependsOnParameters.push(tempParameter) : (parameter.dataDependsOnParameters = [tempParameter])
                        tempParameter.dataDependentParameters ? tempParameter.dataDependentParameters.push(parameter) : (tempParameter.dataDependentParameters = [parameter])
                    }
                })
            }
        },
        resetParameterValue(parameter: any) {
            if (!parameter.driverDefaultValue) {
                if (parameter.multivalue) {
                    parameter.parameterValue = []
                    this.selectedParameterCheckbox[parameter.id] = []
                } else {
                    parameter.parameterValue[0] = { value: '', description: '' }
                }
                this.parameters.filterStatus.forEach((el: any) => this.updateDependency(el))
                return
            }

            const valueColumn = parameter.metadata.valueColumn
            const descriptionColumn = parameter.metadata.descriptionColumn
            let valueIndex = null as any
            if (parameter.metadata.colsMap) {
                valueIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === valueColumn)
            }
            let descriptionIndex = null as any
            if (parameter.metadata.colsMap) {
                descriptionIndex = Object.keys(parameter.metadata.colsMap).find((key: string) => parameter.metadata.colsMap[key] === descriptionColumn)
            }
            if ((parameter.selectionType === 'LIST' || parameter.selectionType === 'COMBOBOX') && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = [] as { value: string; description: string }[]
                this.selectedParameterCheckbox[parameter.id] = []
                for (let i = 0; i < parameter.driverDefaultValue.length; i++) {
                    const temp = parameter.driverDefaultValue[i]
                    parameter.parameterValue.push({ value: valueIndex ? temp[valueIndex] : '', description: descriptionIndex ? temp[descriptionIndex] : '' })
                    if (valueIndex) {
                        this.selectedParameterCheckbox[parameter.id].push(temp[valueIndex])
                    }
                }
            } else if (parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue?.map((el: { value: string; desc: string }) => {
                    return { value: el.value, description: el.desc }
                })
            } else if (parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true' && !parameter.multivalue) {
                parameter.parameterValue[0] = { value: parameter.driverDefaultValue[0].value, description: parameter.driverDefaultValue[0].desc }
            } else if ((parameter.selectionType === 'COMBOBOX' || parameter.selectionType === 'LOOKUP') && parameter.showOnPanel === 'true' && !parameter.multivalue) {
                parameter.parameterValue[0] = { value: parameter.driverDefaultValue[0][valueIndex], description: parameter.driverDefaultValue[0][descriptionIndex] }
            } else if (parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue.map((el: any) => {
                    return { value: valueIndex ? el[valueIndex] : '', description: descriptionIndex ? el[descriptionIndex] : '' }
                })
            } else if (parameter.type === 'DATE' && parameter.showOnPanel === 'true') {
                if (parameter.driverDefaultValue[0].desc?.split('#')[0]) {
                    parameter.parameterValue[0].value = this.getFormattedDate(parameter.driverDefaultValue[0].desc?.split('#')[0], undefined)
                }
            } else {
                if (!parameter.parameterValue[0]) {
                    parameter.parameterValue[0] = { value: '', description: '' }
                }
                parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value ?? parameter.driverDefaultValue[0][valueIndex]
            }
            this.parameters.filterStatus.forEach((el: any) => this.updateDependency(el))
        },
        resetAllParameters() {
            this.parameters.filterStatus.forEach((el: any) => this.resetParameterValue(el))
            this.parameters.filterStatus.forEach((el: any) => this.updateDependency(el))
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.executeButtonMenu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.executeMenuItems = []
            this.executeMenuItems.push({ label: this.$t('common.exportCSV'), command: () => this.$emit('exportCSV') })
        },
        requiredFiledMissing() {
            if (this.mode === 'qbeView' || this.mode === 'workspaceView') {
                for (let i = 0; i < this.qbeParameters.length; i++) {
                    if (!this.qbeParameters[i].value) {
                        return true
                    }
                }
            }

            for (let i = 0; i < this.parameters.filterStatus.length; i++) {
                const parameter = this.parameters.filterStatus[i]
                if (parameter.mandatory && parameter.showOnPanel == 'true') {
                    if (!parameter.parameterValue || parameter.parameterValue.length === 0) {
                        return true
                    } else {
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            if (!parameter.parameterValue[i].value) {
                                return true
                            }
                        }
                    }
                }
            }
            return false
        },
        setRadioButtonValue(parameter: iParameter) {
            const index = parameter.data?.findIndex((el: any) => el.value === parameter.parameterValue[0].value)
            if (index !== -1) parameter.parameterValue[0].description = parameter.data[index].description
            this.updateDependency(parameter)
        },
        setCheckboxValue(parameter: iParameter) {
            parameter.parameterValue = this.selectedParameterCheckbox[parameter.id].map((el: any) => {
                return { value: el, description: el }
            })
            this.updateDependency(parameter)
        },
        openPopupDialog(parameter: iParameter) {
            this.selectedParameter = parameter
            this.getParameterPopupInfo(parameter)
            this.popupDialogVisible = true
        },
        openTreeDialog(parameter: iParameter) {
            this.selectedParameter = parameter
            this.formatedParameterValues = this.getFormattedParameters()
            this.treeDialogVisible = true
        },
        onTreeClose() {
            this.selectedParameter = null
            this.formatedParameterValues = null
            this.treeDialogVisible = false
        },
        async getParameterPopupInfo(parameter: iParameter) {
            this.loading = true
            const role = this.sessionRole && this.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.sessionRole : this.role
            const postData = { label: this.document?.label, parameters: this.getFormattedParameters(), paramId: parameter.urlName, role: role }

            let url = '2.0/documentExeParameters/admissibleValues'
            if (this.mode !== 'execution' && this.document) {
                url = this.document.type === 'businessModel' ? `1.0/businessmodel/${this.document.name}/admissibleValues` : `/3.0/datasets/${this.document.label}/admissibleValues`
            }

            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, postData)
                .then((response: AxiosResponse<any>) => (this.parameterPopUpData = response.data))
                .catch((error: any) => console.log('ERROR: ', error))
            this.loading = false
        },
        getFormattedParameters() {
            let parameters = [] as any[]
            Object.keys(this.parameters.filterStatus).forEach((key: any) => {
                const parameter = this.parameters.filterStatus[key]
                if (!parameter.multivalue) {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description ?? '' })
                } else {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue?.map((el: any) => el.value), description: parameter.parameterDescription ?? '' })
                }
            })
            return parameters
        },
        getParameterValues() {
            let parameters = {} as any
            Object.keys(this.parameters.filterStatus).forEach((key: any) => {
                const parameter = this.parameters.filterStatus[key]
                if (parameter.type === 'DATE') {
                    parameters[parameter.urlName] = parameter.parameterValue[0].value
                    parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].value
                } else if (parameter.valueSelection === 'man_in' && !parameter.multivalue) {
                    parameters[parameter.urlName] = parameter.type === 'NUM' ? +parameter.parameterValue[0].value : parameter.parameterValue[0].value
                    parameters[parameter.urlName + '_field_visible_description'] = parameter.type === 'NUM' ? +parameter.parameterValue[0].description : parameter.parameterValue[0].description
                } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                    parameters[parameter.urlName] = parameter.parameterValue.map((el: any) => el.value)
                    let tempString = ''
                    for (let i = 0; i < parameter.parameterValue.length; i++) {
                        tempString += parameter.parameterValue[i].description
                        tempString += i === parameter.parameterValue.length - 1 ? '' : ';'
                    }
                    parameters[parameter.urlName + '_field_visible_description'] = tempString
                } else {
                    parameters[parameter.urlName] = parameter.parameterValue[0].value
                    parameters[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].description
                }
            })
            return parameters
        },
        onPopupSave(parameter: iParameter) {
            this.updateDependency(parameter)
            this.popupDialogVisible = false
        },
        onTreeSave(parameter: iParameter) {
            this.updateVisualDependency(parameter)
            this.treeDialogVisible = false
        },
        updateDependency(parameter: iParameter) {
            const role = this.sessionRole && this.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.sessionRole : this.role
            this.updateVisualDependency(parameter)
            updateDataDependency(this.parameters, parameter, this.loading, this.document, role, this.$http, this.mode)
            updateLovDependency(this.parameters, parameter, this.loading, this.document, role, this.$http, this.mode)
            this.$emit('parametersChanged', { parameters: this.parameters, document: this.propDocument })
        },
        openSaveParameterDialog() {
            this.parameterSaveDialogVisible = true
        },
        async saveViewpoint(viewpoint: any) {
            const role = this.sessionRole && this.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.sessionRole : this.role

            if (!role) return

            const postData = { ...viewpoint, OBJECT_LABEL: this.document?.label, ROLE: role, VIEWPOINT: this.getParameterValues() }
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentviewpoint/addViewpoint`, postData)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.parameterSaveDialogVisible = false
                })
                .catch(() => {})
            this.loading = false
        },
        async openSavedParametersDialog() {
            const role = this.sessionRole && this.sessionRole !== this.$t('role.defaultRolePlaceholder') ? this.sessionRole : this.role
            if (!role) return
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentviewpoint/getViewpoints?label=${this.document?.label}&role=${role}`).then((response: AxiosResponse<any>) => {
                this.viewpoints = response.data.viewpoints
                this.savedParametersDialogVisible = true
            })
            this.loading = false
        },
        fillParameterForm(viewpoint: any) {
            const tempParameters = this.decodeViewpointPrameterValues(viewpoint.vpValueParams)

            Object.keys(tempParameters)?.forEach((key: any) => {
                const index = this.parameters.filterStatus.findIndex((el: any) => el.urlName === key)
                if (index !== -1) {
                    const parameter = this.parameters.filterStatus[index]
                    if (parameter.type === 'DATE') {
                        const temp = new Date(tempParameters[key])
                        parameter.parameterValue[0].value = temp instanceof Date && !isNaN(temp as any) ? this.getFormattedDate(moment(temp).format('DD/MM/YYYY'), 'DD/MM/YYYY') : this.getFormattedDate(tempParameters[key], 'DD/MM/YYYY')
                    } else if ((parameter.valueSelection === 'man_in' || parameter.selectionType === 'COMBOBOX') && !parameter.multivalue) {
                        parameter.parameterValue[0].value = tempParameters[key]
                        parameter.parameterValue[0].description = tempParameters[key + '_field_visible_description']
                    } else if (parameter.selectionType === 'TREE' || parameter.selectionType === 'LOOKUP' || parameter.multivalue) {
                        const tempArrayValues = JSON.parse(tempParameters[key])
                        const tempArrayDescriptions = tempParameters[key + '_field_visible_description'].split(';')
                        parameter.parameterValue = []
                        for (let i = 0; i < tempArrayValues.length; i++) {
                            parameter.parameterValue[i] = { value: tempArrayValues[i], description: tempArrayDescriptions[i] ?? '' }
                        }
                        if (parameter.selectionType === 'LIST') {
                            this.selectedParameterCheckbox[parameter.id] = parameter.parameterValue?.map((parameterValue: any) => parameterValue.value)
                        }
                    } else {
                        parameter.parameterValue[0].value = tempParameters[key]
                        parameter.parameterValue[0].description = tempParameters[key + '_field_visible_description']
                    }
                }
                this.savedParametersDialogVisible = false
            })
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, undefined, format)
        },
        decodeViewpointPrameterValues(string: string) {
            const parametersJson = {}
            const parameterArray = string.split('%26')
            for (let i = 0; i < parameterArray.length; i++) {
                const temp = parameterArray[i].split('%3D')
                parametersJson[temp[0]] = temp[1]
            }
            return parametersJson
        },
        executeViewpoint(viewpoint: any) {
            this.fillParameterForm(viewpoint)
            this.$emit('execute')
            this.savedParametersDialogVisible = false
        },
        async deleteViewpoint(viewpoint: any) {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentviewpoint/deleteViewpoint`, { VIEWPOINT: '' + viewpoint.vpId })
                .then(async () => {
                    this.removeViewpoint(viewpoint)
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                })
                .catch(() => {})
            this.loading = false
        },
        removeViewpoint(viewpoint: any) {
            const index = this.viewpoints.findIndex((el: any) => el.vpId === viewpoint.vpId)
            if (index !== -1) this.viewpoints.splice(index, 1)
        },
        loadMode() {
            this.mode = this.propMode ? this.propMode : 'execution'
        },
        loadQBEParameters() {
            this.qbeParameters = []
            this.propQBEParameters?.forEach((parameter: any) => {
                if (parameter.multiValue && parameter.defaultValue && !Array.isArray(parameter.defaultValue)) {
                    parameter.defaultValue = parameter.defaultValue?.split(',').map((el: any) => el.trim())
                }
                if (!parameter.value) parameter.value = parameter.defaultValue
                this.qbeParameters.push(parameter)
            })
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
    z-index: 100;
    background-color: white;
    height: 100%;
    min-height: 0;
    width: 350px;
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    font-size: 0.9rem;
    border-left: 1px solid var(--kn-color-borders);
    .kn-parameter-sidebar-content {
        min-height: 0;
        flex: 1;
        overflow: auto;
        position: relative;
        .p-field {
            label {
                padding: 4px 0;
            }
            display: flex;
            flex-direction: column;
            min-height: 70px;
            .inputScrollPanel {
                height: 150px;
            }
            .lookupScrollPanel {
                height: 50px;
            }
            .p-flex-row {
                min-height: 0;
                flex: 1;
            }
        }
    }
    &.kn-parameter-sidebar-west {
        right: unset;
        border-left: unset;
        border-right: 1px solid var(--kn-color-borders);
    }
    &.kn-parameter-sidebar-north {
        right: unset;
        border-left: unset;
        border-bottom: 1px solid var(--kn-color-borders);
        width: 100%;
        height: 200px;
        .kn-parameter-sidebar-content {
            display: flex;
            flex-direction: row;
            overflow-y: clip;
            .p-field {
                margin: 0 !important;
                min-width: 300px;
                max-width: 300px;
                .inputScrollPanel,
                .lookupScrollPanel {
                    height: 75px;
                }
                .p-flex-row,
                .p-flex-column {
                    overflow-y: auto;
                    overflow-x: clip;
                    max-height: 79px;
                }
            }
        }

        .kn-parameter-sidebar-buttons {
            justify-content: flex-end;
            .kn-button {
                width: auto;
            }
        }
    }
    &.kn-parameter-sidebar-south {
        right: unset;
        top: unset;
        bottom: 0;
        border-left: unset;
        border-top: 1px solid var(--kn-color-borders);
        width: 100%;
        height: 200px;
        .kn-parameter-sidebar-content {
            display: flex;
            flex-direction: row;
            overflow-y: clip;
            .p-field {
                margin: 0 !important;
                min-width: 300px;
                max-width: 300px;
                .inputScrollPanel,
                .lookupScrollPanel {
                    height: 75px;
                }
                .p-flex-row,
                .p-flex-column {
                    overflow-y: auto;
                    overflow-x: clip;
                    max-height: 79px;
                }
            }
        }
    }
    .parameter-clear-icon {
        margin-left: auto;
        line-height: 22px;
    }
    .p-calendar {
        background-color: transparent;
    }
    .p-field-radiobutton {
        font-size: 1rem;
        margin: 0.1rem;
        width: calc(100% - 3px);
        height: 15px;
        .p-radiobutton {
            width: 15px;
            height: 15px;
            .p-radiobutton-box {
                width: 15px;
                height: 15px;
                .p-radiobutton-icon {
                    width: 5px;
                    height: 5px;
                }
            }
        }
        .p-checkbox {
            width: 15px;
            height: 15px;
            .p-checkbox-box {
                width: 15px;
                height: 15px;
                .p-checkbox-icon {
                    width: 5px;
                    height: 5px;
                    &.pi-check::before {
                        top: 4px;
                        left: -1px;
                    }
                }
            }
        }
    }
    .p-dropdown {
        background-color: transparent;
        font-size: 0.9rem;
    }
    .p-inputtext {
        padding: 0.5rem 0.5rem;
        background-color: transparent;
        &.p-inputtext-sm {
            padding: 0.5rem 0.5rem;
        }
    }
    .parameterValueChip {
        font-size: 0.9rem;
        margin: 2px;
    }
}
</style>
