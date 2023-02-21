<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-7 p-m-0 p-p-0 right-border p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.info.infoTitle') }}
                </template>
                <template #end>
                    <Button v-if="designerButtonVisible" :label="$t('documentExecution.olap.openDesigner')" class="p-button-text p-button-plain" @click="openDesignerConfirm" />
                </template>
            </Toolbar>
            <div id="informations-content" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <Card class="p-m-2">
                        <template #content>
                            <div v-if="templates.length == 0">
                                <div class="p-field p-col-12 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="fileName" v-model="templateToUpload.name" class="kn-material-input kn-width-full" :disabled="true" />
                                            <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
                                        </span>
                                    </div>
                                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                                    <KnInputFile v-if="!uploading" label="" :change-function="setTemplateForUpload" :trigger-input="triggerUpload" />
                                </div>
                            </div>
                            <form class="p-fluid p-formgrid p-grid p-m-1">
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText
                                            id="label"
                                            v-model="v$.document.label.$model"
                                            class="kn-material-input"
                                            type="text"
                                            max-length="100"
                                            :class="{
                                                'p-invalid': v$.document.label.$invalid && v$.document.label.$dirty
                                            }"
                                            @blur="v$.document.label.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }} * </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.label" :additional-translate-params="{ fieldName: $t('common.label') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText
                                            id="name"
                                            v-model="v$.document.name.$model"
                                            class="kn-material-input"
                                            type="text"
                                            max-length="200"
                                            :class="{
                                                'p-invalid': v$.document.name.$invalid && v$.document.name.$dirty
                                            }"
                                            @blur="v$.document.name.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <img v-if="selectedDocument?.previewFile && !imagePreview" id="image-preview" :src="getImageUrl" :height="mainDescriptor.style.previewImage" />
                                    <img v-if="imagePreviewUrl && imagePreview" id="image-preview" :src="imagePreviewUrl" :height="mainDescriptor.style.previewImage" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Textarea
                                            id="description"
                                            v-model="v$.document.description.$model"
                                            class="kn-material-input"
                                            rows="9"
                                            max-length="400"
                                            :auto-resize="true"
                                            :class="{
                                                'p-invalid': v$.document.description.$invalid && v$.document.description.$dirty
                                            }"
                                            @blur="v$.document.description.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }} </label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.description" :additional-translate-params="{ fieldName: $t('common.description') }" />
                                </div>

                                <div class="p-field p-col-12 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="fileName" v-model="document.previewFile" class="kn-material-input" :disabled="true" />
                                            <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.previewImage') }} </label>
                                        </span>
                                    </div>
                                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setImageUploadType" />
                                    <Button v-if="document.previewFile" icon="fas fa-trash fa-1x" class="p-button-text p-button-plain p-ml-2" @click="$emit('deleteImage')" />
                                    <KnInputFile :change-function="setImageForUpload" accept=".png, .jpg, .jpeg" :trigger-input="triggerImageUpload" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="type"
                                            v-model="v$.document.typeCode.$model"
                                            class="kn-material-input"
                                            :options="documentTypes"
                                            option-label="translatedValueName"
                                            option-value="valueCd"
                                            :class="{
                                                'p-invalid': v$.document.typeCode.$invalid && v$.document.typeCode.$dirty
                                            }"
                                            @blur="v$.document.typeCode.$touch()"
                                            @change="onTypeChange"
                                        />
                                        <label for="type" class="kn-material-input-label"> {{ $t('importExport.catalogFunction.column.type') }} *</label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.typeCode" :additional-translate-params="{ fieldName: $t('importExport.catalogFunction.column.type') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="engine"
                                            v-model="v$.document.engine.$model"
                                            class="kn-material-input"
                                            :options="filteredEngines"
                                            option-label="name"
                                            option-value="label"
                                            :disabled="!document.typeCode || document.typeCode == ''"
                                            :class="{
                                                'p-invalid': v$.document.engine.$invalid && v$.document.engine.$dirty
                                            }"
                                            @blur="v$.document.engine.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="engine" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.engine') }} *</label>
                                    </span>
                                    <small>{{ $t('documentExecution.documentDetails.info.engineHint') }}</small>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.engine" :additional-translate-params="{ fieldName: $t('documentExecution.documentDetails.info.engine') }" />
                                </div>

                                <span v-if="isDataSourceVisible" class="p-field p-float-label p-col-12 p-lg-6" :class="{ 'p-lg-12': !isDataSetVisible }">
                                    <Dropdown id="datasource" v-model="document.dataSourceLabel" class="kn-material-input" :options="availableDatasources" option-label="label" option-value="label" />
                                    <label for="datasource" class="kn-material-input-label"> {{ $t('managers.businessModelManager.dataSource') }} </label>
                                </span>

                                <div v-if="isDataSetVisible" class="p-field p-col-12 p-lg-6 p-d-flex">
                                    <div class="kn-flex">
                                        <span class="p-float-label">
                                            <InputText id="dataset" v-model="dataset.name" class="kn-material-input" :disabled="true" />
                                            <label for="dataset" class="kn-material-input-label"> {{ $t('common.dataset') }} </label>
                                        </span>
                                    </div>
                                    <Button v-if="isDataSetVisible" icon="fas fa-search fa-1x" class="p-button-text p-button-plain p-ml-2" @click="showDatasetDialog = true" />
                                </div>

                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="state"
                                            v-model="v$.document.stateCode.$model"
                                            class="kn-material-input"
                                            :options="availableStates"
                                            option-label="translatedValueName"
                                            option-value="valueCd"
                                            :class="{
                                                'p-invalid': v$.document.stateCode.$invalid && v$.document.stateCode.$dirty
                                            }"
                                            @blur="v$.document.stateCode.$touch()"
                                            @change="$emit('touched')"
                                        />
                                        <label for="state" class="kn-material-input-label"> {{ $t('common.state') }} *</label>
                                    </span>
                                    <KnValidationMessages class="p-mt-1" :v-comp="v$.document.stateCode" :additional-translate-params="{ fieldName: $t('common.state') }" />
                                </div>
                                <div class="p-field p-col-12 p-lg-6">
                                    <span class="p-float-label">
                                        <InputText id="refresh" v-model="document.refreshSeconds" class="kn-material-input" type="number" />
                                        <label for="refresh" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.refresh') }} </label>
                                    </span>
                                    <small>{{ $t('documentExecution.documentDetails.info.refreshHint') }}</small>
                                </div>
                                <span class="p-field p-col-12 p-lg-6 p-jc-center p-mt-3">
                                    <InputSwitch id="visible" v-model="document.visible" />
                                    <i class="far fa-eye p-ml-2" />
                                    <label for="visible" class="kn-material-input-label p-ml-2"> {{ $t('common.visible') }} </label>
                                </span>
                                <span class="p-field p-col-12 p-lg-6 p-mt-3">
                                    <InputSwitch id="locked" v-model="lockedByUser" @change="setIsLockedByUser" />
                                    <i class="fas fa-lock p-ml-2" />
                                    <label for="locked" class="kn-material-input-label p-ml-2"> {{ $t('common.locked') }} </label>
                                </span>
                            </form>
                        </template>
                    </Card>
                </div>
            </div>
        </div>
        <div class="p-col-5 p-m-0 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('documentExecution.documentDetails.info.positionTitle') }}
                </template>
            </Toolbar>
            <div id="position-content" class="kn-flex kn-relative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <div v-if="document.drivers && document.drivers.length > 0" id="driver-position-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.info.parametersPanelPosition') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <span class="p-field p-float-label p-col-12">
                                    <Dropdown id="attributes" v-model="document.parametersRegion" class="kn-material-input kn-width-full" :options="driversPositions" :option-label="translatedLabel" option-value="value">
                                        <template #option="slotProps">
                                            <div class="p-dropdown-option">
                                                <span class="kn-capitalize">{{ $t(slotProps.option.label) }}</span>
                                            </div>
                                        </template>
                                    </Dropdown>
                                    <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.positionTitle') }} </label>
                                </span>
                            </template>
                        </Card>
                    </div>
                    <div id="restriction-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.info.restrictionsTitle') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <form class="p-formgrid p-grid p-mb-3">
                                    <span class="p-float-label p-col-10">
                                        <Textarea id="profiledVisibility" v-model="document.profiledVisibility" class="kn-material-input kn-width-full" rows="1" :auto-resize="true" :disabled="true" />
                                        <label for="profiledVisibility" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.profiledVisibility') }} </label>
                                    </span>
                                    <Button icon="fas fa-plus-circle fa-1x" class="p-button-text p-button-plain p-ml-2 p-col-1" :disabled="!visibilityAttribute" @click="addRestriction" />
                                    <Button icon="fas fa-eraser fa-1x" class="p-button-text p-button-plain p-ml-2 p-col-1" @click="clearAllRestrictions" />
                                </form>
                                <form class="p-formgrid p-grid">
                                    <span class="p-field p-float-label p-col-12 p-lg-5">
                                        <Dropdown id="attributes" v-model="visibilityAttribute" class="kn-material-input kn-width-full" :options="availableAttributes" option-label="attributeName" option-value="attributeName" />
                                        <label for="attributes" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.attribute') }} </label>
                                    </span>
                                    <span class="p-col-12 p-lg-1" :style="infoDescriptor.style.center">=</span>
                                    <span class="p-field p-float-label p-col-12 p-lg-6">
                                        <InputText id="restrictionValue" v-model="restrictionValue" class="kn-material-input kn-width-full" />
                                        <label for="restrictionValue" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.restrictionValueHint') }} </label>
                                    </span>
                                </form>
                            </template>
                        </Card>
                    </div>
                    <div id="tree-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #start>
                                {{ $t('documentExecution.documentDetails.info.visibilityLocationTitle') }}
                            </template>
                        </Toolbar>
                        <Card class="card-0-padding">
                            <template #content>
                                <DocumentDetailsTree :prop-functionalities="folders" :prop-selected-folders="document.functionalities" @selected="setFunctionality" />
                            </template>
                        </Card>
                    </div>
                </div>
            </div>
        </div>
        <DatasetDialog v-if="showDatasetDialog" :selected-dataset="selectedDataset" :visible="showDatasetDialog" @closeDialog="showDatasetDialog = false" @saveSelectedDataset="saveSelectedDataset" />
    </div>
</template>

<script lang="ts">
import { iDocument, iDataSource, iEngine, iTemplate, iAttribute, iFolder } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { defineComponent, PropType } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import { mapState } from 'pinia'
import { startOlap } from '../../dialogs/olapDesignerDialog/DocumentDetailOlapHelpers'
import mainDescriptor from '../../DocumentDetailsDescriptor.json'
import infoDescriptor from './DocumentDetailsInformationsDescriptor.json'
import useValidate from '@vuelidate/core'
import DatasetDialog from './DocumentDetailsDatasetDialog.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import DocumentDetailsTree from './DocumentDetailsTree.vue'
import mainStore from '../../../../../App.store'

import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'document-details-informations',
    components: { DatasetDialog, Card, Textarea, Dropdown, InputSwitch, KnValidationMessages, KnInputFile, DocumentDetailsTree },
    props: {
        selectedDocument: { type: Object as PropType<iDocument> },
        selectedDataset: { type: Object },
        availableStates: { type: Array },
        documentTypes: { type: Array as any, required: true },
        documentEngines: { type: Array as PropType<iEngine[]>, required: true },
        availableDatasources: { type: Array as PropType<iDataSource[]> },
        availableFolders: { type: Array as PropType<iFolder[]> },
        availableTemplates: { type: Array as PropType<iTemplate[]> },
        availableAttributes: { type: Array as PropType<iAttribute[]> }
    },
    emits: ['setTemplateForUpload', 'setImageForUpload', 'deleteImage', 'touched', 'openDesignerDialog'],
    computed: {
        filteredEngines(): any {
            if (this.document.typeCode) {
                return this.documentEngines.filter((engine) => engine.biobjTypeId === this.documentTypes.filter((type) => type.valueCd === this.document.typeCode)[0].valueId)
            }
            return []
        },
        isDataSourceVisible(): boolean {
            switch (this.document.engine) {
                case 'knowageofficeengine':
                case 'knowagecompositedoce':
                case 'knowageprocessengine':
                case 'knowagechartengine':
                case 'knowagenetworkengine':
                case 'knowagecockpitengine':
                case 'knowagedossierengine':
                case 'knowagekpiengine':
                case 'knowagesvgviewerengine':
                    return false
                default:
                    return true
            }
        },
        isDataSetVisible(): boolean {
            switch (this.document.engine) {
                case 'knowagegisengine':
                case 'knowagechartengine':
                case 'knowagenetworkengine':
                    return true
                default:
                    return false
            }
        },
        getImageUrl(): string {
            return import.meta.env.VITE_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument?.previewFile}`
        },
        designerButtonVisible(): boolean {
            return this.document.typeCode == 'OLAP' || this.document.typeCode == 'KPI' || this.document.engine == 'knowagegisengine'
        },
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            v$: useValidate() as any,
            mainDescriptor,
            infoDescriptor,
            uploading: false,
            lockedByUser: false,
            triggerUpload: false,
            showDatasetDialog: false,
            triggerImageUpload: false,
            dataset: {} as any,
            folders: [] as iFolder[],
            document: {} as iDocument,
            templates: [] as iTemplate[],
            templateToUpload: { name: '' } as any,
            imageToUpload: { name: '' } as any,
            visibilityAttribute: '',
            restrictionValue: '',
            driversPositions: infoDescriptor.driversPositions,
            listOfTemplates: [] as iTemplate[],
            imagePreviewUrl: null as any,
            imagePreview: false
        }
    },
    watch: {
        async selectedDocument() {
            this.setData()
            await this.getAllTemplates()
        }
    },
    async created() {
        this.setData()
        await this.getAllTemplates()
    },
    validations() {
        const validationObject = { document: createValidations('document', infoDescriptor.validations.document) }
        return validationObject
    },
    methods: {
        setData() {
            this.templates = this.availableTemplates as iTemplate[]
            this.document = this.selectedDocument as iDocument
            this.dataset = this.selectedDataset
            this.folders = this.availableFolders as iFolder[]
            this.resetImagePreview()
            this.IsLockedByUser()
        },
        IsLockedByUser() {
            this.lockedByUser = this.document.lockedByUser === 'true' ? true : false
        },
        setIsLockedByUser() {
            this.document.lockedByUser = this.lockedByUser ? 'true' : 'false'
        },
        addRestriction() {
            if (this.document.profiledVisibility) {
                this.document.profiledVisibility = this.document.profiledVisibility + ' AND ' + this.visibilityAttribute + ' = ' + this.restrictionValue
            } else {
                this.document.profiledVisibility = this.visibilityAttribute + ' = ' + this.restrictionValue
            }
        },
        clearAllRestrictions() {
            this.document.profiledVisibility = ''
            this.visibilityAttribute = ''
            this.restrictionValue = ''
        },
        saveSelectedDataset(event) {
            this.document.dataSetId = event.id
            this.dataset = event
        },
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        setTemplateForUpload(event) {
            this.uploading = true
            this.templateToUpload = event.target.files[0]
            this.$emit('setTemplateForUpload', event.target.files[0])
            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        setImageUploadType() {
            this.triggerImageUpload = false
            setTimeout(() => (this.triggerImageUpload = true), 200)
        },
        setImageForUpload(event) {
            this.uploading = true
            this.imageToUpload = event.target.files[0]
            this.$emit('setImageForUpload', event.target.files[0])
            this.setImagePreview(event.target.files[0])
            this.triggerImageUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        setImagePreview(imageFile) {
            this.imagePreviewUrl = URL.createObjectURL(imageFile)
            this.imagePreview = true
            this.store.setInfo({ title: this.$t('common.uploadFileSuccess'), msg: this.$t('documentExecution.documentDetails.info.imageInfo') })
        },
        resetImagePreview() {
            this.imagePreviewUrl = null
            this.imagePreview = false
        },
        setFunctionality(event) {
            this.document.functionalities = event
        },
        onTypeChange() {
            this.$emit('touched')
            this.document.engine = ''
        },
        openDesignerConfirm() {
            this.$confirm.require({
                header: this.$t('common.toast.warning'),
                message: this.$t('documentExecution.olap.openDesignerMsg'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    switch (this.document.typeCode) {
                        case 'KPI':
                            this.openKpiDocumentDesigner()
                            break
                        case 'MAP': {
                            this.openGis()
                            break
                        }
                        default:
                            this.openDesigner()
                    }
                }
            })
        },
        async openDesigner() {
            if (this.listOfTemplates.length === 0) {
                this.$emit('openDesignerDialog')
            } else {
                const activeTemplate = this.findActiveTemplate()
                const sbiExecutionId = cryptoRandomString({ length: 16, type: 'base64' })
                await startOlap(this.$http, this.user, sbiExecutionId, this.document, activeTemplate, this.$router)
            }
        },
        findActiveTemplate() {
            let activeTemplate = null as any
            for (let i = 0; i < this.listOfTemplates.length; i++) {
                if (this.listOfTemplates[i].active) {
                    activeTemplate = this.listOfTemplates[i]
                    break
                }
            }
            return activeTemplate
        },
        translatedLabel(a) {
            return this.$t(a.label)
        },
        openKpiDocumentDesigner() {
            this.$router.push(`/kpi-edit/${this.document.id}?from=documentDetail`)
        },
        openGis() {
            this.$router.push(`/gis/edit?documentId=${this.document.id}`)
        },
        async getAllTemplates() {
            if (this.document && this.document.id) this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.document.id}/templates`).then((response: AxiosResponse<any>) => (this.listOfTemplates = response.data as iTemplate[]))
        }
    }
})
</script>
<style lang="scss">
.p-dropdown-label {
    text-transform: capitalize;
}
.card-0-padding .p-card-body,
.card-0-padding .p-card-content {
    padding: 0px;
}
</style>
