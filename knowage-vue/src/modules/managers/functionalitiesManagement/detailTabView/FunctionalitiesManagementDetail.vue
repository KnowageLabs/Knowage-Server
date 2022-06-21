<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #start> {{ selectedFolder.name }} </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div v-if="!selectedFolder.id || selectedFolder.parentId" class="kn-detail">
        <Card class="p-m-3">
            <template #content>
                <form class="p-fluid p-m-3">
                    <div class="p-field" :style="detailDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="label"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.selectedFolder.code.$model"
                                :class="{
                                    'p-invalid': v$.selectedFolder.code.$invalid && v$.selectedFolder.code.$dirty
                                }"
                                maxLength="100"
                                @blur="v$.selectedFolder.code.$touch()"
                                @input="$emit('touched')"
                                data-test="code-input"
                            />
                            <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.selectedFolder.code"
                            :additionalTranslateParams="{
                                fieldName: $t('common.label')
                            }"
                        />
                    </div>
                    <div class="p-field" :style="detailDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText
                                id="name"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.selectedFolder.name.$model"
                                :class="{
                                    'p-invalid': v$.selectedFolder.name.$invalid && v$.selectedFolder.name.$dirty
                                }"
                                maxLength="255"
                                @blur="v$.selectedFolder.name.$touch()"
                                @input="$emit('touched')"
                                data-test="name-input"
                            />
                            <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.selectedFolder.name"
                            :additionalTranslateParams="{
                                fieldName: $t('common.name')
                            }"
                        />
                    </div>
                    <div class="p-field" :style="detailDescriptor.pField.style">
                        <span class="p-float-label">
                            <InputText id="description" class="kn-material-input" type="text" v-model.trim="selectedFolder.description" maxLength="255" @input="$emit('touched')" data-test="description-input" />
                            <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                        </span>
                    </div>
                </form>
            </template>
        </Card>
        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        {{ $t('managers.menuManagement.roles') }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <DataTable v-if="!loading" :value="roles" dataKey="id" class="p-datatable-sm kn-table" responsiveLayout="scroll" data-test="roles-table">
                    <Column field="name" :header="$t('managers.functionalitiesManagement.roles')" :sortable="true" />
                    <Column :header="$t('managers.functionalitiesManagement.development')" :style="detailDescriptor.checkboxColumns.style">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.development" :binary="true" :disabled="!slotProps.data['devRoles'].checkable" />
                        </template>
                    </Column>
                    <Column :header="$t('common.test')" :style="detailDescriptor.checkboxColumns.style">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.test" :binary="true" :disabled="!slotProps.data['testRoles'].checkable" />
                        </template>
                    </Column>
                    <Column :header="$t('managers.functionalitiesManagement.execution')" :style="detailDescriptor.checkboxColumns.style">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.execution" :binary="true" :disabled="!slotProps.data['execRoles'].checkable" />
                        </template>
                    </Column>
                    <Column :header="$t('managers.functionalitiesManagement.creation')" :style="detailDescriptor.checkboxColumns.style">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.creation" :binary="true" :disabled="!slotProps.data['createRoles'].checkable" />
                        </template>
                    </Column>
                    <Column @rowClick="false">
                        <template #body="slotProps">
                            <div class="p-d-flex p-jc-end">
                                <Button icon="pi pi-check" class="p-button-link" @click="checkAll(slotProps.data)" :disabled="slotProps.data.isButtonDisabled" :data-test="'check-all-' + slotProps.data.id" />
                                <Button icon="pi pi-times" class="p-button-link" @click="uncheckAll(slotProps.data)" :disabled="slotProps.data.isButtonDisabled" :data-test="'uncheck-all-' + slotProps.data.id" />
                            </div>
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import detailDescriptor from './FunctionalitiesManagementDetailDescriptor.json'
import validationDescriptor from './FunctionalitiesManagementValidation.json'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    emits: ['touched', 'close', 'inserted'],
    props: {
        functionality: Object,
        rolesShort: Array as any,
        parentId: Number
    },
    components: {
        Card,
        DataTable,
        Column,
        Checkbox,
        KnValidationMessages
    },
    data() {
        return {
            v$: useValidate() as any,
            detailDescriptor,
            validationDescriptor,
            formVisible: false,
            selectedFolder: {} as any,
            parentFolder: null as any,
            roles: [] as any,
            checked: [] as any,
            loading: false,
            dirty: false
        }
    },
    computed: {
        buttonDisabled(): Boolean {
            return this.v$.$invalid
        }
    },
    validations() {
        return {
            selectedFolder: createValidations('selectedFolder', validationDescriptor.validations.selectedFolder)
        }
    },
    async created() {
        this.loading = true
        this.selectedFolder = { ...this.functionality }
        await this.loadParentFolder()
        this.loadRoles()
        this.loading = false
    },
    watch: {
        async functionality() {
            this.loading = true
            this.v$.$reset()
            this.selectedFolder = { ...this.functionality }
            await this.loadParentFolder()
            this.loadRoles()
            this.loading = false
        },
        rolesShort() {
            this.loadRoles()
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        },
        loadRoles() {
            this.roles = []
            const tempFolder = this.selectedFolder.id ? this.selectedFolder : this.parentFolder
            this.rolesShort.forEach((role: any) => {
                const tempRole = {
                    id: role.id,
                    name: role.name,
                    development: false,
                    test: false,
                    execution: false,
                    creation: false,
                    isButtonDisabled: false
                }
                this.roleIsChecked(tempRole, tempFolder.devRoles, 'development')
                this.roleIsChecked(tempRole, tempFolder.testRoles, 'test')
                this.roleIsChecked(tempRole, tempFolder.execRoles, 'execution')
                this.roleIsChecked(tempRole, tempFolder.createRoles, 'creation')

                for (let field of ['devRoles', 'testRoles', 'execRoles', 'createRoles']) {
                    this.isCheckable(tempRole, field)
                }

                if (tempRole['devRoles'].checkable == false && tempRole['testRoles'].checkable == false && tempRole['execRoles'].checkable == false && tempRole['createRoles'].checkable == false) tempRole.isButtonDisabled = true

                this.roles.push(tempRole)
            })
        },
        async loadParentFolder() {
            if (this.parentId) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/getParent/${this.parentId}`).then((response: AxiosResponse<any>) => (this.parentFolder = response.data))
            }
        },
        roleIsChecked(role: any, roles: [], roleField: string) {
            if (roles) {
                const index = roles.findIndex((currentRole: any) => role.id === currentRole.id)

                if (index > -1) {
                    role[roleField] = true
                }
            }
        },
        isCheckable(role: any, roleField: string) {
            role[roleField] = { checkable: false }
            if (this.parentFolder.path === '/Functionalities') {
                role[roleField].checkable = true
            } else if (this.parentFolder[roleField] && this.parentFolder[roleField].length > 0) {
                this.parentFolder[roleField].forEach((currentRole) => {
                    if (role.name === currentRole.name) {
                        role[roleField].checkable = true
                    }
                })
            }
        },
        prepareFunctionalityToSend(functionalityToSend) {
            var roles = [...this.roles]
            functionalityToSend.codeType = functionalityToSend.codType
            delete functionalityToSend.codType
            delete functionalityToSend.biObjects
            this.emptyFunctionalityRoles(functionalityToSend)
            roles.forEach((role) => {
                if (role.development) functionalityToSend.devRoles.push(role)
                if (role.test) functionalityToSend.testRoles.push(role)
                if (role.execution) functionalityToSend.execRoles.push(role)
                if (role.creation) functionalityToSend.createRoles.push(role)
            })
            if (!functionalityToSend.id) {
                this.prepareNewFunctionality(functionalityToSend)
            }
        },
        prepareNewFunctionality(functionalityToSend) {
            functionalityToSend.codeType = this.parentFolder.codType
            functionalityToSend.parentId = this.parentFolder.id
            functionalityToSend.path = this.parentFolder.path + '/' + functionalityToSend.name
            if (!functionalityToSend.description) functionalityToSend.description = ''
        },
        emptyFunctionalityRoles(functionality) {
            functionality.devRoles = []
            functionality.testRoles = []
            functionality.execRoles = []
            functionality.createRoles = []
        },
        async createOrUpdate(functionalityToSend) {
            return this.selectedFolder.id ? this.$http.put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/${functionalityToSend.id}`, functionalityToSend) : this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/functionalities/', functionalityToSend)
        },
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let functionalityToSend = { ...this.selectedFolder }
            this.prepareFunctionalityToSend(functionalityToSend)
            await this.createOrUpdate(functionalityToSend).then((response: AxiosResponse<any>) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'Error', msg: response.data.error })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                    this.$emit('inserted', response.data.id)
                }
            })
            this.dirty = false
        },
        checkSingleRole(role, roleField, checkboxField, value) {
            if (role[roleField].checkable) {
                role[checkboxField] = value
            }
        },
        checkAll(role) {
            this.checkSingleRole(role, 'createRoles', 'creation', true)
            this.checkSingleRole(role, 'devRoles', 'development', true)
            this.checkSingleRole(role, 'execRoles', 'execution', true)
            this.checkSingleRole(role, 'testRoles', 'test', true)
        },
        uncheckAll(role) {
            this.checkSingleRole(role, 'createRoles', 'creation', false)
            this.checkSingleRole(role, 'devRoles', 'development', false)
            this.checkSingleRole(role, 'execRoles', 'execution', false)
            this.checkSingleRole(role, 'testRoles', 'test', false)
        }
    }
})
</script>
