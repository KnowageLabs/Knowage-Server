<template>
    <form ref="bcForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
        <div class="p-field p-col-12 p-md-6">
            <span class="p-float-label">
                <InputText
                    id="name"
                    class="kn-material-input"
                    v-model.trim="v$.tmpBnssView.name.$model"
                    :class="{
                        'p-invalid': v$.tmpBnssView.name.$invalid && v$.tmpBnssView.name.$dirty
                    }"
                    @blur="v$.tmpBnssView.name.$touch()"
                    @change="$emit('touched')"
                />
                <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
            </span>
            <KnValidationMessages class="p-mt-1" :vComp="v$.tmpBnssView.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
        </div>
        <div class="p-field p-col-12 p-md-6">
            <span class="p-float-label">
                <InputText id="desc" class="kn-material-input" v-model="tmpBnssView.description" />
                <label for="desc" class="kn-material-input-label"> {{ $t('common.description') }}</label>
            </span>
        </div>
    </form>
    <div class="kn-relative kn-flex">
        <div class="kn-height-full kn-width-full" :style="bsDescriptor.style.absolute">
            <DataTable class="p-datatable-sm kn-table p-ml-2" :value="physicalModels" v-model:selection="tmpBnssView.physicalModels" :scrollable="true" scrollHeight="100%" v-model:filters="filters" :globalFilterFields="bsDescriptor.globalFilterFields">
                <template #empty>
                    {{ $t('metaweb.businessModel.') }}
                </template>
                <template #header>
                    <div class="table-header p-d-flex">
                        <span class="p-input-icon-left p-mr-3 p-col-12">
                            <i class="pi pi-search" />
                            <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                        </span>
                    </div>
                </template>
                <Column selectionMode="multiple" />
                <Column field="name" :header="$t('common.name')" style="flex-basis:100%" />
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import bsDescriptor from '../../MetawebBusinessModelDescriptor.json'

export default defineComponent({
    components: { DataTable, Column, KnValidationMessages },
    props: { physicalModels: Array, showBusinessViewDialog: Boolean, bnssViewObject: Object },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBnssView: {} as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    created() {
        this.tmpBnssView = this.bnssViewObject
    },
    watch: {
        bnssViewObject() {
            this.tmpBnssView = this.bnssViewObject
        }
    },
    validations() {
        const bvRequired = (value) => {
            return !this.showBusinessViewDialog || value
        }
        const customValidators: ICustomValidatorMap = {
            'bv-dialog-required': bvRequired
        }
        const validationObject = {
            tmpBnssView: createValidations('tmpBnssView', bsDescriptor.validations.tmpBnssView, customValidators)
        }
        return validationObject
    }
})
</script>
