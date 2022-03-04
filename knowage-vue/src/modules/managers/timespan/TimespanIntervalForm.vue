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
            <Button id="timespan-interval-add-button" class="kn-button kn-button--primary" :disabled="addButtonDisabled" @click="onIntervalAdd"> {{ $t('common.add') }}</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan } from './Timespan'
import Calendar from 'primevue/calendar'

export default defineComponent({
    name: 'timespan-interval-form',
    components: { Calendar },
    props: { propTimespan: { type: Object as PropType<iTimespan | null> } },
    emits: ['addInterval'],
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
        onIntervalAdd() {
            this.$emit('addInterval', this.interval)
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
