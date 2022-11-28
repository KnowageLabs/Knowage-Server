<template>
    <Message class="p-m-2" severity="info" :closable="false" :style="workspaceSchedulationOldSchedulationsTableDescriptor.styles.message">
        {{ $t('workspace.schedulation.oldSchedulationsMessage') }}
    </Message>
    <DataTable
        :value="schedulations"
        id="old-chedulations-table"
        class="p-datatable-sm kn-table"
        dataKey="id"
        v-model:filters="filters"
        :globalFilterFields="workspaceSchedulationOldSchedulationsTableDescriptor.globalFilterFields"
        :paginator="schedulations.length > 20"
        :rows="20"
        responsiveLayout="stack"
        breakpoint="600px"
    >
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="workspaceSchedulationOldSchedulationsTableDescriptor.styles.message">
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

        <Column class="kn-truncated" field="name" :header="$t('common.packages')" key="name" :sortable="true"></Column>
        <Column class="kn-truncated" field="dateCreation" :header="$t('common.time')" key="dateCreation" :sortable="true">
            <template #body="slotProps">
                {{ getFormattedDate(slotProps.data.dateCreation, 'MMM DD, YYYY h:mm:ss A') }}
            </template>
        </Column>
        <Column :style="workspaceSchedulationOldSchedulationsTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button icon="pi pi-download" class="p-button-link" v-tooltip.top="$t('common.download')" @click="downloadSnapshot(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { ISchedulation } from '@/modules/workspace/Workspace'
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import workspaceSchedulationOldSchedulationsTableDescriptor from './WorkspaceSchedulationOldSchedulationsTableDescriptor.json'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'workspace-schedulation-old-schedulations-table',
    components: { Column, DataTable, Message },
    props: { propSchedulations: { type: Array } },
    data() {
        return {
            workspaceSchedulationOldSchedulationsTableDescriptor,
            schedulations: [] as ISchedulation[],
            filters: { global: [filterDefault] } as any,
            user: null as any
        }
    },
    watch: {
        propSchedulations() {
            this.loadSchedulations()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.user = (this.store.$state as any).user
        this.loadSchedulations()
    },
    methods: {
        loadSchedulations() {
            this.schedulations = this.propSchedulations as ISchedulation[]
        },
        getFormattedDate(date: any, format: any) {
            return formatDate(date, format)
        },
        downloadSnapshot(schedulation: ISchedulation) {
            const url = import.meta.env.VITE_HOST_URL + `/knowage/servlet/AdapterHTTP?NEW_SESSION=TRUE&user_id=${this.user?.userUniqueIdentifier}&ACTION_NAME=GET_SNAPSHOT_CONTENT&SNAPSHOT_ID=${schedulation.id}&LIGHT_NAVIGATOR_DISABLED=TRUE&OBJECT_ID=${schedulation.biobjId}`
            window.open(url, '_blank')
        }
    }
})
</script>
