<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :content-style="documentExecutionSchedulationsTableDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.snapshot') }}
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeDialog" />
                </template>
            </Toolbar>
        </template>

        <iframe id="document-execution-snapshot-dialog-iframe" :src="url"></iframe>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Dialog from 'primevue/dialog'
    import documentExecutionSchedulationsTableDescriptor from './DocumentExecutionSchedulationsTableDescriptor.json'

    export default defineComponent({
        name: 'document-execution-help-dialog',
        components: { Dialog },
        props: { visible: { type: Boolean }, propUrl: { type: String } },
        emits: ['close'],
        data() {
            return {
                documentExecutionSchedulationsTableDescriptor,
                url: '' as string
            }
        },
        watch: {
            propUrl() {
                this.loadUrl()
            }
        },
        created() {
            this.loadUrl()
        },
        methods: {
            loadUrl() {
                this.url = this.propUrl as any
            },
            closeDialog() {
                this.$emit('close')
            }
        }
    })
</script>

<style lang="scss" scoped>
    #document-execution-snapshot-dialog-iframe {
        height: 95%;
        width: 100%;
        border: none;
    }
</style>
