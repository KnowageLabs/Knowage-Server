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
        <div v-if="triggerType === 'single'" id="startDateContainer">
            SINGLE
            <span>
                <Calendar id="startDate" v-model="startDate" :showTime="true" />
                <label for="startDate" class="kn-material-input-label"></label>
            </span>
        </div>
        <div v-else-if="triggerType === 'event'">
            EVENT
        </div>
        <div v-else>
            SCHEDULER
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Calendar from 'primevue/calendar'
import RadioButton from 'primevue/radiobutton'
import schedulerTimingOutputTimingTabDescriptor from './SchedulerTimingOutputTimingTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-timing-output-timing-tab',
    components: { Calendar, RadioButton },
    props: {
        propTrigger: { type: Object }
    },
    data() {
        return {
            schedulerTimingOutputTimingTabDescriptor,
            trigger: null as any,
            triggerType: 'single',
            startDate: null as any,
            endDate: null as any
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
            this.setTriggerDates()
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
            this.endDate = this.trigger.zonedEndTime ? new Date(this.trigger.zonedEndTime) : null
        }
    }
})
</script>

<style lang="scss" scoped>
#startDateContainer {
    margin: 2rem;
    width: 20%;
}
</style>
