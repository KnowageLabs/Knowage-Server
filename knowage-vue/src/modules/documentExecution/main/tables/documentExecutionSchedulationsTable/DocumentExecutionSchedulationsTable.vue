<template>
    <div class="p-grid">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
            <template #left>{{ $t('documentExecution.main.scheduledExecutions') }} </template>

            <template #right>
                <Button id="document-execution-schedulations-close-button" class="kn-button kn-button--primary" @click="closeTable"> {{ $t('common.close') }}</Button>
            </template>
        </Toolbar>

        <DataTable
            :value="schedulations"
            id="old-chedulations-table"
            class="p-datatable-sm kn-table p-col-12"
            dataKey="id"
            v-model:filters="filters"
            :globalFilterFields="documentExecutionSchedulationsTableDescriptor.globalFilterFields"
            :paginator="schedulations.length > 20"
            :rows="20"
            responsiveLayout="stack"
            breakpoint="600px"
        >
            <template #empty>
                <Message class="p-m-2" severity="info" :closable="false" :style="documentExecutionSchedulationsTableDescriptor.styles.message">
                    {{ $t('common.info.noDataFound') }}
                </Message>
            </template>
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column class="kn-truncated" field="name" :header="$t('common.name')" key="name" :sortable="true"></Column>
            <Column class="kn-truncated" field="description" :header="$t('common.description')" key="description" :sortable="true"></Column>
            <Column class="kn-truncated" field="dateCreation" :header="$t('common.creationDate')" key="dateCreation" :sortable="true">
                <template #body="slotProps">
                    {{ getFormattedDate(slotProps.data.dateCreation, 'DD/MM/YYYY hh:mm') }}
                </template>
            </Column>
            <Column :style="documentExecutionSchedulationsTableDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="pi pi-download" class="p-button-link" v-tooltip.top="$t('common.download')" @click="downloadSnapshot(slotProps.data)" />
                    <Button icon="pi pi-trash" class="p-button-link" @click="deleteSchedulationConfirm(slotProps.data)" />
                </template>
            </Column>
        </DataTable>

        <DocumentExecutionSnapshotDialog :visible="snapshotDialogVisible" :propUrl="url" @close="snapshotDialogVisible = false"></DocumentExecutionSnapshotDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import documentExecutionSchedulationsTableDescriptor from './DocumentExecutionSchedulationsTableDescriptor.json'
import DocumentExecutionSnapshotDialog from './DocumentExecutionSnapshotDialog.vue'

export default defineComponent({
    name: 'document-execution-schedulations-table',
    components: { Column, DataTable, DocumentExecutionSnapshotDialog, Message },
    props: { propSchedulations: { type: Array } },
    emits: ['deleteSchedulation', 'close'],
    data() {
        return {
            documentExecutionSchedulationsTableDescriptor,
            schedulations: [] as any[],
            filters: { global: [filterDefault] } as Object,
            url: '' as any,
            snapshotDialogVisible: false,
            user: null as any
        }
    },
    watch: {
        propSchedulations() {
            this.loadSchedulations()
        }
    },
    created() {
        this.user = (this.$store.state as any).user
        this.loadSchedulations()
    },
    methods: {
        loadSchedulations() {
            this.schedulations = this.propSchedulations as any[]
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        },
        downloadSnapshot(schedulation: any) {
            this.url = process.env.VUE_APP_HOST_URL + `/knowage/servlet/AdapterHTTP?NEW_SESSION=TRUE&user_id=${this.user?.userUniqueIdentifier}&ACTION_NAME=GET_SNAPSHOT_CONTENT&SNAPSHOT_ID=${schedulation.id}&LIGHT_NAVIGATOR_DISABLED=TRUE&OBJECT_ID=${schedulation.biobjId}`
            this.snapshotDialogVisible = true
        },
        deleteSchedulationConfirm(schedulation: any) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteSchedulation', schedulation)
            })
        },
        closeTable() {
            this.schedulations = []
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
#document-execution-schedulations-close-button {
    font-size: 0.75rem;
}
</style>
