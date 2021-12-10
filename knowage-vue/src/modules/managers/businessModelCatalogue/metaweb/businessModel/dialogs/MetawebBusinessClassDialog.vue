<template>
    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="showBusinessClassDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.newBusiness') }}
                </template>
            </Toolbar>
        </template>
        <form ref="bcForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2 kn-flex-0">
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText
                        id="name"
                        class="kn-material-input"
                        v-model.trim="v$.tmpBusinessModel.name.$model"
                        :class="{
                            'p-invalid': v$.tmpBusinessModel.name.$invalid && v$.tmpBusinessModel.name.$dirty
                        }"
                        @blur="v$.tmpBusinessModel.name.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.tmpBusinessModel.name" :additionalTranslateParams="{ fieldName: $t('common.name') }" />
            </div>
            <div class="p-field p-col-12 p-md-6">
                <span class="p-float-label">
                    <InputText id="desc" class="kn-material-input" v-model="tmpBusinessModel.description" />
                    <label for="desc" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                </span>
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label ">
                    <Dropdown id="driver" class="kn-material-input" v-model="tmpBusinessModel.physicalModel" :options="physicalModels" optionLabel="name" />
                    <label for="driver" class="kn-material-input-label"> {{ $t('metaweb.businessModel.physTable') }}</label>
                </span>
            </div>
        </form>

        <div :style="bsDescriptor.style.relativeFlex">
            <div class="kn-height-full kn-width-full" :style="bsDescriptor.style.absolute">
                <DataTable
                    v-if="tmpBusinessModel.physicalModel"
                    :value="tmpBusinessModel.physicalModel.columns"
                    class="p-datatable-sm kn-table p-ml-2"
                    v-model:selection="tmpBusinessModel.selectedColumns"
                    :scrollable="true"
                    :scrollHeight="bsDescriptor.style.mainList"
                    dataKey="position"
                    v-model:filters="filters"
                    :globalFilterFields="bsDescriptor.globalFilterFields"
                >
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
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
                    <Column field="name" :header="$t('common.name')" :style="bsDescriptor.style.tableCell" />
                </DataTable>
            </div>
        </div>
        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="onCancel" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import bsDescriptor from '../MetawebBusinessModelDescriptor.json'

export default defineComponent({
    name: 'document-drivers',
    components: { Dialog, Dropdown, DataTable, Column, KnValidationMessages },
    emits: ['closeDialog'],
    props: { physicalModels: Array, showBusinessClassDialog: Boolean },
    computed: {
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.tmpBusinessModel.selectedColumns.length === 0) {
                return true
            } else return false
        }
    },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBusinessModel: { physicalModel: null, selectedColumns: [], name: '', description: '' } as any,
            filters: {
                global: [filterDefault]
            } as Object
        }
    },
    created() {},
    validations() {
        const bmRequired = (value) => {
            return !this.showBusinessClassDialog || value
        }
        const customValidators: ICustomValidatorMap = {
            'bm-dialog-required': bmRequired
        }
        const validationObject = {
            tmpBusinessModel: createValidations('tmpBusinessModel', bsDescriptor.validations.tmpBusinessModel, customValidators)
        }
        return validationObject
    },
    methods: {
        resetPhModel() {
            this.tmpBusinessModel.selectedColumns = []
        },
        onCancel() {
            this.$emit('closeDialog')
            this.tmpBusinessModel = { physicalModel: { columns: [] }, selectedColumns: [], name: '', description: '' } as any
        }
    }
})
</script>
<style lang="scss">
.bsdialog.p-dialog .p-dialog-header,
.bsdialog.p-dialog .p-dialog-content {
    padding: 0;
}
.bsdialog.p-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
