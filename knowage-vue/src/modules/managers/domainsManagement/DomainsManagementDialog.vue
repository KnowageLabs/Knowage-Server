<template>
    <Dialog :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="formHeader" :closable="false" :style="domainsManagementDescriptor.form.style">
        <form ref="domain-form" class="p-fluid p-m-5">
            <div class="p-field" :style="domainsManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="valueCd"
                        v-model.trim="v$.domain.valueCd.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.domain.valueCd.$invalid && v$.domain.valueCd.$dirty
                        }"
                        max-length="100"
                        @blur="v$.domain.valueCd.$touch()"
                    />
                    <label for="valueCd" class="kn-material-input-label"> {{ $t('managers.domainsManagement.valueCode') }} *</label>
                </span>

                <KnValidationMessages :v-comp="v$.domain.valueCd" :additional-translate-params="{ fieldName: $t('managers.domainsManagement.valueCode') }"></KnValidationMessages>
            </div>

            <div class="p-field" :style="domainsManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="valueName"
                        v-model.trim="v$.domain.valueName.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.domain.valueName.$invalid && v$.domain.valueName.$dirty
                        }"
                        max-length="40"
                        @blur="v$.domain.valueName.$touch()"
                    />
                    <label for="valueName" class="kn-material-input-label">{{ $t('managers.domainsManagement.valueName') }} * </label>
                </span>

                <KnValidationMessages :v-comp="v$.domain.valueName" :additional-translate-params="{ fieldName: $t('managers.domainsManagement.valueName') }"></KnValidationMessages>
            </div>

            <div class="p-field" :style="domainsManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="domainCode"
                        v-model.trim="v$.domain.domainCode.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.domain.domainCode.$invalid && v$.domain.domainCode.$dirty
                        }"
                        max-length="20"
                        @blur="v$.domain.domainCode.$touch()"
                    />
                    <label for="domainCode" class="kn-material-input-label">{{ $t('managers.domainsManagement.domainCode') }} * </label>
                </span>

                <KnValidationMessages :v-comp="v$.domain.domainCode" :additional-translate-params="{ fieldName: $t('managers.domainsManagement.domainCode') }"></KnValidationMessages>
            </div>

            <div class="p-field" :style="domainsManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="domainName"
                        v-model.trim="v$.domain.domainName.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.domain.domainName.$invalid && v$.domain.domainName.$dirty
                        }"
                        max-length="40"
                        @blur="v$.domain.domainName.$touch()"
                    />
                    <label for="domainName" class="kn-material-input-label">
                        {{ $t('managers.domainsManagement.domainName') }}
                        *
                    </label>
                </span>

                <KnValidationMessages :v-comp="v$.domain.domainName" :additional-translate-params="{ fieldName: $t('managers.domainsManagement.domainName') }"></KnValidationMessages>
            </div>

            <div class="p-field" :style="domainsManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="valueDescription"
                        v-model.trim="v$.domain.valueDescription.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.domain.valueDescription.$invalid && v$.domain.valueDescription.$dirty
                        }"
                        max-length="160"
                        @blur="v$.domain.valueDescription.$touch()"
                    />
                    <label for="valueDescription" class="kn-material-input-label">{{ $t('managers.domainsManagement.valueDescription') }} * </label>
                </span>

                <KnValidationMessages :v-comp="v$.domain.valueDescription" :additional-translate-params="{ fieldName: $t('managers.domainsManagement.valueDescription') }"></KnValidationMessages>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" @click="handleSubmit"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDomain } from './DomainsManagement'
import { createValidations } from '../../../helpers/commons/validationHelper'
import Dialog from 'primevue/dialog'
import domainsManagementDescriptor from './DomainsManagementDescriptor.json'
import domainsManagementValidationDescriptor from './DomainsManagementValidationDescriptor.json'
import KnValidationMessages from '../../../components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'domain-management-dialog',
    components: { Dialog, KnValidationMessages },
    props: {
        model: {
            type: Object,
            requried: false
        }
    },
    emits: ['close', 'created'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            domainsManagementDescriptor: domainsManagementDescriptor,
            domainsManagementValidationDescriptor,
            domain: {} as iDomain,
            dirty: false,
            v$: useValidate() as any,
            operation: 'insert'
        }
    },
    validations() {
        return {
            domain: createValidations('domain', domainsManagementValidationDescriptor.validations.domain)
        }
    },
    computed: {
        formHeader(): any {
            return this.domain.valueId ? this.$t('common.edit') : this.$t('common.new')
        },
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        model() {
            this.domain = { ...this.model } as iDomain
        }
    },
    mounted() {
        if (this.model) {
            this.domain = { ...this.model } as iDomain
        }
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }

            let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains'
            if (this.domain.valueId) {
                this.operation = 'update'
                url += '/' + this.domain.valueId
            }

            await this.sendRequest(url).then(() => {
                this.store.setInfo({
                    title: this.$t(this.domainsManagementDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.domainsManagementDescriptor.operation.success)
                })
                this.$emit('created')
            })
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return this.$http.post(url, this.domain)
            } else {
                return this.$http.put(url, this.domain)
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
