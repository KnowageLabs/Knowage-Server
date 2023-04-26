<template>
    <Sidebar v-model:visible="sidebarVisible" class="mySidebar" :show-close-icon="false" position="right" @hide="$emit('close')">
        <div id="sidebarItemsContainer" :style="descriptor.style.sidebarContainer">
            <div class="kn-toolbar kn-toolbar--default" :style="descriptor.style.sidebarToolbar">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button v-if="button.visible" v-tooltip.top="button.tooltip" :icon="button.icon" :class="button.class" @click="button.command" />
                </span>
            </div>
            <img v-if="viewType && document.previewFile && !descriptor.typesWithoutImages.includes(viewType)" class="p-mt-5" :style="descriptor.style.sidebarImage" align="center" :src="documentImageSource" />
            <div class="p-m-5">
                <div v-for="(field, index) of documentFields" :key="index" class="p-mb-5">
                    <h3 class="p-m-0">
                        <b>{{ $t(field.translation) }}</b>
                    </h3>
                    <p v-if="field.type === 'category' && datasetCategory" class="p-m-0">
                        {{ datasetCategory }}
                    </p>
                    <p v-if="field.type === 'date'" class="p-m-0">
                        {{
                            getFormattedDate(document[field.value], {
                                year: 'numeric',
                                month: '2-digit',
                                day: '2-digit',
                                hour: '2-digit',
                                minute: '2-digit',
                                second: '2-digit'
                            })
                        }}
                    </p>
                    <p v-if="field.type != 'date' && field.type != 'category'" class="p-m-0">{{ document[field.value] }}</p>
                </div>
            </div>
        </div>
    </Sidebar>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" :style="descriptor.style.menuItems" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import descriptor from './DetailSidebarDescriptor.json'
import Sidebar from 'primevue/sidebar'
import Menu from 'primevue/contextmenu'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'
import mainStore from '../../../App.store'
import UserFunctionalitiesConstants from '@/UserFunctionalitiesConstants.json'

export default defineComponent({
    name: 'workspace-sidebar',
    components: { Sidebar, Menu },
    props: { visible: Boolean, viewType: String, document: Object as any, isPrepared: Boolean, datasetCategories: Array as any },
    //prettier-ignore
    emits: ['close', 'executeRecent', 'executeDocumentFromOrganizer', 'moveDocumentToFolder', 'deleteDocumentFromOrganizer', 'executeAnalysisDocument', 'editAnalysisDocument', 'shareAnalysisDocument', 'cloneAnalysisDocument', 'deleteAnalysisDocument', 'uploadAnalysisPreviewFile', 'openDatasetInQBE', 'editDataset', 'previewDataset', 'deleteDataset', 'editDataset', 'exportToXlsx', 'exportToCsv', 'getHelp', 'downloadDatasetFile', 'shareDataset', 'cloneDataset', 'prepareData', 'openDataPreparation'],
    setup() {
        const store = mainStore()
        return { store }
    },

    data() {
        return {
            descriptor,
            sidebarVisible: false,
            menuButtons: [] as any
        }
    },
    computed: {
        isOwner(): any {
            return (this.store.$state as any).user.userId === this.document.creationUser
        },
        isAnalysisShared(): any {
            return this.document.functionalities.length > 1
        },
        isDatasetOwner(): any {
            return (this.store.$state as any).user.userId === this.document.owner
        },
        showQbeEditButton(): any {
            return (this.store.$state as any).user.userId === this.document.owner && (this.document.dsTypeCd == 'Federated' || this.document.dsTypeCd == 'Qbe')
        },
        datasetHasDrivers(): any {
            return this.document.drivers && this.document.length > 0
        },
        datasetHasParams(): any {
            return this.document.pars && this.document.pars > 0
        },
        datasetIsIterable(): any {
            // in order to export to XLSX, dataset must implement an iterator (BE side)
            const notIterableDataSets = ['Federated']
            if (notIterableDataSets.includes(this.document.dsTypeCd)) return false
            else return true
        },
        canLoadData(): any {
            if (this.document.actions) {
                for (let i = 0; i < this.document.actions.length; i++) {
                    const action = this.document.actions[i]
                    if (action.name == 'loaddata') {
                        return true
                    }
                }
            }
            return false
        },
        datasetCategory(): any {
            let category = ''
            if (this.datasetCategories) {
                this.datasetCategories.find((cat) => {
                    if (cat.VALUE_ID === this.document.catTypeId) {
                        category = cat.VALUE_CD
                    }
                })
            }
            return category
        },
        documentImageSource(): any {
            return import.meta.env.VITE_HOST_URL + descriptor.imgPath + this.document.previewFile
        },
        documentFields(): any {
            switch (this.viewType) {
                case 'recent':
                    return descriptor.defaultViewFields
                case 'repository':
                    return descriptor.defaultViewFields
                case 'dataset':
                    return descriptor.datasetViewFields
                case 'analysis':
                    return descriptor.analysisViewFields
                case 'businessModel':
                    return descriptor.businessModelViewFields
                case 'federationDataset':
                    return descriptor.federationDatasetViewFields
                default:
                    return []
            }
        },
        documentButtons(): any {
            switch (this.viewType) {
                case 'recent':
                    return [{ icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: () => this.emitEvent('executeRecent') }]
                case 'repository':
                    return [
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: () => this.emitEvent('executeDocumentFromOrganizer') },
                        { icon: 'fas fa-share', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.moveDoc'), visible: true, command: () => this.emitEvent('moveDocumentToFolder') },
                        { icon: 'fas fa-trash', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.deleteDoc'), visible: true, command: () => this.emitEvent('deleteDocumentFromOrganizer') }
                    ]
                case 'dataset':
                    return [
                        { icon: 'fas fa-eye', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.previewDs'), visible: this.canLoadData, command: () => this.emitEvent('previewDataset') },
                        { icon: 'fas fa-question-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.help'), visible: true, command: () => this.emitEvent('getHelp') },
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.other'), visible: true, command: this.showMenu }
                    ]
                case 'analysis':
                    return [
                        { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: () => this.emitEvent('executeAnalysisDocument') },
                        { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.editDoc'), visible: this.isOwner, command: () => this.emitEvent('editAnalysisDocument') },
                        { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.other'), visible: true, command: this.showMenu }
                    ]
                case 'businessModel':
                    return [{ icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.myModels.openInQBE'), visible: true, command: () => this.emitEvent('openDatasetInQBE') }]
                case 'federationDataset':
                    return [
                        { icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: () => this.emitEvent('openDatasetInQBE') },
                        { icon: 'pi pi-pencil', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: () => this.emitEvent('editDataset') },
                        { icon: 'fas fa-trash-alt', class: 'p-button-text p-button-rounded p-button-plain', visible: (this.store.$state as any).user.isSuperadmin || (this.store.$state as any).user.userId === this.document.owner, command: () => this.emitEvent('deleteDataset') }
                    ]
                default:
                    return []
            }
        }
    },
    watch: {
        visible() {
            this.sidebarVisible = this.visible
        }
    },
    created() {
        this.sidebarVisible = this.visible
    },
    methods: {
        showMenu(event) {
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        createMenuItems() {
            this.menuButtons = []
            if (this.viewType == 'analysis') {
                this.menuButtons.push(
                    { key: 1, label: this.$t('workspace.myAnalysis.menuItems.share'), icon: 'fas fa-share-alt', command: () => this.emitEvent('shareAnalysisDocument'), visible: !this.isAnalysisShared },
                    { key: 2, label: this.$t('workspace.myAnalysis.menuItems.unshare'), icon: 'fas fa-times-circle', command: () => this.emitEvent('shareAnalysisDocument'), visible: this.isAnalysisShared },
                    { key: 3, label: this.$t('workspace.myAnalysis.menuItems.clone'), icon: 'fas fa-clone', command: () => this.emitEvent('cloneAnalysisDocument') },
                    { key: 4, label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => this.emitEvent('deleteAnalysisDocument') },
                    { key: 5, label: this.$t('workspace.myAnalysis.menuItems.upload'), icon: 'fas fa-upload', command: () => this.emitEvent('uploadAnalysisPreviewFile') }
                )
            } else if (this.viewType == 'dataset') {
                let tmp = [] as any
                tmp.push({ key: 0, label: this.$t('workspace.myModels.editDataset'), icon: 'fas fa-pen', command: () => this.emitEvent('editDataset'), visible: this.isDatasetOwner && (this.document.dsTypeCd == 'File' || this.document.dsTypeCd == 'Prepared') })
                tmp.push({ key: 1, label: this.$t('workspace.myModels.openInQBE'), icon: 'fas fa-file-circle-question', command: () => this.emitEvent('openDatasetInQBE'), visible: this.isOpenInQBEVisible(this.document) })

                if ((this.store.$state as any).user?.functionalities.includes(UserFunctionalitiesConstants.DATA_PREPARATION)) {
                    tmp.push(
                        { key: 2, label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: () => this.emitEvent('openDataPreparation'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && this.document.pars && this.document.pars.length == 0 },
                        { key: 3, label: this.$t('workspace.myData.monitoring'), icon: 'fas fa-cogs', command: () => this.emitEvent('monitoring'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && this.document.pars && this.document.pars.length == 0 }
                    )
                }

                tmp.push({
                    key: 4,
                    label: this.$t('common.export'),
                    icon: 'fa-solid fa-file-export',
                    visible: this.canLoadData && !this.datasetHasDrivers && !this.datasetHasParams && this.document.dsTypeCd != 'File',
                    items: [
                        { key: 40, label: this.$t('workspace.myData.xlsxExport'), icon: 'fas fa-file-excel', command: () => this.emitEvent('exportToXlsx'), visible: this.datasetIsIterable },
                        { key: 41, label: this.$t('workspace.myData.csvExport'), icon: 'fas fa-file-csv', command: () => this.emitEvent('exportToCsv') }
                    ]
                })

                tmp.push({ key: 5, label: this.$t('workspace.myData.fileDownload'), icon: 'fas fa-download', command: () => this.emitEvent('downloadDatasetFile'), visible: this.document.dsTypeCd == 'File' })
                tmp.push({ key: 6, label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: () => this.emitEvent('shareDataset'), visible: this.canLoadData && this.isDatasetOwner })
                tmp.push({ key: 7, label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: () => this.emitEvent('cloneDataset'), visible: this.canLoadData && this.document.dsTypeCd == 'Qbe' })
                tmp.push({ key: 100, label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: () => this.emitEvent('deleteDataset'), visible: this.isDatasetOwner })

                tmp = tmp.sort((a, b) => a.key < b.key)
                tmp.forEach((element) => {
                    if (element.items) {
                        element.items = element.items.sort((a, b) => a.key < b.key)
                    }
                })
                this.menuButtons = tmp
            }
        },
        getFormattedDate(date: any, format: any) {
            return formatDateWithLocale(date, format)
        },
        emitEvent(event) {
            return () => this.$emit(event, this.document)
        },
        isOpenInQBEVisible(dataset: any) {
            return dataset.pars?.length == 0 && ((dataset.isPersisted && dataset.dsTypeCd == 'File') || dataset.dsTypeCd == 'Query' || dataset.dsTypeCd == 'Flat')
        }
    }
})
</script>
