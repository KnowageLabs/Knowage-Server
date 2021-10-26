<template>
    <h1>IT WORKS</h1>
    {{ schedulations }}
</template>

<script lang="ts">
import { ISchedulation } from '@/modules/workspace/Workspace'
import { defineComponent } from 'vue'
import workspaceSchedulationOldSchedulationsTableDescriptor from './WorkspaceSchedulationOldSchedulationsTableDescriptor.json'

export default defineComponent({
    name: 'workspace-schedulation-old-schedulations-table',
    components: {},
    props: { propSchedulations: { type: Array } },
    data() {
        return {
            workspaceSchedulationOldSchedulationsTableDescriptor,
            schedulations: [] as ISchedulation[]
        }
    },
    watch: {
        propSchedulations() {
            this.loadSchedulations()
        }
    },
    created() {
        this.loadSchedulations()
    },
    methods: {
        loadSchedulations() {
            this.schedulations = this.propSchedulations as ISchedulation[]
        }
    }
})
</script>
