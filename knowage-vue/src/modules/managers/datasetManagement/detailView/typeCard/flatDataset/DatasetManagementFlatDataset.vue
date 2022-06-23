<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="flatTableName"
                            class="kn-material-input"
                            v-model.trim="v$.dataset.flatTableName.$model"
                            :class="{
                                'p-invalid': v$.dataset.flatTableName.$invalid && v$.dataset.flatTableName.$dirty
                            }"
                            @blur="v$.dataset.flatTableName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="flatTableName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.flatTableName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.flatTableName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.flatTableName') }" />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="dataSourceFlat"
                            class="kn-material-input"
                            :options="dataSources"
                            optionLabel="label"
                            optionValue="label"
                            v-model="v$.dataset.dataSourceFlat.$model"
                            :class="{
                                'p-invalid': v$.dataset.dataSourceFlat.$invalid && v$.dataset.dataSourceFlat.$dirty
                            }"
                            @before-show="v$.dataset.dataSourceFlat.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.businessModelManager.dataSource') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.dataSourceFlat"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.businessModelManager.dataSource')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import flatTypeDescriptor from './DatasetManagementFlatDataset.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import mainStore from '../../../../../../App.store'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            flatTypeDescriptor,
            dataset: {} as any,
            v$: useValidate() as any
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    validations() {
        const flatFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Flat' || value
        }
        const customValidators: ICustomValidatorMap = { 'flat-fields-required': flatFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', flatTypeDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        changeTypeWarning() {
            this.store.setInfo({ title: this.$t('documentExecution.registry.warning'), msg: this.$t('managers.datasetManagement.changeTypeMsg') })
        }
    }
})
</script>
