<template>
    <Tree id="folders-tree" :value="nodes" selectionMode="single" v-model:selectionKeys="selectedFolderKey" @node-select="setSelectedFolder($event)" @node-unselect="removeSelectedFolder" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)"> </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IFolder } from '../Workspace'
import Tree from 'primevue/tree'
import workspaceDocumentTreeDescriptor from './WorkspaceDocumentTreeDescriptor.json'

export default defineComponent({
    name: 'workspace-document-tree',
    components: { Tree },
    props: { propFolders: { type: Array }, mode: { type: String } },
    emits: ['folderSelected'],
    data() {
        return {
            workspaceDocumentTreeDescriptor,
            folders: [] as IFolder[],
            nodes: [] as any[],
            selectedFolderKey: {},
            selectedFolder: null as any
        }
    },
    watch: {
        propFolders() {
            this.loadTree()
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
            console.log('DOCUMENT TREE LOADED FODLERS: ', this.folders)
        },
        createNodeTree() {
            console.log('   createNodeTree() {')
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
                    data: { name: folder.name, hasDocuments: false }
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
                        foldersWithMissingParent[i].children?.push(folder)
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
        }
    }
})
</script>

<style lang="scss" scoped>
#folders-tree {
    border: none;
}
</style>
