<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionLinkDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
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
            <div class="p-m-2">
                <p>{{ $t('documentExecution.main.copyLinkAndShare') }}</p>
            </div>

            <div class="p-fluid p-formgrid p-grid p-m-2">
                <div class="p-field p-col-12">
                    <Textarea v-if="embedHTML" class="kn-material-input" v-model="publicUrl" :rows="6"></Textarea>
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
import qs from 'qs'

export default defineComponent({
    name: 'document-execution-link-dialog',
    components: { Dialog, Textarea },
    props: { visible: { type: Boolean }, linkInfo: { type: Object }, embedHTML: { type: Boolean }, propDocument: { type: Object }, parameters: { type: Array } },
    emits: ['close'],
    data() {
        return {
            documentExecutionLinkDialogDescriptor,
            publicUrl: '',
            document: null as any,
            linkParameters: [] as any
        }
    },
    watch: {
        visible() {
            this.loadLink()
        },
        propDocument() {
            this.loadLink()
        },
        linkParameters() {
            this.loadLink()
        }
    },
    created() {
        this.loadLink()
    },
    methods: {
        loadLink() {
            this.loadDocument()
            this.loadParameters()
            this.getPublicUrl()
        },
        loadDocument() {
            this.document = this.propDocument
        },
        loadParameters() {
            this.linkParameters = this.parameters as any[]
        },
        getPublicUrl() {
            const tenet = (this.$store.state as any).user.organization

            if (!this.document) return

            if (this.document.typeCode === 'DATAMART' || this.document.typeCode === 'DOSSIER') {
                if (this.embedHTML) {
                    this.publicUrl = '<iframe width="600" height="600" src=' + import.meta.env.VITE_HOST_URL + this.$route.fullPath + ' frameborder="0"></iframe>'
                } else {
                    this.publicUrl = import.meta.env.VITE_HOST_URL + this.$route.fullPath
                }
            } else {
                if (this.embedHTML) {
                    this.publicUrl =
                        '<iframe width="600" height="600" src=' +
                        import.meta.env.VITE_HOST_URL +
                        `/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&OBJECT_LABEL=${this.document.label}&TOOLBAR_VISIBLE=true&ORGANIZATION=${tenet}&NEW_SESSION=true&PARAMETERS=${qs.stringify(this.linkParameters)}` +
                        ' frameborder="0"></iframe>'
                } else {
                    this.publicUrl = import.meta.env.VITE_HOST_URL + `/knowage/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&OBJECT_LABEL=${this.document.label}&TOOLBAR_VISIBLE=true&ORGANIZATION=${tenet}&NEW_SESSION=true&PARAMETERS=${qs.stringify(this.linkParameters)}`
                }
            }
        },
        closeDialog() {
            this.$emit('close')
            this.publicUrl = ''
        }
    }
})
</script>
