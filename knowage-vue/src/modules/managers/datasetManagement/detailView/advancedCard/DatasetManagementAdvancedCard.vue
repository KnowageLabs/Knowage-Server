<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <InputSwitch v-model="isTransformable" @change="setTransformationType" class="p-mr-2" />
            <span>{{ $t('managers.datasetManagement.pivotTransformer') }}</span>
        </template>
    </Toolbar>
    <Card v-if="isTransformable">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-3">
                    <span class="p-float-label">
                        <InputText
                            id="pivotColName"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.pivotColName.$model"
                            :class="{
                                'p-invalid': v$.dataset.pivotColName.$invalid && v$.dataset.pivotColName.$dirty
                            }"
                            @blur="v$.dataset.pivotColName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="pivotColName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.pivotColName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.pivotColName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.pivotColName') }" />
                </div>
                <div class="p-field p-col-3">
                    <span class="p-float-label">
                        <InputText
                            id="pivotColValue"
                            class="kn-material-input"
                            type="text"
                            v-model="dataset.pivotColValue"
                            :class="{
                                'p-invalid': v$.dataset.pivotColValue.$invalid && v$.dataset.pivotColValue.$dirty
                            }"
                            @blur="v$.dataset.pivotColValue.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="pivotColValue" class="kn-material-input-label"> {{ $t('managers.datasetManagement.pivotColValue') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.pivotColValue" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.pivotColValue') }" />
                </div>
                <div class="p-field p-col-3">
                    <span class="p-float-label">
                        <InputText
                            id="pivotRowName"
                            class="kn-material-input"
                            type="text"
                            v-model="dataset.pivotRowName"
                            :class="{
                                'p-invalid': v$.dataset.pivotRowName.$invalid && v$.dataset.pivotRowName.$dirty
                            }"
                            @blur="v$.dataset.pivotRowName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="pivotRowName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.pivotRowName') }} *</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.pivotRowName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.pivotRowName') }" />
                </div>

                <span class="p-field-checkbox p-col-3">
                    <label for="pivotIsNumRows">{{ $t('managers.datasetManagement.pivotIsNumRows') }}</label>
                    <Checkbox id="pivotIsNumRows" class="p-ml-2" v-model="dataset.pivotIsNumRows" :binary="true" @change="$emit('touched')" />
                </span>
            </form>
        </template>
    </Card>

    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #start>
            <InputSwitch v-model="dataset.isPersistedHDFS" class="p-mr-2" @change="$emit('touched')" />
            <span>{{ $t('managers.datasetManagement.isPersistedHDFS') }}</span>
        </template>
    </Toolbar>

    <div v-if="dataset.dsTypeCd != 'Flat'">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
            <template #start>
                <InputSwitch v-model="dataset.isPersisted" :disabled="disablePersist" class="p-mr-2" @change="$emit('touched')" />
                <span v-tooltip.top="{ value: $t('managers.datasetManagement.peristenceWarning'), disabled: !disablePersist }">{{ $t('managers.datasetManagement.isPersisted') }}</span>
            </template>
        </Toolbar>
        <Card v-if="dataset.isPersisted">
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-3">
                        <span class="p-float-label">
                            <InputText
                                id="persistTableName"
                                class="kn-material-input"
                                type="text"
                                v-model="dataset.persistTableName"
                                :class="{
                                    'p-invalid': v$.dataset.persistTableName.$invalid && v$.dataset.persistTableName.$dirty
                                }"
                                @blur="v$.dataset.persistTableName.$touch()"
                                @change="$emit('touched')"
                            />
                            <label for="persistTableName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.persistTableName') }} *</label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.persistTableName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.persistTableName') }" />
                    </div>
                </form>
                <Toolbar class="kn-toolbar kn-toolbar--default p-mt-3" v-if="dataset.isPersisted">
                    <template #start>
                        <InputSwitch v-model="dataset.isScheduled" class="p-mr-2" @change="$emit('touched')" />
                        <span>{{ $t('managers.datasetManagement.isScheduled') }}</span>
                    </template>
                </Toolbar>
                <DatasetScheduler v-if="isSchedulerVisible" :selectedDataset="dataset" :schedulingData="schedulingData" />
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import advancedCardDescriptor from './DatasetManagementAdvancedCardDescriptor.json'
import DatasetScheduler from './DatasetManagementScheduler.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import InputSwitch from 'primevue/inputswitch'
import { mapState } from 'vuex'

export default defineComponent({
    components: { Card, InputSwitch, Checkbox, KnValidationMessages, DatasetScheduler },
    props: {
        selectedDataset: { type: Object as any },
        transformationDataset: { type: Object as any },
        schedulingData: { type: Object as any }
    },
    computed: {
        ...mapState({
            user: 'user'
        }),
        disablePersist() {
            if (this.dataset['pars'] && this.dataset['pars'].length > 0) {
                return true
            }
            return false
        },
        isSchedulerVisible(): Boolean {
            return this.user.functionalities.includes('SchedulingDatasetManagement') && this.dataset.isPersisted && this.dataset.isScheduled
        }
    },
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            advancedCardDescriptor,
            dataset: {} as any,
            testInput: 'testinput',
            testCheckbox: true,
            isTransformable: false
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.isDatasetTransformable()
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.isDatasetTransformable()
        }
    },
    validations() {
        const transformationFieldsRequired = (value) => {
            return !this.isTransformable || value
        }
        const persistFieldsRequired = (value) => {
            return !this.dataset.isPersisted || value
        }
        const customValidators: ICustomValidatorMap = {
            'transformable-field-required': transformationFieldsRequired,
            'persist-field-required': persistFieldsRequired
        }
        const validationObject = {
            dataset: createValidations('dataset', advancedCardDescriptor.validations.advancedTab, customValidators)
        }
        return validationObject
    },
    methods: {
        isDatasetTransformable() {
            if (this.dataset.trasfTypeCd && this.dataset.trasfTypeCd == this.transformationDataset.VALUE_CD) {
                this.isTransformable = true
            } else {
                this.isTransformable = false
            }
        },
        setTransformationType() {
            if (this.isTransformable) {
                this.dataset.trasfTypeCd = this.transformationDataset.VALUE_CD
            } else {
                this.dataset.trasfTypeCd ? (this.dataset.trasfTypeCd = '') : null
                this.dataset.pivotColName ? (this.dataset.pivotColName = '') : null
                this.dataset.pivotColValue ? (this.dataset.pivotColValue = '') : null
                this.dataset.pivotIsNumRows ? (this.dataset.pivotIsNumRows = '') : null
                this.dataset.pivotRowName ? (this.dataset.pivotRowName = '') : null
            }
        }
    }
})
</script>
