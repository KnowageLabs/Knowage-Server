<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            {{ menuNode.name }}
        </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="formValid" @click="save" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeForm" />
        </template>
    </Toolbar>

    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />

    <div class="p-grid p-m-0 p-fluid kn-page-content">
        <div class="p-col-12">
            <Card>
                <template #content>
                    <form ref="menu-configuration-form" class="p-p-3">
                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="name" v-model.trim="v$.menuNode.name.$model" type="text" class="p-inputtext p-component kn-material-input" @change="onDataChange(v$.menuNode.name)" />
                                    <label for="name">{{ $t('managers.menuManagement.form.name') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.menuNode.name" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.name') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="descr" v-model.trim="v$.menuNode.descr.$model" type="text" class="p-inputtext p-component kn-material-input" aria-describedby="descr-help" @blur="onDataChange(v$.menuNode.descr)" />
                                    <Button v-if="isIconSelectorShown(menuNode) && (menuNode.icon != null || menuNode.custIcon != null)" icon="pi pi-times" @click="clearSelectedIcon" />
                                    <Button v-if="isCustomIconShown(menuNode)"><img style="max-height: 26px; max-width: 26px" :src="selectedIcon" /></Button>
                                    <Button v-if="isFaIconShown(menuNode)"><i :class="selectedIcon"></i></Button>
                                    <Button v-if="isIconSelectorShown(menuNode)" class="p-button" @click="openFontAwesomeSelectionModal()">{{ $t('managers.menuManagement.chooseIcon').toUpperCase() }}</Button>
                                    <label for="descr">{{ $t('managers.menuManagement.description') }} *</label>
                                </span>
                            </div>
                            <small id="descr-help">{{ $t('managers.menuManagement.descrHelp') }}</small>
                            <KnValidationMessages :v-comp="v$.menuNode.descr" :additional-translate-params="{ fieldName: $t('managers.menuManagement.description') }"></KnValidationMessages>
                        </div>

                        <FontAwesomePicker :show-modal="chooseIconModalShown" @chooseIcon="onChoosenIcon" @closeFontAwesomeModal="closeFontAwesomeSelectionModal"></FontAwesomePicker>

                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="menuNodeContent"
                                        v-model="v$.menuNode.menuNodeContent.$model"
                                        :options="menuNodeContent"
                                        option-label="name"
                                        option-value="value"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                        @change="onMenuNodeChange(v$.menuNode.menuNodeContent)"
                                    />
                                    <label for="menuNodeContent">{{ $t('managers.menuManagement.form.menuNodeContent') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.menuNode.menuNodeContent" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.menuNodeContent') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5" :hidden="staticPageHidden">
                            <div class="p-field">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="staticPage"
                                            v-model="v$.menuNode.staticPage.$model"
                                            :options="staticPagesList"
                                            option-label="name"
                                            option-value="name"
                                            class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                            @change="onStaticPageSelect(v$.menuNode.staticPage)"
                                        />
                                        <label for="staticPage">{{ $t('managers.menuManagement.form.staticPage') }} *</label>
                                    </span>
                                </div>
                                <KnValidationMessages :v-comp="v$.menuNode.staticPage" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.staticPage') }"></KnValidationMessages>
                            </div>
                        </div>

                        <div class="p-field p-mb-5" :hidden="externalAppHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="externalApplicationUrl" v-model.trim="v$.menuNode.externalApplicationUrl.$model" type="text" class="p-inputtext p-component kn-material-input" @blur="onDataChange(v$.menuNode.externalApplicationUrl)" />
                                    <label for="externalApplicationUrl">{{ $t('managers.menuManagement.form.externalApplicationUrl') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.menuNode.externalApplicationUrl" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.externalApplicationUrl') }"></KnValidationMessages>
                        </div>

                        <div :hidden="documentHidden">
                            <div class="p-field p-mb-5">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <InputText id="selectedDocument" v-model.trim="v$.menuNode.document.$model" type="text" class="p-inputtext p-component kn-material-input" @blur="onDataChange(v$.menuNode.document)" />
                                        <InputText id="objId" v-model.trim="v$.menuNode.objId.$model" :hidden="true" type="text" class="p-inputtext p-component kn-material-input" @blur="onDataChange(v$.menuNode.objId)" />
                                        <Button icon="pi pi-search" class="p-button" @click="openRelatedDocumentModal()" />
                                        <label for="objId">{{ $t('managers.menuManagement.form.document') }} *</label>
                                    </span>
                                </div>
                                <KnValidationMessages :v-comp="v$.menuNode.document" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.document') }"></KnValidationMessages>
                            </div>

                            <div class="p-field p-mb-5">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <InputText id="objParameters" v-model.trim="v$.menuNode.objParameters.$model" type="text" class="p-inputtext p-component kn-material-input" @blur="onDataChange(v$.menuNode.objParameters)" />
                                        <label for="objParameters">{{ $t('managers.menuManagement.form.objParameters') }}</label>
                                    </span>
                                </div>
                                <KnValidationMessages :v-comp="v$.menuNode.objParameters" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.objParameters') }"></KnValidationMessages>
                            </div>

                            <Dialog v-model:visible="displayModal" :header="$t('managers.menuManagement.selectDocument')" :style="{ width: '50vw' }" :modal="true">
                                <RelatedDocumentList :loading="loading" data-test="related-documents-list" @selectedDocument="onDocumentSelect"></RelatedDocumentList>
                            </Dialog>
                        </div>

                        <div class="p-field p-mb-5" :hidden="functionalityHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="functionality"
                                        v-model="v$.menuNode.functionality.$model"
                                        :options="menuNodeContentFunctionalies"
                                        option-label="name"
                                        option-value="value"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                        @change="onFunctionalityTypeChange(v$.menuNode.functionality)"
                                    />
                                    <label for="functionality">{{ $t('managers.menuManagement.form.functionality') }}*</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="v$.menuNode.functionality" :additional-translate-params="{ fieldName: $t('managers.menuManagement.form.functionality') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5" :hidden="workspaceInitialHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown id="initialPath" v-model="v$.menuNode.initialPath.$model" :options="workspaceOptions" option-label="name" option-value="value" class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input" />
                                    <label for="initialPath">{{ $t('managers.menuManagement.form.initialPath') }} *</label>
                                </span>
                            </div>
                        </div>

                        <div class="p-field p-mb-5" :hidden="documentTreeHidden">
                            <p>Open document browser on {{ v$.menuNode.initialPath.$model }}</p>
                            <DocumentBrowserTree :selected="v$.menuNode.initialPath.$model" :loading="loading" @selectedDocumentNode="onSelectedDocumentNode"></DocumentBrowserTree>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
        <div class="p-col-12">
            <RolesCard :hidden="hideForm" :roles-list="roles" :parent-node-roles="parentNodeRoles" :selected="selectedMenuNode.roles" @changed="setSelectedRoles($event)"></RolesCard>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iMenuNode } from '../MenuManagement'
import { iRole } from '../../usersManagement/UsersManagement'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dropdown from 'primevue/dropdown'
import Dialog from 'primevue/dialog'
import RelatedDocumentList from '../RelatedDocumentsList/MenuManagementRelatedDocumentList.vue'
import RolesCard from '../RolesCard/MenuManagementRolesCard.vue'
import DocumentBrowserTree from '../DocumentBrowserTree/MenuManagementDocumentBrowserTree.vue'
import FontAwesomePicker from '../IconPicker/IconPicker.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import MenuConfigurationDescriptor from '../MenuManagementDescriptor.json'
import MenuConfigurationValidationDescriptor from './MenuManagementValidationDescriptor.json'
import MenuManagementElementDetailDescriptor from './MenuManagementElementDetailDescriptor.json'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'profile-attributes-detail',
    components: { Dropdown, DocumentBrowserTree, RelatedDocumentList, KnValidationMessages, Dialog, FontAwesomePicker, RolesCard },
    props: { roles: { type: Array }, selectedMenuNode: { type: Object, required: true }, selectedRoles: { type: Array }, staticPagesList: { type: Array }, menuNodes: { type: Array }, parentNodeRoles: { type: Array } },
    emits: ['refreshRecordSet', 'closesForm', 'dataChanged'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            v$: useValidate() as any,
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            menuNode: {} as iMenuNode,
            loading: false as boolean,
            hideForm: false as boolean,
            documentHidden: true as boolean,
            staticPageHidden: true as boolean,
            externalAppHidden: true as boolean,
            functionalityHidden: true as boolean,
            workspaceInitialHidden: true as boolean,
            documentTreeHidden: true as boolean,
            dirty: false as boolean,
            displayModal: false as boolean,
            chooseIconModalShown: false as boolean,
            relatedDocuments: [],
            selectedRelatedDocument: null as string | null,
            selectedIcon: null as string | null,
            selectedFunctionality: {},
            menuNodeContent: MenuConfigurationDescriptor.menuNodeContent,
            workspaceOptions: MenuConfigurationDescriptor.workspaceOptions,
            menuNodeContentFunctionalies: MenuConfigurationDescriptor.menuNodeContentFunctionalies,
            menuManagementElementDetailDescriptor: MenuManagementElementDetailDescriptor.importantfields,
            nodes: [] as iMenuNode[]
        }
    },
    computed: {
        formValid(): any {
            return this.v$.$invalid
        }
    },
    watch: {
        selectedMenuNode: {
            handler: function (node) {
                this.v$.$reset()
                this.loadNode(node)
            }
        },
        selectedRoles: {
            handler: function (roles) {
                this.menuNode.roles = roles
            }
        },
        menuNodes() {
            this.loadNodes()
        }
    },
    validations() {
        return {
            menuNode: createValidations('menuNode', MenuConfigurationValidationDescriptor.validations.menuNode)
        }
    },
    async created() {
        this.loadNodes()
        if (this.selectedMenuNode) {
            this.loadNode(this.selectedMenuNode)
        }
    },
    methods: {
        loadNodes() {
            this.nodes = this.menuNodes as iMenuNode[]
        },
        resetForm() {
            Object.keys(this.menuNode).forEach((k) => delete this.menuNode[k])
        },
        openRelatedDocumentModal() {
            this.displayModal = true
        },
        closeRelatedDocumentModal() {
            this.displayModal = false
        },
        showForm() {
            this.resetForm()
            this.hideForm = false
        },
        clearSelectedIcon() {
            this.selectedIcon = ''
            this.menuNode.custIcon = null
            this.menuNode.icon = null
        },
        setSelectedRoles(roles: iRole[]) {
            this.menuNode.roles = roles
        },
        toggleDocument() {
            this.functionalityHidden = this.staticPageHidden = this.externalAppHidden = this.documentTreeHidden = this.workspaceInitialHidden = true
            this.documentHidden = false
        },
        toggleStaticPage() {
            this.functionalityHidden = this.externalAppHidden = this.documentHidden = this.documentTreeHidden = this.workspaceInitialHidden = true
            this.staticPageHidden = false
        },
        toggleExternalApp() {
            this.functionalityHidden = this.documentHidden = this.staticPageHidden = this.documentTreeHidden = this.workspaceInitialHidden = true
            this.externalAppHidden = false
        },
        toggleFunctionality() {
            this.externalAppHidden = this.documentHidden = this.staticPageHidden = true
            this.functionalityHidden = false
            if (this.menuNode.functionality == 'WorkspaceManagement') {
                this.toggleWorkspaceInitial()
            } else if (this.menuNode.functionality == 'DocumentUserBrowser') {
                this.toggleDocumentTreeSelect()
            }
        },
        isIconSelectorShown(node: iMenuNode) {
            if (node.level == 1) {
                return true
            }
        },
        isFaIconShown(node: iMenuNode) {
            if (node.level == 1 && node.icon != null) {
                return true
            }
        },
        isCustomIconShown(node: iMenuNode) {
            if (node.level == 1 && node.custIcon != null) {
                return true
            }
        },
        toggleEmpty() {
            this.functionalityHidden = this.externalAppHidden = this.documentHidden = this.staticPageHidden = this.documentTreeHidden = this.workspaceInitialHidden = true
        },
        toggleWorkspaceInitial() {
            this.workspaceInitialHidden = false
            this.documentTreeHidden = true
        },
        toggleDocumentTreeSelect() {
            this.documentTreeHidden = false
            this.workspaceInitialHidden = true
        },
        onMenuNodeChange(menuNodeContent) {
            if (menuNodeContent.$model == 1) {
                this.toggleDocument()
            } else if (menuNodeContent.$model == 3) {
                this.toggleStaticPage()
            } else if (menuNodeContent.$model == 2) {
                this.toggleExternalApp()
            } else if (menuNodeContent.$model == 4) {
                this.toggleFunctionality()
            } else {
                this.toggleEmpty()
            }
        },
        onFunctionalityTypeChange(functionality) {
            if (functionality.$model == 'WorkspaceManagement') {
                this.toggleWorkspaceInitial()
            } else if (functionality.$model == 'DocumentUserBrowser') {
                this.toggleDocumentTreeSelect()
            }
        },
        openFontAwesomeSelectionModal() {
            this.chooseIconModalShown = true
        },
        closeFontAwesomeSelectionModal() {
            this.chooseIconModalShown = false
        },
        setBase64Image(base64image) {
            this.menuNode.icon = null
            this.menuNode.custIcon = {
                id: null,
                className: 'custom',
                unicode: null,
                category: 'custom',
                label: 'logo.png',
                src: base64image,
                visible: true
            }
            this.selectedIcon = base64image
        },
        onChoosenIcon(choosenIcon) {
            if (typeof choosenIcon == 'string') {
                this.setBase64Image(choosenIcon)
            } else {
                this.menuNode.icon = {
                    id: choosenIcon.id,
                    className: 'fas fa-' + choosenIcon.name,
                    unicode: choosenIcon.value,
                    category: 'solid',
                    label: '',
                    src: null,
                    visible: true
                }

                this.menuNode.custIcon = null
                this.selectedIcon = this.menuNode.icon.className = 'fas fa-' + choosenIcon.name
                this.menuNode.icon.id = choosenIcon.id
            }

            this.closeFontAwesomeSelectionModal()
        },
        onDocumentSelect(document) {
            this.menuNode.objId = document.DOCUMENT_ID
            this.menuNode.document = document.DOCUMENT_NAME
            this.closeRelatedDocumentModal()
        },
        async save() {
            if (this.checkIfNodeExists()) {
                this.store.setError({ title: this.$t('managers.menuManagement.info.errorTitle'), msg: this.$t('managers.menuManagement.info.duplicateErrorMessage') })
                return
            }

            let response: AxiosResponse<any>

            if (this.menuNode.menuId != null) {
                response = await this.$http.put(this.apiUrl + 'menu/' + this.menuNode.menuId, this.getMenuDataForSave())
            } else {
                response = await this.$http.post(this.apiUrl + 'menu/', this.getMenuDataForSave())
            }

            if (response.status == 200) {
                if (response.data.errors) {
                    this.store.setError({ title: this.$t('managers.menuManagement.info.errorTitle'), msg: this.$t('managers.menuManagement.info.errorMessage') })
                } else {
                    this.store.setInfo({ title: this.$t('managers.menuManagement.info.saveTitle'), msg: this.$t('managers.menuManagement.info.saveMessage') })
                }
            }
            this.$emit('refreshRecordSet')
            this.resetForm()
        },
        checkIfNodeExists() {
            let exists = false
            const menuItemForSave = this.getMenuDataForSave()

            if (!menuItemForSave.parentId) menuItemForSave.parentId = null

            for (let i = 0; i < this.nodes.length; i++) {
                const tempNode = this.nodes[i] as iMenuNode
                if (tempNode.menuId != menuItemForSave.menuId && tempNode.parentId === menuItemForSave.parentId && tempNode.name === menuItemForSave.name) {
                    exists = true
                    break
                }
            }

            return exists
        },
        closeForm() {
            this.$emit('closesForm')
        },
        onAttributeSelect(event: any) {
            this.populateForm(event.data)
        },
        populateForm(menuNode: iMenuNode) {
            this.hideForm = false
            this.menuNode = { ...menuNode }
            if (menuNode.objId) {
                this.getDocumentNameByID(menuNode.objId)
            }

            if (menuNode.custIcon != null) {
                //var base64regex = /^([0-9a-zA-Z+/]{4})*(([0-9a-zA-Z+/]{2}==)|([0-9a-zA-Z+/]{3}=))?$/;
                this.selectedIcon = menuNode.custIcon.src
            } else if (menuNode.icon != null) {
                this.selectedIcon = menuNode.icon.className
            } else {
                this.selectedIcon = null
            }
            if (this.menuNode.functionality != null) {
                this.menuNode.menuNodeContent = 4
                this.toggleFunctionality()
            } else if (this.menuNode.externalApplicationUrl != null) {
                this.menuNode.menuNodeContent = 2
                this.toggleExternalApp()
            } else if (this.menuNode.objId != null) {
                this.menuNode.menuNodeContent = 1
                this.toggleDocument()
            } else if (this.menuNode.staticPage != null && this.menuNode.staticPage != '') {
                this.menuNode.menuNodeContent = 3
                this.toggleStaticPage()
            } else {
                this.menuNode.menuNodeContent = 0
                this.toggleEmpty()
            }
        },
        getMenuDataForSave() {
            const menuNodeForSave = { ...this.menuNode }

            const fieldsList: string[] = this.menuManagementElementDetailDescriptor.fieldsList
            const fieldToSave: any = this.menuManagementElementDetailDescriptor.filedsToSave[menuNodeForSave.menuNodeContent]

            fieldsList.forEach((field) => !fieldToSave.fields.includes(field) && (menuNodeForSave[field] = null))

            delete menuNodeForSave.menuNodeContent

            if (!menuNodeForSave.parentId) menuNodeForSave.parentId = null

            return menuNodeForSave
        },
        async getDocumentNameByID(id: any) {
            await this.$http.get(this.apiUrl + 'documents/' + id).then((response: AxiosResponse<any>) => {
                this.menuNode.document = response.data.name
            })
        },
        onStaticPageSelect() {
            this.menuNode.initialPath = this.menuNode.functionality = this.menuNode.objParameters = this.menuNode.objId = this.menuNode.externalApplicationUrl = null
        },
        onDataChange(v$Comp) {
            v$Comp.$touch()
            this.$emit('dataChanged')
        },
        loadNode(menuNode) {
            if (menuNode.menuId === null) {
                this.resetForm()
                return
            }
            this.populateForm(menuNode)
        },

        onSelectedDocumentNode(documentInitialPath) {
            this.menuNode.initialPath = documentInitialPath
        }
    }
})
</script>

<style lang="scss" scoped>
.table-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    @media screen and (max-width: 960px) {
        align-items: start;
    }
}

.record-image {
    width: 50px;
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
}

.p-dialog .record-image {
    width: 50px;
    margin: 0 auto 2rem auto;
    display: block;
}

.confirmation-content {
    display: flex;
    align-items: center;
    justify-content: center;
}
@media screen and (max-width: 960px) {
    ::v-deep(.p-toolbar) {
        flex-wrap: wrap;

        .p-button {
            margin-bottom: 0.25rem;
        }
    }
}
</style>
