<template>
    <Card class="p-m-2">
        <template #content>
            <TimespanIntervalForm :propTimespan="propTimespan" />

            <DataTable v-if="timespan && timespan.definition?.length > 0" class="p-datatable-sm kn-table p-m-2" :value="timespan.definition" responsiveLayout="stack" breakpoint="960px" :scrollable="true" scrollHeight="60vh">
                <Column v-for="column in columns" :key="column.header" :field="column.field" :header="$t(column.header)" :style="column.style"> </Column>
                <Column :style="timespanDescriptor.iconColumnStyle">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteInterval(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iTimespan, iInterval } from './Timespan'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import timespanDescriptor from './TimespanDescriptor.json'
import Card from 'primevue/card'
import TimespanIntervalForm from './TimespanIntervalForm.vue'
export default defineComponent({
    name: 'timespan-interval-table',
    props: { propTimespan: { type: Object as PropType<iTimespan | null> } },
    components: { Column, DataTable, Card, TimespanIntervalForm },
    data() {
        return {
            timespanDescriptor,
            timespan: null as iTimespan | null
        }
    },
    watch: {
        propTimespan() {
            this.loadTimespan()
        }
    },
    computed: {
        columns(): { field: string; header: string; style: string }[] {
            return this.timespan?.type === 'temporal' ? this.timespanDescriptor.temporalColumns : this.timespanDescriptor.timeColumns
        }
    },
    created() {
        this.loadTimespan()
    },
    methods: {
        loadTimespan() {
            this.timespan = this.propTimespan as iTimespan
        },
        deleteInterval(interval: iInterval) {
            if (this.timespan) {
                const index = this.timespan.definition.findIndex((tempInterval: iInterval) => interval.from === tempInterval.from && interval.to === tempInterval.to)
                if (index !== -1) this.timespan.definition.splice(index, 1)
            }
        }
    }
})
</script>
