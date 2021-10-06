<template>
    {{ trigger }}
    <div v-if="trigger" class="p-d-flex p-flex-row">
        <div class="kn-flex">
            <span>
                <label class="kn-material-input-label">{{ $t('managers.scheduler.timingDescription') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="trigger.triggerDescription" :maxLength="500" />
            </span>
        </div>
        <div class="p-d-flex p-flex-row kn-flex p-jc-around p-ai-center">
            <div v-for="type in schedulerTimingOutputTimingTabDescriptor.types" :key="type.value">
                <RadioButton :id="type.value" name="type" :value="type.value" class="p-mr-2" v-model="triggerType"></RadioButton>
                <label :for="type.value" class="kn-material-input-label">{{ $t(type.label) }}</label>
            </div>
        </div>
    </div>
    <div class="p-mt-4">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.scheduler.timeWindow') }}
            </template>
        </Toolbar>
        <div v-if="triggerType !== 'scheduler'" class="p-d-flex p-flex-row">
            SINGLE
            <div class="p-d-flex p-ai-center p-m-2">
                <div class="p-col-5">
                    <span>
                        <label for="startDate" class="kn-material-input-label">{{ $t('cron.startDate') + ':' }}</label>
                        <Calendar id="startDate" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.calendarInput" v-model="startDate" :showIcon="true" :manualInput="false" />
                    </span>
                </div>
                <div class="p-col-7 p-d-flex p-ai-center">
                    <label for="startTime" class="kn-material-input-label p-m-2"> {{ $t('cron.startTime') + ':' }}</label>
                    <span>
                        <Calendar id="startTime" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.timePicker" v-model="startTime" :showTime="true" :manualInput="false" :timeOnly="true" hourFormat="24" :inline="true" />
                    </span>
                </div>
            </div>
            <div class="p-d-flex p-ai-center p-m-2" v-if="triggerType === 'event'">
                EVENT
                <div class="p-col-5">
                    <span>
                        <label for="endDate" class="kn-material-input-label">{{ $t('cron.endDate') + ':' }}</label>
                        <Calendar id="endDate" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.calendarInput" v-model="endDate" :showIcon="true" :manualInput="false" />
                    </span>
                </div>
                <div class="p-col-7 p-d-flex p-ai-center">
                    <label for="endTime" class="kn-material-input-label p-m-2"> {{ $t('cron.endTime') + ':' }}</label>
                    <span>
                        <Calendar id="endTime" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.timePicker" v-model="endTime" :showTime="true" :manualInput="false" :timeOnly="true" hourFormat="24" :inline="true" />
                    </span>
                </div>
            </div>
        </div>
        <div v-else>
            <KnCron :frequency="frequency" @cronValid="setCronValid($event)"></KnCron>
            <Button @click="test">CHECK CRON FREQ</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import RadioButton from 'primevue/radiobutton'
import schedulerTimingOutputTimingTabDescriptor from './SchedulerTimingOutputTimingTabDescriptor.json'
import KnCron from '@/components/UI/KnCron/KnCron.vue'

export default defineComponent({
    name: 'scheduler-timing-output-timing-tab',
    components: { Calendar, KnCron, RadioButton },
    props: {
        propTrigger: { type: Object }
    },
    data() {
        return {
            schedulerTimingOutputTimingTabDescriptor,
            trigger: null as any,
            triggerType: 'single',
            startDate: null as any,
            startTime: null as any,
            endDate: null as any,
            endTime: null as any,
            frequency: null as any,
            validCron: true
        }
    },
    watch: {
        propTrigger() {
            this.loadTrigger()
        }
    },
    created() {
        this.loadTrigger()
    },
    methods: {
        loadTrigger() {
            this.trigger = this.propTrigger as any
            this.setTriggerType()
            this.triggerType === 'scheduler' ? this.setCronFrequency() : this.setTriggerDates()
            console.log('TRIGGER IN TIMING: ', this.trigger)
        },
        setTriggerType() {
            switch (this.trigger.chrono.type) {
                case 'single':
                    this.triggerType = 'single'
                    break
                case 'event':
                    this.triggerType = 'event'
                    break
                default:
                    this.triggerType = 'scheduler'
            }
        },
        setTriggerDates() {
            this.startDate = this.trigger.zonedStartTime ? new Date(this.trigger.zonedStartTime) : null
            this.startTime = this.startDate ? this.startDate : null

            this.endDate = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null
            this.endTime = this.endDate ? this.endDate : null

            console.log('START DATE: ', this.startDate)
            console.log('START TIME: ', this.startTime)
            console.log('END DATE: ', this.endDate)
            console.log('END TIME: ', this.endTime)
        },
        setCronFrequency() {
            const startDate = this.trigger.zonedStartTime ? new Date(this.trigger.zonedStartTime) : null
            const endDate = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null

            this.frequency = {
                cron: this.trigger.chrono,
                startDate: startDate ? startDate.valueOf() : 0,
                startTime: startDate ? startDate.getHours() + ':' + startDate.getMinutes() : '',
                endDate: endDate ? endDate.valueOf() : 0,
                endTime: endDate ? endDate.getHours() + ':' + endDate.getMinutes() : ''
            }
            console.log('FREQUENCY: ', this.frequency)
        },
        setCronValid(value: boolean) {
            this.validCron = value
        },
        test() {
            console.log('FREQUENCY: ', this.frequency)
        }
    }
})
</script>

<style lang="css">
.custom-timepicker .p-datepicker {
    border-color: transparent;
}
</style>
