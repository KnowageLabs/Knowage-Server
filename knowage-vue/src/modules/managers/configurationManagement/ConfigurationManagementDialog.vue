<template>
    <Dialog :visible="true" :modal="true" class="kn-dialog--toolbar--primary" :header="formHeader" :closable="false" :style="configurationManagementDescriptor.form.style">
        <form class="p-fluid p-m-5">
            <div class="p-field" :style="configurationManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="label"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.configuration.label.$model"
                        :class="{
                            'p-invalid': v$.configuration.label.$invalid && v$.configuration.label.$dirty
                        }"
                        maxLength="100"
                        @blur="v$.configuration.label.$touch()"
                    />
                    <label for="label" class="kn-material-input-label"> {{ $t('managers.configurationManagement.headers.label') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.configuration.label"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.label')
                    }"
                ></KnValidationMessages>
            </div>

            <div class="p-field" :style="configurationManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.configuration.name.$model"
                        :class="{
                            'p-invalid': v$.configuration.name.$invalid && v$.configuration.name.$dirty
                        }"
                        maxLength="100"
                        @blur="v$.configuration.name.$touch()"
                    />
                    <label for="name" class="kn-material-input-label"> {{ $t('managers.configurationManagement.headers.name') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.configuration.name"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.name')
                    }"
                ></KnValidationMessages>
            </div>

            <div class="p-field" :style="configurationManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="description"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.configuration.description.$model"
                        :class="{
                            'p-invalid': v$.configuration.description.$invalid && v$.configuration.description.$dirty
                        }"
                        maxLength="500"
                        @blur="v$.configuration.description.$touch()"
                    />
                    <label for="description" class="kn-material-input-label">
                        {{ $t('managers.configurationManagement.headers.description') }}
                    </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.configuration.description"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.description')
                    }"
                ></KnValidationMessages>
            </div>

            <div class="p-field" :style="configurationManagementDescriptor.pField.style">
                <span class="p-float-label">
                    <Dropdown
                        id="category"
                        class="kn-material-input"
                        v-model="v$.configuration.category.$model"
                        :class="{
                            'p-invalid': v$.configuration.category.$invalid && v$.configuration.category.$dirty
                        }"
                        :options="configurationManagementDescriptor.category"
                        optionLabel="name"
                        optionValue="value"
                        @before-show="v$.configuration.category.$touch()"
                    />
                    <label for="category" class="kn-material-input-label"> {{ $t('managers.configurationManagement.headers.category') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.configuration.category"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.category')
                    }"
                ></KnValidationMessages>
            </div>

            <div class="p-field" :style="configurationManagementDescriptor.pField.style">
                <div class="p-field-checkbox">
                    <Checkbox id="isActive" v-model="v$.configuration.active.$model" :binary="true" />
                    <label for="isActive"> {{ $t('managers.configurationManagement.headers.active') }} *</label>
                </div>
                <KnValidationMessages
                    :vComp="v$.configuration.active"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.configurationManagement.headers.active')
                    }"
                ></KnValidationMessages>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" @click="handleSubmit" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { iConfiguration } from './ConfigurationManagement'
import configurationManagementDescriptor from './ConfigurationManagementDescriptor.json'
import configurationManagementValidationDescriptor from './ConfigurationManagementValidationDescriptor.json'
import axios from 'axios'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'configuration-management-dialog',
    components: { Dialog, Dropdown, Checkbox, KnValidationMessages },
    props: {
        model: {
            type: Object,
            requried: false
        }
    },
    emits: ['close', 'created'],
    data() {
        return {
            configurationManagementDescriptor: configurationManagementDescriptor,
            configurationManagementValidationDescriptor: configurationManagementValidationDescriptor,
            configuration: {} as iConfiguration,
            v$: useValidate() as any,
            operation: 'insert',
            dirty: false,
            options: [true, false]
        }
    },
    validations() {
        return {
            configuration: createValidations('configuration', configurationManagementValidationDescriptor.validations.configuration)
        }
    },
    computed: {
        formHeader(): any {
            return this.configuration.id ? this.configuration.name : this.$t('managers.configurationManagement.createNewHeader')
        },
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    mounted() {
        if (this.model) {
            this.configuration = { ...this.model } as iConfiguration
        }
    },
    watch: {
        model() {
            this.configuration = { ...this.model } as iConfiguration
        }
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }
            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/configs'
            console.log(this.configuration)
            if (this.configuration.id) {
                this.operation = 'update'
                url += '/' + this.configuration.id
            }
            await this.sendRequest(url).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t(this.configurationManagementDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.configurationManagementDescriptor.operation.success)
                })
                this.$emit('created')
            })
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return axios.post(url, this.configuration)
            } else {
                return axios.put(url, this.configuration)
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
