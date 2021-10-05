<template>
    Is invalid: {{ v$.$invalid }}
    <p></p>
    DsType: {{ dataset.dsTypeCd }}
    <div v-if="dataset.dsTypeCd == 'Federated'">
        <label>{{ $t('managers.datasetManagement.selectDatasetType') }}: </label> <b>Federated</b>
    </div>
    <div id="is-not-federated" v-else>
        <Card>
            <template #content>
                <div id="dropdownContainer">
                    <span class="p-float-label">
                        <Dropdown
                            id="scope"
                            class="kn-material-input"
                            style="width:100%"
                            :options="datasetTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.dsTypeCd.$model"
                            :class="{
                                'p-invalid': v$.dataset.dsTypeCd.$invalid && v$.dataset.dsTypeCd.$dirty
                            }"
                            @before-show="v$.dataset.dsTypeCd.$touch()"
                            @click="changeTypeWarning"
                            @change=";((this.dataset.pars = []), (this.dataset.restJsonPathAttributes = []), (this.dataset.restRequestHeaders = [])), $emit('touched')"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.selectDatasetType') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.dsTypeCd"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.selectDatasetType')
                        }"
                    />
                </div>
            </template>
        </Card>
    </div>
    <QueryDataset v-if="dataset.dsTypeCd == 'Query'" :selectedDataset="selectedDataset" :dataSources="dataSources" :scriptTypes="scriptTypes" />
    <ScriptDataset v-if="dataset.dsTypeCd == 'Script'" :selectedDataset="selectedDataset" :scriptTypes="scriptTypes" />
    <JavaDataset v-if="dataset.dsTypeCd == 'Java Class'" :selectedDataset="selectedDataset" />
    <QbeDataset v-if="dataset.dsTypeCd == 'Qbe' || dataset.dsTypeCd == 'Federated'" :selectedDataset="selectedDataset" :businessModels="businessModels" :dataSources="dataSources" :parentValid="parentValid" />
    <FlatDataset v-if="dataset.dsTypeCd == 'Flat'" :selectedDataset="selectedDataset" :dataSources="dataSources" />
    <CkanDataset v-if="dataset.dsTypeCd == 'Ckan'" :selectedDataset="selectedDataset" />
    <RestDataset v-if="dataset.dsTypeCd == 'REST'" :selectedDataset="selectedDataset" />
    <SolrDataset v-if="dataset.dsTypeCd == 'Solr'" :selectedDataset="selectedDataset" />
    <SparqlDataset v-if="dataset.dsTypeCd == 'SPARQL'" :selectedDataset="selectedDataset" />
    <ParamTable v-if="dataset.dsTypeCd && dataset.dsTypeCd != 'File' && dataset.dsTypeCd != 'Flat'" :selectedDataset="selectedDataset" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import ParamTable from './tables/DatasetManagementParamTable.vue'
import QueryDataset from './queryDataset/DatasetManagementQueryDataset.vue'
import ScriptDataset from './scriptDataset/DatasetManagementScriptDataset.vue'
import FlatDataset from './flatDataset/DatasetManagementFlatDataset.vue'
import JavaDataset from './javaDataset/DatasetManagementJavaDataset.vue'
import CkanDataset from './ckanDataset/DatasetManagementCkanDataset.vue'
import QbeDataset from './qbeDataset/DatasetManagementQbeDataset.vue'
import SolrDataset from './solrDataset/DatasetManagementSolrDataset.vue'
import SparqlDataset from './sparqlDataset/DatasetManagementSparqlDataset.vue'
import RestDataset from './restDataset/DatasetManagementRestDataset.vue'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import typeTabDescriptor from './DatasetManagementTypeCardDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, ParamTable, CkanDataset, QbeDataset, RestDataset, JavaDataset, FlatDataset, SolrDataset, QueryDataset, ScriptDataset, SparqlDataset },
    props: {
        parentValid: { type: Boolean },
        selectedDataset: { type: Object as any },
        datasetTypes: { type: Array as any },
        dataSources: { type: Array as any },
        businessModels: { type: Array as any },
        scriptTypes: { type: Array as any }
    },
    computed: {},
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            typeTabDescriptor,
            dataset: {} as any,
            expandParamsCard: true
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
        changeTypeWarning() {
            this.$store.commit('setInfo', { title: this.$t('documentExecution.registry.warning'), msg: this.$t('managers.datasetManagement.changeTypeMsg') })
        },
        clearAllTables() {}
    }
})
</script>
