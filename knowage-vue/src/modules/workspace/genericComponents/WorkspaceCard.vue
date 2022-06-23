<template>
    <div class="p-col-12 p-md-6 p-lg-4 p-xl-3" tabindex="0">
        <div class="card-container">
            <span class="details-container">
                <div class="p-ml-3 detail-type" role="type">
                    {{ document[documentFields.type] }}
                </div>
                <div class="p-ml-3 detail-info">
                    <h4 class="p-m-0 kn-truncated" role="title" v-tooltip="document[documentFields.label]">
                        {{ document[documentFields.label] }}
                    </h4>
                    <p class="p-m-0 kn-truncated" v-tooltip="document[documentFields.name]">{{ document[documentFields.name] }}</p>
                </div>
                <div class="detail-buttons">
                    <template v-for="(button, index) of documentButtons" :key="index">
                        <Button :id="button.id" v-if="button.visible" :icon="button.icon" class="p-mx-1 p-button-text p-button-rounded p-button-plain p-button-lg" @click="button.command" v-tooltip="$t(button.label)" :aria-label="$t(button.label)" />
                    </template>
                </div>
            </span>
            <div aria-hidden="true" v-if="document[documentFields.image]" class="card-image" :style="[documentImageSource]" />
            <div v-else aria-hidden="true" class="card-image" :style="[documentImageSource]" />
        </div>
    </div>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import cardDescriptor from './WorkspaceCardDescriptor.json'
import Menu from 'primevue/contextmenu'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'workspace-sidebar',
    components: { Menu },
    //prettier-ignore
    emits: ['executeRecent','executeDocumentFromOrganizer','moveDocumentToFolder','deleteDocumentFromOrganizer','executeAnalysisDocument','editAnalysisDocument','shareAnalysisDocument','cloneAnalysisDocument','deleteAnalysisDocument','uploadAnalysisPreviewFile','openDatasetInQBE','editDataset','deleteDataset','previewDataset','deleteDataset','editDataset','exportToXlsx','exportToCsv','getHelp','downloadDatasetFile','shareDataset','openSidebar', 'cloneDataset', 'prepareData', 'openDataPreparation'],
    props: { visible: Boolean, viewType: String, document: Object as any, isPrepared: Boolean },
    computed: {
        isOwner(): any {
            return (this.store.$state as any).user.fullName === this.document.creationUser
        },

        isAnalysisShared(): any {
            return this.document.functionalities.length > 1
        },
        isDatasetOwner(): any {
            return (this.store.$state as any).user.fullName === this.document.owner
        },
        showQbeEditButton(): any {
            return (this.store.$state as any).user.fullName === this.document.owner && (this.document.dsTypeCd == 'Federated' || this.document.dsTypeCd == 'Qbe')
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
                    'background-image': `url(${import.meta.env.VITE_HOST_URL}${descriptor.imgPath}${this.document[this.documentFields.image]}),url(${require('@/assets/images/workspace/documentTypes/' + cardDescriptor.defaultImages.missing)})`
                }
            }
            return {
                'background-image': `url(${require('@/assets/images/workspace/documentTypes/' + (cardDescriptor.defaultImages[this.document.type] || cardDescriptor.defaultImages.missing))})`
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
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fas fa-play-circle', visible: true, command: this.emitEvent('executeRecent'), label: 'common.execute' }
                    ]
                case 'repository':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', visible: true, command: this.showMenu, label: 'common.menu' },
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fas fa-play-circle', visible: true, command: this.emitEvent('executeDocumentFromOrganizer'), label: 'common.execute' }
                    ]
                case 'dataset':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', visible: true, command: this.showMenu, label: 'common.menu' },
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fas fa-eye', visible: true, command: this.emitEvent('previewDataset'), label: 'common.details' }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', visible: true, command: this.showMenu, label: 'common.menu' },
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fas fa-play-circle', visible: true, command: this.emitEvent('executeAnalysisDocument'), label: 'common.execute' }
                    ]
                case 'businessModel':
                    return [
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fa fa-search', visible: true, command: this.emitEvent('openDatasetInQBE'), label: 'common.execute' }
                    ]
                case 'federationDataset':
                    return [
                        { icon: 'fas fa-ellipsis-v', id: 'list-button', visible: true, command: this.showMenu, label: 'common.menu' },
                        { icon: 'fas fa-info-circle', id: 'list-button', visible: true, command: this.emitEvent('openSidebar'), label: 'common.details' },
                        { icon: 'fa fa-search', visible: true, command: this.emitEvent('openDatasetInQBE'), label: 'common.execute' }
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
    setup() {
        const store = mainStore()
        return { store }
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
                let tmp = [] as any
                tmp.push(
                    { key: '0', label: this.$t('workspace.myAnalysis.menuItems.showDsDetails'), icon: 'fas fa-pen', command: this.emitEvent('editDataset'), visible: this.isDatasetOwner && (this.document.dsTypeCd == 'File' || this.document.dsTypeCd == 'Prepared')},
                    { key: '1', label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-pen', command: this.emitEvent('openDatasetInQBE'), visible: this.showQbeEditButton },
                    { key: '2', label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: this.emitEvent('exportToXlsx'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.document.dsTypeCd != 'File' && this.datasetIsIterable },
                    { key: '3', label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: this.emitEvent('exportToCsv'), visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.document.dsTypeCd != 'File' },
                    { key: '4', label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: this.emitEvent('downloadDatasetFile'), visible: this.document.dsTypeCd == 'File' },
                    { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: this.emitEvent('shareDataset'), visible: this.canLoadData && this.isDatasetOwner },
                    { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: this.emitEvent('cloneDataset'), visible: this.canLoadData && this.document.dsTypeCd == 'Qbe' },
                    { key: '9', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: this.emitEvent('deleteDataset'), visible: this.isDatasetOwner }
                )

                if ((this.store.$state as any).user?.functionalities.includes('DataPreparation')) {
                    tmp.push(
                        { key: '7', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: this.emitEvent('openDataPreparation'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && (this.document.pars && this.document.pars.length == 0) },
                        { key: '8', label: this.$t('workspace.myData.monitoring'), icon: 'fas fa-cogs', command: this.emitEvent('monitoring'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && (this.document.pars && this.document.pars.length == 0) }
                    )
                }
                
                tmp = tmp.sort((a,b)=>a.key.localeCompare(b.key))
                this.menuButtons = tmp

            } else if (this.viewType === 'federationDataset') {
                this.menuButtons.push( 
                    { key: '0', icon: 'pi pi-pencil', label: this.$t('workspace.myModels.editDataset'), class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('editDataset') },
                    { key: '1', icon: 'fas fa-trash-alt', label: this.$t('workspace.myModels.deleteDataset'), class: 'p-button-text p-button-rounded p-button-plain', visible: (this.store.$state as any).user.isSuperadmin || (this.store.$state as any).user.userId === this.document.owner, command: this.emitEvent('deleteDataset') })
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
.card-container {
    display: flex;
    flex-direction: row;
    position: relative;
    border-radius: 0;
    overflow: hidden;
    height: 120px;
    padding: 0;
    border: 1px solid var(--kn-color-borders);
    .details-container {
        order: 1;
        -webkit-clip-path: polygon(0 0, 0 100%, 70% 101%, 90% 0);
        clip-path: polygon(0 0, 0 100%, 70% 101%, 90% 0);
        background-color: white;
        height: 100%;
        z-index: 2;
        width: 100%;
        .detail-type {
            text-transform: uppercase;
            font-size: 0.8rem;
            font-weight: bold;
            color: grey;
            margin-top: 1rem;
        }
        .detail-info {
            width: 70%;
        }

        .detail-buttons {
            position: absolute;
            left: 0;
            bottom: 0;
        }
    }
    .card-image {
        position: absolute;
        right: 0;
        top: 0;
        order: 2;
        height: 100%;
        z-index: 1;
        width: 200px;
    }
}

@media screen and (max-width: 576px) {
    .card-container {
        .details-container {
            -webkit-clip-path: none;
            clip-path: none;
            .detail-info {
                width: 100%;
                h4,
                p {
                    white-space: normal;
                }
            }
        }
        .card-image {
            display: none;
        }
    }

    #list-button {
        display: none;
    }
}
</style>
