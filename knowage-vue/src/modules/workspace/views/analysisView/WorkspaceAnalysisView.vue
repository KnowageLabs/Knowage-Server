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
        <DataTable
            v-if="!toggleCardDisplay"
            class="p-datatable-sm kn-table"
            :value="analysisDocuments"
            :loading="loading"
            :scrollable="true"
            scrollHeight="89vh"
            dataKey="id"
            responsiveLayout="stack"
            breakpoint="600px"
            v-model:filters="filters"
            v-model:selection="selectedAnalysis"
            selectionMode="single"
            @rowSelect="showDetailSidebar = true"
        >
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
            <Column style="width:10%;text-align:end" @rowClick="false">
                <template #body>
                    <Button icon="fas fa-edit" class="p-button-link" />
                    <Button icon="fas fa-play-circle" class="p-button-link" />
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
        @deleteAnalysisDocumentConfirm="deleteAnalysisDocumentConfirm"
        @uploadAnalysisPreviewFile="uploadAnalysisPreviewFile"
        @close="showDetailSidebar = false"
    />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import analysisDescriptor from './WorkspaceAnalysisViewDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'workspace-analysis-view',
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, KnFabButton },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            analysisDescriptor,
            loading: false,
            showDetailSidebar: false,
            analysisDocuments: [] as any,
            selectedAnalysis: {} as any,
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
        executeAnalysisDocument(event) {
            console.log('executeAnalysisDocument', event)
            this.$store.commit('setInfo', {
                title: 'Todo',
                msg: 'Functionality not in this sprint'
            })
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
        deleteAnalysisDocumentConfirm(analysis: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteAnalysis(analysis)
            })
        },
        deleteAnalysis(analysis: any) {
            console.log('deleteAnalysisDocument', analysis)
            this.loading = true
            this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${analysis.name}`)
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
        uploadAnalysisPreviewFile(event) {
            console.log('uploadAnalysisPreviewFile', event)
        }
    }
})
</script>
