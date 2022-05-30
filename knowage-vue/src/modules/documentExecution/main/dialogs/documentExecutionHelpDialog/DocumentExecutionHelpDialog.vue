<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionHelpDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.onlineHelp') }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>

        <iframe id="document-execution-help-dialog-iframe" :src="url"></iframe>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import documentExecutionHelpDialogDescriptor from './DocumentExecutionHelpDialogDescriptor.json'

    export default defineComponent({
        name: 'document-execution-help-dialog',
        components: { Dialog },
        props: { visible: { type: Boolean }, propDocument: { type: Object } },
        emits: ['close'],
        data() {
            return {
                documentExecutionHelpDialogDescriptor,
                document: null as any
            }
        },
        computed: {
            url(): string {
                return this.document && this.visible ? process.env.VUE_APP_HOST_URL + `/knowage/restful-services/publish?PUBLISHER=glossaryHelpOnline?DOCUMENT=${this.document.id}&LABEL=${this.document.label}` : ''
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
                this.document = this.propDocument ? { ...this.propDocument } : {}
            },
            closeDialog() {
                this.$emit('close')
            }
        }
    })
</script>

<style lang="scss" scoped>
    #document-execution-help-dialog-iframe {
        height: 95%;
        width: 100%;
        border: none;
    }
</style>
