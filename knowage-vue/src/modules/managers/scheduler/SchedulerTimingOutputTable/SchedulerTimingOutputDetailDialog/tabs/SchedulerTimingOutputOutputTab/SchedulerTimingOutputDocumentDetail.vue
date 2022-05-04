<template>
    <div>
        <div class="p-d-flex p-flex-row p-m-2">
            <div class="p-m-2">
                <Checkbox v-model="document.saveassnapshot" :binary="true" @change="removeProperty('saveassnapshot')" data-test="snapshot-checkbox" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsSnapshot') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.saveasfile" :binary="true" @change="removeProperty('saveasfile')" data-test="file-checkbox" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsFile') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.saveasdocument" :binary="true" @change="removeProperty('saveasdocument')" data-test="document-checkbox" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsDocument') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.sendtojavaclass" :binary="true" @change="removeProperty('sendtojavaclass')" data-test="java-checkbox" />
                <span class="p-ml-2">{{ $t('managers.scheduler.sendToJavaClass') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.sendmail" :binary="true" @change="removeProperty('sendmail')" data-test="mail-checkbox" />
                <span class="p-ml-2">{{ $t('managers.scheduler.sendMail') }}</span>
            </div>
        </div>
        <div v-if="document">
            <SchedulerSnapshotAccordion v-if="document.saveassnapshot" class="p-m-3" :propDocument="document" data-test="snapshot-accordion"></SchedulerSnapshotAccordion>
            <SchedulerFileAccordion v-if="document.saveasfile" class="p-m-3" :propDocument="document" data-test="file-accordion"></SchedulerFileAccordion>
            <SchedulerDocumentAccordion v-if="document.saveasdocument" class="p-m-3" :propDocument="document" :functionalities="functionalities" :datasets="datasets" :jobInfo="jobInfo" data-test="document-accordion"></SchedulerDocumentAccordion>
            <SchedulerJavaClassAccordion v-if="document.sendtojavaclass" class="p-m-3" :propDocument="document" data-test="java-accordion"></SchedulerJavaClassAccordion>
            <SchedulerMailAccordion v-if="document.sendmail" class="p-m-3" :propDocument="document" :datasets="datasets" :jobInfo="jobInfo" data-test="mail-accordion"></SchedulerMailAccordion>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Checkbox from 'primevue/checkbox'
import SchedulerSnapshotAccordion from './accordions/SchedulerSnapshotAccordion.vue'
import SchedulerFileAccordion from './accordions/SchedulerFileAccordion.vue'
import SchedulerDocumentAccordion from './accordions/SchedulerDocumentAccordion.vue'
import SchedulerJavaClassAccordion from './accordions/SchedulerJavaClassAccordion.vue'
import SchedulerMailAccordion from './accordions/SchedulerMailAccordion.vue'

export default defineComponent({
    name: 'scheduler-tming-output-document-detail',
    components: { Checkbox, SchedulerSnapshotAccordion, SchedulerFileAccordion, SchedulerJavaClassAccordion, SchedulerDocumentAccordion, SchedulerMailAccordion },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            document: null as any
        }
    },
    watch: {
        propDocument() {
            this.loadDocument()
        }
    },
    created() {
        this.loadDocument()
    },
    methods: {
        loadDocument() {
            this.document = this.propDocument
            this.document.invalid = {}
        },
        removeProperty(propName: string) {
            if (!this.document[propName]) {
                delete this.document[propName]
            }

            if (this.document.invalid) {
                this.resetInvalid(propName)
            }
        },
        resetInvalid(propName: string) {
            switch (propName) {
                case 'saveassnapshot':
                    this.document.invalid.invalidSnapshot = false
                    break
                case 'saveasfile':
                    this.document.invalid.invalidFile = false
                    break
                case 'saveasdocument':
                    this.document.invalid.invalidDocument = false
                    break
                case 'sendtojavaclass':
                    this.document.invalid.invalidJavaClass = false
                    break
                case 'sendmail':
                    this.document.invalid.invalidMail = false
            }
        }
    }
})
</script>
