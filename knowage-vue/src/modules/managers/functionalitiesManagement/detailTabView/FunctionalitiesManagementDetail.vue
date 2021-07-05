<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left> {{ selectedFolder.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div v-if="!selectedFolder.id || selectedFolder.parentId">
        <Card :style="detailDescriptor.card.style">
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
        <Card :style="detailDescriptor.card.style">
            <template #content>
                {{ roles }}
                <DataTable v-if="!loading" :value="roles" dataKey="id" class="p-datatable-sm kn-table" responsiveLayout="scroll" data-test="roles-table">
                    <Column field="name" header="Roles" :sortable="true" />
                    <Column header="Development">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.development" :binary="true" :disabled="!isCheckable(slotProps.data, 'devRoles')" />
                        </template>
                    </Column>
                    <Column header="Test">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.test" :binary="true" :disabled="!isCheckable(slotProps.data, 'testRoles')" />
                        </template>
                    </Column>
                    <Column header="Execution">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.execution" :binary="true" :disabled="!isCheckable(slotProps.data, 'execRoles')" />
                        </template>
                    </Column>
                    <Column header="Creation">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.creation" :binary="true" :disabled="!isCheckable(slotProps.data, 'createRoles')" aria-colcount="" />
                        </template>
                    </Column>
                    <Column @rowClick="false">
                        <template #body="slotProps">
                            <Button icon="pi pi-check" class="p-button-link" @click="checkAll(slotProps.data)" :data-test="'check-all-' + slotProps.data.id" />
                            <Button icon="pi pi-times" class="p-button-link" @click="uncheckAll(slotProps.data)" :data-test="'uncheck-all-' + slotProps.data.id" />
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
import axios from 'axios'
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
            operation: 'insert',
            loading: false
        }
    },
    computed: {
        buttonDisabled(): Boolean {
            return this.v$.$invalid
        }
    },
    operation() {
        if (this.selectedFolder.id) {
            return 'update'
        }
        return 'insert'
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
            // console.log(this.selectedFolder)
            // console.log(this.roles)
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
            console.log('PARENT ID', this.parentId)
            console.log('PARENT FOLDER', this.parentFolder)
            console.log('TEMP FOLDER', tempFolder)
            this.rolesShort.forEach((role: any) => {
                const tempRole = { id: role.id, name: role.name, development: false, test: false, execution: false, creation: false }

                this.roleIsChecked(tempRole, tempFolder.devRoles, 'development')
                this.roleIsChecked(tempRole, tempFolder.testRoles, 'test')
                this.roleIsChecked(tempRole, tempFolder.execRoles, 'execution')
                this.roleIsChecked(tempRole, tempFolder.createRoles, 'creation')

                this.roles.push(tempRole)
            })
        },
        async loadParentFolder() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functionalities/getParent/${this.parentId}`).then((response) => (this.parentFolder = response.data))
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
            // console.log('isCheckable ROLE', role)
            // console.log('THIS PARENT FOLDER!!!!!!!', this.parentFolder)
            let checkable = false
            if (this.selectedFolder.path === '/Functionalities') {
                checkable = true
            } else if (this.parentFolder[roleField] && this.parentFolder[roleField].length > 0) {
                this.parentFolder[roleField].forEach((currentRole) => {
                    // console.log(role.name + ' === ' + currentRole.name)
                    if (role.name === currentRole.name) {
                        checkable = true
                    }
                })
            }

            return checkable
        },
        prepareFunctionalityToSend(functionalityToSend) {
            var roles = [...this.roles]
            var functionality = functionalityToSend
            functionality.codeType = functionality.codType
            delete functionality.codType
            delete functionality.biObjects
            this.emptyFunctionalityRoles(functionality)
            roles.forEach((role) => {
                if (role.development) functionality.devRoles.push(role)
                if (role.test) functionality.testRoles.push(role)
                if (role.execution) functionality.execRoles.push(role)
                if (role.creation) functionality.createRoles.push(role)
            })
            console.log('insertSelectedRolesIntoFunctionality', functionality)
        },
        emptyFunctionalityRoles(functionality) {
            functionality.devRoles = []
            functionality.testRoles = []
            functionality.execRoles = []
            functionality.createRoles = []
        },
        async createOrUpdate(functionalityToSend) {
            return this.operation === 'update' ? axios.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functionalities/${functionalityToSend.id}`, functionalityToSend) : axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/functionalities/', functionalityToSend)
        },
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let functionalityToSend = { ...this.selectedFolder }
            this.prepareFunctionalityToSend(functionalityToSend)
            await this.createOrUpdate(functionalityToSend).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { title: 'Error', msg: response.data.error })
                } else {
                    this.$store.commit('setInfo', { title: 'Ok', msg: 'Saved OK' })
                }
            })
            this.$emit('inserted')
        },
        checkAll(role) {
            var checkedRole = role
            checkedRole.development = true
            checkedRole.test = true
            checkedRole.execution = true
            checkedRole.creation = true
        },
        uncheckAll(role) {
            var checkedRole = role
            checkedRole.development = false
            checkedRole.test = false
            checkedRole.execution = false
            checkedRole.creation = false
        }
    }
})
</script>

<style lang="scss" scoped></style>
