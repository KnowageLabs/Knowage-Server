<template>
    <div v-if="timespan" class="p-d-flex kn-flex">
        <Calendar class="timespan-interval-calendar kn-flex " v-model="interval.from" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
        <Calendar class="timespan-interval-calendar kn-flex p-mx-auto" v-model="interval.to" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
        <Button id="timespan-interval-add-button" class="kn-button kn-button--primary p-ml-auto" :disabled="addButtonDisabled" @click="onAddInterval" data-test="add-button"> {{ $t('common.add') }}</Button>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iInterval } from './Timespan'
import { formatDate } from '@/helpers/commons/localeHelper'
import Calendar from 'primevue/calendar'

const deepcopy = require('deepcopy')

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
            if (interval.from instanceof Date && interval.to instanceof Date) {
                const from = this.padTo2Digits(interval.from.getHours()) + ':' + this.padTo2Digits(interval.from.getMinutes())
                const to = this.padTo2Digits(interval.to.getHours()) + ':' + this.padTo2Digits(interval.to.getMinutes())

                const fromTime = Date.parse('01/01/2011 ' + from)
                const toTime = Date.parse('01/01/2011 ' + to)

                if (fromTime > toTime) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startTimeGreaterError') })
                    return
                }

                this.addNewTimeInterval(interval, from, to, fromTime, toTime)
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
            }
        },
        addNewTimeInterval(interval: iInterval, from: string, to: string, fromTime: number, toTime: number) {
            if (this.timespan) {
                for (let i in this.timespan.definition) {
                    const tempStart = Date.parse('01/01/2011 ' + this.timespan.definition[i].from)
                    const tempEnd = Date.parse('01/01/2011 ' + this.timespan.definition[i].to)
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
            if (interval.from instanceof Date && interval.to instanceof Date) {
                const fromDate = interval.from
                const toDate = interval.to

                if (fromDate > toDate) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startDateGreaterError') })
                    return
                }

                this.addNewTemporalInterval(fromDate, toDate)
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
            }
        },
        addNewTemporalInterval(fromDate: Date, toDate: Date) {
            if (this.timespan) {
                for (let i in this.timespan.definition) {
                    const tempStart = new Date(this.timespan.definition[i].from.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3'))
                    const tempEnd = new Date(this.timespan.definition[i].to.replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3'))

                    if (fromDate <= tempEnd && toDate >= tempStart) {
                        this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.temporalOverlapError') })
                        return
                    }
                }
                const from = ('0' + fromDate.getDate()).slice(-2) + '/' + ('0' + (fromDate.getMonth() + 1)).slice(-2) + '/' + fromDate.getFullYear()
                const to = ('0' + toDate.getDate()).slice(-2) + '/' + ('0' + (toDate.getMonth() + 1)).slice(-2) + '/' + toDate.getFullYear()
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
