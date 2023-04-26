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
            <SchedulerTimingOutputDocumentDetail v-if="selectedDocument" :prop-document="selectedDocument" :functionalities="functionalities" :datasets="datasets" :job-info="jobInfo"></SchedulerTimingOutputDocumentDetail>
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
            selectedDocument: null
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
        }
    }
})
</script>
