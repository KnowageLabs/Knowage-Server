<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('common.documents') }}
            </template>
            <template #end>
                <Button class="kn-button p-button-text p-button-rounded" @click="openDocumentsSelectionDialog">{{ $t('common.add') }}</Button>
            </template>
        </Toolbar>
        <Message class="p-m-4" v-if="documents?.length === 0" severity="info" :closable="false" :style="schedulerDocumentsTableDescriptor.styles.message">
            {{ $t('managers.scheduler.noDocumentsInfo') }}
        </Message>
        <DataTable
            v-else
            id="documents-datatable"
            :value="documents"
            :paginator="true"
            :rows="schedulerDocumentsTableDescriptor.rows"
            class="p-datatable-sm kn-table p-m-2"
            dataKey="name"
            :responsiveLayout="schedulerDocumentsTableDescriptor.responsiveLayout"
            :breakpoint="schedulerDocumentsTableDescriptor.breakpoint"
            data-test="documents-table"
        >
            <Column class="kn-truncated" :header="$t('common.name')" :style="schedulerDocumentsTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.name }}
                </template></Column
            >
            <Column :header="$t('common.parameters')">
                <template #body="slotProps">
                    <span v-if="checkIfParameterValuesSet(slotProps.data.parameters)">{{ slotProps.data.condensedParameters }}</span>
                    <span v-else v-tooltip.top="$t('managers.scheduler.parametersWarningTooltip')"> <i class="pi pi-exclamation-triangle kn-warning-icon" :data-test="'warning-icon-' + slotProps.data.name"></i></span>
                    <Button v-if="slotProps.data.parameters?.length > 0" icon="pi pi-pencil" class="p-button-link" @click="openDocumentParameterDialog(slotProps.data)" />
                </template>
                ></Column
            >
            <Column :style="schedulerDocumentsTableDescriptor.iconColumnStyle">
                <template #body="slotProps">
                    <Button icon="pi pi-trash" class="p-button-link" @click="removeDocumentConfirm(slotProps.index)" />
                </template>
            </Column>
        </DataTable>

        <SchedulerDocumentsSelectionDialog :visible="documentsSelectionDialogVisible" :propFiles="files" @close="documentsSelectionDialogVisible = false" @documentSelected="onDocumentSelected"></SchedulerDocumentsSelectionDialog>
        <SchedulerDocumentParameterDialog
            :visible="documentParameterDialogVisible"
            :propParameters="selectedDocument?.parameters"
            :roles="roles"
            :deletedParams="deletedParams"
            @close="closeDocumentParameterDialog"
            @setParameters="onParametersSet"
            :documentLabel="documentLabel"
        ></SchedulerDocumentParameterDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import schedulerDocumentsTableDescriptor from './SchedulerDocumentsTableDescriptor.json'
import SchedulerDocumentsSelectionDialog from './SchedulerDocumentsSelectionDialog.vue'
import SchedulerDocumentParameterDialog from './SchedulerDocumentParameterDialog.vue'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'scheduler-documents-table',
    components: { Column, DataTable, Message, SchedulerDocumentsSelectionDialog, SchedulerDocumentParameterDialog },
    props: { jobDocuments: { type: Array } },
    emits: ['loading'],
    data() {
        return {
            schedulerDocumentsTableDescriptor,
            documents: [] as any[],
            files: [] as any[],
            documentsSelectionDialogVisible: false,
            selectedDocument: null as any,
            documentParameterDialogVisible: false,
            roles: [] as any[],
            parameterWithValues: [] as any[],
            deletedParams: [] as any[],
            documentLabel: ''
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
            this.documents = this.jobDocuments as any[]
        },
        checkIfParameterValuesSet(parameters: any[]) {
            let valuesSet = true

            if (parameters) {
                for (let i = 0; i < parameters.length; i++) {
                    if (!parameters[i].value) {
                        valuesSet = false
                        break
                    }
                }
            }

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
            this.documents.splice(documentIndex, 1)
        },
        async openDocumentsSelectionDialog() {
            this.$emit('loading', true)
            if (this.files.length === 0) {
                await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `scheduleree/folders/?includeDocs=true`).then((response: AxiosResponse<any>) => (this.files = response.data))
            }
            this.documentsSelectionDialogVisible = true
            this.$emit('loading', false)
        },
        onDocumentSelected(selectedDocument: any) {
            this.documentsSelectionDialogVisible = false
            if (selectedDocument) {
                this.loadDocumentData(selectedDocument, true)
            }
        },
        async loadDocumentData(document: any, pushToTable: boolean) {
            this.$emit('loading', true)

            if (document?.parametersTouched) {
                this.$emit('loading', false)
                return
            }
            const label = document.label ?? document.name
            const tempDocument = await this.loadDocumentInfo(label)

            this.roles = await this.loadSelectedDocumentRoles(tempDocument)

            const tempParams = await this.loadSelectedDocumentParameters(label)
            this.updateDocumentParameters(document, tempParams)

            tempDocument.condensedParameters = this.updateCondensedParameters(tempParams)

            this.selectedDocument = { name: label, nameTitle: tempDocument.label, condensedParameters: tempDocument.condensedParameters, parameters: document.parameters }
            this.selectedDocument.parameters?.forEach((el: any) => (el.role = this.roles[0]?.role))
            if (pushToTable) this.documents.push(this.selectedDocument)
            this.$emit('loading', false)
        },
        updateDocumentParameters(document: any, apiParameters: any[]) {
            if (!document.parameters) {
                document.parameters = apiParameters
            } else {
                this.deletedParams = []
                for (let i = 0; i < apiParameters.length; i++) {
                    const index = document.parameters?.findIndex((el: any) => el.name === apiParameters[i].name)
                    if (index === -1) {
                        this.deletedParams.push(document.parameters[i])
                    } else {
                        document.parameters[index].id = apiParameters[i].id
                        document.parameters[index].temporal = apiParameters[i].temporal
                    }
                }
            }
        },
        async loadDocumentInfo(documentLabel: string) {
            let tempDocument = null as any
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${documentLabel}`).then((response: AxiosResponse<any>) => (tempDocument = response.data))
            return tempDocument
        },
        async loadSelectedDocumentRoles(tempDocument: any) {
            let tempRoles = []
            let formatedRoles = [] as { userAndRole: string; user: string; role: string }[]
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${tempDocument.id}/userroles`).then((response: AxiosResponse<any>) => (tempRoles = response.data))
            tempRoles.forEach((el: string) => {
                const userAndRole = el.split('|')
                formatedRoles.push({ userAndRole: el, user: userAndRole[0], role: userAndRole[1] })
            })

            return formatedRoles
        },
        async loadSelectedDocumentParameters(documentLabel: string) {
            let tempParameters = [] as any[]
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${documentLabel}/parameters`).then((response: AxiosResponse<any>) => (tempParameters = response.data))

            tempParameters = tempParameters.map((el: any) => {
                return { id: el.parID, name: el.parameterUrlName, value: '', type: 'fixed', iterative: false, temporal: el.parameter.temporal, documentLabel: documentLabel } as any
            })

            return tempParameters
        },
        updateCondensedParameters(parameters: any[]) {
            let condensedParameters = ''
            for (let i = 0; i < parameters.length; i++) {
                if (parameters[i].type === 'fixed') {
                    condensedParameters += ' ' + parameters[i].name + ' = ' + parameters[i].value
                    condensedParameters += i === parameters.length - 1 ? ' ' : ' | '
                }
            }

            return condensedParameters
        },
        async openDocumentParameterDialog(document: any) {
            this.selectedDocument = document
            this.parameterWithValues = document.parameters
            await this.loadDocumentData(document, false)
            this.documentLabel = this.selectedDocument.name
            this.documentParameterDialogVisible = true
        },
        closeDocumentParameterDialog() {
            this.$emit('loading', false)
            this.selectedDocument = null
            this.documentParameterDialogVisible = false
        },
        onParametersSet(parameters: any[]) {
            this.selectedDocument.parameters = parameters
            this.selectedDocument.condensedParameters = this.updateCondensedParameters(parameters)
            this.selectedDocument.parametersTouched = true

            const index = this.documents.findIndex((el: any) => el.name === this.selectedDocument.name)
            if (index !== -1) this.documents[index] = this.selectedDocument
            this.documentParameterDialogVisible = false
        }
    }
})
</script>

<style lang="scss">
#documents-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
