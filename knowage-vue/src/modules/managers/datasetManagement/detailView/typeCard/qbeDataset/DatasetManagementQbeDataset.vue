<template>
    <Card class="p-mt-3">
        <template #content>
            <form v-if="dataset.dsTypeCd == 'Qbe'" class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="qbeDataSource"
                            class="kn-material-input"
                            :options="dataSources"
                            optionLabel="label"
                            optionValue="label"
                            v-model="v$.dataset.qbeDataSource.$model"
                            :class="{
                                'p-invalid': v$.dataset.qbeDataSource.$invalid && v$.dataset.qbeDataSource.$dirty
                            }"
                            @before-show="v$.dataset.qbeDataSource.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.glossary.glossaryUsage.dataSource') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.qbeDataSource"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.glossary.glossaryUsage.dataSource')
                        }"
                    />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="qbeDatamarts"
                            class="kn-material-input"
                            :options="businessModels"
                            optionLabel="name"
                            optionValue="name"
                            v-model="v$.dataset.qbeDatamarts.$model"
                            :class="{
                                'p-invalid': v$.dataset.qbeDatamarts.$invalid && v$.dataset.qbeDatamarts.$dirty
                            }"
                            @before-show="v$.dataset.qbeDatamarts.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.qbeDatamarts') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.qbeDatamarts"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.qbeDatamarts')
                        }"
                    />
                </div>
            </form>
            <div v-if="dataset.dsTypeCd == 'Qbe' || dataset.dsTypeCd == 'Federated'">
                <Button :label="$t('managers.datasetManagement.viewQbeButton')" class="p-col-2 p-mr-2 p-button kn-button--primary" style="max-height:38px" @click="viewQbe" />
                <Button :label="$t('managers.datasetManagement.openQbeButton')" class="p-col-2 p-button kn-button--primary" :disabled="parentValid" @click="showQbeDataset" />
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import qbeDescriptor from './DatasetManagementQbeDatasetDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages },
    props: { parentValid: { type: Boolean }, selectedDataset: { type: Object as any }, dataSources: { type: Array as any }, businessModels: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            qbeDescriptor,
            dataset: {} as any,
            v$: useValidate() as any
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
        const qbeFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Qbe' || value
        }
        const customValidators: ICustomValidatorMap = { 'qbe-fields-required': qbeFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', qbeDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        viewQbe() {
            if (this.dataset.qbeJSONQuery) {
                if (typeof this.dataset.qbeJSONQuery === 'string') {
                    this.dataset.qbeJSONQuery = JSON.stringify(JSON.parse(this.dataset.qbeJSONQuery), null, 2)
                } else {
                    this.dataset.qbeJSONQuery = JSON.stringify(this.dataset.qbeJSONQuery, null, 2)
                }

                this.$store.commit('setInfo', { title: 'TODO', msg: 'This functionality is to be created in the following sprints...' })
            } else {
                this.$store.commit('setInfo', { title: this.$t('managers.datasetManagement.viewQbeWarning'), msg: this.$t('managers.datasetManagement.viewQbeMsg') })
            }
        },
        showQbeDataset() {
            this.$store.commit('setInfo', { title: 'TODO', msg: 'This functionality is to be created in the following sprints...' })
        }
    }
})
</script>
