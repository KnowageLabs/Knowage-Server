<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="fa fa-camera"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.saveAsSnapshot') }}</span>
                <i v-if="document.invalid?.invalidSnapshot" class="pi pi-exclamation-triangle kn-warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <div class="p-d-flex p-flex-row">
                    <div id="snapshot-name-container" class="p-m-2">
                        <span>
                            <label class="kn-material-input-label">{{ $t('common.name') }} *</label>
                            <InputText
                                class="kn-material-input"
                                v-model="document.snapshotname"
                                :class="{
                                    'p-invalid': snapshotNameDirty && (!document.snapshotname || document.snapshotname.length === 0)
                                }"
                                :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.snapshot.nameMaxLength"
                                @input="setNameValidation"
                                @blur="setNameValidation"
                            />
                        </span>
                        <div class="p-d-flex p-flex-row p-jc-between">
                            <div>
                                <div v-show="snapshotNameDirty && (!document.snapshotname || document.snapshotname.length === 0)" class="p-error p-grid p-m-2">
                                    {{ $t('common.validation.required', { fieldName: $t('common.name') }) }}
                                </div>
                            </div>
                            <p class="name-help p-m-0">{{ nameHelp }}</p>
                        </div>
                    </div>

                    <div class="p-m-2">
                        <span>
                            <label class="kn-material-input-label">{{ $t('managers.scheduler.historyLength') }}</label>
                            <InputText class="kn-material-input" type="number" v-model="document.snapshothistorylength" />
                        </span>
                    </div>
                </div>

                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.description') }}</label>
                        <InputText class="kn-material-input" v-model="document.snapshotdescription" :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.snapshot.descriptionMaxLength" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ descriptionHelp }}</small>
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
import schedulerTimingOutputOutputTabDescriptor from '../SchedulerTimingOutputOutputTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-snapshot-accordion',
    components: { Accordion, AccordionTab },
    props: { propDocument: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            document: null as any,
            snapshotNameDirty: false
        }
    },
    computed: {
        nameHelp(): string {
            return (this.document.snapshotname?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.snapshot.nameMaxLength
        },
        descriptionHelp(): string {
            return (this.document.snapshotdescription?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.snapshot.descriptionMaxLength
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
            this.document.invalid.invalidSnapshot = false
            this.validateDocument()
        },
        setNameValidation() {
            this.snapshotNameDirty = true
            this.validateDocument()
        },
        validateDocument() {
            this.document.invalid.invalidSnapshot = !this.document.snapshotname || this.document.snapshotname.length === 0
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
