<template>
    <Card class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <Dropdown
                            id="ckanFileType"
                            class="kn-material-input"
                            :options="availableFileTypes"
                            optionLabel="name"
                            optionValue="name"
                            v-model="v$.dataset.ckanFileType.$model"
                            :class="{
                                'p-invalid': v$.dataset.ckanFileType.$invalid && v$.dataset.ckanFileType.$dirty
                            }"
                            @before-show="v$.dataset.ckanFileType.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.fileType') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.ckanFileType"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.fileType')
                        }"
                    />
                </div>

                <div v-if="dataset.ckanFileType == 'XLS'" class="p-formgrid p-grid p-col-12">
                    <div class="p-field p-col-4">
                        <span class="p-float-label">
                            <InputText id="ckanSkipRows" class="kn-material-input" type="number" v-model.trim="dataset.ckanSkipRows" @change="$emit('touched')" />
                            <label for="ckanSkipRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanSkipRows') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-4">
                        <span class="p-float-label">
                            <InputText id="ckanLimitRows" class="kn-material-input" type="number" v-model.trim="dataset.ckanLimitRows" @change="$emit('touched')" />
                            <label for="ckanLimitRows" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanLimitRows') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-4">
                        <span class="p-float-label">
                            <InputText id="ckanXslSheetNumber" class="kn-material-input" type="number" v-model.trim="dataset.ckanXslSheetNumber" @change="$emit('touched')" />
                            <label for="ckanXslSheetNumber" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanXslSheetNumber') }} </label>
                        </span>
                    </div>
                </div>

                <div v-if="dataset.ckanFileType == 'CSV'" class="p-formgrid p-grid p-col-12">
                    <div class="p-field p-col-3">
                        <span class="p-float-label">
                            <Dropdown
                                id="ckanCsvDelimiter"
                                class="kn-material-input"
                                :options="csvDelimiterCharacterTypes"
                                optionLabel="name"
                                optionValue="name"
                                v-model="v$.dataset.ckanCsvDelimiter.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanCsvDelimiter.$invalid && v$.dataset.ckanCsvDelimiter.$dirty
                                }"
                                @before-show="v$.dataset.ckanCsvDelimiter.$touch()"
                            />
                            <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvDelimiter') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.dataset.ckanCsvDelimiter"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.datasetManagement.ckanCsvDelimiter')
                            }"
                        />
                    </div>
                    <div class="p-field p-col-3">
                        <span class="p-float-label">
                            <Dropdown
                                id="ckanCsvQuote"
                                class="kn-material-input"
                                :options="csvQuoteCharacterTypes"
                                optionLabel="name"
                                optionValue="name"
                                v-model="v$.dataset.ckanCsvQuote.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanCsvQuote.$invalid && v$.dataset.ckanCsvQuote.$dirty
                                }"
                                @before-show="v$.dataset.ckanCsvQuote.$touch()"
                            />
                            <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvQuote') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.dataset.ckanCsvQuote"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.datasetManagement.ckanCsvQuote')
                            }"
                        />
                    </div>
                    <div class="p-field p-col-3">
                        <span class="p-float-label">
                            <Dropdown
                                id="ckanCsvEncoding"
                                class="kn-material-input"
                                :options="csvEncodingTypes"
                                optionLabel="name"
                                optionValue="name"
                                v-model="v$.dataset.ckanCsvEncoding.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanCsvEncoding.$invalid && v$.dataset.ckanCsvEncoding.$dirty
                                }"
                                @before-show="v$.dataset.ckanCsvEncoding.$touch()"
                            />
                            <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanCsvEncoding') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.dataset.ckanCsvEncoding"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.datasetManagement.ckanCsvEncoding')
                            }"
                        />
                    </div>
                    <div class="p-field p-col-3">
                        <span class="p-float-label">
                            <Dropdown
                                id="ckanDateFormat"
                                class="kn-material-input"
                                :options="ckanDateFormat"
                                optionLabel="name"
                                optionValue="name"
                                v-model="v$.dataset.ckanDateFormat.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanDateFormat.$invalid && v$.dataset.ckanDateFormat.$dirty
                                }"
                                @before-show="v$.dataset.ckanDateFormat.$touch()"
                            />
                            <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.dataset.ckanDateFormat"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.datasetManagement.ckanDateFormat')
                            }"
                        />
                    </div>
                </div>

                <div v-if="dataset.ckanFileType" class="p-formgrid p-grid p-col-12">
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="ckanId"
                                class="kn-material-input"
                                v-model.trim="v$.dataset.ckanId.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanId.$invalid && v$.dataset.ckanId.$dirty
                                }"
                                @blur="v$.dataset.ckanId.$touch()"
                                @change="$emit('touched')"
                            />
                            <label for="ckanId" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanId') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.ckanId" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.ckanId') }" />
                    </div>
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="ckanUrl"
                                class="kn-material-input"
                                v-model.trim="v$.dataset.ckanUrl.$model"
                                :class="{
                                    'p-invalid': v$.dataset.ckanUrl.$invalid && v$.dataset.ckanUrl.$dirty
                                }"
                                @blur="v$.dataset.ckanUrl.$touch()"
                                @change="$emit('touched')"
                            />
                            <label for="ckanUrl" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanUrl') }} * </label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.ckanUrl" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.ckanUrl') }" />
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
import ckanDescriptor from './DatasetManagementCkanDatasetDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages },
    props: { selectedDataset: { type: Object as any } },
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            ckanDescriptor,
            dataset: {} as any,
            ckanDateFormat: ckanDescriptor.ckanDateFormat,
            availableFileTypes: ckanDescriptor.ckanFileTypes,
            csvEncodingTypes: ckanDescriptor.csvEncodingTypes,
            csvQuoteCharacterTypes: ckanDescriptor.csvQuoteCharacterTypes,
            csvDelimiterCharacterTypes: ckanDescriptor.csvDelimiterCharacterTypes
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
        const ckanFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Ckan' || value
        }
        const csvFieldsRequired = (value) => {
            return this.dataset.ckanFileType != 'CSV' || value
        }
        const customValidators: ICustomValidatorMap = { 'ckan-fields-required': ckanFieldsRequired, 'csv-fields-required': csvFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', ckanDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {}
})
</script>
