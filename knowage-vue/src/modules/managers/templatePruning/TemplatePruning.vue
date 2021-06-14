<template>
    <div class="kn-page">
        <div class="kn-page-content p-m-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('managers.templatePruning.title') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
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
                                                type="text"
                                                v-model="selectedDate"
                                                :class="{
                                                    'p-invalid': !selectedDate
                                                }"
                                                :showIcon="true"
                                                :maxDate="maxDate"
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
                                <div class="kn-flex p-mt-3" v-if="documentsAvailable">
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
import { iFile } from './TemplatePruning'
import axios from 'axios'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'template-pruning',
    components: {
        Calendar,
        Card,
        Tree
    },
    data() {
        return {
            selectedDate: new Date(),
            folderStructure: [] as any[],
            documents: [] as iFile[],
            selectedDocuments: [] as iFile[],
            documentSelectionVisible: false,
            loading: false,
            nodes: [] as any[]
        }
    },
    computed: {
        maxDate() {
            return new Date()
        },
        filterDisabled(): boolean {
            return this.selectedDate === null
        },
        documentSelectionMessage(): string {
            return this.documents.length != 0 ? this.$t('managers.templatePruning.documentSelectionMessage') : this.$t('managers.templatePruning.noDocuments')
        },
        documentsAvailable(): boolean {
            return this.documents.length > 0
        },
        deleteDisabled(): boolean {
            return this.selectedDocuments.length === 0
        }
    },
    methods: {
        formatDate(date: Date) {
            return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate()
        },
        async loadDocumentSelection() {
            this.loading = true
            await this.loadDocuments(this.selectedDate)
            await this.loadFolderStructure()
            this.createNodeTree()
            this.loading = false
            this.documentSelectionVisible = true
        },
        async loadFolderStructure() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/folders?includeDocs=true').then((response) => {
                this.folderStructure = response.data
            })
        },
        async loadDocuments(date: Date) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents?date=${this.formatDate(date)}`).then((response) => (this.documents = response.data))
        },
        createNodeTree() {
            this.nodes = []
            const foldersWithoutParents = [] as any
            this.folderStructure.map((folder: any) => {
                const tempFolder = { key: folder.name, icon: 'pi pi-folder', id: folder.id, parentId: folder.parentId, label: folder.name, children: [] as any, data: { name: folder.name, hasDocuments: false }, exportable: false }
                tempFolder.children = foldersWithoutParents.filter((folder: any) => tempFolder.id === folder.parentId)
                folder.biObjects.map((document: iFile) => {
                    const index = this.documents.findIndex((el: iFile) => el.id === document.id)
                    if (index >= 0) {
                        tempFolder.data.hasDocuments = true
                        tempFolder.children.push({ key: document.id, icon: 'pi pi-file', id: document.id, label: document.name, data: document.name, exportable: false })
                    }
                })

                if (tempFolder.parentId) {
                    let parentFolder = null as any
                    for (let i = 0; i < this.nodes.length; i++) {
                        parentFolder = this.findParentFolder(tempFolder, this.nodes[i])
                        if (parentFolder) {
                            parentFolder.data.hasDocuments = true
                            parentFolder.children.push(tempFolder)
                            break
                        }
                    }
                    if (!parentFolder) {
                        foldersWithoutParents.push(tempFolder)
                    }
                } else {
                    this.nodes.push(tempFolder)
                }
            })
            for (let i = 0; i < this.nodes.length; i++) {
                this.filterDocuments(this.nodes[i], this.nodes)
            }
        },
        findParentFolder(folderToAdd, folderToSearch) {
            if (folderToAdd.parentId === folderToSearch.id) {
                return folderToSearch
            } else {
                let tempFolder = null
                for (let i = 0; i < folderToSearch.children.length; i++) {
                    tempFolder = this.findParentFolder(folderToAdd, folderToSearch.children[i])
                    if (tempFolder) {
                        break
                    }
                }
                return tempFolder
            }
        },
        filterDocuments(folder, parentFolder) {
            if (folder.data.hasDocuments) {
                for (let i = 0; i < folder.children.length; i++) {
                    this.filterDocuments(folder.children[i], folder)
                }
            } else {
                if (folder.children) {
                    const array = parentFolder.children ? parentFolder.children : parentFolder
                    const index = array.findIndex((el: any) => el.id === folder.id)
                    array.splice(index, 1)
                }
            }
        },
        setOpenFolderIcon(node: any) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: any) {
            node.icon = 'pi pi-folder'
        },
        deleteConfirm() {
            this.$confirm.require({
                message: this.$t('managers.templatePruning.deleteConfirmMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDocuments()
            })
        },
        async deleteDocuments() {
            const documentsToDelete = [] as any
            Object.keys(this.selectedDocuments).map((document: any) => {
                if (!isNaN(document)) {
                    documentsToDelete.push({ id: +document, data: this.formatDate(this.selectedDate) })
                }
            })

            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + 'template/deleteTemplate', documentsToDelete).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.selectedDocuments = []
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
</style>
