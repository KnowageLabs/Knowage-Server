<template>
    <Card class="p-mt-4">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field  p-col-12">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" maxLength="50" v-model="v$.dataset.name.$model" :class="{ 'p-invalid': v$.dataset.name.$invalid && v$.dataset.name.$dirty }" @blur="v$.dataset.name.$touch()" @change="$emit('touched')" data-test="name-input" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field  p-col-12">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" maxLength="50" v-model="dataset.description" data-test="description-input" />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                </div>
                <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-1 p-mx-2" :style="dataViewDescriptor.style.maxwidth">
                    <template #left>
                        <InputSwitch v-model="dataset.exportToHdfs" class="p-mr-2" @change="$emit('touched')" />
                        <span>{{ $t('managers.datasetManagement.isPersistedHDFS') }}</span>
                    </template>
                </Toolbar>
                <div class="persistence-container" :style="dataViewDescriptor.style.maxwidth">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3 p-mx-2">
                        <template #left>
                            <InputSwitch v-model="dataset.persist" :disabled="disablePersist" class="p-mr-2" @change="$emit('touched')" />
                            <span>{{ $t('managers.datasetManagement.isPersisted') }} </span>
                        </template>
                    </Toolbar>
                    <div v-if="dataset.persist" class="p-field">
                        <span class="p-float-label p-mt-3 p-mx-2">
                            <InputText
                                id="persistTableName"
                                class="kn-material-input"
                                type="text"
                                maxLength="50"
                                v-model="dataset.tableName"
                                :class="{
                                    'p-invalid': v$.dataset.tableName.$invalid && v$.dataset.tableName.$dirty
                                }"
                                @blur="v$.dataset.tableName.$touch()"
                                @change="$emit('touched')"
                            />
                            <label for="persistTableName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.persistTableName') }} *</label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.tableName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.persistTableName') }" />
                    </div>
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
import dataViewDescriptor from './WorkspaceDatasetWizardDescriptor.json'
import Card from 'primevue/card'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    components: { Card, KnValidationMessages, InputSwitch },
    props: { selectedDataset: { type: Object as any } },
    data() {
        return {
            v$: useValidate() as any,
            dataViewDescriptor,
            dataset: {} as any
        }
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
        const wizardFieldsRequired = (value) => {
            return value
        }
        const persistFieldsRequired = (value) => {
            return !this.dataset.isPersisted || value
        }
        const customValidators: ICustomValidatorMap = {
            'wizard-field-required': wizardFieldsRequired,
            'persist-field-required': persistFieldsRequired
        }
        const validationObject = {
            dataset: createValidations('dataset', dataViewDescriptor.validations.dataset, customValidators)
        }
        return validationObject
    }
})
</script>
