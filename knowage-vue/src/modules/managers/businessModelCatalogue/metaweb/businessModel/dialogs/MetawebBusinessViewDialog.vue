<template>
    <Dialog class="bsdialog" :style="bsDescriptor.style.bsDialog" :visible="showBusinessViewDialog" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #left>
                    {{ $t('metaweb.businessModel.newView') }}
                </template>
            </Toolbar>
        </template>
        <div v-show="wizardStep == 1" id="step-1">
            <form ref="bcForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2">
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
        <div v-show="wizardStep == 2" id="step-2">
            <form ref="bvForm" class="p-fluid p-formgrid p-grid p-mt-4 p-mx-2">
                <div class="p-field p-col-12 p-md-6">
                    <span class="p-float-label ">
                        <Dropdown id="source" class="kn-material-input" v-model="sourceTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                        <label for="source" class="kn-material-input-label"> {{ $t('metaweb.businessModel.sourceTable') }}</label>
                    </span>
                </div>
                <div class="p-field p-col-12 p-md-6">
                    <span class="p-float-label ">
                        <Dropdown id="target" class="kn-material-input" v-model="targetTable" :options="tmpBnssView.physicalModels" optionLabel="name" />
                        <label for="target" class="kn-material-input-label"> {{ $t('metaweb.businessModel.targetTable') }}</label>
                    </span>
                </div>
            </form>
            <div id="attr-container" class="p-grid p-m-2">
                <div class="kn-remove-card-padding p-col">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('metaweb.businessModel.sourceAttr') }}
                        </template>
                    </Toolbar>
                    <Listbox class="kn-list data-condition-list" :options="sourceTable.columns">
                        <template #empty>{{ $t('metaweb.businessModel.sourceHint') }} </template>
                        <template #option="slotProps">
                            <div class="kn-list-item">
                                <div class="kn-list-item-text">
                                    <span class="kn-truncated">
                                        {{ slotProps.option.name }}
                                    </span>
                                </div>
                            </div>
                        </template>
                    </Listbox>
                </div>
                <div class="kn-remove-card-padding p-col">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #left>
                            {{ $t('metaweb.businessModel.targetAttr') }}
                        </template>
                    </Toolbar>
                    <Listbox class="kn-list data-condition-list" :options="targetTable.columns">
                        <template #empty>{{ $t('metaweb.businessModel.targetHint') }} </template>
                        <template #option="slotProps">
                            <div class="kn-list-item">
                                <div class="kn-list-item-text">
                                    <span class="kn-truncated">
                                        {{ slotProps.option.name }}
                                    </span>
                                </div>
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
            <div id="summary-container" class="p-m-3">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('metaweb.businessModel.summary') }}
                    </template>
                    <template #right>
                        <Button v-if="!expandSummary" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = true" />
                        <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandSummary = false" />
                    </template>
                </Toolbar>
                <Listbox v-show="expandSummary" class="kn-list data-condition-list" :options="summary">
                    <template #empty>{{ $t('metaweb.businessModel.summaryHint') }} </template>
                    <template #option="slotProps">
                        <div class="kn-list-item">
                            <div class="kn-list-item-text">
                                <span class="kn-truncated">
                                    {{ slotProps.option.name }}
                                </span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" />
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
        <template #footer>
            <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="onCancel" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--secondary" :label="$t('common.back')" :disabled="buttonDisabled" @click="previousStep" />
            <Button v-if="wizardStep == 1" class="kn-button kn-button--primary" :label="$t('common.next')" :disabled="buttonDisabled" @click="nextStep" />
            <Button v-if="wizardStep == 2" class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import bsDescriptor from '../MetawebBusinessModelDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'document-drivers',
    components: { Dialog, DataTable, Column, KnValidationMessages, Dropdown, Listbox },
    emits: ['closeDialog'],
    props: { physicalModels: Array, showBusinessViewDialog: Boolean },
    computed: {
        buttonDisabled(): boolean {
            if (this.v$.$invalid || this.tmpBnssView.physicalModels.length < 2) {
                return true
            } else return false
        }
    },
    data() {
        return {
            bsDescriptor,
            v$: useValidate() as any,
            tmpBnssView: { physicalModels: [], name: '', description: '' } as any,
            wizardStep: 1,
            filters: {
                global: [filterDefault]
            } as Object,
            sourceTable: { columns: [] } as any,
            targetTable: { columns: [] } as any,
            summary: [] as any,
            expandSummary: true
        }
    },
    created() {},
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
    },
    methods: {
        resetPhModel() {
            this.tmpBnssView.physicalModels = []
        },
        onCancel() {
            this.$emit('closeDialog')
            this.tmpBnssView = { physicalModels: [], name: '', description: '' } as any
        },
        nextStep() {
            this.wizardStep++
        },
        previousStep() {
            this.wizardStep--
        }
    }
})
</script>
<style lang="scss">
.bsdialog.p-dialog .p-dialog-header,
.bsdialog.p-dialog .p-dialog-content {
    padding: 0;
    // margin: 0;
}
.data-condition-list {
    border: 1px solid $color-borders !important;
    border-top: none;
}
</style>
