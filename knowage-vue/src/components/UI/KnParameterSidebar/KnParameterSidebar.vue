<template>
    <div id="kn-parameter-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                <div id="kn-parameter-sidebar-toolbar-icons-container" class="p-d-flex p-flex-row p-jc-around">
                    <i class="fa fa-eraser kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.resetParametersTooltip')" @click="resetAllParameters"></i>
                    <i class="pi pi-pencil kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.savedParametersTooltip')" @click="openSavedParametersDialog"></i>
                    <i class="fas fa-save kn-cursor-pointer" v-tooltip.top="$t('documentExecution.main.saveParametersFromStateTooltip')" @click="openSaveParameterDialog"></i>
                </div>
            </template>
        </Toolbar>

        <div class="p-fluid kn-parameter-sidebar-content kn-alternated-rows">
            <div class="p-field p-m-1 p-p-2" v-if="user && (!sessionRole || sessionRole === 'No default role selected')">
                <div class="p-d-flex">
                    <label class="kn-material-input-label">{{ $t('common.roles') }}</label>
                </div>
                <Dropdown class="kn-material-input" v-model="role" :options="user.roles" @change="setNewSessionRole" />
            </div>

            <div v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <div class="p-field p-m-1 p-p-2" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters }" :data-test="'parameter-input-label-' + parameter.id">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
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
                <div class="p-field p-m-1 p-p-2" v-if="parameter.type === 'DATE' && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters }" :data-test="'parameter-date-label-' + parameter.id">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
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
                <div class="p-field p-m-1 p-p-2" v-if="parameter.selectionType === 'LIST' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            :data-test="'parameter-checkbox-label-' + parameter.id"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)" :data-test="'parameter-checkbox-clear-' + parameter.id"></i>
                    </div>
                    <div class="p-d-flex p-flex-column">
                        <div class="p-field-radiobutton" v-for="(option, index) in parameter.data" :key="index" :data-test="'parameter-list-' + parameter.id">
                            <RadioButton v-if="!parameter.multivalue && parameter.parameterValue" :value="option.value" v-model="parameter.parameterValue[0].value" @change="updateDependency(parameter)" />
                            <Checkbox v-if="parameter.multivalue && parameter.parameterValue" :value="option.value" v-model="selectedParameterCheckbox[parameter.id]" @change="setCheckboxValue(parameter)" />
                            <label>{{ option.description }}</label>
                        </div>
                    </div>
                </div>
                <div class="p-field p-m-1 p-p-2" v-if="parameter.selectionType === 'COMBOBOX' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Dropdown v-if="!parameter.multivalue && parameter.parameterValue" class="kn-material-input" v-model="parameter.parameterValue[0]" :options="parameter.data" optionLabel="description" @change="updateDependency(parameter)" />
                    <MultiSelect v-else v-model="parameter.parameterValue" :options="parameter.data" optionLabel="description" @change="updateDependency(parameter)" />
                </div>
                <div class="p-field p-m-1 p-p-2" v-if="parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openPopupDialog(parameter)"></i>
                        <div>
                            <Chip class="parameterValueChip" v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.description }}</Chip>
                        </div>
                    </div>
                </div>
                <div class="p-field p-m-1 p-p-2" v-if="parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && parameter.parameterValue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openTreeDialog(parameter)"></i>
                        <div>
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.description }}</Chip>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div v-if="parameters && parameters.filterStatus.length > 0" class="p-fluid p-d-flex p-flex-row p-mx-5 kn-parameter-sidebar-buttons">
            <Button class="kn-button kn-button--primary" :disabled="buttonsDisabled" @click="$emit('execute')"> {{ $t('common.execute') }}</Button>
            <Button class="kn-button kn-button--primary p-ml-1" icon="fa fa-chevron-down" :disabled="buttonsDisabled" @click="toggle($event)" />
            <Menu ref="executeButtonMenu" :model="executeMenuItems" :popup="true" />
        </div>
        <KnParameterPopupDialog :visible="popupDialogVisible" :selectedParameter="selectedParameter" :propLoading="loading" :parameterPopUpData="parameterPopUpData" @close="popupDialogVisible = false" @save="onPopupSave"></KnParameterPopupDialog>
        <KnParameterTreeDialog :visible="treeDialogVisible" :selectedParameter="selectedParameter" :formatedParameterValues="formatedParameterValues" :document="document" @close="onTreeClose" @save="onTreeSave"></KnParameterTreeDialog>
        <KnParameterSaveDialog :visible="parameterSaveDialogVisible" :propLoading="loading" @close="parameterSaveDialogVisible = false" @saveViewpoint="saveViewpoint"></KnParameterSaveDialog>
        <KnParameterSavedParametersDialog :visible="savedParametersDialogVisible" :propViewpoints="viewpoints" @close="savedParametersDialogVisible = false" @fillForm="fillParameterForm" @executeViewpoint="executeViewpoint" @deleteViewpoint="deleteViewpoint"></KnParameterSavedParametersDialog>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { formatDate } from '@/helpers/commons/localeHelper'
import { iDocument, iParameter, iAdmissibleValues } from './KnParameterSidebar'
import { setVisualDependency, updateVisualDependency } from './KnParameterSidebarVisualDependency'
import { setDataDependency, updateDataDependency } from './KnParameterSidebarDataDependency'
import Calendar from 'primevue/calendar'
import Chip from 'primevue/chip'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import KnParameterPopupDialog from './dialogs/KnParameterPopupDialog.vue'
import KnParameterTreeDialog from './dialogs/KnParameterTreeDialog.vue'
import KnParameterSaveDialog from './dialogs/KnParameterSaveDialog.vue'
import KnParameterSavedParametersDialog from './dialogs/KnParameterSavedParametersDialog.vue'
import Menu from 'primevue/menu'
import MultiSelect from 'primevue/multiselect'
import RadioButton from 'primevue/radiobutton'
export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: { Calendar, Chip, Checkbox, Dropdown, KnParameterPopupDialog, KnParameterTreeDialog, KnParameterSaveDialog, KnParameterSavedParametersDialog, Menu, MultiSelect, RadioButton },
    props: { filtersData: { type: Object }, propDocument: { type: Object }, userRole: { type: String } },
    emits: ['execute', 'exportCSV', 'roleChanged'],
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
            primary: true
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
        }
    },
    computed: {
        sessionRole(): string {
            return (this.$store.state as any).user.sessionRole
        },
        buttonsDisabled(): boolean {
            return this.requiredFiledMissing()
        }
    },
    created() {
        this.user = (this.$store.state as any).user
        this.role = this.userRole as string
        this.loadDocument()
        this.loadParameters()
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
                parameter.parameterValue[0] = { value: '', description: '' }
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
                parameter.parameterValue = [...parameter.driverDefaultValue]
            } else if ((parameter.selectionType === 'COMBOBOX' || parameter.selectionType === 'LOOKUP') && parameter.showOnPanel === 'true' && !parameter.multivalue) {
                parameter.parameterValue[0] = { value: parameter.driverDefaultValue[0][valueIndex], description: parameter.driverDefaultValue[0][descriptionIndex] }
            } else if (parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue.map((el: any) => {
                    return { value: valueIndex ? el[valueIndex] : '', description: descriptionIndex ? el[descriptionIndex] : '' }
                })
            } else {
                if (!parameter.parameterValue[0]) {
                    parameter.parameterValue[0] = { value: '', description: '' }
                }
                parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value ?? parameter.driverDefaultValue[0][valueIndex]
            }
        },
        resetAllParameters() {
            this.parameters.filterStatus.forEach((el: any) => this.resetParameterValue(el))
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
            const postData = { label: this.document?.label, parameters: this.getFormattedParameters(), paramId: parameter.urlName, role: this.sessionRole }
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentExeParameters/admissibleValues`, postData)
                .then((response: AxiosResponse<any>) => (this.parameterPopUpData = response.data))
                .catch((error: any) => console.log('ERROR: ', error))
            this.loading = false
        },
        getFormattedParameters() {
            let parameters = [] as any[]
            Object.keys(this.parameters.filterStatus).forEach((key: any) => {
                const parameter = this.parameters.filterStatus[key]
                if (!parameter.multivalue) {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description })
                } else {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue, description: parameter.parameterDescription ?? '' })
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
            this.updateVisualDependency(parameter)
            updateDataDependency(this.parameters, parameter, this.loading, this.document, this.sessionRole, this.$http)
        },
        openSaveParameterDialog() {
            this.parameterSaveDialogVisible = true
        },
        async saveViewpoint(viewpoint: any) {
            const postData = { ...viewpoint, OBJECT_LABEL: this.document?.label, ROLE: this.sessionRole, VIEWPOINT: this.getParameterValues() }
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
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentviewpoint/getViewpoints?label=${this.document?.label}&role=${this.sessionRole}`).then((response: AxiosResponse<any>) => {
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
                        parameter.parameterValue[0].value = this.getFormattedDate(tempParameters[key], 'MM/DD/YYYY')
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
                    }
                }
                this.savedParametersDialogVisible = false
            })
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
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
    width: 350px;
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    font-size: 0.9rem;
}
.parameter-clear-icon {
    margin-left: auto;
}
.kn-parameter-sidebar-content {
    height: 80vh;
    overflow: auto;
    position: relative;
}
.kn-parameter-sidebar-buttons {
    margin-top: auto;
    margin-bottom: 15px;
}
.p-calendar {
    background-color: transparent;
}
.p-field-radiobutton {
    font-size: 1rem;
    margin: 0.1rem;
    width: 15px;
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
}
</style>
