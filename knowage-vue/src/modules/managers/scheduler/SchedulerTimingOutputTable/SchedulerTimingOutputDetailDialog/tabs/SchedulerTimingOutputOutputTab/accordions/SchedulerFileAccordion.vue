<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="pi pi-file"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsFile') }}</span>
                <i v-if="document.invalid?.invalidFile" class="pi pi-exclamation-triangle kn-warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.fileName') }} *</label>
                        <InputText
                            class="kn-material-input  p-inputtext-sm"
                            v-model="document.fileName"
                            :class="{
                                'p-invalid': fileNameDirty && (!document.fileName || document.fileName.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.file.nameMaxLength"
                            @input="setFileNameValidation"
                            @blur="setFileNameValidation"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="fileNameDirty && (!document.fileName || document.fileName.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('common.fileName') }) }}
                            </div>
                        </div>
                        <p class="name-help p-m-0">{{ fileNameHelp }}</p>
                    </div>
                </div>

                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.destinationFolder') }}</label>
                        <InputText class="kn-material-input  p-inputtext-sm" v-model="document.destinationfolder" :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.file.destinationFolderMaxLength" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ destinationFolderHelp }}</small>
                    </div>
                </div>

                <div class="p-m-2">
                    <Checkbox v-model="document.zipFileDocument" :binary="true" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.zipFileDocument') }}</span>
                </div>
                <div class="kn-flex p-mx-2 p-my-4" v-if="document.zipFileDocument">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.zipFileName') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="document.zipFileName" :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.file.zipFileNameMaxLength" />
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
import schedulerTimingOutputOutputTabDescriptor from '../SchedulerTimingOutputOutputTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-file-accordion',
    components: { Accordion, AccordionTab, Checkbox },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            document: null as any,
            fileNameDirty: false
        }
    },
    computed: {
        fileNameHelp(): string {
            return (this.document.fileName?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.file.nameMaxLength
        },
        destinationFolderHelp(): string {
            return (this.document.destinationfolder?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.file.destinationFolderMaxLength
        },
        zipFileNameHelp(): string {
            return (this.document.zipFileName?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.file.zipFileNameMaxLength
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
            this.document.invalid.invalidFile = false
            this.validateDocument()
        },
        setFileNameValidation() {
            this.fileNameDirty = true
            this.validateDocument()
        },
        validateDocument() {
            this.document.invalid.invalidFile = !this.document.fileName || this.document.fileName.length === 0
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
