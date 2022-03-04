<template>
    <div v-if="timespan" class="p-d-flex kn-flex">
        <Calendar class="timespan-interval-calendar kn-flex " v-model="interval.from" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
        <Calendar class="timespan-interval-calendar kn-flex p-mx-auto" v-model="interval.to" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24"></Calendar>
        <Button id="timespan-interval-add-button" class="kn-button kn-button--primary p-ml-auto" :disabled="addButtonDisabled" @click="onAddInterval"> {{ $t('common.add') }}</Button>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iInterval } from './Timespan'
import Calendar from 'primevue/calendar'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'timespan-interval-form',
    components: { Calendar },
    props: { propTimespan: { type: Object as PropType<iTimespan | null> } },
    data() {
        return {
            interval: {} as any,
            timespan: null as iTimespan | null
        }
    },
    watch: {
        propTimespan() {
            this.loadTimespan()
        }
    },
    computed: {
        addButtonDisabled(): boolean {
            return !this.interval.from || !this.interval.to
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
            this.timespan?.type === 'temporal' ? this.addNewTemporalInterval(tempInterval) : this.addNewTimeInterval(tempInterval)
        },
        addNewTimeInterval(interval: iInterval) {
            if (interval.from instanceof Date && interval.to instanceof Date) {
                const from = this.padTo2Digits(interval.from.getHours()) + ':' + this.padTo2Digits(interval.from.getMinutes())
                const to = this.padTo2Digits(interval.to.getHours()) + ':' + this.padTo2Digits(interval.to.getMinutes())

                const fromTime = Date.parse('01/01/2011 ' + from)
                const toTime = Date.parse('01/01/2011 ' + to)

                if (fromTime > toTime) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startTimeGreaterError') })
                    return
                }

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
                    const millsHour = 60 * 1000
                    this.interval.from = new Date(toTime + millsHour)
                    const diffTime = toTime - fromTime
                    this.interval.to = new Date(toTime + millsHour + diffTime)
                    this.interval = deepcopy(this.interval)
                }
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
            }
        },

        addNewTemporalInterval(interval: iInterval) {
            if (interval.from instanceof Date && interval.to instanceof Date) {
                const fromDate = interval.from
                const toDate = interval.to

                if (fromDate > toDate) {
                    this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.startDateGreaterError') })
                    return
                }

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
                    const fromLocalized = this.formatDate(fromDate, this.parseDateTemp('d/m/Y'))
                    const toLocalized = this.formatDate(toDate, this.parseDateTemp('d/m/Y'))
                    this.timespan.definition.push({ from: from, to: to, fromLocalized: fromLocalized, toLocalized: toLocalized })

                    const millsDay = 86400000
                    this.interval.from = toDate
                    this.interval.from.setTime(toDate.getTime() + millsDay)
                    this.interval.to = new Date()
                    this.interval.to.setTime(this.interval.from.getTime() + toDate.getTime() - fromDate.getTime() - millsDay)
                    this.interval = deepcopy(this.interval)
                }
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
            }
        },
        padTo2Digits(num) {
            return String(num).padStart(2, '0')
        },
        parseDateTemp(date) {
            let result = ''
            if (date === 'd/m/Y') {
                result = 'dd/MM/yyyy'
            }
            if (date === 'm/d/Y') {
                result = 'MM/dd/yyyy'
            }
            return result
        },
        formatDate(date, format) {
            const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
            const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

            format = format + ''
            let result = ''
            let i_format = 0
            let c = ''
            let token = ''
            let y = (date.getYear() + '') as any
            const M = date.getMonth() + 1
            const d = date.getDate()
            const E = date.getDay()
            const H = date.getHours()
            const m = date.getMinutes()
            const s = date.getSeconds()

            const value = new Object()
            if (y.length < 4) {
                y = '' + (y - 0 + 1900)
            }
            value['y'] = '' + y
            value['yyyy'] = y
            value['yy'] = y.substring(2, 4)
            value['M'] = M
            value['MM'] = this.LZ(M)
            value['MMM'] = MONTH_NAMES[M - 1]
            value['NNN'] = MONTH_NAMES[M + 11]
            value['d'] = d
            value['dd'] = this.LZ(d)
            value['E'] = DAY_NAMES[E + 7]
            value['EE'] = DAY_NAMES[E]
            value['H'] = H
            value['HH'] = this.LZ(H)
            if (H == 0) {
                value['h'] = 12
            } else if (H > 12) {
                value['h'] = H - 12
            } else {
                value['h'] = H
            }
            value['hh'] = this.LZ(value['h'])
            if (H > 11) {
                value['K'] = H - 12
            } else {
                value['K'] = H
            }
            value['k'] = H + 1
            value['KK'] = this.LZ(value['K'])
            value['kk'] = this.LZ(value['k'])
            if (H > 11) {
                value['a'] = 'PM'
            } else {
                value['a'] = 'AM'
            }
            value['m'] = m
            value['mm'] = this.LZ(m)
            value['s'] = s
            value['ss'] = this.LZ(s)
            while (i_format < format.length) {
                c = format.charAt(i_format)
                token = ''
                while (format.charAt(i_format) == c && i_format < format.length) {
                    token += format.charAt(i_format++)
                }
                if (value[token] != null) {
                    result = result + value[token]
                } else {
                    result = result + token
                }
            }
            return result
        },
        LZ(x) {
            return (x < 0 || x > 9 ? '' : '0') + x
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
