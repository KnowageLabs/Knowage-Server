<template>
    <DataTable :value="jobs" id="jobs-table" class="p-datatable-sm kn-table" dataKey="jobName" v-model:filters="filters" :globalFilterFields="workspaceSchedulationTableDescriptor.globalFilterFields" responsiveLayout="stack" breakpoint="600px">
        <template #header>
            <div class="table-header p-d-flex p-ai-center">
                <span id="search-container" class="p-input-icon-left p-mr-3">
                    <i class="pi pi-search" />
                    <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" data-test="search-input" />
                </span>
            </div>
        </template>
        <template #empty>
            <Message class="p-m-2" severity="info" :closable="false" :style="workspaceSchedulationTableDescriptor.styles.message">
                {{ $t('common.info.noDataFound') }}
            </Message>
        </template>

        <Column class="kn-truncated" v-for="col of workspaceSchedulationTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
        <Column :style="workspaceSchedulationTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button icon="fa fa-eye" class="p-button-link" v-tooltip.left="$t('workspace.schedulation.ranSchedulations')" @click.stop="viewRanSchedulations(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IPackage } from '../../../Workspace'
import { filterDefault } from '@/helpers/commons/filterHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import workspaceSchedulationTableDescriptor from './WorkspaceSchedulationTableDescriptor.json'

export default defineComponent({
    name: 'workspace-schedulation-table',
    components: { Column, DataTable, Message },
    props: { propJobs: { type: Array } },
    emits: ['viewOldSchedulationsClick'],
    data() {
        return {
            workspaceSchedulationTableDescriptor,
            jobs: [] as IPackage[],
            filters: { global: [filterDefault] } as Object
        }
    },
    watch: {
        propJobs() {
            this.loadJobs()
        }
    },
    created() {
        this.loadJobs()
    },
    methods: {
        loadJobs() {
            this.jobs = this.propJobs as IPackage[]
        },
        viewRanSchedulations(job: IPackage) {
            this.$emit('viewOldSchedulationsClick', job)
        }
    }
})
</script>
