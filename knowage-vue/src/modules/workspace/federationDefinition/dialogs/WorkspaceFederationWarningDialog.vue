<template>
    <div>
        <Dialog :header="$t('workspace.federationDefinition.datasetDetail')" :style="workspaceFederationWarningDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
            <Message class="p-m-2" severity="warn" :closable="false" :style="workspaceFederationWarningDialogDescriptor.styles.message">
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
import workspaceFederationWarningDialogDescriptor from './WorkspaceFederationWarningDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-federation-warning-dialog',
    components: { Dialog, Message },
    emits: ['close'],
    props: { visible: { type: Boolean }, warningMessage: { type: String } },
    data() {
        return {
            workspaceFederationWarningDialogDescriptor
        }
    }
})
</script>
