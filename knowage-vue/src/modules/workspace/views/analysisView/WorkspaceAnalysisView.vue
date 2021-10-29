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
        @cloneAnalysisDocument="cloneAnalysisDocument"
        @deleteAnalysisDocument="deleteAnalysisDocument"
        @uploadAnalysisPreviewFile="uploadAnalysisPreviewFile"
        @close="showDetailSidebar = false"
    />
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
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

export default defineComponent({
    name: 'workspace-analysis-view',
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, KnFabButton, Menu },
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
            menuButtons: [
                {
                    key: '0',
                    label: this.$t('workspace.myAnalysis.menuItems.edit'),
                    icon: 'fas fa-edit',
                    visible: this.isOwner,
                    command: () => {
                        this.editAnalysisDocument(this.selectedAnalysis)
                    }
                },
                {
                    key: '1',
                    label: this.$t('workspace.myAnalysis.menuItems.share'),
                    icon: 'fas fa-share',
                    command: () => {
                        this.shareAnalysisDocument(this.selectedAnalysis)
                    }
                },
                {
                    key: '2',
                    label: this.$t('workspace.myAnalysis.menuItems.clone'),
                    icon: 'fas fa-clone',
                    command: () => {
                        this.cloneAnalysisDocument(this.selectedAnalysis)
                    }
                },
                {
                    key: '3',
                    label: this.$t('workspace.myAnalysis.menuItems.delete'),
                    icon: 'fas fa-trash',
                    command: () => {
                        this.deleteAnalysisDocument(this.selectedAnalysis)
                    }
                },
                {
                    key: '4',
                    label: this.$t('workspace.myAnalysis.menuItems.upload'),
                    icon: 'fas fa-share-alt',
                    command: () => {
                        this.uploadAnalysisPreviewFile(this.selectedAnalysis)
                    }
                }
            ] as any,
            filters: {
                global: [filterDefault]
            } as Object
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
        showMenu(event, selectedDocument) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
            this.selectedAnalysis = selectedDocument
        },
        executeAnalysisDocument(event) {
            console.log('executeAnalysisDocument', event)
            this.$store.commit('setInfo', { title: 'Todo', msg: 'Functionality not in this sprint' })
        },
        editAnalysisDocument(event) {
            console.log('editAnalysisDocument', event)
        },
        shareAnalysisDocument(event) {
            console.log('shareAnalysisDocument', event)
        },
        cloneAnalysisDocument(event) {
            console.log('cloneAnalysisDocument', event)
        },
        deleteAnalysisDocument(event) {
            console.log('deleteAnalysisDocument', event)
        },
        uploadAnalysisPreviewFile(event) {
            console.log('uploadAnalysisPreviewFile', event)
        }
    }
})
</script>
