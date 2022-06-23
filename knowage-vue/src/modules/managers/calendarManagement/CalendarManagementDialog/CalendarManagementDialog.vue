<template>
    <Dialog id="calendar-management-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="calendarManagementDialogDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ calendar?.calendar }}
                </template>
            </Toolbar>
        </template>

        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <div>
            <CalendarManagementDetailForm :propCalendar="calendar" :generateButtonVisible="generateButtonVisible" :generateButtonDisabled="generateButtonDisabled" @generateCalendarClicked="generateCalendarConfirm"></CalendarManagementDetailForm>
            <CalendarManagementDetailTable v-if="calendarDetailTableVisible" class="p-m-4" :propCalendarInfo="calendar?.splittedCalendar" :domains="domains"></CalendarManagementDetailTable>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="close"> {{ $t('common.cancel') }}</Button>
            <Button v-if="canManageCalendar" class="kn-button kn-button--primary" :disabled="buttonDisabled" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iCalendar, iCalendarDate, iDomain } from '../CalendarManagement'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import calendarManagementDialogDescriptor from './CalendarManagementDialogDescriptor.json'
import CalendarManagementDetailForm from './CalendarManagementDetailForm/CalendarManagementDetailForm.vue'
import CalendarManagementDetailTable from './CalendarManagementDetailTable/CalendarManagementDetailTable.vue'
import moment from 'moment'

import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'calendar-management-dialog',
    components: { CalendarManagementDetailForm, CalendarManagementDetailTable, Dialog },
    props: { visible: { type: Boolean }, propCalendar: { type: Object as PropType<iCalendar | null> }, domains: { type: Array as PropType<iDomain[]> } },
    emits: ['close', 'calendarSaved'],
    data() {
        return {
            calendarManagementDialogDescriptor,
            calendar: null as iCalendar | null,
            calendarInfo: [] as iCalendarDate[],
            generateButtonVisible: false,
            generateButtonDisabled: true,
            calendarDetailTableVisible: false,
            loading: false
        }
    },
    computed: {
        buttonDisabled(): boolean {
            return this.calendar === null || !this.calendar.calendar || !this.calendar.calStartDay || !this.calendar.calEndDay
        },
        canManageCalendar(): boolean {
            return (this.store.state as any).user.functionalities.includes('ManageCalendar')
        }
    },
    watch: {
        propCalendar() {
            if (this.visible) this.loadCalendar()
        }
    },
    created() {
        this.loadCalendar()
    },
    methods: {
        async loadCalendar() {
            this.calendar = deepcopy(this.propCalendar)
            if (this.calendar && this.calendar.calStartDay) this.calendar.calStartDay = new Date(this.calendar.calStartDay)
            if (this.calendar && this.calendar.calEndDay) this.calendar.calEndDay = new Date(this.calendar.calEndDay)

            if (this.calendar?.calendarId) {
                await this.loadCalendarInfo(this.calendar.calendarId)
                this.calendarDetailTableVisible = true
            }
        },
        async loadCalendarInfo(calendarId: number) {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/${calendarId}/getInfoCalendarById`)
                .then((response: AxiosResponse<any>) => {
                    this.calendarInfo = response.data
                    this.generateButtonVisible = true
                    this.getRealCalendarInfo()
                })
                .catch(() => {})
            this.loading = false
        },
        getRealCalendarInfo() {
            if (!this.calendar) return

            this.calendar.splittedCalendar = []
            for (let i = 0; i < this.calendarInfo.length; i++) {
                const tempDate = deepcopy(this.calendarInfo[i])

                tempDate['day'] = tempDate.timeByDay.dayName
                tempDate.isHoliday = tempDate.isHoliday == 1
                tempDate.pubHoliday = tempDate.pubHoliday == 'true'

                tempDate.listOfAttributes = tempDate.listOfAttributes?.map((attribute: any) => attribute.calendarAttributeDomain.attributeDomainDescr)

                this.calendar.splittedCalendar.push(tempDate)
            }
        },
        async save() {
            this.loading = true
            if (this.calendar?.calendarId) {
                await this.updateCalendar()
            } else {
                await this.saveCalendar()
            }

            this.loading = false
        },
        async saveCalendar() {
            const tempCalendar = deepcopy(this.calendar)
            tempCalendar.calStartDay = moment(tempCalendar.calStartDay).valueOf()
            tempCalendar.calEndDay = moment(tempCalendar.calEndDay).valueOf()

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/saveCalendar`, tempCalendar)
                .then((response: AxiosResponse<any>) => {
                    this.store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.createSuccess')
                    })
                    if (this.calendar) this.calendar.calendarId = response.data
                    this.generateButtonVisible = true
                    this.generateButtonDisabled = false
                    this.$emit('calendarSaved')
                })
                .catch(() => {})
        },
        async updateCalendar() {
            if (!this.calendar) return

            const postData = this.getFormattedSplittedCalendar()

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/${this.calendar.calendarId}/updateDaysGenerated`, postData)
                .then(() => {
                    this.store.commit('setInfo', {
                        title: this.$t('common.toast.updateTitle'),
                        msg: this.$t('common.toast.updateSuccess')
                    })
                })
                .catch(() => {})
        },
        getFormattedSplittedCalendar() {
            if (!this.calendar) return
            const tempData = deepcopy(this.calendar.splittedCalendar)
            tempData?.forEach((el: any) => {
                el.isHoliday = el.isHoliday ? 1 : null
                el.pubHoliday = el.pubHoliday ? 'true' : null
                delete el.date
                delete el.day
                delete el.checkEvent
            })

            return tempData
        },
        generateCalendarConfirm() {
            this.$confirm.require({
                message: this.$t('managers.calendarManagement.generateConfirmMessage', { numberOfDays: this.getDays() }),
                header: this.$t('managers.calendarManagement.generateConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.generateCalendar()
            })
        },
        getDays() {
            if (!this.calendar) return

            const timeDiff = Math.abs((this.calendar.calEndDay as Date).getTime() - (this.calendar.calStartDay as Date).getTime())
            const diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24))
            return diffDays + 1
        },
        async generateCalendar() {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `calendar/${this.calendar?.calendarId}/generateCalendarDays`, {})
                .then(async () => {
                    await this.loadCalendarInfo(this.calendar?.calendarId as number)
                    this.generateButtonDisabled = true
                    this.calendarDetailTableVisible = true
                })
                .catch(() => {})
            this.loading = false
        },
        close() {
            this.calendar = null
            this.generateButtonVisible = false
            this.generateButtonDisabled = true
            this.calendarDetailTableVisible = false
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#calendar-management-dialog .p-dialog-header,
#calendar-management-dialog .p-dialog-content {
    padding: 0;
}
#calendar-management-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
