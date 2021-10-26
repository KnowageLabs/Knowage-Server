<template>
    <Sidebar class="mySidebar" v-model:visible="sidenavVisible" :showCloseIcon="false" position="right" @hide="$emit('close')">
        <div id="sidebarItemsContainer" :style="descriptor.style.sidenavContainer">
            <div class="kn-toolbar kn-toolbar--default" :style="descriptor.style.sidenavToolbar">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button v-if="button.visible" :icon="button.icon" :class="button.class" />
                </span>
            </div>
            <img class="p-mt-5" :style="descriptor.style.sidenavImage" align="center" :src="documentImageSource" style="width:80%" />
            <div class="p-m-5">
                <div class="p-mb-5" v-for="(field, index) of documentFields" :key="index">
                    <h3 class="p-m-0">
                        <b>{{ $t(field.translation) }}</b>
                    </h3>
                    <p class="p-m-0" v-if="field.type === 'date'">{{ formatDate(selectedDocument[field.value]) }}</p>
                    <p class="p-m-0" v-else>{{ selectedDocument[field.value] }}</p>
                </div>
            </div>
        </div>
    </Sidebar>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuItems" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import Sidebar from 'primevue/sidebar'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    components: { Sidebar, Menu },
    emits: ['close'],
    props: { visible: Boolean, viewType: String, document: Object as any },
    computed: {
        isOwner(): any {
            return (this.$store.state as any).user.fullName === this.selectedDocument.creationUser
        },
        documentImageSource(): any {
            if (this.selectedDocument.previewFile) {
                return `${process.env.VUE_APP_HOST_URL}/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=${this.selectedDocument.previewFile}`
            }
            //DEFAULT IMAGE
            return `${process.env.VUE_APP_HOST_URL}/knowage/servlet/AdapterHTTP?ACTION_NAME=MANAGE_PREVIEW_FILE_ACTION&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=TRUE&operation=DOWNLOAD&fileName=82300081364511eca64e159ee59cd4dc.jpg`
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return descriptor.defaultViewFields
                case 'analysis':
                    return descriptor.analysisViewFields
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [{ icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', visible: true }]
                case 'analysis':
                    return [
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', visible: true },
                        { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', visible: this.isOwner },
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', visible: true }
                    ]
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        }
    },

    data() {
        return {
            descriptor,
            sidenavVisible: false,
            selectedDocument: {} as any,
            sidenavFields: null as any,
            menuItems: [
                { key: '0', label: this.$t('workspace.myAnalysis.menuItems.share'), icon: 'fas fa-share', command: () => {} },
                { key: '1', label: this.$t('workspace.myAnalysis.menuItems.clone'), icon: 'fas fa-clone', command: () => {} },
                { key: '2', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => {} },
                { key: '3', label: this.$t('workspace.myAnalysis.menuItems.upload'), icon: 'fas fa-share-alt', command: () => {} }
            ]
        }
    },
    created() {
        this.sidenavVisible = this.visible
        this.selectedDocument = this.document
    },
    watch: {
        visible() {
            this.sidenavVisible = this.visible
            this.selectedDocument = this.document
            console.log(this.selectedDocument)
        }
    },
    methods: {
        showMenu(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        }
    }
})
</script>
