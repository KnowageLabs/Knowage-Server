<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="fa fa-file"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsDocument') }}</span>
            </template>

            <div v-if="document">
                <div>
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.name') }}</label>
                        <InputText class="kn-material-input" v-model="document.documentname" :maxLength="100" />
                    </span>
                </div>
                <div>
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText class="kn-material-input" v-model="document.documentdescription" :maxLength="100" />
                    </span>
                </div>
                <div class="p-m-2">
                    <Checkbox v-model="document.useFixedFolder" :binary="true" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.fixedFolder') }}</span>
                </div>
                <div v-if="document.useFixedFolder">
                    <SchedulerDocumentAccordionTree :propFunctionalities="functionalities" :propSelectedFolders="document.funct" @selected="setSelectedFolders"></SchedulerDocumentAccordionTree>
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Checkbox from 'primevue/checkbox'
import SchedulerDocumentAccordionTree from './SchedulerDocumentAccordionTree.vue'

export default defineComponent({
    name: 'scheduler-document-accordion',
    components: { Accordion, AccordionTab, Checkbox, SchedulerDocumentAccordionTree },
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
        },
        setSelectedFolders(folders: any[]) {
            console.log('SELECTED FOlDERS: ', folders)
        }
    }
})
</script>
