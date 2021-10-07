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
                        <InputText class="kn-material-input" v-model="document.documentname" :maxLength="10" />
                    </span>
                </div>
            </div>
        </AccordionTab>
    </Accordion>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'

export default defineComponent({
    name: 'scheduler-document-accordion',
    components: { Accordion, AccordionTab },
    props: { propDocument: { type: Object } },
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
    async created() {},
    methods: {
        loadDocument() {
            this.document = this.propDocument
        }
    }
})
</script>
