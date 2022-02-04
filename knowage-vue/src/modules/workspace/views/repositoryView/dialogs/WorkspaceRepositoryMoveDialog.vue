<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceRepositoryMoveDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.myRepository.destinationFolder') }}
                </template>
            </Toolbar>
        </template>

        <WorkspaceDocumentTree :propFolders="folders" mode="move" @folderSelected="setSelectedFolder"></WorkspaceDocumentTree>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="moveDocument">{{ $t('common.move') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import workspaceRepositoryMoveDialogDescriptor from './WorkspaceRepositoryMoveDialogDescriptor.json'
import WorkspaceDocumentTree from '../../../genericComponents/WorkspaceDocumentTree.vue'

export default defineComponent({
    name: 'workspace-repository-move-dialog',
    components: { Dialog, WorkspaceDocumentTree },
    props: { visible: { type: Boolean }, propFolders: { type: Array } },
    emits: ['close', 'move'],
    data() {
        return {
            workspaceRepositoryMoveDialogDescriptor,
            folders: [] as any[],
            selectedFolder: null as any
        }
    },
    watch: {
        propFolders() {
            this.loadFolders()
        }
    },
    created() {
        this.loadFolders()
    },
    methods: {
        loadFolders() {
            this.folders = this.propFolders as any[]
        },
        closeDialog() {
            this.selectedFolder = null
            this.$emit('close')
        },
        moveDocument() {
            this.$emit('move', this.selectedFolder)
        },
        setSelectedFolder(folder: any) {
            this.selectedFolder = folder
        }
    }
})
</script>
