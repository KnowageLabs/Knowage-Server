<template>
    <div v-if="timespan" class="p-d-flex kn-flex">
        <div class="timespan-interval-calendar p-fluid kn-flex">
            <span class="p-float-label">
                <Calendar v-model="interval.from" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
                <label class="kn-material-input-label"> {{ $t('common.from') }}</label>
            </span>
        </div>
        <div class="timespan-interval-calendar p-fluid  kn-flex p-mx-auto">
            <span class="p-float-label">
                <Calendar v-model="interval.to" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
                <label class="kn-material-input-label"> {{ $t('common.to') }}</label>
            </span>
        </div>
        <Button id="timespan-interval-add-button" class="kn-button kn-button--primary p-ml-auto" :disabled="addButtonDisabled" @click="onAddInterval" data-test="add-button"> {{ $t('common.add') }}</Button>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iInterval } from './Timespan'
import { formatDate } from '@/helpers/commons/localeHelper'
import { createDateFromIntervalTime } from './timespanHelpers'
import Calendar from 'primevue/calendar'

import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'timespan-interval-form',
    components: { Calendar },
    props: { propTimespan: { type: Object as PropType<iTimespan | null> } },
    emits: ['touched'],
    data() {
        return {
            interval: {} as any,
            timespan: null as iTimespan | null
        }
    },
    watch: {
        propTimespan() {
            this.loadTimespan()
        },
        timespanType() {
            this.loadTimespan()
        }
    },
    computed: {
        addButtonDisabled(): boolean {
            return !this.interval.from || !this.interval.to
        },
        timespanType(): any {
            return this.timespan?.type
        }
    },
    created() {
        this.loadTimespan()
    },
    methods: {
        loadTimespan() {
            this.timespan = this.propTimespan as iTimespan
            this.initializeInterval()
        },
        initializeInterval() {
            this.interval = {
                to: new Date(),
                from: new Date()
            }
        },
        onAddInterval() {
            const tempInterval = deepcopy(this.interval)
            this.timespan?.type === 'temporal' ? this.createNewTemporalInterval(tempInterval) : this.createNewTimeInterval(tempInterval)
            this.$emit('touched')
        },
        createNewTimeInterval(interval: iInterval) {
            if (!(interval.from instanceof Date) || !(interval.to instanceof Date)) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
                return
            }

            const from = this.getHoursAndMinutes(interval.from)
            const to = this.getHoursAndMinutes(interval.to)

            const fromTime = this.createDateFromHoursAndMinutes(from)
            const toTime = this.createDateFromHoursAndMinutes(to)

            if (fromTime > toTime) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startTimeGreaterError') })
                return
            }

            this.addNewTimeInterval(interval, from, to, fromTime, toTime)
        },
        addNewTimeInterval(interval: iInterval, from: string, to: string, fromTime: number, toTime: number) {
            if (this.timespan) {
                for (let i in this.timespan.definition) {
                    const tempStart = this.createDateFromHoursAndMinutes(this.timespan.definition[i].from)
                    const tempEnd = this.createDateFromHoursAndMinutes(this.timespan.definition[i].to)
                    if (fromTime <= tempEnd && toTime >= tempStart) {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.timeOverlapError') })
                        return
                    }
                }
                interval.from = from
                interval.to = to
                this.timespan.definition.push(interval)
                this.refreshTimeInterval(fromTime, toTime)
            }
        },
        refreshTimeInterval(fromTime: number, toTime: number) {
            const millsHour = 60 * 1000
            this.interval.from = new Date(toTime + millsHour)
            const diffTime = toTime - fromTime
            this.interval.to = new Date(toTime + millsHour + diffTime)
            this.interval = deepcopy(this.interval)
        },
        createNewTemporalInterval(interval: iInterval) {
            if (!(interval.from instanceof Date) || !(interval.to instanceof Date)) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
                return
            }

            const fromDate = interval.from
            const toDate = interval.to

            if (fromDate > toDate) {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startDateGreaterError') })
                return
            }

            this.addNewTemporalInterval(fromDate, toDate)
        },
        addNewTemporalInterval(fromDate: Date, toDate: Date) {
            if (this.timespan) {
                for (let i in this.timespan.definition) {
                    const tempStart = createDateFromIntervalTime(this.timespan.definition[i].from)
                    const tempEnd = createDateFromIntervalTime(this.timespan.definition[i].to)

                    if (fromDate <= tempEnd && toDate >= tempStart) {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.temporalOverlapError') })
                        return
                    }
                }
                const from = this.getFormattedDateString(fromDate)
                const to = this.getFormattedDateString(toDate)
                const fromLocalized = this.getFormattedDate(from)
                const toLocalized = this.getFormattedDate(to)
                this.timespan.definition.push({ from: from, to: to, fromLocalized: fromLocalized, toLocalized: toLocalized })
                this.refreshTimespanInterval(fromDate, toDate)
            }
        },
        refreshTimespanInterval(fromDate: Date, toDate: Date) {
            const millsDay = 86400000
            this.interval.from = toDate
            this.interval.from.setTime(toDate.getTime() + millsDay)
            this.interval.to = new Date()
            this.interval.to.setTime(this.interval.from.getTime() + toDate.getTime() - fromDate.getTime() - millsDay)
            this.interval = deepcopy(this.interval)
        },
        padTo2Digits(num) {
            return String(num).padStart(2, '0')
        },
        getFormattedDate(date: string) {
            return formatDate(date, '', 'DD/MM/yyyy')
        },
        getHoursAndMinutes(date: Date) {
            return this.padTo2Digits(date.getHours()) + ':' + this.padTo2Digits(date.getMinutes())
        },
        createDateFromHoursAndMinutes(date: string) {
            return Date.parse('01/01/2011 ' + date)
        },
        getFormattedDateString(date: Date) {
            return ('0' + date.getDate()).slice(-2) + '/' + ('0' + (date.getMonth() + 1)).slice(-2) + '/' + date.getFullYear()
        }
    }
})
</script>

<style lang="scss" scoped>
#timespan-interval-add-button {
    max-width: 100px;
    text-align: center;
}

#timespan-interval-add-button-container {
    text-align: end;
}

.timespan-interval-calendar {
    max-width: 350px;
}
</style>
