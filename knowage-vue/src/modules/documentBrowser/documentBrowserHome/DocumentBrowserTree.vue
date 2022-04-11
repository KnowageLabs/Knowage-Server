<template>
    <Tree
        id="folders-tree"
        class="kn-tree kn-column-tree kn-flex p-p-0"
        scrollHeight="calc(100vh - 127px)"
        maximizable
        :value="nodes"
        selectionMode="single"
        v-model:selectionKeys="selectedFolderKey"
        :filter="true"
        filterMode="lenient"
        :expandedKeys="expandedKeys"
        @node-select="setSelectedFolder"
        @node-expand="setOpenFolderIcon($event)"
        @node-collapse="setClosedFolderIcon($event)"
    ></Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode } from '../DocumentBrowser'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'document-browser-tree',
    components: { Tree },
    props: { propFolders: { type: Array }, selectedBreadcrumb: { type: Object } },
    emits: ['folderSelected'],
    data() {
        return {
            folders: [] as any[],
            nodes: [] as iNode[],
            selectedFolderKey: {},
            expandedKeys: {},
            selectedFolder: null as any
        }
    },
    watch: {
        propFolders() {
            this.loadFolders()
            this.createNodeTree()
        },
        selectedBreadcrumb() {
            this.onBreadcrumbSelected()
        }
    },
    created() {
        this.loadFolders()
        this.createNodeTree()
    },
    methods: {
        loadFolders() {
            this.folders = this.propFolders as any[]
            this.loadSelectedFolderFromLocalStorage()
        },
        createNodeTree() {
            const personalFolder = {
                key: 'Personal_Folders',
                icon: 'pi pi-folder',
                id: 1,
                parentId: null,
                label: 'Personal_Folders',
                children: [] as iNode[],
                data: {
                    id: 1,
                    codType: 'LOW_FUNCT',
                    code: 'Personal_Folders',
                    createRoles: [],
                    description: 'Personal Folders',
                    name: 'Personal_Folders',
                    parentId: null,
                    subfolders: [],
                    path: '/Personal-Folders'
                }
            }
            this.nodes = [personalFolder]
            const foldersWithMissingParent = [] as iNode[]
            this.folders.forEach((folder: any) => {
                const node = { key: folder.name, icon: 'pi pi-folder', id: folder.id, parentId: folder.parentId, label: folder.name, children: [] as iNode[], data: folder }
                node.children = foldersWithMissingParent.filter((folder: iNode) => node.id === folder.parentId)
                this.attachFolderToTree(node, foldersWithMissingParent, personalFolder)
            })
        },
        attachFolderToTree(folder: iNode, foldersWithMissingParent: iNode[], personalFolder: iNode) {
            if (folder.parentId) {
                let parentFolder = null as iNode | null
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
            } else if (folder.data.codType === 'USER_FUNCT') {
                folder.data.parentFolder = personalFolder
                folder.data.parentId = personalFolder.id
                personalFolder.children?.push(folder)
            } else {
                this.nodes.push(folder)
            }
        },
        findParentFolder(folderToAdd: iNode, folderToSearch: iNode) {
            if (folderToAdd.data.codType === 'USER_FUNCT') return null
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
        setSelectedFolder(node: iNode) {
            this.selectedFolder = node.data
            localStorage.setItem('documentSelectedFolderId', JSON.stringify(this.selectedFolder.id))
            this.$emit('folderSelected', this.selectedFolder)
        },
        setOpenFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder'
        },
        onBreadcrumbSelected() {
            this.selectedFolder = this.selectedBreadcrumb?.node
            this.selectedFolderKey = {}
            this.selectedFolderKey[this.selectedFolder.key] = true
        },
        loadSelectedFolderFromLocalStorage() {
            const folderId = localStorage.getItem('documentSelectedFolderId')
            if (folderId) {
                const index = this.folders.findIndex((el: any) => el.id === JSON.parse(folderId))
                if (index !== -1) {
                    this.selectedFolder = this.folders[index]
                    this.selectedFolderKey[this.folders[index].name] = true
                    this.expandedKeys[this.folders[index].name] = true
                    this.$emit('folderSelected', this.selectedFolder)
                }
            }
        }
    }
})
</script>

<style lang="scss" scoped>
#folders-tree {
    border: none;
}
</style>
