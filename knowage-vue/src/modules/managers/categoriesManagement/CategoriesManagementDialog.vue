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
                        :disabled="inputDisabled"
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

        <div v-if="propCategory?.id && categoryTags.length > 0" class="p-d-flex p-flex-column" :style="descriptor.chipsTable">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start> {{ $t('managers.categoriesManagement.occurrences') }} </template>
            </Toolbar>
            <div class="p-grid p-m-1">
                <span v-for="(tag, index) of categoryTags" :key="index" class="detail-chips"> {{ tag.name }}</span>
            </div>
        </div>

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
import { AxiosResponse } from 'axios'
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
            type: Object as PropType<iCategory> | any,
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
            operation: 'insert',
            loading: false,
            categoryTags: [] as any
        }
    },
    validations() {
        return {
            category: createValidations('category', validationDescriptor.validations.category)
        }
    },
    computed: {
        formHeader(): any {
            return this.propCategory?.id ? this.$t('common.edit') : this.$t('common.new')
        },
        inputDisabled(): any {
            return this.propCategory
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
    created() {
        if (this.propCategory?.id) this.loadCategoryData()
    },
    mounted() {
        if (this.propCategory) {
            this.category = { ...this.propCategory } as iCategory
        }
    },
    methods: {
        async loadCategoryData() {
            this.loading = true
            const url = this.getCategoryTagsUrl() as string
            await this.$http
                .get(url)
                .then((response: AxiosResponse<any>) => (this.categoryTags = response.data))
                .finally(() => (this.loading = false))
        },
        getCategoryTagsUrl() {
            switch (this.propCategory.type) {
                case 'DATASET_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/dataset/${this.propCategory.id}`
                case 'BM_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/metamodel/${this.propCategory.id}`
                case 'GEO_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/geolayer/${this.propCategory.id}`
                case 'KPI_KPI_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/kpi/${this.propCategory.id}`
                case 'KPI_TARGET_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/kpitarget/${this.propCategory.id}`
                case 'KPI_MEASURE_CATEGORY':
                    return import.meta.env.VITE_RESTFUL_SERVICES_PATH + `3.0/category/kpiruleoutput/${this.propCategory.id}`
            }
        },
        async handleSubmit() {
            if (this.v$.$invalid) return
            if (this.category.id) this.operation = 'update'

            await this.sendRequest().then(() => {
                this.store.setInfo({
                    title: this.$t(this.descriptor.operation[this.operation].toastTitle),
                    msg: this.$t(this.descriptor.operation.success)
                })
                this.$emit('created')
            })
        },
        sendRequest() {
            if (this.operation === 'insert') return this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '3.0/category', this.category)
            else {
                delete this.category.occurrences
                return this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '3.0/category/update', this.category)
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
<style lang="scss">
.detail-chips {
    color: rgba(0, 0, 0, 0.87);
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    background-color: #ccc;
    padding: 2px 8px;
    border-radius: 20px;
    margin: 2px;
    font-size: 0.85rem;
    margin-bottom: 2px;
}
</style>
