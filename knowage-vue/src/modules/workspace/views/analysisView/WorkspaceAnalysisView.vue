<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myAnalysis') }}
        </template>
        <template #end>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="$emit('toggleDisplayView')" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="$emit('toggleDisplayView')" />
            <KnFabButton v-if="addButtonIsVisible" icon="fas fa-plus" data-test="new-folder-button" @click="showCreationMenu" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <InputText class="kn-material-input p-m-2" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />

    <div class="p-m-2 kn-overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table p-mx-2" :value="filteredAnalysisDocuments" :loading="loading" dataKey="id" responsiveLayout="stack" breakpoint="600px" data-test="analysis-table">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #filter="{ filterModel }">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
            <Column field="name" :header="$t('importExport.gallery.column.name')" :sortable="true" />
            <Column field="creationUser" :header="$t('kpi.targetDefinition.kpiAuthor')" :sortable="true" />
            <Column field="creationDate" :header="$t('kpi.targetDefinition.kpiDate')" :sortable="true">
                <template #body="{ data }">
                    {{ formatDate(data.creationDate) }}
                </template>
            </Column>
            <Column :style="mainDescriptor.style.iconColumn">
                <template #body="slotProps">
                    <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="showMenu($event, slotProps.data)" />
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click="showSidebar(slotProps.data)" :data-test="'info-button-' + slotProps.data.name" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="executeAnalysisDocument(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2" data-test="card-container">
            <Message v-if="filteredAnalysisDocuments.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard
                    v-for="(document, index) of filteredAnalysisDocuments"
                    :key="index"
                    :viewType="'analysis'"
                    :document="document"
                    @executeAnalysisDocument="executeAnalysisDocument"
                    @editAnalysisDocument="openKpiDesigner"
                    @shareAnalysisDocument="shareAnalysisDocument"
                    @cloneAnalysisDocument="cloneAnalysisDocument"
                    @deleteAnalysisDocument="deleteAnalysisDocumentConfirm"
                    @uploadAnalysisPreviewFile="uploadAnalysisPreviewFile"
                    @openSidebar="showSidebar"
                />
            </template>
        </div>
    </div>
    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'analysis'"
        :document="selectedAnalysis"
        @executeAnalysisDocument="executeAnalysisDocument"
        @editAnalysisDocument="editAnalysisDocument"
        @shareAnalysisDocument="shareAnalysisDocument"
        @cloneAnalysisDocument="cloneAnalysisDocumentConfirm"
        @deleteAnalysisDocument="deleteAnalysisDocumentConfirm"
        @uploadAnalysisPreviewFile="uploadAnalysisPreviewFile"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
    />
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <Menu id="creationMenu" ref="creationMenu" :model="creationMenuButtons" />

    <WorkspaceAnalysisViewShareDialog :visible="shareDialogVisible" :propFolders="folders" @close="shareDialogVisible = false" @share="handleAnalysShared($event, false)"></WorkspaceAnalysisViewShareDialog>
    <WorkspaceAnalysisViewEditDialog :visible="editDialogVisible" :propAnalysis="selectedAnalysis" @close="editDialogVisible = false" @save="handleEditAnalysis"></WorkspaceAnalysisViewEditDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :title="$t('workspace.menuLabels.myAnalysis')" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>

    <KnInputFile v-if="!uploading" :changeFunction="uploadAnalysisFile" accept="image/*" :triggerInput="triggerUpload" />
    <WorkspaceCockpitDialog :visible="cockpitDialogVisible" @close="closeCockpitDialog"></WorkspaceCockpitDialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import DataTable from 'primevue/datatable'
import Menu from 'primevue/contextmenu'
import Message from 'primevue/message'
import Column from 'primevue/column'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import WorkspaceAnalysisViewEditDialog from './dialogs/WorkspaceAnalysisViewEditDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'
import WorkspaceAnalysisViewShareDialog from './dialogs/WorkspaceAnalysisViewShareDialog.vue'
import { AxiosResponse } from 'axios'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'
import WorkspaceCockpitDialog from './dialogs/WorkspaceCockpitDialog.vue'
import mainStore from '../../../../App.store'
import { getCorrectRolesForExecution } from '../../../../helpers/commons/roleHelper'
import { mapState } from 'pinia'

export default defineComponent({
    name: 'workspace-analysis-view',
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, KnFabButton, Menu, Message, KnInputFile, WorkspaceAnalysisViewEditDialog, WorkspaceWarningDialog, WorkspaceAnalysisViewShareDialog, WorkspaceCockpitDialog },
    emits: ['showMenu', 'toggleDisplayView', 'execute'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        isOwner(): any {
            return (this.store.$state as any).user.userId === this.selectedAnalysis.creationUser
        },
        isShared(): any {
            return this.selectedAnalysis.functionalities.length > 1
        },
        ...mapState(mainStore, {
            user: 'user'
        }),
        addButtonIsVisible(): boolean {
            return this.user.functionalities.includes('CreateSelfSelviceCockpit') || this.user.functionalities.includes('CreateSelfSelviceGeoreport') || this.user.functionalities.includes('CreateSelfSelviceKpi')
        }
    },
    data() {
        return {
            mainDescriptor,
            loading: false,
            showDetailSidebar: false,
            analysisDocuments: [] as any,
            filteredAnalysisDocuments: [] as any[],
            selectedAnalysis: {} as any,
            menuButtons: [] as any,
            folders: [] as any[],
            searchWord: '' as string,
            editDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '',
            triggerUpload: false,
            uploading: false,
            shareDialogVisible: false,
            creationMenuButtons: [] as any,
            cockpitDialogVisible: false
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.getAnalysisDocs()
    },

    methods: {
        getAnalysisDocs() {
            this.loading = true
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documents/myAnalysisDocsList`)
                .then((response: AxiosResponse<any>) => {
                    this.analysisDocuments = [...response.data.root]
                    this.filteredAnalysisDocuments = [...this.analysisDocuments]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            return formatDateWithLocale(date, { dateStyle: 'short', timeStyle: 'short' })
        },
        showSidebar(clickedDocument) {
            this.selectedAnalysis = clickedDocument
            this.showDetailSidebar = true
        },
        showMenu(event, clickedDocument) {
            this.selectedAnalysis = clickedDocument
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        // prettier-ignore
        createMenuItems() {
        this.menuButtons = []
        this.menuButtons.push(
            { key: '0', label: this.$t('workspace.myAnalysis.menuItems.edit'), icon: 'fas fa-edit', command: () => { this.editAnalysisDocument(this.selectedAnalysis) }, visible: this.isOwner},
            { key: '1', label: this.$t('workspace.myAnalysis.menuItems.share'), icon: 'fas fa-share-alt', command: () => { this.shareAnalysisDocument(this.selectedAnalysis) }, visible: !this.isShared},
            { key: '1', label: this.$t('workspace.myAnalysis.menuItems.unshare'), icon: 'fas fa-times-circle', command: () => { this.shareAnalysisDocument(this.selectedAnalysis) }, visible: this.isShared},
            { key: '2', label: this.$t('workspace.myAnalysis.menuItems.clone'), icon: 'fas fa-clone', command: () => { this.cloneAnalysisDocument(this.selectedAnalysis) }},
            { key: '3', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => { this.deleteAnalysisDocumentConfirm(this.selectedAnalysis) }},
            { key: '4', label: this.$t('workspace.myAnalysis.menuItems.upload'), icon: 'fas fa-upload', command: () => { this.uploadAnalysisPreviewFile(this.selectedAnalysis) }}
        )
    },
        executeAnalysisDocument(document: any) {
            let typeCode = 'DOCUMENT'
            if (document.type === 'businessModel') {
                typeCode = 'DATAMART'
            } else if (document.dsTypeCd) {
                typeCode = 'DATASET'
            }
            getCorrectRolesForExecution(document).then(() => {
                this.$emit('execute', document)
            })
        },
        openKpiDesigner(analysis: any) {
            this.$router.push(`/kpi-edit/${analysis?.id}?from=Workspace`)
        },
        editAnalysisDocument(analysis: any) {
            this.selectedAnalysis = analysis
            this.editDialogVisible = true
        },
        async handleEditAnalysis(analysis: any) {
            const formatedAnalysis = {
                document: {
                    name: analysis.label,
                    label: analysis.name,
                    description: analysis.description,
                    id: analysis.id
                },
                updateFromWorkspace: true
            }
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/saveDocument/', formatedAnalysis, { headers: { 'X-Disable-Errors': 'true' } })
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.editDialogVisible = false
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch((response: any) => {
                    this.warningMessage = response
                    this.warningDialogVisbile = true
                })
        },
        async shareAnalysisDocument(analysis: any) {
            this.selectedAnalysis = analysis
            this.loading = true
            const shared = this.selectedAnalysis.functionalities.length > 1
            if (!shared) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/functionalities/forsharing/${analysis.id}`).then((response: AxiosResponse<any>) => {
                    this.folders = response.data
                    this.shareDialogVisible = true
                })
            } else {
                await this.handleAnalysShared(null, shared)
            }
            this.loading = false
        },
        async handleAnalysShared(selectedFolders: any, shared: boolean) {
            this.loading = true

            let url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documents/share?docId=${this.selectedAnalysis.id}&`
            if (!shared) {
                Object.keys(selectedFolders).forEach((id: any) => (url += `functs=${selectedFolders[id]}&`))
            }
            url += `isShare=${!shared}`

            await this.$http
                .post(url)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.shareDialogVisible = false
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch(() => {})

            this.loading = false
        },
        async cloneAnalysisDocumentConfirm(analysis: any) {
            this.$confirm.require({
                header: this.$t('common.toast.cloneConfirmTitle'),
                accept: async () => await this.cloneAnalysisDocument(analysis)
            })
        },
        async cloneAnalysisDocument(analysis: any) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `documents/clone?docId=${analysis.id}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch(() => {})
            this.loading = true
        },
        deleteAnalysisDocumentConfirm(analysis: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteAnalysis(analysis)
            })
        },
        deleteAnalysis(analysis: any) {
            this.loading = true
            this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documents/${analysis.label}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch(() => {})
            this.loading = false
        },
        uploadAnalysisPreviewFile(analysis: any) {
            this.selectedAnalysis = analysis
            this.triggerUpload = false
            setTimeout(() => (this.triggerUpload = true), 200)
        },
        uploadAnalysisFile(event: any) {
            this.uploading = true
            let uploadedFile = event.target.files[0]

            this.startUpload(uploadedFile)

            this.triggerUpload = false
            setTimeout(() => (this.uploading = false), 200)
        },
        startUpload(uploadedFile: any) {
            var formData = new FormData()
            formData.append('file', uploadedFile)
            this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/analysis/${this.selectedAnalysis.id}`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                })
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.uploading'),
                        msg: this.$t('common.toast.uploadSuccess')
                    })
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch()
                .finally(() => {
                    this.triggerUpload = false
                })
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredAnalysisDocuments = [...this.analysisDocuments] as any[]
                } else {
                    this.filteredAnalysisDocuments = this.analysisDocuments.filter((el: any) => {
                        return el.name?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.creationUser?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        },
        showCreationMenu(event) {
            this.createCreationMenuButtons()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.creationMenu.toggle(event)
        },
        createCreationMenuButtons() {
            this.creationMenuButtons = []

            if (this.user.functionalities.includes('CreateSelfSelviceCockpit')) this.creationMenuButtons.push({ key: '0', label: this.$t('common.cockpit'), command: this.openCockpitDialog, visible: true })
            if (this.user.functionalities.includes('CreateSelfSelviceGeoreport')) this.creationMenuButtons.push({ key: '1', label: this.$t('workspace.myAnalysis.geoRef'), command: this.openGeoRefCreation, visible: true })
            if (this.user.functionalities.includes('CreateSelfSelviceKpi')) this.creationMenuButtons.push({ key: '2', label: this.$t('common.kpi'), command: this.openKpiDocumentDesigner, visible: true })
        },
        openCockpitDialog() {
            this.cockpitDialogVisible = true
        },
        closeCockpitDialog() {
            this.cockpitDialogVisible = false
            this.getAnalysisDocs()
        },
        openKpiDocumentDesigner() {
            this.$router.push('/kpi-edit/new-kpi?from=Workspace')
        },
        openGeoRefCreation() {
            this.$router.push('/gis/new')
        }
    }
})
</script>
