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
        <!-- #region Java Class Type ---------------------------------------------------------------------------------------------------------->
        <Card v-if="dataset.dsTypeCd == 'Java Class'" class="p-mt-3">
            <template #content>
                <div class="p-field">
                    <span class="p-float-label">
                        <InputText
                            id="jClassName"
                            class="kn-material-input"
                            type="text"
                            :style="typeTabDescriptor.style.maxWidth"
                            v-model.trim="v$.dataset.jClassName.$model"
                            :class="{
                                'p-invalid': v$.dataset.jClassName.$invalid && v$.dataset.jClassName.$dirty
                            }"
                            @blur="v$.dataset.jClassName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="jClassName" class="kn-material-input-label"> {{ $t('managers.lovsManagement.javaClassName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.jClassName" :additionalTranslateParams="{ fieldName: $t('managers.lovsManagement.javaClassName') }" />
                </div>
            </template>
        </Card>
        <!-- #endregion -->

        <!-- #region Flat Type ---------------------------------------------------------------------------------------------------------->
        <Card v-if="dataset.dsTypeCd == 'Flat'" class="p-mt-3">
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
                            <label for="scope" class="kn-material-input-label"> {{ $t('managers.managers.dataSource') }} * </label>
                        </span>
                        <KnValidationMessages
                            :vComp="v$.dataset.dataSourceFlat"
                            :additionalTranslateParams="{
                                fieldName: $t('managers.managers.dataSource')
                            }"
                        />
                    </div>
                </form>
            </template>
        </Card>
        <!-- #endregion -->

        <CkanDataset v-if="dataset.dsTypeCd == 'Ckan'" :selectedDataset="selectedDataset" />
        <QbeDataset v-if="dataset.dsTypeCd == 'Qbe'" :selectedDataset="selectedDataset" :businessModels="businessModels" :dataSources="dataSources" :parentValid="parentValid" />
        <RestDataset v-if="dataset.dsTypeCd == 'REST'" :selectedDataset="selectedDataset" />
        <ParamTable v-if="dataset.dsTypeCd && dataset.dsTypeCd != 'File' && dataset.dsTypeCd != 'Flat'" :selectedDataset="selectedDataset" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import ParamTable from './tables/DatasetManagementParamTable.vue'
import CkanDataset from './ckanDataset/DatasetManagementCkanDataset.vue'
import QbeDataset from './qbeDataset/DatasetManagementQbeDataset.vue'
import RestDataset from './restDataset/DatasetManagementRestDataset.vue'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import typeTabDescriptor from './DatasetManagementTypeCardDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, ParamTable, CkanDataset, QbeDataset, RestDataset },
    props: {
        parentValid: { type: Boolean },
        selectedDataset: { type: Object as any },
        datasetTypes: { type: Array as any },
        dataSources: { type: Array as any },
        businessModels: { type: Array as any }
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
        const javaClassFieldRequired = (value) => {
            return this.dataset.dsTypeCd != 'Java Class' || value
        }
        const flatFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Flat' || value
        }
        const customValidators: ICustomValidatorMap = {
            'java-class-field-required': javaClassFieldRequired,
            'flat-fields-required': flatFieldsRequired
        }
        const validationObject = {
            dataset: createValidations('dataset', typeTabDescriptor.validations.dataset, customValidators)
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
