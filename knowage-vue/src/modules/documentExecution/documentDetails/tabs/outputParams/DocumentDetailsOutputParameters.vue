<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.outputParams.title') }}
                </template>
                <template #end>
                    <Button :label="$t('common.add')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="addParam" />
                </template>
            </Toolbar>
            <div id="drivers-list-container" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <KnListBox class="kn-height-full" :options="document.outputParameters" :settings="outputParamDescriptor.knListSettings" @click="selectParam($event.item)" @delete.stop="deleteParamConfirm($event)"></KnListBox>
                </div>
            </div>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :style="mainDescriptor.style.driverDetailsContainer">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.outputParams.paramDetails') }}
                </template>
            </Toolbar>
            <div id="driver-details-container" class="p-m-2 kn-flex kn-relative">
                <div v-if="Object.keys(selectedParam).length === 0">
                    <InlineMessage severity="info" class="kn-width-full"> {{ $t('documentExecution.documentDetails.outputParams.noParamSelected') }}</InlineMessage>
                </div>
                <Card v-else>
                    <template #content>
                        <form class="p-fluid p-formgrid p-grid p-m-2">
                            <div class="p-field p-col-12 p-mt-2">
                                <span class="p-float-label">
                                    <InputText
                                        id="title"
                                        class="kn-material-input"
                                        :disabled="selectedParam.isUserDefined === null"
                                        v-model.trim="v$.selectedParam.name.$model"
                                        :class="{
                                            'p-invalid': v$.selectedParam.name.$invalid && v$.selectedParam.name.$dirty
                                        }"
                                        @blur="v$.selectedParam.name.$touch()"
                                        @input="markSelectedParamForChange"
                                    />
                                    <label for="title" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.outputParams.paramName') }} *</label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedParam.name" :additionalTranslateParams="{ fieldName: $t('documentExecution.documentDetails.outputParams.paramName') }" />
                            </div>
                            <div class="p-field p-col-12">
                                <span class="p-float-label">
                                    <Dropdown id="type" class="kn-material-input" v-model="selectedParam.type" :options="typeList" optionLabel="valueCd" :disabled="selectedParam.isUserDefined === null" @change="markSelectedParamForChange" />
                                    <label for="type" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.outputParams.paramType') }} </label>
                                </span>
                            </div>
                            <div v-if="selectedParam.type.valueCd === 'DATE'" class="p-field p-col-12">
                                <span class="p-float-label">
                                    <Dropdown id="dateFormat" class="kn-material-input" v-model="selectedParam.formatCode" :options="dateFormats" optionLabel="translatedValueName" optionValue="valueCd" :disabled="selectedParam.isUserDefined === null" @change="markSelectedParamForChange" />
                                    <label for="dateFormat" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                                </span>
                            </div>
                            <div v-if="selectedParam.type.valueCd === 'DATE' && selectedParam.formatCode === 'CUSTOM'" class="p-field p-col-12">
                                <span class="p-float-label">
                                    <InputText id="title" class="kn-material-input" v-model="selectedParam.formatValue" :disabled="selectedParam.isUserDefined === null" @input="markSelectedParamForChange" />
                                    <label for="title" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.outputParams.customValue') }} *</label>
                                </span>
                            </div>
                        </form>
                    </template>
                </Card>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { iDocument, iParType, iDateFormat, iOutputParam } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { defineComponent, PropType } from 'vue'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from '@/modules/documentExecution/documentDetails/tabs/drivers/DocumentDetailsDriversDescriptor.json'
import outputParamDescriptor from './DocumentDetailsOutputParametersDescriptor.json'
import useValidate from '@vuelidate/core'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dropdown from 'primevue/dropdown'
import InlineMessage from 'primevue/inlinemessage'

export default defineComponent({
    name: 'document-drivers',
    components: { KnListBox, Dropdown, KnValidationMessages, InlineMessage },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, typeList: { type: Array as PropType<iParType[]>, required: true }, dateFormats: { type: Array as PropType<iDateFormat[]>, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            v$: useValidate() as any,
            mainDescriptor,
            driversDescriptor,
            outputParamDescriptor,
            selectedParam: {} as iOutputParam,
            document: {} as any
        }
    },
    created() {
        this.document = this.selectedDocument
    },
    validations() {
        const outputParamsValidator = (value) => {
            return Object.keys(this.selectedParam).length === 0 || value
        }
        const customValidators: ICustomValidatorMap = { 'output-params-validator': outputParamsValidator }
        const validationObject = { selectedParam: createValidations('selectedParam', outputParamDescriptor.validations.selectedParam, customValidators) }
        return validationObject
    },
    methods: {
        addParam() {
            this.selectedParam = { numberOfErrors: 1, biObjectId: this.document.id, formatCode: null, formatValue: null, isUserDefined: true, type: this.typeList[0] ? this.typeList[0] : ({} as iParType), tempId: this.document.outputParameters.length + 1 } as iOutputParam
            this.document.outputParameters.push(this.selectedParam)
        },
        selectParam(event) {
            this.selectedParam = event
        },
        markSelectedParamForChange() {
            this.selectedParam.isChanged = true
            this.selectedParam.numberOfErrors = this.v$.$errors.length
        },

        deleteParamConfirm(event) {
            this.$confirm.require({
                header: this.$t('common.toast.deleteConfirmTitle'),
                message: this.$t('common.toast.deleteMessage'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteParam(event.item)
            })
        },
        async deleteParam(paramToDelete) {
            if (paramToDelete.id) {
                await this.$http
                    .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters/${paramToDelete.id}`, { headers: { 'X-Disable-Errors': 'true' } })
                    .then(() => {
                        let deletedParam = this.document.outputParameters.findIndex((param) => param.id === paramToDelete.id)
                        this.document.outputParameters.splice(deletedParam, 1)
                        this.store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                        this.selectedParam = {} as iOutputParam
                    })
                    .catch((error) => {
                        this.store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message })
                    })
            } else {
                let deletedParam = this.document.outputParameters.findIndex((param) => param.tempId === paramToDelete.tempId)
                this.document.outputParameters.splice(deletedParam, 1)
                this.selectedParam = {} as iOutputParam
            }
        }
    }
})
</script>
