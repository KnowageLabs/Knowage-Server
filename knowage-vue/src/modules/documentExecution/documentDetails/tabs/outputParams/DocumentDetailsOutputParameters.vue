<template>
    <div class="p-grid p-m-0" :style="mainDescriptor.style.flexOne">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0" :style="mainDescriptor.style.flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.outputParams.title') }}
                </template>
                <template #right>
                    <Button :label="$t('common.add')" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" @click="addParam" />
                </template>
            </Toolbar>
            <div id="drivers-list-container" :style="mainDescriptor.style.flexOneRelative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <KnListBox :style="mainDescriptor.style.height100" :options="document.outputParameters" :settings="outputParamDescriptor.knListSettings" @click="selectParam($event.item)" @delete.stop="deleteParamConfirm($event)"></KnListBox>
                </div>
            </div>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0" :style="mainDescriptor.style.driverDetailsContainer">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.outputParams.paramDetails') }}
                </template>
                <template #right>
                    <Button :label="$t('common.save')" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" :disabled="!selectedParam.name" @click="saveParam" />
                </template>
            </Toolbar>
            <div id="driver-details-container" class="p-m-2" :style="mainDescriptor.style.flexOneRelative">
                <div class="kn-details-info-div" v-if="Object.keys(selectedParam).length === 0">
                    {{ $t('documentExecution.documentDetails.outputParams.noParamSelected') }}
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
                                    />
                                    <label for="title" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.outputParams.paramName') }} *</label>
                                </span>
                                <KnValidationMessages class="p-mt-1" :vComp="v$.selectedParam.name" :additionalTranslateParams="{ fieldName: $t('documentExecution.documentDetails.outputParams.paramName') }" />
                            </div>
                            <div class="p-field p-col-12">
                                <span class="p-float-label">
                                    <Dropdown id="type" class="kn-material-input" v-model="selectedParam.type" :options="typeList" optionLabel="valueCd" :disabled="selectedParam.isUserDefined === null" />
                                    <label for="type" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.outputParams.paramType') }} </label>
                                </span>
                            </div>
                            <div v-if="selectedParam.type.valueCd === 'DATE'" class="p-field p-col-12">
                                <span class="p-float-label">
                                    <Dropdown id="dateFormat" class="kn-material-input" v-model="selectedParam.formatCode" :options="dateFormats" optionLabel="translatedValueName" optionValue="valueCd" :disabled="selectedParam.isUserDefined === null" />
                                    <label for="dateFormat" class="kn-material-input-label"> {{ $t('managers.datasetManagement.ckanDateFormat') }} </label>
                                </span>
                            </div>
                            <div v-if="selectedParam.type.valueCd === 'DATE' && selectedParam.formatCode === 'CUSTOM'" class="p-field p-col-12">
                                <span class="p-float-label">
                                    <InputText id="title" class="kn-material-input" v-model="selectedParam.formatValue" :disabled="selectedParam.isUserDefined === null" />
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
import { iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { createValidations } from '@/helpers/commons/validationHelper'
import { defineComponent, PropType } from 'vue'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from '@/modules/documentExecution/documentDetails/tabs/drivers/DocumentDetailsDriversDescriptor.json'
import outputParamDescriptor from './DocumentDetailsOutputParametersDescriptor.json'
import useValidate from '@vuelidate/core'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'document-drivers',
    components: { KnListBox, Dropdown, KnValidationMessages },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, typeList: { type: Array as any, required: true }, dateFormats: { type: Array as any, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            outputParamDescriptor,
            v$: useValidate() as any,
            selectedParam: {} as any,
            document: {} as any
        }
    },
    created() {
        this.document = this.selectedDocument
    },
    validations() {
        const validationObject = { selectedParam: createValidations('selectedParam', outputParamDescriptor.validations.selectedParam) }
        return validationObject
    },
    methods: {
        addParam() {
            this.selectedParam = { biObjectId: this.document.id, formatCode: '', formatValue: '', isUserDefined: true, type: this.typeList[0] ? this.typeList[0] : {} }
            this.document.outputParameters.push(this.selectedParam)
        },
        selectParam(event) {
            this.selectedParam = event
        },
        async saveParam() {
            await this.saveRequest()
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.save'), msg: this.$t('common.toast.updateSuccess') })
                })
                .catch((error) => {
                    console.log(error)
                })
        },
        saveRequest() {
            if (!this.selectedParam.id) {
                return this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters`, this.selectedParam, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
            } else {
                return this.$http.put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters/${this.selectedParam.id}`, this.selectedParam, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
            }
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
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/outputparameters/${paramToDelete.id}`, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    let deletedParam = this.document.outputParameters.findIndex((param) => param.id === paramToDelete.id)
                    this.document.outputParameters.splice(deletedParam, 1)
                    this.$store.commit('setInfo', { title: this.$t('common.toast.deleteTitle'), msg: this.$t('common.toast.deleteSuccess') })
                })
                .catch((error) => {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: error.message })
                })
        }
    }
})
</script>
<style lang="scss">
.kn-details-info-div {
    margin: 8px !important;
    border: 1px solid rgba(204, 204, 204, 0.6);
    padding: 8px;
    background-color: #e6e6e6;
    text-align: center;
    position: relative;
    text-transform: uppercase;
    font-size: 0.8rem;
}
</style>
