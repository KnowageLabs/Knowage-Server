<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <InputSwitch v-model="dataset.isPersistedHDFS" class="p-mr-2" @change="$emit('touched')" />
            <span>{{ $t('managers.datasetManagement.isPersistedHDFS') }}</span>
        </template>
    </Toolbar>

    <div v-if="dataset.dsTypeCd != 'Flat'">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
            <template #start>
                <InputSwitch v-model="dataset.isPersisted" :disabled="disablePersist" class="p-mr-2" @change="$emit('touched')" />
                <span>{{ $t('managers.datasetManagement.isPersisted') }}</span>
            </template>
        </Toolbar>
        <Card v-if="dataset.isPersisted">
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-6">
                        <span class="p-float-label">
                            <InputText
                                id="persistTableName"
                                v-model="dataset.persistTableName"
                                class="kn-material-input"
                                type="text"
                                :class="{
                                    'p-invalid': v$.dataset.persistTableName.$invalid && v$.dataset.persistTableName.$dirty
                                }"
                                @blur="v$.dataset.persistTableName.$touch()"
                                @change="$emit('touched')"
                            />
                            <label for="persistTableName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.persistTableName') }} *</label>
                        </span>
                        <KnValidationMessages class="p-mt-1" :v-comp="v$.dataset.persistTableName" :additional-translate-params="{ fieldName: $t('managers.datasetManagement.persistTableName') }" />
                    </div>
                </form>
                <Toolbar v-if="dataset.isPersisted" class="kn-toolbar kn-toolbar--default p-mt-3">
                    <template #start>
                        <InputSwitch v-model="dataset.isScheduled" class="p-mr-2" @change="$emit('touched')" />
                        <span>{{ $t('managers.datasetManagement.isScheduled') }}</span>
                    </template>
                </Toolbar>
                <DatasetScheduler v-if="dataset.isPersisted && dataset.isScheduled" :selected-dataset="dataset" :scheduling-data="schedulingData" />
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import InputSwitch from 'primevue/inputswitch'
import DatasetScheduler from '@/modules/managers/datasetManagement/detailView/advancedCard/DatasetManagementScheduler.vue'
import Card from 'primevue/card'
import descriptor from './QBESavingDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Card, KnValidationMessages, InputSwitch, DatasetScheduler },
    props: { propDataset: Object, schedulingData: Object },
    data() {
        return {
            v$: useValidate() as any,
            dataset: {} as any,
            descriptor
        }
    },
    computed: {
        disablePersist() {
            if (this.dataset['pars'] && this.dataset['pars'].length > 0) {
                return true
            }
            return false
        }
    },
    watch: {
        selectedDataset() {
            this.dataset = this.propDataset
        }
    },
    created() {
        this.dataset = this.propDataset
    },
    validations() {
        const persistFieldsRequired = (value) => {
            return !this.dataset.isPersisted || value
        }
        const customValidators: ICustomValidatorMap = {
            'persist-field-required': persistFieldsRequired
        }
        const validationObject = {
            dataset: createValidations('dataset', descriptor.validations.advancedTab, customValidators)
        }
        return validationObject
    },
    methods: {
        updateIdFromCd(optionsArray, fieldToUpdate, updatedField) {
            const selectedField = optionsArray.find((option) => option.VALUE_CD === updatedField)
            selectedField ? (this.dataset[fieldToUpdate] = selectedField.VALUE_ID) : ''
        }
    }
})
</script>
<style lang="scss">
#qbe-saving-dialog .p-dialog-content {
    padding: 0;
}
</style>
