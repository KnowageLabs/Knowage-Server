<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="workspaceSchedulationOldSchedulationsDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('workspace.schedulation.executedSchedulations') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <WorkspaceSchedulationOldSchedulationsTable :propSchedulations="schedulations"></WorkspaceSchedulationOldSchedulationsTable>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IPackage, ISchedulation } from '../../../Workspace'
import Dialog from 'primevue/dialog'
import workspaceSchedulationOldSchedulationsDialogDescriptor from './WorkspaceSchedulationOldSchedulationsDialogDescriptor.json'
import WorkspaceSchedulationOldSchedulationsTable from './WorkspaceSchedulationOldSchedulationsTable.vue'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'workspace-schedulation-old-schedulations-dialog',
    components: { Dialog, WorkspaceSchedulationOldSchedulationsTable },
    props: { visible: { type: Boolean }, selectedJob: { type: Object as PropType<any> } },
    emits: ['close'],
    data() {
        return {
            workspaceSchedulationOldSchedulationsDialogDescriptor,
            job: null as IPackage | null,
            schedulations: [] as ISchedulation[],
            loading: false
        }
    },
    watch: {
        selectedJob() {
            this.loadJob()
        }
    },
    created() {
        this.loadJob()
    },
    methods: {
        async loadJob() {
            this.loading = true
            this.job = { ...this.selectedJob } as IPackage
            await this.loadAllSchedulers()
            this.loading = false
        },
        closeDialog() {
            this.$emit('close')
        },
        async loadAllSchedulers() {
            this.schedulations = []
            if (this.job && this.job.documents) {
                for (let i = 0; i < this.job.documents.length; i++) {
                    let documentId = await this.getDocumentId(this.job.documents[i].name)
                    if (documentId) {
                        this.loadDocumentSchedulers(documentId, this.job.jobName)
                    }
                }
            }
        },
        async getDocumentId(documentName: string) {
            let documentId = null
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/workspace/scheduler/${documentName}`).then((response: AxiosResponse<any>) => {
                documentId = response.data
            })
            return documentId
        },
        async loadDocumentSchedulers(documentId: number, schedulerName: string) {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documentsnapshot/getSnapshotsForSchedulationAndDocument?id=${documentId}&scheduler=${schedulerName}`).then((response: AxiosResponse<any>) => {
                response.data?.schedulers.forEach((el: ISchedulation) => this.schedulations.push({ ...el, urlPath: response.data.urlPath }))
            })
        }
    }
})
</script>
