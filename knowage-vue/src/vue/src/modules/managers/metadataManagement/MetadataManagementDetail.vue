<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #end>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center" data-test="metadata-form">
        <div class="p-col-9">
            <Card>
                <template #content>
                    <form class="p-fluid p-m-5">
                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="label"
                                    v-model.trim="v$.metadata.label.$model"
                                    class="kn-material-input"
                                    type="text"
                                    :class="{
                                        'p-invalid': v$.metadata.label.$invalid && v$.metadata.label.$dirty
                                    }"
                                    data-test="label-input"
                                    @blur="v$.metadata.label.$touch()"
                                    @change="setDirty"
                                />
                                <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                            </span>

                            <KnValidationMessages
                                :v-comp="v$.metadata.label"
                                :additional-translate-params="{
                                    fieldName: $t('common.label')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="name"
                                    v-model.trim="v$.metadata.name.$model"
                                    class="kn-material-input"
                                    type="text"
                                    :class="{
                                        'p-invalid': v$.metadata.name.$invalid && v$.metadata.name.$dirty
                                    }"
                                    data-test="name-input"
                                    @blur="v$.metadata.name.$touch()"
                                    @change="setDirty"
                                />
                                <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                            </span>

                            <KnValidationMessages
                                :v-comp="v$.metadata.name"
                                :additional-translate-params="{
                                    fieldName: $t('common.name')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="description"
                                    v-model.trim="v$.metadata.description.$model"
                                    class="kn-material-input"
                                    type="text"
                                    :class="{
                                        'p-invalid': v$.metadata.description.$invalid && v$.metadata.description.$dirty
                                    }"
                                    data-test="description-input"
                                    @blur="v$.metadata.description.$touch()"
                                    @change="setDirty"
                                />
                                <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                            </span>

                            <KnValidationMessages
                                :v-comp="v$.metadata.description"
                                :additional-translate-params="{
                                    fieldName: $t('common.description')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <Dropdown
                                    id="dataType"
                                    v-model="v$.metadata.dataType.$model"
                                    class="kn-material-input"
                                    :class="{
                                        'p-invalid': v$.metadata.dataType.$invalid && v$.metadata.dataType.$dirty
                                    }"
                                    :options="metadataTypes"
                                    option-label="name"
                                    option-value="value"
                                    data-test="dataType-dropdown"
                                    @before-show="v$.metadata.dataType.$touch()"
                                    @change="setDirty"
                                />
                                <label for="dataType" class="kn-material-input-label">{{ $t('common.type') }} * </label>
                            </span>

                            <KnValidationMessages
                                :v-comp="v$.metadata.dataType"
                                :additional-translate-params="{
                                    fieldName: $t('common.type')
                                }"
                            >
                            </KnValidationMessages>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMetadata } from './MetadataManagement'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import metadataManagementDescriptor from './MetadataManagementDescriptor.json'
import metadataManagementValidationDescriptor from './MetadataManagementValidationDescriptor.json'
import useValidate from '@vuelidate/core'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'metadata-management-detail',
    components: {
        Dropdown,
        KnValidationMessages
    },
    props: {
        model: {
            type: Object,
            required: false
        }
    },
    emits: ['close', 'saved', 'touched'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            metadataManagementDescriptor: metadataManagementDescriptor,
            metadataManagementValidationDescriptor,
            metadata: {} as iMetadata,
            metadataTypes: metadataManagementDescriptor.metadataTypes,
            submitted: false as boolean,
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            metadata: createValidations('metadata', metadataManagementValidationDescriptor.validations.metadata)
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        },
        title(): any {
            return this.metadata.id ? this.$t('common.edit') : this.$t('common.new')
        }
    },
    watch: {
        model() {
            this.v$.$reset()
            this.metadata = { ...this.model } as iMetadata
        }
    },
    mounted() {
        if (this.model) {
            this.metadata = { ...this.model } as iMetadata
        }
    },
    methods: {
        async handleSubmit() {
            if (this.v$.$invalid) {
                return
            }

            if (this.metadata.id) {
                this.operation = 'update'
            }

            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/objMetadata', this.metadata).then(() => {
                this.store.setInfo({
                    title: this.$t(this.metadataManagementDescriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.metadataManagementDescriptor.operation.success)
                })
                this.$emit('saved')
            })
        },
        closeTemplate() {
            this.$emit('close')
        },
        setDirty(): void {
            this.$emit('touched')
        }
    }
})
</script>
