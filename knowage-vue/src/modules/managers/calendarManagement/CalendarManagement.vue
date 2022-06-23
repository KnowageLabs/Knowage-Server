<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('managers.calendarManagement.title') }}
            </template>
            <template #end>
                <KnFabButton v-show="canManageCalendar" icon="fas fa-plus" @click="showForm()"></KnFabButton>
            </template>
        </Toolbar>
        <KnOverlaySpinnerPanel :visibility="loading" data-test="spinner" />
        <div class="kn-page-content p-grid p-m-0">
            <div class="p-col" v-if="!loading">
                <DataTable
                    :value="calendars"
                    :paginator="calendars.length > 20"
                    :loading="loading"
                    :rows="20"
                    class="p-datatable-sm kn-table"
                    dataKey="id"
                    v-model:filters="filters"
                    :globalFilterFields="calendarManagementDescriptor.globalFilterFields"
                    responsiveLayout="stack"
                    breakpoint="960px"
                    @rowClick="showForm($event)"
                    data-test="calendar-table"
                >
                    <template #header>
                        <div class="table-header">
                            <span class="p-input-icon-left">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" data-test="search-input" />
                            </span>
                        </div>
                    </template>
                    <template #empty>
                        {{ $t('common.info.noDataFound') }}
                    </template>
                    <template #loading>
                        {{ $t('common.info.dataLoading') }}
                    </template>
                    <Column class="kn-truncated" field="calendar" :header="$t('common.name')" :sortable="true"></Column>
                    <Column class="kn-truncated" field="calStartDay" :header="$t('cron.startDate')" :sortable="true">
                        <template #body="slotProps">
                            <span :data-test="'calendar-start-day-' + slotProps.data.calendar">{{ getFormattedDate(slotProps.data.calStartDay) }}</span>
                        </template>
                    </Column>
                    <Column class="kn-truncated" field="calEndDay" :header="$t('cron.endDate')" :sortable="true">
                        <template #body="slotProps">
                            <span>{{ getFormattedDate(slotProps.data.calEndDay) }}</span>
                        </template>
                    </Column>
                    <Column class="kn-truncated" field="calType" :header="$t('common.type')" :sortable="true"></Column>
                    <Column v-if="canManageCalendar" :style="calendarManagementDescriptor.iconColumnStyle">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteCalendarConfirm(slotProps.data)" :data-test="'delete-button-' + slotProps.data.calendar" />
                        </template>
                    </Column>
                </DataTable>
            </div>
        </div>

        <CalendarManagementDialog :visible="calendarDialogVisible" :propCalendar="selectedCalendar" :domains="domains" @close="closeCalendarDialog" @calendarSaved="onCalendarSaved"></CalendarManagementDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCalendar, iDomain } from './CalendarManagement'
import { AxiosResponse } from 'axios'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { formatDate } from '@/helpers/commons/localeHelper'
import moment from 'moment'
import calendarManagementDescriptor from './CalendarManagementDescriptor.json'
import CalendarManagementDialog from './CalendarManagementDialog/CalendarManagementDialog.vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'

export default defineComponent({
    name: 'calendar-management',
    components: { CalendarManagementDialog, Column, DataTable, KnFabButton, KnOverlaySpinnerPanel },
    data() {
        return {
            calendarManagementDescriptor,
            calendars: [] as iCalendar[],
            domains: [] as iDomain[],
            filters: { global: [filterDefault] },
            selectedCalendar: null as iCalendar | null,
            calendarDialogVisible: false,
            loading: false
        }
    },
    computed: {
        canManageCalendar(): boolean {
            return (this.store.$state as any).user.functionalities.includes('ManageCalendar')
        }
    },
    async created() {
        await this.loadCalendars()
        await this.loadDomains()
    },
    methods: {
        async loadCalendars() {
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/getCalendarList`).then((response: AxiosResponse<any>) => (this.calendars = response.data))
            this.loading = false
        },
        async loadDomains() {
            this.loading = true
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/getDomains`).then((response: AxiosResponse<any>) => (this.domains = response.data))
            this.loading = false
        },
        showForm(event: any = null) {
            this.selectedCalendar = event ? event.data : this.createNewCalendar()
            this.calendarDialogVisible = true
        },
        getFormattedDate(date: number) {
            const tempDate = moment(date).format('DD/MM/YYYY')
            return formatDate(tempDate, '', 'DD/MM/YYYY')
        },
        deleteCalendarConfirm(calendar: iCalendar) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteCalendar(calendar)
            })
        },
        async deleteCalendar(calendar: iCalendar) {
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/${calendar.calendarId}/deleteCalendar`)
                .then(() => {
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.removeCalendar(calendar)
                })
                .catch(() => {})
        },
        removeCalendar(calendar: iCalendar) {
            const index = this.calendars.findIndex((tempCalendar: iCalendar) => tempCalendar.calendarId === calendar.calendarId)
            if (index !== -1) this.calendars.splice(index, 1)
        },
        createNewCalendar() {
            return {
                realDateGenerated: [],
                splittedCalendar: [],
                calStartDay: null,
                calEndDay: null,
                calendar: '',
                calType: ''
            } as iCalendar
        },
        closeCalendarDialog() {
            this.selectedCalendar = null
            this.calendarDialogVisible = false
        },
        onCalendarSaved() {
            this.loadCalendars()
        }
    }
})
</script>
