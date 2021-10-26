<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary">
        <template #left>
            {{ $t('workspace.schedulation.title') }}
        </template>

        <template #right>
            <Button class="kn-button p-button-text p-button-rounded" @click="runAllSchedulations">{{ $t('common.run') }}</Button>
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
    <WorkspaceSchedulationTable class="overflow p-m-2" :propJobs="jobs" @runSchedulationClick="runSingleSchedulation" @schedulationsSelected="setSelectedSchedulations"></WorkspaceSchedulationTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { IPackage, ITrigger } from '../../Workspace'
import WorkspaceSchedulationTable from './tables/WorkspaceSchedulationTable.vue'

export default defineComponent({
    name: 'workspace-schedulation-view',
    components: { WorkspaceSchedulationTable },
    data() {
        return {
            jobs: [] as IPackage[],
            selectedSchedulations: {} as any,
            loading: false
        }
    },
    async created() {
        await this.loadJobs()
    },
    methods: {
        async loadJobs() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs`).then((response) => (this.jobs = response.data.root))
            this.loading = false
        },
        setSelectedSchedulations(schedulations: any) {
            this.selectedSchedulations = schedulations
        },
        async runSingleSchedulation(schedulation: ITrigger) {
            console.log('RUN SCHEDULATION: ', schedulation)
            await this.runSchedulations([{ jobName: schedulation.jobName, jobGroup: schedulation.jobGroup, triggerName: schedulation.triggerName, triggerGroup: schedulation.triggerGroup }])
        },
        async runAllSchedulations() {
            console.log('RUN ALL SCHEDULATIONS CLICKED!', this.selectedSchedulations)
            // console.log('FORMATED SCHEDUL', this.getFormatedSchedulations())
            const formatedSchedulations = this.getFormatedSchedulations()
            await this.runSchedulations(formatedSchedulations)
        },
        getFormatedSchedulations() {
            let formatedSchedulations = [] as ITrigger[]
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
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/executeMultipleTrigger`, schedulations)
                .then((response) => {
                    if (response.data.resp === 'ok') {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.information'),
                            msg: this.$t('managers.scheduler.schedulationExecuted')
                        })
                    }
                })
                .catch(() => {})
            this.loading = false
        }
    }
})
</script>

<style lang="scss">
.overflow {
    overflow: auto;
}
</style>
