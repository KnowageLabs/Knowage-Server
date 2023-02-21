<template>
    <Tree id="folders-tree" v-model:selectionKeys="selectedFolderKey" :value="nodes" selection-mode="single" @node-select="setSelectedFolder($event)" @node-unselect="removeSelectedFolder" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)">
        <template #default="slotProps">
            <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonsVisible[slotProps.node.id] = true" @mouseleave="buttonsVisible[slotProps.node.id] = false">
                <span>{{ slotProps.node.label }}</span>
                <div v-show="mode === 'select' && buttonsVisible[slotProps.node.id]" class="p-ml-2">
                    <Button icon="fa fa-plus" class="p-button-link p-button-sm p-p-0" @click.stop="createFolder(slotProps.node)" />
                    <Button icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" @click.stop="deleteFolderConfirm(slotProps.node)" />
                </div>
            </div>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IFolder } from '../Workspace'
import Tree from 'primevue/tree'
import workspaceDocumentTreeDescriptor from './WorkspaceDocumentTreeDescriptor.json'

export default defineComponent({
    name: 'workspace-document-tree',
    components: { Tree },
    props: { propFolders: { type: Array }, mode: { type: String }, selectedBreadcrumb: { type: Object } },
    emits: ['folderSelected', 'delete', 'createFolder'],
    data() {
        return {
            workspaceDocumentTreeDescriptor,
            folders: [] as IFolder[],
            nodes: [] as any[],
            selectedFolderKey: {},
            selectedFolder: null as any,
            buttonsVisible: []
        }
    },
    watch: {
        propFolders() {
            this.loadTree()
        },
        selectedBreadcrumb() {
            this.onBreadcrumbSelected()
        }
    },
    created() {
        this.loadTree()
    },
    methods: {
        loadTree() {
            this.loadFolders()
            this.createNodeTree()
        },
        loadFolders() {
            this.folders = this.propFolders as IFolder[]
        },
        createNodeTree() {
            this.nodes = [] as any[]
            const foldersWithMissingParent = [] as IFolder[]
            this.folders?.forEach((folder: IFolder) => {
                const node = {
                    key: folder.name,
                    icon: 'pi pi-folder',
                    id: folder.functId,
                    parentId: folder.parentFunct,
                    label: folder.name,
                    path: folder.path,
                    prog: folder.prog,
                    children: [] as IFolder[],
                    data: { name: folder.name, hasDocuments: false },
                    style: this.workspaceDocumentTreeDescriptor.node.style
                }
                node.children = foldersWithMissingParent.filter((folder: any) => node.id === folder.parentId)
                this.attachFolderToTree(node, foldersWithMissingParent)
            })
        },
        attachFolderToTree(folder: any, foldersWithMissingParent: any[]) {
            if (folder.parentId) {
                let parentFolder = null as any
                for (let i = 0; i < foldersWithMissingParent.length; i++) {
                    if (folder.parentId === foldersWithMissingParent[i].id) {
                        folder.data.parentFolder = foldersWithMissingParent[i]
                        foldersWithMissingParent[i].children?.push(folder)
                        break
                    }
                }
                for (let i = 0; i < this.nodes.length; i++) {
                    parentFolder = this.findParentFolder(folder, this.nodes[i])
                    if (parentFolder) {
                        folder.data.parentFolder = parentFolder
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
        findParentFolder(folderToAdd: any, folderToSearch: any) {
            if (folderToAdd.parentId === folderToSearch.id) {
                return folderToSearch
            } else {
                let tempFolder = null as any | null
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
        setOpenFolderIcon(node: any) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: any) {
            node.icon = 'pi pi-folder'
        },
        setSelectedFolder(folder: any) {
            this.selectedFolder = folder
            this.$emit('folderSelected', this.selectedFolder)
        },
        removeSelectedFolder() {
            this.selectedFolder = null
            this.$emit('folderSelected', this.selectedFolder)
        },
        createFolder(folder: any) {
            this.$emit('createFolder', folder)
        },
        deleteFolderConfirm(folder: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('delete', folder)
            })
        },
        onBreadcrumbSelected() {
            this.selectedFolder = this.selectedBreadcrumb?.node
            this.selectedFolderKey = {}
            this.selectedFolderKey[this.selectedFolder.key] = true
        }
    }
})
</script>

<style lang="scss" scoped>
#folders-tree {
    border: none;
}
</style>
