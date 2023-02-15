<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :content-style="documentExecutionMailDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.sendMail') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-fluid p-formgrid p-grid p-mx-2 p-my-4">
            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.to') }} *</label>
                <InputText
                    v-model="mail.TO"
                    class="kn-material-input p-inputtext-sm"
                    :class="{
                        'p-invalid': mailToDirty && (!mail.TO || mail.TO.length === 0)
                    }"
                    @input="mailToDirty = true"
                    @blur="mailToDirty = true"
                />
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.cc') }}</label>
                <InputText v-model="mail.CC" class="kn-material-input p-inputtext-sm" :max-length="documentExecutionMailDialogDescriptor.ccMaxLength" />
            </div>
            <div class="p-d-flex p-flex-row p-jc-end p-col-12">
                <p class="max-length-help p-m-0">{{ ccHelp }}</p>
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.object') }}</label>
                <InputText v-model="mail.OBJECT" class="kn-material-input p-inputtext-sm" :max-length="documentExecutionMailDialogDescriptor.objectMaxLength" />
            </div>
            <div class="p-d-flex p-flex-row p-jc-end p-col-12">
                <p class="max-length-help p-m-0">{{ objectHelp }}</p>
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.message') }}</label>
                <Editor v-model="mail.MESSAGE" class="p-m-2" :editor-style="documentExecutionMailDialogDescriptor.editor.style"></Editor>
            </div>

            <Accordion class="p-col-12">
                <AccordionTab>
                    <template #header>
                        <span>{{ $t('common.advanced') }}</span>
                    </template>

                    <Message class="p-m-2" severity="info" :closable="false" :style="documentExecutionMailDialogDescriptor.styles.message">
                        {{ $t('documentExecution.main.sendEmailMessage') }}
                    </Message>

                    <div class="p-grid p-mt-4">
                        <div class="p-field p-col-6">
                            <label class="kn-material-input-label">{{ $t('common.login') }}</label>
                            <InputText v-model="mail.LOGIN" class="kn-material-input p-inputtext-sm" :max-length="documentExecutionMailDialogDescriptor.objectMaxLength" />
                        </div>

                        <div class="p-field p-col-6">
                            <label class="kn-material-input-label">{{ $t('common.password') }}</label>
                            <InputText v-model="mail.PASSWORD" class="kn-material-input p-inputtext-sm" type="password" :max-length="documentExecutionMailDialogDescriptor.objectMaxLength" />
                        </div>

                        <div class="p-field p-col-12">
                            <label class="kn-material-input-label">{{ $t('common.replyTo') }}</label>
                            <InputText v-model="mail.REPLAYTO" class="kn-material-input p-inputtext-sm" :max-length="documentExecutionMailDialogDescriptor.objectMaxLength" aria-describedby="reply-to-help" />
                            <small id="reply-to-help">{{ $t('documentExecution.main.replyToHint') }}</small>
                        </div>
                    </div>
                </AccordionTab>
            </Accordion>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" :disabled="saveButtonDisabled" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMail } from '../../DocumentExecution'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Dialog from 'primevue/dialog'
import Editor from 'primevue/editor'
import documentExecutionMailDialogDescriptor from './DocumentExecutionMailDialogDescriptor.json'
import Message from 'primevue/message'

export default defineComponent({
    name: 'document-execution-help-dialog',
    components: { Accordion, AccordionTab, Dialog, Editor, Message },
    props: { visible: { type: Boolean } },
    emits: ['close', 'sendMail'],
    data() {
        return {
            documentExecutionMailDialogDescriptor,
            mail: {} as iMail,
            mailToDirty: false
        }
    },
    computed: {
        ccHelp(): string {
            return (this.mail.CC?.length ?? '0') + ' / ' + this.documentExecutionMailDialogDescriptor.ccMaxLength
        },
        objectHelp(): string {
            return (this.mail.OBJECT?.length ?? '0') + ' / ' + this.documentExecutionMailDialogDescriptor.objectMaxLength
        },
        saveButtonDisabled(): boolean {
            return !this.mail.TO || this.mail.TO.length === 0
        }
    },
    created() {},
    methods: {
        closeDialog() {
            this.mail = {} as iMail
            this.$emit('close')
        },
        save() {
            this.$emit('sendMail', this.mail)
        }
    }
})
</script>

<style lang="scss" scoped>
.max-length-help {
    font-size: smaller;
}
</style>
