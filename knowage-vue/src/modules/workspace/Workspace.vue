<template>
    <div class="kn-page p-d-flex p-flex-row">
        <div id="sideMenu" class="kn-list--column">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('workspace.menuLabels.menuTitle') }}
                </template>
            </Toolbar>
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
                                <WorkspaceDocumentTree :propFolders="allFolders" mode="select" @folderSelected="setSelectedFolder"></WorkspaceDocumentTree>
                            </AccordionTab>
                        </Accordion>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class=" p-d-flex p-flex-column" style="width:100%">
            <Button id="showSidenavIcon" v-if="$router.currentRoute._rawValue.fullPath === '/workspace/'" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="sidebarVisible = true" />
            <router-view class="kn-router-view" :selectedFolder="selectedFolder" :toggleCardDisplay="toggleCardDisplay" @toggleDisplayView="toggleDisplayView" @showMenu="sidebarVisible = true" @reloadRepositoryMenu="getAllFolders" />
        </div>
    </div>

    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('workspace.menuLabels.menuTitle') }}
            </template>
        </Toolbar>
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
                            <WorkspaceDocumentTree :propFolders="allFolders" mode="select" @folderSelected="setSelectedFolder"></WorkspaceDocumentTree>
                        </AccordionTab>
                    </Accordion>
                </div>
            </template>
        </Listbox>
    </Sidebar>
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

export default defineComponent({
    name: 'dataset-management',
    components: { Sidebar, Listbox, Accordion, AccordionTab, WorkspaceDocumentTree },
    data() {
        return {
            workspaceDescriptor,
            sidebarVisible: false,
            toggleCardDisplay: false,
            allFolders: [] as IFolder[],
            selectedFolder: {} as IFolder,
            allDocuments: [] as IDocument[],
            items: [] as IFolder[],
            displayMenu: false
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
            // console.log('SELECTED FOLDER IN WORKSPACE MAIN: ', this.selectedFolder)
            this.$router.push(`/workspace/repository/${folder.id}`)
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
