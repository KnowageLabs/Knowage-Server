<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field p-m-5">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.lov.label.$model"
                            :class="{
                                'p-invalid': v$.lov.label.$invalid && v$.lov.label.$dirty
                            }"
                            maxLength="20"
                            @blur="v$.lov.label.$touch()"
                            @input="$emit('touched')"
                        />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.lov.label"
                        :additionalTranslateParams="{
                            fieldName: $t('common.label')
                        }"
                        :specificTranslateKeys="{
                            custom_unique_label: 'managers.lovsManagement.lovLabelNotUnique'
                        }"
                    />
                </div>
                <div class="p-field p-m-5">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.lov.name.$model"
                            :class="{
                                'p-invalid': v$.lov.name.$invalid && v$.lov.name.$dirty
                            }"
                            maxLength="50"
                            @blur="v$.lov.name.$touch()"
                            @input="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.lov.name"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                        :specificTranslateKeys="{
                            custom_unique_name: 'managers.lovsManagement.lovNameNotUnique'
                        }"
                    />
                </div>
                <div class="p-field p-m-5">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" v-model.trim="lov.description" maxLength="160" @input="$emit('touched')" />
                        <label for="description" class="kn-material-input-label"> {{ $t('managers.lovsManagement.description') }}</label>
                    </span>
                </div>
                <div class="p-field p-m-5">
                    <span>
                        <label for="typeLovDropdown" class="kn-material-input-label">{{ $t('managers.lovsManagement.lovType') }} * </label>
                        <Dropdown
                            id="typeLovDropdown"
                            class="kn-material-input"
                            :class="{
                                'p-invalid': v$.lov.itypeCd.$invalid && v$.lov.itypeCd.$dirty
                            }"
                            v-model="v$.lov.itypeCd.$model"
                            :options="listOfInputTypes"
                            optionLabel="VALUE_NM"
                            optionValue="VALUE_CD"
                            :placeholder="$t('managers.lovsManagement.lovTypePlaceholder')"
                            aria-label="dropdown"
                            @before-show="v$.lov.itypeCd.$touch()"
                            @change="typeChanged"
                        />
                    </span>

                    <KnValidationMessages
                        :vComp="v$.lov.itypeCd"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.lovsManagement.lovType')
                        }"
                    >
                    </KnValidationMessages>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iLov } from '../../LovsManagement'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import lovsManagementDetailCardValidation from './LovsManagementDetailCardValidation.json'
import useValidate from '@vuelidate/core'

export default defineComponent({
    name: 'lovs-management-detail-card',
    components: { Card, Dropdown, KnValidationMessages },
    props: {
        selectedLov: { type: Object },
        lovs: { type: Array, required: true },
        listOfInputTypes: { type: Array }
    },
    emits: ['touched', 'typeChanged'],
    data() {
        return {
            lovsManagementDetailCardValidation,
            lov: {} as iLov,
            v$: useValidate() as any
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            'custom-unique-label': (value: string) => {
                return this.fieldNotUnique(value, 'label')
            },
            'custom-unique-name': (value: string) => {
                return this.fieldNotUnique(value, 'name')
            }
        }

        const validationObject = {
            lov: createValidations('lov', lovsManagementDetailCardValidation.validations.lov, customValidators)
        }

        return validationObject
    },
    watch: {
        selectedLov() {
            this.loadLov()
        }
    },
    async created() {
        this.loadLov()
    },
    methods: {
        loadLov() {
            this.lov = this.selectedLov as iLov
        },
        fieldNotUnique(value: string, field: string) {
            const index = this.lovs.findIndex((lov: any) => lov[field] === value && lov.id != this.lov.id)
            return index === -1
        },
        typeChanged() {
            this.$emit('touched')
            this.$emit('typeChanged')
        }
    }
})
</script>
