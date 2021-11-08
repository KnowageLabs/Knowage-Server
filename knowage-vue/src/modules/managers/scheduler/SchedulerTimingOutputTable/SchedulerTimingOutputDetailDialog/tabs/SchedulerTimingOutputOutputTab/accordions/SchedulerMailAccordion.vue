<template>
    <Accordion :activeIndex="0">
        <AccordionTab>
            <template #header>
                <i class="fa fa-envelope"></i>
                <span class="p-m-4">{{ $t('managers.scheduler.sendMail') }}</span>
                <i v-if="document.invalid?.invalidMail" class="pi pi-exclamation-triangle kn-warning-icon" data-test="warning-icon"></i>
            </template>

            <div v-if="document">
                <Message class="p-m-4" severity="info" :closable="true" :style="schedulerTimingOutputOutputTabDescriptor.styles.message">
                    <span v-html="$t('managers.scheduler.fixedRecipientsListHelp')"></span>
                </Message>

                <div class="p-m-4">
                    <Checkbox v-model="document.useFixedRecipients" :binary="true" @change="removeDocumentExpressionAndDatasets" />
                    <span class="p-ml-2">{{ $t('managers.scheduler.fixedRecipientsList') }}</span>
                </div>

                <div v-if="document.useFixedRecipients" class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.mailTo') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="document.mailtos"
                            :class="{
                                'p-invalid': fixedRecipientsListDirty && (!document.mailtos || document.mailtos.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailToMaxLength"
                            @input="validateDocument('fixedRecipientsListDirty')"
                            @blur="validateDocument('fixedRecipientsListDirty')"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="fixedRecipientsListDirty && (!document.mailtos || document.mailtos.length === 0)" class="p-error p-grid p-m-4">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.fixedRecipientsList') }) }}
                            </div>
                        </div>
                        <p class="max-length-help p-m-0">{{ mailToHelp }}</p>
                    </div>
                </div>

                <div v-if="drivers.length > 0" class="p-m-4">
                    <Message severity="info" :closable="true" :style="schedulerTimingOutputOutputTabDescriptor.styles.message">
                        <span>{{ $t('managers.scheduler.useDatasetListHelp') }}</span>
                    </Message>

                    <div class="p-my-4">
                        <Checkbox v-model="document.useDataset" :binary="true" @change="removeDocumentFixedRecipientsAndExpression" />
                        <span class="p-ml-2" v-html="$t('managers.scheduler.useDatasetList')"></span>
                    </div>

                    <div v-if="document.useDataset">
                        <div>
                            <span>
                                <label class="kn-material-input-label">{{ $t('managers.scheduler.datasetVerification') }} *</label>
                                <Dropdown
                                    class="kn-material-input"
                                    v-model="document.datasetLabel"
                                    :class="{
                                        'p-invalid': datasetLabelDirty && (!document.datasetLabel || document.datasetLabel?.length === 0)
                                    }"
                                    :options="datasets"
                                    optionLabel="label"
                                    optionValue="label"
                                    @blur="validateDocument('datasetLabelDirty')"
                                    @change="validateDocument('datasetLabelDirty')"
                                />
                                <div v-show="datasetLabelDirty && (!document.datasetLabel || document.datasetLabel?.length === 0)" class="p-error p-grid p-m-4">
                                    {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.datasetVerification') }) }}
                                </div>
                            </span>
                        </div>

                        <div class="p-my-2">
                            <span>
                                <label class="kn-material-input-label">{{ $t('common.parameter') }} *</label>
                                <Dropdown
                                    class="kn-material-input"
                                    v-model="document.datasetParameter"
                                    :class="{
                                        'p-invalid': datasetParameterDirty && (!document.datasetParameter || document.datasetParameter?.length === 0)
                                    }"
                                    :options="drivers"
                                    @blur="validateDocument('datasetParameterDirty')"
                                    @change="validateDocument('datasetParameterDirty')"
                                />
                                <div v-show="datasetParameterDirty && (!document.datasetParameter || document.datasetParameter?.length === 0)" class="p-error p-grid p-m-4">
                                    {{ $t('common.validation.required', { fieldName: $t('common.parameter') }) }}
                                </div>
                            </span>
                        </div>
                    </div>
                </div>

                <Message class="p-m-4" severity="info" :closable="true" :style="schedulerTimingOutputOutputTabDescriptor.styles.message">
                    <span v-html="$t('managers.scheduler.useExpressionHelp')"></span>
                </Message>

                <div class="p-m-4">
                    <Checkbox v-model="document.useExpression" :binary="true" @change="removeDocumentFixedRecipientsAndDatasets" />
                    <span class="p-ml-2" v-html="$t('managers.scheduler.useExpression')"></span>
                </div>

                <div v-if="document.useExpression" class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.expression') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="document.expression"
                            :class="{
                                'p-invalid': expressionDirty && (!document.expression || document.expression.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.expressionMaxLength"
                            @input="validateDocument('expressionDirty')"
                            @blur="validateDocument('expressionDirty')"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="expressionDirty && (!document.expression || document.expression.length === 0)" class="p-error p-grid p-m-4">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.expression') }) }}
                            </div>
                        </div>
                        <p class="max-length-help p-m-0">{{ expresionHelp }}</p>
                    </div>
                </div>

                <div class="p-m-4">
                    <Checkbox v-model="document.uniqueMail" :binary="true" />
                    <span class="p-ml-2" v-html="$t('managers.scheduler.uniqueMail')"></span>
                </div>

                <div class="p-m-4">
                    <Checkbox v-model="document.zipMailDocument" :binary="true" />
                    <span class="p-ml-2" v-html="$t('managers.scheduler.zipMailDocument')"></span>
                </div>

                <div v-if="document.zipMailDocument" class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.zipFileName') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="document.zipMailName" :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.zipMailNameMaxLength" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ zipMailNameHelp }}</small>
                    </div>
                </div>

                <div class="p-m-4">
                    <Checkbox v-model="document.reportNameInSubject" :binary="true" />
                    <span class="p-ml-2" v-html="$t('managers.scheduler.reportNameInSubject')"></span>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.mailSubject') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="document.mailsubj"
                            :class="{
                                'p-invalid': subjectDirty && (!document.mailsubj || document.mailsubj.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailSubjectMaxLength"
                            @input="validateDocument('subjectDirty')"
                            @blur="validateDocument('subjectDirty')"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="subjectDirty && (!document.mailsubj || document.mailsubj.length === 0)" class="p-error p-grid p-m-4">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.mailSubject') }) }}
                            </div>
                        </div>
                        <p class="max-length-help p-m-0">{{ mailSubjectHelp }}</p>
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('common.fileName') }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="document.containedFileName" :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.fileNameMaxLength" />
                    </span>
                    <div class="p-d-flex p-jc-end">
                        <small>{{ fileNameHelp }}</small>
                    </div>
                </div>

                <div class="p-m-4">
                    <span>
                        <label class="kn-material-input-label">{{ $t('managers.scheduler.mailTextMessage') }} *</label>
                        <InputText
                            class="kn-material-input p-inputtext-sm"
                            v-model="document.mailtxt"
                            :class="{
                                'p-invalid': mailTextDirty && (!document.mailtxt || document.mailtxt.length === 0)
                            }"
                            :maxLength="schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailTextMaxLength"
                            @input="validateDocument('mailTextDirty')"
                            @blur="validateDocument('mailTextDirty')"
                        />
                    </span>
                    <div class="p-d-flex p-flex-row p-jc-between">
                        <div>
                            <div v-show="mailTextDirty && (!document.mailtxt || document.mailtxt.length === 0)" class="p-error p-grid p-m-4">
                                {{ $t('common.validation.required', { fieldName: $t('managers.scheduler.mailMessage') }) }}
                            </div>
                        </div>
                        <p class="max-length-help p-m-0">{{ mailTextHelp }}</p>
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
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import schedulerTimingOutputOutputTabDescriptor from '../SchedulerTimingOutputOutputTabDescriptor.json'

export default defineComponent({
    name: 'scheduler-mail-accordion',
    components: { Accordion, AccordionTab, Checkbox, Dropdown, Message },
    props: { propDocument: { type: Object }, functionalities: { type: Array }, datasets: { type: Array }, jobInfo: { type: Object } },
    data() {
        return {
            schedulerTimingOutputOutputTabDescriptor,
            document: null as any,
            drivers: [],
            subjectDirty: false,
            mailTextDirty: false,
            fixedRecipientsListDirty: false,
            expressionDirty: false,
            datasetLabelDirty: false,
            datasetParameterDirty: false
        }
    },
    computed: {
        mailToHelp(): string {
            return (this.document.mailtos?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailToMaxLength
        },
        expresionHelp(): string {
            return (this.document.expression?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.expressionMaxLength
        },
        zipMailNameHelp(): string {
            return (this.document.zipMailName?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.zipMailNameMaxLength
        },
        mailSubjectHelp(): string {
            return (this.document.mailsubj?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailSubjectMaxLength
        },
        fileNameHelp(): string {
            return (this.document.containedFileName?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.fileNameMaxLength
        },
        mailTextHelp(): string {
            return (this.document.mailtxt?.length ?? '0') + ' / ' + schedulerTimingOutputOutputTabDescriptor.accordion.mail.mailTextMaxLength
        }
    },
    watch: {
        propDocument() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.loadDocument()
            this.loadDrivers()
        },
        loadDocument() {
            this.document = this.propDocument
            if (!this.document.useFixedRecipients && !this.document.useExpression && !this.document.useDataset) {
                this.document.useFixedRecipients = true
            }
            this.document.invalid.invalidMail = false
            this.validateDocument(null)
        },
        loadDrivers() {
            const index = this.jobInfo?.documents.findIndex((el: any) => el.label === this.document.label)
            if (index !== -1) {
                this.drivers = this.jobInfo?.documents[index].parameters
            }
        },
        removeDocumentExpressionAndDatasets() {
            this.validateDocument(null)
            if (this.document.useFixedRecipients) {
                this.resetExpression()
                this.resetFolderDataset()
            }
        },
        removeDocumentFixedRecipientsAndDatasets() {
            this.validateDocument(null)
            if (this.document.useExpression) {
                this.resetFixedRecipients()
                this.resetFolderDataset()
            }
        },
        removeDocumentFixedRecipientsAndExpression() {
            this.validateDocument(null)
            if (this.document.useDataset) {
                this.resetFixedRecipients()
                this.resetExpression()
            }
        },
        resetFixedRecipients() {
            if (this.document.useFixedRecipients) {
                this.document.useFixedRecipients = false
                delete this.document.mailtos
            }
        },
        resetExpression() {
            if (this.document.useExpression) {
                this.document.useExpression = false
                delete this.document.expression
            }
        },
        resetFolderDataset() {
            if (this.document.useDataset) {
                this.document.useDataset = false
                delete this.document.useDataset.datasetLabel
                delete this.document.useDataset.parameters
            }
        },
        validateDocument(dirty: string | null) {
            if (dirty) {
                this[dirty] = true
            }
            const subjectInvalid = !this.document.mailsubj || this.document.mailsubj.length === 0
            const mailTextInvalid = !this.document.mailtxt || this.document.mailtxt.length === 0
            const fixedRecipientsListInvalid = this.document.useFixedRecipients && (!this.document.mailtos || this.document.mailtos.length === 0)
            const expressionInvalid = this.document.useExpression && (!this.document.expression || this.document.expression.length === 0)
            const datasetInvalid = this.document.useDataset && (!this.document.datasetLabel || this.document.datasetLabel?.length === 0 || !this.document.datasetParameter || this.document.datasetParameter?.length === 0)

            this.document.invalid.invalidMail = subjectInvalid || mailTextInvalid || fixedRecipientsListInvalid || expressionInvalid || datasetInvalid
        }
    }
})
</script>

<style lang="scss" scoped>
#snapshot-name-container {
    flex: 2;
}

.max-length-help {
    font-size: smaller;
}
</style>
