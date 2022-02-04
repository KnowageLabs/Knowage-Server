<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary">
        <template #start>
            <i class="fa fa-ellipsis-v p-mr-3" id="sidebar-button" @click="toggleSidebarView" />
            <span>{{ searchMode ? $t('documentBrowser.documentsSearch') : $t('documentBrowser.title') }}</span>
            <span v-if="searchMode" class="p-mx-4">
                <i class="fa fa-arrow-left search-pointer p-mx-4" @click="exitSearchMode" />
                <InputText id="document-search" class="kn-material-input p-inputtext-sm p-mx-2" v-model="searchWord" :placeholder="$t('common.search')" />
                <i class="fa fa-times search-pointer p-mx-4" @click="searchWord = ''" />
                <i class="pi pi-search search-pointer p-mx-4" @click="loadDocuments" />
            </span>
        </template>

        <template #end>
            <span v-if="!searchMode" class="p-mx-4">
                <i class="pi pi-search search-pointer" @click="searchMode = true" />
            </span>
            <KnFabButton v-if="isSuperAdmin && selectedFolder && selectedFolder.parentId" icon="fas fa-plus" @click="toggle($event)" aria-haspopup="true" aria-controls="overlay_menu"></KnFabButton>
            <Menu ref="menu" :model="items" :popup="true" />
        </template>
    </Toolbar>

    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
    <div id="document-browser-detail" class="p-d-flex p-flex-row kn-flex p-m-0">
        <div v-if="sidebarVisible && windowWidth < 1024" id="document-browser-sidebar-backdrop" @click="sidebarVisible = false"></div>

        <div v-show="!searchMode" class="document-sidebar kn-flex kn-overflow-y" :class="{ 'sidebar-hidden': isSidebarHidden, 'document-sidebar-absolute': sidebarVisible && windowWidth < 1024 }">
            <DocumentBrowserTree :propFolders="folders" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder"></DocumentBrowserTree>
        </div>

        <div id="detail-container" class="p-d-flex p-flex-column">
            <DocumentBrowserDetail
                v-if="selectedFolder || searchMode"
                :propDocuments="searchMode ? searchedDocuments : documents"
                :breadcrumbs="breadcrumbs"
                :searchMode="searchMode"
                @breadcrumbClicked="setSelectedBreadcrumb($event)"
                @documentCloned="loadDocuments"
                @documentStateChanged="loadDocuments"
                @itemSelected="$emit('itemSelected', $event)"
                @showDocumentDetails="showDocumentDetailsDialog"
            ></DocumentBrowserDetail>
            <DocumentBrowserHint v-else data-test="document-browser-hint"></DocumentBrowserHint>
        </div>
    </div>

    <DocumentDetails v-if="showDocumentDetails" :docId="documentId" :selectedDocument="selectedDocument" :selectedFolder="selectedFolder" :visible="showDocumentDetails" @closeDetails="showDocumentDetails = false" @reloadDocument="getSelectedDocument" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import DocumentBrowserHint from './DocumentBrowserHint.vue'
import DocumentBrowserTree from './DocumentBrowserTree.vue'
import DocumentBrowserDetail from './DocumentBrowserDetail.vue'
import DocumentDetails from '@/modules/documentExecution/documentDetails/DocumentDetails.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/menu'

export default defineComponent({
    name: 'document-browser-home',
    components: { DocumentBrowserHint, DocumentBrowserTree, DocumentBrowserDetail, KnFabButton, Menu, DocumentDetails },
    emits: ['itemSelected'],
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
            items: [] as any[],
            user: null as any,
            sidebarVisible: false,
            windowWidth: window.innerWidth,
            loading: false,
            showDocumentDetails: false,
            selectedDocument: null as any,
            documentId: null as any
        }
    },
    computed: {
        isSuperAdmin(): boolean {
            return this.user?.isSuperadmin
        },
        hasCreateCockpitFunctionality(): boolean {
            return this.user.functionalities.includes('CreateCockpitFunctionality')
        },
        isSidebarHidden(): boolean {
            if (this.sidebarVisible) {
                return false
            } else {
                return this.windowWidth < 1024
            }
        }
    },
    async created() {
        window.addEventListener('resize', this.onResize)

        await this.loadFolders()
        this.user = (this.$store.state as any).user
    },
    beforeUnmount() {
        window.removeEventListener('resize', this.onResize)
    },
    methods: {
        onResize() {
            this.windowWidth = window.innerWidth
        },
        async loadFolders() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/folders/`).then((response: AxiosResponse<any>) => (this.folders = response.data))
            this.loading = false
        },
        async loadDocuments() {
            this.loading = true
            const url = this.searchMode ? `2.0/documents?searchAttributes=all&searchKey=${this.searchWord}` : `2.0/documents?folderId=${this.selectedFolder?.id}`
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url).then((response: AxiosResponse<any>) => {
                this.searchMode ? (this.searchedDocuments = response.data) : (this.documents = response.data)
            })
            this.loading = false
        },
        async setSelectedFolder(folder: any) {
            if (this.selectedFolder?.id === folder.id) {
                return
            }

            this.selectedFolder = folder
            await this.loadDocumentsWithBreadcrumbs()
        },
        async loadDocumentsWithBreadcrumbs() {
            if (this.selectedFolder) {
                await this.loadDocuments()
                this.createBreadcrumbs()
            }
        },
        createBreadcrumbs() {
            let currentFolder = { key: this.selectedFolder.name, label: this.selectedFolder.name, data: this.selectedFolder } as any
            this.breadcrumbs = [] as any[]
            do {
                this.breadcrumbs.unshift({ label: currentFolder.data.name, node: currentFolder })
                currentFolder = currentFolder.data.parentFolder
            } while (currentFolder)
        },
        async setSelectedBreadcrumb(breadcrumb: any) {
            this.selectedBreadcrumb = breadcrumb

            if (this.selectedFolder?.id === breadcrumb.node.data.id) {
                return
            }
            this.selectedFolder = breadcrumb.node.data
            await this.loadDocuments()
        },
        exitSearchMode() {
            this.searchMode = false
        },
        toggle(event: any) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.items = []
            this.items.push({ label: this.$t('documentBrowser.genericDocument'), command: () => this.createNewDocument() })
            if (this.hasCreateCockpitFunctionality) {
                this.items.push({ label: this.$t('common.cockpit'), command: () => this.createNewCockpit() })
            }
        },
        createNewDocument() {
            this.documentId = null
            this.showDocumentDetails = true
        },
        async showDocumentDetailsDialog(event) {
            this.documentId = event.id
            this.showDocumentDetails = true
        },
        createNewCockpit() {
            this.$emit('itemSelected', { item: null, mode: 'createCockpit' })
        },
        toggleSidebarView() {
            this.sidebarVisible = !this.sidebarVisible
        }
    }
})
</script>

<style lang="scss" scoped>
#sidebar-button {
    display: none;
    cursor: pointer;
}

.document-sidebar {
    border-right: 1px solid #c2c2c2;
    height: 85vh;
}

.document-sidebar-absolute {
    position: absolute;
    z-index: 100;
    width: 400px;
    height: 100%;
    background-color: white;
}

#document-browser-detail {
    position: relative;
}

#document-browser-sidebar-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.sidebar-hidden {
    display: none;
}

@media screen and (max-width: 1024px) {
    #sidebar-button {
        display: inline;
    }
}

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
