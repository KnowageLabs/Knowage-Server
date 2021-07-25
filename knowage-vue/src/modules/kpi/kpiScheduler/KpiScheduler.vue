<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('kpi.kpiScheduler.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click="showForm" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list"
                    :options="schedulerList"
                    :listStyle="kpiSchedulerDescriptor.listBox.style"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    filterMatchMode="contains"
                    :filterFields="kpiSchedulerDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="showForm($event.value)"
                    data-test="scheduler-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" data-test="list-item">
                            <div class="kn-list-item-text">
                                <div>
                                    <i :class="kpiSchedulerDescriptor.iconTypesMap[slotProps.option.jobStatus]"></i>
                                    <span class="p-ml-2">{{ slotProps.option.name }}</span>
                                </div>
                                <div class="p-d-flex p-flex-row kn-truncated">
                                    <Chip class="p-m-1" v-tooltip.top="slotProps.option.kpiNames" v-for="(kpiName, index) in slotProps.option.kpiNames.split(',')" :key="index" :label="kpiName"></Chip>
                                </div>
                            </div>
                            <Button icon="pi pi-copy" class="p-button-link" @click.stop="showForm(slotProps.option, true)" />
                            <Button icon="far fa-trash-alt" class="p-button-link p-button-sm" @click.stop="deleteScheduleConfirm(slotProps.option.id)" :data-test="'delete-button-' + slotProps.option.id" />
                            <Button v-if="slotProps.option.jobStatus.toUpperCase() !== 'EXPIRED'" :icon="playIcon(slotProps.option.jobStatus)" class="p-button-link" @click="startSchedule(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view @touched="touched = true" @closed="touched = false" @inserted="loadPage" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iKpiSchedule } from './KpiScheduler'
import axios from 'axios'
import Chip from 'primevue/chip'
import kpiSchedulerDescriptor from './KpiSchedulerDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'kpi-scheduler',
    components: { Chip, FabButton, Listbox },
    data() {
        return {
            kpiSchedulerDescriptor,
            schedulerList: [] as iKpiSchedule[],
            loading: false,
            touched: false
        }
    },

    async created() {
        await this.loadPage()
        // console.log('SCHEDULER LIST: ', this.schedulerList)
    },
    methods: {
        async loadAllSchedules() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/listSchedulerKPI')
                .then((response) => {
                    this.schedulerList = response.data
                    this.schedulerList.sort((a: iKpiSchedule, b: iKpiSchedule) => (a.name.toUpperCase() > b.name.toUpperCase() ? 1 : -1))
                })
                .finally(() => (this.loading = false))
        },
        async loadPage() {
            this.loading = true
            await this.loadAllSchedules()
            // const id = schedulerId ? schedulerId : this.selectedScheduler.id
            // this.selectedScheduler = this.schedulerList.find((scheduler: iScheduler) => scheduler.id === id)
            this.touched = false
            this.loading = false
        },
        playIcon(jobStatus: string) {
            // console.log('jobStatus: ', jobStatus)
            return jobStatus.toUpperCase() === 'SUSPENDED' ? 'fa fa-play' : 'fa fa-pause'
        },
        showForm(event: any, clone: boolean) {
            clone = clone ? true : false
            // console.log('SCHEDULE: ', event)
            const path = event.id ? `/kpi-scheduler/edit-kpi-schedule?id=${event.id}&clone=${clone}` : '/kpi-scheduler/new-kpi-schedule'
            if (!this.touched) {
                this.$router.push(path)
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.$router.push(path)
                    }
                })
            }
        },
        startSchedule(schedule: iKpiSchedule) {
            // console.log('SCHEDULE: ', schedule)
            if (schedule.jobStatus.toUpperCase() === 'EXPIRED') {
                return
            }
            const query = '?jobGroup=KPI_SCHEDULER_GROUP&triggerGroup=KPI_SCHEDULER_GROUP&jobName=' + schedule.id + '&triggerName=' + schedule.id
            const action = schedule.jobStatus.toUpperCase() === 'SUSPENDED' ? 'resumeTrigger' : 'pauseTrigger'
            axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '/scheduler/' + action + query).then((response) => {
                console.log('RESPONSE: ', response)
                if (response.data.resp === 'ok') {
                    schedule.jobStatus = schedule.jobStatus === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED'
                }
            })
        },
        deleteScheduleConfirm(scheduleId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteSchedule(scheduleId)
            })
        },
        async deleteSchedule(scheduleId: number) {
            await axios.delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${scheduleId}/deleteKpiScheduler`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/kpi-scheduler')
                this.loadPage()
            })
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-chip-text) {
    font-size: 0.5rem;
}
</style>
