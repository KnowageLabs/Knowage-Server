<template>
    <Tree id="folders-tree" :value="nodes" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)">
        <template #default="slotProps">
            <Checkbox v-model="selectedFolders" name="folders" :value="slotProps.node.id" @change="emitSelectedFolders" />
            <i :class="slotProps.node.customIcon" class="p-mx-2"></i>
            <b>{{ slotProps.node.label }}</b>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { INode } from '../../../Workspace'
import Checkbox from 'primevue/checkbox'
import Tree from 'primevue/tree'
import workspaceAnalysisFolderTreeDescriptor from './WorkspaceAnalysisFolderTreeDescriptor.json'

export default defineComponent({
    name: 'workspace-document-tree',
    components: { Checkbox, Tree },
    props: { propFolders: { type: Array } },
    emits: ['foldersSelected'],
    data() {
        return {
            workspaceAnalysisFolderTreeDescriptor,
            folders: [] as any[],
            nodes: [] as INode[],
            selectedFolders: [] as any[]
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
            this.folders = this.propFolders as any[]
        },
        createNodeTree() {
            this.nodes = []
            const foldersWithMissingParent = [] as INode[]
            this.folders.forEach((folder: any) => {
                const node = {
                    key: folder.id,
                    id: folder.id,
                    parentId: folder.parentId,
                    label: folder.name,
                    children: [] as INode[],
                    data: folder,
                    style: this.workspaceAnalysisFolderTreeDescriptor.node.style,
                    customIcon: 'pi pi-folder'
                }
                const temp = foldersWithMissingParent.filter((folder: INode) => node.id === folder.parentId)
                temp.forEach((el: any) => node.children.push(el))
                this.attachFolderToTree(node, foldersWithMissingParent)
            })
        },
        attachFolderToTree(folder: INode, foldersWithMissingParent: INode[]) {
            if (folder.parentId) {
                let parentFolder = null as INode | null
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
        findParentFolder(folderToAdd: INode, folderToSearch: INode) {
            if (folderToAdd.parentId === folderToSearch.id) {
                return folderToSearch
            } else {
                let tempFolder = null as INode | null
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
        setOpenFolderIcon(node: INode) {
            node.customIcon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: INode) {
            node.customIcon = 'pi pi-folder'
        },
        emitSelectedFolders() {
            this.$emit('foldersSelected', this.selectedFolders)
        }
    }
})
</script>

<style lang="scss" scoped>
#folders-tree {
    border: none;
}
</style>
