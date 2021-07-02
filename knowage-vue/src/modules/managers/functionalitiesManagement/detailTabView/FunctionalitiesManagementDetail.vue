<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left> {{ selectedFolder.name }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" />
        </template>
    </Toolbar>
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
                            data-test="label-input"
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
                        <InputText id="description" class="kn-material-input" type="text" v-model.trim="selectedFolder.description" maxLength="255" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import detailDescriptor from './FunctionalitiesManagementDetailDescriptor.json'
import validationDescriptor from './FunctionalitiesManagementValidation.json'
import Card from 'primevue/card'

export default defineComponent({
    emits: ['touched', 'close'],
    props: {
        functionality: Object
    },
    components: {
        Card,
        KnValidationMessages
    },
    data() {
        return {
            v$: useValidate() as any,
            detailDescriptor,
            validationDescriptor,
            formVisible: false,
            loading: false,
            selectedFolder: {} as any
        }
    },
    validations() {
        return {
            selectedFolder: createValidations('selectedFolder', validationDescriptor.validations.selectedFolder)
        }
    },
    created() {
        this.selectedFolder = { ...this.functionality }
    },
    watch: {
        functionality() {
            this.selectedFolder = { ...this.functionality }
            console.log(this.selectedFolder)
        }
    },
    methods: {
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped></style>
