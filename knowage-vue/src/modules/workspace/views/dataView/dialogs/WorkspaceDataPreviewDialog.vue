<template>
    <Dialog :style="workspaceDataPreviewDialogDescriptor.dialog.style" :header="$t('kpi.kpiScheduler.saveScheduler')" :visible="visible" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <DatasetPreviewTable></DatasetPreviewTable>
        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import DatasetPreviewTable from '../tables/DatasetPreviewTable.vue'
import workspaceDataPreviewDialogDescriptor from './WorkspaceDataPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'kpi-scheduler-save-dialog',
    components: { Dialog, DatasetPreviewTable },
    props: { visible: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            workspaceDataPreviewDialogDescriptor
        }
    },
    created() {},
    methods: {}
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}

.full-screen-dialog .p-dialog .p-dialog-content {
    padding: 0;
}
</style>
