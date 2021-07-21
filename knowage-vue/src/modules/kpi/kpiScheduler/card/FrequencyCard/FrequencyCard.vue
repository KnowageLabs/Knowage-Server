<template>
    <Card>
        <template #content>
            <div class="p-d-flex">
                <label for="startDate" class="kn-material-input-label p-m-2"> {{ $t('cron.startDate') + ':' }}</label>
                <span>
                    <Calendar id="startDate" class="kn-material-input" v-model="startDate" :showIcon="true" :manualInput="false" @date-select="setDate('startDate')" />
                </span>
                <label for="startTime" class="kn-material-input-label p-m-2"> {{ $t('cron.startTime') + ':' }}</label>
                <span>
                    <Calendar id="startTime" class="kn-material-input" v-model="startTime" :showIcon="true" :manualInput="false" :timeOnly="true" hourFormat="24" @date-select="setDate('startDate')" />
                </span>
            </div>

            <div class="p-d-flex">
                <label for="endDate" class="kn-material-input-label p-m-2"> {{ $t('cron.endDate') + ':' }}</label>
                <span class="p-float-label">
                    <Calendar id="endDate" class="kn-material-input" v-model="endDate" :showIcon="true" :manualInput="false" @date-select="setDate('endDate')" />
                </span>
                <label for="endTime" class="kn-material-input-label p-m-2"> {{ $t('cron.endTime') + ':' }}</label>
                <span>
                    <Calendar id="endTime" class="kn-material-input" v-model="endTime" :showIcon="true" :manualInput="false" :timeOnly="true" hourFormat="24" @date-select="setDate('endDate')" />
                </span>
            </div>

            <div class="p-d-flex">
                <label for="endDate" class="kn-material-input-label p-m-2"> {{ $t('cron.repeatInterval') + ':' }}</label>
                <span>
                    <Dropdown id="repeatInterval" class="kn-material-input" optionLabel="name" optionValue="value" v-model="repeatInterval" :options="frequencyCardDescriptor.intervals" @change="updateCronInterval" />
                </span>

                <div v-if="repeatInterval === 'minute' || repeatInterval === 'hour' || repeatInterval === 'day'">
                    <label for="parameter" class="kn-material-input-label p-m-2"> {{ $t('kpi.kpiScheduler.every') }}</label>
                    <span>
                        <Dropdown id="parameter" class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameter" :options="parameterOptions" @change="updateCronNumberOfRepetition" />
                    </span>
                </div>
                <div v-else-if="repeatInterval === 'week'" class="p-d-flex p-m-2">
                    <div v-for="(day, index) in frequencyCardDescriptor.weeklyOptions" :key="index">
                        <span class="p-m-1">{{ day.name + ':' }}</span>
                        <Checkbox :value="day.value" v-model="selectedDays" @click="updateCronDays" />
                    </div>
                </div>
                <div v-else-if="repeatInterval === 'month'" class="p-d-flex">
                    <div class="p-m-2">
                        <div>
                            <span class="p-mr-2">{{ $t('cron.advanced') }}</span>
                            <InputSwitch class="p-mr-2" v-model="simpleMonth" />
                            <span>{{ $t('cron.simple') }}</span>
                        </div>
                        <div v-if="simpleMonth">
                            <label for="parameterMonth" class="kn-material-input-label p-m-2"> {{ $t('kpi.kpiScheduler.every') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameter" :options="parameterOptions" @change="updateCronSimpleMonthRepetition" />
                            <label for="parameterMonth" class="kn-material-input-label p-m-2"> {{ $t('cron.months') }}</label>
                        </div>
                        <div v-else>
                            <label class="kn-material-input-label p-m-2"> {{ $t('cron.inMonth') }}</label>
                            <MultiSelect class="kn-material-input" optionLabel="name" optionValue="value" v-model="selectedMonths" :options="parameterOptions" @change="updateCronAdvancedMonthRepetition" />
                        </div>
                    </div>
                    <div class="p-m-2">
                        <span class="p-mr-2">{{ $t('cron.advanced') }}</span>
                        <InputSwitch class="p-mr-2" v-model="simpleDay" />
                        <span>{{ $t('cron.simple') }}</span>
                        <div v-if="simpleDay">
                            <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.theDay') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" optionValue="value" v-model="simpleDayParameter" :options="dayOptions" @change="updateCronSimpleDayRepetition" />
                        </div>
                        <div v-else>
                            <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.theWeek') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameterDay" :options="frequencyCardDescriptor.dayOptions" @change="updateCronAdvancedDayRepetition" />
                            <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.inDay') }}</label>
                            <MultiSelect class="kn-material-input" optionLabel="name" optionValue="value" v-model="selectedDays" :options="dayOptions" @change="updateCronAdvancedDayRepetition" />
                        </div>
                    </div>
                </div>
                <label v-if="repeatInterval === 'minute'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.minutes') }}</label>
                <label v-else-if="repeatInterval === 'hour'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.hours') }}</label>
                <label v-else-if="repeatInterval === 'day'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.days') }}</label>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import frequencyCardDescriptor from './FrequencyCardDescriptor.json'
import MultiSelect from 'primevue/multiselect'

export default defineComponent({
    name: 'frequency-card',
    components: {
        Calendar,
        Card,
        Checkbox,
        Dropdown,
        InputSwitch,
        MultiSelect
    },
    props: {
        frequency: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            frequencyCardDescriptor,
            currentFrequency: {} as any,
            startDate: null as Date | null,
            endDate: null as Date | null,
            startTime: null as Date | null,
            endTime: null as Date | null,
            repeatInterval: null as String | null,
            parameter: null as String | null,
            parameterWeekly: [] as any[],
            parameterOptions: [] as any[],
            simpleMonth: true,
            simpleDay: true,
            parameterMonth: null,
            parameterDay: null as Number | null,
            dayOptions: [] as any[],
            selectedDays: [] as any,
            selectedMonths: [] as any,
            simpleDayParameter: null as Number | null
        }
    },
    watch: {
        repeatInterval() {
            // console.log('REPEAT INTERVAL: ', this.repeatInterval)
            switch (this.repeatInterval) {
                case 'minute':
                    this.fillParameterOptions(60)
                    break

                case 'hour':
                    this.fillParameterOptions(24)
                    break
                case 'day':
                    this.fillParameterOptions(31)
                    break
                case 'month': {
                    this.fillParameterOptions(12)
                    this.fillDayOptions()
                    this.selectedDays = []
                }
            }
            this.parameter = 1 as any
            this.simpleDayParameter = 1
        },
        simpleMonth(value) {
            if (value) {
                this.fillParameterOptions(12)
                delete this.currentFrequency.cron.parameter.months
                this.parameter = 1 as any
                this.updateCronSimpleMonthRepetition()
            } else {
                this.parameterOptions = this.frequencyCardDescriptor.monthOptions
                delete this.currentFrequency.cron.parameter.numRepetition
                this.parameter = null
                this.updateCronAdvancedMonthRepetition()
            }
            // console.log('THIS PARAMETER EEE', this.parameter)
        },
        simpleDay(value) {
            if (value) {
                this.fillDayOptions()
                delete this.currentFrequency.cron.parameter.weeks
                delete this.currentFrequency.cron.parameter.days
                this.updateCronSimpleDayRepetition()
            } else {
                this.dayOptions = this.frequencyCardDescriptor.weeklyOptions
                delete this.currentFrequency.cron.parameter.dayRepetition
                this.updateCronAdvancedDayRepetition()
            }
        }
    },
    async created() {
        this.loadFrequency()
    },
    methods: {
        loadFrequency() {
            // console.log('FREQUENCY: ', this.frequency)

            this.currentFrequency = this.frequency as any
            this.startDate = new Date(this.frequency.startDate)
            this.endDate = new Date(this.frequency.endDate)
            this.startTime = new Date(this.frequency.startDate)
            this.endTime = new Date(this.frequency.endDate)

            this.currentFrequency.startTime = ''
            this.currentFrequency.endTime = ''

            if (!this.currentFrequency.cron) {
                return
            }

            this.repeatInterval = this.currentFrequency.cron.type
            switch (this.repeatInterval) {
                case 'minute':
                case 'hour':
                case 'day':
                    this.parameter = +this.currentFrequency.cron.parameter.numRepetition as any
                    break
                case 'week':
                    this.selectedDays = []
                    this.currentFrequency.cron.parameter.days.forEach((day) => this.selectedDays.push(+day))

                    break
                case 'month': {
                    if (this.currentFrequency.cron.parameter.months) {
                        this.selectedMonths = []
                        this.currentFrequency.cron.parameter.months.forEach((month) => {
                            this.selectedMonths.push(+month)
                        })
                        this.simpleMonth = false
                    }
                    if (this.currentFrequency.cron.parameter.weeks) {
                        this.selectedDays = []
                        this.currentFrequency.cron.parameter.days.forEach((day) => this.selectedDays.push(+day))
                        this.parameterDay = +this.currentFrequency.cron.parameter.weeks
                        this.simpleDay = false
                    }
                }
            }

            // console.log('PARAMETER', this.parameter)
            // console.log('repeatInterval', this.repeatInterval)
        },
        fillParameterOptions(number: number) {
            this.parameterOptions = []
            for (let i = 1; i <= number; i++) {
                this.parameterOptions.push({
                    name: i,
                    value: i
                })
            }
        },
        fillDayOptions() {
            this.dayOptions = []
            for (let i = 1; i <= 31; i++) {
                this.dayOptions.push({
                    name: i,
                    value: i
                })
            }
        },
        updateCronInterval() {
            console.log('REPEAT INTERVAL', this.repeatInterval)
            this.currentFrequency.cron.type = this.repeatInterval
            switch (this.repeatInterval) {
                case 'minute':
                case 'hour':
                case 'day':
                    this.currentFrequency.cron = { type: this.currentFrequency.cron.type, parameter: { numRepetition: this.parameter } }
                    break
                case 'week':
                    this.currentFrequency.cron = { type: this.currentFrequency.cron.type, parameter: { days: this.selectedDays } }
                    break
                case 'month': {
                    this.currentFrequency.cron = { type: this.currentFrequency.cron.type, parameter: { numRepetition: this.parameter, dayRepetition: this.simpleDayParameter } }
                }
            }
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronNumberOfRepetition() {
            console.log('PARAMETER AFTER CHANGE', this.parameter)
            this.currentFrequency.cron = { type: this.currentFrequency.cron.type, parameter: { numRepetition: this.parameter } }
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronDays() {
            console.log('selectedDays AFTER CHANGE', this.selectedDays)
            this.currentFrequency.cron = { type: this.currentFrequency.cron.type, parameter: { days: this.selectedDays } }
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronSimpleMonthRepetition() {
            console.log('PARAMETER AFTER CHANGE', this.parameter)
            this.currentFrequency.cron.parameter.numRepetition = this.parameter
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronSimpleDayRepetition() {
            console.log('PARAMETER AFTER CHANGE', this.simpleDayParameter)
            this.currentFrequency.cron.parameter.dayRepetition = this.simpleDayParameter
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronAdvancedMonthRepetition() {
            console.log('PARAMETER AFTER CHANGE', this.selectedMonths)
            this.currentFrequency.cron.parameter.months = this.selectedMonths
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        updateCronAdvancedDayRepetition() {
            console.log('PARAMETERS AFTER CHANGE', this.parameterDay)
            console.log('PARAMETERS AFTER CHANGE', this.selectedDays)
            this.currentFrequency.cron.parameter.weeks = this.parameterDay
            this.currentFrequency.cron.parameter.days = this.selectedDays
            console.log('CRON AFTER CHANGE', this.currentFrequency.cron)
        },
        setDate(type: string) {
            const date = type === 'startDate' ? this.startDate?.valueOf() : this.endDate?.valueOf()
            const tempTime = type === 'startDate' ? this.startTime : this.endTime
            console.log('DATE', date)
            let time = 0
            if (tempTime) {
                console.log('TIME', tempTime.getHours() * 60 * 60 * 1000, tempTime.getMinutes() * 60 * 1000)
                time = tempTime.getHours() * 60 * 60 * 1000 + tempTime.getMinutes() * 60 * 1000
            }

            if (date) {
                this.currentFrequency[type] = date + time
            }

            console.log('NEW DATE', this.currentFrequency[type])
            console.log('NEW FREQ', this.currentFrequency)
        }
    }
})
</script>
