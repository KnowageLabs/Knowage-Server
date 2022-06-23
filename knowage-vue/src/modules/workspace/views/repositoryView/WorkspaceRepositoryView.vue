<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myRepository') }}
        </template>
        <template #end>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
        </template>
    </Toolbar>

    <WorkspaceRepositoryBreadcrumb :breadcrumbs="breadcrumbs" @breadcrumbClicked="$emit('breadcrumbClicked', $event)"></WorkspaceRepositoryBreadcrumb>

    <InputText class="kn-material-input p-m-2" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
    <div class="p-m-2 kn-overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table p-mx-2" :value="filteredDocuments" :loading="loading" dataKey="biObjId" responsiveLayout="stack" breakpoint="600px" data-test="documents-table">
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
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click="showSidebar(slotProps.data)" :data-test="'info-button-' + slotProps.data.documentName" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="executeDocumentFromOrganizer(slotProps.data)" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2" data-test="card-container">
            <Message v-if="filteredDocuments.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard
                    v-for="(document, index) of filteredDocuments"
                    :key="index"
                    :viewType="'repository'"
                    :document="document"
                    @executeDocumentFromOrganizer="executeDocumentFromOrganizer"
                    @moveDocumentToFolder="moveDocumentToFolder"
                    @deleteDocumentFromOrganizer="deleteDocumentConfirm"
                    @openSidebar="showSidebar"
                />
            </template>
        </div>
    </div>

    <DetailSidebar
        :visible="showDetailSidebar"
        :viewType="'repository'"
        :document="selectedDocument"
        @executeDocumentFromOrganizer="executeDocumentFromOrganizer"
        @moveDocumentToFolder="moveDocumentToFolder"
        @deleteDocumentFromOrganizer="deleteDocumentConfirm"
        @close="showDetailSidebar = false"
        data-test="detail-sidebar"
    />

    <WorkspaceRepositoryMoveDialog :visible="moveDialogVisible" :propFolders="folders" @close="moveDialogVisible = false" @move="handleDocumentMove"></WorkspaceRepositoryMoveDialog>
    <WorkspaceWarningDialog :visible="warningDialogVisbile" :warningMessage="warningMessage" @close="closeWarningDialog"></WorkspaceWarningDialog>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { IDocument, IFolder } from '@/modules/workspace/Workspace'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import Message from 'primevue/message'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import repositoryDescriptor from './WorkspaceRepositoryViewDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Menu from 'primevue/contextmenu'
import WorkspaceRepositoryMoveDialog from './dialogs/WorkspaceRepositoryMoveDialog.vue'
import WorkspaceWarningDialog from '../../genericComponents/WorkspaceWarningDialog.vue'
import WorkspaceRepositoryBreadcrumb from './breadcrumbs/WorkspaceRepositoryBreadcrumb.vue'
import { AxiosResponse } from 'axios'
import { formatDateWithLocale } from '@/helpers/commons/localeHelper'

export default defineComponent({
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, Menu, Message, WorkspaceRepositoryMoveDialog, WorkspaceWarningDialog, WorkspaceRepositoryBreadcrumb },
    emits: ['showMenu', 'reloadRepositoryMenu', 'toggleDisplayView', 'breadcrumbClicked', 'execute'],
    props: { selectedFolder: { type: Object }, id: { type: String, required: false }, toggleCardDisplay: { type: Boolean }, breadcrumbs: { type: Array }, allFolders: { type: Array } },
    data() {
        return {
            mainDescriptor,
            loading: false,
            showDetailSidebar: false,
            documents: [] as IDocument[],
            filteredDocuments: [] as IDocument[],
            menuButtons: [] as any,
            selectedDocument: {} as IDocument,
            columns: repositoryDescriptor.columns,
            searchWord: '' as string,
            folders: [] as IFolder[],
            moveDialogVisible: false,
            warningDialogVisbile: false,
            warningMessage: ''
        }
    },
    watch: {
        id() {
            this.getFolderDocuments()
        },
        allFolders() {
            this.loadFolders()
        }
    },
    created() {
        this.loadFolders()
        this.getFolderDocuments()
    },
    methods: {
        loadFolders() {
            this.folders = this.allFolders as IFolder[]
        },
        getFolderDocuments() {
            this.loading = true
            return this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${this.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.documents = [...response.data]
                    this.filteredDocuments = [...this.documents]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            return formatDateWithLocale(date, { dateStyle: 'short', timeStyle: 'short' })
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
                { key: '4', label: this.$t('workspace.myAnalysis.menuItems.delete'), icon: 'fas fa-trash', command: () => { this.deleteDocumentConfirm(this.selectedDocument) }},
            )
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        executeDocumentFromOrganizer(document: IDocument) {
            this.$emit('execute', document)
        },
        moveDocumentToFolder(document: IDocument) {
            this.selectedDocument = document
            this.moveDialogVisible = true
        },
        async handleDocumentMove(folder: any) {
            this.loading = true
            await this.$http
                .put(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/documentsee/${this.selectedDocument.biObjId}/${this.selectedDocument.functId}/${folder.id}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.moveDialogVisible = false
                    this.showDetailSidebar = false
                    this.getFolderDocuments()
                })
                .catch((response: any) => {
                    this.warningMessage = response
                    this.warningDialogVisbile = true
                })
            this.loading = false
        },
        deleteDocumentConfirm(document: IDocument) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDocument(document)
            })
        },
        deleteDocument(document: IDocument) {
            this.loading = true
            this.$http
                .delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/organizer/documents/${document.functId}/${document.biObjId}`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.showDetailSidebar = false
                    this.getFolderDocuments()
                })
                .catch(() => {})
            this.loading = false
        },
        closeWarningDialog() {
            this.warningMessage = ''
            this.warningDialogVisbile = false
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDocuments = [...this.documents] as IDocument[]
                } else {
                    this.filteredDocuments = this.documents.filter((el: any) => {
                        return (
                            el.documentType?.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            el.documentLabel?.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            el.documentName?.toLowerCase().includes(this.searchWord.toLowerCase()) ||
                            el.documentDescription?.toLowerCase().includes(this.searchWord.toLowerCase())
                        )
                    })
                }
            }, 250)
        }
    }
})
</script>
