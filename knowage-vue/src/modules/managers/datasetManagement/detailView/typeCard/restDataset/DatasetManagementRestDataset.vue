<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="restAddress"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.restAddress.$model"
                            :class="{
                                'p-invalid': v$.dataset.restAddress.$invalid && v$.dataset.restAddress.$dirty
                            }"
                            @blur="v$.dataset.restAddress.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="restAddress" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restAddress') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.restAddress" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.restAddress') }" />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="solrCollection" class="kn-material-input" v-model.trim="dataset.solrCollection" @change="$emit('touched')" />
                        <label for="solrCollection" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrCollection') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="restRequestBody" class="kn-material-input" maxLength="2000" v-model.trim="dataset.restRequestBody" @change="$emit('touched')" />
                        <label for="restRequestBody" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restRequestBody') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <Dropdown
                            id="restHttpMethod"
                            class="kn-material-input"
                            :options="httpMethods"
                            optionLabel="value"
                            optionValue="value"
                            v-model="v$.dataset.restHttpMethod.$model"
                            :class="{
                                'p-invalid': v$.dataset.restHttpMethod.$invalid && v$.dataset.restHttpMethod.$dirty
                            }"
                            @before-show="v$.dataset.restHttpMethod.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restHttpMethod') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.restHttpMethod"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.restHttpMethod')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>

    <RequestHeadersTable :selectedDataset="selectedDataset" />

    <Card class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-col-12 p-field" :style="restDescriptor.style.infoColumnsContainer">
                    <span class="p-float-label" :style="restDescriptor.style.maxWidth">
                        <InputText id="restJsonPathItems" class="kn-material-input" :style="restDescriptor.style.maxWidth" v-model.trim="dataset.restJsonPathItems" @change="$emit('touched')" />
                        <label for="restJsonPathItems" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restJsonPathItems') }} </label>
                    </span>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="jsonItemsHelpVisible = true" />
                </div>
                <div class="p-col-12" :style="restDescriptor.style.infoColumnsContainer">
                    <span class="p-field-checkbox">
                        <label for="binary">{{ $t('managers.datasetManagement.restDirectlyJSONAttributes') }}: </label>
                        <Checkbox id="binary" class="p-ml-2" v-model="dataset.restDirectlyJSONAttributes" :binary="true" @change="$emit('touched')" />
                    </span>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="directAttributesHelpVisible = true" />
                </div>
                <div class="p-col-12" :style="restDescriptor.style.infoColumnsContainer">
                    <span class="p-field-checkbox">
                        <label for="binary">{{ $t('managers.datasetManagement.restNGSI') }}: </label>
                        <Checkbox id="binary" class="p-ml-2" v-model="dataset.restNGSI" :binary="true" @change="$emit('touched')" />
                    </span>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="ngsiHelpVisible = true" />
                </div>
            </form>
        </template>
    </Card>

    <JsonPathTable :selectedDataset="selectedDataset" />

    <Card class="p-mt-3">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText id="restOffset" type="number" class="kn-material-input" v-model.trim="dataset.restOffset" @change="$emit('touched')" />
                        <label for="restOffset" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restOffset') }} </label>
                    </span>
                </div>
                <div class="p-col-4">
                    <span class="p-float-label">
                        <InputText id="restFetchSize" type="number" class="kn-material-input" v-model.trim="dataset.restFetchSize" @change="$emit('touched')" />
                        <label for="restFetchSize" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restFetchSize') }} </label>
                    </span>
                </div>
                <div class="p-col-4">
                    <span class="p-float-label">
                        <InputText id="restMaxResults" type="number" class="kn-material-input" v-model.trim="dataset.restMaxResults" @change="$emit('touched')" />
                        <label for="restMaxResults" class="kn-material-input-label"> {{ $t('managers.datasetManagement.restMaxResults') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>

    <JsonItemsHelpDialog :visible="jsonItemsHelpVisible" @close="jsonItemsHelpVisible = false" />
    <DirectAttributesHelpDialog :visible="directAttributesHelpVisible" @close="directAttributesHelpVisible = false" />
    <NgsiHelpDialog :visible="ngsiHelpVisible" @close="ngsiHelpVisible = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import JsonItemsHelpDialog from '../infoDialogs/DatasetManagementJsonPathItemsInfoDialog.vue'
import DirectAttributesHelpDialog from '../infoDialogs/DatasetManagementAttributesDirectInfoDialog.vue'
import NgsiHelpDialog from '../infoDialogs/DatasetManagementNgsiInfoDialog.vue'
import RequestHeadersTable from '../tables/DatasetManagementRequestHeadersTable.vue'
import JsonPathTable from '../tables/DatasetManagementJsonPathAttributesTable.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import restDescriptor from './DatasetManagementRestDatasetDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, RequestHeadersTable, JsonPathTable, Checkbox, JsonItemsHelpDialog, DirectAttributesHelpDialog, NgsiHelpDialog },
    props: { selectedDataset: { type: Object as any } },
    emits: ['touched'],
    data() {
        return {
            restDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            ngsiHelpVisible: false,
            jsonItemsHelpVisible: false,
            directAttributesHelpVisible: false,
            httpMethods: restDescriptor.httpMethods
        }
    },
    created() {
        this.setDatasetToComponent()
    },
    watch: {
        selectedDataset() {
            this.setDatasetToComponent()
        }
    },
    validations() {
        const restFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'REST' || value
        }
        const customValidators: ICustomValidatorMap = { 'rest-fields-required': restFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', restDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        setDatasetToComponent() {
            this.dataset = this.selectedDataset

            this.dataset.restDirectlyJSONAttributes = Boolean(this.dataset.restDirectlyJSONAttributes)
            this.dataset.restNGSI = this.dataset.restNGSI === 'true'
        }
    }
})
</script>
