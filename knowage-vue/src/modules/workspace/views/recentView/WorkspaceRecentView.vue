<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.recentDocuments') }}
        </template>
        <template #right>
            <FabButton icon="fas fa-folder" data-test="new-folder-button" />
        </template>
    </Toolbar>
    <div class="p-m-2">
        <DataTable
            class="p-datatable-sm kn-table"
            :value="recentDocumentsList"
            :loading="loading"
            :scrollable="true"
            scrollHeight="89vh"
            dataKey="objId"
            responsiveLayout="stack"
            breakpoint="600px"
            v-model:filters="filters"
            v-model:selection="selectedDocument"
            selectionMode="single"
            @rowSelect="showDetailSidebar = true"
        >
            <template #header>
                <div class="table-header">
                    <span class="p-input-icon-left">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #filter="{ filterModel }">
                <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
            </template>
            <Column field="documentType" :header="$t('importExport.gallery.column.type')" :sortable="true" />
            <Column field="documentName" :header="$t('importExport.gallery.column.name')" :sortable="true" />
            <Column field="requestTime" :header="$t('managers.functionalitiesManagement.execution')" :sortable="true">
                <template #body="{data}">
                    {{ formatDate(data.requestTime) }}
                </template>
            </Column>
        </DataTable>
    </div>
    <Sidebar class="mySidebar" v-model:visible="showDetailSidebar" :showCloseIcon="false" position="right">
        <div class="kn-toolbar kn-toolbar--default" :style="workspaceDescriptor.style.sidenavToolbar">
            <Button icon="fas fa-play-circle" class="p-button-text p-button-rounded p-button-plain " />
        </div>
        <div class="p-m-5">
            <div class="p-mb-5" v-for="(field, index) of sidenavFields" :key="index">
                <h3 class="p-m-0">
                    <b>{{ $t(field.translation) }}</b>
                </h3>
                <p class="p-m-0" v-if="field.type === 'date'">{{ formatDate(selectedDocument[field.value]) }}</p>
                <p class="p-m-0" v-else>{{ selectedDocument[field.value] }}</p>
            </div>
        </div>
    </Sidebar>
</template>
<script lang="ts">
import { filterDefault } from '@/helpers/commons/filterHelper'
import { defineComponent } from 'vue'
import { IDocument } from '@/modules/workspace/Workspace'
import workspaceDescriptor from './WorkspaceRecentViewDescriptor.json'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Sidebar from 'primevue/sidebar'

export default defineComponent({
    components: { DataTable, Column, Sidebar },
    emits: ['showMenu'],
    data() {
        return {
            workspaceDescriptor,
            loading: false,
            showDetailSidebar: false,
            recentDocumentsList: [] as IDocument[],
            selectedDocument: {} as IDocument,
            filters: {
                global: [filterDefault]
            } as Object,
            sidenavFields: workspaceDescriptor.sidenavFields
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
        }
    }
})
</script>
