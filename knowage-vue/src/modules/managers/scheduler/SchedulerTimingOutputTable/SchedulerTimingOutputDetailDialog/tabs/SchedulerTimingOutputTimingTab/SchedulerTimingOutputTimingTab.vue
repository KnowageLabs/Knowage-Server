<template>
    <div v-if="trigger" class="p-d-flex p-flex-row">
        <div class="kn-flex p-m-4">
            <span>
                <label class="kn-material-input-label">{{ $t('managers.scheduler.timingDescription') }} *</label>
                <InputText
                    class="kn-material-input p-inputtext-sm"
                    v-model="trigger.triggerDescription"
                    :class="{
                        'p-invalid': descriptionDirty && (!trigger.triggerDescription || trigger.triggerDescription.length === 0)
                    }"
                    :maxLength="500"
                    @input="setTriggerName"
                    @blur="descriptionDirty = true"
                />
                <div>
                    <div v-show="descriptionDirty && (!trigger.triggerDescription || trigger.triggerDescription.length === 0)" class="p-error p-grid p-m-2">
                        {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.timingDescription') }) }}
                    </div>
                </div>
            </span>
        </div>

        <div class="p-d-flex p-flex-row kn-flex p-jc-around p-ai-center">
            <div v-for="type in schedulerTimingOutputTimingTabDescriptor.types" :key="type.value">
                <RadioButton :id="type.value" name="type" :value="type.value" class="p-mr-2" v-model="triggerType" @change="formatFrequency" :data-test="'trigger-type-button-' + type.value"></RadioButton>
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

        <div v-if="triggerType !== 'scheduler'" class="p-d-flex p-flex-row p-m-4">
            <div class="p-d-flex p-ai-center p-m-2" data-test="single-execution-trigger">
                <div class="p-col-5">
                    <span>
                        <label for="startDateTiming" class="kn-material-input-label">{{ $t('cron.startDate') + ':' }}</label>
                        <Calendar id="startDateTiming" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.calendarInput" v-model="trigger.startDateTiming" :showIcon="true" :manualInput="false" @date-select="setStartDate" />
                    </span>
                    <div v-if="!validDates" class="p-error p-grid">
                        <small class="p-col-12">
                            {{ $t('managers.scheduler.dateError') }}
                        </small>
                    </div>
                </div>

                <div class="p-col-7 p-d-flex p-ai-center">
                    <label for="startTime" class="kn-material-input-label p-m-2"> {{ $t('cron.startTime') + ':' }}</label>
                    <span>
                        <Calendar
                            id="startTime"
                            class="kn-material-input custom-timepicker"
                            :style="schedulerTimingOutputTimingTabDescriptor.style.timePicker"
                            v-model="trigger.startTimeTiming"
                            :showTime="true"
                            :manualInput="false"
                            :timeOnly="true"
                            hourFormat="24"
                            :inline="true"
                            @date-select="setStartDate"
                        />
                    </span>
                </div>
            </div>

            <div v-if="triggerType === 'event'" class="p-d-flex p-ai-center p-m-2" data-test="event-trigger-endDate">
                <div class="p-col-5">
                    <span>
                        <label for="endDateTiming" class="kn-material-input-label">{{ $t('cron.endDate') + ':' }}</label>
                        <Calendar id="endDateTiming" class="kn-material-input custom-timepicker" :style="schedulerTimingOutputTimingTabDescriptor.style.calendarInput" v-model="trigger.endDateTiming" :showIcon="true" :manualInput="false" @date-select="setEndDate" />
                    </span>
                    <div v-if="!validDates" class="p-error p-grid">
                        <small class="p-col-12">
                            {{ $t('managers.scheduler.dateError') }}
                        </small>
                    </div>
                </div>

                <div class="p-col-7 p-d-flex p-ai-center">
                    <label for="endTime" class="kn-material-input-label p-m-2"> {{ $t('cron.endTime') + ':' }}</label>
                    <span>
                        <Calendar
                            id="endTime"
                            class="kn-material-input custom-timepicker"
                            :style="schedulerTimingOutputTimingTabDescriptor.style.timePicker"
                            v-model="trigger.endTimeTiming"
                            :showTime="true"
                            :manualInput="false"
                            :timeOnly="true"
                            hourFormat="24"
                            :inline="true"
                            @date-select="setEndDate"
                        />
                    </span>
                </div>
            </div>
        </div>

        <div v-if="triggerType === 'event'" class="p-my-4" data-test="event-trigger">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('common.details') }}
                </template>
            </Toolbar>

            <div class="p-m-4">
                <span>
                    <label class="kn-material-input-label">{{ $t('managers.scheduler.eventType') }}</label>
                    <Dropdown class="kn-material-input" v-model="trigger.chrono.parameter.type" :options="eventTypes" optionLabel="label" optionValue="value" />
                </span>
            </div>

            <div v-if="trigger.chrono.parameter && trigger.chrono.parameter.type === 'dataset'">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('common.dataset') }}
                    </template>
                </Toolbar>

                <div class="p-d-flex p-flex-row">
                    <div class="kn-flex p-m-4">
                        <span>
                            <label class="kn-material-input-label">{{ $t('managers.scheduler.datasetVerification') }}</label>
                            <Dropdown class="kn-material-input" v-model="trigger.chrono.parameter.dataset" :options="datasets" optionLabel="label" optionValue="id.dsId" />
                        </span>
                    </div>
                    <div class="kn-flex p-m-4">
                        <span>
                            <label class="kn-material-input-label">{{ $t('managers.scheduler.frequencyMinutes') }}</label>
                            <InputText class="kn-material-input p-inputtext-sm" type="number" v-model="trigger.chrono.parameter.frequency" />
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <div v-if="triggerType === 'scheduler'" data-test="scheduler-trigger">
            <KnCron v-if="trigger && trigger.frequency" :frequency="trigger.frequency" @cronValid="setCronValid($event)"></KnCron>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import RadioButton from 'primevue/radiobutton'
import schedulerTimingOutputTimingTabDescriptor from './SchedulerTimingOutputTimingTabDescriptor.json'
import KnCron from '@/components/UI/KnCron/KnCron.vue'

export default defineComponent({
    name: 'scheduler-timing-output-timing-tab',
    components: { Calendar, Dropdown, KnCron, RadioButton },
    props: {
        propTrigger: { type: Object },
        datasets: { type: Array }
    },
    emits: ['cronValid'],
    data() {
        return {
            schedulerTimingOutputTimingTabDescriptor,
            trigger: null as any,
            triggerType: 'single',
            eventTypes: [
                { value: 'rest', label: this.$t('managers.scheduler.restService') },
                { value: 'contextbroker', label: this.$t('managers.scheduler.contextBroker') },
                { value: 'dataset', label: this.$t('common.dataset') }
            ],
            selectedDataset: null,
            validCron: true,
            descriptionDirty: false
        }
    },
    watch: {
        propTrigger() {
            this.loadTrigger()
        }
    },
    computed: {
        validDates() {
            let valid = true
            const now = new Date()

            if (this.trigger.endDateTiming && this.trigger.endDateTiming.valueOf() < now.valueOf()) {
                valid = false
            }

            if (this.trigger.endDateTiming && this.trigger.endDateTiming.valueOf() < this.trigger.startTimeTiming.valueOf()) {
                valid = false
            }

            this.$emit('cronValid', valid)
            return valid
        }
    },
    created() {
        this.loadTrigger()
    },
    methods: {
        loadTrigger() {
            this.trigger = this.propTrigger as any
            this.trigger.triggerDescription = this.trigger.triggerName
            this.setTriggerType()
            this.setTriggerDates()
            if (this.triggerType === 'scheduler') this.setCronFrequency()
            if (this.trigger.chrono.parameter?.dataset) this.trigger.chrono.parameter.dataset = +this.trigger.chrono.parameter.dataset
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
            this.trigger.startDateTiming = this.trigger.zonedStartTime ? new Date(this.trigger.zonedStartTime) : new Date()
            this.trigger.startTimeTiming = this.trigger.zonedStartTime ? new Date(this.trigger.zonedStartTime) : new Date()
            this.trigger.startDateTiming.setHours(0)
            this.trigger.startDateTiming.setMinutes(0)

            this.trigger.endDateTiming = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null
            this.trigger.endTimeTiming = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null
            if (this.trigger.endTimeTiming) {
                this.trigger.endDateTiming.setHours(0)
                this.trigger.endDateTiming.setMinutes(0)
            }
        },
        setCronFrequency() {
            const startDate = this.trigger.zonedStartTime ? new Date(this.trigger.zonedStartTime) : new Date()
            const endDate = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null

            this.trigger.frequency = {
                cron: this.trigger.chrono,
                startDate: startDate.valueOf(),
                startTime: startDate.getHours() + ':' + startDate.getMinutes(),
                endDate: endDate ? endDate.valueOf() : null,
                endTime: endDate ? endDate.getHours() + ':' + endDate.getMinutes() : ''
            }
        },
        formatFrequency() {
            this.trigger.chrono = { type: this.triggerType, parameter: { type: '' } }

            switch (this.trigger.chrono.type) {
                case 'single':
                    delete this.trigger.endDateTiming
                    delete this.trigger.endTimeTiming
                    break
                case 'scheduler':
                    this.setCronFrequency()
            }
        },
        setCronValid(value: boolean) {
            this.validCron = value
        },
        setTriggerName() {
            this.descriptionDirty = true
            this.trigger.triggerName = this.trigger.triggerDescription.substring(0, 100)
        },
        setStartDate() {
            this.trigger.zonedStartTime = this.trigger.startDateTiming
            this.trigger.zonedStartTime.setHours(this.trigger.startTimeTiming?.getHours())
            this.trigger.zonedStartTime.setMinutes(this.trigger.startTimeTiming?.getMinutes())
        },
        setEndDate() {
            this.trigger.zonedEndTime = this.trigger.endDateTiming
            this.trigger.zonedEndTime.setHours(this.trigger.endTimeTiming.getHours())
            this.trigger.zonedEndTime.setMinutes(this.trigger.endTimeTiming.getMinutes())
        }
    }
})
</script>

<style lang="css">
.custom-timepicker .p-datepicker {
    border-color: transparent;
}
</style>
