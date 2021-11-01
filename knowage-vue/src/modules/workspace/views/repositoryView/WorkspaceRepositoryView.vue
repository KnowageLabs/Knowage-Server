<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myRepository') }} - {{ selectedFolder.label }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <FabButton icon="fas fa-folder" data-test="new-folder-button" @click="$emit('createFolderClick')" />
        </template>
    </Toolbar>
    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
    <div class="p-m-2 overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table" :value="documents" :loading="loading" dataKey="biObjId" responsiveLayout="stack" breakpoint="600px" v-model:filters="filters">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #filter="{ filterModel }">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
            <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" />
            <Column class="icon-cell" :style="mainDescriptor.style.iconColumn">
                <template #body="slotProps">
                    <Button icon="fas fa-ellipsis-v" class="p-button-link" @click="showMenu($event, slotProps.data)" />
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click="showSidebar(slotProps.data)" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="logEvent(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2">
            <WorkspaceCard v-for="(document, index) of documents" :key="index" :viewType="'repository'" :document="document" @executeDocumentFromOrganizer="executeDocumentFromOrganizer" @moveDocumentToFolder="moveDocumentToFolder" @deleteDocumentFromOrganizer="deleteDocumentFromOrganizer" />
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'repository'"
        :document="selectedDocument"
        @executeDocumentFromOrganizer="executeDocumentFromOrganizer"
        @moveDocumentToFolder="moveDocumentToFolder"
        @deleteDocumentFromOrganizer="deleteDocumentFromOrganizer"
        @close="showDetailSidebar = false"
    />

    <WorkspaceRepositoryMoveDialog :visible="moveDialogVisible" :propFolders="folders" @close="moveDialogVisible = false" @move="handleDocumentMove"></WorkspaceRepositoryMoveDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import repositoryDescriptor from './WorkspaceRepositoryViewDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import FabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/contextmenu'
import WorkspaceRepositoryMoveDialog from './dialogs/WorkspaceRepositoryMoveDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'

export default defineComponent({
    components: { DataTable, Column, FabButton, DetailSidebar, WorkspaceCard, Menu, WorkspaceRepositoryMoveDialog, WorkspaceWarningDialog },
    emits: ['showMenu', 'reloadRepositoryMenu', 'toggleDisplayView', 'createFolderClick'],
    props: { selectedFolder: { type: Object }, id: { type: String, required: false }, toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            mainDescriptor,
            loading: false,
            showDetailSidebar: false,
            displayCreateFolderDialog: false,
            documents: [] as IDocument[],
            menuButtons: [] as any,
            selectedDocument: {} as IDocument,
            newFolder: {} as IFolder,
            columns: repositoryDescriptor.columns,
            filters: {
                global: [filterDefault]
            } as Object,
            folders: [] as IFolder[], // premestiti u prop nakon promena u meniju?
            moveDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: ''
        }
    },
    watch: {
        id() {
            this.getFolderDocuments()
        }
    },
    async created() {
        await this.loadFolders()
        this.getFolderDocuments()
    },
    methods: {
        async loadFolders() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/folders/`).then((response) => (this.folders = response.data))
            this.loading = false
            console.log('ALL FOLDERS: ', this.folders)
        },
        getFolderDocuments() {
            this.loading = true
            return this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${this.id}`)
                .then((response) => {
                    this.documents = [...response.data]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        showSidebar(clickedDocument) {
            this.selectedDocument = clickedDocument
            this.showDetailSidebar = true
        },
        showMenu(event, clickedDocument) {
            this.selectedDocument = clickedDocument
            this.createMenuItems()
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        // prettier-ignore
        createMenuItems() {
            this.menuButtons = []
            this.menuButtons.push(
                { key: '3', label: this.$t('workspace.myRepository.moveDocument'), icon: 'fas fa-share', command: () => { this.moveDocumentToFolder(this.selectedDocument) }},
                { key: '4', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => { this.deleteDocumentFromOrganizer(this.selectedDocument) }},
            )
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        logEvent(event) {
            console.log(event)
        },
        executeDocumentFromOrganizer(event) {
            console.log('executeDocumentFromOrganizer() {', event)
        },
        moveDocumentToFolder(document: IDocument) {
            console.log('moveDocumentToFolder() {', document)
            this.selectedDocument = document
            this.moveDialogVisible = true
        },
        async handleDocumentMove(folder: any) {
            this.loading = true
            console.log('SELECTED DOCUMENT FOR MOVE: ', this.selectedDocument)
            console.log('SELECTED FOLDER FOR MOVE: ', folder)
            await this.$http
                .put(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/organizer/documentsee/${this.selectedDocument.biObjId}/${this.selectedDocument.functId}/${folder.id}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.moveDialogVisible = false
                    this.showDetailSidebar = false
                    this.getFolderDocuments()
                })
                .catch((response) => {
                    this.warningMessage = response
                    this.warningDialogVisbile = true
                })
            this.loading = false
        },
        deleteDocumentFromOrganizer(event) {
            console.log('deleteDocumentFromOrganizer() {', event)
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        }
    }
})
</script>
