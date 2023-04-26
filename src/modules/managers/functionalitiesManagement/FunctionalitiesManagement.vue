<template>
    <div class="kn-page">
        <div class="p-grid p-m-0">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.functionalitiesManagement.title') }}
                    </template>
                    <template #end>
                        <FabButton v-if="selectedFunctionality" icon="fas fa-plus" data-test="new-button" @click="showForm(null, selectedFunctionality.id)" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />

                <Tree
                    id="document-tree"
                    scroll-height="calc(100vh - 91px)"
                    maximizable
                    :value="nodes"
                    selection-mode="single"
                    :expanded-keys="expandedKeys"
                    :filter="true"
                    filter-mode="lenient"
                    data-test="functionality-tree"
                    class="kn-tree kn-column-tree kn-flex p-p-0"
                    @node-select="showForm($event.data, $event.data.parentId)"
                >
                    <template #default="slotProps">
                        <div class="p-d-flex p-flex-row p-ai-center" :data-test="'tree-item-' + slotProps.node.id" @mouseover="buttonsVisible[slotProps.node.id] = true" @mouseleave="buttonsVisible[slotProps.node.id] = false">
                            <span>{{ slotProps.node.label }}</span>
                            <div v-show="buttonsVisible[slotProps.node.id]" class="p-ml-2">
                                <Button v-if="canBeMovedUp(slotProps.node.data)" v-tooltip.top="$t('managers.functionalitiesManagement.moveUp')" icon="fa fa-arrow-up" class="p-button-link p-button-sm p-p-0" :data-test="'move-up-button-' + slotProps.node.id" @click.stop="moveUp(slotProps.node.id)" />
                                <Button
                                    v-if="canBeMovedDown(slotProps.node.data)"
                                    v-tooltip.top="$t('managers.functionalitiesManagement.moveDown ')"
                                    icon="fa fa-arrow-down"
                                    class="p-button-link p-button-sm p-p-0"
                                    :data-test="'move-down-button-' + slotProps.node.id"
                                    @click.stop="moveDown(slotProps.node.id)"
                                />
                                <Button v-if="canBeDeleted(slotProps.node)" v-tooltip.top="$t('common.delete')" icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" :data-test="'delete-button-' + slotProps.node.id" @click.stop="deleteFunctionalityConfirm(slotProps.node.id)" />
                            </div>
                        </div>
                    </template>
                </Tree>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-height-full-vertical">
                <KnHint v-if="showHint" :title="'managers.functionalitiesManagement.title'" :hint="'managers.functionalitiesManagement.hint'" data-test="functionality-hint"></KnHint>
                <FunctionalitiesManagementDetail v-if="formVisible" :functionality="selectedFunctionality" :parent-id="functionalityParentId" :roles-short="rolesShort" @touched="touched = true" @close="onClose" @inserted="loadPage($event)" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunctionality, iNode } from './FunctionalitiesManagement'
import FunctionalitiesManagementDetail from './detailTabView/FunctionalitiesManagementDetail.vue'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import functionalitiesManagementDescriptor from './FunctionalitiesManagementDescriptor.json'
import KnHint from '@/components/UI/KnHint.vue'
import Tree from 'primevue/tree'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'functionalities-management',
    components: {
        FunctionalitiesManagementDetail,
        FabButton,
        KnHint,
        Tree
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            functionalitiesManagementDescriptor,
            functionalities: [] as iFunctionality[],
            rolesShort: [] as { id: number; name: 'string' }[],
            nodes: [] as iNode[],
            selectedFunctionality: null as iFunctionality | null,
            functionalityParentId: null as number | null,
            expandedKeys: {},
            showHint: true,
            touched: false,
            loading: false,
            buttonsVisible: [],
            formVisible: false
        }
    },
    async created() {
        await this.loadPage(null)
    },
    methods: {
        async loadFunctionalities() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/functionalities/').then((response: AxiosResponse<any>) => (this.functionalities = response.data))
        },
        async loadRolesShort() {
            this.rolesShort = []
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/roles/short/').then((response: AxiosResponse<any>) => (this.rolesShort = response.data))
        },
        createNodeTree() {
            this.nodes = []
            const foldersWithMissingParent = [] as iNode[]
            this.functionalities.forEach((functionality: iFunctionality) => {
                if (functionality.codType !== 'USER_FUNCT') {
                    const node = {
                        key: functionality.id,
                        id: functionality.id,
                        parentId: functionality.parentId,
                        label: functionality.name,
                        children: [] as iNode[],
                        data: functionality,
                        style: this.functionalitiesManagementDescriptor.node.style
                    }
                    node.children = foldersWithMissingParent.filter((folder: iNode) => node.id === folder.parentId)

                    this.attachFolderToTree(node, foldersWithMissingParent)
                }
            })
        },
        attachFolderToTree(folder: iNode, foldersWithMissingParent: iNode[]) {
            if (folder.parentId) {
                let parentFolder = null as iNode | null

                for (let i = 0; i < foldersWithMissingParent.length; i++) {
                    if (folder.parentId === foldersWithMissingParent[i].id) {
                        foldersWithMissingParent[i].children.push(folder)
                        break
                    }
                }

                for (let i = 0; i < this.nodes.length; i++) {
                    parentFolder = this.findParentFolder(folder, this.nodes[i])
                    if (parentFolder) {
                        parentFolder.children?.push(folder)
                        break
                    }
                }

                if (!parentFolder) {
                    foldersWithMissingParent.push(folder)
                }
            } else {
                this.nodes.push(folder)
            }
        },
        findParentFolder(folderToAdd: iNode, folderToSearch: iNode) {
            if (folderToAdd.parentId === folderToSearch.id) {
                return folderToSearch
            } else {
                let tempFolder = null as iNode | null
                if (folderToSearch.children) {
                    for (let i = 0; i < folderToSearch.children.length; i++) {
                        tempFolder = this.findParentFolder(folderToAdd, folderToSearch.children[i])
                        if (tempFolder) {
                            break
                        }
                    }
                }
                return tempFolder
            }
        },
        expandAll() {
            for (const node of this.nodes) {
                this.expandNode(node)
            }

            this.expandedKeys = { ...this.expandedKeys }
        },

        expandNode(node: iNode) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true

                for (const child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        showForm(functionality: iFunctionality, parentId: number) {
            this.showHint = false
            this.functionalityParentId = parentId
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
        onClose() {
            this.touched = false
            this.formVisible = false
            this.showHint = true
        },
        setSelected(functionality: iFunctionality) {
            this.selectedFunctionality = functionality
            this.formVisible = true
        },
        canBeMovedUp(functionality: iFunctionality) {
            return functionality.prog !== 1
        },
        moveUp(functionalityId: number) {
            this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/moveUp/${functionalityId}`).then(() => this.loadPage(null))
        },
        canBeMovedDown(functionality: iFunctionality) {
            let canBeMoved = false
            this.functionalities.forEach((currentFunctionality) => {
                if (functionality.parentId === currentFunctionality.parentId && functionality.prog < currentFunctionality.prog) {
                    canBeMoved = true
                }
            })

            return canBeMoved
        },
        moveDown(functionalityId: number) {
            this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/moveDown/${functionalityId}`).then(() => this.loadPage(null))
        },
        canBeDeleted(functionality: iFunctionality) {
            return functionality.parentId && functionality.codType !== 'LOW_FUNCT'
        },
        deleteFunctionalityConfirm(functionalityId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.deleteFunctionality(functionalityId)
                }
            })
        },
        async deleteFunctionality(functionalityId: number) {
            await this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/${functionalityId}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.selectedFunctionality = null
                    this.formVisible = false
                    this.showHint = true
                    this.loadPage(null)
                })
                .catch((error) => {
                    this.store.setError({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: error.message
                    })
                })
        },
        async loadPage(functionalityId: any) {
            this.loading = true
            await this.loadFunctionalities()
            await this.loadRolesShort()
            this.createNodeTree()
            this.expandAll()
            const id = functionalityId ? functionalityId : this.selectedFunctionality?.id
            this.selectedFunctionality = this.functionalities.find((functionality) => functionality.id === id) as any
            this.touched = false
            this.loading = false
        }
    }
})
</script>
