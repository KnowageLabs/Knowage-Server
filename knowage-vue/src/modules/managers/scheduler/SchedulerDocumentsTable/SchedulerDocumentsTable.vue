<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.scheduler.documents') }}
            </template>
            <template #right>
                <Button class="kn-button p-button-text p-button-rounded" @click="openDocumentsSelectionDialog">{{ $t('common.add') }}</Button>
            </template>
        </Toolbar>
        <Message class="p-m-2" v-if="documents.length === 0" severity="info" :closable="false" :style="schedulerDocumentsTableDescriptor.styles.message">
            {{ $t('managers.scheduler.noDocumentsInfo') }}
        </Message>
        <DataTable
            v-else
            id="documents-datatable"
            :value="documents"
            :paginator="true"
            :rows="schedulerDocumentsTableDescriptor.rows"
            class="p-datatable-sm kn-table"
            dataKey="name"
            :globalFilterFields="schedulerDocumentsTableDescriptor.globalFilterFields"
            :responsiveLayout="schedulerDocumentsTableDescriptor.responsiveLayout"
            :breakpoint="schedulerDocumentsTableDescriptor.breakpoint"
        >
            <Column class="kn-truncated" :header="$t('common.name')" :style="schedulerDocumentsTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.name }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('managers.scheduler.parameters')">
                <template #body="slotProps">
                    <span v-if="checkIfParameterValuesSet(slotProps.data.parameters)">{{ getParametersString(slotProps.data.parameters) }}</span>
                    <span v-else class="warning-icon"> <i class="pi pi-exclamation-triangle"></i></span>
                    <Button v-if="slotProps.data.parameters.length > 0" icon="pi pi-pencil" class="p-button-link" />
                </template>
                ></Column
            >
            <Column :style="schedulerDocumentsTableDescriptor.iconColumnStyle">
                <template #body="slotProps">
                    <Button icon="pi pi-trash" class="p-button-link" @click="removeDocumentConfirm(slotProps.index)" />
                </template>
            </Column>
        </DataTable>

        <SchedulerDocumentsSelectionDialog :visible="documentsSelectionDialogVisible" :propFiles="files" @close="documentsSelectionDialogVisible = false" @documentsSelected="onDocumentsSelected"></SchedulerDocumentsSelectionDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDocument, iParameter } from '../Scheduler'
import axios from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import schedulerDocumentsTableDescriptor from './SchedulerDocumentsTableDescriptor.json'
import SchedulerDocumentsSelectionDialog from './SchedulerDocumentsSelectionDialog.vue'

export default defineComponent({
    name: 'scheduler-documents-table',
    components: { Column, DataTable, Message, SchedulerDocumentsSelectionDialog },
    props: { jobDocuments: { type: Array } },
    emits: ['loading'],
    data() {
        return {
            schedulerDocumentsTableDescriptor,
            documents: [] as iDocument[],
            files: [] as any[],
            documentsSelectionDialogVisible: false
        }
    },
    watch: {
        jobDocuments() {
            this.loadDocuments()
        }
    },
    created() {
        this.loadDocuments()
    },
    methods: {
        loadDocuments() {
            this.documents = this.jobDocuments as iDocument[]
            console.log('DOCUMENTS: ', this.documents)
        },
        getParametersString(parameters: iParameter[]) {
            let parameterString = ''
            for (let i = 0; i < parameters.length; i++) {
                parameterString += parameters[i].name + ' = ' + parameters[i].value
                parameterString += i === parameters.length - 1 ? '' : ' | '
            }
            return parameterString
        },
        checkIfParameterValuesSet(parameters: iParameter[]) {
            let valuesSet = true

            if (parameters) {
                for (let i = 0; i < parameters.length; i++) {
                    if (!parameters[i].value) {
                        valuesSet = false
                        break
                    }
                }
            }

            // console.log('PARAMTERS VALUES SET: ', valuesSet)
            return valuesSet
        },
        removeDocumentConfirm(documentIndex: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.removeDocument(documentIndex)
            })
        },
        removeDocument(documentIndex: number) {
            console.log('DOCUMENT TO REMOVE: ', documentIndex)
            this.documents.splice(documentIndex, 1)
        },
        async openDocumentsSelectionDialog() {
            this.$emit('loading', true)
            if (this.files.length === 0) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/folders/?includeDocs=true`).then((response) => (this.files = response.data))
            }
            // console.log('LOADED FILES: ', this.files)
            this.documentsSelectionDialogVisible = true
            this.$emit('loading', false)
        },
        onDocumentsSelected(selectedDocuments: any[]) {
            console.log('SELECTED DOCUMENTS: ', selectedDocuments)
            selectedDocuments.forEach((document: any) => this.loadDocumentData(document))
            // this.documents = selectedDocuments
            this.documentsSelectionDialogVisible = false
        },
        async loadDocumentData(document: any) {
            this.$emit('loading', true)
            console.log('DOCUMENT FOR LOAD: ', document)
            await this.loadDocumentInfo(document.label)
            await this.loadSelectedDocumentRoles(document.id)

            this.$emit('loading', false)
        },
        async loadDocumentInfo(documentLabel: string) {
            let tempDocument = null as any
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${documentLabel}`).then((response) => (tempDocument = response.data))
            console.log('TEMP DOCUMENT: ', tempDocument)
            return tempDocument
        },
        async loadSelectedDocumentRoles(documentId: number) {
            let tempRoles = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${documentId}/userroles`).then((response) => (tempRoles = response.data))
            console.log('TEMP ROLES: ', tempRoles)
            return tempRoles
        }
    }
})
</script>

<style lang="scss">
#documents-datatable .p-datatable-wrapper {
    height: auto;
}

.warning-icon {
    color: rgb(209, 209, 26);
}
</style>
