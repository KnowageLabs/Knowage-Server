<template>
    <div class="p-fluid p-formgrid p-grid p-m-2" v-if="calendar">
        <div class="p-col-12 p-md-6 p-d-flex p-flex-row p-ai-center">
            <div class="kn-flex">
                <span class="p-float-label p-m-2">
                    <InputText
                        class="kn-material-input"
                        v-model.trim="calendar.calendar"
                        :maxlength="calendarManagementDetailFormDescriptor.nameMaxLength"
                        :class="{
                            'p-invalid': calendarNameDirty && calendar.calendar.trim().length === 0
                        }"
                        :disabled="readonly"
                        @input="calendarNameDirty = true"
                        @blur="calendarNameDirty = true"
                        data-test="calendar-name-input"
                    />
                    <label class="kn-material-input-label"> {{ $t('common.name') + ' *' }}</label>
                </span>
                <div class="p-d-flex p-flex-row p-jc-between">
                    <div>
                        <div v-show="calendarNameDirty && calendar.calendar.trim().length === 0" class="p-error p-mx-2">
                            {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                        </div>
                    </div>
                    <p class="calendar-management-input-help p-m-0">{{ nameHelp }}</p>
                </div>
            </div>
            <div class="kn-flex">
                <span class="p-float-label p-m-2">
                    <InputText class="kn-material-input" v-model.trim="calendar.calType" :maxlength="calendarManagementDetailFormDescriptor.typeMaxLength" :disabled="readonly" data-test="calendar-type-input" />
                    <label class="kn-material-input-label"> {{ $t('common.type') }}</label>
                </span>
                <div class="p-d-flex p-flex-row p-jc-end">
                    <p class="calendar-management-input-help p-m-0">{{ typeHelp }}</p>
                </div>
            </div>
        </div>
        <div class="p-col-12 p-md-6 p-flex p-d-flex p-ai-center">
            <div class="kn-flex p-mx-2">
                <span class="p-float-label">
                    <Calendar
                        class="calendar-management-detail-form-calendar-input"
                        v-model="calendar.calStartDay"
                        :class="{
                            'p-invalid': startDateDirty && !calendar.calStartDay
                        }"
                        :manualInput="true"
                        :disabled="readonly"
                        @input="startDateDirty = true"
                        @blur="startDateDirty = true"
                        data-test="calendar-start-date-input"
                    ></Calendar>
                    <label class="kn-material-input-label"> {{ $t('managers.calendarManagement.startValidityDate') + ' *' }}</label>
                </span>
            </div>
            <div class="kn-flex">
                <span class="p-float-label">
                    <Calendar
                        class="calendar-management-detail-form-calendar-input"
                        v-model="calendar.calEndDay"
                        :class="{
                            'p-invalid': endDateDirty && !calendar.calEndDay
                        }"
                        :manualInput="true"
                        :minDate="calendar.calStartDay"
                        :disabled="readonly"
                        @input="endDateDirty = true"
                        @blur="endDateDirty = true"
                        data-test="calendar-end-date-input"
                    ></Calendar>
                    <label class="kn-material-input-label"> {{ $t('managers.calendarManagement.endValidityDate') + ' *' }}</label>
                </span>
            </div>
            <Button v-show="canManageCalendar && generateButtonVisible" id="calendar-management-generate-calendar-button" class="kn-button kn-button--primary p-ml-2" :disabled="generateButtonDisabled" @click="generateCalendar"> {{ $t('common.generate') }}</Button>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iCalendar } from '../../CalendarManagement'
import Calendar from 'primevue/calendar'
import calendarManagementDetailFormDescriptor from './CalendarManagementDetailFormDescriptor.json'

export default defineComponent({
    name: 'calendar-management-detail-form',
    components: { Calendar },
    props: { propCalendar: { type: Object as PropType<iCalendar | null> }, generateButtonVisible: { type: Boolean }, generateButtonDisabled: { type: Boolean } },
    emits: ['generateCalendarClicked'],
    data() {
        return {
            calendarManagementDetailFormDescriptor,
            calendar: null as iCalendar | null,
            calendarNameDirty: false,
            startDateDirty: false,
            endDateDirty: false
        }
    },
    computed: {
        canManageCalendar(): boolean {
            return (this.store.$state as any).user.functionalities.includes('ManageCalendar')
        },
        readonly(): boolean {
            return this.calendar?.calendarId !== undefined
        },
        nameHelp(): string {
            return (this.calendar?.calendar.length ?? '0') + ' / ' + calendarManagementDetailFormDescriptor.nameMaxLength
        },
        typeHelp(): string {
            return (this.calendar?.calType.length ?? '0') + ' / ' + calendarManagementDetailFormDescriptor.typeMaxLength
        }
    },
    watch: {
        propCalendar() {
            this.loadCalendar()
        }
    },
    created() {
        this.loadCalendar()
    },
    methods: {
        loadCalendar() {
            this.calendar = this.propCalendar as iCalendar
        },
        generateCalendar() {
            this.$emit('generateCalendarClicked')
        }
    }
})
</script>

<style lang="scss" scoped>
.calendar-management-input-help {
    font-size: smaller;
}

.calendar-management-detail-form-calendar-input {
    max-width: 300px;
}

#calendar-management-generate-calendar-button {
    max-width: 200px;
    flex: 0.3;
}
</style>
