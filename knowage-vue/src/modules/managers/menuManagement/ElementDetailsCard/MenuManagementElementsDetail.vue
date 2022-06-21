<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            {{ menuNode.name }}
        </template>
        <template #end>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="save" :disabled="formValid" />
            <Button class="p-button-text p-button-rounded p-button-plain" icon="pi pi-times" @click="closeForm" />
        </template>
    </Toolbar>

    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />

    <div class="p-grid p-m-0 p-fluid kn-page-content">
        <div class="p-col-12">
            <Card>
                <template #content>
                    <form ref="menu-configuration-form" class="p-p-3">
                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="name" type="text" v-model.trim="v$.menuNode.name.$model" @change="onDataChange(v$.menuNode.name)" class="p-inputtext p-component kn-material-input" />
                                    <label for="name">{{ $t('managers.menuManagement.form.name') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.menuNode.name" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.name') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="descr" type="text" v-model.trim="v$.menuNode.descr.$model" @blur="onDataChange(v$.menuNode.descr)" class="p-inputtext p-component kn-material-input" aria-describedby="descr-help" />
                                    <Button v-if="isIconSelectorShown(menuNode) && (menuNode.icon != null || menuNode.custIcon != null)" icon="pi pi-times" @click="clearSelectedIcon" />
                                    <Button v-if="isCustomIconShown(menuNode)"><img style="max-height: 26px; max-width: 26px" :src="selectedIcon" /></Button>
                                    <Button v-if="isFaIconShown(menuNode)"><i :class="selectedIcon"></i></Button>
                                    <Button v-if="isIconSelectorShown(menuNode)" class="p-button" @click="openFontAwesomeSelectionModal()">{{ $t('managers.menuManagement.chooseIcon').toUpperCase() }}</Button>
                                    <label for="descr">{{ $t('managers.menuManagement.description') }} *</label>
                                </span>
                            </div>
                            <small id="descr-help">{{ $t('managers.menuManagement.descrHelp') }}</small>
                            <KnValidationMessages :vComp="v$.menuNode.descr" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.description') }"></KnValidationMessages>
                        </div>

                        <FontAwesomePicker :showModal="chooseIconModalShown" @chooseIcon="onChoosenIcon" @closeFontAwesomeModal="closeFontAwesomeSelectionModal"></FontAwesomePicker>

                        <div class="p-field p-mb-5">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="menuNodeContent"
                                        v-model="v$.menuNode.menuNodeContent.$model"
                                        :options="menuNodeContent"
                                        @change="onMenuNodeChange(v$.menuNode.menuNodeContent)"
                                        optionLabel="name"
                                        optionValue="value"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                    />
                                    <label for="menuNodeContent">{{ $t('managers.menuManagement.form.menuNodeContent') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.menuNode.menuNodeContent" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.menuNodeContent') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5" :hidden="staticPageHidden">
                            <div class="p-field">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <Dropdown
                                            id="staticPage"
                                            v-model="v$.menuNode.staticPage.$model"
                                            :options="staticPagesList"
                                            @change="onStaticPageSelect(v$.menuNode.staticPage)"
                                            optionLabel="name"
                                            optionValue="name"
                                            class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                        />
                                        <label for="staticPage">{{ $t('managers.menuManagement.form.staticPage') }} *</label>
                                    </span>
                                </div>
                                <KnValidationMessages :vComp="v$.menuNode.staticPage" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.staticPage') }"></KnValidationMessages>
                            </div>
                        </div>

                        <div class="p-field p-mb-5" :hidden="externalAppHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="externalApplicationUrl" type="text" v-model.trim="v$.menuNode.externalApplicationUrl.$model" @blur="onDataChange(v$.menuNode.externalApplicationUrl)" class="p-inputtext p-component kn-material-input" />
                                    <label for="externalApplicationUrl">{{ $t('managers.menuManagement.form.externalApplicationUrl') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.menuNode.externalApplicationUrl" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.externalApplicationUrl') }"></KnValidationMessages>
                        </div>

                        <div :hidden="documentHidden">
                            <div class="p-field p-mb-5">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <InputText id="selectedDocument" type="text" v-model.trim="v$.menuNode.document.$model" @blur="onDataChange(v$.menuNode.document)" class="p-inputtext p-component kn-material-input" />
                                        <InputText :hidden="true" id="objId" type="text" v-model.trim="v$.menuNode.objId.$model" @blur="onDataChange(v$.menuNode.objId)" class="p-inputtext p-component kn-material-input" />
                                        <Button icon="pi pi-search" class="p-button" @click="openRelatedDocumentModal()" />
                                        <label for="objId">{{ $t('managers.menuManagement.form.document') }} *</label>
                                    </span>
                                </div>
                                <KnValidationMessages :vComp="v$.menuNode.document" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.document') }"></KnValidationMessages>
                            </div>

                            <div class="p-field p-mb-5">
                                <div class="p-inputgroup">
                                    <span class="p-float-label">
                                        <InputText id="objParameters" type="text" v-model.trim="v$.menuNode.objParameters.$model" @blur="onDataChange(v$.menuNode.objParameters)" class="p-inputtext p-component kn-material-input" />
                                        <label for="objParameters">{{ $t('managers.menuManagement.form.objParameters') }}</label>
                                    </span>
                                </div>
                                <KnValidationMessages :vComp="v$.menuNode.objParameters" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.objParameters') }"></KnValidationMessages>
                            </div>

                            <Dialog :header="$t('managers.menuManagement.selectDocument')" v-model:visible="displayModal" :style="{ width: '50vw' }" :modal="true">
                                <RelatedDocumentList :loading="loading" @selectedDocument="onDocumentSelect" data-test="related-documents-list"></RelatedDocumentList>
                            </Dialog>
                        </div>

                        <div class="p-field p-mb-5" :hidden="functionalityHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown
                                        id="functionality"
                                        v-model="v$.menuNode.functionality.$model"
                                        :options="menuNodeContentFunctionalies"
                                        @change="onFunctionalityTypeChange(v$.menuNode.functionality)"
                                        optionLabel="name"
                                        optionValue="value"
                                        class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input"
                                    />
                                    <label for="functionality">{{ $t('managers.menuManagement.form.functionality') }}*</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="v$.menuNode.functionality" :additionalTranslateParams="{ fieldName: $t('managers.menuManagement.form.functionality') }"></KnValidationMessages>
                        </div>

                        <div class="p-field p-mb-5" :hidden="workspaceInitialHidden">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <Dropdown id="initialPath" v-model="v$.menuNode.initialPath.$model" :options="workspaceOptions" optionLabel="name" optionValue="value" class="p-dropdown p-component p-inputwrapper p-inputwrapper-filled kn-material-input" />
                                    <label for="initialPath">{{ $t('managers.menuManagement.form.initialPath') }} *</label>
                                </span>
                            </div>
                        </div>

                        <div class="p-field p-mb-5" :hidden="documentTreeHidden">
                            <p>Open document browser on {{ v$.menuNode.initialPath.$model }}</p>
                            <DocumentBrowserTree :selected="v$.menuNode.initialPath.$model" @selectedDocumentNode="onSelectedDocumentNode" :loading="loading"></DocumentBrowserTree>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
        <div class="p-col-12">
            <RolesCard :hidden="hideForm" :rolesList="roles" :parentNodeRoles="parentNodeRoles" :selected="selectedMenuNode.roles" @changed="setSelectedRoles($event)"></RolesCard>
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
export default defineComponent({
    name: 'profile-attributes-detail',
    components: { Dropdown, DocumentBrowserTree, RelatedDocumentList, KnValidationMessages, Dialog, FontAwesomePicker, RolesCard },
    props: { roles: { type: Array }, selectedMenuNode: { type: Object, required: true }, selectedRoles: { type: Array }, staticPagesList: { type: Array }, menuNodes: { type: Array }, parentNodeRoles: { type: Array } },
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
    emits: ['refreshRecordSet', 'closesForm', 'dataChanged'],
    data() {
        return {
            v$: useValidate() as any,
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            menuNode: {} as iMenuNode,
            loading: false as Boolean,
            hideForm: false as Boolean,
            documentHidden: true as Boolean,
            staticPageHidden: true as Boolean,
            externalAppHidden: true as Boolean,
            functionalityHidden: true as Boolean,
            workspaceInitialHidden: true as Boolean,
            documentTreeHidden: true as Boolean,
            dirty: false as Boolean,
            displayModal: false as Boolean,
            chooseIconModalShown: false as Boolean,
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
                this.$store.commit('setError', { title: this.$t('managers.menuManagement.info.errorTitle'), msg: this.$t('managers.menuManagement.info.duplicateErrorMessage') })
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
                    this.$store.commit('setError', { title: this.$t('managers.menuManagement.info.errorTitle'), msg: this.$t('managers.menuManagement.info.errorMessage') })
                } else {
                    this.$store.commit('setInfo', { title: this.$t('managers.menuManagement.info.saveTitle'), msg: this.$t('managers.menuManagement.info.saveMessage') })
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
