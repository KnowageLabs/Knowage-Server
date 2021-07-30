<template>
    <div class="kn-page">
        <div class="kn-page-content p-m-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.templatePruning.title') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <div id="spinner" v-if="loading">
                <ProgressSpinner />
            </div>
            <div id="cards-container" class="kn-page-contentp-grid p-m-0">
                <div class="p-col-12">
                    <Card>
                        <template #header>
                            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                                <template #left>
                                    {{ $t('managers.templatePruning.referenceDate') }}
                                </template>
                            </Toolbar>
                        </template>
                        <template #content>
                            <div class="p-d-flex">
                                <div class="kn-flex">
                                    {{ $t('managers.templatePruning.enterDateMessage') }}
                                </div>
                                <div class="kn-flex">
                                    <div class="p-d-flex">
                                        <span class="p-float-label">
                                            <Calendar
                                                id="expirationDate"
                                                class="kn-material-input"
                                                v-model="selectedDate"
                                                :class="{
                                                    'p-invalid': !selectedDate
                                                }"
                                                :showIcon="true"
                                                :maxDate="maxDate"
                                                :manualInput="false"
                                                data-test="date-input"
                                            />
                                            <label for="expirationDate" class="kn-material-input-label"> {{ $t('managers.templatePruning.selectDate') }} * </label>
                                        </span>
                                        <Button icon="pi pi-filter" class="p-button-text p-button-rounded p-button-plain" :disabled="filterDisabled" @click="loadDocumentSelection" aria-label="Filter" data-test="filter-button" />
                                    </div>
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>
                <div class="p-col-12" v-if="documentSelectionVisible">
                    <Card data-test="document-selection-card">
                        <template #header>
                            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                                <template #left>
                                    {{ $t('managers.templatePruning.documentSelection') }}
                                </template>
                            </Toolbar>
                        </template>
                        <template #content>
                            <div class="p-d-flex p-flex-column">
                                <div class="kn-flex">
                                    <p>{{ documentSelectionMessage }}</p>
                                    <Button class="kn-button kn-button--primary" v-if="documentsAvailable" :disabled="deleteDisabled" @click="deleteConfirm" aria-label="Delete Templates" data-test="delete-button">{{ $t('common.delete') }}</Button>
                                </div>
                                <div class="kn-flex p-mt-3">
                                    <Tree id="document-tree" :value="nodes" selectionMode="checkbox" v-model:selectionKeys="selectedDocuments" :filter="true" filterMode="lenient" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)" data-test="document-tree"></Tree>
                                </div>
                            </div>
                        </template>
                    </Card>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFile, iNode } from './TemplatePruning'
import axios from 'axios'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import ProgressSpinner from 'primevue/progressspinner'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'template-pruning',
    components: {
        Calendar,
        Card,
        ProgressSpinner,
        Tree
    },
    data() {
        return {
            selectedDate: new Date(),
            folderStructure: [] as iFile[],
            documents: [] as iFile[],
            nodes: [] as iNode[],
            selectedDocuments: {},
            documentSelectionVisible: false,
            loading: false
        }
    },
    computed: {
        maxDate() {
            return new Date()
        },
        filterDisabled(): boolean {
            return !this.selectedDate
        },
        documentSelectionMessage(): string {
            return this.documents.length != 0 ? this.$t('managers.templatePruning.documentSelectionMessage') : this.$t('managers.templatePruning.noDocuments')
        },
        documentsAvailable(): boolean {
            return this.documents.length > 0
        },
        deleteDisabled(): boolean {
            return Object.keys(this.selectedDocuments).length === 0
        }
    },
    methods: {
        formatDate(date: Date) {
            return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate()
        },
        async loadDocumentSelection() {
            this.loading = true
            this.selectedDocuments = {}
            await this.loadDocuments(this.selectedDate)
            await this.loadFolderStructure()
            this.createNodeTree()
            this.removeEmptyFolders()
            this.documentSelectionVisible = true
            this.loading = false
        },
        async loadFolderStructure() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true').then((response) => (this.folderStructure = response.data))
        },
        async loadDocuments(date: Date) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents?date=${this.formatDate(date)}`).then((response) => (this.documents = response.data))
        },
        createNodeTree() {
            this.nodes = []
            const foldersWithMissingParent = [] as iNode[]
            this.folderStructure.forEach((folder: iFile) => {
                const node = { key: folder.name, icon: 'pi pi-folder', id: folder.id, parentId: folder.parentId, label: folder.name, children: [] as iNode[], data: { name: folder.name } }
                node.children = foldersWithMissingParent.filter((folder: iNode) => node.id === folder.parentId)

                this.attachDocumentsToNode(folder, node)
                this.attachFolderToTree(node, foldersWithMissingParent)
            })
        },
        attachDocumentsToNode(folder: iFile, node: iNode) {
            folder.biObjects.forEach((document: iFile) => {
                const index = this.documents.findIndex((file: iFile) => file.id === document.id)

                if (index >= 0) {
                    node.children?.push({ key: document.id, icon: 'pi pi-file', id: document.id, label: document.name, data: document.name })
                }
            })
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
        deleteConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDocuments()
            })
        },
        async deleteDocuments() {
            const documentsToDelete = [] as { id: number; data: string }[]
            if (this.selectedDocuments) {
                Object.keys(this.selectedDocuments as {}).forEach((id: any) => {
                    if (!isNaN(id)) {
                        documentsToDelete.push({ id: +id, data: this.formatDate(this.selectedDate) })
                    }
                })
            }

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'template/deleteTemplate', documentsToDelete).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.selectedDocuments = {}
                this.loadDocumentSelection()
            })
        }
    }
})
</script>

<style lang="scss" scoped>
#cards-container {
    flex: 0.5;
}

#document-tree {
    border: 0;
}

#spinner {
    position: fixed;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    opacity: 0.7;
    background-color: rgba(0, 0, 0, $alpha: 0.5);
    z-index: 99;
}

::v-deep(.p-progress-spinner-circle) {
    animation: p-progress-spinner-color 4s ease-in-out infinite;
}
@keyframes p-progress-spinner-color {
    100%,
    0% {
        stroke: #43749e;
    }
    80%,
    90% {
        stroke: #d62d20;
    }
}
</style>
