<template>
    <Sidebar class="mySidebar" v-model:visible="sidebarVisible" :showCloseIcon="false" position="right" @hide="$emit('close')">
        <div id="sidebarItemsContainer" :style="descriptor.style.sidebarContainer">
            <div class="kn-toolbar kn-toolbar--default" :style="descriptor.style.sidebarToolbar">
                <span v-for="(button, index) of documentButtons" :key="index">
                    <Button v-if="button.visible" :icon="button.icon" :class="button.class" @click="button.command" v-tooltip.top="button.tooltip" />
                </span>
            </div>
            <!-- TODO: Change default image to your liking -->
            <img v-if="viewType != 'businessModel' && viewType != 'analysis'" class="p-mt-5" onerror="this.src='https://i.imgur.com/9N1aRkx.png'" :style="descriptor.style.sidebarImage" align="center" :src="documentImageSource" />
            <div class="p-m-5">
                <div class="p-mb-5" v-for="(field, index) of documentFields" :key="index">
                    <h3 class="p-m-0">
                        <b>{{ $t(field.translation) }}</b>
                    </h3>
                    <p class="p-m-0" v-if="field.type === 'category' && datasetCategory">
                        {{ datasetCategory }}
                    </p>
                    <p class="p-m-0" v-if="field.type === 'date'">{{ getFormattedDate(document[field.value], 'MM/DD/YYYY hh:mm:ss') }}</p>
                    <p class="p-m-0" v-if="field.type != 'date' && field.type != 'category'">{{ document[field.value] }}</p>
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
    import { formatDate } from '@/helpers/commons/localeHelper'

    export default defineComponent({
        name: 'workspace-sidebar',
        components: { Sidebar, Menu },
        //prettier-ignore
        emits: ['close','executeRecent','executeDocumentFromOrganizer','moveDocumentToFolder','deleteDocumentFromOrganizer','executeAnalysisDocument','editAnalysisDocument','shareAnalysisDocument','cloneAnalysisDocument','deleteAnalysisDocument','uploadAnalysisPreviewFile','openDatasetInQBE','editDataset','previewDataset','deleteDataset','editFileDataset','exportToXlsx','exportToCsv','getHelp','downloadDatasetFile','shareDataset','cloneDataset', 'prepareData', 'openDataPreparation'],
        props: { visible: Boolean, viewType: String, document: Object as any, isPrepared: Boolean, datasetCategories: Array as any },
        computed: {
            isOwner(): any {
                return (this.$store.state as any).user.userId === this.document.creationUser
            },
            isAnalysisShared(): any {
                return this.document.functionalities.length > 1
            },
            isDatasetOwner(): any {
                return (this.$store.state as any).user.userId === this.document.owner
            },
            showQbeEditButton(): any {
                return (this.$store.state as any).user.userId === this.document.owner && (this.document.dsTypeCd == 'Federated' || this.document.dsTypeCd == 'Qbe')
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
                if (this.document.previewFile) {
                    return process.env.VUE_APP_HOST_URL + descriptor.imgPath + this.document.previewFile
                }
                //TODO: Change default image to your liking
                return process.env.VUE_APP_HOST_URL + descriptor.imgPath + descriptor.imgSource
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
                        return [{ icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: this.emitEvent('executeRecent') }]
                    case 'repository':
                        return [
                            { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: this.emitEvent('executeDocumentFromOrganizer') },
                            { icon: 'fas fa-share', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.moveDoc'), visible: true, command: this.emitEvent('moveDocumentToFolder') },
                            { icon: 'fas fa-trash', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.deleteDoc'), visible: true, command: this.emitEvent('deleteDocumentFromOrganizer') }
                        ]
                    case 'dataset':
                        return [
                            { icon: 'fas fa-eye', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.previewDs'), visible: this.canLoadData, command: this.emitEvent('previewDataset') },
                            { icon: 'fas fa-question-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.help'), visible: true, command: this.emitEvent('getHelp') },
                            { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.other'), visible: true, command: this.showMenu }
                        ]
                    case 'analysis':
                        return [
                            { icon: 'fas fa-play-circle', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.executeDoc'), visible: true, command: this.emitEvent('executeAnalysisDocument') },
                            { icon: 'fas fa-edit', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.editDoc'), visible: this.isOwner, command: this.emitEvent('editAnalysisDocument') },
                            { icon: 'fas fa-ellipsis-v', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.buttonsTooltips.other'), visible: true, command: this.showMenu }
                        ]
                    case 'businessModel':
                        return [{ icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', tooltip: this.$t('workspace.myModels.openInQBE'), visible: true, command: this.emitEvent('openDatasetInQBE') }]
                    case 'federationDataset':
                        return [
                            { icon: 'fa fa-search', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('openDatasetInQBE') },
                            { icon: 'pi pi-pencil', class: 'p-button-text p-button-rounded p-button-plain', visible: true, command: this.emitEvent('editDataset') },
                            { icon: 'fas fa-trash-alt', class: 'p-button-text p-button-rounded p-button-plain', visible: (this.$store.state as any).user.isSuperadmin || (this.$store.state as any).user.userId === this.document.owner, command: this.emitEvent('deleteDataset') }
                        ]
                    default:
                        return []
                }
            }
        },

        data() {
            return {
                descriptor,
                sidebarVisible: false,
                menuButtons: [] as any
            }
        },
        created() {
            this.sidebarVisible = this.visible
        },
        watch: {
            visible() {
                this.sidebarVisible = this.visible
            }
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
                        { key: '5', label: this.$t('workspace.myData.shareDataset'), icon: 'fas fa-share-alt', command: this.emitEvent('shareDataset'), visible: this.canLoadData && this.isDatasetOwner && this.document.dsTypeCd != 'Prepared' },
                        { key: '6', label: this.$t('workspace.myData.cloneDataset'), icon: 'fas fa-clone', command: this.emitEvent('cloneDataset'), visible: this.canLoadData && this.document.dsTypeCd == 'Qbe' },
                        { key: '7', label: this.$t('workspace.myData.openDataPreparation'), icon: 'fas fa-cogs', command: this.emitEvent('openDataPreparation'), visible: this.canLoadData && this.document.dsTypeCd != 'Qbe' && this.document.pars && this.document.pars.length == 0 },
                        { key: '9', label: this.$t('workspace.myData.deleteDataset'), icon: 'fas fa-trash', command: this.emitEvent('deleteDataset'), visible: this.isDatasetOwner }
                    )
                }
            },
            getFormattedDate(date: any, format: any) {
                return formatDate(date, format)
            },
            emitEvent(event) {
                return () => this.$emit(event, this.document)
            }
        }
    })
</script>
