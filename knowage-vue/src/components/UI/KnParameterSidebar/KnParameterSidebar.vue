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

        {{ user.sessionRole }}

        {{ buttonsDisabled }}

        <div class="p-fluid kn-parameter-sidebar-content">
            <div class="p-field p-m-4" v-if="user && (!user.sessionRole || user.sessionRole === 'No default role selected')">
                <div class="p-d-flex">
                    <label class="kn-material-input-label">{{ $t('common.roles') }}</label>
                </div>
                <Dropdown class="kn-material-input" v-model="newSessionRole" :options="user.roles" @change="setNewSessionRole" />
            </div>

            <div v-for="(parameter, index) in parameters.filterStatus" :key="index">
                <!-- Manual Text/Number Input -->
                <div class="p-field p-m-4" v-if="(parameter.type === 'STRING' || parameter.type === 'NUM') && !parameter.selectionType && parameter.valueSelection === 'man_in' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters }">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
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
                        <label class="kn-material-input-label" :class="{ 'p-text-italic': parameter.dependsOnParameters }">{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label>
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

                <!-- LOV list radio/multiple input -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'LIST' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0].value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-column">
                        <div class="p-field-radiobutton" v-for="(option, index) in parameter.data" :key="index">
                            <RadioButton v-if="!parameter.multivalue" :value="option.value" v-model="parameter.parameterValue[0].value" @change="updateVisualDependency(parameter)" />
                            <Checkbox v-if="parameter.multivalue" :value="option.value" v-model="selectedParameterCheckbox[parameter.id]" @change="setCheckboxValue(parameter)" />
                            <label>{{ option.value }}</label>
                        </div>
                    </div>
                </div>

                <!-- LOV combobox single and multiple input -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'COMBOBOX' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0].value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <Dropdown v-if="!parameter.multivalue" class="kn-material-input" v-model="parameter.parameterValue[0]" :options="parameter.data" optionLabel="value" @change="updateVisualDependency(parameter)" />
                    <MultiSelect v-else v-model="parameter.parameterValue" :options="parameter.data" optionLabel="value" @change="updateVisualDependency(parameter)" />
                </div>

                <!-- POP UP -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openPopupDialog(parameter)"></i>
                        <div>
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.value }}</Chip>
                        </div>
                    </div>
                </div>

                <!-- Tree -->
                <div class="p-field p-m-4" v-if="parameter.selectionType === 'TREE' && parameter.showOnPanel === 'true'">
                    <div class="p-d-flex">
                        <label
                            class="kn-material-input-label"
                            :class="{
                                'kn-required-alert': parameter.mandatory && ((!parameter.multivalue && !parameter.parameterValue[0]?.value) || (parameter.multivalue && parameter.parameterValue.length === 0)),
                                'p-text-italic': parameter.dependsOnParameters
                            }"
                            >{{ parameter.label }} {{ parameter.mandatory ? '*' : '' }}</label
                        >
                        <i class="fa fa-eraser parameter-clear-icon kn-cursor-pointer" v-tooltip.left="$t('documentExecution.main.parameterClearTooltip')" @click="resetParameterValue(parameter)"></i>
                    </div>
                    <div class="p-d-flex p-flex-row">
                        <i class="pi pi-external-link kn-cursor-pointer p-mr-2" @click="openTreeDialog(parameter)"></i>
                        <div>
                            <Chip v-for="(parameterValue, index) in parameter.parameterValue" :key="index">{{ parameterValue.value }}</Chip>
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
        <KnParameterSaveDialog :visible="parameterSaveDialogVisible" :propLoading="loading" @close="parameterSaveDialogVisible = false" @saveViewpoint="saveViewpoint"></KnParameterSaveDialog>
        <KnParameterSavedParametersDialog :visible="savedParametersDialogVisible" :propViewpoints="viewpoints" @close="savedParametersDialogVisible = false" @fillForm="fillParameterForm" @executeViewpoint="executeViewpoint" @deleteViewpoint="deleteViewpoint"></KnParameterSavedParametersDialog>
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
import KnParameterSaveDialog from './dialogs/KnParameterSaveDialog.vue'
import KnParameterSavedParametersDialog from './dialogs/KnParameterSavedParametersDialog.vue'
import Menu from 'primevue/menu'
import MultiSelect from 'primevue/multiselect'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'kn-parameter-sidebar',
    components: {
        Calendar,
        Chip,
        Checkbox,
        Dropdown,
        KnParameterPopupDialog,
        KnParameterTreeDialog,
        KnParameterSaveDialog,
        KnParameterSavedParametersDialog,
        Menu,
        MultiSelect,
        RadioButton
    },
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
            parameterSaveDialogVisible: false,
            savedParametersDialogVisible: false,
            viewpoints: [],
            newSessionRole: '' as any,
            user: null as any,
            loading: false
        }
    },
    watch: {
        sessionRole() {
            this.newSessionRole = ''
            this.parameters = { isReadyForExecution: false, filterStatus: [] }
        },
        filtersData() {
            this.loadDocument()
            this.loadParameters()
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

        this.loadDocument()
        this.loadParameters()

        console.log('STORE: ', this.$store)
    },
    methods: {
        setNewSessionRole() {
            console.log(' >>> USER: ', this.user)
            console.log(' >>> NE ROLE: ', this.newSessionRole)
            this.$store.commit('setUserSessionRole', this.newSessionRole)
            console.log('THIS STORE USER: ', (this.$store.state as any).user)
            this.$emit('execute')
            this.parameters = { isReadyForExecution: false, filterStatus: [] }
        },
        loadDocument() {
            this.document = this.propDocument
        },
        loadParameters() {
            this.parameters.isReadyForExecution = this.filtersData?.isReadyForExecution
            this.parameters.filterStatus = []

            this.filtersData?.filterStatus.forEach((el: any) => {
                el.parameterValue = []
                if (el.driverDefaultValue?.length > 0) {
                    el.parameterValue = el.driverDefaultValue.map((defaultValue: any) => {
                        return { value: defaultValue.value ?? defaultValue._col0, description: defaultValue.desc ?? defaultValue._col1 }
                    })
                }

                if (el.data) {
                    el.data = el.data.map((data: any) => {
                        return { value: data._col0, description: data._col1 }
                    })
                }

                if (el.selectionType == 'LIST' && el.showOnPanel == 'true' && el.multivalue) {
                    this.selectedParameterCheckbox[el.id] = el.parameterValue?.map((parameterValue: any) => parameterValue.value)
                }

                this.parameters.filterStatus.push(el)
            })

            this.parameters?.filterStatus.forEach((el: any) => this.setVisualDependency(el))
            this.parameters?.filterStatus.forEach((el: any) => this.updateVisualDependency(el))
            console.log('>>> LOADED PARAMETERS: ', this.parameters?.filterStatus)
        },
        setVisualDependency(parameter: any) {
            console.log(' >>> VIS DEP PARMAETR: ', parameter)
            if (parameter.dependencies.visual.length !== 0) {
                parameter.dependencies.visual.forEach((dependency: any) => {
                    const index = this.parameters.filterStatus.findIndex((param: any) => {
                        return param.urlName === dependency.parFatherUrlName
                    })
                    if (index !== -1) {
                        const tempParameter = this.parameters.filterStatus[index]
                        parameter.dependsOnParameters ? parameter.dependsOnParameters.push(tempParameter) : (parameter.dependsOnParameters = [tempParameter])
                        tempParameter.dependentParameters ? tempParameter.dependentParameters.push(parameter) : (tempParameter.dependentParameters = [parameter])
                    }
                })
            }
        },
        resetParameterValue(parameter: any) {
            if ((parameter.selectionType === 'LIST' || parameter.selectionType === 'COMBOBOX') && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = []
                this.selectedParameterCheckbox[parameter.id] = []
                for (let i = 0; i < parameter.driverDefaultValue.length; i++) {
                    const temp = parameter.driverDefaultValue[i]
                    parameter.parameterValue.push({ value: temp._col0, description: temp._col1 })
                    this.selectedParameterCheckbox[parameter.id].push(temp._col0)
                }
            } else if ((parameter.selectionType === 'COMBOBOX' || parameter.selectionType === 'TREE') && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = [...parameter.driverDefaultValue]
            } else if (parameter.selectionType === 'LOOKUP' && parameter.showOnPanel === 'true' && parameter.multivalue) {
                parameter.parameterValue = parameter.driverDefaultValue.map((el: any) => {
                    return { value: el._col0, description: el._col1 }
                })
            } else {
                if (!parameter.parameterValue[0]) {
                    parameter.parameterValue[0] = { value: '', desc: '' }
                }
                parameter.parameterValue[0].value = parameter.driverDefaultValue[0].value ?? parameter.driverDefaultValue[0]._col0
            }
        },
        resetAllParameters() {
            this.parameters.filterStatus.forEach((el: any) => this.resetParameterValue(el))
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

                // console.log('PARAMETER REQUIRED: ', parameter)

                if (parameter.mandatory && parameter.showOnPanel == 'true') {
                    if (!parameter.parameterValue || parameter.parameterValue.length === 0) {
                        // console.log('REQUIRED 1', parameter)
                        return true
                    } else {
                        for (let i = 0; i < parameter.parameterValue.length; i++) {
                            if (!parameter.parameterValue[i].value) {
                                // console.log('ENTERED REQUIRED 2!!!!!!!!!')
                                return true
                            }
                        }
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

            const postData = { label: this.document.label, parameters: this.getFormatedParameters(), paramId: parameter.urlName, role: this.sessionRole }
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentExeParameters/admissibleValues`, postData).then((response: AxiosResponse<any>) => (this.parameterPopUpData = response.data))
            this.loading = false
        },
        getFormatedParameters() {
            let parameters = [] as any[]

            Object.keys(this.parameters.filterStatus).forEach((key: any) => {
                const parameter = this.parameters.filterStatus[key]

                console.log('PARAMETER: ', parameter)

                // parameter.multivalue ? parameters.push({ value: parameter.parameterValue, description: parameter.parameterDescription }) : parameters.push({ value: parameter.parameterValue[0].value, description: parameter.parameterDescription[0].description })

                if (parameter.valueSelection === 'man_in') {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue[0].value, description: parameter.parameterValue[0].description })
                } else if (parameter.multivalue) {
                    parameters.push({ label: parameter.label, value: parameter.parameterValue, description: parameter.parameterDescription })
                    // let tempArrayValue = [] as any[]
                    // let tempArrayDescription = [] as any[]
                    // for (let i = 0; i < parameter.parameterValue.length; i++) {
                    //     tempArrayValue.push(parameter.parameterValue[i].value)
                    //     tempArrayDescription.push(parameter.parameterValue[i].description)
                    // }

                    // parameter[parameter.urlName] = tempArrayValue
                    // parameter[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].description
                } else if (parameter.type === 'DATE') {
                    parameter[parameter.urlName] = parameter.parameterValue[0].value
                    parameter[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0].value
                } else {
                    parameter[parameter.urlName] = parameter.parameterValue[0] ? parameter.parameterValue[0].value : parameter.parameterValue.value
                    parameter[parameter.urlName + '_field_visible_description'] = parameter.parameterValue[0] ? parameter.parameterValue[0].description : parameter.parameterValue.description
                }
            })

            return parameters
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

                this.visualDependencyCheck(dependentParameter, parameter)

                // console.log('DEPENEDEN AFTER: ', dependentParameter)
            })
        },
        visualDependencyCheck(parameter: any, changedParameter: any) {
            console.log(' >>> VISUAL DEP CHECK: ', parameter)

            let showOnPanel = 'true'
            for (let i = 0; i < parameter.dependencies.visual.length && showOnPanel === 'true'; i++) {
                showOnPanel = 'false'
                const visualDependency = parameter.dependencies.visual[i]

                const index = parameter.dependsOnParameters.findIndex((el: any) => el.urlName === visualDependency.parFatherUrlName)
                const parentParameter = parameter.dependsOnParameters[index]

                for (let i = 0; i < parentParameter.parameterValue.length; i++) {
                    if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
                        // console.log(' >>>> ENTERED', parentParameter.parameterValue[i].value, ' === ', visualDependency.compareValue)
                        console.log('CHANGED PARAM', changedParameter, 'VISUAL DEP', visualDependency)
                        if (changedParameter.urlName === visualDependency.parFatherUrlName) {
                            parameter.label = visualDependency.viewLabel
                        }
                        showOnPanel = 'true'
                        break
                    }
                }

                if (visualDependency.operation === 'not contains') {
                    if (showOnPanel == 'true') {
                        showOnPanel = 'false'
                        break
                    } else {
                        showOnPanel = 'true'
                    }
                }
            }

            parameter.showOnPanel = showOnPanel

            // if (visualDependency.operation === 'contains') {
            //     for (let i = 0; i < parentParameter.parameterValue.length; i++) {
            //         if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
            //             // console.log(' >>>> ENTERED', parentParameter.parameterValue[i].value, ' === ', visualDependency.compareValue)
            //             console.log('CHANGED PARAM', changedParameter, 'VISUAL DEP', visualDependency)
            //             if (changedParameter.urlName === visualDependency.parFatherUrlName) {
            //                 parameter.label = visualDependency.viewLabel
            //             }
            //             showOnPanel = 'true'
            //             break
            //         }
            //     }
            // } else if (visualDependency.operation === 'not contains') {
            //     console.log(' >>> >>> ENTERED NOT CONTAINS')
            //     for (let i = 0; i < parentParameter.parameterValue.length; i++) {
            //         console.log(' >>>> TEST', parentParameter.parameterValue[i].value, ' === ', visualDependency.compareValue)
            //         if (parentParameter.parameterValue[i].value === visualDependency.compareValue) {
            //             showOnPanel = 'false'
            //             break
            //         }
            //     }
            // }

            // console.log('SHOW ON PANEL ITERATION ', i, showOnPanel)
        },
        openSaveParameterDialog() {
            this.parameterSaveDialogVisible = true
        },
        async saveViewpoint(viewpoint: any) {
            console.log('VIEWPOINT FOR SAVE: ', viewpoint)
            const postData = { ...viewpoint, OBJECT_LABEL: this.document.label, ROLE: (this.$store.state as any).user.defaultRole, VIEWPOINT: this.getFormatedParameters() }
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
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        },
        async openSavedParametersDialog() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentviewpoint/getViewpoints?label=${this.document.label}&role=${this.sessionRole}`).then((response: AxiosResponse<any>) => {
                this.viewpoints = response.data.viewpoints
                this.savedParametersDialogVisible = true
            })
            this.loading = false
        },
        fillParameterForm(viewpoint: any) {
            console.log('VIEWPOINT FOR FILL FORM: ', viewpoint)
            console.log(' >>> TEEEEEEEST ', this.decodeViewpointPrameterValues(viewpoint.vpValueParams))
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
            console.log('EXECUTE VIEWPOINT: ', viewpoint)
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
