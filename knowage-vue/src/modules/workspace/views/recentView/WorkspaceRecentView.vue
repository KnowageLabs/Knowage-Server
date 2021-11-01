<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.recentDocuments') }}
        </template>
        <template #right>
            <Button v-if="toggleCardDisplay" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
            <Button v-if="!toggleCardDisplay" icon="fas fa-th-large" class="p-button-text p-button-rounded p-button-plain" @click="toggleDisplayView" />
        </template>
    </Toolbar>
    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
    <div class="overflow">
        <DataTable v-if="!toggleCardDisplay" class="p-datatable-sm kn-table" :value="recentDocumentsList" :loading="loading" dataKey="objId" responsiveLayout="stack" breakpoint="600px" v-model:filters="filters">
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
                    <Button icon="fas fa-info-circle" class="p-button-link" v-tooltip.left="$t('workspace.myModels.showInfo')" @click.stop="showSidebar(slotProps.data)" />
                    <Button icon="fas fa-play-circle" class="p-button-link" @click="executeRecent" />
                </template>
            </Column>
        </DataTable>
        <div v-if="toggleCardDisplay" class="p-grid p-m-2">
            <WorkspaceCard v-for="(document, index) of recentDocumentsList" :key="index" :viewType="'recent'" :document="document" @executeRecent="executeRecent" @openSidebar="showSidebar" />
        </div>
    </div>

    <DetailSidebar :visible="showDetailSidebar" :viewType="'recent'" :document="selectedDocument" @executeRecent="executeRecent" @close="showDetailSidebar = false" />
</template>
<script lang="ts">
import { filterDefault } from '@/helpers/commons/filterHelper'
import { defineComponent } from 'vue'
import { IDocument } from '@/modules/workspace/Workspace'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import WorkspaceCard from '@/modules/workspace/genericComponents/WorkspaceCard.vue'
import workspaceDescriptor from './WorkspaceRecentViewDescriptor.json'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { DataTable, Column, DetailSidebar, WorkspaceCard },
    emits: ['showMenu', 'toggleDisplayView'],
    props: { toggleCardDisplay: { type: Boolean } },
    data() {
        return {
            mainDescriptor,
            workspaceDescriptor,
            loading: false,
            showDetailSidebar: false,
            recentDocumentsList: [] as IDocument[],
            selectedDocument: {} as IDocument,
            filters: {
                global: [filterDefault]
            } as Object
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
                .then((response) => {
                    this.recentDocumentsList = [...response.data]
                })
                .finally(() => (this.loading = false))
        },
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        executeRecent(event) {
            console.log('executeRecent() {', event)
            this.$store.commit('setInfo', { title: 'Todo', msg: 'Functionality not in this sprint' })
        },
        toggleDisplayView() {
            this.$emit('toggleDisplayView')
        },
        showSidebar(clickedDocument) {
            this.selectedDocument = clickedDocument
            this.showDetailSidebar = true
        }
    }
})
</script>
