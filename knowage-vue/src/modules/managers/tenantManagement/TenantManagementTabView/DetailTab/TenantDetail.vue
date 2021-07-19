<template>
    <Card :style="tabViewDescriptor.card.style">
        <template #content>
            <form class="p-fluid p-m-5">
                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <InputText
                            id="MULTITENANT_NAME"
                            class="kn-material-input"
                            type="text"
                            :disabled="disableField"
                            v-model.trim="v$.tenant.MULTITENANT_NAME.$model"
                            :class="{
                                'p-invalid': v$.tenant.MULTITENANT_NAME.$invalid && v$.tenant.MULTITENANT_NAME.$dirty
                            }"
                            maxLength="20"
                            @blur="v$.tenant.MULTITENANT_NAME.$touch()"
                            @input="onFieldChange('MULTITENANT_NAME', $event.target.value)"
                            data-test="name-input"
                        />
                        <label for="MULTITENANT_NAME" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.tenant.MULTITENANT_NAME"
                        :additionalTranslateParams="{
                            fieldName: $t('common.name')
                        }"
                    />
                </div>

                <div class="p-field" :style="tabViewDescriptor.pField.style">
                    <span class="p-float-label">
                        <Dropdown
                            id="MULTITENANT_THEME"
                            class="kn-material-input"
                            :options="themes"
                            optionLabel="VALUE_CHECK"
                            optionValue="VALUE_CHECK"
                            v-model="v$.tenant.MULTITENANT_THEME.$model"
                            :class="{
                                'p-invalid': v$.tenant.MULTITENANT_THEME.$invalid && v$.tenant.MULTITENANT_THEME.$dirty
                            }"
                            @before-show="v$.tenant.MULTITENANT_THEME.$touch()"
                            @change="onFieldChange('MULTITENANT_THEME', $event.value)"
                        >
                        </Dropdown>
                        <label for="MULTITENANT_THEME" class="kn-material-input-label"> {{ $t('managers.tenantManagement.detail.theme') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.tenant.MULTITENANT_THEME"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.tenantManagement.detail.theme')
                        }"
                    />
                </div>
            </form>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../TenantManagementTabViewDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import tenantDetailValidationDescriptor from './TenantDetailValidationDescriptor.json'
import { iMultitenant } from '../../TenantManagement'

export default defineComponent({
    name: 'detail-tab',
    components: {
        Card,
        Dropdown,
        KnValidationMessages
    },
    props: {
        selectedTenant: {
            type: Object,
            requried: false
        },
        listOfThemes: [] as any
    },
    computed: {
        disableField() {
            if (this.tenant.MULTITENANT_ID) return true
            return false
        }
    },
    emits: ['fieldChanged', 'roleTypeChanged'],
    data() {
        return {
            tabViewDescriptor,
            tenantDetailValidationDescriptor,
            v$: useValidate() as any,
            tenant: {} as iMultitenant,
            themes: [] as any
        }
    },
    validations() {
        return {
            tenant: createValidations('tenant', tenantDetailValidationDescriptor.validations.tenant)
        }
    },
    async created() {
        if (this.selectedTenant && Object.keys(this.selectedTenant).length > 0) {
            this.tenant = { ...this.selectedTenant } as any
        } else {
            this.tenant = {} as iMultitenant
            this.tenant.MULTITENANT_THEME = 'sbi_default'
        }
        this.themes = [...this.listOfThemes] as any
    },
    watch: {
        selectedTenant() {
            this.v$.$reset()
            this.tenant = { ...this.selectedTenant } as any
        },
        listOfThemes() {
            this.themes = [...this.listOfThemes] as any
        }
    },
    methods: {
        onFieldChange(fieldName: string, value: any) {
            this.$emit('fieldChanged', { fieldName, value })
        }
    }
})
</script>
