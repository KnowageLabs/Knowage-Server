<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.recentDocuments') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
        </template>
    </Toolbar>
    <InputText class="kn-material-input p-m-2" :style="mainDescriptor.style.filterInput" v-model="searchWord" type="text" :placeholder="$t('common.search')" @input="searchItems" data-test="search-input" />
    <div class="kn-overflow p-mx-2">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table" :value="filteredDocuments" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" data-test="recent-table">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column field="documentType" :header="$t('importExport.gallery.column.type')" :sortable="true" />
            <Column field="documentName" :header="$t('importExport.gallery.column.name')" :sortable="true" />
            <Column field="requestTime" :header="$t('managers.functionalitiesManagement.execution')" :sortable="true">
                <template #body="{data}">
                    {{ formatDate(data.requestTime) }}
                </template>
            </Column>
            <Column :style="mainDescriptor.style.iconColumn">
                <template #header> &ensp; </template>
                <template #body="slotProps">
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click.stop="showSidebar(slotProps.data)" :data-test="'info-button-' + slotProps.data.documentName" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="executeRecent" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2" data-test="card-container">
            <Message v-if="filteredDocuments.length === 0" class="kn-flex p-m-2" severity="info" :closable="false" :style="mainDescriptor.style.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
            <template v-else>
                <WorkspaceCard v-for="(document, index) of filteredDocuments" :key="index" :viewType="'recent'" :document="document" @executeRecent="executeRecent" @openSidebar="showSidebar" />
            </template>
        </div>
    </div>

    <DetailSidebar :visible="showDetailSidebar" :viewType="'recent'" :document="selectedDocument" @executeRecent="executeRecent" @close="showDetailSidebar = false" data-test="detail-sidebar" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { IDocument } from '@/modules/workspace/Workspace'
import { AxiosResponse } from 'axios'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import Message from 'primevue/message'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { DataTable, Column, DetailSidebar, WorkspaceCard, Message },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            mainDescriptor,
            loading: false,
            showDetailSidebar: false,
            recentDocumentsList: [] as IDocument[],
            filteredDocuments: [] as IDocument[],
            selectedDocument: {} as IDocument,
            searchWord: '' as string
        }
    },
    created() {
        this.getRecentDocuments()
    },
    methods: {
        getRecentDocuments() {
            this.loading = true
            return this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/recents`)
                .then((response: AxiosResponse<any>) => {
                    this.recentDocumentsList = [...response.data]
                    this.filteredDocuments = [...this.recentDocumentsList]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        executeRecent() {
            this.$store.commit('setInfo', { title: 'Todo', msg: 'Functionality not in this sprint' })
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        showSidebar(clickedDocument) {
            this.selectedDocument = clickedDocument
            this.showDetailSidebar = true
        },
        searchItems() {
            setTimeout(() => {
                if (!this.searchWord.trim().length) {
                    this.filteredDocuments = [...this.recentDocumentsList] as any[]
                } else {
                    this.filteredDocuments = this.recentDocumentsList.filter((el: any) => {
                        return el.documentType?.toLowerCase().includes(this.searchWord.toLowerCase()) || el.documentName?.toLowerCase().includes(this.searchWord.toLowerCase())
                    })
                }
            }, 250)
        }
    }
})
</script>
