<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="label" v-model="v$.dataset.label.$model" class="kn-material-input" type="text" max-length="50" :class="{ 'p-invalid': v$.dataset.label.$invalid && v$.dataset.label.$dirty }" data-test="label-input" @blur="v$.dataset.label.$touch()" @change="$emit('touched')" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.dataset.label" :additional-translate-params="{ fieldName: $t('common.label') }" />
                </div>
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="name" v-model="v$.dataset.name.$model" class="kn-material-input" type="text" max-length="50" :class="{ 'p-invalid': v$.dataset.name.$invalid && v$.dataset.name.$dirty }" data-test="name-input" @blur="v$.dataset.name.$touch()" @change="$emit('touched')" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.dataset.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-mt-1 p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            v-model="v$.dataset.description.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="150"
                            :class="{ 'p-invalid': v$.dataset.description.$invalid && v$.dataset.description.$dirty }"
                            data-test="description-input"
                            @blur="v$.dataset.description.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.dataset.description" :additional-translate-params="{ fieldName: $t('common.description') }" />
                </div>
                <div v-if="qbeAdvancedSaving" class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="scope"
                            v-model="v$.dataset.scopeCd.$model"
                            class="kn-material-input"
                            :options="scopeTypes"
                            option-label="VALUE_CD"
                            option-value="VALUE_CD"
                            :class="{
                                'p-invalid': v$.dataset.scopeCd.$invalid && v$.dataset.scopeCd.$dirty
                            }"
                            data-test="scope-input"
                            @before-show="v$.dataset.scopeCd.$touch()"
                            @change="updateIdFromCd(scopeTypes, 'scopeId', $event.value), $emit('touched')"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.scope') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.dataset.scopeCd"
                        :additional-translate-params="{
                            fieldName: $t('managers.datasetManagement.scope')
                        }"
                    />
                </div>
                <div v-if="qbeAdvancedSaving" class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="category"
                            v-model="v$.dataset.catTypeVn.$model"
                            class="kn-material-input"
                            :options="categoryTypes"
                            option-label="VALUE_CD"
                            option-value="VALUE_CD"
                            :class="{
                                'p-invalid': v$.dataset.catTypeVn.$invalid && v$.dataset.catTypeVn.$dirty
                            }"
                            data-test="category-input"
                            @before-show="v$.dataset.catTypeVn.$touch()"
                            @change="updateIdFromCd(categoryTypes, 'catTypeId', $event.value), $emit('touched')"
                        />
                        <label v-if="dataset.scopeCd == 'USER'" for="category" class="kn-material-input-label"> {{ $t('common.category') }} </label>
                        <label v-else for="category" class="kn-material-input-label"> {{ $t('common.category') }} * </label>
                    </span>
                    <KnValidationMessages
                        :v-comp="v$.dataset.catTypeVn"
                        :additional-translate-params="{
                            fieldName: $t('managers.datasetManagement.scope')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import descriptor from './QBESavingDialogDescriptor.json'
import mainStore from '../../../../App.store'
import UserFunctionalitiesConstants from '@/UserFunctionalitiesConstants.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Card, Dropdown, KnValidationMessages },
    props: { propDataset: Object, scopeTypes: Array, categoryTypes: Array },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            v$: useValidate() as any,
            dataset: {} as any,
            descriptor
        }
    },
    computed: {
        qbeAdvancedSaving(): any {
            return (this.store.$state as any).user.functionalities.includes(UserFunctionalitiesConstants.QBE_ADVANCED_SAVING)
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
        const catTypeRequired = (value) => {
            return this.dataset.scopeCd == 'USER' || value
        }
        const customValidators: ICustomValidatorMap = { 'cat-type-required': catTypeRequired }
        const validationObject = { dataset: createValidations('dataset', descriptor.validations.dataset, customValidators) }
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
