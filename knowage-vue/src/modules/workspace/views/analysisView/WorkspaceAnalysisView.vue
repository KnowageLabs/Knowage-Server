<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myAnalysis') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="$emit('toggleDisplayView')" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="$emit('toggleDisplayView')" />
            <KnFabButton icon="fas fa-plus" data-test="new-folder-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
    <div class="p-m-2 overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table" :value="analysisDocuments" :loading="loading" :scrollable="true" scrollHeight="89vh" dataKey="id" responsiveLayout="stack" breakpoint="600px" v-model:filters="filters">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #filter="{ filterModel }">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
            <Column field="name" :header="$t('importExport.gallery.column.name')" :sortable="true" />
            <Column field="creationUser" :header="$t('kpi.targetDefinition.kpiAuthor')" :sortable="true" />
            <Column field="creationDate" :header="$t('kpi.targetDefinition.kpiDate')" :sortable="true">
                <template #body="{data}">
                    {{ formatDate(data.creationDate) }}
                </template>
            </Column>
            <Column>
                <template #body="slotProps">
                    <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="showMenu($event, slotProps.data)" />
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click="showSidebar(slotProps.data)" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="executeAnalysisDocument" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2">
            <WorkspaceCard v-for="(document, index) of analysisDocuments" :key="index" :viewType="'analysis'" :document="document" />
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
    />
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />

    <WorkspaceAnalysisViewShareDialog :visible="shareDialogVisible" :propFolders="folders" @close="shareDialogVisible = false" @share="handleAnalysshared($event, false)"></WorkspaceAnalysisViewShareDialog>
    <WorkspaceAnalysisViewEditDialog :visible="editDialogVisible" :propAnalysis="selectedAnalysis" @close="editDialogVisible = false" @save="handleEditAnalysis"></WorkspaceAnalysisViewEditDialog>
    <WorkspaceAnalysisViewWarningDialog :visible="warningDialogVisbile" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceAnalysisViewWarningDialog>

    <KnInputFile v-if="!uploading" :changeFunction="uploadAnalysisFile" accept="image/*" :triggerInput="triggerUpload" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import analysisDescriptor from './WorkspaceAnalysisViewDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import DataTable from 'primevue/datatable'
import Menu from 'primevue/contextmenu'
import Column from 'primevue/column'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import WorkspaceAnalysisViewEditDialog from './dialogs/WorkspaceAnalysisViewEditDialog.vue'
import WorkspaceAnalysisViewWarningDialog from './dialogs/WorkspaceAnalysisViewWarningDialog.vue'
import WorkspaceAnalysisViewShareDialog from './dialogs/WorkspaceAnalysisViewShareDialog.vue'

export default defineComponent({
    name: 'workspace-analysis-view',
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, KnFabButton, Menu, KnInputFile, WorkspaceAnalysisViewEditDialog, WorkspaceAnalysisViewWarningDialog, WorkspaceAnalysisViewShareDialog },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    computed: {
        isOwner(): any {
            return (this.$store.state as any).user.fullName === this.selectedAnalysis.creationUser
        }
    },
    data() {
        return {
            analysisDescriptor,
            loading: false,
            showDetailSidebar: false,
            analysisDocuments: [] as any,
            selectedAnalysis: {} as any,
            menuButtons: [] as any,
            filters: {
                global: [filterDefault]
            } as Object,
            folders: [] as any[],
            editDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: '',
            triggerUpload: false,
            uploading: false,
            shareDialogVisible: false
        }
    },
    created() {
        this.getAnalysisDocs()
    },
    methods: {
        getAnalysisDocs() {
            this.loading = true
            return this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documents/myAnalysisDocsList`)
                .then((response) => {
                    this.analysisDocuments = [...response.data.root]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
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
                { key: '1', label: this.$t('workspace.myAnalysis.menuItems.share'), icon: 'fas fa-share', command: () => { this.shareAnalysisDocument(this.selectedAnalysis) }},
                { key: '2', label: this.$t('workspace.myAnalysis.menuItems.clone'), icon: 'fas fa-clone', command: () => { this.cloneAnalysisDocument(this.selectedAnalysis) }},
                { key: '3', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => { this.deleteAnalysisDocumentConfirm(this.selectedAnalysis) }},
                { key: '4', label: this.$t('workspace.myAnalysis.menuItems.upload'), icon: 'fas fa-share-alt', command: () => { this.uploadAnalysisPreviewFile(this.selectedAnalysis) }}
            )
        },
        executeAnalysisDocument(event) {
            console.log('executeAnalysisDocument', event)
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
        },
        editAnalysisDocument(analysis: any) {
            // console.log('editAnalysisDocument', analysis)
            this.selectedAnalysis = analysis
            this.editDialogVisible = true
        },
        async handleEditAnalysis(analysis: any) {
            console.log('ANALYSIS FOR EDIT: ', analysis)
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/saveDocument/', formatedAnalysis, { headers: { 'X-Disable-Errors': true } })
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.editDialogVisible = false
                    this.showDetailSidebar = false
                    this.getAnalysisDocs()
                })
                .catch((response) => {
                    this.warningMessage = response
                    this.warningDialogVisbile = true
                })
        },
        async shareAnalysisDocument(analysis: any) {
            // console.log('shareAnalysisDocument', analysis)
            this.selectedAnalysis = analysis
            this.loading = true
            const shared = this.selectedAnalysis.functionalities.length > 1
            if (!shared) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/functionalities/forsharing/${analysis.id}`).then((response) => {
                    this.folders = response.data
                    this.shareDialogVisible = true
                })
            } else {
                await this.handleAnalysshared(null, shared)
            }
            // console.log('LOADED FOLDERS: ', this.folders)s
            this.loading = false
        },
        async handleAnalysshared(selectedFolders: any, shared: boolean) {
            console.log('handleAnalysshared SELECTED FOLDERS: ', selectedFolders)
            this.loading = true

            let url = process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documents/share?docId=${this.selectedAnalysis.id}&`
            if (!shared) {
                Object.keys(selectedFolders).forEach((id: any) => (url += `functs=${selectedFolders[id]}&`))
            }
            url += `isShare=${!shared}`

            await this.$http
                .post(url)
                .then(() => {
                    this.$store.commit('setInfo', {
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documents/clone?docId=${analysis.id}`)
                .then(() => {
                    this.$store.commit('setInfo', {
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
            // console.log('deleteAnalysisDocument', analysis)
            this.loading = true
            this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${analysis.label}`)
                .then(() => {
                    this.$store.commit('setInfo', {
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
            console.log('uploadAnalysisPreviewFile', analysis)
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
            console.log('UPLOAD STARTED!', uploadedFile)
            var formData = new FormData()
            formData.append('file', uploadedFile)
            this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analysis/${this.selectedAnalysis.id}`, formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                })
                .then(() => {
                    this.$store.commit('setInfo', {
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
        }
    }
})
</script>
