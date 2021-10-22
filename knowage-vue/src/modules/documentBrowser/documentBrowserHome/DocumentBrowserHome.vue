<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div id="document-browser-home-toolbar">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        <span>{{ searchMode ? $t('documentBrowser.documentsSearch') : $t('documentBrowser.title') }}</span>
                        <span v-if="searchMode" class="p-mx-4">
                            <i class="fa fa-arrow-left search-pointer p-mx-4" @click="exitSearchMode" />
                            <InputText id="document-search" class="kn-material-input p-inputtext-sm p-mx-2" v-model="searchWord" :placeholder="$t('common.search')" />
                            <i class="fa fa-times search-pointer p-mx-4" @click="searchWord = ''" />
                            <i class="pi pi-search search-pointer p-mx-4" @click="loadDocuments" />
                        </span>
                    </template>

                    <template #right>
                        <span v-if="!searchMode" class="p-mx-4">
                            <i class="pi pi-search search-pointer" @click="searchMode = true" />
                        </span>
                    </template>
                </Toolbar>

                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
            </div>

            <div class="p-d-flex p-flex-row full-width">
                <div v-show="!searchMode" class="kn-list--column kn-flex p-p-0">
                    <DocumentBrowserTree :propFolders="folders" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder"></DocumentBrowserTree>
                </div>

                <div id="detail-container" class="p-p-0 p-m-0 kn-page">
                    <DocumentBrowserDetail v-if="selectedFolder || searchMode" :propDocuments="searchMode ? searchedDocuments : documents" :breadcrumbs="breadcrumbs" :searchMode="searchMode" @breadcrumbClicked="setSelectedBreadcrumb($event)"></DocumentBrowserDetail>
                    <DocumentBrowserHint v-else></DocumentBrowserHint>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import DocumentBrowserHint from './DocumentBrowserHint.vue'
import DocumentBrowserTree from './DocumentBrowserTree.vue'
import DocumentBrowserDetail from './DocumentBrowserDetail.vue'

export default defineComponent({
    name: 'document-browser-home',
    components: { DocumentBrowserHint, DocumentBrowserTree, DocumentBrowserDetail },
    data() {
        return {
            folders: [] as any[],
            selectedFolder: null as any,
            documents: [] as any[],
            searchedDocuments: [] as any[],
            breadcrumbs: [] as any[],
            selectedBreadcrumb: null as any,
            searchWord: null as any,
            searchMode: false,
            loading: false
        }
    },
    async created() {
        await this.loadFolders()
    },
    methods: {
        async loadFolders() {
            this.loading = true
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/folders/`).then((response) => (this.folders = response.data))
            this.loading = false
            // console.log('LOADED FOLDERS: ', this.folders)
        },
        async loadDocuments() {
            this.loading = true
            const url = this.searchMode ? `2.0/documents?searchAttributes=all&searchKey=${this.searchWord}` : `2.0/documents?folderId=${this.selectedFolder?.id}`
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response) => {
                this.searchMode ? (this.searchedDocuments = response.data) : (this.documents = response.data)
            })
            this.loading = false
            // console.log('LOADED DOCUMENTS: ', this.documents)
        },
        async setSelectedFolder(folder: any) {
            if (this.selectedFolder?.id === folder.id) {
                return
            }

            this.selectedFolder = folder
            if (this.selectedFolder) {
                await this.loadDocuments()
                this.createBreadcrumbs()
            }
        },
        createBreadcrumbs() {
            console.log('SELECTED FOLDER FOR BREADCRUMBS: ', this.selectedFolder)
            let currentFolder = { key: this.selectedFolder.name, label: this.selectedFolder.name, data: this.selectedFolder } as any
            this.breadcrumbs = [] as any[]
            do {
                // console.log('TEST: ', currentFolder.data.name)
                this.breadcrumbs.unshift({ label: currentFolder.data.name, node: currentFolder })
                currentFolder = currentFolder.data.parentFolder
                // console.log('CURRENT FOLDER: ', currentFolder)
            } while (currentFolder)
            // console.log('BREADCRUMBS: ', this.breadcrumbs)
        },
        async setSelectedBreadcrumb(breadcrumb: any) {
            console.log('BREADCRUMB SELECTED IN HOME: ', breadcrumb)
            console.log('SELECTED FOLDER BEFORE: ', this.selectedFolder)
            this.selectedBreadcrumb = breadcrumb

            if (this.selectedFolder?.id === breadcrumb.node.data.id) {
                return
            }
            this.selectedFolder = breadcrumb.node.data
            await this.loadDocuments()
            console.log('SELECTED FOLDER AFTER: ', this.selectedFolder)
        },
        exitSearchMode() {
            this.searchMode = false
        }
    }
})
</script>

<style lang="scss" scoped>
#document-browser-home-toolbar {
    width: 100%;
}

.search-pointer:hover {
    cursor: pointer;
}

#document-search {
    min-width: 500px;
    background-color: $color-primary;
    color: white;
    border-bottom-color: white;
}

.full-width {
    width: 100%;
}

#detail-container {
    flex: 3;
}
</style>
