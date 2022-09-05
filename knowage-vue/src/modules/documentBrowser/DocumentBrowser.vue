<template>
    <div class="kn-page">
        <div class="document-browser-tab-container kn-page-content">
            <TabView id="document-browser-tab-view" class="p-d-flex p-flex-column kn-flex kn-tab" v-model:activeIndex="activeIndex" @tab-change="onTabChange">
                <TabPanel>
                    <template #header>
                        <i class="fa fa-folder-open"></i>
                    </template>

                    <DocumentBrowserHome :documentSaved="documentSaved" :documentSavedTrigger="documentSavedTrigger" @itemSelected="onItemSelect($event)"></DocumentBrowserHome>
                </TabPanel>

                <TabPanel v-for="(tab, index) in tabs" :key="index">
                    <template #header>
                        <span>{{ getTabName(tab) }}</span>
                    </template>
                </TabPanel>
            </TabView>

            <DocumentBrowserTab v-show="selectedItem && selectedItem.mode" :item="selectedItem?.item" :functionalityId="selectedItem?.functionalityId" @close="closeDocument('current')" @iframeCreated="onIFrameCreated" @closeIframe="closeIframe" @documentSaved="onDocumentSaved"></DocumentBrowserTab>
            <div v-for="(iframe, index) in iFrameContainers" :key="index">
                <iframe v-show="iframe.item?.routerId === selectedItem?.item.routerId" ref="iframe" class="document-browser-cockpit-iframe" :src="iframe.iframe"></iframe>
            </div>
            <div id="document-browser-tab-icon-container" v-if="activeIndex !== 0">
                <i id="document-browser-tab-icon" class="fa fa-times-circle" @click="toggle($event)"></i>
                <Menu ref="menu" :model="menuItems" :popup="true" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { getRouteDocumentType } from './documentBrowserHelper'
import DocumentBrowserHome from './documentBrowserHome/DocumentBrowserHome.vue'
import DocumentBrowserTab from './DocumentBrowserTab.vue'
import Menu from 'primevue/menu'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

import cryptoRandomString from 'crypto-random-string'

export default defineComponent({
    name: 'document-browser',
    components: { DocumentBrowserHome, DocumentBrowserTab, Menu, TabView, TabPanel },
    props: { selectedMenuItem: { type: Object }, menuItemClickedTrigger: { type: Boolean } },
    data() {
        return {
            tabs: [] as any[],
            activeIndex: 0,
            menuItems: [] as any[],
            selectedItem: null as any,
            id: 0,
            iFrameContainers: [] as any[],
            menuItem: null,
            documentSaved: null,
            documentSavedTrigger: false,
            getRouteDocumentType
        }
    },
    watch: {
        menuItemClickedTrigger() {
            if (!this.selectedMenuItem) return
            if (this.selectedMenuItem.to === '/document-browser') {
                this.selectedItem = null
                this.activeIndex = 0
            } else if (this.selectedMenuItem.to && this.selectedMenuItem.to.includes('document-browser')) {
                this.loadPage()
            }
        }
    },
    async created() {
        this.loadPage()
    },
    methods: {
        async loadPage() {
            window.addEventListener('message', (event) => {
                if (event.data.type === 'saveCockpit' && this.$router.currentRoute.value.name === 'new-dashboard') {
                    this.loadSavedCockpit(event.data.model)
                    this.documentSaved = event.data.model
                    this.documentSavedTrigger = !this.documentSavedTrigger
                }
            })

            let id = this.$router.currentRoute.value.params.id ?? this.parseSelectedMenuItem()

            if (id && id !== 'document-browser' && (this.$router.currentRoute.value.name === 'document-browser-document-execution' || this.$router.currentRoute.value.name === 'document-browser-document-details-edit' || this.$router.currentRoute.value.name === 'document-browser')) {
                let tempDocument = {} as any
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documents/${id}`).then((response: AxiosResponse<any>) => (tempDocument = response.data))
                const tempItem = {
                    item: {
                        name: tempDocument.name,
                        label: id,
                        mode: this.$router.currentRoute.value.params.mode,
                        routerId: cryptoRandomString({ length: 16, type: 'base64' }),
                        id: id,
                        showMode: this.$router.currentRoute.value.name === 'document-browser-document-execution' ? 'execute' : 'documentDetail'
                    },
                    mode: this.$router.currentRoute.value.name === 'document-browser-document-execution' && this.$router.currentRoute.value.params.id ? 'execute' : 'documentDetail'
                }
                this.tabs.push(tempItem)

                this.activeIndex = 1
                this.selectedItem = tempItem
            }
        },
        parseSelectedMenuItem() {
            if (!this.selectedMenuItem) return null

            return this.selectedMenuItem.to?.substring(this.selectedMenuItem.to.lastIndexOf('/') + 1)
        },
        onTabChange() {
            if (this.activeIndex === 0) {
                this.selectedItem = null
                this.$router.push('/document-browser')
                return
            }

            const id = this.tabs[this.activeIndex - 1].item ? this.tabs[this.activeIndex - 1].item.label : 'new-dashboard'

            this.selectedItem = this.tabs[this.activeIndex - 1]
            this.selectedItem.item.fromTab = true

            if (this.selectedItem.mode === 'documentDetail') {
                const path = this.selectedItem.functionalityId ? `/document-browser/document-details/new/${this.selectedItem.functionalityId}` : `/document-browser/document-details/${this.selectedItem.item.id}`
                this.$router.push(path)
            } else {
                let routeDocumentType = this.tabs[this.activeIndex - 1].item.mode ? this.tabs[this.activeIndex - 1].item.mode : this.getRouteDocumentType(this.tabs[this.activeIndex - 1].item)
                routeDocumentType ? this.$router.push(`/document-browser/${routeDocumentType}/` + id) : this.$router.push('/document-browser/new-dashboard')
            }
        },
        onItemSelect(payload: any) {
            if (payload.item) {
                payload.item.routerId = cryptoRandomString({ length: 16, type: 'base64' })
            }

            const tempItem = { ...payload, item: { ...payload.item } }
            if (payload.mode === 'documentDetail') tempItem.mode = 'documentDetail'

            this.tabs.push(tempItem)

            this.selectedItem = tempItem

            if (payload.mode === 'documentDetail') {
                const path = payload.functionalityId ? `/document-browser/document-details/new/${payload.functionalityId}` : `/document-browser/document-details/${payload.item.id}`
                this.selectedItem.item.showMode = 'documentDetail'
                this.$router.push(path)
            } else {
                const id = payload.item ? payload.item.label : 'new-dashboard'
                if (payload.item) {
                    let routeDocumentType = this.getRouteDocumentType(payload.item)
                    this.selectedItem.item.showMode = 'execute'
                    this.$router.push(`/document-browser/${routeDocumentType}/` + id)
                } else {
                    this.selectedItem.item = { routerId: cryptoRandomString({ length: 16, type: 'base64' }) }
                    this.selectedItem.item.showMode = 'createCockpit'
                    this.$router.push(`/document-browser/new-dashboard`)
                }
            }

            this.activeIndex = this.tabs.length
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
            let index = -1
            switch (mode) {
                case 'current':
                    this.tabs.splice(this.activeIndex - 1, 1)
                    this.activeIndex = 0
                    index = this.iFrameContainers.findIndex((iframe: any) => iframe.item?.routerId === this.selectedItem?.item.routerId)
                    if (index !== -1) this.iFrameContainers.splice(index, 1)
                    this.$router.push('/document-browser')
                    break
                case 'other':
                    this.tabs = [this.tabs[this.activeIndex - 1]]
                    this.activeIndex = 1
                    this.iFrameContainers = this.iFrameContainers.filter((iframe: any) => iframe.item?.routerId === this.selectedItem?.item.routerId)
                    break
                case 'right':
                    this.tabs.splice(this.activeIndex)
                    index = this.iFrameContainers.findIndex((iframe: any) => iframe.item?.routerId === this.selectedItem?.item.routerId)
                    if (index !== -1) this.iFrameContainers.splice(index + 1)
                    break
                case 'all':
                    this.$router.push('/document-browser')
                    this.tabs = []
                    this.activeIndex = 0
                    this.iFrameContainers = []
            }
        },
        onIFrameCreated(payload: any) {
            const index = this.iFrameContainers.findIndex((iframe: any) => iframe.item?.routerId === this.selectedItem?.item.routerId)
            if (index === -1) this.iFrameContainers.push(payload)
        },
        closeIframe() {
            const index = this.iFrameContainers.findIndex((iframe: any) => iframe.item?.routerId === this.selectedItem?.item.routerId)
            if (index !== -1) this.iFrameContainers.splice(index, 1)
        },
        loadSavedCockpit(cockpit: any) {
            this.closeIframe()
            this.selectedItem = { item: { ...cockpit, routerId: cryptoRandomString({ length: 16, type: 'base64' }), name: cockpit.DOCUMENT_NAME, label: cockpit.DOCUMENT_LABEL, showMode: 'execute' } }
            this.tabs[this.activeIndex - 1] = this.selectedItem
            this.$router.push(`/document-browser/document-composite/${cockpit.DOCUMENT_LABEL}`)
        },
        getTabName(tab: any) {
            if (tab.item && tab.item.name) {
                return tab.item.name
            } else {
                return tab.mode === 'documentDetail' ? 'new document' : 'new dashboard'
            }
        },
        onDocumentSaved(document: any) {
            this.documentSaved = document
            this.documentSavedTrigger = !this.documentSavedTrigger
            this.selectedItem.functionalityId = null
            this.selectedItem.item = { name: document.name, label: document.id, routerId: cryptoRandomString({ length: 16, type: 'base64' }), id: document.id, showMode: 'documentDetail' }
            this.$router.push(`/document-browser/document-details/${document.id}`)
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
    display: flex;
    flex-direction: column;
}

.document-browser-tab-container .p-tabview .p-tabview-panel,
.document-browser-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
}

.document-browser-cockpit-iframe {
    position: absolute;
    top: 39px;
    width: 100%;
    height: calc(100% - 39px);
    border: none;
}
</style>
