<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerDocumentsSelectionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.scheduler.selectDocument') }}
                </template>
            </Toolbar>
        </template>

        <Tree
            id="document-tree"
            :value="nodes"
            selectionMode="single"
            v-model:selectionKeys="selectedDocumentsKeys"
            :metaKeySelection="false"
            :filter="true"
            filterMode="lenient"
            @node-select="setSelectedDocument($event)"
            @node-unselect="removeSelectedDocument"
            @node-expand="setOpenFolderIcon($event)"
            @node-collapse="setClosedFolderIcon($event)"
        ></Tree>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="addDocument">{{ $t('common.add') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iFile, iNode } from '../Scheduler'
    import Dialog from 'primevue/dialog'
    import schedulerDocumentsSelectionDialogDescriptor from './SchedulerDocumentsSelectionDialogDescriptor.json'
    import Tree from 'primevue/tree'

    export default defineComponent({
        name: 'scheduler-documents-selection-dialog',
        components: { Dialog, Tree },
        props: { propFiles: { type: Array }, visible: { type: Boolean } },
        emits: ['documentSelected', 'close'],
        data() {
            return {
                schedulerDocumentsSelectionDialogDescriptor,
                files: [] as iFile[],
                documents: [] as iFile[],
                nodes: [] as iNode[],
                selectedDocumentsKeys: {},
                selectedDocument: null as any
            }
        },
        watch: {
            propFiles() {
                this.loadTree()
            }
        },
        created() {
            this.loadTree()
        },
        methods: {
            loadTree() {
                this.loadFiles()
                this.createNodeTree()
                this.removeEmptyFolders()
            },
            loadFiles() {
                this.files = this.propFiles as iFile[]
            },
            createNodeTree() {
                this.nodes = []
                const foldersWithMissingParent = [] as iNode[]
                this.files.forEach((file: iFile) => {
                    const node = {
                        key: file.id,
                        id: file.id,
                        parentId: file.parentId,
                        label: file.name,
                        children: this.formatFolderChildren(file.biObjects),
                        data: file,
                        style: this.schedulerDocumentsSelectionDialogDescriptor.node.style,
                        icon: 'pi pi-folder',
                        selectable: false
                    }

                    const temp = foldersWithMissingParent.filter((folder: iNode) => node.id === folder.parentId)
                    temp.forEach((el: any) => node.children.push(el))

                    this.attachFolderToTree(node, foldersWithMissingParent)
                })
            },
            formatFolderChildren(folderChildren: any[]) {
                const formattedChildren = [] as iNode[]
                folderChildren.forEach((document: any) => {
                    if (document.visible) {
                        formattedChildren.push({ key: document.id, icon: 'pi pi-file', id: document.id, label: document.name, data: document, selectable: true })
                    }
                })

                return formattedChildren
            },
            attachFolderToTree(folder: iNode, foldersWithMissingParent: iNode[]) {
                if (folder.parentId) {
                    let parentFolder = null as iNode | null
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
            filterDocuments(folder: iNode, parentFolder: any) {
                if (folder.children && folder.children.length > 0) {
                    this.sortDocumentChildren(folder)
                    for (let i = folder.children.length - 1; i >= 0; i--) {
                        this.filterDocuments(folder.children[i], folder)
                    }
                }
                if (folder.children?.length == 0 && parentFolder && parentFolder.children) {
                    const array = parentFolder.children
                    const index = array.findIndex((node: iNode) => node.id === folder.id)
                    array.splice(index, 1)
                }
            },
            sortDocumentChildren(folder: iNode) {
                folder.children?.sort((a: any, b: any) => (a.icon > b.icon ? -1 : 1))
            },
            removeEmptyFolders() {
                for (let i = 0; i < this.nodes.length; i++) {
                    this.filterDocuments(this.nodes[i], null as any)
                    if (this.nodes[i].children?.length === 0) {
                        this.nodes[i].selectable = false
                    }
                }
            },
            setOpenFolderIcon(node: iNode) {
                node.icon = 'pi pi-folder-open'
            },
            setClosedFolderIcon(node: iNode) {
                node.icon = 'pi pi-folder'
            },
            setSelectedDocument(node: iNode) {
                this.selectedDocument = node.data
            },
            removeSelectedDocument() {
                this.selectedDocument = null
            },
            closeDialog() {
                this.selectedDocumentsKeys = {}
                this.selectedDocument = null
                this.$emit('close')
            },
            addDocument() {
                this.$emit('documentSelected', this.selectedDocument)
                this.selectedDocumentsKeys = {}
                this.selectedDocument = null
            }
        }
    })
</script>

<style lang="scss">
    #document-tree {
        border: none;
    }
</style>
