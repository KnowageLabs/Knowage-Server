<template>
    <div class="kn-page">
        <div class="document-browser-tab-container kn-page-content p-d-flex p-flex-column">
            <TabView id="document-browser-tab-view" class="p-d-flex p-flex-column kn-flex" v-model:activeIndex="activeIndex" @tab-change="onTabChange">
                <TabPanel>
                    <template #header>
                        <i class="fa fa-folder-open"></i>
                    </template>

                    <DocumentBrowserHome @itemSelected="onItemSelect($event)"></DocumentBrowserHome>
                </TabPanel>

                <TabPanel v-for="(tab, index) in tabs" :key="index">
                    <template #header>
                        <span>{{ tab.item?.name ? tab.item?.name : 'new dashboard' }}</span>
                    </template>

                    <!-- <DocumentBrowserTab :item="tab.item" :mode="tab.mode" @close="closeDocument('current')"></DocumentBrowserTab> -->
                </TabPanel>
            </TabView>

            <!-- TODO -->
            <!-- {{ selectedItem }} -->

            <!-- {{ tabs }} -->

            <DocumentBrowserTab v-if="selectedItem" :item="selectedItem.item" :mode="selectedItem.mode" @close="closeDocument('current')"></DocumentBrowserTab>
            <div id="document-browser-tab-icon-container" v-if="activeIndex !== 0">
                <i id="document-browser-tab-icon" class="fa fa-times-circle" @click="toggle($event)"></i>
                <Menu ref="menu" :model="menuItems" :popup="true" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentBrowserHome from './documentBrowserHome/DocumentBrowserHome.vue'
import DocumentBrowserTab from './DocumentBrowserTab.vue'
import Menu from 'primevue/menu'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'document-browser',
    components: { DocumentBrowserHome, DocumentBrowserTab, Menu, TabView, TabPanel },
    data() {
        return {
            tabs: [] as any[],
            activeIndex: 0,
            menuItems: [] as any[],
            selectedItem: null as any,
            id: 0
        }
    },
    created() {
        if (this.$route.params.id && this.$route.name === 'document-browser-document-execution') {
            const tempItem = { item: { name: this.$route.params.id, label: this.$route.params.id, mode: this.$route.params.mode, routerId: this.id++ }, mode: 'execute' }
            this.tabs.push(tempItem)

            this.activeIndex = 1
            this.selectedItem = tempItem
        }
    },
    methods: {
        onTabChange() {
            if (this.activeIndex === 0) {
                this.$router.push('/document-browser')
                return
            }

            const id = this.tabs[this.activeIndex - 1].item ? this.tabs[this.activeIndex - 1].item.label : 'new-dashboard'

            console.log('ITEM ON TAB CHANGE: ', this.tabs[this.activeIndex - 1].item)
            this.selectedItem = this.tabs[this.activeIndex - 1]

            let routeDocumentType = this.tabs[this.activeIndex - 1].item.mode ? this.tabs[this.activeIndex - 1].item.mode : this.getRouteDocumentType(this.tabs[this.activeIndex - 1].item)
            this.$router.push(`/document-browser/${routeDocumentType}/` + id)
        },
        onItemSelect(payload: any) {
            payload.item.routerId = this.id++

            const tempItem = { ...payload, item: { ...payload.item } }

            this.tabs.push(tempItem)

            console.log('TABS AFTER ITEM SELECT: ', this.tabs)

            this.selectedItem = tempItem

            const id = payload.item ? payload.item.label : 'new-dashboard'
            console.log(' DOCUMENT BROWSER SELECTED ITEM: ', payload.item)
            let routeDocumentType = this.getRouteDocumentType(payload.item)

            this.$router.push(`/document-browser/${routeDocumentType}/` + id)
            this.activeIndex = this.tabs.length
        },
        getRouteDocumentType(item: any) {
            let routeDocumentType = ''

            switch (item.typeCode) {
                case 'DATAMART':
                    routeDocumentType = 'registry'
                    break
                case 'DOCUMENT_COMPOSITE':
                    routeDocumentType = 'document-composite'
                    break
                case 'OFFICE_DOC':
                    routeDocumentType = 'office-doc'
                    break
                case 'OLAP':
                    routeDocumentType = 'olap'
                    break
                case 'MAP':
                    routeDocumentType = 'map'
                    break
                case 'REPORT':
                    routeDocumentType = 'report'
                    break
                case 'KPI':
                    routeDocumentType = 'kpi'
                    break
                case 'DOSSIER':
                    routeDocumentType = 'dossier'
                    break
                case 'ETL':
                    routeDocumentType = 'etl'
                    break
            }

            return routeDocumentType
        },
        toggle(event: any) {
            this.createMenuItems()
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems() {
            this.menuItems = []
            this.menuItems.push({ label: this.$t('documentBrowser.closeCurrentDocument'), command: () => this.closeDocumentConfirm('current') })
            this.menuItems.push({ label: this.$t('documentBrowser.closeOtherDocuments'), command: () => this.closeDocumentConfirm('other') })
            this.menuItems.push({ label: this.$t('documentBrowser.closeDocumentRight'), command: () => this.closeDocumentConfirm('right') })
            this.menuItems.push({ label: this.$t('documentBrowser.closeAllDocuments'), command: () => this.closeDocumentConfirm('all') })
        },
        closeDocumentConfirm(mode: string) {
            this.$confirm.require({
                message: this.$t('documentBrowser.closeDocumentConfirmMessage'),
                header: this.$t('documentBrowser.closeDocumentConfirmTitle', { numberOfDocuments: this.getNumberOfDocuments(mode) }),
                accept: () => this.closeDocument(mode)
            })
        },
        getNumberOfDocuments(mode: string) {
            switch (mode) {
                case 'current':
                    return 1
                case 'other':
                    return this.tabs.length - 1
                case 'right':
                    return this.tabs.length - this.activeIndex
                case 'all':
                    return this.tabs.length
            }
        },
        closeDocument(mode: string) {
            switch (mode) {
                case 'current':
                    this.tabs.splice(this.activeIndex - 1, 1)
                    this.activeIndex = 0
                    this.$router.push('/document-browser')
                    break
                case 'other':
                    this.tabs = [this.tabs[this.activeIndex - 1]]
                    this.activeIndex = 1
                    break
                case 'right':
                    this.tabs.splice(this.activeIndex)
                    break
                case 'all':
                    this.$router.push('/document-browser')
                    this.tabs = []
                    this.activeIndex = 0
            }
        }
    }
})
</script>

<style lang="scss">
#document-browser-tab-view .p-tabview-panels {
    padding: 0;
}

#document-browser-tab-icon-container {
    position: absolute;
    top: 0.8rem;
    right: 2rem;
}

#document-browser-tab-icon {
    font-size: 1.2rem;
    color: rgba(0, 0, 0, 0.6);
    cursor: pointer;
}

.document-browser-tab-container {
    position: relative;
}

.document-browser-tab-container .p-tabview .p-tabview-panel,
.document-browser-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
