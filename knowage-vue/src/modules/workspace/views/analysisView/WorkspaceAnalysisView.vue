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
    <div class="p-m-2">
        <DataTable class="p-datatable-sm kn-table" :value="analysisDocuments" :loading="loading" :scrollable="true" scrollHeight="89vh" dataKey="id" responsiveLayout="stack" breakpoint="600px" v-model:selection="selectedDocument" v-model:filters="filters" @row-select="onActiveVersionChange">
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
            <Column field="name" :header="$t('importExport.gallery.column.name')" :sortable="true" />
            <Column field="creationUser" :header="$t('kpi.targetDefinition.kpiAuthor')" :sortable="true" />
            <Column field="creationDate" :header="$t('kpi.targetDefinition.kpiDate')" :sortable="true">
                <template #body="{data}">
                    {{ formatDate(data.creationDate) }}
                </template>
            </Column>
            <Column style="width:10%;text-align:end" @rowClick="false">
                <template #body>
                    <Button icon="fas fa-edit" class="p-button-text p-button-rounded p-button-plain" />
                    <Button icon="fas fa-play-circle" class="p-button-text p-button-rounded p-button-plain" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import { filterDefault } from '@/helpers/commons/filterHelper'

export default defineComponent({
    components: { DataTable, Column },
    emits: ['showMenu'],
    data() {
        return {
            loading: false,
            analysisDocuments: [] as any,
            selectedDocument: {} as any,
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
        }
    }
})
</script>
