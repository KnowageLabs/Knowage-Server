<template>
    <Dialog id="calendar-management-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="calendarManagementDialogDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('kpi.kpiDocumentDesigner.scorecardList') }}
                </template>
            </Toolbar>
        </template>

        {{ calendar }}

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="close"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="buttonDisabled" @click="saveDate"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iCalendarDate } from '../CalendarManagement'
import Dialog from 'primevue/dialog'
import calendarManagementDialogDescriptor from './CalendarManagementDialogDescriptor.json'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'calendar-management-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, propCalendar: { type: Object as PropType<iCalendarDate> } },
    emits: ['close'],
    data() {
        return {
            calendarManagementDialogDescriptor,
            calendar: null as iCalendarDate | null
        }
    },
    computed: {
        buttonDisabled(): boolean {
            return false
        }
    },
    watch: {
        propCalendar() {
            if (this.visible) this.loadCalendar()
        }
    },
    created() {
        this.loadCalendar()
    },
    methods: {
        loadCalendar() {
            this.calendar = deepcopy(this.propCalendar)
            console.log('LOADED CALENDAR: ', this.calendar)
        },
        saveDate() {
            console.log('SAVE CLICKED!')
        },
        close() {
            this.calendar = null
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss">
#calendar-management-dialog .p-dialog-header,
#calendar-management-dialog .p-dialog-content {
    padding: 0;
}
#calendar-management-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
