<template>
    <Dialog class="kn-dialog--toolbar--primary dossier-designer-dialog" :visible="visible" footer="footer" :header="$t(`documentExecution.dossier.designerDialog.step${step}.title`)" modal :base-z-index="9990" :closable="false">
        <ProgressSpinner v-if="loading" class="kn-progress-spinner" />
        <Message v-if="step == 0" class="p-m-4" severity="info" :closable="false">
            {{ $t(`documentExecution.dossier.designerDialog.step0.info`) }}
        </Message>

        <div>
            <div v-if="step == 0" class="p-grid kn-height-full p-pl-2 p-ml-2 p-pr-2 p-mr-2">
                <div class="p-col-6 p-d-flex">
                    <span class="p-float-label p-col">
                        <InputText id="fileName" v-model="v$.activeTemplate.name.$model" class="kn-material-input kn-width-full" :disabled="true" @change="setDirty()" />
                        <label for="fileName" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
                    </span>

                    <Button icon="fas fa-upload fa-1x" class="p-button-text p-button-plain p-ml-2" @click="setUploadType" />
                    <KnInputFile v-if="!uploading" :label="$t('documentExecution.dossier.designerDialog.templateFile')" :change-function="startTemplateUpload" accept=".docx, .pptx" :trigger-input="triggerUpload" />
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.activeTemplate.name.$model" />
                </div>
                <div class="p-col-6 p-d-flex">
                    <span class="p-float-label p-col">
                        <InputText
                            id="prefix"
                            v-model="v$.activeTemplate.prefix.$model"
                            class="kn-material-input kn-width-full"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.activeTemplate.prefix.$invalid && v$.activeTemplate.prefix.$dirty
                            }"
                            @blur="v$.activeTemplate.prefix.$touch()"
                            @change="setDirty()"
                        />
                        <label for="prefix" class="kn-material-input-label"> {{ $t('dashboard.widgetEditor.prefix') }}</label>
                        <small id="prefix-help">{{ $t('documentExecution.dossier.designerDialog.prefixHint') }}</small>
                    </span>

                    <KnValidationMessages class="p-mt-1" :v-comp="v$.activeTemplate.prefix" :additional-translate-params="{ fieldName: $t('dashboard.widgetEditor.prefix') }" />
                </div>

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
            </div>

            <div v-if="step == 1" class="p-grid kn-height-full p-pl-2 p-ml-2 p-pr-2 p-mr-2">
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
                            <div :class="['kn-list-item', 'selected']">
                                <div class="kn-list-item-text">{{ slotProps.option.imageName }}</div>

                                <i v-if="!slotProps.option.label" v-tooltip="$t('documentExecution.dossier.designerDialog.noDocumentLinkedToThePlaceholder')" class="fa-solid fa-triangle-exclamation"></i>
                            </div>
                        </template>
                    </Listbox>
                </div>
                <div v-if="currentSelectedIndex == -1" class="p-col-8">
                    <KnHint class="kn-hint-sm" :title="$t('documentExecution.dossier.designerDialog.placeholders')" :hint="$t('documentExecution.dossier.designerDialog.noPlaceholdersHint')" data-test="hint"></KnHint>
                </div>
                <div v-else class="p-col-8">
                    <Message class="p-m-4" severity="info" :closable="false">
                        {{ $t(`documentExecution.dossier.designerDialog.linkToDocumentHint`) }}
                    </Message>
                    <div class="p-col p-d-flex p-ai-center">
                        <span class="p-float-label p-col-8">
                            <InputText id="label" v-model="activeTemplate.placeholders[currentSelectedIndex].label" class="kn-material-input kn-width-full" type="text" :disabled="true" />
                            <label for="label" class="kn-material-input-label"> {{ $t('common.label') }}</label>
                        </span>
                        <Button icon="pi pi-plus-circle" class="p-button-text p-button-rounded p-button-plain" @click="handleDocDialog"></Button>
                        <span v-if="activeTemplate.placeholders[currentSelectedIndex].label && docHasDriversOrViews()" class="p-float-label p-col">
                            <Dropdown v-model="activeTemplate.placeholders[currentSelectedIndex].source" class="kn-material-input kn-width-full" :options="getTypes()" option-label="label" option-value="code" />
                            <label for="label" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.linkTo') }}</label>
                        </span>
                        <DocDialog :dialog-visible="docDialogVisible" :selected-doc="docId" :documents="documents" @close="docDialogVisible = false" @apply="handleDoc"></DocDialog>
                    </div>
                    <div v-if="activeTemplate.placeholders[currentSelectedIndex].source">
                        <Accordion :active-index="activeIndex" class="widget-editor-accordion">
                            <AccordionTab :header="$t('common.settings')">
                                <div class="p-grid kn-height-full p-pl-2 p-ml-2 p-pr-2 p-mr-2">
                                    <div class="p-col-4 p-d-flex">
                                        <span class="p-float-label">
                                            <InputNumber id="sheetHeight" v-model="activeTemplate.placeholders[currentSelectedIndex].sheetHeight" class="kn-material-input p-inputtext-sm kn-width-full kn-height-full" type="text" />
                                            <label for="sheetHeight" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.sheetHeight') }}</label>
                                        </span>
                                    </div>
                                    <div class="p-col-4 p-d-flex">
                                        <span class="p-float-label">
                                            <InputNumber id="sheetWidth" v-model="activeTemplate.placeholders[currentSelectedIndex].sheetWidth" class="kn-material-input p-inputtext-sm kn-width-full kn-height-full" type="text" />
                                            <label for="sheetWidth" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.sheetWidth') }}</label>
                                        </span>
                                    </div>
                                    <div class="p-col-4 p-d-flex">
                                        <span class="p-float-label">
                                            <InputNumber id="deviceScaleFactor" v-model="activeTemplate.placeholders[currentSelectedIndex].deviceScaleFactor" class="kn-material-input p-inputtext-sm kn-width-full kn-height-full" type="text" />
                                            <label for="deviceScaleFactor" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.deviceScaleFactor') }}</label>
                                        </span>
                                    </div>
                                </div>
                            </AccordionTab>
                            <AccordionTab :header="$t('common.parameters')" class="accordionTab">
                                <div v-if="activeTemplate.placeholders[currentSelectedIndex].label && !docHasDriversOrViews()">
                                    <Message class="p-m-4" severity="warn" :closable="false">
                                        {{ $t(`documentExecution.dossier.designerDialog.noDriversAndNoViewsForDocument`) }}
                                    </Message>
                                </div>
                                <div v-if="activeTemplate.placeholders[currentSelectedIndex].source === 'views'">
                                    <DataTable
                                        ref="dt"
                                        v-model:selection="activeTemplate.placeholders[currentSelectedIndex].views.selected"
                                        v-model:filters="filters"
                                        :value="activeTemplate.placeholders[currentSelectedIndex].views.availableViews"
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
                                <div v-else-if="activeTemplate.placeholders[currentSelectedIndex].source === 'drivers'">
                                    <div v-for="driver in activeTemplate.placeholders[currentSelectedIndex].parameters" :key="driver.label" class="kn-card p-m-2 p-p-2">
                                        <span class="p-text-bold p-text-italic">{{ driver.label }} </span>

                                        <div class="p-grid p-mt-2">
                                            <span class="p-float-label p-col-3">
                                                <Dropdown id="driverLinkType" v-model="driver.type" class="kn-material-input kn-width-full" :options="driverTypes" option-label="label" option-value="code" />
                                                <label for="driverLinkType" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.driverLinkType') }}</label>
                                            </span>
                                            <div v-if="driver.type == 'static'" class="p-grid p-col">
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
                                            <div v-else-if="driver.type == 'dynamic'" class="p-grid p-col">
                                                <div class="p-field p-col p-mb-0">
                                                    <span class="p-float-label">
                                                        <Dropdown v-model="driver.dossier_url_name" class="kn-material-input kn-width-full" :options="document?.drivers" option-label="label" option-value="parameterUrlName" />
                                                        <label for="dossierUrlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.dossierUrlName') }}</label>
                                                    </span>
                                                    <KnValidationMessages class="p-mt-1" :v-comp="driver.dossier_url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.dossierUrlName') }" />
                                                </div>
                                                <div class="p-field p-col p-mb-0">
                                                    <span class="p-float-label">
                                                        <InputText id="urlName" v-model="driver.dossier_url_name" class="kn-material-input" type="text" :disabled="true" />
                                                        <label for="urlName" class="kn-material-input-label"> {{ $t('documentExecution.dossier.designerDialog.urlName') }}</label>
                                                    </span>
                                                    <KnValidationMessages class="p-mt-1" :v-comp="driver.dossier_url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.urlName') }" />
                                                </div>
                                            </div>
                                            <div v-else-if="driver.type == 'inherit'" class="p-grid p-col">
                                                <div class="p-field p-col p-mb-0">
                                                    <span class="p-float-label">
                                                        <InputText id="urlName" v-model="driver.dossier_url_name" class="kn-material-input" type="text" :disabled="true" :hidden="true" />
                                                    </span>
                                                    <KnValidationMessages class="p-mt-1" :v-comp="driver.dossier_url_name" :additional-translate-params="{ fieldName: $t('documentExecution.dossier.designerDialog.urlName') }" />
                                                </div>
                                            </div>
                                        </div>

                                        <Divider class="p-m-0 p-p-0 dividerCustomConfig" type="solid" />
                                    </div>
                                </div>
                            </AccordionTab>
                        </Accordion>
                    </div>
                </div>
            </div>

            <DashboardControllerSaveDialog v-if="saveDialogVisible" :visible="saveDialogVisible" @save="saveNewDossier" @close="saveDialogVisible = false"></DashboardControllerSaveDialog>
        </div>
        <template #footer>
            <Button v-if="step >= 0 && step < 2" class="kn-button kn-button--secondary p-jc-start" @click="back"> {{ $t('common.back') }}</Button>
            <Button v-if="step == 0" class="kn-button kn-button--primary p-jc-end" :disabled="v$.$invalid" @click="next"> {{ $t('common.next') }}</Button>

            <Button v-if="step == 1" class="kn-button kn-button--primary p-jc-end" @click="saveAndClose()"> {{ $t('common.save') }}</Button>
            <Button v-if="step == 1" class="kn-button kn-button--primary p-jc-end" @click="saveAndRun()"> {{ $t('documentExecution.dossier.designerDialog.saveAndRun') }}</Button>
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
import { iDossierTemplate, iPlaceholder } from '@/modules/documentExecution/documentDetails/dialogs/dossierDesignerDialog/DossierTemplate'
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
import Listbox from 'primevue/listbox'
import InputSwitch from 'primevue/inputswitch'
import InputNumber from 'primevue/inputnumber'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Divider from 'primevue/divider'
import DashboardControllerSaveDialog from '@/modules/documentExecution/dashboard/DashboardControllerSaveDialog.vue'

export default defineComponent({
    name: 'document-detail-dossier-designer-dialog',
    components: { Accordion, AccordionTab, Divider, Column, DataTable, Dialog, Dropdown, DocDialog, KnInputFile, KnHint, InputNumber, InputSwitch, Listbox, Message, ProgressSpinner, DashboardControllerSaveDialog },
    props: {
        visible: { type: Boolean },
        selectedDocument: { type: Object as PropType<iDocument> },
        isFromWorkspace: Boolean
    },
    emits: ['designerStarted', 'close', 'touched'],
    data() {
        return {
            descriptor,
            filterDefault,
            FilterOperator,
            document: null as iDocument | null,
            loading: false,
            uploadedFile: {} as any,
            name: '',
            step: 0,
            v$: useValidate() as any,
            dirty: false,
            maxSizeLimit: 10000000,
            triggerUpload: false,
            uploading: false,
            activeTemplate: {} as iDossierTemplate,
            viewColumns: [] as { name: string; header: string }[],
            docDialogVisible: false,
            docId: -1,
            currentSelectedIndex: -1,
            executeMenuItems: [] as { label: string; command: Function }[],
            documents: [],
            driverTypes: [] as { code: string; label: string }[],
            linkTypes: [] as { code: string; label: string }[],
            sheetHeight: 1366,
            sheetWidth: 650,
            deviceScaleFactor: 1.5,
            activeIndex: 1,
            saveDialogVisible: false,
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
        await this.loadDocument()
        await this.setActiveTemplate()
    },
    validations() {
        const validationObject = { activeTemplate: createValidations('activeTemplate', descriptor.validations.activeTemplate) }
        return validationObject
    },
    methods: {
        ...mapActions(mainStore, ['setError', 'setInfo', 'setLoading']),

        docHasDriversOrViews(): boolean {
            return this.getTypes().length > 0
        },

        isEmpty(): boolean {
            const selectedPlaceholder = this.activeTemplate.placeholders[this.currentSelectedIndex]
            return (this.currentSelectedIndex != -1 && selectedPlaceholder?.parameters?.length != 0) || selectedPlaceholder?.views?.length != 0
        },

        getTypes() {
            let types = JSON.parse(JSON.stringify(this.linkTypes))
            const currTempl = this.activeTemplate.placeholders[this.currentSelectedIndex]
            if (this.currentSelectedIndex != -1) {
                if (!currTempl.parameters || currTempl.parameters?.length == 0) types = types.filter((x) => x.code !== 'drivers')
                if (!currTempl.views || currTempl.views?.length == 0) types = types.filter((x) => x.code !== 'views')
                if (types.length == 1) currTempl.source = types[0].code
            }

            return types
        },
        async loadDocument() {
            this.document = this.selectedDocument ? { ...this.selectedDocument } : ({} as iDocument)

            await this.initialize()
        },
        async initialize() {
            if (!this.document || !this.user) return
            this.loading = true

            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument')
                .then((response: AxiosResponse<any>) => (this.documents = response.data))
                .finally(() => (this.loading = false))

            this.applyTranslations()

            /* MOCKS */

            /*    this.views.push({ name: 'View 1', creationDate: new Date() })
                this.views.push({ name: 'View 2', creationDate: new Date() }) */

            // const url = `/knowagedossierengine/api/start/dossierTemplate?documentId=${this.document.id}`
            //  const url = 'http://localhost:3000/knowagedossierengine/api/start'
            // await this.$http.post(url, null, { headers: { Accept: 'application/json, text/plain, */*' } }).then((response: AxiosResponse<any>) => {
            //     console.log(response)
            // })

            this.loading = false
        },

        applyTranslations() {
            const descriptorColumns = JSON.parse(JSON.stringify(descriptor.view.columns))
            descriptorColumns.forEach((element) => {
                element.header = this.$t(element.header)
            })
            this.viewColumns = descriptorColumns

            const descriptorDriverTypes = JSON.parse(JSON.stringify(descriptor.driverTypes.filter((x) => (this.isFromWorkspace ? x.code !== 'dynamic' : true))))
            descriptorDriverTypes.forEach((element) => {
                element.label = this.$t(element.label)
            })
            this.driverTypes = descriptorDriverTypes

            const descriptorLinkTypes = JSON.parse(JSON.stringify(descriptor.linkTypes.filter((x) => (this.isFromWorkspace ? x.code !== 'views' : true))))
            descriptorLinkTypes.forEach((element) => {
                element.label = this.$t(element.label)
            })
            this.linkTypes = descriptorLinkTypes
        },
        closeDialog() {
            this.$emit('close')
        },

        async setActiveTemplate() {
            /*const userId = this.user?.userUniqueIdentifier
            await this.$http.get(`/knowagedossierengine/api/dossierdocument/dossierTemplate?user_id=${userId}`).then((response: any) => {
                if (response.data) this.activeTemplate = response
            })*/
            /*this.activeTemplate = {
                name: 'TEST_SIL_TEMPLATE.docx',
                type: 'DOC_TEMPLATE',
                prefix: 'img_sil',
                placeholders: [
                    {
                        imageName: 'img_sil_01',
                        label: 'Registry_with_2_ADs',
                        source: 'drivers',
                        sheetHeight:0,
                        sheetWidth:0,
                        deviceScaleFactor:1.5,
                        parameters: [
                            { url_name: 'Categoria', type: 'dynamic', dossier_url_name: 'categoria' },
                            { dossier_url_name: 'familia', url_name: 'familia', type: 'dynamic' }
                        ],
                        views: []
                    },
                    { imageName: 'img_sil_02', label: 'R_registry_uno_a_uno', source: 'drivers', parameters: [{ url_name: 'ad_regione', type: 'static', dossier_url_name: 'a', value: 'c' }], views: [] },
                    { imageName: 'img_sil_03', label: 'KPI1_NODRIVER', source: 'drivers', parameters: [], views: [] }
                ]
            }*/
        },
        async next() {
            if (this.step == 0) {
                const ppt = this.activeTemplate.type === 'PPT_TEMPLATE_V2'
                const typeEndpoint = ppt ? 'pptplaceholders' : 'docplaceholders'

                if (!(await this.isValidFile())) {
                    // validation
                    this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.templateUploadError') })
                    return
                }

                let url = `/knowagedossierengine/api/dossierdocument/${typeEndpoint}`

                const fileName = this.activeTemplate.name
                const userId = this.user?.userUniqueIdentifier
                const prefix = this.activeTemplate.prefix

                url += `?fileName=${fileName}&prefix=${prefix}&user_id=${userId}`
                this.loading = true

                const placeholdersInTheLastTemplate = this.activeTemplate.placeholders?.length > 0
                await this.$http
                    .get(url)
                    .then((response: AxiosResponse<any>) => {
                        response.data.forEach((element) => {
                            if (placeholdersInTheLastTemplate) {
                                const existing = this.activeTemplate.placeholders.filter((x) => x.imageName == element.name)
                                if (existing.length > 0) {
                                    return
                                }
                            }
                            const item = { imageName: element.name, sheetHeight: this.sheetHeight, sheetWidth: this.sheetWidth, deviceScaleFactor: this.deviceScaleFactor } as iPlaceholder
                            this.activeTemplate.placeholders.push(item)
                        })
                    })
                    .finally(() => {
                        this.loading = false
                    })

                if (this.activeTemplate.placeholders.length == 0) {
                    this.setInfo({ title: this.$t('common.toast.info'), msg: this.$t('documentExecution.dossier.noPlaceholdersFound') })
                    return
                }

                this.step++
            } else if (this.step == 1) {
                this.saveDialogVisible = true
                this.step++
            } else {
                this.step++
            }
        },
        async saveNewDossier(document: { name: string; label: string }) {
            await this.saveDossier(document)
        },
        async saveDossier(document: any) {
            this.setLoading(true)
            if (!this.document) return

            const formattedAnalysis = {
                document: {
                    name: document.name,
                    label: document.label,
                    description: document.description
                },
                action: 'DOC_SAVE',
                updateFromWorkspace: false
            }
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/saveDocument/', formattedAnalysis, { headers: { 'X-Disable-Errors': 'true' } })
                .then((response: any) => {
                    this.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.saveDialogVisible = false

                    this.document = { ...response.data }

                    this.save()
                })
                .catch((response: any) => {
                    this.setError({
                        title: this.$t('common.toast.createTitle'),
                        msg: response
                    })
                })

            this.setLoading(false)
        },
        back() {
            this.dirty = false
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
            this.uploadedFile = event.target.files[0]
            this.uploadTemplate()
            this.setUploadType()
            setTimeout(() => (this.uploading = false), 200)
        },
        async uploadTemplate() {
            this.activeTemplate.name = this.uploadedFile.name
            this.activeTemplate.type = this.getDossierType(this.activeTemplate.name)
            this.activeTemplate.placeholders = []
        },
        selected(event) {
            const pos = this.activeTemplate.placeholders.map((e) => e.imageName).indexOf(event.value.imageName)

            if (pos == -1) {
                this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.designerDialog.errorSelectingPlaceholder') })
                return
            }

            this.currentSelectedIndex = pos
        },
        async handleDoc(doc) {
            this.docDialogVisible = false

            this.activeTemplate.placeholders[this.currentSelectedIndex] = { ...this.activeTemplate.placeholders[this.currentSelectedIndex], label: doc.DOCUMENT_LABEL, source: '' }

            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${doc.DOCUMENT_ID}/drivers`).then((response: AxiosResponse<any>) => {
                this.activeTemplate.placeholders[this.currentSelectedIndex].parameters = []
                this.activeTemplate.placeholders[this.currentSelectedIndex].parameters = response.data
            })

            /* TODO LOAD VIEWS */
            /*await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documents/${doc.DOCUMENT_LABEL}/views/`).then((response: AxiosResponse<any>) => {
                response.data.forEach((element) => {
                    this.activeTemplate.placeholders[this.currentSelectedIndex].views = []
                    this.activeTemplate.placeholders[this.currentSelectedIndex]?.views?.push({
                        label: element.label,
                        type: '',
                        url_name: '',
                        url_name_description: ''
                    })
                })
            }) */
        },
        handleDocDialog() {
            this.docId = this.document?.id
            this.docDialogVisible = true
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
        async saveAndRun() {
            if (this.isFromWorkspace) {
                this.saveDialogVisible = true
            } else {
                await this.save().then(() => {
                    this.$router.push(`/dossier/${this.document?.label}`)
                })
            }
        },
        async saveAndClose() {
            if (this.isFromWorkspace) {
                this.saveDialogVisible = true
            } else {
                await this.save().then(() => {
                    this.$router.go(0)
                })
            }
        },
        async save() {
            const formData = new FormData()
            formData.append('file', this.uploadedFile)
            formData.append('documentId', '' + this.getDocument()?.id)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'dossier/importTemplateFile', formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                .then(async () => {
                    this.activeTemplate.name = this.uploadedFile.name
                    this.activeTemplate.type = this.getDossierType(this.activeTemplate.name)

                    const templateToSave = await this.handleDrivers()

                    const objToSend = { id: this.getDocument()?.id, template: templateToSave }

                    // SAVE TEMPLATE
                    await this.$http
                        .post(`/knowagedossierengine/api/dossierdocument/saveTemplate?user_id=${this.user?.userUniqueIdentifier}`, objToSend)
                        .then(() => {
                            this.closeDialog()
                            this.setInfo({ title: this.$t('common.toast.success'), msg: this.$t('common.toast.uploadSuccess') })
                        })
                        .catch(() => {
                            this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.errorSavingDocument') })
                        })
                })
                .catch(() => this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.templateUploadError') }))
                .finally(() => (this.triggerUpload = false))
        },
        async handleDrivers() {
            const objToSave = JSON.parse(JSON.stringify(this.activeTemplate))
            objToSave.placeholders = objToSave.placeholders.filter((x) => x.label)
            const tempDrivers = objToSave.placeholders.filter((x) => x.label)
            for (let i = 0; i < tempDrivers.length; i++) {
                const placeholder = tempDrivers[i]

                for (let j = 0; j < placeholder.parameters?.length; j++) {
                    if (placeholder.parameters[j].type === 'static') {
                        placeholder.parameters[j] = {
                            url_name: placeholder.parameters[j].parameterUrlName,
                            type: 'static',
                            dossier_url_name: placeholder.parameters[j].dossier_url_name,
                            value: placeholder.parameters[j].value
                        }
                    } else if (placeholder.parameters[j].type === 'dynamic') {
                        placeholder.parameters[j] = {
                            url_name: placeholder.parameters[j].parameterUrlName,
                            type: 'dynamic',
                            dossier_url_name: placeholder.parameters[j].dossier_url_name
                        }
                    } else if (placeholder.parameters[j].type === 'inherit') {
                        const existing = this.document?.drivers?.filter((x) => x.parameterUrlName === placeholder.parameters[j].parameterUrlName)

                        if (existing?.length > 0) {
                            placeholder.parameters[j] = {
                                url_name: existing[0].parameterUrlName,
                                type: 'dynamic',
                                dossier_url_name: existing[0].parameterUrlName
                            }
                        } else {
                            const newDriver = { ...placeholder.parameters[j] }
                            newDriver.modifiable = 0
                            delete newDriver.id
                            newDriver.biObjectID = this.getDocument()?.id
                            delete newDriver.type
                            newDriver.prog = this.document?.drivers?.length ?? 1

                            await this.$http
                                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${newDriver.biObjectID}/drivers`, newDriver, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                                .then(() => {
                                    placeholder.parameters[j] = {
                                        url_name: newDriver.parameterUrlName,
                                        type: 'dynamic',
                                        dossier_url_name: newDriver.dossier_url_name
                                    }
                                })
                                .catch(() => this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.documentDetails.drivers.persistError') }))

                            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${newDriver.biObjectID}/drivers`).then((response: AxiosResponse<any>) => {
                                if (this.document && this.document.drivers) this.document.drivers = response.data
                            })
                        }
                    } else {
                        this.setError({
                            title: this.$t('common.error.generic'),
                            msg: this.$t('documentExecution.dossier.designerDialog.driverNotHandled', { driverName: placeholder.parameters[j].label, placeholderName: placeholder.imageName })
                        })
                        return
                    }
                }
            }
            return objToSave
        },
        setDirty() {
            this.dirty = true
            this.$emit('touched')
        },
        getDossierType(fileName: string): string {
            return fileName.indexOf('.docx') == -1 ? 'PPT_TEMPLATE_V2' : 'DOC_TEMPLATE'
        },
        async isValidFile() {
            const fileName = this.uploadedFile?.name
            let valid = !this.activeTemplate?.type || this.activeTemplate?.type === this.getDossierType(fileName)
            if (!valid) return false

            const formData = new FormData()
            formData.append('file', this.uploadedFile)
            formData.append('documentId', '' + this.getDocument()?.id)
            formData.append('prefix', '' + this.activeTemplate.prefix)
            await this.$http
                .post(`/knowagedossierengine/api/dossiervalidator/validateDocument?user_id=${this.user?.userUniqueIdentifier}`, formData, { headers: { 'Content-Type': 'multipart/form-data', 'X-Disable-Errors': 'true' } })
                .then(() => (valid = true))
                .catch(() => this.setError({ title: this.$t('common.error.generic'), msg: this.$t('documentExecution.dossier.errorDuringValidation') }))
                .finally()

            return valid
        },
        getDocument() {
            return this.selectedDocument || this.document
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
.dividerCustomConfig {
    border: 0.5px solid;
    border-color: var(--kn-color-borders);
}

.widget-editor-accordion {
    ::v-deep(.p-accordion-tab-active) {
        margin: 0;
    }
}

.accordionTab {
    min-height: 50px;
}
</style>
