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
                <DataTable :value="roles" dataKey="id" class="p-datatable-sm kn-table" responsiveLayout="scroll" data-test="roles-table">
                    <Column field="name" header="Roles" :sortable="true" />
                    <Column header="Development">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.development" :binary="true" @click="test(slotProps.data)" />
                        </template>
                    </Column>
                    <Column header="Test">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.test" :binary="true" @click="test(slotProps.data)" />
                        </template>
                    </Column>
                    <Column header="Execution">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.execution" :binary="true" @click="test(slotProps.data)" />
                        </template>
                    </Column>
                    <Column header="Creation">
                        <template #body="slotProps">
                            <Checkbox v-model="slotProps.data.creation" :binary="true" @click="test(slotProps.data)" />
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
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import detailDescriptor from './FunctionalitiesManagementDetailDescriptor.json'
import validationDescriptor from './FunctionalitiesManagementValidation.json'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    emits: ['touched', 'close'],
    props: {
        functionality: Object,
        rolesShort: Array as any
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
            roles: [] as any,
            checked: [] as any
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
    created() {
        this.selectedFolder = { ...this.functionality }
        this.loadRoles()
    },
    watch: {
        functionality() {
            this.v$.$reset()
            this.selectedFolder = { ...this.functionality }
            console.log(this.selectedFolder)
            console.log(this.roles)
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
            this.rolesShort.forEach((role: any) => {
                const tempRole = { id: role.id, name: role.name, development: false, test: false, execution: false, creation: false }

                this.roleIsChecked(tempRole, this.selectedFolder.devRoles, 'development')
                this.roleIsChecked(tempRole, this.selectedFolder.testRoles, 'test')
                this.roleIsChecked(tempRole, this.selectedFolder.execRoles, 'execution')
                this.roleIsChecked(tempRole, this.selectedFolder.createRoles, 'creation')

                this.roles.push(tempRole)
            })
            console.log('ROLES: ', this.roles)
        },
        roleIsChecked(role: any, roles: [], roleField: string) {
            if (roles) {
                const index = roles.findIndex((currentRole: any) => role.id === currentRole.id)

                if (index > -1) {
                    role[roleField] = true
                }
            }
        },
        test(role) {
            console.log('ROLE AFTER CHECK: ', role)
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
