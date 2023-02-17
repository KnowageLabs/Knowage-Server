<template>
    <DataTable v-if="calendarSplitData" :value="calendarSplitData" :paginator="calendarSplitData.length > 20" :rows="20" class="p-datatable-sm" data-key="idCalComposition" responsive-layout="stack" breakpoint="960px">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>

        <Column class="kn-truncated" field="timeByDay.timeDate" :header="$t('cron.date')" :style="calendarManagementDetailTableDescriptor.columnStyle.date" :sortable="true">
            <template #body="slotProps">
                <span>{{ getFormattedDate(slotProps.data.timeByDay.timeDate) }}</span>
            </template>
        </Column>
        <Column class="kn-truncated" field="day" :header="$t('cron.day')" :style="calendarManagementDetailTableDescriptor.columnStyle.day" :sortable="true"> </Column>
        <Column class="kn-truncated" field="isHoliday" :header="$t('managers.calendarManagement.holiday')" :style="calendarManagementDetailTableDescriptor.columnStyle.checkbox" :sortable="true">
            <template #body="slotProps">
                <Checkbox v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
            </template>
        </Column>
        <Column class="kn-truncated" field="pubHoliday" :header="$t('managers.calendarManagement.publicHoliday')" :style="calendarManagementDetailTableDescriptor.columnStyle.checkbox" :sortable="true">
            <template #body="slotProps">
                <Checkbox v-model="slotProps.data[slotProps.column.props.field]" :binary="true"></Checkbox>
            </template>
        </Column>
        <Column class="kn-truncated" field="listOfAttributes" :header="$t('common.attributes')" :style="calendarManagementDetailTableDescriptor.columnStyle.checkEvent" :sortable="true">
            <template #body="slotProps">
                <MultiSelect v-model="slotProps.data[slotProps.column.props.field]" class="kn-material-input" :options="domains" option-label="attributeDomainDescr" option-value="attributeDomainDescr" />
            </template>
        </Column>
        <Column v-if="canManageCalendar" :style="calendarManagementDetailTableDescriptor.iconColumnStyle">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteItemConfirm(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDomain } from '../../CalendarManagement'
import { formatDate } from '@/helpers/commons/localeHelper'
import moment from 'moment'
import calendarManagementDetailTableDescriptor from './CalendarManagementDetailTableDescriptor.json'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import MultiSelect from 'primevue/multiselect'
import mainStore from '../../../../../App.store'

export default defineComponent({
    name: 'calendar-management-detail-table',
    components: { Checkbox, Column, DataTable, MultiSelect },
    props: { propCalendarInfo: { type: Array as PropType<any[]> }, domains: { type: Array as PropType<iDomain[]> } },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            calendarManagementDetailTableDescriptor,
            calendarSplitData: [] as any[]
        }
    },
    computed: {
        canManageCalendar(): boolean {
            return (this.store.$state as any).user.functionalities.includes('ManageCalendar')
        }
    },
    watch: {
        propCalendarInfo() {
            this.loadCalendarInfo()
        }
    },
    created() {
        this.loadCalendarInfo()
    },
    methods: {
        loadCalendarInfo() {
            this.calendarSplitData = this.propCalendarInfo as any[]
        },
        getFormattedDate(date: number) {
            const tempDate = moment(date).format('DD/MM/YYYY')
            return formatDate(tempDate, '', 'DD/MM/YYYY')
        },
        deleteItemConfirm(item: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteItem(item)
            })
        },
        deleteItem(item: any) {
            const index = this.calendarSplitData.findIndex((tempItem: any) => item.idCalComposition === tempItem.idCalComposition)
            if (index !== -1) this.calendarSplitData.splice(index, 1)
        }
    }
})
</script>
