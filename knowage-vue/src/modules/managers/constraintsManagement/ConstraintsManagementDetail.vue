<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeForm" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <Card :style="constraintsManagementDetailDescriptor.styles.basicCard" class="p-m-2">
            <template #content>
                <form class="p-fluid p-formgrid p-grid" data-test="constraints-form">
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="label"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.constraint.label.$model"
                                :class="{
                                    'p-invalid': v$.constraint.label.$invalid && v$.constraint.label.$dirty
                                }"
                                @blur="v$.constraint.label.$touch()"
                                @input="$emit('touched')"
                                :disabled="inputDisabled"
                            />
                            <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.constraint.label" :additionalTranslateParams="{ fieldName: $t('common.label') }"></KnValidationMessages>
                    </div>
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="name"
                                class="kn-material-input"
                                type="text"
                                v-model.trim="v$.constraint.name.$model"
                                :class="{
                                    'p-invalid': v$.constraint.name.$invalid && v$.constraint.name.$dirty
                                }"
                                @blur="v$.constraint.name.$touch()"
                                @input="$emit('touched')"
                                :disabled="inputDisabled"
                            />
                            <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.constraint.name" :additionalTranslateParams="{ fieldName: $t('common.name') }"></KnValidationMessages>
                    </div>
                    <div class="p-field p-col-12">
                        <span class="p-float-label">
                            <InputText id="description" class="kn-material-input" type="text" v-model.trim="constraint.description" :disabled="inputDisabled" @input="$emit('touched')" />
                            <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-4">
                        <span class="p-float-label">
                            <Dropdown
                                v-if="!constraint.predifined"
                                id="type"
                                class="kn-material-input"
                                v-model="v$.constraint.valueTypeId.$model"
                                dataKey="id"
                                optionLabel="VALUE_NM"
                                optionValue="VALUE_ID"
                                :options="domains"
                                :class="{
                                    'p-invalid': v$.constraint.valueTypeId.$invalid && v$.constraint.valueTypeId.$dirty
                                }"
                                @blur="v$.constraint.valueTypeId.$touch()"
                                @change="clearInput"
                            />
                            <InputText v-else id="type" class="kn-material-input" type="text" v-model.trim="constraint.valueTypeCd" disabled />
                            <label for="type" class="kn-material-input-label">{{ $t('managers.constraintManagement.type') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.constraint.valueTypeId" :additionalTranslateParams="{ fieldName: $t('managers.constraintManagement.type') }"></KnValidationMessages>
                    </div>
                    <div v-if="!constraint.predifined && constraint.valueTypeId" :class="constraintsManagementDetailDescriptor.firstValue[constraint.valueTypeId].class">
                        <span class="p-float-label">
                            <InputText v-if="!numberType" id="type" class="kn-material-input" type="text" v-model.trim="constraint.firstValue" @input="$emit('touched')" />
                            <InputNumber v-else id="type" inputClass="kn-material-input" v-model="constraint.firstValue" @input="$emit('touched')" />
                            <label for="type" class="kn-material-input-label">{{ $t(constraintsManagementDetailDescriptor.firstValue[constraint.valueTypeId].label) }}</label>
                        </span>
                    </div>
                    <div v-if="!constraint.predifined && constraint.valueTypeId && constraint.valueTypeId == 46" :class="constraintsManagementDetailDescriptor.firstValue[constraint.valueTypeId].class">
                        <span class="p-float-label">
                            <InputNumber
                                id="typeTwo"
                                inputClass="kn-material-input"
                                v-model="v$.constraint.secondValue.$model"
                                :class="{
                                    'p-invalid': v$.constraint.secondValue.$invalid && v$.constraint.secondValue.$dirty
                                }"
                                @blur="v$.constraint.secondValue.$touch()"
                                @input="$emit('touched')"
                            />
                            <label for="typeTwo" class="kn-material-input-label">{{ $t(constraintsManagementDetailDescriptor.firstValue[constraint.valueTypeId].labelTwo) }}</label>
                        </span>
                        <KnValidationMessages
                            class="p-mt-1"
                            :vComp="v$.constraint.secondValue"
                            :additionalTranslateParams="{ fieldName: $t(constraintsManagementDetailDescriptor.firstValue[constraint.valueTypeId].labelTwo) }"
                            :specificTranslateKeys="{ range_check: 'managers.constraintManagement.rangeCheck' }"
                        ></KnValidationMessages>
                    </div>
                </form>
            </template>
        </Card>
    </div>
</template>
<script lang="ts">
import { defineComponent, PropType } from 'vue'
import axios from 'axios'
import { iConstraint } from './ConstraintsManagement'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import Dropdown from 'primevue/dropdown'
import InputNumber from 'primevue/inputnumber'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import constraintsManagementDetailDescriptor from './ConstraintsManagementDetailDescriptor.json'
import constraintsManagementValidationDescriptor from './ConstraintsManagementValidationDescriptor.json'
export default defineComponent({
    name: 'constraint-management-detail',
    components: { Dropdown, KnValidationMessages, InputNumber },
    props: {
        selectedConstraint: {
            type: Object as PropType<iConstraint>,
            required: false
        },
        domains: {
            type: Array as PropType<any[]>,
            required: true
        }
    },
    emits: ['close', 'created', 'touched'],
    data() {
        return {
            constraintsManagementDetailDescriptor,
            constraint: {} as iConstraint,
            constraintsManagementValidationDescriptor,
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    validations() {
        const customValidators: ICustomValidatorMap = {
            'range-check': () => {
                return (this.constraint && this.constraint.firstValue && this.constraint.secondValue && this.constraint.firstValue < this.constraint.secondValue) || this.constraint.valueTypeId != 46
            }
        }
        return {
            constraint: createValidations('constraint', constraintsManagementValidationDescriptor.validations.constraint, customValidators)
        }
    },
    computed: {
        inputDisabled(): any {
            return this.constraint.predifined == true
        },
        buttonDisabled(): any {
            return this.constraint.predifined == true || this.v$.$invalid
        },
        numberType(): any {
            return this.constraint.valueTypeId == 45 || this.constraint.valueTypeId == 46 || this.constraint.valueTypeId == 47 || this.constraint.valueTypeId == 48
        }
    },
    watch: {
        selectedConstraint() {
            this.v$.$reset()
            this.constraint = { ...this.selectedConstraint } as iConstraint
        }
    },
    mounted() {
        if (this.selectedConstraint) {
            this.constraint = { ...this.selectedConstraint } as iConstraint
        }
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            delete this.constraint.predifined
            let selectedDomain = this.domains.filter((cd) => {
                return cd.VALUE_ID == this.constraint?.valueTypeId
            })
            this.constraint.valueTypeCd = selectedDomain[0].VALUE_CD

            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/customChecks/'
            if (this.constraint.checkId) {
                this.operation = 'update'
                url += this.constraint.checkId
            } else {
                this.operation = 'insert'
            }

            await this.sendRequest(url)
                .then((response) => {
                    this.constraint.checkId = response.data
                    this.$store.commit('setInfo', {
                        title: this.$t(this.constraintsManagementDetailDescriptor.operation[this.operation].toastTitle),
                        msg: this.$t(this.constraintsManagementDetailDescriptor.operation.success)
                    })
                    this.$emit('created', this.constraint)
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('managers.constraintManagement.saveError'),
                        msg: error.message
                    })
                })
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return axios.post(url, this.constraint)
            } else {
                return axios.put(url, this.constraint)
            }
        },
        clearInput() {
            this.constraint.firstValue = null
            this.constraint.secondValue = null
            this.$emit('touched')
        },
        closeForm() {
            this.$emit('close')
        }
    }
})
</script>
