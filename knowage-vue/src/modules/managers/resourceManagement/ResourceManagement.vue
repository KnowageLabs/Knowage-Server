<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.resourceManagement.title') }}
                    </template>
                    <template #end>
                        <Button icon="fas fa-sync-alt" class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0" @click="loadPage(showHint, formVisible)"/>
                        <Button icon="fas fa-folder-plus" class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0" @click="openCreateFolderDialog"
                    /></template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <ResourceManagementMetadataDialog v-model:visibility="displayMetadataDialog" v-model:id="metadataKey"></ResourceManagementMetadataDialog>
                <ResourceManagementCreateFolderDialog v-model:visibility="folderCreation" @createFolder="createFolder" v-bind:path="selectedFolder ? selectedFolder.relativePath : ''" />

                <Tree id="folders-tree" :value="nodes" selectionMode="single" :expandedKeys="expandedKeys" :filter="true" filterMode="lenient" data-test="functionality-tree" class="kn-tree kn-flex p-flex-column foldersTree" @node-select="showForm($event)" v-model:selectionKeys="selectedKeys">
                    <template #default="slotProps">
                        <div class="p-d-flex p-flex-row p-ai-center p-jc-between" @mouseover="buttonsVisible[slotProps.node.key] = true" @mouseleave="buttonsVisible[slotProps.node.key] = false" :data-test="'tree-item-' + slotProps.node.key">
                            <span v-if="!slotProps.node.edit" class="kn-truncated" @dblclick="toggleInput(slotProps.node)">
                                {{ slotProps.node.label }}
                            </span>
                            <InputText class="kn-material-input fileNameInputText" type="text" v-if="slotProps.node.edit" v-model="slotProps.node.label" maxlength="50" @blur="toggleInput(slotProps.node)" @keyup.enter="toggleInput(slotProps.node)" />

                            <div>
                                <Button v-if="slotProps.node.modelFolder" icon="fas fa-table" v-tooltip.top="$t('managers.resourceManagement.openMetadata')" :class="getButtonClass(slotProps.node)" @click="openMetadataDialog(slotProps.node)" :data-test="'move-up-button-' + slotProps.node.key" />
                                <Button icon="fa fa-download " v-tooltip.top="$t('common.download')" :class="getButtonClass(slotProps.node)" @click="downloadDirect(slotProps.node)" :data-test="'move-down-button-' + slotProps.node.key" />
                                <Button icon="far fa-trash-alt" v-tooltip.top="$t('common.delete')" :class="getButtonClass(slotProps.node)" @click="showDeleteDialog(slotProps.node)" :data-test="'delete-button-' + slotProps.node.key" v-if="slotProps.node.level > 0" />
                            </div>
                        </div>
                    </template>
                </Tree>
            </div>
            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <KnHint :title="'managers.resourceManagement.title'" :hint="'managers.resourceManagement.hint'" v-if="showHint"></KnHint>
                <ResourceManagementDetail v-if="formVisible" :folder="selectedFolder" :parentKey="folderParentKey" @touched="touched = true" @close="onClose" @inserted="loadPage($event)" @folderCreated="loadPage" @closed="switchToHint()" @fileUploaded="loadPage(false, true)" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import descriptor from './ResourceManagementDescriptor.json'
    import Tree from 'primevue/tree'
    import { IFolderTemplate } from '@/modules/managers/resourceManagement/ResourceManagement'
    import { downloadDirectFromResponseWithCustomName } from '@/helpers/commons/fileHelper'
    import ResourceManagementMetadataDialog from '@/modules/managers/resourceManagement/ResourceManagementMetadataDialog.vue'
    import ResourceManagementDetail from './ResourceManagementDetail.vue'
    import KnHint from '@/components/UI/KnHint.vue'
    import ResourceManagementCreateFolderDialog from './ResourceManagementCreateFolderDialog.vue'

    export default defineComponent({
        name: 'resource-management',
        components: { KnHint, ResourceManagementMetadataDialog, ResourceManagementCreateFolderDialog, ResourceManagementDetail, Tree },
        data() {
            return {
                descriptor,
                displayMetadataDialog: false,
                loading: false,
                nodes: [] as IFolderTemplate[],
                expandedKeys: {},
                selectedKeys: null,
                metadataKey: null,
                dirty: false,
                buttonsVisible: [],
                showHint: true,
                touched: false,
                selectedFolder: {} as IFolderTemplate,
                folderCreation: false,
                formVisible: false
            }
        },
        async created() {
            this.loadPage()
        },
        methods: {
            createFolder(folderName: string) {
                if (folderName && this.selectedFolder) {
                    let obj = {} as JSON
                    obj['key'] = '' + this.selectedFolder.key
                    obj['folderName'] = folderName
                    this.$http
                        .post(import.meta.env.VUE_APP_API_PATH + `2.0/resources/folders`, obj, {
                            responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

                            headers: {
                                'Content-Type': 'application/json'
                            }
                        })
                        .then(() => {
                            this.$emit('folderCreated', true)
                        })
                        .catch((error) => {
                            this.$store.commit('setError', {
                                title: this.$t('common.error.saving'),
                                msg: this.$t(error)
                            })
                        })
                        .finally(() => {
                            this.loading = false
                            this.openCreateFolderDialog()
                            this.loadPage(this.showHint, this.formVisible)
                        })
                }
            },
            getCurrentFolderPath() {
                return this.selectedFolder ? this.selectedFolder.relativePath : undefined
            },
            getCurrentFolderKey() {
                return this.selectedFolder ? '' + this.selectedFolder.key : undefined
            },
            getButtonClass(node) {
                let visibility = ' kn-hide'
                if (this.buttonsVisible[node.key] && !node.edit) visibility = ''
                return 'p-button-text p-button-sm p-button-rounded p-button-plain p-p-0' + visibility
            },
            openCreateFolderDialog() {
                this.folderCreation = !this.folderCreation
            },
            toggleInput(node) {
                if (node.level > 0) {
                    if (node.edit && node.label !== node.edit) {
                        if (this.selectedFolder) {
                            let obj = {} as JSON
                            obj['key'] = this.selectedFolder.key
                            obj['folderName'] = node.label
                            this.loading = true
                            this.$http
                                .post(import.meta.env.VUE_APP_API_PATH + `2.0/resources/folders/update`, obj, {
                                    responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

                                    headers: {
                                        'Content-Type': 'application/json'
                                    }
                                })
                                .then(() => {
                                    delete node.edit
                                    this.$store.commit('setInfo', {
                                        title: this.$t('managers.resoruceManagement.renameFolder'),
                                        msg: this.$t('managers.resoruceManagement.folderRenamedSuccessfully')
                                    })
                                })
                                .catch((error) => {
                                    this.$store.commit('setError', {
                                        title: this.$t('managers.resoruceManagement.renameFolder'),
                                        msg: this.$t(error)
                                    })
                                })
                                .finally(() => {
                                    this.loading = false
                                })
                        }
                    } else node.edit = node.label
                }
            },
            addIcon(nodes) {
                for (var idx in nodes) {
                    let node = nodes[idx]
                    node.icon = 'far fa-folder'
                    if (node.children && node.children.length > 0) {
                        this.addIcon(node.children)
                    }
                }
            },
            loadPage(showHint?, formVisible?): void {
                this.loading = true
                this.showHint = showHint != undefined ? showHint : true
                this.formVisible = formVisible != undefined ? formVisible : false
                this.$http
                    .get(import.meta.env.VUE_APP_API_PATH + `2.0/resources/folders`)
                    .then((response: AxiosResponse<any>) => {
                        let root = response.data.root[0]
                        root.label = 'HOME'
                        root.icon = 'pi pi-home'
                        root.disabled = true
                        this.addIcon(root.children)
                        this.nodes = response.data.root
                        this.loading = false
                    })
                    .finally(() => (this.loading = false))
            },
            openMetadataDialog(node): void {
                this.metadataKey = node.key
                this.displayMetadataDialog = !this.displayMetadataDialog
            },
            deleteFolder(node) {
                this.loading = true
                this.$http
                    .delete(import.meta.env.VUE_APP_API_PATH + `2.0/resources/folders`, {
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        data: {
                            key: node.key
                        }
                    })
                    .then(() => {
                        this.loadPage()
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    })
                    .catch(() => {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteFailed')
                        })
                    })
                    .finally(() => (this.loading = false))
            },
            downloadDirect(node) {
                this.loading = true
                let obj = {} as JSON
                obj['key'] = node.key
                this.$http
                    .post(import.meta.env.VUE_APP_API_PATH + `2.0/resources/folders/download`, obj, {
                        responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

                        headers: {
                            'Content-Type': 'application/json',
                            Accept: 'application/zip; charset=utf-8'
                        }
                    })
                    .then((response: AxiosResponse<any>) => {
                        downloadDirectFromResponseWithCustomName(response, this.selectedFolder.label + '.zip')
                    })
                    .finally(() => (this.loading = false))
            },
            setDirty(): void {
                this.dirty = true
            },
            showForm(functionality: IFolderTemplate) {
                /*             this.functionalityParentId = parentId */
                if (!this.touched) {
                    this.setSelected(functionality)
                } else {
                    this.$confirm.require({
                        message: this.$t('common.toast.unsavedChangesMessage'),
                        header: this.$t('common.toast.unsavedChangesHeader'),
                        icon: 'pi pi-exclamation-triangle',
                        accept: () => {
                            this.touched = false
                            this.setSelected(functionality)
                        }
                    })
                }
            },
            setSelected(functionality: IFolderTemplate) {
                this.selectedFolder = functionality
                this.formVisible = true
                this.showHint = false
            },
            showDeleteDialog(node) {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteConfirmTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteFolder(node)
                })
            },
            switchToHint() {
                this.formVisible = false
                this.showHint = true

                this.expandedKeys = {}
                this.selectedKeys = null
                this.metadataKey = null
                this.dirty = false

                this.touched = false
                this.selectedFolder = {} as IFolderTemplate
            }
        }
    })
</script>

<style lang="scss">
    .knTreeLabel {
        border: none;
    }
    .foldersTree {
        border-radius: 0;
    }
    .rightFolderIconsBar {
        align-items: center;
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
        justify-content: flex-end;
        flex: 1;
    }
    .treeCustomElement {
        align-items: center;
        display: flex;
        flex-direction: row;
        flex-wrap: nowrap;
    }
    .rightFolderIcon {
        margin-right: 0.5rem;
    }
    .p-tree .p-tree-container .p-treenode {
        padding: 0;
        margin: 0;
    }
    .p-treenode-content {
        border-radius: 0 !important;
    }
    .p-treenode-label {
        width: 100%;
        height: 100%;
    }
    .p-tree-toggler p-link {
        margin: 0px;
    }
    .p-treenode-icon {
        margin: 0px;
    }
</style>
