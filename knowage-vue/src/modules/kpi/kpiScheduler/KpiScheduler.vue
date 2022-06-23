<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('kpi.kpiScheduler.title') }}
                    </template>
                    <template #end>
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
                        <div class="kn-list-item" :class="getBordersClass(slotProps.option.jobStatus)" data-test="list-item">
                            <div class="kn-list-item-text">
                                <div>
                                    <span>{{ slotProps.option.name }}</span>
                                </div>
                                <div class="p-d-flex p-flex-row kn-truncated">
                                    <Chip class="p-m-1" v-tooltip.top="slotProps.option.kpiNames" v-for="(kpiName, index) in slotProps.option.kpiNames.split(',')" :key="index" :label="kpiName"></Chip>
                                </div>
                            </div>
                            <i v-if="slotProps.option.jobStatus.toUpperCase() !== 'EXPIRED'" :class="playIcon(slotProps.option.jobStatus)" @click="startSchedule(slotProps.option)" />
                            <Button class="p-button-link p-button-sm" icon="fa fa-ellipsis-v" @click="toggle($event, slotProps.option)" aria-haspopup="true" aria-controls="overlay_menu" data-test="menu-button" />
                            <Menu ref="menu" :model="items" :popup="true" data-test="menu"></Menu>
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
import { AxiosResponse } from 'axios'
import Chip from 'primevue/chip'
import kpiSchedulerDescriptor from './KpiSchedulerDescriptor.json'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'

export default defineComponent({
    name: 'kpi-scheduler',
    components: { Chip, FabButton, Listbox, Menu },
    data() {
        return {
            kpiSchedulerDescriptor,
            schedulerList: [] as iKpiSchedule[],
            items: [] as { label: String; icon: string; command: Function }[],
            loading: false,
            touched: false
        }
    },

    async created() {
        await this.loadPage()
    },
    methods: {
        async loadAllSchedules() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/kpi/listSchedulerKPI')
                .then((response: AxiosResponse<any>) => {
                    this.schedulerList = response.data
                    this.schedulerList.sort((a: iKpiSchedule, b: iKpiSchedule) => (a.name.toUpperCase() > b.name.toUpperCase() ? 1 : -1))
                })
                .finally(() => (this.loading = false))
        },
        async loadPage() {
            this.loading = true
            await this.loadAllSchedules()
            this.touched = false
            this.loading = false
        },
        toggle(event: any, scheduler: iKpiSchedule) {
            this.createMenuItems(scheduler)
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems(scheduler: iKpiSchedule) {
            this.items = []
            this.items.push({ label: this.$t('common.clone'), icon: 'pi pi-copy', command: () => this.cloneSchedulerConfirm(scheduler) })
            this.items.push({ label: this.$t('common.delete'), icon: 'far fa-trash-alt', command: () => this.deleteScheduleConfirm(scheduler.id as number) })
        },
        playIcon(jobStatus: string) {
            return jobStatus.toUpperCase() === 'SUSPENDED' ? 'fa fa-play' : 'fa fa-pause'
        },
        cloneSchedulerConfirm(scheduler: iKpiSchedule) {
            this.$confirm.require({
                header: this.$t('common.toast.cloneConfirmTitle'),
                accept: () => this.showForm(scheduler, true)
            })
        },
        showForm(event: any, clone: boolean) {
            clone = clone ? true : false

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
            if (schedule.jobStatus?.toUpperCase() === 'EXPIRED') {
                return
            }
            const query = '?jobGroup=KPI_SCHEDULER_GROUP&triggerGroup=KPI_SCHEDULER_GROUP&jobName=' + schedule.id + '&triggerName=' + schedule.id
            const action = schedule.jobStatus?.toUpperCase() === 'SUSPENDED' ? 'resumeTrigger' : 'pauseTrigger'
            this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + 'scheduler/' + action + query).then((response: AxiosResponse<any>) => {
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
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${scheduleId}/deleteKpiScheduler`).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/kpi-scheduler')
                this.loadPage()
            })
        },
        getBordersClass(jobStatus: string) {
            switch (jobStatus) {
                case 'SUSPENDED':
                    return 'kn-list-item-warning'
                case 'ACTIVE':
                    return 'kn-list-item-success'
                case 'EXPIRED':
                    return 'kn-list-item-error'
            }
        }
    }
})
</script>

<style lang="scss" scoped>
::v-deep(.p-chip-text) {
    font-size: 0.5rem;
}
</style>
