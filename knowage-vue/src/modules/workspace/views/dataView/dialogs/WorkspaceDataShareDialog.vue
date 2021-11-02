<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceDataShareDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('workspace.myData.shareDataset') }}
                </template>
            </Toolbar>
        </template>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary">{{ $t('workspace.myData.unshareDataset') }}</Button>
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="shareDataset">{{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import workspaceDataShareDialogDescriptor from './WorkspaceDataShareDialogDescriptor.json'

export default defineComponent({
    name: 'workspace-repository-move-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, propDataset: { type: Object }, datasetCategories: { type: Array } },
    emits: ['close', 'share'],
    data() {
        return {
            workspaceDataShareDialogDescriptor,
            dataset: {} as any
        }
    },
    watch: {
        propDataset() {
            this.loadDataset()
        }
    },
    created() {
        this.loadDataset()
    },
    methods: {
        loadDataset() {
            if (this.propDataset) {
                this.dataset = { ...this.propDataset }
            }
            console.log('DATASET LOADED IN SHARE DIALOG: ', this.dataset)
            console.log('DATASET CATEGORIES IN SHARE DIALOG: ', this.datasetCategories)
        },
        closeDialog() {
            this.loadDataset()
            this.$emit('close')
        },
        shareDataset() {
            this.$emit('share', this.dataset)
            this.loadDataset()
        }
    }
})
</script>
