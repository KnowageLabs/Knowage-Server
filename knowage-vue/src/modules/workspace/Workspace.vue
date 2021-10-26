<template>
    <div class="kn-page p-d-flex p-flex-row">
        <div id="sideMenu" class="kn-list--column">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('workspace.menuLabels.menuTitle') }}
                </template>
            </Toolbar>
            <PanelMenu v-if="displayMenu" :model="menuItems">
                <!-- <template #item="{item}">
                    {{ item }}
                </template> -->
            </PanelMenu>
        </div>
        <div class=" p-d-flex p-flex-column" style="width:100%">
            <Button id="showSidenavIcon" v-if="$router.currentRoute._rawValue.fullPath === '/workspace/'" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="sidebarVisible = true" />
            <router-view class="kn-router-view" :selectedFolder="selectedFolder" @showMenu="sidebarVisible = true" @reloadRepositoryMenu="getAllFolders" />
        </div>
    </div>

    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('workspace.menuLabels.menuTitle') }}
            </template>
        </Toolbar>
        <PanelMenu :model="menuItems"> </PanelMenu>
    </Sidebar>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import PanelMenu from 'primevue/panelmenu'
import Sidebar from 'primevue/sidebar'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'

export default defineComponent({
    name: 'dataset-management',
    components: { PanelMenu, Sidebar },
    data() {
        return {
            sidebarVisible: false,
            allFolders: [] as IFolder[],
            selectedFolder: {} as IFolder,
            allDocuments: [] as IDocument[],
            items: [] as IFolder[],
            displayMenu: false,
            menuItems: [
                {
                    key: '0',
                    label: this.$t('workspace.menuLabels.recent'),
                    icon: 'fas fa-history',
                    command: () => {
                        // event.originalEvent: Browser event
                        // event.item: Menuitem instance
                        this.setActiveView('/workspace/recent')
                    }
                },
                {
                    key: '1',
                    label: this.$t('workspace.menuLabels.myRepository'),
                    icon: 'fas fa-folder',
                    command: () => {}
                },
                {
                    key: '2',
                    label: this.$t('workspace.menuLabels.myData'),
                    icon: 'fas fa-database',
                    command: () => {
                        this.setActiveView('/workspace/data')
                    }
                },
                {
                    key: '3',
                    label: this.$t('workspace.menuLabels.myModels'),
                    icon: 'fas fa-table',
                    command: () => {
                        this.setActiveView('/workspace/models')
                    }
                },
                {
                    key: '5',
                    label: this.$t('workspace.menuLabels.myAnalysis'),
                    icon: 'fas fa-th-large',
                    command: () => {
                        this.setActiveView('/workspace/analysis')
                    }
                },
                {
                    key: '5',
                    label: this.$t('workspace.menuLabels.schedulation'),
                    icon: 'fas fa-stopwatch',
                    command: () => {
                        this.setActiveView('/workspace/schedulation')
                    }
                }
            ] as any
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
                this.createNodeTree()
                this.menuItems[1].items = this.items
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
        //#region ==================================== Create Menu Items ====================================
        createNodeTree() {
            console.log('   createNodeTree() {')
            this.items = []
            const foldersWithMissingParent = [] as IFolder[]
            this.allFolders.forEach((folder: IFolder) => {
                const node = {
                    key: folder.name,
                    icon: 'pi pi-folder',
                    functId: folder.functId,
                    parentFunct: folder.parentFunct,
                    label: folder.name,
                    path: folder.path,
                    prog: folder.prog,
                    items: [] as IFolder[],
                    data: { name: folder.name, hasDocuments: false },
                    command: (event) => {
                        console.log(event.item)
                        this.selectedFolder = event.item
                        this.$router.push(`/workspace/repository/${event.item.functId}`)
                    }
                }
                node.items = foldersWithMissingParent.filter((folder: any) => node.functId === folder.parentFunct)
                this.attachFolderToTree(node, foldersWithMissingParent)
            })
            console.log(this.items)
        },
        attachFolderToTree(folder, foldersWithMissingParent) {
            if (folder.parentFunct) {
                let parentFolder = null as IFolder | null
                for (let i = 0; i < this.items.length; i++) {
                    parentFolder = this.findParentFolder(folder, this.items[i])
                    if (parentFolder) {
                        parentFolder.data ? (parentFolder.data.hasDocuments = true) : ''
                        parentFolder.items?.push(folder)
                        break
                    }
                }
                if (!parentFolder) {
                    foldersWithMissingParent.push(folder)
                }
            } else {
                this.items.push(folder)
            }
        },
        findParentFolder(folderToAdd, folderToSearch) {
            if (folderToAdd.parentFunct === folderToSearch.functId) {
                return folderToSearch
            } else {
                let tempFolder = null as IFolder | null
                if (folderToSearch.items) {
                    for (let i = 0; i < folderToSearch.items.length; i++) {
                        tempFolder = this.findParentFolder(folderToAdd, folderToSearch.items[i])
                        if (tempFolder) {
                            break
                        }
                    }
                }
                return tempFolder
            }
        }
        //#endregion ======================================================================================================
    }
})
</script>
<style scoped lang="scss">
#sideMenu {
    width: 33.3333%;
}
@media screen and (max-width: 1017px) {
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
.mySidebar .p-listbox {
    height: calc(100% - 2.5rem);
}
@media screen and (min-width: 1017px) {
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
