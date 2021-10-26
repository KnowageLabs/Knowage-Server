<template>
    <DataTable :value="schedulations" id="schedulations-table" class="p-datatable-sm kn-table" v-model:selection="selectedSchedulations" dataKey="triggerName" :paginator="true" :rows="20" responsiveLayout="stack" breakpoint="960px" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect">
        <template #empty>
            {{ $t('common.info.noDataFound') }}
        </template>

        <Column selectionMode="multiple" :headerStyle="workspaceSchedulationSchedulationsTableDescriptor.checkboxColumn.style"></Column>
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
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import workspaceSchedulationSchedulationsTableDescriptor from './WorkspaceSchedulationSchedulationsTableDescriptor.json'

export default defineComponent({
    name: 'workspace-schedulation-schedulations-list',
    components: { Column, DataTable },
    props: { triggers: { type: Array } },
    emits: ['runSchedulationClick'],
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
        }
    },
    created() {
        this.loadSchedulations()
    },
    methods: {
        loadSchedulations() {
            this.schedulations = this.triggers as ITrigger[]
            console.log('LOADED SCHEDULATIONS: ', this.schedulations)
        },
        runSchedulation(schedulation: ITrigger) {
            this.$emit('runSchedulationClick', schedulation)
        },
        onRowSelect() {
            console.log('SELECTED SCHEDULATIONS: ', this.selectedSchedulations)
        },
        onRowUnselect() {
            console.log('SELECTED SCHEDULATIONS: ', this.selectedSchedulations)
        }
    }
})
</script>
