<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="pi pi-file"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsFile') }}</span>
                <i v-if="document.invalid" class="pi pi-exclamation-triangle warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <div>
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.fileName') }} *</label>
                        <InputText
                            class="kn-material-input  p-inputtext-sm"
                            v-model="document.fileName"
                            :class="{
                                'p-invalid': fileNameDirty && (!document.fileName || document.fileName.length === 0)
                            }"
                            :maxLength="100"
                            @input="setFileNameValidation"
                            @blur="setFileNameValidation"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="fileNameDirty && (!document.fileName || document.fileName.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.fileName') }) }}
                            </div>
                        </div>
                        <p class="name-help p-m-0">{{ fileNameHelp }}</p>
                    </div>
                </div>
                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.destinationFolder') }}</label>
                        <InputText class="kn-material-input  p-inputtext-sm" v-model="document.destinationfolder" :maxLength="100" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ destinationFolderHelp }}</small>
                    </div>
                </div>
                <div class="p-m-2">
                    <Checkbox v-model="document.zipFileDocument" :binary="true" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.zipFileDocument') }}</span>
                </div>
                <div class="kn-flex" v-if="document.zipFileDocument">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.zipFileName') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="document.zipFileName" :maxLength="100" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ zipFileNameHelp }}</small>
                    </div>
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

export default defineComponent({
    name: 'scheduler-file-accordion',
    components: { Accordion, AccordionTab, Checkbox },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            document: null as any,
            fileNameDirty: false
        }
    },
    computed: {
        fileNameHelp(): string {
            return (this.document.fileName?.length ?? '0') + ' / 100'
        },
        destinationFolderHelp(): string {
            return (this.document.destinationfolder?.length ?? '0') + ' / 100'
        },
        zipFileNameHelp(): string {
            return (this.document.zipFileName?.length ?? '0') + ' / 100'
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
            this.document.invalid = true
        },
        setFileNameValidation() {
            this.fileNameDirty = true
            this.document.invalid = !this.document.fileName || this.document.fileName.length === 0
        }
    }
})
</script>

<style lang="scss" scoped>
#snapshot-name-container {
    flex: 2;
}

.name-help {
    font-size: smaller;
}
</style>
