<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary" style="width:100%">
        <template #left>
            <Button id="showSidenavIcon" icon="fas fa-list" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.menuLabels.myAnalysis') }}
        </template>
        <template #right>
            <FabButton icon="fas fa-folder" data-test="new-folder-button" />
        </template>
    </Toolbar>
    <InputText class="kn-material-input p-m-2" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" badge="0" />
    <div class="p-m-2 overflow">
        <DataTable
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
    </div>
    <DetailSidebar :visible="showDetailSidebar" :viewType="'analysis'" :document="selectedAnalysis" @executeAnalysisDocument="executeAnalysisDocument" @editAnalysisDocument="editAnalysisDocument" @close="showDetailSidebar = false" />

    <Menu id="optionsMenu" ref="optionsMenu" :model="menuItems" />
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import analysisDescriptor from './WorkspaceAnalysisViewDescriptor.json'
import DetailSidebar from '@/modules/workspace/genericComponents/DetailSidebar.vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Menu from 'primevue/contextmenu'

export default defineComponent({
    components: { DataTable, Column, Menu, DetailSidebar },
    emits: ['showMenu'],
    data() {
        return {
            analysisDescriptor,
            loading: false,
            showDetailSidebar: false,
            analysisDocuments: [] as any,
            selectedAnalysis: {} as any,
            filters: {
                global: [filterDefault]
            } as Object,
            sidenavFields: analysisDescriptor.sidenavFields,
            menuItems: [
                {
                    key: '0',
                    label: this.$t('workspace.myAnalysis.menuItems.share'),
                    icon: 'fas fa-share',
                    command: () => {
                        // event.originalEvent: Browser event
                        // event.item: Menuitem instance
                    }
                },
                {
                    key: '1',
                    label: this.$t('workspace.myAnalysis.menuItems.clone'),
                    icon: 'fas fa-clone',
                    command: () => {}
                },
                {
                    key: '2',
                    label: this.$t('workspace.myAnalysis.menuItems.delete'),
                    icon: 'fas fa-trash',
                    command: () => {}
                },
                {
                    key: '3',
                    label: this.$t('workspace.myAnalysis.menuItems.upload'),
                    icon: 'fas fa-share-alt',
                    command: () => {}
                }
            ]
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
        showMenu(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        executeAnalysisDocument() {
            console.log('executeAnalysisDocument')
        },
        editAnalysisDocument() {
            console.log('editAnalysisDocument')
        }
    }
})
</script>
