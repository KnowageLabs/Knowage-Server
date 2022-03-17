<template>
    <div class="card-container p-col-12 p-md-6 p-lg-4 p-xl-3" :style="cardDescriptor.style.cardContainer">
        <div v-if="document[documentFields.image]" class="card-image" :style="[documentImageSource, cardDescriptor.style.cardImage]" />
        <div v-else class="card-image" :style="[documentImageSource, cardDescriptor.style.cardImage]" />
        <span class="details-container" :style="cardDescriptor.style.detailsContainer">
            <div class="p-mr-3" :style="cardDescriptor.style.typeContainer">
                <p>{{ document[documentFields.type] }}</p>
            </div>
            <div class="p-ml-3">
                <h4 class="p-m-0" :style="cardDescriptor.style.nameContainerText" v-tooltip="document[documentFields.label]">
                    <b>{{ document[documentFields.label] }}</b>
                </h4>
                <p class="p-m-0" :style="cardDescriptor.style.nameContainerText" v-tooltip="document[documentFields.name]">{{ document[documentFields.name] }}</p>
            </div>
            <span :style="cardDescriptor.style.buttonContainer">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button :id="button.id" class="p-mx-1" v-if="button.visible" :icon="button.icon" :class="button.class" :style="cardDescriptor.style.icon" @click="button.command" />
                </span>
            </span>
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
    //prettier-ignore
    emits: ['executeRecent','executeDocumentFromOrganizer','moveDocumentToFolder','deleteDocumentFromOrganizer','executeAnalysisDocument','editAnalysisDocument','shareAnalysisDocument','cloneAnalysisDocument','deleteAnalysisDocument','uploadAnalysisPreviewFile','openDatasetInQBE','editDataset','deleteDataset','previewDataset','deleteDataset','editFileDataset','exportToXlsx','exportToCsv','getHelp','downloadDatasetFile','shareDataset','openSidebar', 'cloneDataset', 'prepareData', 'openDataPreparation'],
    props: { visible: Boolean, viewType: String, document: Object as any, isPrepared: Boolean },
    computed: {
        isOwner(): any {
            return (this.$store.state as any).user.fullName === this.document.creationUser
        },

        isAnalysisShared(): any {
            return this.document.functionalities.length > 1
        },
        isDatasetOwner(): any {
            return (this.$store.state as any).user.fullName === this.document.owner
        },
        showQbeEditButton(): any {
            return (this.$store.state as any).user.fullName === this.document.owner && (this.document.dsTypeCd == 'Federated' || this.document.dsTypeCd == 'Qbe')
        },
        datasetHasDrivers(): any {
            return this.document.drivers && this.document.length > 0
        },
        datasetHasParams(): any {
            return this.document.pars && this.document.pars > 0
        },
        datasetIsIterable(): any {
            // in order to export to XLSX, dataset must implement an iterator (BE side)
            let notIterableDataSets = ['Federated']
            if (notIterableDataSets.includes(this.document.dsTypeCd)) return false
            else return true
        },
        canLoadData(): any {
            if (this.document.actions) {
                for (var i = 0; i < this.document.actions.length; i++) {
                    var action = this.document.actions[i]
                    if (action.name == 'loaddata') {
                        return true
                    }
                }
            }
            return false
        },
        documentImageSource(): any {
            if (this.document[this.documentFields.image]) {
                return {
                    //TODO: 2nd image is the fallback in case there is an error iwth source image --- url(imgSource)url(fallbackImg), Change default image to your liking
                    'background-image': `url(${process.env.VUE_APP_HOST_URL}${descriptor.imgPath}${this.document[this.documentFields.image]}),url(https://i.imgur.com/9N1aRkx.png)`
                }
            }
            return {
                //TODO: Change default image to your liking
                'background-image': `url(https://i.imgur.com/9N1aRkx.png)`
            }
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return cardDescriptor.defaultViewFields
                case 'repository':
                    return cardDescriptor.defaultViewFields
                case 'dataset':
                    return cardDescriptor.datasetViewFields
                case 'analysis':
                    return cardDescriptor.analysisViewFields
                case 'businessModel':
                    return cardDescriptor.businessModelViewFields
                case 'federationDataset':
                    return cardDescriptor.federationDatasetViewFields
                default:
                    return []
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeRecent') }
                    ]
                case 'repository':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeDocumentFromOrganizer') }
                    ]
                case 'dataset':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-eye', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('previewDataset') }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('executeAnalysisDocument') }
                    ]
                case 'businessModel':
                    return [
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('openDatasetInQBE') }
                    ]
                case 'federationDataset':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.showMenu },
                        { icon: 'fas fa-info-circle', id: 'list-button', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openSidebar') },
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded', visible: true, command: this.emitEvent('openDatasetInQBE') }
                    ]
                default:
                    return []
            }
        }
    },

    data() {
        return {
            cardDescriptor,
            sidebarVisible: false,
            menuButtons: [] as any
        }
    },
    methods: {
        showMenu(event) {
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        emitEvent(event) {
            return () => this.$emit(event, this.document)
        },
        // prettier-ignore
        createMenuItems() {
            this.menuButtons = []
            if (this.viewType == 'analysis') {
                this.menuButtons.push(
                    { key: '0', label: this.$t('workspace.myAnalysis.menuItems.edit'), icon: 'fas fa-edit', command: this.emitEvent('editAnalysisDocument'), visible: this.isOwner },
                    { key: '1', label: this.$t('workspace.myAnalysis.menuItems.share'), icon: 'fas fa-share-alt', command: this.emitEvent('shareAnalysisDocument'), visible: !this.isAnalysisShared },
                    { key: '2', label: this.$t('workspace.myAnalysis.menuItems.unshare'), icon: 'fas fa-times-circle', command: this.emitEvent('shareAnalysisDocument'), visible: this.isAnalysisShared },
                    { key: '3', label: this.$t('workspace.myAnalysis.menuItems.clone'), icon: 'fas fa-clone', command: this.emitEvent('cloneAnalysisDocument') },
                    { key: '4', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: this.emitEvent('deleteAnalysisDocument') },
                    { key: '5', label: this.$t('workspace.myAnalysis.menuItems.upload'), icon: 'fas fa-upload', command: this.emitEvent('uploadAnalysisPreviewFile') }
                )
            } else if (this.viewType == 'dataset') {
                this.menuButtons.push(
                    { key: '0', label: this.$t('workspace.myAnalysis.menuItems.showDsDetails'), icon: 'fas fa-pen', command: this.emitEvent('editFileDataset'), visible: this.isDatasetOwner && this.document.dsTypeCd == 'File' },
                    { key: '1', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: this.emitEvent('openDatasetInQBE'), visible: this.showQbeEditButton },
                    { key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: this.emitEvent('exportToXlsx'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.document.dsTypeCd != 'File' && this.datasetIsIterable },
                    { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: this.emitEvent('exportToCsv'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.document.dsTypeCd != 'File' },
                    { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: this.emitEvent('downloadDatasetFile'), visible: this.document.dsTypeCd == 'File' },
                    { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: this.emitEvent('shareDataset'), visible: this.canLoadData && this.isDatasetOwner },
                    { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: this.emitEvent('cloneDataset'), visible: this.canLoadData && this.document.dsTypeCd == 'Qbe' },
                    { key: '7', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: this.emitEvent('openDataPreparation'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && (this.document.pars && this.document.pars.length == 0) },
                    { key: '8', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: this.emitEvent('deleteDataset'), visible: this.isDatasetOwner }
                )
            } else if (this.viewType === 'federationDataset') {
                this.menuButtons.push( 
                    { key: '0', icon: 'pi pi-pencil', label: this.$t('workspace.myModels.editDataset'), class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('editDataset') },
                    { key: '1', icon: 'fas fa-trash-alt', label: this.$t('workspace.myModels.deleteDataset'), class: 'p-button-text p-button-rounded p-button-plain', visible: (this.$store.state as any).user.isSuperadmin || (this.$store.state as any).user.userId === this.document.owner, command: this.emitEvent('deleteDataset') })
            } else if (this.viewType === 'repository') {
                this.menuButtons.push(
                    { key: '3', label: this.$t('workspace.myRepository.moveDocument'), icon: 'fas fa-share', command:  this.emitEvent('moveDocumentToFolder') },
                    { key: '4', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command:   this.emitEvent('deleteDocumentFromOrganizer') },
            )
            }
        }
    }
})
</script>
<style lang="scss" scoped>
@media screen and (max-width: 576px) {
    .card-image {
        display: none;
    }
    .details-container {
        border-radius: 10px;
    }
    #list-button {
        display: none;
    }
}
</style>
