<template>
    <div>
        <Dialog :header="title" :style="workspaceWarningDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <Message class="p-m-2" severity="warn" :closable="false" :style="workspaceWarningDialogDescriptor.styles.message">
                {{ warningMessage }}
            </Message>
            <template #footer>
                <Button class="kn-button kn-button--primary" @click="$emit('close')"> {{ $t('common.close') }}</Button>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import workspaceWarningDialogDescriptor from './WorkspaceWarningDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-warning-dialog',
    components: { Dialog, Message },
    emits: ['close'],
    props: { visible: { type: Boolean }, title: { type: String }, warningMessage: { type: String } },
    data() {
        return {
            workspaceWarningDialogDescriptor
        }
    }
})
</script>
