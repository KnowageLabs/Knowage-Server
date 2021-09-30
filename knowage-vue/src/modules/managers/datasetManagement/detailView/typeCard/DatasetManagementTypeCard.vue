<template>
    <Card>
        <template #content>
            <div v-if="(dataset.dsTypeCd = 'Federated')">
                <label>{{ $t('managers.datasetManagement.selectDatasetType') }}: </label> <b>Federated</b>
            </div>
            <div id="dropdownContainer" v-else>
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
                        @change="$emit('touched')"
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
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import typeTabDescriptor from './DatasetManagementTypeCardDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages },
    props: {
        selectedDataset: { type: Object as any },
        datasetTypes: { type: Array as any }
    },
    computed: {},
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            typeTabDescriptor,
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
        return {
            dataset: createValidations('dataset', typeTabDescriptor.validations.dataset)
        }
    },
    methods: {}
})
</script>
