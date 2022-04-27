<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="label" class="kn-material-input" type="text" maxLength="50" v-model="v$.dataset.label.$model" :class="{ 'p-invalid': v$.dataset.label.$invalid && v$.dataset.label.$dirty }" @blur="v$.dataset.label.$touch()" @change="$emit('touched')" data-test="label-input" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.label" :additionalTranslateParams="{ fieldName: $t('common.label') }" />
                </div>
                <div class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" maxLength="50" v-model="v$.dataset.name.$model" :class="{ 'p-invalid': v$.dataset.name.$invalid && v$.dataset.name.$dirty }" @blur="v$.dataset.name.$touch()" @change="$emit('touched')" data-test="name-input" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
                </div>
                <div class="p-field p-mt-1 p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            class="kn-material-input"
                            type="text"
                            maxLength="150"
                            v-model="v$.dataset.description.$model"
                            :class="{ 'p-invalid': v$.dataset.description.$invalid && v$.dataset.description.$dirty }"
                            @blur="v$.dataset.description.$touch()"
                            @change="$emit('touched')"
                            data-test="description-input"
                        />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.description" :additionalTranslateParams="{ fieldName: $t('common.description') }" />
                </div>
                <div v-if="qbeAdvancedSaving" class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="scope"
                            class="kn-material-input"
                            :options="scopeTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.scopeCd.$model"
                            :class="{
                                'p-invalid': v$.dataset.scopeCd.$invalid && v$.dataset.scopeCd.$dirty
                            }"
                            @before-show="v$.dataset.scopeCd.$touch()"
                            @change="updateIdFromCd(this.scopeTypes, 'scopeId', $event.value), $emit('touched')"
                            data-test="scope-input"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.scope') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.scopeCd"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.scope')
                        }"
                    />
                </div>
                <div v-if="qbeAdvancedSaving" class="p-field p-mt-1 p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="category"
                            class="kn-material-input"
                            :options="categoryTypes"
                            optionLabel="VALUE_CD"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.catTypeVn.$model"
                            :class="{
                                'p-invalid': v$.dataset.catTypeVn.$invalid && v$.dataset.catTypeVn.$dirty
                            }"
                            @before-show="v$.dataset.catTypeVn.$touch()"
                            @change="updateIdFromCd(this.categoryTypes, 'catTypeId', $event.value), $emit('touched')"
                            data-test="category-input"
                        />
                        <label v-if="this.dataset.scopeCd == 'USER'" for="category" class="kn-material-input-label"> {{ $t('common.category') }} </label>
                        <label v-else for="category" class="kn-material-input-label"> {{ $t('common.category') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.catTypeVn"
                        :additionalTranslateParams="{
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

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { Card, Dropdown, KnValidationMessages },
    props: { propDataset: Object, scopeTypes: Array, categoryTypes: Array },
    computed: {
        qbeAdvancedSaving(): any {
            return (this.$store.state as any).user.functionalities.includes('QbeAdvancedSaving')
        }
    },
    data() {
        return {
            v$: useValidate() as any,
            dataset: {} as any,
            descriptor
        }
    },
    created() {
        this.dataset = this.propDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.propDataset
        }
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
