<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.scheduler.title') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click="showJobDetail(null, false)" data-test="progress-bar" />
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
                <KnListBox :options="jobs" :settings="schedulerDescriptor.knListSettings" @click="showJobDetail($event, false)" @clone.stop="showJobDetail($event, true)" @delete.stop="deleteJobConfirm" />
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <router-view :selectedJob="selectedJob" @closed="touched = false" @documentSaved="loadSelectJob" @close="closeDetail" @triggerSaved="loadPage" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iPackage } from './Scheduler'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import schedulerDescriptor from './SchedulerDescriptor.json'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'scheduler',
    components: { FabButton, KnListBox },
    data() {
        return {
            schedulerDescriptor,
            jobs: [] as iPackage[],
            selectedJob: null as iPackage | null,
            loading: false
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            if (this.$route.query.id) {
                await this.loadSelectJob(this.$route.query.id as string)
            } else {
                this.selectedJob = { jobName: '', jobDescription: '', documents: [], triggers: [], edit: false } as any
                await this.loadJobs()
            }
        },
        async loadJobs() {
            this.loading = true
            this.jobs = []
            let tempJobs = [] as iPackage[]
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/listAllJobs/`).then((response: AxiosResponse<any>) => (tempJobs = response.data.root))
            tempJobs.forEach((el: iPackage) => {
                if (el.jobGroup === 'BIObjectExecutions') {
                    this.jobs.push({ ...el, numberOfDocuments: el.documents.length })
                }
            })
            this.loading = false
        },
        showJobDetail(event: any, clone: boolean) {
            this.selectedJob = event && event.item ? { ...event.item, edit: true } : { jobName: '', jobDescription: '', documents: [], triggers: [] }

            if (clone && this.selectedJob) {
                this.selectedJob.jobName = this.$t('common.copyOf') + ' ' + this.selectedJob.jobName
                this.selectedJob.edit = false
            }

            const path = event && event.item ? `/scheduler/edit-package-schedule?id=${event.item.jobName}&clone=${clone}` : '/scheduler/new-package-schedule'
            this.$router.push(path)
        },
        async deleteJobConfirm(event: any) {
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
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `scheduleree/deleteJob?jobGroup=BIObjectExecutions&jobName=${jobName}`)
                .then((response: AxiosResponse<any>) => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    tempResponse = response.data
                })
                .catch((error) => {
                    this.store.setError({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: error?.message
                    })
                })

            if (tempResponse?.resp === 'ok') {
                await this.loadJobs()
                this.selectedJob = null
                this.$router.push('/scheduler')
            }
            this.loading = false
        },
        async loadSelectJob(name: string) {
            await this.loadJobs()

            this.selectedJob = this.jobs.find((el: iPackage) => {
                return el.jobName === name
            }) as iPackage

            if (this.selectedJob) {
                this.selectedJob.edit = true
            }
        },
        closeDetail() {
            this.selectedJob = null
        }
    }
})
</script>
