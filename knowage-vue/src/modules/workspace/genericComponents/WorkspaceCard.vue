<template>
    <div class="card-container p-col-12 p-md-4 p-lg-3" :style="cardDescriptor.style.cardContainer">
        <img v-if="document[documentFields.image]" class="card-image" onerror="this.src='https://i.imgur.com/9YqXpxc.jpeg'" :src="documentImageSource" :style="cardDescriptor.style.cardImage" />
        <!-- TODO: insert the default image source -->
        <img v-else class="card-image default" src="https://i.imgur.com/9YqXpxc.jpeg" :style="cardDescriptor.style.cardImage" />
        <!--  -->
        <span class="details-container" :style="cardDescriptor.style.detailsContainer">
            <div class="type-container" :style="cardDescriptor.style.typeContainer">
                <p class="p-mb-1">{{ document[documentFields.type] }}</p>
            </div>
            <div class="name-container" :style="cardDescriptor.style.nameContainer">
                <h4 class="p-m-0">
                    <b>{{ document[documentFields.label] }}</b>
                </h4>
                <p class="p-m-0">{{ document[documentFields.name] }}</p>
            </div>
            <div class="button-container" :style="cardDescriptor.style.buttonContainer">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button v-if="button.visible" :icon="button.icon" :class="button.class" :style="cardDescriptor.style.icon" @click="button.command" />
                </span>
            </div>
        </span>
    </div>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import cardDescriptor from './WorkspaceCardDescriptor.json'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    name: 'workspace-sidebar',
    components: { Menu },
    emits: [
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
            if (this.selectedDocument[this.documentFields.image]) {
                return process.env.VUE_APP_HOST_URL + descriptor.imgPath + this.selectedDocument[this.documentFields.image]
            }
            //DEFAULT IMAGE
            return process.env.VUE_APP_HOST_URL + descriptor.imgPath + `82300081364511eca64e159ee59cd4dc.jpg`
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return cardDescriptor.defaultViewFields
                case 'repository':
                    return cardDescriptor.defaultViewFields
                case 'analysis':
                    return cardDescriptor.analysisViewFields
                case 'businessModel':
                    return cardDescriptor.businessModelViewFields
                case 'federationDataset':
                    return cardDescriptor.federationDatasetViewFields
                default:
                    return console.log('How did this happen, no valid file type.')
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [{ icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitExecuteRecent }]
                case 'repository':
                    return [
                        { icon: 'fas fa-trash', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitDeleteDocumentFromOrganizer },
                        { icon: 'fas fa-share', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitMoveDocumentToFolder },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitExecuteDocumentFromOrganizer }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', visible: this.isOwner, command: this.emitEditAnalysisDocument },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitExecuteAnalysisDocument }
                    ]
                case 'businessModel':
                    return [{ icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitOpenDatasetInQBE }]
                case 'federationDataset':
                    return [
                        { icon: 'fas fa-trash-alt', class: 'p-button-text p-button-rounded p-button-plain', visible: (this.$store.state as any).user.isSuperadmin || (this.$store.state as any).user.userId === this.selectedDocument.owner, command: this.emitDeleteDataset },
                        { icon: 'pi pi-pencil', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEditDataset },
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitOpenDatasetInQBE }
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
            cardDescriptor,
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
        returnDefaultImage() {
            return process.env.VUE_APP_HOST_URL + descriptor.imgPath + `82300081364511eca64e159ee59cd4dc.jpg`
        },
        showMenu(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
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
        },
        logDoc() {
            console.log(this.selectedDocument)
        }
    }
})
</script>
<style lang="scss" scoped>
@media screen and (max-width: 768px) {
    .card-image {
        display: none;
    }
    .details-container {
        border-radius: 10px;
    }
}
</style>
