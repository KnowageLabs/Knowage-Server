<template>
    <div class="p-d-flex p-flex-row">
        <div class="p-col-4 p-sm-4 p-md-3  p-p-0">
            <Listbox class="kn-list--column" :options="documents" @change="showDocumentDetail($event.value)">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.label }}</span>
                        </div>
                        <i v-if="documentInvalid(slotProps.option)" class="pi pi-exclamation-triangle kn-warning-icon"></i>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="p-col-8 p-sm-8 p-md-9  p-p-0">
            <SchedulerTimingOutputDocumentDetail
                v-if="selectedDocument"
                :propDocument="selectedDocument"
                :functionalities="functionalities"
                :datasets="datasets"
                :jobInfo="jobInfo"
                :documentWithUniqueMail="documentWithUniqueMail"
                @sendUniqueMailSelected="onSendUniqueMailSelected"
                @uniqueMailOptionsChanged="updateOtherDocumentsAfterUniqueMailSelected"
            ></SchedulerTimingOutputDocumentDetail>
            <Message v-else class="p-m-4" severity="info" :closable="false" :style="schedulerTimingOutputOutputTabDescriptor.styles.message">
                {{ $t('managers.scheduler.noDocumentSelected') }}
            </Message>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import SchedulerTimingOutputDocumentDetail from './SchedulerTimingOutputDocumentDetail.vue'
import schedulerTimingOutputOutputTabDescriptor from './SchedulerTimingOutputOutputTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-timing-output-output-tab',
    components: { Listbox, Message, SchedulerTimingOutputDocumentDetail },
    props: { propDocuments: { type: Array }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            documents: [] as any[],
            selectedDocument: null,
            documentWithUniqueMail: null as any
        }
    },
    watch: {
        propDocuments() {
            this.loadDocuments()
        }
    },
    async created() {
        this.loadDocuments()
    },
    methods: {
        loadDocuments() {
            this.documents = this.propDocuments as any[]
        },
        showDocumentDetail(document: any) {
            this.selectedDocument = document
        },
        documentInvalid(document: any) {
            return (
                (document.invalid && (document.invalid.invalidSnapshot || document.invalid.invalidFile || document.invalid.invalidJavaClass || document.invalid.invalidMail || document.invalid.invalidDocument)) ||
                (!document.saveassnapshot && !document.saveasfile && !document.saveasdocument && !document.sendtojavaclass && !document.sendmail)
            )
        },
        onSendUniqueMailSelected(document: any) {
            if (this.documentWithUniqueMail && this.documentWithUniqueMail.uniqueMail === document.uniqueMail) return
            this.documents.forEach((tempDocument: any) => {
                if (document.id !== tempDocument.id && tempDocument.sendmail) this.updateOtherDocumentAfterUniqueMailSelected(tempDocument, document)
            })
            this.documentWithUniqueMail = document
        },
        updateOtherDocumentsAfterUniqueMailSelected(document: any) {
            this.documents.forEach((tempDocument: any) => this.setMailOptionsFromDocumentWithUniqueMail(tempDocument, document))
        },
        updateOtherDocumentAfterUniqueMailSelected(document: any, documentWithUniqueMail: any) {
            documentWithUniqueMail.uniqueMail ? this.setMailOptionsFromDocumentWithUniqueMail(document, documentWithUniqueMail) : this.resetMailOptions(document)
        },
        setMailOptionsFromDocumentWithUniqueMail(document: any, documentWithUniqueMail: any) {
            document.useFixedRecipients = documentWithUniqueMail.useFixedRecipients
            document.mailtos = documentWithUniqueMail.mailtos
            document.useDataset = documentWithUniqueMail.useDataset
            if (documentWithUniqueMail.datasetLabel !== undefined) document.datasetLabel = documentWithUniqueMail.datasetLabel
            if (documentWithUniqueMail.datasetParameter !== undefined) document.datasetParameter = documentWithUniqueMail.datasetParameter
            if (documentWithUniqueMail.useExpression !== undefined) document.useExpression = documentWithUniqueMail.useExpression
            if (documentWithUniqueMail.zipMailDocument !== undefined) document.zipMailDocument = documentWithUniqueMail.zipMailDocument
            if (documentWithUniqueMail.reportNameInSubject !== undefined) document.reportNameInSubject = documentWithUniqueMail.reportNameInSubject
            if (documentWithUniqueMail.mailsubj !== undefined) document.mailsubj = documentWithUniqueMail.mailsubj
            if (documentWithUniqueMail.containedFileName !== undefined) document.containedFileName = documentWithUniqueMail.containedFileName
            if (documentWithUniqueMail.mailtxt !== undefined) document.mailtxt = documentWithUniqueMail.mailtxt
            if (!document.invalid) document.invalid = {}
            document.invalid.invalidMail = false
        },
        resetMailOptions(document: any) {
            document.useFixedRecipients = true
            document.mailtos = ''
            if (document.useDataset) document.useDataset = false
            if (document.datasetLabel) document.datasetLabel = ''
            if (document.datasetParameter) document.datasetParameter = ''
            if (document.useExpression) document.useExpression = false
            if (document.uniqueMail) document.uniqueMail = false
            if (document.zipMailDocument) document.zipMailDocument = false
            if (document.reportNameInSubject) document.reportNameInSubject = false
            if (document.mailsubj) document.mailsubj = ''
            if (document.containedFileName) document.containedFileName = ''
            if (document.mailtxt) document.mailtxt = ''
            if (!document.invalid) document.invalid = {}
            document.invalid.invalidMail = true
        }
    }
})
</script>
