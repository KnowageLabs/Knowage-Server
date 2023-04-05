<template>
    <Dialog class="kn-dialog--toolbar--primary dossier-designer-dialog" :visible="visible" footer="footer" :header="$t(`documentExecution.dossier.designerDialog.step${step}`)" :closable="false" modal>
        <ProgressSpinner v-if="loading" class="kn-progress-spinner" />

        <div v-if="step == 0" class="p-grid p-pt-2 p-mt-2">
            <div class="p-col-6 p-d-flex">
                <span class="p-float-label p-col">
                    <InputText id="fileName" v-model="v$.activeTemplate.fileName.$model" class="kn-material-input kn-width-full" :disabled="true" />
                    <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
                </span>

                <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                <KnInputFile v-if="!uploading" label="Template file" :change-function="startTemplateUpload" :trigger-input="triggerUpload" />
            </div>
            <div class="p-col-6 p-d-flex">
                <span class="p-float-label p-col">
                    <InputText
                        id="label"
                        v-model="v$.activeTemplate.prefix.$model"
                        class="kn-material-input kn-width-full"
                        type="text"
                        max-length="100"
                        :class="{
                            'p-invalid': v$.activeTemplate.prefix.$invalid && v$.activeTemplate.prefix.$dirty
                        }"
                        @blur="v$.activeTemplate.prefix.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="label" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.prefix') }}</label>
                </span>

                <KnValidationMessages class="p-mt-1" :v-comp="v$.activeTemplate.prefix" :additional-translate-params="{ fieldName: $t('dashboard.widgetEditor.prefix') }" />
            </div>
            <!--
            <div class="p-col-6 p-d-flex">
                <span class="p-float-label">
                    <InputSwitch v-model="activeTemplate.uploadable" class="p-mr-2" />
                    <span>{{ $t('documentExecution.dossier.designerDialog.uploadable') }}</span>
                </span>
            </div>
            <div class="p-col-6 p-d-flex">
                <span class="p-float-label">
                    <InputSwitch v-model="activeTemplate.downloadable" class="p-mr-2" />
                    <span>{{ $t('documentExecution.dossier.designerDialog.downloadable') }}</span>
                </span>
            </div>
            -->
        </div>
        <div v-if="step == 1" class="p-grid kn-height-full">
            <div class="p-col-4">
                <Listbox
                    option-label="name"
                    class="kn-list kn-flex kn-height-full"
                    :options="activeTemplate.placeholders"
                    :filter="true"
                    :filter-placeholder="$t('common.search')"
                    filter-match-mode="contains"
                    :filter-fields="['label']"
                    :empty-filter-message="$t('common.info.noDataFound')"
                    @change="selected($event)"
                >
                    <template #option="slotProps">
                        <div class="kn-list-item">
                            <div class="kn-list-item-text">{{ slotProps.option.name }}</div>
                            <i v-if="!slotProps.option.documentLabel" v-tooltip="$t('documentExecution.dossier.designerDialog.noDocumentLinkedToThePlaceholder')" class="fa-solid fa-triangle-exclamation"></i>
                            <i v-else class="fa-regular fa-circle-check"></i>
                        </div>
                    </template>
                </Listbox>
            </div>
            <div v-if="currentSelectedIndex == -1" class="p-col-8">
                <KnHint class="kn-hint-sm" :title="$t('documentExecution.dossier.designerDialog.placeholders')" :hint="$t('documentExecution.dossier.designerDialog.noPlaceholdersHint')" data-test="hint"></KnHint>
            </div>
            <div v-else class="p-col-8">
                <Message class="p-m-2" severity="info" :closable="false">
                    {{ $t('documentExecution.dossier.designerDialog.chooseDocumentInfo') }}
                </Message>

                <div class="p-col p-d-flex">
                    <span class="p-float-label p-col">
                        <InputText id="label" v-model="activeTemplate.placeholders[currentSelectedIndex].documentLabel" class="kn-material-input" type="text" :disabled="true" />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.label') }}</label>
                    </span>
                    <span class="p-float-label p-col">
                        <Button class="kn-button kn-button--primary dossier-designer-toolbar-button p-col-4 kn-width-full" @click="handleDocDialog"> {{ $t('common.choose') }}</Button></span
                    >
                    <span v-if="activeTemplate.placeholders[currentSelectedIndex].documentLabel" class="p-float-label p-col">
                        <Dropdown v-model="activeTemplate.placeholders[currentSelectedIndex].source" class="kn-material-input kn-width-full" :options="getTypes()" option-label="label" option-value="code" />
                        <label for="label" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.linkTo') }}</label>
                    </span>
                    <DocDialog :dialog-visible="dialogVisible" :selected-doc="docId" @close="dialogVisible = false" @apply="hadleDoc"></DocDialog>
                </div>
                <div v-if="activeTemplate.placeholders[currentSelectedIndex].source === 'VIEWS'">
                    <DataTable
                        ref="dt"
                        v-model:selection="selectedView"
                        v-model:filters="filters"
                        :value="views"
                        class="p-datatable-sm kn-table"
                        data-key="id"
                        :paginator="true"
                        :rows="10"
                        paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        :rows-per-page-options="[10, 15, 20]"
                        responsive-layout="stack"
                        breakpoint="960px"
                        :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                        :global-filter-fields="['name', 'type', 'tags']"
                    >
                        <Column v-for="col of viewColumns" :key="col.name" class="kn-truncated" :field="col.name" :header="col.header" :sortable="true"> </Column>
                    </DataTable>
                </div>
                <div v-else-if="activeTemplate.placeholders[currentSelectedIndex].source === 'DRIVERS'">
                    <Card v-for="driver in activeTemplate.placeholders[currentSelectedIndex].parameters" :key="driver.label" class="kn-card p-m-1 p-p-1">
                        <template #title></template>
                        <template #subtitle
                            ><span class="p-text-bold p-text-italic">{{ driver.label }} </span></template
                        >
                        <template #content>
                            <div class="p-grid p-mt-2">
                                <span class="p-float-label p-col-3">
                                    <Dropdown id="driverLinkType" v-model="driver.type" class="kn-material-input kn-width-full" :options="descriptor.driverTypes" option-label="label" option-value="code" />
                                    <label for="driverLinkType" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.driverLinkType') }}</label>
                                </span>
                                <div v-if="driver.type == 'STATIC'" class="p-grid p-col">
                                    <div class="p-field p-col p-mb-0">
                                        <span class="p-float-label">
                                            <InputText id="dossierUrlName" v-model="driver.dossier_url_name" class="kn-material-input" type="text" />
                                            <label for="dossierUrlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.dossierUrlName') }}</label>
                                        </span>
                                        <KnValidationMessages class="p-mt-1" :v-comp="driver.dossier_url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.dossierUrlName') }" />
                                    </div>
                                    <div class="p-field p-col p-mb-0">
                                        <span class="p-float-label">
                                            <InputText id="urlName" v-model="driver.url_name" class="kn-material-input" type="text" />
                                            <label for="urlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.urlName') }}</label>
                                        </span>
                                        <KnValidationMessages class="p-mt-1" :v-comp="driver.url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.urlName') }" />
                                    </div>
                                    <div class="p-field p-col p-mb-0">
                                        <span class="p-float-label">
                                            <InputText id="value" v-model="driver.value" class="kn-material-input" type="text" />
                                            <label for="value" class="kn-material-input-label"> {{ $t('common.value') }}</label>
                                        </span>
                                        <KnValidationMessages class="p-mt-1" :v-comp="driver.value" :additional-translate-params="{ fieldName: $t('common.value') }" />
                                    </div>
                                </div>
                                <div v-else-if="driver.type == 'DYNAMIC'" class="p-grid p-col">
                                    <!--  <Dropdown v-model="driver.dossier_url_name" class="kn-material-input" :options="descriptor.driverTypes" option-label="label" option-value="code" /> -->

                                    <div class="p-field p-col p-mb-0">
                                        <span class="p-float-label">
                                            <InputSwitch v-model="driver.inherit" class="p-mr-2" />
                                            <span>{{ $t('documentExecution.dossier.designerDialog.inherit') }}</span>
                                        </span>
                                    </div>

                                    <span v-if="!driver.inherit">
                                        <div class="p-field p-col p-mb-0">
                                            <span class="p-float-label">
                                                <InputText id="dossierUrlName" v-model="driver.dossier_url_name" class="kn-material-input" type="text" />
                                                <label for="dossierUrlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.dossierUrlName') }}</label>
                                            </span>
                                            <KnValidationMessages class="p-mt-1" :v-comp="driver.dossier_url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.dossierUrlName') }" />
                                        </div>
                                        <div class="p-field p-col p-mb-0">
                                            <span class="p-float-label">
                                                <InputText id="urlName" v-model="driver.url_name" class="kn-material-input" type="text" />
                                                <label for="urlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.urlName') }}</label>
                                            </span>
                                            <KnValidationMessages class="p-mt-1" :v-comp="driver.url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.urlName') }" />
                                        </div>
                                    </span>
                                </div></div
                        ></template>
                    </Card>
                </div>
            </div>
        </div>
        <template #footer>
            <Button v-if="step < 2" class="kn-button kn-button--secondary" @click="back"> {{ $t('common.back') }}</Button>
            <Button v-if="step == 0" class="kn-button kn-button--primary" :disabled="v$.$invalid" @click="next"> {{ $t('common.next') }}</Button>
            <template v-if="step == 1">
                <Button class="kn-button kn-button--primary" @click="closeAndGoToDocument()"> {{ $t('documentExecution.dossier.designerDialog.closeAndRun') }}</Button>
                <Button class="kn-button kn-button--primary" icon="fa fa-chevron-down" @click="toggle($event)" />
                <Menu ref="executeButtonMenu" :model="executeMenuItems" :popup="true" />
            </template>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import { iDocument } from '../../DocumentDetails'
import { createValidations } from '@/helpers/commons/validationHelper'
import { mapState, mapActions } from 'pinia'
import Dialog from 'primevue/dialog'
import ProgressSpinner from 'primevue/progressspinner'
import mainStore from '../../../../../App.store'
import cryptoRandomString from 'crypto-random-string'
import { iDossierTemplate, iPlaceholder, iView } from '@/modules/documentExecution/documentDetails/dialogs/dossierDesignerDialog/DossierTemplate'
import useValidate from '@vuelidate/core'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import descriptor from '@/modules/documentExecution/documentDetails/dialogs/dossierDesignerDialog/DocumentDetailDossierDesignerDialogDescriptor.json'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { FilterOperator } from 'primevue/api'
import Dropdown from 'primevue/dropdown'
import DocDialog from '@/modules/managers/crossNavigationManagement/dialogs/CrossNavigationManagementDocDialog.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import KnHint from '@/components/UI/KnHint.vue'
import Message from 'primevue/message'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'document-detail-dossier-designer-dialog',
    components: { Card, Column, DataTable, Dialog, Dropdown, DocDialog, KnInputFile, KnHint, InputSwitch, Listbox, Menu, Message, ProgressSpinner },
    props: {
        visible: { type: Boolean },
        selectedDocument: { type: Object as PropType<iDocument> }
    },
    emits: ['designerStarted', 'close', 'touched'],
    data() {
        return {
            descriptor,
            filterDefault,
            FilterOperator,
            document: null as iDocument | null,
            sbiExecutionId: '',
            loading: false,
            uploadedFiles: [],
            fileName: '',
            step: 0,
            v$: useValidate() as any,
            dirty: false,
            placeholders: [],
            maxSizeLimit: 10000000,
            triggerUpload: false,
            uploading: false,
            activeTemplate: {} as iDossierTemplate,
            views: [] as iView[],
            selectedView: {} as iView,
            viewColumns: [],
            dialogVisible: false,
            docId: null,
            currentSelectedIndex: -1,
            executeMenuItems: [] as { label: string; command: Function }[],
            filters: {
                global: [filterDefault],
                typeCode: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                creationUser: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                stateCodeStr: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as any
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    watch: {
        selectedDocument() {
            this.loadDocument()
        }
    },

    async created() {
        this.sbiExecutionId = cryptoRandomString({ length: 16, type: 'base64' })
        await this.loadDocument()
        await this.setActiveTemplate()
        if (this.activeTemplate && Object.keys(this.activeTemplate).length > 0) {
            this.activeTemplate.prefix ?? ''
            this.activeTemplate.placeholders ?? []
        }

        const descriptorColumns = JSON.parse(JSON.stringify(descriptor.view.columns))
        descriptorColumns.forEach((element) => {
            element.header = this.$t(element.header)
        })

        this.viewColumns = descriptorColumns
        /* MOCKS */
        this.views.push({ name: 'View 1', creationDate: new Date() })
        this.views.push({ name: 'View 2', creationDate: new Date() })
    },
    validations() {
        const validationObject = { activeTemplate: createValidations('activeTemplate', descriptor.validations.activeTemplate) }
        return validationObject
    },
    methods: {
        ...mapActions(mainStore, ['setLoading', 'setError', 'setInfo']),

        getTypes() {
            let types = JSON.parse(JSON.stringify(descriptor.linkTypes))
            if (this.activeTemplate.placeholders[this.currentSelectedIndex].parameters?.length == 0) types = types.filter((x) => x.code !== 'DRIVERS')
            if (this.activeTemplate.placeholders[this.currentSelectedIndex].views?.length == 0) types = types.filter((x) => x.code !== 'VIEWS')

            return types
        },
        async loadDocument() {
            this.document = this.selectedDocument ? { ...this.selectedDocument } : ({} as iDocument)
            this.sbiExecutionId = cryptoRandomString({ length: 16, type: 'base64' })

            this.initialize()
        },
        async initialize() {
            if (!this.document || !this.user) return
            this.setLoading(true)

            const url = 'http://localhost:3000/knowagedossierengine/api/start'
            this.setLoading(false)
        },
        closeDialog() {
            this.$emit('close')
        },
        onDelete(idx) {
            this.uploadedFiles.splice(idx)
        },
        onAdvancedUpload(data) {
            // eslint-disable-next-line
            // @ts-ignore
            this.uploadedFiles[0] = data.files[0]
        },
        async setActiveTemplate() {
            // TODO call service to have the template
        },
        async next() {
            if (this.step == 0) {
                const ppt = this.activeTemplate.type === 'PPT_TEMPLATE_V2'
                const typeEndpoint = ppt ? 'pptplaceholders' : 'docplaceholders'

                let url = `/knowagedossierengine/api/dossierdocument/${typeEndpoint}`

                const fileName = this.activeTemplate.fileName
                const userId = this.user?.userUniqueIdentifier
                const prefix = this.activeTemplate.prefix

                url += `?fileName=${fileName}&userId=${userId}&prefix=${prefix}`
                this.setLoading(true)
                this.activeTemplate.placeholders = []
                await this.$http
                    .get(url)
                    .then((response: AxiosResponse<any>) => {
                        response.data.forEach((element) => {
                            const item = { name: element.name } as iPlaceholder
                            this.activeTemplate.placeholders.push(item)
                        })
                    })
                    .finally(() => {
                        this.setLoading(false)
                    })

                if (this.activeTemplate.placeholders.length == 0) {
                    this.setInfo({ title: this.$t('common.toast.info'), msg: this.$t('documentExecution.dossier.noPlaceholdersFound') })
                    return
                }

                this.step++
            } else {
                this.step++
            }
        },
        back() {
            this.step--
            if (this.step < 0) {
                this.step = 0
                this.closeDialog()
            }
        },
        setUploadType() {
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        startTemplateUpload(event) {
            this.uploading = true
            this.uploadTemplate(event.target.files[0])
            this.setUploadType()
        },
        async uploadTemplate(uploadedFile) {
            const formData = new FormData()
            formData.append('file', uploadedFile)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'dossier/importTemplateFile', formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                .then(async () => {
                    this.activeTemplate.fileName = uploadedFile.name
                    this.activeTemplate.type = this.activeTemplate.fileName.indexOf('.docx') == -1 ? 'PPT_TEMPLATE_V2' : 'DOC_TEMPLATE'
                    this.setInfo({ title: this.$t('common.toast.success'), msg: this.$t('common.toast.uploadSuccess') })
                })
                .catch(() => this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.templateUploadError') }))
                .finally(() => (this.triggerUpload = false))
        },
        startTemplateCreation() {},
        selected(event) {
            let i = 0
            while (i < this.activeTemplate.placeholders.length) {
                if (this.activeTemplate.placeholders[i].name === event.value.name) break
                i++
            }

            this.currentSelectedIndex = i
        },
        async hadleDoc(doc) {
            //TODO
            console.log(doc)
            this.dialogVisible = false

            this.activeTemplate.placeholders[this.currentSelectedIndex] = { ...this.activeTemplate.placeholders[this.currentSelectedIndex], documentLabel: doc.DOCUMENT_LABEL, source: '' }
            this.activeTemplate.placeholders[this.currentSelectedIndex].parameters = []

            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documents/${doc.DOCUMENT_LABEL}/analyticalDrivers/`).then((response: AxiosResponse<any>) => {
                response.data.forEach((element) => {
                    this.activeTemplate.placeholders[this.currentSelectedIndex]?.parameters?.push({
                        label: element.label,
                        type: '',
                        url_name: '',
                        url_name_description: ''
                    })
                })
            })
        },
        handleDocDialog() {
            this.docId = this.document?.id
            this.dialogVisible = true
        },
        createMenuItems() {
            this.executeMenuItems = []
            this.executeMenuItems.push({ label: this.$t('common.close'), command: () => this.closeDialog() })
        },
        toggle(event: Event) {
            this.createMenuItems()
            const menu = this.$refs.executeButtonMenu as any
            menu.toggle(event)
        },
        closeAndGoToDocument() {
            this.closeDialog()
            this.$router.push(`/document-browser/dossier/${this.document?.label}`)
        },
        async saveTemplate() {
            /*    await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'dossier/importTemplateFile', this.activeTemplate, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } }).then(() => this.closeAndGoToDocument()) */
            this.closeAndGoToDocument()
        }
    }
})
</script>

<style lang="scss">
.dossier-designer-dialog {
    min-width: 900px;
    width: 70%;
    max-width: 1200px;
    min-height: 600px;
    height: 70%;

    .p-dialog-content {
        height: calc(100% - 35px);
        padding: 0;
        overflow-x: hidden;
    }

    .p-fileupload-buttonbar {
        border: none;

        .p-button:not(.p-fileupload-choose) {
            display: none;
        }

        .p-fileupload-choose {
            @extend .kn-button--primary;
        }
    }

    .p-card,
    .p-card .p-card-body,
    .p-card .p-card-content {
        padding: 0.25rem;
    }
}
</style>
