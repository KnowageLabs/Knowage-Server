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
            :responsiveLayout="schedulerDocumentsTableDescriptor.responsiveLayout"
            :breakpoint="schedulerDocumentsTableDescriptor.breakpoint"
        >
            <Column class="kn-truncated" :header="$t('common.name')" :style="schedulerDocumentsTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.name }}
                </template></Column
            >
            <Column :header="$t('managers.scheduler.parameters')">
                <template #body="slotProps">
                    <span v-if="checkIfParameterValuesSet(slotProps.data.parameters)">{{ slotProps.data.condensedParameters }}</span>
                    <span v-else class="warning-icon"> <i class="pi pi-exclamation-triangle"></i></span>
                    <Button v-if="slotProps.data.parameters.length > 0" icon="pi pi-pencil" class="p-button-link" @click="openDocumentParameterDialog(slotProps.data)" />
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
        <SchedulerDocumentParameterDialog :visible="documentParameterDialogVisible" :propParameters="selectedDocument?.parameters" :parameterWithValues="parameterWithValues" :roles="roles" @close="closeDocumentParameterDialog" @setParameters="onParametersSet"></SchedulerDocumentParameterDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import schedulerDocumentsTableDescriptor from './SchedulerDocumentsTableDescriptor.json'
import SchedulerDocumentsSelectionDialog from './SchedulerDocumentsSelectionDialog.vue'
import SchedulerDocumentParameterDialog from './SchedulerDocumentParameterDialog.vue'

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
            parameterWithValues: [] as any[]
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
            // console.log('DOCUMENTS: ', this.documents)
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
            // console.log('DOCUMENT TO REMOVE: ', documentIndex)
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
        onDocumentSelected(selectedDocument: any) {
            // console.log('SELECTED DOCUMENT: ', selectedDocument)

            this.documentsSelectionDialogVisible = false
            this.loadDocumentData(selectedDocument, true)
        },
        async loadDocumentData(document: any, pushToTable: boolean) {
            this.$emit('loading', true)
            // console.log('DOCUMENT FOR LOAD: ', document)
            if (document.parametersTouched) {
                return
            }
            const label = document.label ?? document.name
            const tempDocument = await this.loadDocumentInfo(label)
            // console.log('TEMP DOCUMENT: ', tempDocument)
            this.roles = await this.loadSelectedDocumentRoles(tempDocument)
            tempDocument.parameters = await this.loadSelectedDocumentParameters(label)
            tempDocument.condensedParameters = this.updateCondensedParameters(tempDocument.parameters)
            // console.log('TEMP DOCUMENT: ', tempDocument)
            // console.log('ROLES: ', this.roles)
            this.selectedDocument = { name: label, nameTitle: tempDocument.label, condensedParameters: tempDocument.condensedParameters, parameters: tempDocument.parameters }
            this.selectedDocument.parameters?.forEach((el: any) => (el.role = this.roles[0].role))
            if (pushToTable) this.documents.push(this.selectedDocument)
            this.$emit('loading', false)
        },
        async loadDocumentInfo(documentLabel: string) {
            let tempDocument = null as any
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${documentLabel}`).then((response) => (tempDocument = response.data))
            console.log('TEMP DOCUMENT: ', tempDocument)
            return tempDocument
        },
        async loadSelectedDocumentRoles(tempDocument: any) {
            let tempRoles = []
            let formatedRoles = [] as { userAndRole: string; user: string; role: string }[]
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${tempDocument.id}/userroles`).then((response) => (tempRoles = response.data))
            tempRoles.forEach((el: string) => {
                const userAndRole = el.split('|')
                formatedRoles.push({ userAndRole: el, user: userAndRole[0], role: userAndRole[1] })
            })

            //console.log('TEMP ROLES: ', tempRoles)
            //console.log('FORMATED ROLES: ', formatedRoles)
            return formatedRoles
        },
        async loadSelectedDocumentParameters(documentLabel: string) {
            let tempParameters = [] as any[]
            // let deletedParameters = []
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${documentLabel}/parameters`).then((response) => (tempParameters = response.data))
            // console.log('TEMP PARAMETERES: ', tempParameters)
            tempParameters = tempParameters.map((el: any) => {
                return { id: el.parID, name: el.parameterUrlName, value: '', type: 'fixed', iterative: false, temporal: el.parameter.temporal, documentLabel: documentLabel } as any
            })
            // console.log('TEMP PARAMETERES FORMATED: ', tempParameters)
            return tempParameters
        },
        updateCondensedParameters(parameters: any[]) {
            let condensedParameters = ''
            for (let i = 0; i < parameters.length; i++) {
                //console.log('parameters[i]', parameters[i])
                if (parameters[i].type === 'fixed') {
                    condensedParameters += ' ' + parameters[i].name + ' = ' + parameters[i].values
                    condensedParameters += i === parameters.length - 1 ? ' ' : ' | '
                }
            }
            // console.log('CONDENSED PARAMETERS: ', condensedParameters)
            return condensedParameters
        },
        async openDocumentParameterDialog(document: any) {
            // console.log('DOCUMENT: ', document)
            this.selectedDocument = document
            this.parameterWithValues = document.parameters
            await this.loadDocumentData(document, false)
            this.documentParameterDialogVisible = true
        },
        closeDocumentParameterDialog() {
            this.selectedDocument = null
            this.documentParameterDialogVisible = false
        },
        onParametersSet(parameters: any[]) {
            this.selectedDocument.parameters = parameters
            this.selectedDocument.condensedParameters = this.updateCondensedParameters(parameters)
            this.selectedDocument.parametersTouched = true

            const index = this.documents.findIndex((el: any) => el.name === this.selectedDocument.name)
            if (index !== -1) this.documents[index] = this.selectedDocument
            // console.log('DOCUMENT AFTER PARAMETERS SET: ', this.selectedDocument)
            // console.log('DOCUMENTS AFTER :', this.documents)
            this.documentParameterDialogVisible = false
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
