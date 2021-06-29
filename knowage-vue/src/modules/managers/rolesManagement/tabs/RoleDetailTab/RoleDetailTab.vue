<template>
    <Card :style="rolesManagementTabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field" :style="rolesManagementTabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.role.name.$model"
                            :class="{
                                'p-invalid': v$.role.name.$invalid && v$.role.name.$dirty
                            }"
                            maxLength="100"
                            @blur="v$.role.name.$touch()"
                            @input="onFieldChange('name', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.role.name"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>

                <div class="p-field" :style="rolesManagementTabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="code"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.role.code.$model"
                            :class="{
                                'p-invalid': v$.role.code.$invalid && v$.role.code.$dirty
                            }"
                            maxLength="20"
                            @blur="v$.role.code.$touch()"
                            @input="onFieldChange('code', $event.target.value)"
                            data-test="code-input"
                        />
                        <label for="code" class="kn-material-input-label">
                            {{ $t('managers.rolesManagement.detail.code') }}
                        </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.role.code"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.rolesManagement.detail.code')
                        }"
                    />
                </div>

                <div class="p-field" :style="rolesManagementTabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.role.description.$model"
                            :class="{
                                'p-invalid': v$.role.description.$invalid && v$.role.description.$dirty
                            }"
                            maxLength="225"
                            @blur="v$.role.description.$touch()"
                            @input="onFieldChange('description', $event.target.value)"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label">
                            {{ $t('common.description') }}
                        </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.role.description"
                        :additionalTranslateParams="{
                            fieldName: $t('common.description')
                        }"
                    />
                </div>

                <div class="p-field" :style="rolesManagementTabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <Dropdown
                            id="roleTypeID"
                            class="kn-material-input"
                            :options="translatedRoleTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_ID"
                            v-model="v$.role.roleTypeID.$model"
                            :class="{
                                'p-invalid': v$.role.roleTypeID.$invalid && v$.role.roleTypeID.$dirty
                            }"
                            @before-show="v$.role.roleTypeID.$touch()"
                            @change="onRoleTypeChange('roleTypeID', 'roleTypeCD', $event)"
                        >
                        </Dropdown>
                        <label for="roleTypeID" class="kn-material-input-label"> {{ $t('managers.rolesManagement.detail.roleTypeID') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.role.roleTypeID"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.rolesManagement.detail.roleTypeID')
                        }"
                    />
                </div>

                <div class="p-field" :style="rolesManagementTabViewDescriptor.pField.style">
                    <span class="p-field-checkbox">
                        <Checkbox id="isPublic" name="isPublic" v-model="role.isPublic" @change="onFieldChange('isPublic', role.isPublic)" :binary="true" data-test="is-public-checkbox" />
                        <label for="isPublic">
                            {{ $t('managers.rolesManagement.detail.isPublic') }}
                        </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import axios from 'axios'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import rolesManagementTabViewDescriptor from '../../RolesManagementTabViewDescriptor.json'
import roleDetailValidationDescriptor from './RoleDetailValidationDescriptor.json'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        Dropdown,
        Checkbox,
        KnValidationMessages
    },
    props: {
        selectedRole: {
            type: Object,
            requried: false
        }
    },
    emits: ['fieldChanged', 'roleTypeChanged'],
    data() {
        return {
            rolesManagementTabViewDescriptor,
            roleDetailValidationDescriptor,
            translatedRoleTypes: [] as any,
            v$: useValidate() as any,
            roleTypes: [] as any,
            role: {} as any
        }
    },
    validations() {
        return {
            role: createValidations('role', roleDetailValidationDescriptor.validations.role)
        }
    },
    async created() {
        if (this.selectedRole) {
            this.role = { ...this.selectedRole } as any
        }
        await this.loadRoleTypes()
    },
    watch: {
        selectedRole() {
            this.v$.$reset()
            this.role = { ...this.selectedRole } as any
        }
    },
    methods: {
        async loadRoleTypes() {
            await this.loadDomains('ROLE_TYPE').then((response) => {
                this.roleTypes = response.data
                this.translatedRoleTypes = response.data.map((roleType) => {
                    return {
                        VALUE_CD: this.$t(`managers.rolesManagement.rolesDropdown.${roleType.VALUE_CD}`),
                        VALUE_ID: roleType.VALUE_ID
                    }
                })
            })
        },
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        },
        loadDomains(type: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=${type}`)
        },
        onRoleTypeChange(roleTypeIDField: string, roleTypeCDField: string, event) {
            const selRoleType = this.roleTypes.find((roleType) => roleType.VALUE_ID === event.value)
            if (selRoleType) {
                this.role.roleTypeCD = selRoleType.VALUE_CD
            }
            const ID = event.value
            const CD = this.role.roleTypeCD
            this.$emit('roleTypeChanged', { roleTypeIDField, roleTypeCDField, ID, CD })
        }
    }
})
</script>
