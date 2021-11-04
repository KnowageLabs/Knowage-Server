<template>
    <Dialog :style="workspaceDataPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12" :style="mainDescriptor.style.maxWidth">
                <template #left>
                    {{ $t('workspace.myData.datasetPreview') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-button-rounded p-button-plain" :label="$t('common.close')" @click="$emit('close')"></Button>
                    <!-- <Button class="kn-button kn-button--primary p-button-sm" :label="$t('common.close')" @click="$emit('close')"></Button> -->
                </template>
            </Toolbar>
        </template>

        <DatasetPreviewTable></DatasetPreviewTable>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import DatasetPreviewTable from '../tables/DatasetPreviewTable.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import workspaceDataPreviewDialogDescriptor from './WorkspaceDataPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'kpi-scheduler-save-dialog',
    components: { Dialog, DatasetPreviewTable },
    props: { visible: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            mainDescriptor,
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

::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
