<template>
    <Card class="p-m-2">
        <template #content>
            <div class="date-picker-container p-ml-2 p-mb-5">
                <div class="p-field p-grid">
                    <label for="startDate" class="kn-material-input-label p-col-12  p-md-1 p-mb-md-0"> {{ $t('cron.startDate') }}: </label>
                    <span>
                        <Calendar
                            id="startDate"
                            v-model="startTemp"
                            class="kn-material-input"
                            style="width:20rem"
                            :class="{
                                'p-invalid': !validDates
                            }"
                            :show-icon="true"
                            :manual-input="true"
                            @date-select="setDate($event, 'startDate')"
                        />
                        <div v-if="!validDates" class="p-error p-grid p-mt-1">
                            <small class="p-col-12">
                                {{ $t('kpi.kpiScheduler.dateError') }}
                            </small>
                        </div>
                    </span>
                </div>
                <div class="p-field p-grid">
                    <label for="endDate" class="kn-material-input-label p-col-12  p-md-1 p-mb-md-0"> {{ $t('cron.endDate') }}: </label>
                    <span>
                        <Calendar
                            id="endDate"
                            v-model="endTemp"
                            class="kn-material-input"
                            style="width:20rem"
                            :class="{
                                'p-invalid': !validDates
                            }"
                            :show-icon="true"
                            :manual-input="true"
                            :show-button-bar="true"
                            @date-select="setDate($event, 'endDate')"
                        />
                        <div v-if="!validDates" class="p-error p-grid p-mt-1">
                            <small class="p-col-12">
                                {{ $t('kpi.kpiScheduler.dateError') }}
                            </small>
                        </div>
                    </span>
                </div>
            </div>
            <div class="p-d-flex p-grid p-mt-5">
                <div class="p-mx-2">
                    <label for="repeatInterval" class="kn-material-input-label p-m-2"> {{ $t('managers.functionalitiesManagement.execution') }}: </label>
                    <span>
                        <Dropdown id="repeatInterval" v-model="scheduling.repeatInterval" class="kn-material-input" :style="knCronDescriptor.style.intervalInput" option-label="name" option-value="value" :options="knCronDescriptor.intervals" @change="updateCronInterval" />
                    </span>
                </div>

                <div v-if="scheduling.repeatInterval === 'week'" class="p-field">
                    <label for="weekdays" class="kn-material-input-label "> {{ $t('cron.inWeekday') }}</label>
                    <MultiSelect id="weekdays" v-model="scheduling.weekdaysSelected" class="kn-material-input p-mx-2" style="max-width:8rem" :options="weekdays" option-label="label" option-value="value" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div v-if="scheduling.repeatInterval === 'month'" class="p-field">
                    <label for="months" class="kn-material-input-label"> {{ $t('cron.inMonth') }} </label>
                    <MultiSelect id="months" v-model="scheduling.monthsSelected" class="kn-material-input p-mx-2" style="max-width:8rem" :options="months" option-label="label" option-value="value" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div v-if="scheduling.repeatInterval === 'day' || scheduling.repeatInterval === 'month'" class="p-field">
                    <label for="days" class="kn-material-input-label  "> {{ $t('cron.inDay') }} </label>
                    <MultiSelect id="days" v-model="scheduling.daysSelected" class="kn-material-input p-mx-2" style="max-width:8rem" :options="days" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div v-if="scheduling.repeatInterval && scheduling.repeatInterval != 'minute'" class="p-field">
                    <label for="hours" class="kn-material-input-label"> {{ $t('cron.inHour') }}</label>
                    <MultiSelect id="hours" v-model="scheduling.hoursSelected" class="kn-material-input p-mx-2" style="max-width:8rem" :options="hours" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div v-if="scheduling.repeatInterval" class="p-field p-ml-2">
                    <label for="minutes" class="kn-material-input-label"> {{ $t('cron.inMinute') }} </label>
                    <MultiSelect id="minutes" v-model="scheduling.minutesSelected" class="kn-material-input p-mx-2" style="max-width:8rem" :options="minutes" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import advancedTabDescriptor from './DatasetManagementAdvancedCardDescriptor.json'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import MultiSelect from 'primevue/multiselect'
import knCronDescriptor from '@/components/UI/KnCron/KnCronDescriptor.json'
import Dropdown from 'primevue/dropdown'
import moment from 'moment'

export default defineComponent({
    components: { Card, Calendar, MultiSelect, Dropdown },
    props: {
        selectedDataset: { type: Object as any },
        schedulingData: { type: Object as any }
    },
    emits: ['touched', 'cronValid'],
    data() {
        return {
            moment,
            knCronDescriptor,
            advancedTabDescriptor,
            dataset: {} as any,
            scheduling: {} as any,
            nextSchedulation: null as any,
            startTemp: null as any,
            endTemp: null as any,
            minutes: Array.from(Array(60).keys()).map(String),
            hours: Array.from(Array(24).keys()).map(String),
            days: Array.from({ length: 31 }, (_, i) => i + 1).map(String),
            months: [
                { value: '1', label: this.$t('cron.january') },
                { value: '2', label: this.$t('cron.february') },
                { value: '3', label: this.$t('cron.march') },
                { value: '4', label: this.$t('cron.april') },
                { value: '5', label: this.$t('cron.may') },
                { value: '6', label: this.$t('cron.june') },
                { value: '7', label: this.$t('cron.july') },
                { value: '8', label: this.$t('cron.august') },
                { value: '9', label: this.$t('cron.september') },
                { value: '10', label: this.$t('cron.october') },
                { value: '11', label: this.$t('cron.november') },
                { value: '12', label: this.$t('cron.december') }
            ],
            weekdays: [
                { value: '1', label: this.$t('cron.monday') },
                { value: '2', label: this.$t('cron.tuesday') },
                { value: '3', label: this.$t('cron.wednesday') },
                { value: '4', label: this.$t('cron.thursday') },
                { value: '5', label: this.$t('cron.friday') },
                { value: '6', label: this.$t('cron.saturday') },
                { value: '7', label: this.$t('cron.sunday') }
            ]
        }
    },
    computed: {
        validDates() {
            let valid = true
            const startDate = this.dataset.startDate
            const now = new Date()
            const endDate = this.dataset.endDate

            if (endDate && endDate.valueOf() < now.valueOf()) {
                valid = false
            }

            if (endDate && endDate.valueOf() < startDate.valueOf()) {
                valid = false
            }

            this.$emit('cronValid', valid)
            return valid
        }
    },
    watch: {
        selectedDataset() {
            this.loadData()
            this.deparseScheduling()
        }
    },
    created() {
        this.loadData()
        this.deparseScheduling()
    },

    methods: {
        loadData() {
            this.dataset = this.selectedDataset
            this.scheduling = this.schedulingData
            this.nextSchedulation = this.selectedDataset.schedulingCronLine
            this.startTemp = this.selectedDataset.startDate ? new Date(this.selectedDataset.startDate) : null
            this.endTemp = this.selectedDataset.endDate ? new Date(this.selectedDataset.endDate) : null
        },
        deparseScheduling() {
            let cronNoSeconds = ''

            if (this.dataset.isScheduled && this.dataset.schedulingCronLine) {
                this.dataset.startDate = new Date(this.dataset.startDate)
                this.dataset.endDate ? (this.dataset.endDate = new Date(this.dataset.endDate)) : (this.dataset.endDate = null)

                const splitCron = this.dataset.schedulingCronLine.split(' ')
                const selectedMinutesCronString = splitCron[1] != '*' ? splitCron[1] : null
                const selectedHoursCronString = splitCron[2] != '*' ? splitCron[2] : null
                const selectedDaysCronString = splitCron[3] != '*' ? splitCron[3] : null
                const selectedMonthsCronString = splitCron[4] != '*' ? splitCron[4] : null
                const selectedWeekdaysCronString = splitCron[5] != '*' && splitCron[5] != '?' ? splitCron[5] : null

                for (let i = 1; i < splitCron.length; i++) {
                    cronNoSeconds += splitCron[i] + ' '
                }
                this.dataset.schedulingCronLine = cronNoSeconds
                this.setSchedulingValues(selectedMinutesCronString, 'minutesSelected', 'minute')
                this.setSchedulingValues(selectedHoursCronString, 'hoursSelected', 'hour')
                this.setSchedulingValues(selectedDaysCronString, 'daysSelected', 'day')
                this.setSchedulingValues(selectedMonthsCronString, 'monthsSelected', 'month')
                this.setSchedulingValues(selectedWeekdaysCronString, 'weekdaysSelected', 'week')
            }
        },
        setSchedulingValues(cronString, valueToSet, customCheck) {
            const splitValue = []

            if (cronString != null) {
                const tempValue = cronString.split(',')

                for (let i = 0; i < tempValue.length; i++) {
                    splitValue.push(tempValue[i])
                }

                this.scheduling[valueToSet] = splitValue
                this.scheduling.repeatInterval = customCheck
            } else {
                this.scheduling[valueToSet] = []
            }
        },
        formatCronForSave() {
            if (this.dataset.isScheduled) {
                if (this.dataset.startDate == null) {
                    this.dataset.startDate = new Date()
                }
                const repeatInterval = this.scheduling.repeatInterval

                const minutesForCron = this.stringifySchedulingValues(this.scheduling.minutesSelected && this.scheduling.minutesSelected.length != 0, 'minutesSelected')
                const hoursForCron = this.stringifySchedulingValues(repeatInterval != 'minute' && this.scheduling.hoursSelected && this.scheduling.hoursSelected.length != 0, 'hoursSelected')
                let daysForCron = this.stringifySchedulingValues((repeatInterval === 'day' || repeatInterval === 'month') && this.scheduling.daysSelected && this.scheduling.daysSelected.length != 0, 'daysSelected')
                const monthsForCron = this.stringifySchedulingValues(repeatInterval === 'month' && this.scheduling.monthsSelected.length != 0, 'monthsSelected')
                let weekdaysForCron = this.stringifySchedulingValues(repeatInterval === 'week' && this.scheduling.weekdaysSelected.length != 0, 'weekdaysSelected')

                if (daysForCron == '*' && weekdaysForCron != '*') {
                    daysForCron = '?'
                } else {
                    weekdaysForCron = '?'
                }

                this.nextSchedulation = minutesForCron + ' ' + hoursForCron + ' ' + daysForCron + ' ' + monthsForCron + ' ' + weekdaysForCron
            }
        },
        stringifySchedulingValues(condition, selectedValue) {
            let stringValue = ''
            if (condition) {
                for (let i = 0; i < this.scheduling[selectedValue].length; i++) {
                    stringValue += '' + this.scheduling[selectedValue][i]

                    if (i < this.scheduling[selectedValue].length - 1) {
                        stringValue += ','
                    }
                }
                return stringValue
            } else {
                stringValue = '*'
                return stringValue
            }
        },
        setDate(event, type) {
            const date = moment(event)
            type === 'startDate' ? (this.dataset.startDate = date.format('YYYY-MM-DD[T]HH:mm:ss[Z]')) : (this.dataset.endDate = date.format('YYYY-MM-DD[T]HH:mm:ss[Z]'))
        }
    }
})
</script>
