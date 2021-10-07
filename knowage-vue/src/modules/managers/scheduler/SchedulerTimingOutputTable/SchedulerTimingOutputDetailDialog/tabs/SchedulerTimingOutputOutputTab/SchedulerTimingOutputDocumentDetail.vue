<template>
    {{ document }}
    <div>
        <div class="p-d-flex p-flex-row p-m-2">
            <div class="p-m-2">
                <Checkbox v-model="document.saveassnapshot" :binary="true" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsSnapshot') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.saveasfile" :binary="true" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsFile') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.saveasdocument" :binary="true" />
                <span class="p-ml-2">{{ $t('managers.scheduler.saveAsDocument') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.sendtojavaclass" :binary="true" />
                <span class="p-ml-2">{{ $t('managers.scheduler.sendToJavaClass') }}</span>
            </div>
            <div class="p-m-2">
                <Checkbox v-model="document.sendmail" :binary="true" />
                <span class="p-ml-2">{{ $t('managers.scheduler.sendMail') }}</span>
            </div>
        </div>
        <div>
            <SchedulerDocumentAccordion v-if="document.saveasdocument" class="p-m-3" :propDocument="document" :functionalities="functionalities"></SchedulerDocumentAccordion>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Checkbox from 'primevue/checkbox'
import SchedulerDocumentAccordion from './accordions/SchedulerDocumentAccordion.vue'

export default defineComponent({
    name: 'scheduler-tming-output-document-detail',
    components: { Checkbox, SchedulerDocumentAccordion },
    props: { propDocument: { type: Object }, functionalities: { type: Array } },
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
        }
    }
})
</script>
