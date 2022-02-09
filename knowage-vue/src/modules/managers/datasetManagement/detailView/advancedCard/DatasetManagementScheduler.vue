<template>
    <Card>
        <template #content>
            <div class="date-picker-container p-ml-2 p-mb-5">
                <div class="p-field p-grid">
                    <label for="startDate" class="kn-material-input-label p-col-12  p-md-1 p-mb-md-0"> {{ $t('cron.startDate') }}: </label>
                    <span>
                        <Calendar
                            id="startDate"
                            class="kn-material-input"
                            v-model="startTemp"
                            style="width:20rem"
                            :class="{
                                'p-invalid': !validDates
                            }"
                            :showIcon="true"
                            :manualInput="true"
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
                            class="kn-material-input"
                            v-model="endTemp"
                            style="width:20rem"
                            :class="{
                                'p-invalid': !validDates
                            }"
                            :showIcon="true"
                            :manualInput="true"
                            :showButtonBar="true"
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
                        <Dropdown id="repeatInterval" class="kn-material-input" :style="knCronDescriptor.style.intervalInput" v-model="scheduling.repeatInterval" optionLabel="name" optionValue="value" :options="knCronDescriptor.intervals" @change="updateCronInterval" />
                    </span>
                </div>

                <div class="p-field" v-if="scheduling.repeatInterval === 'week'">
                    <label for="weekdays" class="kn-material-input-label "> {{ $t('cron.inWeekday') }}</label>
                    <MultiSelect id="weekdays" class="kn-material-input p-mx-2" style="max-width:8rem" v-model="scheduling.weekdaysSelected" :options="weekdays" optionLabel="label" optionValue="value" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div class="p-field" v-if="scheduling.repeatInterval === 'month'">
                    <label for="months" class="kn-material-input-label"> {{ $t('cron.inMonth') }} </label>
                    <MultiSelect id="months" class="kn-material-input p-mx-2" style="max-width:8rem" v-model="scheduling.monthsSelected" :options="months" optionLabel="label" optionValue="value" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div class="p-field" v-if="scheduling.repeatInterval === 'day' || scheduling.repeatInterval === 'month'">
                    <label for="days" class="kn-material-input-label  "> {{ $t('cron.inDay') }} </label>
                    <MultiSelect id="days" class="kn-material-input p-mx-2" style="max-width:8rem" v-model="scheduling.daysSelected" :options="days" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div class="p-field" v-if="scheduling.repeatInterval && scheduling.repeatInterval != 'minute'">
                    <label for="hours" class="kn-material-input-label"> {{ $t('cron.inHour') }}</label>
                    <MultiSelect id="hours" class="kn-material-input p-mx-2" style="max-width:8rem" v-model="scheduling.hoursSelected" :options="hours" :placeholder="$t('common.default')" @change="formatCronForSave" />
                </div>
                <div class="p-field p-ml-2" v-if="scheduling.repeatInterval">
                    <label for="minutes" class="kn-material-input-label"> {{ $t('cron.inMinute') }} </label>
                    <MultiSelect id="minutes" class="kn-material-input p-mx-2" style="max-width:8rem" v-model="scheduling.minutesSelected" :options="minutes" :placeholder="$t('common.default')" @change="formatCronForSave" />
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
    created() {
        this.loadData()
        this.deparseScheduling()
    },
    watch: {
        selectedDataset() {
            this.loadData()
            this.deparseScheduling()
        }
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
            var cronNoSeconds = ''

            if (this.dataset.isScheduled && this.dataset.schedulingCronLine) {
                this.dataset.startDate = new Date(this.dataset.startDate)
                this.dataset.endDate ? (this.dataset.endDate = new Date(this.dataset.endDate)) : (this.dataset.endDate = null)

                var splitCron = this.dataset.schedulingCronLine.split(' ')
                var selectedMinutesCronString = splitCron[1] != '*' ? splitCron[1] : null
                var selectedHoursCronString = splitCron[2] != '*' ? splitCron[2] : null
                var selectedDaysCronString = splitCron[3] != '*' ? splitCron[3] : null
                var selectedMonthsCronString = splitCron[4] != '*' ? splitCron[4] : null
                var selectedWeekdaysCronString = splitCron[5] != '*' && splitCron[5] != '?' ? splitCron[5] : null

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
            var splitValue = new Array()

            if (cronString != null) {
                var tempValue = cronString.split(',')

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
                var repeatInterval = this.scheduling.repeatInterval

                var minutesForCron = this.stringifySchedulingValues(this.scheduling.minutesSelected && this.scheduling.minutesSelected.length != 0, 'minutesSelected')
                var hoursForCron = this.stringifySchedulingValues(repeatInterval != 'minute' && this.scheduling.hoursSelected && this.scheduling.hoursSelected.length != 0, 'hoursSelected')
                var daysForCron = this.stringifySchedulingValues((repeatInterval === 'day' || repeatInterval === 'month') && this.scheduling.daysSelected.length != 0, 'daysSelected')
                var monthsForCron = this.stringifySchedulingValues(repeatInterval === 'month' && this.scheduling.monthsSelected.length != 0, 'monthsSelected')
                var weekdaysForCron = this.stringifySchedulingValues(repeatInterval === 'week' && this.scheduling.weekdaysSelected.length != 0, 'weekdaysSelected')

                if (daysForCron == '*' && weekdaysForCron != '*') {
                    daysForCron = '?'
                } else {
                    weekdaysForCron = '?'
                }

                this.nextSchedulation = minutesForCron + ' ' + hoursForCron + ' ' + daysForCron + ' ' + monthsForCron + ' ' + weekdaysForCron
            }
        },
        stringifySchedulingValues(condition, selectedValue) {
            var stringValue = ''
            if (condition) {
                for (var i = 0; i < this.scheduling[selectedValue].length; i++) {
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
            var date = moment(event)
            type === 'startDate' ? (this.dataset.startDate = date.format('YYYY-MM-DD[T]HH:mm:ss[Z]')) : (this.dataset.endDate = date.format('YYYY-MM-DD[T]HH:mm:ss[Z]'))
        }
    }
})
</script>
