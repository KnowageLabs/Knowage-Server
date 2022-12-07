<template>
    <div v-if="dataset.dsTypeCd == 'Federated'">
        <label>{{ $t('managers.datasetManagement.selectDatasetType') }}: </label> <b>{{ dataset.dsTypeCd }}</b>
    </div>
    <div id="is-not-federated p-d-flex" v-else>
        <Card class="p-m-2 p-d-flex">
            <template #content>
                <div id="dropdownContainer">
                    <span class="p-float-label p-d-flex">
                        <Dropdown
                            id="scope"
                            class="kn-material-input"
                            :style="typeTabDescriptor.style.maxWidth"
                            :options="datasetTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.dsTypeCd.$model"
                            :class="{
                                'p-invalid': v$.dataset.dsTypeCd.$invalid && v$.dataset.dsTypeCd.$dirty
                            }"
                            @before-show="v$.dataset.dsTypeCd.$touch()"
                            @change="handleTypeChange"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.selectDatasetType') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.dsTypeCd"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.selectDatasetType')
                        }"
                    />
                    <span class="p-float-label p-d-flex">
                        <Button :label="$t('workspace.myModels.openInQBE')" class="kn-button kn-button--primary p-d-flex" @click="openDatasetInQbe" v-if="isOpenInQBEVisible(selectedDataset)" :disabled="touched" />
                    </span>
                </div>
            </template>
        </Card>
    </div>
    <FileDataset v-if="dataset.dsTypeCd == 'File'" :selectedDataset="selectedDataset" @fileUploaded="$emit('fileUploaded')" />
    <QueryDataset v-if="dataset.dsTypeCd == 'Query'" :selectedDataset="selectedDataset" :dataSources="dataSources" :scriptTypes="scriptTypes" :activeTab="activeTab" @queryEdited="$emit('queryEdited')" />
    <JavaDataset v-if="dataset.dsTypeCd == 'Java Class'" :selectedDataset="selectedDataset" />
    <ScriptDataset v-if="dataset.dsTypeCd == 'Script'" :selectedDataset="selectedDataset" :scriptTypes="scriptTypes" :activeTab="activeTab" />
    <QbeDataset v-if="dataset.dsTypeCd == 'Qbe' || dataset.dsTypeCd == 'Federated'" :selectedDataset="selectedDataset" :businessModels="businessModels" :dataSources="dataSources" :parentValid="parentValid" />
    <FlatDataset v-if="dataset.dsTypeCd == 'Flat'" :selectedDataset="selectedDataset" :dataSources="dataSources" />
    <CkanDataset v-if="dataset.dsTypeCd == 'Ckan'" :selectedDataset="selectedDataset" />
    <RestDataset v-if="dataset.dsTypeCd == 'REST'" :selectedDataset="selectedDataset" />
    <SparqlDataset v-if="dataset.dsTypeCd == 'SPARQL'" :selectedDataset="selectedDataset" />
    <SolrDataset v-if="dataset.dsTypeCd == 'Solr'" :selectedDataset="selectedDataset" />
    <PythonDataset v-if="dataset.dsTypeCd == 'Python/R'" :selectedDataset="selectedDataset" :pythonEnvironments="pythonEnvironments" :rEnvironments="rEnvironments" />
    <PreparedDataset v-if="dataset.dsTypeCd == 'Prepared'" :selectedDataset="selectedDataset" :pythonEnvironments="pythonEnvironments" :rEnvironments="rEnvironments" />
    <ParamTable v-if="dataset.dsTypeCd && dataset.dsTypeCd != 'File' && dataset.dsTypeCd != 'Flat' && dataset.dsTypeCd != 'Prepared'" :selectedDataset="selectedDataset" />
    <QBE v-if="qbeVisible" :sourceDataset="selectedDataset" @close="closeQbe" />
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
import PreparedDataset from './preparedDataset/DatasetManagementPreparedDataset.vue'
import QBE from '@/modules/qbe/QBE.vue'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, ParamTable, CkanDataset, QbeDataset, RestDataset, JavaDataset, FlatDataset, SolrDataset, QueryDataset, ScriptDataset, SparqlDataset, PythonDataset, FileDataset, PreparedDataset, QBE },
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
    computed: {},
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
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
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
        },
        isOpenInQBEVisible(dataset: any) {
            return dataset.pars?.length == 0 && (dataset.isPersisted || dataset.dsTypeCd == 'File' || dataset.dsTypeCd == 'Query' || dataset.dsTypeCd == 'Flat')
        },
        openDatasetInQbe() {
            this.qbeVisible = true
        },
        closeQbe() {
            this.qbeVisible = false
        }
    }
})
</script>

<style lang="scss">
.p-card .p-card-content {
    .dropdownContainer {
        .p-card-body {
            display: flex;
            flex-direction: row;
            width: 100%;
        }
    }
}
</style>
