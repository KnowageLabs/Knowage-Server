<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="fab fa-java"></i>
                <span class="p-m-2">{{ $t('managers.scheduler.sendToJavaClass') }}</span>
                <i v-if="document.invalid.invalidJavaClass" class="pi pi-exclamation-triangle kn-warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <div class="p-m-2">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.classPath') }} *</label>
                        <InputText
                            class="kn-material-input"
                            v-model="document.javaclasspath"
                            :class="{
                                'p-invalid': javaClassPathDirty && (!document.javaclasspath || document.javaclasspath.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.javaClass.classPathMaxLength"
                            @input="setJavaClassPathValidation"
                            @blur="setJavaClassPathValidation"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="javaClassPathDirty && (!document.javaclasspath || document.javaclasspath.length === 0)" class="p-error p-grid p-m-2">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.classPath') }) }}
                            </div>
                        </div>
                        <p class="name-help p-m-0">{{ javaClassPathHelp }}</p>
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
    name: 'scheduler-java-class-accordion',
    components: { Accordion, AccordionTab },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            document: null as any,
            javaClassPathDirty: false
        }
    },
    computed: {
        javaClassPathHelp(): string {
            return (this.document.javaclasspath?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.javaClass.classPathMaxLength
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
            this.document.invalid.invalidJavaClass = false
            this.validateDocument()
        },
        setJavaClassPathValidation() {
            this.javaClassPathDirty = true
            this.validateDocument()
        },
        validateDocument() {
            this.document.invalid.invalidJavaClass = !this.document.javaclasspath || this.document.javaclasspath.length === 0
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
