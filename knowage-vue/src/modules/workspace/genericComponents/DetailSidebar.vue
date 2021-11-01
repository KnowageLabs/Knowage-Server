<template>
    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false" position="right" @hide="$emit('close')">
        <div id="sidebarItemsContainer" :style="descriptor.style.sidebarContainer">
            <div class="kn-toolbar kn-toolbar--default" :style="descriptor.style.sidebarToolbar">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button v-if="button.visible" :icon="button.icon" :class="button.class" @click="button.command" />
                </span>
            </div>
            <img class="p-mt-5" :style="descriptor.style.sidebarImage" align="center" :src="documentImageSource" style="width:80%" />
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
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import Sidebar from 'primevue/sidebar'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    name: 'workspace-sidebar',
    components: { Sidebar, Menu },
    emits: [
        'close',
        'executeRecent',
        'executeDocumentFromOrganizer',
        'moveDocumentToFolder',
        'deleteDocumentFromOrganizer',
        'executeAnalysisDocument',
        'editAnalysisDocument',
        'shareAnalysisDocument',
        'cloneAnalysisDocument',
        'deleteAnalysisDocument',
        'uploadAnalysisPreviewFile',
        'openDatasetInQBE',
        'editDataset',
        'deleteDataset'
    ],
    props: { visible: Boolean, viewType: String, document: Object as any },
    computed: {
        isOwner(): any {
            return (this.$store.state as any).user.fullName === this.selectedDocument.creationUser
        },
        documentImageSource(): any {
            if (this.selectedDocument.previewFile) {
                return process.env.VUE_APP_HOST_URL + descriptor.imgPath + this.selectedDocument.previewFile
            }
            //DEFAULT IMAGE
            return process.env.VUE_APP_HOST_URL + descriptor.imgPath + `82300081364511eca64e159ee59cd4dc.jpg`
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return descriptor.defaultViewFields
                case 'repository':
                    return descriptor.defaultViewFields
                case 'analysis':
                    return descriptor.analysisViewFields
                case 'businessModel':
                    return descriptor.businessModelViewFields
                case 'federationDataset':
                    return descriptor.federationDatasetViewFields
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [{ icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeRecent') }]
                case 'repository':
                    return [
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeDocumentFromOrganizer') },
                        { icon: 'fas fa-share', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('moveDocumentToFolder') },
                        { icon: 'fas fa-trash', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('deleteDocumentFromOrganizer') }
                    ]
                case 'dataset':
                    return [
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-info-circle', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-eye', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('previewDataset') }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitExecuteAnalysisDocument },
                        { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', visible: this.isOwner, command: this.emitEditAnalysisDocument },
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu }
                    ]
                case 'businessModel':
                    return [{ icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitOpenDatasetInQBE }]
                case 'federationDataset':
                    return [
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitOpenDatasetInQBE },
                        { icon: 'pi pi-pencil', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEditDataset },
                        { icon: 'fas fa-trash-alt', class: 'p-button-text p-button-rounded p-button-plain', visible: (this.$store.state as any).user.isSuperadmin || (this.$store.state as any).user.userId === this.selectedDocument.owner, command: this.emitDeleteDataset }
                    ]
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        menuButtons(): any {
            return [
                {
                    key: '0',
                    label: this.$t('workspace.myAnalysis.menuItems.share'),
                    icon: 'fas fa-share',
                    command: () => {
                        this.$emit('shareAnalysisDocument', this.selectedDocument)
                    }
                },
                {
                    key: '1',
                    label: this.$t('workspace.myAnalysis.menuItems.clone'),
                    icon: 'fas fa-clone',
                    command: () => {
                        this.$emit('cloneAnalysisDocument', this.selectedDocument)
                    }
                },
                {
                    key: '2',
                    label: this.$t('workspace.myAnalysis.menuItems.delete'),
                    icon: 'fas fa-trash',
                    command: () => {
                        this.$emit('deleteAnalysisDocument', this.selectedDocument)
                    }
                },
                {
                    key: '3',
                    label: this.$t('workspace.myAnalysis.menuItems.upload'),
                    icon: 'fas fa-share-alt',
                    command: () => {
                        this.$emit('uploadAnalysisPreviewFile', this.selectedDocument)
                    }
                }
            ]
        }
    },

    data() {
        return {
            descriptor,
            sidebarVisible: false,
            selectedDocument: {} as any,
            sidebarFields: null as any
        }
    },
    created() {
        this.sidebarVisible = this.visible
        this.selectedDocument = this.document
    },
    watch: {
        visible() {
            this.sidebarVisible = this.visible
            this.selectedDocument = this.document
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
        },
        emitEvent(event) {
            return () => this.$emit(event, this.document)
        },
        //iz nekog razloga ne mogu samo da stavim this.$emit(), direkno u komandi dugmeta, ako to odradim pozivaju se emiteri samo kada se kreira komponenta, posle toga ne, al ovako radi
        emitExecuteRecent() {
            this.$emit('executeRecent', this.selectedDocument)
        },
        emitExecuteDocumentFromOrganizer() {
            this.$emit('executeDocumentFromOrganizer', this.selectedDocument)
        },
        emitMoveDocumentToFolder() {
            this.$emit('moveDocumentToFolder', this.selectedDocument)
        },
        emitDeleteDocumentFromOrganizer() {
            this.$emit('deleteDocumentFromOrganizer', this.selectedDocument)
        },
        emitExecuteAnalysisDocument() {
            this.$emit('executeAnalysisDocument', this.selectedDocument)
        },
        emitEditAnalysisDocument() {
            this.$emit('editAnalysisDocument', this.selectedDocument)
        },
        emitOpenDatasetInQBE() {
            this.$emit('openDatasetInQBE', this.selectedDocument)
        },
        emitEditDataset() {
            this.$emit('editDataset', this.selectedDocument)
        },
        emitDeleteDataset() {
            this.$emit('deleteDataset', this.selectedDocument)
        }
    }
})
</script>
