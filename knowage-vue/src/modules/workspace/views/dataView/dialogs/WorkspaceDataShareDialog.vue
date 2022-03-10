<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceDataShareDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.myData.shareDataset') }}
                </template>
            </Toolbar>
        </template>

        <Message class="p-m-4" severity="info" :closable="false" :style="workspaceDataShareDialogDescriptor.styles.message">
            {{ $t('workspace.myData.shareDatasetHint') }}
        </Message>

        <div class="p-m-4">
            <span>
                <Dropdown class="kn-material-input" v-model="dataset.catTypeId" :options="datasetCategories" optionLabel="VALUE_NM" optionValue="VALUE_ID" />
            </span>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="shareDataset(false)">{{ $t('workspace.myData.unshareDataset') }}</Button>
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="shareDataset(true)" :disabled="!dataset.catTypeId">{{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import Message from 'primevue/message'
    import workspaceDataShareDialogDescriptor from './WorkspaceDataShareDialogDescriptor.json'

    export default defineComponent({
        name: 'workspace-repository-move-dialog',
        components: { Dialog, Dropdown, Message },
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
            },
            closeDialog() {
                this.loadDataset()
                this.$emit('close')
            },
            shareDataset(share: boolean) {
                if (!share) {
                    this.dataset.catTypeId = null
                }
                this.$emit('share', this.dataset)
            }
        }
    })
</script>
