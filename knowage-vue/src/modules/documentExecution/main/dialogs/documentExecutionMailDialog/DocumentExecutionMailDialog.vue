<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionMailDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('common.sendMail') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-fluid p-formgrid p-grid p-mx-2 p-my-4">
            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.to') }} *</label>
                <InputText
                    class="kn-material-input p-inputtext-sm"
                    v-model="mail.TO"
                    :class="{
                        'p-invalid': mailToDirty && (!mail.TO || mail.TO.length === 0)
                    }"
                    @input="mailToDirty = true"
                    @blur="mailToDirty = true"
                />
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.cc') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="mail.CC" :maxLength="documentExecutionMailDialogDescriptor.ccMaxLength" />
            </div>
            <div class="p-d-flex p-flex-row p-jc-end p-col-12">
                <p class="max-length-help p-m-0">{{ ccHelp }}</p>
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.object') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="mail.OBJECT" :maxLength="documentExecutionMailDialogDescriptor.objectMaxLength" />
            </div>
            <div class="p-d-flex p-flex-row p-jc-end p-col-12">
                <p class="max-length-help p-m-0">{{ objectHelp }}</p>
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.message') }}</label>
                <Editor v-model="mail.MESSAGE" class="p-m-2" :editorStyle="documentExecutionMailDialogDescriptor.editor.style"></Editor>
            </div>

            <div class="p-field p-col-6">
                <label class="kn-material-input-label">{{ $t('common.login') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="mail.LOGIN" :maxLength="documentExecutionMailDialogDescriptor.objectMaxLength" />
            </div>

            <div class="p-field p-col-6">
                <label class="kn-material-input-label">{{ $t('common.password') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" type="password" v-model="mail.PASSWORD" :maxLength="documentExecutionMailDialogDescriptor.objectMaxLength" />
            </div>

            <div class="p-field p-col-12">
                <label class="kn-material-input-label">{{ $t('common.replyTo') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="mail.REPLAYTO" :maxLength="documentExecutionMailDialogDescriptor.objectMaxLength" />
            </div>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save" :disabled="saveButtonDisabled"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMail } from '../../DocumentExecution'
import Dialog from 'primevue/dialog'
import Editor from 'primevue/editor'
import documentExecutionMailDialogDescriptor from './DocumentExecutionMailDialogDescriptor.json'

export default defineComponent({
    name: 'document-execution-help-dialog',
    components: { Dialog, Editor },
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
