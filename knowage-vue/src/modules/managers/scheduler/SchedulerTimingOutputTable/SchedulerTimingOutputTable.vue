<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.scheduler.timingAndOutput') }}
            </template>
            <template #right>
                <Button class="kn-button p-button-text p-button-rounded">{{ $t('common.add') }}</Button>
            </template>
        </Toolbar>
        <Message class="p-m-2" v-if="triggers.length === 0" severity="info" :closable="false" :style="schedulerTimingOutputTableDescriptor.styles.message">
            {{ $t('managers.scheduler.noTriggersInfo') }}
        </Message>
        <DataTable
            v-else
            id="triggers-datatable"
            :value="triggers"
            :paginator="true"
            :rows="schedulerTimingOutputTableDescriptor.rows"
            class="p-datatable-sm kn-table"
            dataKey="triggerName"
            :responsiveLayout="schedulerTimingOutputTableDescriptor.responsiveLayout"
            :breakpoint="schedulerTimingOutputTableDescriptor.breakpoint"
        >
            <Column class="kn-truncated" :header="$t('common.name')" :style="schedulerTimingOutputTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.triggerName }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('common.type')" :style="schedulerTimingOutputTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.triggerChronType }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('managers.scheduler.startDate')" :style="schedulerTimingOutputTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.triggerStartDate }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('managers.scheduler.endDate')" :style="schedulerTimingOutputTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.triggerEndDate }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('managers.scheduler.paused')" :style="schedulerTimingOutputTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.triggerIsPaused ? $t('common.yes') : $t('common.no') }}
                </template></Column
            >

            <Column :style="schedulerTimingOutputTableDescriptor.iconColumnStyle">
                <template #body>
                    <Button icon="pi pi-trash" class="p-button-link" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import schedulerTimingOutputTableDescriptor from './SchedulerTimingOutputTableDescriptor.json'

export default defineComponent({
    name: 'scheduler-timing-output-table',
    components: { Column, DataTable },
    props: { jobTriggers: { type: Array } },
    data() {
        return {
            schedulerTimingOutputTableDescriptor,
            triggers: [] as any[]
        }
    },
    watch: {
        propTriggers() {
            this.loadTriggers()
        }
    },
    created() {
        this.loadTriggers()
    },
    methods: {
        loadTriggers() {
            this.triggers = this.jobTriggers as any[]
            console.log('TRIGGERS: ', this.triggers)
        }
    }
})
</script>

<style lang="scss">
#documents-datatable .p-datatable-wrapper {
    height: auto;
}

.warning-icon {
    color: rgb(209, 209, 26);
}
</style>
