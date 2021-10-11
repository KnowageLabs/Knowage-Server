<template>
    <Card class="p-mt-3">
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
                <div class="p-field p-col-6 p-float-label">
                    <span class="p-float-label">
                        <InputText
                            id="solrCollection"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.solrCollection.$model"
                            :class="{
                                'p-invalid': v$.dataset.solrCollection.$invalid && v$.dataset.solrCollection.$dirty
                            }"
                            @blur="v$.dataset.solrCollection.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="solrCollection" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrCollection') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.solrCollection" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.solrCollection') }" />
                </div>
                <div class="p-field p-col-12 p-float-label">
                    <InputText id="restRequestBody" class="kn-material-input" type="text" maxLength="2000" v-model.trim="dataset.restRequestBody" @change="$emit('touched')" />
                    <label for="restRequestBody" class="kn-material-input-label"> {{ $t('kpi.measureDefinition.query') }} </label>
                </div>
                <div class="p-field-radiobutton p-col-12 p-mt-2">
                    <RadioButton name="DOCUMENTS" value="DOCUMENTS" v-model="dataset.solrType" />
                    <label for="DOCUMENTS">DOCUMENTS</label>
                    <RadioButton name="FACETS" class="p-ml-3" value="FACETS" v-model="dataset.solrType" />
                    <label for="FACETS">FACETS</label>
                </div>
                <div class="p-field p-col-12 p-float-label" v-if="dataset.solrType == 'DOCUMENTS'">
                    <InputText
                        id="solrFieldList"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.dataset.solrFieldList.$model"
                        :class="{
                            'p-invalid': v$.dataset.solrFieldList.$invalid && v$.dataset.solrFieldList.$dirty
                        }"
                        @blur="v$.dataset.solrFieldList.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="solrFieldList" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrFieldList') }} * </label>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.solrFieldList" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.solrFieldList') }" />
                </div>
                <div id="facet-container" class="p-col-12 p-fluid p-formgrid p-grid" v-if="dataset.solrType == 'FACETS'">
                    <div class="p-col-11 p-field" :style="restDescriptor.style.infoColumnsContainer">
                        <span class="p-float-label" :style="restDescriptor.style.maxWidth">
                            <InputText id="solrFacetQuery" class="kn-material-input" :style="restDescriptor.style.maxWidth" v-model.trim="dataset.solrFacetQuery" @change="$emit('touched')" />
                            <label for="solrFacetQuery" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrFacetQuery') }} </label>
                        </span>
                    </div>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain p-col-1" @click="facetQueryHelpVisible = true" />
                    <div class="p-field p-col-6 p-float-label">
                        <InputText id="solrFacetField" class="kn-material-input" type="text" v-model.trim="dataset.solrFacetField" @change="$emit('touched')" />
                        <label for="solrFacetField" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrFacetField') }} </label>
                    </div>
                    <div class="p-field p-col-6 p-float-label">
                        <InputText id="solrFacetPrefix" class="kn-material-input" type="text" v-model.trim="dataset.solrFacetPrefix" @change="$emit('touched')" />
                        <label for="solrFacetPrefix" class="kn-material-input-label"> {{ $t('managers.datasetManagement.solrFacetPrefix') }} </label>
                    </div>
                </div>
            </form>
        </template>
    </Card>

    <RequestHeadersTable :selectedDataset="selectedDataset" />
    <QueryParamTable :selectedDataset="selectedDataset" />
    <FacetInfoDialog :visible="facetQueryHelpVisible" @close="facetQueryHelpVisible = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import solrDescriptor from './DatasetManagementSolrDatasetDescriptor.json'
import restDescriptor from '../restDataset/DatasetManagementRestDatasetDescriptor.json'
import FacetInfoDialog from '../infoDialogs/DatasetManagementFacetInfoDialog.vue'
import RequestHeadersTable from '../tables/DatasetManagementRequestHeadersTable.vue'
import QueryParamTable from '../tables/DatasetManagementQueryParamTable.vue'
import Card from 'primevue/card'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    components: { Card, KnValidationMessages, RadioButton, FacetInfoDialog, RequestHeadersTable, QueryParamTable },
    props: {
        parentValid: { type: Boolean },
        selectedDataset: { type: Object as any },
        dataSources: { type: Array as any },
        businessModels: { type: Array as any }
    },
    emits: ['touched'],
    data() {
        return {
            restDescriptor,
            solrDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            facetQueryHelpVisible: false
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
        const solrFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Solr' || value
        }
        const documentFieldsRequired = (value) => {
            return this.dataset.solrType != 'DOCUMENTS' || value
        }
        const customValidators: ICustomValidatorMap = { 'solr-fields-required': solrFieldsRequired, 'document-fields-required': documentFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', solrDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {}
})
</script>
