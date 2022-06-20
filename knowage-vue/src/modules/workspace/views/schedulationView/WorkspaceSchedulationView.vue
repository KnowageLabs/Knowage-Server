<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.schedulation.title') }}
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <WorkspaceSchedulationTable class="kn-overflow p-m-2" :propJobs="jobs" @viewOldSchedulationsClick="viewOldSchedulations" data-test="schedulation-table"></WorkspaceSchedulationTable>

    <WorkspaceSchedulationOldSchedulationsDialog :visible="schedulationsDialogVisible" :selectedJob="selectedJob" @close="closeOldSchedulationsDialog"></WorkspaceSchedulationOldSchedulationsDialog>
</template>

<script lang="ts">
    import { AxiosResponse } from 'axios'
    import { defineComponent } from 'vue'
    import { IPackage } from '../../Workspace'
    import WorkspaceSchedulationOldSchedulationsDialog from './dialog/WorkspaceSchedulationOldSchedulationsDialog.vue'
    import WorkspaceSchedulationTable from './tables/WorkspaceSchedulationTable.vue'

    export default defineComponent({
        name: 'workspace-schedulation-view',
        components: { WorkspaceSchedulationOldSchedulationsDialog, WorkspaceSchedulationTable },
        emits: ['showMenu'],
        data() {
            return {
                jobs: [] as IPackage[],
                selectedSchedulations: {} as any,
                schedulationsDialogVisible: false,
                selectedJob: null as IPackage | null,
                loading: false
            }
        },
        async created() {
            await this.loadJobs()
        },
        methods: {
            async loadJobs() {
                this.loading = true
                await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs`).then((response: AxiosResponse<any>) => (this.jobs = response.data.root))
                this.loading = false
            },
            viewOldSchedulations(job: IPackage) {
                this.selectedJob = job
                this.schedulationsDialogVisible = true
            },
            closeOldSchedulationsDialog() {
                this.schedulationsDialogVisible = false
                this.selectedJob = null
            }
        }
    })
</script>
