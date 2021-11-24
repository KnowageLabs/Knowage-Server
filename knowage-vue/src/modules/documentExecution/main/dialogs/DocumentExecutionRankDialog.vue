<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionRankDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('common.rank') }}
                </template>
            </Toolbar>
        </template>

        {{ documentRank }}

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import documentExecutionRankDialogDescriptor from './DocumentExecutionRankDialogDescriptor.json'

export default defineComponent({
    name: 'document-execution-help-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, propDocumentRank: { type: Object } },
    emits: ['close'],
    data() {
        return {
            documentExecutionRankDialogDescriptor,
            documentRank: null as any
        }
    },

    watch: {
        propDocument() {
            this.loadDocumentRank()
        }
    },
    created() {
        this.loadDocumentRank()
    },
    methods: {
        loadDocumentRank() {
            this.documentRank = this.propDocumentRank ? { ...this.propDocumentRank } : {}
            console.log('DOCUMENT RANK DIALOG LOADED: ', this.documentRank)
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
