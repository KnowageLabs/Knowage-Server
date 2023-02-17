<template>
    <Tree
        id="folders-tree"
        v-model:selectionKeys="selectedFolderKey"
        class="kn-tree kn-column-tree kn-flex p-p-0"
        scroll-height="calc(100vh - 127px)"
        maximizable
        :value="nodes"
        selection-mode="single"
        :filter="true"
        filter-mode="lenient"
        :expanded-keys="expandedKeys"
        @node-select="setSelectedFolder"
        @node-expand="setOpenFolderIcon($event)"
        @node-collapse="setClosedFolderIcon($event)"
    >
        <template #default="slotProps">
            <span>{{ getTranslatedLabel(slotProps.node.label) }}</span>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode } from '../DocumentBrowser'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'document-browser-tree',
    components: { Tree },
    props: { propFolders: { type: Array }, selectedBreadcrumb: { type: Object }, selectedFolderProp: { type: Object } },
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
        },
        selectedFolderProp() {
            this.selectedFolder = this.selectedFolderProp
            if (!this.selectedFolder) return
            this.selectedFolderKey = {}
            this.selectedFolderKey[this.selectedFolder.key] = true
            let temp = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                temp = this.findNode(this.nodes[i], this.selectedFolder.id, 'id')
                if (temp) {
                    this.selectedFolderKey[temp.key] = true
                    this.expandedKeys[temp.key] = true
                    const tempPath = this.selectedFolder.path?.substring(1)?.split('/')
                    tempPath?.forEach((el: string) => {
                        const tempFolderByCode = this.findNode(this.nodes[i], el, 'code')
                        if (tempFolderByCode) this.expandedKeys[tempFolderByCode.key] = true
                    })
                    break
                }
            }
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
                id: -1,
                prog: 0,
                parentId: null,
                label: 'Personal_Folders',
                children: [] as iNode[],
                data: {
                    id: -1,
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
                const node = { key: folder.name, icon: 'pi pi-folder', id: folder.id, prog: folder.prog, parentId: folder.parentId, label: folder.name, children: [] as iNode[], data: folder }
                node.children = foldersWithMissingParent.filter((folder: iNode) => node.id === folder.parentId && folder.data.codType !== 'LOW_FUNCT')
                this.attachFolderToTree(node, foldersWithMissingParent, personalFolder)
            })
            this.sortNodesAndChildren(this.nodes)
        },
        sortNodesAndChildren(nodes: iNode[]) {
            nodes.sort((a: iNode, b: iNode) => a.prog - b.prog)
            nodes.forEach((node: iNode) => {
                if (node.children) this.sortNodesAndChildren(node.children)
            })
        },
        attachFolderToTree(folder: iNode, foldersWithMissingParent: iNode[], personalFolder: iNode) {
            if (folder.parentId && folder.parentId !== -1) {
                let parentFolder = null as iNode | null
                for (let i = 0; i < foldersWithMissingParent.length; i++) {
                    if (folder.parentId === foldersWithMissingParent[i].id && foldersWithMissingParent[i].data.codType !== 'USER_FUNCT') {
                        folder.data.parentFolder = foldersWithMissingParent[i]
                        foldersWithMissingParent[i].children?.push(folder)
                        break
                    }
                }
                for (let i = 0; i < this.nodes.length; i++) {
                    parentFolder = this.findParentFolder(folder, this.nodes[i])

                    if (parentFolder && parentFolder.data.codType !== 'USER_FUNCT') {
                        folder.data.parentFolder = parentFolder
                        parentFolder.children?.push(folder)
                        break
                    }
                }
                if (!parentFolder && folder.data.codType !== 'USER_FUNCT') {
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
            if (folderToAdd.data.codType === 'USER_FUNCT') {
                return null
            }
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
        },
        findNode(node: iNode, value: number | string, property: string) {
            if (node.data[property] === value) {
                return node
            } else if (node.children != null) {
                let result = null as any
                for (let i = 0; result == null && i < node.children.length; i++) {
                    result = this.findNode(node.children[i], value, property)
                }
                return result
            }
            return null
        },
        getTranslatedLabel(label: string) {
            return (this as any).$internationalization(label)
        }
    }
})
</script>

<style lang="scss" scoped>
#folders-tree {
    border: none;
}
</style>
