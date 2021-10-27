<template>
    <DataTable :value="schedulations" id="schedulations-table" class="p-datatable-sm kn-table" dataKey="triggerName" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>

        <Column :headerStyle="workspaceSchedulationSchedulationsTableDescriptor.checkboxColumn.style">
            <template #body="slotProps"> <Checkbox :name="index" v-model="selectedSchedulations" :value="slotProps.data" @change="setSelectedSchedulations"></Checkbox> </template
        ></Column>
        <Column class="kn-truncated" v-for="col of workspaceSchedulationSchedulationsTableDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
        <Column :style="workspaceSchedulationSchedulationsTableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button icon="fa fa-play-circle" class="p-button-link" v-tooltip.top="$t('common.run')" @click.stop="runSchedulation(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { ITrigger } from '../../../Workspace'
import Checkbox from 'primevue/checkbox'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import workspaceSchedulationSchedulationsTableDescriptor from './WorkspaceSchedulationSchedulationsTableDescriptor.json'

export default defineComponent({
    name: 'workspace-schedulation-schedulations-list',
    components: { Checkbox, Column, DataTable },
    props: { triggers: { type: Array }, index: { type: Number }, propSelectedSchedulations: { type: Object } },
    emits: ['runSchedulationClick', 'selectedSchedulations'],
    data() {
        return {
            workspaceSchedulationSchedulationsTableDescriptor,
            schedulations: [] as ITrigger[],
            selectedSchedulations: [] as ITrigger[]
        }
    },
    watch: {
        triggers() {
            this.loadSchedulations()
        },
        propSelectedSchedulations: {
            handler() {
                this.loadSelectedSchedulations()
            },
            deep: true
        }
    },
    created() {
        this.loadSchedulations()
        this.loadSelectedSchedulations()
        // console.log('CAAAAAAAAAALED', this.index)
    },
    methods: {
        loadSchedulations() {
            this.schedulations = this.triggers as ITrigger[]
            // console.log('LOADED SCHEDULATIONS: ', this.schedulations)
        },
        loadSelectedSchedulations() {
            this.selectedSchedulations = this.propSelectedSchedulations?.[this.index as number] as any
            // console.log('LOADED SELECTED SCHEDULATIONS', this.propSelectedSchedulations)
            // console.log('LOADED INDEX', this.index)
        },
        runSchedulation(schedulation: ITrigger) {
            this.$emit('runSchedulationClick', schedulation)
        },
        setSelectedSchedulations() {
            // console.log('SELECTED SCHEDULATIONS: ', this.selectedSchedulations)
            this.$emit('selectedSchedulations', { index: this.index, schedulations: this.selectedSchedulations })
        }
    }
})
</script>
