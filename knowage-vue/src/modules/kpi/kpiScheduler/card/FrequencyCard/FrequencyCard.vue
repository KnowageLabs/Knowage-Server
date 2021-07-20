<template>
    <Card>
        <template #content>
            <div class="p-d-flex">
                <label for="startDate" class="kn-material-input-label p-m-2"> {{ $t('cron.startDate') + ':' }}</label>
                <span>
                    <Calendar id="startDate" class="kn-material-input" v-model="startDate" :showIcon="true" :manualInput="false" />
                </span>
                <label for="startTime" class="kn-material-input-label p-m-2"> {{ $t('cron.startTime') + ':' }}</label>
                <span>
                    <Calendar id="startTime" class="kn-material-input" v-model="startTime" :showIcon="true" :manualInput="false" :timeOnly="true" hourFormat="24" />
                </span>
            </div>

            <div class="p-d-flex">
                <label for="endDate" class="kn-material-input-label p-m-2"> {{ $t('cron.endDate') + ':' }}</label>
                <span class="p-float-label">
                    <Calendar id="endDate" class="kn-material-input" v-model="startDate" :showIcon="true" :manualInput="false" />
                </span>
                <label for="endTime" class="kn-material-input-label p-m-2"> {{ $t('cron.endTime') + ':' }}</label>
                <span>
                    <Calendar id="endTime" class="kn-material-input" v-model="endTime" :showIcon="true" :manualInput="false" :timeOnly="true" hourFormat="24" />
                </span>
            </div>

            <div class="p-d-flex">
                <label for="endDate" class="kn-material-input-label p-m-2"> {{ $t('cron.repeatInterval') + ':' }}</label>
                <span>
                    <Dropdown id="repeatInterval" class="kn-material-input" optionLabel="name" optionValue="value" v-model="repeatInterval" :options="frequencyCardDescriptor.intervals" @change="setInputInterval" />
                </span>

                <div v-if="repeatInterval === 'minute' || repeatInterval === 'hour' || repeatInterval === 'daily'">
                    <label for="parameter" class="kn-material-input-label p-m-2"> {{ $t('kpi.kpiScheduler.every') }}</label>
                    <span>
                        <Dropdown id="parameter" class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameter" :options="parameterOptions" />
                    </span>
                </div>
                <div v-else-if="repeatInterval === 'weekly'" class="p-d-flex p-m-2">
                    <div v-for="(day, index) in frequencyCardDescriptor.weeklyOptions" :key="index">
                        <span class="p-m-1">{{ day.name + ':' }}</span>
                        <Checkbox :value="day.value" v-model="parameter" />
                    </div>
                </div>
                <div v-else-if="repeatInterval === 'monthly'" class="p-d-flex">
                    <div class="p-m-2">
                        <div>
                            <span class="p-mr-2">{{ $t('cron.advanced') }}</span>
                            <InputSwitch class="p-mr-2" v-model="simpleMonth" />
                            <span>{{ $t('cron.simple') }}</span>
                        </div>
                        <div v-if="simpleMonth">
                            <label for="parameterMonth" class="kn-material-input-label p-m-2"> {{ $t('kpi.kpiScheduler.every') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" v-model="parameter" :options="parameterOptions" />
                            <label for="parameterMonth" class="kn-material-input-label p-m-2"> {{ $t('cron.months') }}</label>
                        </div>
                        <div v-else>
                            <label class="kn-material-input-label p-m-2"> {{ $t('cron.inMonth') }}</label>
                            <MultiSelect class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameter" :options="parameterOptions" />
                        </div>
                    </div>
                    <div class="p-m-2">
                        <span class="p-mr-2">{{ $t('cron.advanced') }}</span>
                        <InputSwitch class="p-mr-2" v-model="simpleDay" />
                        <span>{{ $t('cron.simple') }}</span>
                        <div v-if="simpleDay">
                            <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.theDay') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" v-model="parameter" :options="dayOptions" />
                        </div>
                        <div v-else>
                            <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.theWeek') }}</label>
                            <Dropdown class="kn-material-input" optionLabel="name" v-model="parameterDay" :options="frequencyCardDescriptor.dayOptions" /> <label for="parameterDay" class="kn-material-input-label p-m-2"> {{ $t('cron.inDay') }}</label>
                            <MultiSelect class="kn-material-input" optionLabel="name" optionValue="value" v-model="parameter" :options="dayOptions" />
                        </div>
                    </div>
                </div>
                <label v-if="repeatInterval === 'minute'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.minutes') }}</label>
                <label v-else-if="repeatInterval === 'hour'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.hours') }}</label>
                <label v-else-if="repeatInterval === 'daily'" for="parameter" class="kn-material-input-label p-m-2"> {{ $t('cron.days') }}</label>
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
            cron: {} as any,
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
            parameterDay: null,
            dayOptions: [] as any[]
        }
    },
    watch: {
        repeatInterval() {
            console.log('REPEAT INTERVAL: ', this.repeatInterval)
            switch (this.repeatInterval) {
                case 'minute':
                    this.fillParameterOptions(60)
                    break

                case 'hour':
                    this.fillParameterOptions(24)
                    break
                case 'daily':
                    this.fillParameterOptions(31)
                    break
                case 'monthly': {
                    this.fillParameterOptions(12)
                    this.fillDayOptions()
                }
            }
        },
        simpleMonth(value) {
            if (value) {
                this.fillParameterOptions(12)
            } else {
                this.parameterOptions = this.frequencyCardDescriptor.monthOptions
            }
            this.parameter = null
        },
        simpleDay(value) {
            if (value) {
                this.fillDayOptions()
            } else {
                this.dayOptions = this.frequencyCardDescriptor.weeklyOptions
            }
            this.parameter = null
        }
    },
    async created() {
        this.loadFrequency()
    },
    methods: {
        loadFrequency() {
            console.log('FREQUENCY: ', this.frequency)
            // console.log('FREQUENCY CRON PARSED', JSON.parse(this.frequency.cron))
            this.cron = this.frequency as any
            const cron = JSON.parse(this.frequency.cron)
            this.startDate = new Date(this.frequency.startDate)
            this.endDate = new Date(this.frequency.endDate)
            this.startTime = new Date(this.frequency.startDate)
            this.endTime = new Date(this.frequency.endDate)
            if (cron) {
                this.repeatInterval = cron.type
            }
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
        setInputInterval() {
            console.log('REPEAT INTERVAL', this.repeatInterval)
            const temp = JSON.parse(this.frequency.cron)
            console.log('temp', temp)
            temp.type = this.repeatInterval
        }
    }
})
</script>
