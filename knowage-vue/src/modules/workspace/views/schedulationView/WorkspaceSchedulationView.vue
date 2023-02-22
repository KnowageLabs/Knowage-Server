<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #start>
            <Button id="showSidenavIcon" icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" @click="$emit('showMenu')" />
            {{ $t('workspace.schedulation.title') }}
        </template>

        <template #end>
            <Button v-if="canRunScheduledExecutions" class="kn-button p-button-text p-button-rounded" @click="runAllSchedulations">{{ $t('common.run') }}</Button>
        </template>
    </Toolbar>
    <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />
    <WorkspaceSchedulationTable class="overflow p-m-2" :prop-jobs="jobs" @runSchedulationClick="runSingleSchedulation" @schedulationsSelected="setSelectedSchedulations" @viewOldSchedulationsClick="viewOldSchedulations"></WorkspaceSchedulationTable>

    <WorkspaceSchedulationOldSchedulationsDialog :visible="schedulationsDialogVisible" :selected-job="selectedJob" @close="closeOldSchedulationsDialog"></WorkspaceSchedulationOldSchedulationsDialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { IPackage, ITrigger } from '../../Workspace'
import WorkspaceSchedulationOldSchedulationsDialog from './dialog/WorkspaceSchedulationOldSchedulationsDialog.vue'
import WorkspaceSchedulationTable from './tables/WorkspaceSchedulationTable.vue'
import mainStore from '../../../../App.store.js'
import { mapActions, mapState } from 'pinia'

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
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        }),
        canRunScheduledExecutions(): any {
            return this.user.functionalities.includes('RunSnapshotsFunctionality')
        }
    },
    async created() {
        await this.loadJobs()
    },
    methods: {
        ...mapActions(mainStore, ['setInfo']),
        async loadJobs() {
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs`).then((response: AxiosResponse<any>) => (this.jobs = response.data.root))
            this.loading = false
        },
        setSelectedSchedulations(schedulations: any) {
            this.selectedSchedulations = schedulations
        },
        async runSingleSchedulation(schedulation: ITrigger) {
            await this.runSchedulations([{ jobName: schedulation.jobName, jobGroup: schedulation.jobGroup, triggerName: schedulation.triggerName, triggerGroup: schedulation.triggerGroup }])
        },
        async runAllSchedulations() {
            const formatedSchedulations = this.getFormatedSchedulations()
            await this.runSchedulations(formatedSchedulations)
        },
        getFormatedSchedulations() {
            const formatedSchedulations = [] as ITrigger[]
            Object.keys(this.selectedSchedulations).forEach((key) => {
                this.selectedSchedulations[key].forEach((schedulation: ITrigger) => {
                    formatedSchedulations.push({ jobName: schedulation.jobName, jobGroup: schedulation.jobGroup, triggerName: schedulation.triggerName, triggerGroup: schedulation.triggerGroup })
                })
            })
            return formatedSchedulations
        },
        async runSchedulations(schedulations: any) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/executeMultipleTrigger`, schedulations)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.resp === 'ok') {
                        this.setInfo({
                            title: this.$t('common.information'),
                            msg: this.$t('managers.scheduler.schedulationExecuted')
                        })
                    }
                })
                .catch(() => {})
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
