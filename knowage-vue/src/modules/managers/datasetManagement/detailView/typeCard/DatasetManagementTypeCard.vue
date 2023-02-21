<template>
    <div v-if="dataset.dsTypeCd == 'Federated'">
        <label>{{ $t('managers.datasetManagement.selectDatasetType') }}: </label> <b>{{ dataset.dsTypeCd }}</b>
    </div>
    <div v-else id="is-not-federated">
        <Card class="p-m-2">
            <template #content>
                <div id="dropdownContainer">
                    <span class="p-float-label">
                        <Dropdown
                            id="scope"
                            v-model="v$.dataset.dsTypeCd.$model"
                            class="kn-material-input"
                            :style="typeTabDescriptor.style.maxWidth"
                            :options="getAllowed"
                            option-label="VALUE_CD"
                            option-value="VALUE_CD"
                            :class="{
                                'p-invalid': v$.dataset.dsTypeCd.$invalid && v$.dataset.dsTypeCd.$dirty
                            }"
                            :disabled="dataset.dsTypeCd == 'Prepared' || dataset.dsTypeCd == 'Derived'"
                            @before-show="v$.dataset.dsTypeCd.$touch()"
                            @change="handleTypeChange"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.selectDatasetType') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.dataset.dsTypeCd"
                        :additional-translate-params="{
                            fieldName: $t('managers.datasetManagement.selectDatasetType')
                        }"
                    />
                </div>
            </template>
        </Card>
    </div>
    <FileDataset v-if="dataset.dsTypeCd == 'File'" :selected-dataset="selectedDataset" @fileUploaded="$emit('fileUploaded')" />
    <QueryDataset v-else-if="dataset.dsTypeCd == 'Query'" :selected-dataset="selectedDataset" :data-sources="dataSources" :script-types="scriptTypes" :active-tab="activeTab" @queryEdited="$emit('queryEdited')" />
    <JavaDataset v-else-if="dataset.dsTypeCd == 'Java Class'" :selected-dataset="selectedDataset" />
    <ScriptDataset v-else-if="dataset.dsTypeCd == 'Script'" :selected-dataset="selectedDataset" :script-types="scriptTypes" :active-tab="activeTab" />
    <QbeDataset v-else-if="dataset.dsTypeCd == 'Qbe' || dataset.dsTypeCd == 'Federated'" :selected-dataset="selectedDataset" :business-models="businessModels" :data-sources="dataSources" :parent-valid="parentValid" />
    <DerivedDataset v-else-if="dataset.dsTypeCd == 'Derived'" :selected-dataset="selectedDataset" :parent-valid="parentValid" />
    <FlatDataset v-else-if="dataset.dsTypeCd == 'Flat'" :selected-dataset="selectedDataset" :data-sources="dataSources" />
    <CkanDataset v-else-if="dataset.dsTypeCd == 'Ckan'" :selected-dataset="selectedDataset" />
    <RestDataset v-else-if="dataset.dsTypeCd == 'REST'" :selected-dataset="selectedDataset" />
    <SparqlDataset v-else-if="dataset.dsTypeCd == 'SPARQL'" :selected-dataset="selectedDataset" />
    <SolrDataset v-else-if="dataset.dsTypeCd == 'Solr'" :selected-dataset="selectedDataset" />
    <PythonDataset v-else-if="dataset.dsTypeCd == 'Python/R'" :selected-dataset="selectedDataset" :python-environments="pythonEnvironments" :r-environments="rEnvironments" />

    <ParamTable v-if="dataset.dsTypeCd && dataset.dsTypeCd != 'File' && dataset.dsTypeCd != 'Flat' && dataset.dsTypeCd != 'Prepared' && dataset.dsTypeCd != 'Derived'" :selected-dataset="selectedDataset" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import typeTabDescriptor from './DatasetManagementTypeCardDescriptor.json'
import ParamTable from './tables/DatasetManagementParamTable.vue'
import FileDataset from './fileDataset/DatasetManagementFileDataset.vue'
import QueryDataset from './queryDataset/DatasetManagementQueryDataset.vue'
import JavaDataset from './javaDataset/DatasetManagementJavaDataset.vue'
import ScriptDataset from './scriptDataset/DatasetManagementScriptDataset.vue'
import QbeDataset from './qbeDataset/DatasetManagementQbeDataset.vue'
import FlatDataset from './flatDataset/DatasetManagementFlatDataset.vue'
import CkanDataset from './ckanDataset/DatasetManagementCkanDataset.vue'
import RestDataset from './restDataset/DatasetManagementRestDataset.vue'
import SparqlDataset from './sparqlDataset/DatasetManagementSparqlDataset.vue'
import SolrDataset from './solrDataset/DatasetManagementSolrDataset.vue'
import PythonDataset from './pythonDataset/DatasetManagementPythonDataset.vue'
import DerivedDataset from './derivedDataset/DatasetManagementDerivedDataset.vue'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, ParamTable, CkanDataset, QbeDataset, RestDataset, JavaDataset, FlatDataset, SolrDataset, QueryDataset, ScriptDataset, SparqlDataset, PythonDataset, FileDataset, DerivedDataset },
    props: {
        parentValid: { type: Boolean },
        selectedDataset: { type: Object as any },
        datasetTypes: { type: Array as any },
        dataSources: { type: Array as any },
        businessModels: { type: Array as any },
        scriptTypes: { type: Array as any },
        pythonEnvironments: { type: Array as any },
        rEnvironments: { type: Array as any },
        activeTab: { type: Number as any }
    },
    emits: ['touched', 'fileUploaded', 'qbeSaved', 'queryEdited'],
    data() {
        return {
            typeTabDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            expandParamsCard: true,
            touched: false,
            qbeVisible: false
        }
    },
    computed: {
        getAllowed() {
            return this.datasetTypes.filter((cd) => {
                if (this.selectedDataset.dsTypeCd == 'Derived' || this.selectedDataset.dsTypeCd == 'Prepared') {
                    return cd.VALUE_CD == this.selectedDataset.dsTypeCd
                } else {
                    return cd.VALUE_CD != 'Derived' && cd.VALUE_CD != 'Prepared'
                }
            })
        }
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    validations() {
        const validationObject = {
            dataset: createValidations('dataset', typeTabDescriptor.validations.dataset)
        }
        return validationObject
    },
    methods: {
        handleTypeChange() {
            this.touched = true
            this.dataset.pars = []
            this.dataset.restJsonPathAttributes = []
            this.dataset.restRequestHeaders = []
            this.$emit('touched')
        }
    }
})
</script>
