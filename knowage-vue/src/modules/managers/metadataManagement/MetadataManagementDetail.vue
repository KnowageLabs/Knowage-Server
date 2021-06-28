<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button class="kn-button p-button-text p-button-rounded" icon="pi pi-times" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <Card>
                <template #content>
                    <form class="p-fluid p-m-5">
                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="label"
                                    class="kn-material-input"
                                    type="text"
                                    v-model.trim="v$.metadata.label.$model"
                                    :class="{
                                        'p-invalid': v$.metadata.label.$invalid && v$.metadata.label.$dirty
                                    }"
                                    @blur="v$.metadata.label.$touch()"
                                    @change="setDirty"
                                    data-test="label-input"
                                />
                                <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                            </span>

                            <KnValidationMessages
                                :vComp="v$.metadata.label"
                                :additionalTranslateParams="{
                                    fieldName: $t('common.label')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="name"
                                    class="kn-material-input"
                                    type="text"
                                    v-model.trim="v$.metadata.name.$model"
                                    :class="{
                                        'p-invalid': v$.metadata.name.$invalid && v$.metadata.name.$dirty
                                    }"
                                    @blur="v$.metadata.name.$touch()"
                                    @change="setDirty"
                                    data-test="name-input"
                                />
                                <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                            </span>

                            <KnValidationMessages
                                :vComp="v$.metadata.name"
                                :additionalTranslateParams="{
                                    fieldName: $t('common.name')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <InputText
                                    id="description"
                                    class="kn-material-input"
                                    type="text"
                                    v-model.trim="v$.metadata.description.$model"
                                    :class="{
                                        'p-invalid': v$.metadata.description.$invalid && v$.metadata.description.$dirty
                                    }"
                                    @blur="v$.metadata.description.$touch()"
                                    @change="setDirty"
                                    data-test="description-input"
                                />
                                <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                            </span>

                            <KnValidationMessages
                                :vComp="v$.metadata.description"
                                :additionalTranslateParams="{
                                    fieldName: $t('common.description')
                                }"
                            >
                            </KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <span class="p-float-label">
                                <Dropdown
                                    id="dataType"
                                    class="kn-material-input"
                                    :class="{
                                        'p-invalid': v$.metadata.dataType.$invalid && v$.metadata.dataType.$dirty
                                    }"
                                    v-model="v$.metadata.dataType.$model"
                                    :options="metadataTypes"
                                    optionLabel="name"
                                    optionValue="value"
                                    @before-show="v$.metadata.dataType.$touch()"
                                    @change="setDirty"
                                    data-test="dataType-dropdown"
                                />
                                <label for="dataType" class="kn-material-input-label">{{ $t('common.type') }} * </label>
                            </span>

                            <KnValidationMessages
                                :vComp="v$.metadata.dataType"
                                :additionalTranslateParams="{
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
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import metadataManagementDescriptor from './MetadataManagementDescriptor.json'
import metadataManagementValidationDescriptor from './MetadataManagementValidationDescriptor.json'
import useValidate from '@vuelidate/core'

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
    data() {
        return {
            metadataManagementDescriptor: metadataManagementDescriptor,
            metadataManagementValidationDescriptor,
            metadata: {} as iMetadata,
            metadataTypes: metadataManagementDescriptor.metadataTypes,
            submitted: false as Boolean,
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

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/objMetadata', this.metadata).then(() => {
                this.$store.commit('setInfo', {
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
