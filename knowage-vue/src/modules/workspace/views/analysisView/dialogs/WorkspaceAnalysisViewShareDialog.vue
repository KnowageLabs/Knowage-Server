<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceAnalysisViewShareDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.myRepository.destinationFolder') }}
                </template>
            </Toolbar>
        </template>

        <WorkspaceAnalysisFolderTree :propFolders="folders" @foldersSelected="setSelectedFolders"></WorkspaceAnalysisFolderTree>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="shareDocument">{{ $t('common.share') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { INode } from '../../../Workspace'
import Dialog from 'primevue/dialog'
import workspaceAnalysisViewShareDialogDescriptor from './WorkspaceAnalysisViewShareDialogDescriptor.json'
import WorkspaceAnalysisFolderTree from '../tree/WorkspaceAnalysisFolderTree.vue'

export default defineComponent({
    name: 'workspace-analysis-view-share-dialog',
    components: { Dialog, WorkspaceAnalysisFolderTree },
    props: { visible: { type: Boolean }, propFolders: { type: Array } },
    emits: ['close', 'share'],
    data() {
        return {
            workspaceAnalysisViewShareDialogDescriptor,
            folders: [] as any[],
            selectedFoldersKeys: {} as any
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
            this.folders = []
            this.$emit('close')
        },
        shareDocument() {
            this.$emit('share', this.selectedFoldersKeys)
        },
        setSelectedFolders(folders: INode[]) {
            this.selectedFoldersKeys = folders
        }
    }
})
</script>
