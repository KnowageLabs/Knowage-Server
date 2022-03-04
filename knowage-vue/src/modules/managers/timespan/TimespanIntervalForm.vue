<template>
    {{ interval }}
    <div v-if="timespan" class="p-fluid p-formgrid  p-grid p-ai-center p-m-2">
        <div class="p-field p-col-5">
            <Calendar class="timespan-interval-calendar kn-flex p-mr-2" v-model="interval.from" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24" @input="onManualDateChange"></Calendar>
        </div>
        <div class="p-field p-col-5">
            <Calendar class="timespan-interval-calendar kn-flex p-mr-2" v-model="interval.to" :manualInput="true" :timeOnly="timespan.type === 'time'" hourFormat="24" @input="onManualDateChange"></Calendar>
        </div>
        <div id="timespan-interval-add-button-container" class="p-field p-col-2">
            <Button id="timespan-interval-add-button" class="kn-button kn-button--primary" :disabled="addButtonDisabled" @click="onAddInterval"> {{ $t('common.add') }}</Button>
        </div>
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
    async created() {},
    methods: {
        loadTimespan() {
            this.timespan = this.propTimespan as iTimespan
            this.initializeInterval()
            console.log('loadTimespan() - LOADED TIMESPAN: ', this.timespan)
        },
        initializeInterval() {
            this.interval = {}
        },
        onManualDateChange() {
            console.log('ON MANUAL DATE CHANGE', this.interval)
        },
        onAddInterval() {
            console.log('onAddInterval() - interval: ')
            const tempInterval = deepcopy(this.interval)
            this.timespan?.type === 'temporal' ? this.addNewTemporalInterval(tempInterval) : this.addNewTimeInterval(tempInterval)
        },
        addNewTimeInterval(interval: iInterval) {
            console.log('addNewTimeInterval() - interval: ', interval)

            if (interval.from instanceof Date && interval.to instanceof Date) {
                const from = this.padTo2Digits(interval.from.getHours()) + ':' + this.padTo2Digits(interval.from.getMinutes())
                const to = this.padTo2Digits(interval.to.getHours()) + ':' + this.padTo2Digits(interval.to.getMinutes())

                console.log(' >>> FROM: ', from)
                console.log(' >>> TO: ', to)

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

                    console.log(' >>> INTERVAL ADDED: ', this.timespan.definition)
                    console.log(' >>> INTERVAL UPDATED: ', this.interval)
                }
            } else {
                this.$store.commit('setError', { title: this.$t('common.toast.errorTitle'), msg: this.$t('managers.timespan.invalidDatesError') })
            }
        },

        addNewTemporalInterval(interval: iInterval) {
            console.log('addNewTemporalInterval() - interval: ', interval)
        },
        padTo2Digits(num) {
            return String(num).padStart(2, '0')
        },
        updateIntervalForm() {
            const tempStart = new Date()
            tempStart.setHours(this.interval.from.split(':'))
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
