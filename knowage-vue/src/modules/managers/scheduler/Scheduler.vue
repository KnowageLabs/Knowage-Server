4 contributors @Redjaw@BojanSovticEngIT@vstanojevic@dbulatovicx32 245 lines (235 sloc) 10.3 KB

<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.scheduler.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showJobDetail" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
                <KnListBox :options="jobs" :settings="schedulerDescriptor.knListSettings" @click="showJobDetail" @delete.stop="deleteJobConfirm" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <SchedulerDetail v-if="selectedJob" :selectedJob="selectedJob" @close="selectedJob = null"></SchedulerDetail>
                <SchedulerHint v-else></SchedulerHint>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iPackage } from './Scheduler'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import SchedulerDetail from './SchedulerDetail.vue'
import SchedulerHint from './SchedulerHint.vue'
import schedulerDescriptor from './SchedulerDescriptor.json'

export default defineComponent({
    name: 'scheduler',
    components: { FabButton, KnListBox, SchedulerDetail, SchedulerHint },
    data() {
        return {
            schedulerDescriptor,
            jobs: [] as iPackage[],
            selectedJob: null as iPackage | null,
            loading: false
        }
    },
    async created() {
        await this.loadJobs()
    },
    methods: {
        async loadJobs() {
            this.loading = true
            this.jobs = []
            let tempJobs = [] as iPackage[]
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs/`).then((response) => (tempJobs = response.data.root))
            tempJobs.forEach((el: iPackage) => {
                if (el.jobGroup === 'BIObjectExecutions') {
                    this.jobs.push({ ...el, numberOfDocuments: el.documents.length })
                }
            })
            this.loading = false
            // console.log('LOADED JOBS: ', this.jobs)
        },
        showJobDetail(event: any) {
            // console.log('EVENT: ', event.item)
            this.selectedJob = event.item ? { ...event.item, edit: true } : { jobName: '', jobDescription: '' }
        },
        async deleteJobConfirm(event: any) {
            // console.log('DELETE EVENT: ', event.item)
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => {
                    await this.deleteJob(event.item.jobName)
                }
            })
        },
        async deleteJob(jobName: string) {
            this.loading = true
            let tempResponse = null as any
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/deleteJob?jobGroup=BIObjectExecutions&jobName=${jobName}`)
                .then((response) => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    tempResponse = response.data
                })
                .catch((error) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: error?.message
                    })
                })

            if (tempResponse?.resp === 'ok') {
                await this.loadJobs()
            }
            this.loading = false
        }
    }
})
</script>
