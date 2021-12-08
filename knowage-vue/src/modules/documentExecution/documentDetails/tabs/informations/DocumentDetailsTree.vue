<template>
    <div>
        <Tree class="documents-tree" :value="nodes" :expandedKeys="expandedKeys" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)">
            <template #default="slotProps">
                <i class="p-mr-2" :class="slotProps.node.customIcon"></i>
                <Checkbox v-if="slotProps.node.selectable" name="folders" v-model="selectedFolders" :value="slotProps.node.path" @change="emitSelectedFolders" />
                <b>{{ slotProps.node.label }}</b>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Checkbox from 'primevue/checkbox'
import documentBrowserTreeDescriptor from '@/modules/documentBrowser/documentBrowserHome/DocumentBrowserTreeDescriptor.json'
import Tree from 'primevue/tree'
export default defineComponent({
    name: 'scheduler-document-accordion-tree',
    components: { Checkbox, Tree },
    props: { propFunctionalities: { type: Array }, propSelectedFolders: { type: Array } },
    data() {
        return {
            documentBrowserTreeDescriptor,
            functionalities: [] as any[],
            selectedFolders: [] as any[],
            nodes: [] as any[],
            expandedKeys: {}
        }
    },
    watch: {
        propFunctionalities() {
            this.loadFunctionalities()
            this.createNodeTree()
            this.expandAll()
        },
        propSelectedFolders() {
            this.loadSelectedFolders()
        }
    },
    created() {
        this.loadFunctionalities()
        this.createNodeTree()
        this.expandAll()
        this.loadSelectedFolders()
    },
    methods: {
        loadFunctionalities() {
            this.functionalities = this.propFunctionalities as any[]
        },
        loadSelectedFolders() {
            this.selectedFolders = this.propSelectedFolders ? this.propSelectedFolders : []
        },
        createNodeTree() {
            this.nodes = []
            const foldersWithMissingParent = [] as any[]
            this.functionalities.forEach((folder: any) => {
                const node = {
                    key: folder.name,
                    id: folder.id,
                    parentId: folder.parentId,
                    label: folder.name,
                    children: [] as any[],
                    data: folder,
                    style: this.documentBrowserTreeDescriptor.node.style,
                    path: folder.path,
                    customIcon: folder.childs ? 'pi pi-folder-open' : 'pi pi-folder',
                    selectable: folder.codType === 'USER_FUNCT' || folder.parentId
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
                let tempFolder = null as any
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
            for (let node of this.nodes) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node: any) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (let child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        setOpenFolderIcon(node: any) {
            node.customIcon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: any) {
            node.customIcon = 'pi pi-folder'
        },
        emitSelectedFolders() {
            this.$emit('selected', this.selectedFolders)
        }
    }
})
</script>

<style lang="scss" scoped>
.documents-tree {
    border: none;
}
</style>
