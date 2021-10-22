<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div id="document-browser-home-toolbar">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('documentBrowser.title') }}
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
            </div>

            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <DocumentBrowserTree :propFolders="folders" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder"></DocumentBrowserTree>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <DocumentBrowserDetail v-if="selectedFolder" :propDocuments="documents" :breadcrumbs="breadcrumbs" @breadcrumbClicked="setSelectedBreadcrumb($event)"></DocumentBrowserDetail>
                <DocumentBrowserHint v-else></DocumentBrowserHint>
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
            breadcrumbs: [] as any[],
            selectedBreadcrumb: null as any,
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
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents?folderId=${this.selectedFolder?.id}`).then((response) => (this.documents = response.data))
            this.loading = false
            // console.log('LOADED DOCUMENTS: ', this.documents)
        },
        async setSelectedFolder(folder: any) {
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
        setSelectedBreadcrumb(breadcrumb: any) {
            console.log('BREADCRUMB SELECTED IN HOME: ', breadcrumb)
            this.selectedBreadcrumb = breadcrumb
        }
    }
})
</script>

<style scoped>
#document-browser-home-toolbar {
    width: 100%;
}
</style>
