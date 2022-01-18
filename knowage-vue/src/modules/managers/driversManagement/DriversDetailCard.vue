<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText
                            id="label"
                            class="kn-material-input"
                            type="text"
                            v-model="v$.driver.label.$model"
                            :class="{
                                'p-invalid': v$.driver.label.$invalid && v$.driver.label.$dirty
                            }"
                            @blur="v$.driver.label.$touch()"
                            @change="setDirty"
                        />
                        <label for="label" class="kn-material-input-label">{{ $t('common.label') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.driver.label" :additionalTranslateParams="{ fieldName: $t('common.label') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            class="kn-material-input"
                            type="text"
                            v-model="v$.driver.name.$model"
                            :class="{
                                'p-invalid': v$.driver.name.$invalid && v$.driver.name.$dirty
                            }"
                            @blur="v$.driver.name.$touch()"
                            @change="setDirty"
                        />
                        <label for="name" class="kn-material-input-label">{{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.driver.name" :additionalTranslateParams="{ fieldName: $t('common.name') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown
                            id="type"
                            class="kn-material-input"
                            v-model="v$.driver.type.$model"
                            :options="types"
                            optionValue="VALUE_CD"
                            optionLabel="VALUE_NM"
                            :class="{
                                'p-invalid': v$.driver.type.$invalid && v$.driver.type.$dirty
                            }"
                            @blur="v$.driver.type.$touch()"
                            @change="setDirty"
                        />
                        <label for="type" class="kn-material-input-label"> {{ $t('common.type') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.driver.type" :additionalTranslateParams="{ fieldName: $t('common.type') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-8">
                    <span class="p-float-label">
                        <InputText id="description" class="kn-material-input" type="text" v-model="driver.description" @change="setDirty" />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <MultiSelect class="kn-material-input" v-model="selectedOptions" :options="driversManagemenDetailtDescriptor.options" optionLabel="name" optionValue="label" @change="changeType" />
                        <label for="description" class="kn-material-input-label">{{ $t('managers.driversManagement.options') }} </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import MultiSelect from 'primevue/multiselect'
import driversManagemenDetailtDescriptor from './DriversManagementDetailDescriptor.json'
import driversManagemenValidationtDescriptor from './DriversManagementValidationDescriptor.json'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'detail-card',
    components: { Dropdown, MultiSelect, KnValidationMessages },
    props: {
        selectedDriver: {
            type: Object,
            required: false
        },
        types: {
            type: Array,
            required: false
        }
    },
    data() {
        return {
            driver: {} as any,
            driversManagemenDetailtDescriptor,
            driversManagemenValidationtDescriptor,
            selectedOptions: [] as String[],
            v$: useValidate() as any
        }
    },
    validations() {
        const validationObject = {
            driver: createValidations('driver', driversManagemenValidationtDescriptor.validations.driver)
        }
        return validationObject
    },
    watch: {
        selectedDriver() {
            this.driver = this.selectedDriver as any
            this.handleTypes()
        }
    },
    mounted() {
        if (this.driver) {
            this.driver = this.selectedDriver as any
            this.handleTypes()
        }
    },
    methods: {
        handleTypes() {
            this.selectedOptions = []
            if (this.driver.functional) {
                this.selectedOptions.push('functional')
            }
            if (this.driver.temporal) {
                this.selectedOptions.push('temporal')
            }
        },
        changeType() {
            this.selectedOptions.includes('temporal') ? (this.driver.temporal = true) : (this.driver.temporal = false)
            this.selectedOptions.includes('functional') ? (this.driver.functional = true) : (this.driver.functional = false)
            this.setDirty()
        },
        setDirty() {
            this.$emit('touched')
        }
    }
})
</script>
