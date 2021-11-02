<template>
    <div class="kn-page p-d-flex p-flex-row">
        <div id="sideMenu" class="kn-list--column">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('workspace.menuLabels.menuTitle') }}
                </template>
            </Toolbar>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <Listbox v-if="displayMenu" :options="workspaceDescriptor.menuItems">
                <template #option="slotProps">
                    <div v-if="slotProps.option.value !== 'repository'" class="kn-list-item" @click="setActiveView(`/workspace/${slotProps.option.value}`)">
                        <i :class="slotProps.option.icon"></i>
                        <div class="kn-list-item-text">
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </div>
                    <div v-else class="menu-accordion">
                        <Accordion>
                            <AccordionTab :header="$t('workspace.menuLabels.myRepository')">
                                <WorkspaceDocumentTree :propFolders="allFolders" mode="select" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder" @delete="deleteFolder" @createFolder="showCreateFolderDialog"></WorkspaceDocumentTree>
                            </AccordionTab>
                        </Accordion>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class=" p-d-flex p-flex-column" style="width:100%">
            <Button id="showSidenavIcon" v-if="$router.currentRoute._rawValue.fullPath === '/workspace/'" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="sidebarVisible = true" />
            <router-view
                class="kn-router-view"
                :selectedFolder="selectedFolder"
                :toggleCardDisplay="toggleCardDisplay"
                :breadcrumbs="breadcrumbs"
                @toggleDisplayView="toggleDisplayView"
                @showMenu="sidebarVisible = true"
                @reloadRepositoryMenu="getAllFolders"
                @createFolderClick="displayCreateFolderDialog = true"
                @breadcrumbClicked="setSelectedBreadcrumb($event)"
            />
        </div>
    </div>

    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('workspace.menuLabels.menuTitle') }}
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Listbox :options="workspaceDescriptor.menuItems">
            <template #option="slotProps">
                <div v-if="slotProps.option.value !== 'repository'" class="kn-list-item" @click="setActiveView(`/workspace/${slotProps.option.value}`)">
                    <i :class="slotProps.option.icon"></i>
                    <div class="kn-list-item-text">
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </div>
                <div v-else class="menu-accordion">
                    <Accordion>
                        <AccordionTab :header="$t('workspace.menuLabels.myRepository')">
                            <WorkspaceDocumentTree :propFolders="allFolders" mode="select" :selectedBreadcrumb="selectedBreadcrumb" @folderSelected="setSelectedFolder" @delete="deleteFolder" @createFolder="showCreateFolderDialog"></WorkspaceDocumentTree>
                        </AccordionTab>
                    </Accordion>
                </div>
            </template>
        </Listbox>
    </Sidebar>

    <WorkspaceNewFolderDialog :visible="displayCreateFolderDialog" @close="displayCreateFolderDialog = false" @create="createNewFolder"></WorkspaceNewFolderDialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import Sidebar from 'primevue/sidebar'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Listbox from 'primevue/listbox'
import WorkspaceDocumentTree from './genericComponents/WorkspaceDocumentTree.vue'
import workspaceDescriptor from './WorkspaceDescriptor.json'
import WorkspaceNewFolderDialog from './views/repositoryView/dialogs/WorkspaceNewFolderDialog.vue'

export default defineComponent({
    name: 'dataset-management',
    components: { Sidebar, Listbox, Accordion, AccordionTab, WorkspaceDocumentTree, WorkspaceNewFolderDialog },
    data() {
        return {
            workspaceDescriptor,
            sidebarVisible: false,
            toggleCardDisplay: false,
            allFolders: [] as IFolder[],
            selectedFolder: {} as any,
            allDocuments: [] as IDocument[],
            items: [] as IFolder[],
            displayMenu: false,
            displayCreateFolderDialog: false,
            breadcrumbs: [] as any[],
            selectedBreadcrumb: null as any,
            loading: false
        }
    },
    created() {
        this.getAllRepositoryData()
    },
    methods: {
        closeSidebar() {
            this.sidebarVisible = false
        },
        async getAllRepositoryData() {
            await this.getAllFolders()
            await this.getAllDocuments()
        },
        async getAllFolders() {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/folders/`).then((response) => {
                this.allFolders = [...response.data]
                this.displayMenu = true
            })
        },
        async getAllDocuments() {
            return this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/`).then((response) => {
                this.allDocuments = [...response.data]
            })
        },
        setActiveView(route) {
            this.$router.push(route)
            this.closeSidebar()
        },
        toggleDisplayView() {
            this.toggleCardDisplay = this.toggleCardDisplay ? false : true
        },
        setSelectedFolder(folder: any) {
            this.selectedFolder = folder
            console.log('SELECTED FOLDER IN WORKSPACE MAIN: ', this.selectedFolder)
            this.createBreadcrumbs()
            this.$router.push(`/workspace/repository/${folder.id}`)
        },
        async deleteFolder(folder: any) {
            console.log('FOLDER FOR DELETE MAIN: ', folder)
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/foldersee/${folder.id}`, { headers: { 'X-Disable-Errors': true } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.getAllRepositoryData()
                    this.$router.push('/workspace')
                })
                .catch((response) => {
                    console.log('response', response)
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response.message === 'sbi.workspace.organizer.folder.error.delete' ? this.$t('workspace.myRepository.folderDeleteError') : response.message
                    })
                })
            this.loading = false
        },
        showCreateFolderDialog(folder: any) {
            this.selectedFolder = folder
            this.displayCreateFolderDialog = true
        },
        async createNewFolder(newFolder: any) {
            newFolder.parentFunct = this.selectedFolder?.id
            newFolder.path = this.selectedFolder?.path + `/` + encodeURIComponent(newFolder.code)
            newFolder.prog = this.selectedFolder?.prog
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/organizer/foldersee/', newFolder, { headers: { 'X-Disable-Errors': true } })
                .then(() => {
                    this.$store.commit('setInfo', { title: this.$t('common.toast.success') })
                    this.getAllFolders()
                })
                .catch((response) => {
                    console.log('CREATE NEW FOLDER ERROR RESPONSE: ', response)
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.displayCreateFolderDialog = false))
        },
        createBreadcrumbs() {
            // console.log('CURRENT FOLDER START METHOD: ', this.selectedFolder)
            let currentFolder = this.selectedFolder as any
            this.breadcrumbs = [] as any[]
            do {
                this.breadcrumbs.unshift({ label: currentFolder.data.name, node: currentFolder })
                currentFolder = currentFolder.data.parentFolder
                // console.log('CURRENT FOLDER: ', currentFolder)
            } while (currentFolder)
            console.log('CREATED BREADCRUMBS: ', this.breadcrumbs)
        },
        async setSelectedBreadcrumb(breadcrumb: any) {
            console.log('SELCTED BREADCRUMB: ', breadcrumb)
            this.selectedBreadcrumb = breadcrumb
            this.$router.push(`/workspace/repository/${this.selectedBreadcrumb.node.id}`)
        }
    }
})
</script>
<style scoped lang="scss">
#sideMenu {
    width: 33.3333%;
}
@media screen and (max-width: 1024px) {
    #sideMenu {
        -webkit-transition: width 0.3s;
        transition: width 0.3s;
        width: 0%;
    }
    #detailContent {
        width: 100%;
    }
    #showSidenavIcon {
        display: inline;
    }
}
</style>

<style lang="scss">
.overflow {
    overflow: auto;
}
.mySidebar.p-sidebar .p-sidebar-header,
.mySidebar.p-sidebar .p-sidebar-content {
    padding: 0 !important;
}

.menu-accordion .p-accordion-tab-active {
    margin: 0 !important;
    padding: 0 !important;
    border-bottom: 1 px solid #f2f2f2;
}

.menu-accordion .p-accordion-content {
    padding: 0 !important;
}

@media screen and (min-width: 1025px) {
    #sideMenu {
        -webkit-transition: width 0.3s;
        transition: width 0.3s;
        width: 100%;
    }
    #showSidenavIcon {
        display: none;
    }
}
</style>
