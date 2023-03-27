<template>
    <Dialog :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :header="formHeader" :closable="false" :style="descriptor.form.style">
        <form class="p-fluid p-formgrid p-grid p-mt-5">
            <div class="p-field p-col-12 p-mb-3" :style="descriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="code"
                        v-model.trim="v$.category.code.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.category.code.$invalid && v$.category.code.$dirty
                        }"
                        max-length="100"
                        @blur="v$.category.code.$touch()"
                    />
                    <label for="code" class="kn-material-input-label"> {{ $t('managers.categoriesManagement.code') }} *</label>
                </span>
                <KnValidationMessages :v-comp="v$.category.code" :additional-translate-params="{ fieldName: $t('managers.categoriesManagement.code') }"></KnValidationMessages>
            </div>

            <div class="p-field p-col-12 p-mb-3" :style="descriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        v-model.trim="v$.category.name.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.category.name.$invalid && v$.category.name.$dirty
                        }"
                        max-length="100"
                        @blur="v$.category.name.$touch()"
                    />
                    <label for="name" class="kn-material-input-label">{{ $t('managers.categoriesManagement.name') }} * </label>
                </span>
                <KnValidationMessages :v-comp="v$.category.name" :additional-translate-params="{ fieldName: $t('managers.categoriesManagement.name') }"></KnValidationMessages>
            </div>

            <div class="p-field p-col-12 p-mb-3" :style="descriptor.pField.style">
                <span class="p-float-label">
                    <InputText
                        id="type"
                        v-model.trim="v$.category.type.$model"
                        class="kn-material-input"
                        type="text"
                        :class="{
                            'p-invalid': v$.category.type.$invalid && v$.category.type.$dirty
                        }"
                        max-length="500"
                        @blur="v$.category.type.$touch()"
                    />
                    <label for="type" class="kn-material-input-label">{{ $t('managers.categoriesManagement.type') }} * </label>
                </span>
                <KnValidationMessages :v-comp="v$.category.type" :additional-translate-params="{ fieldName: $t('managers.categoriesManagement.type') }"></KnValidationMessages>
            </div>
        </form>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="closeTemplate"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" @click="handleSubmit"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iCategory } from './CategoriesManagement'
import { createValidations } from '../../../helpers/commons/validationHelper'
import Dialog from 'primevue/dialog'
import descriptor from './CategoriesManagementDescriptor.json'
import validationDescriptor from './CategoriesManagementValidationDescriptor.json'
import KnValidationMessages from '../../../components/UI/KnValidatonMessages.vue'
import useValidate from '@vuelidate/core'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'category-management-dialog',
    components: { Dialog, KnValidationMessages },
    props: {
        propCategory: {
            type: Object as PropType<iCategory> | null,
            required: true
        }
    },
    emits: ['close', 'created'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            descriptor: descriptor,
            validationDescriptor,
            category: {} as iCategory,
            dirty: false,
            v$: useValidate() as any,
            operation: 'insert'
        }
    },
    validations() {
        return {
            category: createValidations('category', validationDescriptor.validations.category)
        }
    },
    computed: {
        formHeader(): any {
            // return this.category.valueId ? this.$t('common.edit') : this.$t('common.new')
            return 'TEST'
        },
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        propCategory() {
            this.category = { ...this.propCategory } as iCategory
        }
    },
    mounted() {
        if (this.propCategory) {
            this.category = { ...this.propCategory } as iCategory
        }
    },
    methods: {
        async handleSubmit() {
            // if (this.v$.$invalid) {
            //     return
            // }
            // let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/domains'
            // if (this.category.valueId) {
            //     this.operation = 'update'
            //     url += '/' + this.category.valueId
            // }
            // await this.sendRequest(url).then(() => {
            //     this.store.setInfo({
            //         title: this.$t(this.descriptor.operation[this.operation].toastTitle),
            //         msg: this.$t(this.descriptor.operation.success)
            //     })
            //     this.$emit('created')
            // })
        },
        sendRequest(url: string) {
            if (this.operation === 'insert') {
                return this.$http.post(url, this.category)
            } else {
                return this.$http.put(url, this.category)
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
