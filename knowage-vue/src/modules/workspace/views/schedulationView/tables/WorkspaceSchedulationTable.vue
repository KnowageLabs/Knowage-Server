<template>
    <DataTable
        :value="jobs"
        id="jobs-table"
        class="p-datatable-sm kn-table"
        v-model:expandedRows="expandedRows"
        dataKey="jobName"
        v-model:filters="filters"
        :globalFilterFields="workspaceSchedulationTableDescriptor.globalFilterFields"
        :paginator="true"
        :rows="20"
        responsiveLayout="stack"
        breakpoint="960px"
    >
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

        <template #expansion="slotProps">
            <WorkspaceSchedulationSchedulationsTable
                class="p-m-4"
                :triggers="slotProps.data.triggers"
                :index="slotProps.index"
                :propSelectedSchedulations="selectedSchedulations"
                @runSchedulationClick="$emit('runSchedulationClick', $event)"
                @selectedSchedulations="setSelectedSchedulations"
            ></WorkspaceSchedulationSchedulationsTable>
        </template>
        <Column :expander="true" :headerStyle="workspaceSchedulationTableDescriptor.expanderHeaderStyle" />

        <Column class="kn-truncated" v-for="col of workspaceSchedulationTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
        <Column :style="workspaceSchedulationTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button v-if="canSeeScheduledExecutions" icon="fa fa-eye" class="p-button-link" v-tooltip.left="$t('workspace.schedulation.ranSchedulations')" @click.stop="viewRanSchedulations(slotProps.data)" />
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
import WorkspaceSchedulationSchedulationsTable from './WorkspaceSchedulationSchedulationsTable.vue'
import workspaceSchedulationTableDescriptor from './WorkspaceSchedulationTableDescriptor.json'
import { mapState } from 'pinia'
import mainStore from '../../../../../App.store.js'

export default defineComponent({
    name: 'workspace-schedulation-table',
    components: { Column, DataTable, Message, WorkspaceSchedulationSchedulationsTable },
    props: { propJobs: { type: Array } },
    emits: ['runSchedulationClick', 'schedulationsSelected', 'viewOldSchedulationsClick'],
    data() {
        return {
            workspaceSchedulationTableDescriptor,
            jobs: [] as IPackage[],
            expandedRows: [] as any[],
            filters: { global: [filterDefault] } as any,
            selectedSchedulations: {} as any
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        }),
        canSeeScheduledExecutions(): any {
            return this.user.functionalities.includes('ViewScheduledWorkspace')
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
        },
        setSelectedSchedulations(payload: any) {
            this.selectedSchedulations[payload.index] = payload.schedulations
            this.$emit('schedulationsSelected', this.selectedSchedulations)
        }
    }
})
</script>
