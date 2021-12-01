<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionLinkDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('documentExecution.main.linkToDocument ') }}
                </template>
            </Toolbar>
        </template>

        <div class="p-m-2">
            <div v-if="!linkInfo.isPublic" class="p-m-2">
                <p>
                    <i class="fa fa-exclamation-triangle p-mr-2"></i>
                    <span>{{ $t('documentExecution.main.publicUrlExecutionDisabled') }}</span>
                </p>
            </div>
            <div>
                <p>{{ $t('documentExecution.main.copyLinkAndShare') + ' (' + $t('documentExecution.main.linkToDocumentInfo') + ')' }}</p>
            </div>

            <div class="p-fluid p-formgrid p-grid p-m-2">
                <div class="p-field p-col-12">
                    <Textarea v-if="embedHTML" class="kn-material-input" v-model="publicUrl"></Textarea>
                    <InputText v-else class="kn-material-input p-inputtext-sm" v-model="publicUrl" />
                </div>
            </div>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import documentExecutionLinkDialogDescriptor from './DocumentExecutionLinkDialogDescriptor.json'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'document-execution-link-dialog',
    components: { Dialog, Textarea },
    props: { visible: { type: Boolean }, linkInfo: { type: Object }, embedHTML: { type: Boolean } },
    emits: ['close'],
    data() {
        return {
            documentExecutionLinkDialogDescriptor,
            publicUrl: this.$route.fullPath
        }
    },
    watch: {},
    created() {
        console.log('ROUTE: ', this.$route)
    },
    methods: {
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
