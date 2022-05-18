<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionMetadataDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.metadata') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <div v-if="metadata">
            <div v-if="metadata.generalMetadata.length > 0">
                <h2>{{ $t('common.documentDetails') }}</h2>
                <div class="p-grid p-ai-center">
                    <template v-for="(meta, index) in metadata.generalMetadata" :key="index">
                        <div v-if="meta.value && index !== metadata.generalMetadata.length - 1" :class="{ 'p-col-4': index !== 3, 'p-col-12': index === 3 }">
                            <label class="kn-material-input-label">{{ meta.name }}</label>
                            <InputText v-if="index !== 3" class="kn-material-input p-inputtext-sm" v-model="meta.value" :disabled="true" />
                            <Textarea v-else v-model="meta.value" rows="2" :disabled="true" />
                        </div>
                    </template>
                </div>
            </div>

            <div v-if="metadata.shortText.length > 0 || metadata.longText.length > 0">
                <h2>{{ $t('documentExecution.main.customMetadata') }}</h2>

                <div v-show="metadata.shortText.length > 0" class="p-grid">
                    <div v-for="(meta, index) in metadata.shortText" :key="index" class="p-col-4">
                        <label class="kn-material-input-label">{{ meta.name }}</label>
                        <InputText class="kn-material-input p-inputtext-sm" v-model="meta.value" :disabled="!canModify" />
                    </div>
                </div>
            </div>

            <TabView v-if="(metadata.shortText.length > 0 || metadata.longText.length > 0) && metadata.longText.length > 0" scrollable>
                <TabPanel v-for="(meta, index) in metadata.longText" :key="index">
                    <template #header>
                        <span class="p-text-uppercase kn-truncated">{{ meta.name }}</span>
                    </template>

                    <Editor v-model="meta.value" :readonly="!canModify" :editorStyle="documentExecutionMetadataDialogDescriptor.editor.style"></Editor>
                </TabPanel>
            </TabView>

            <div v-show="metadata.file.length > 0">
                <h2>{{ $t('common.attachments') }}</h2>
                <div class="p-grid p-ai-center">
                    <template v-for="(meta, index) in metadata.file" :key="index">
                        <div class="p-col-6">
                            <label class="kn-material-input-label">{{ meta.name }}</label>
                            <KnInputFile :ref="'' + meta.id" :id="meta.id" :changeFunction="uploadFile" :visibility="true" />
                        </div>
                        <div class="p-col-6 p-d-flex p-flex-row p-jc-around">
                            <Button v-if="canModify" class="kn-button kn-button--primary document-execution-metadata-dialog-upload-button" @click="uploadMetaFile(meta)"> {{ $t('common.upload') }}</Button>
                            <Button v-if="canModify" class="kn-button kn-button--primary document-execution-metadata-dialog-upload-button" @click="cleanFile(meta)"> {{ $t('common.clean') }}</Button>
                        </div>
                    </template>
                </div>
            </div>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.close') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMetadata } from '../../DocumentExecution'
import Dialog from 'primevue/dialog'
import Editor from 'primevue/editor'
import documentExecutionMetadataDialogDescriptor from './DocumentExecutionMetadataDialogDescriptor.json'
import KnInputFile from '@/components/UI/KnInputFile.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Textarea from 'primevue/textarea'

export default defineComponent({
    name: 'document-execution-metadata-dialog',
    components: { Dialog, Editor, KnInputFile, TabView, TabPanel, Textarea },
    props: { visible: { type: Boolean }, propDocument: { type: Object }, propMetadata: { type: Object, required: true }, propLoading: { type: Boolean } },
    emits: ['close', 'saveMetadata'],
    data() {
        return {
            documentExecutionMetadataDialogDescriptor,
            document: null as any,
            metadata: null as iMetadata | null,
            uploadedFiles: {} as any,
            loading: false
        }
    },
    watch: {
        propMetadata() {
            this.loadDocument()
            this.loadMetadata()
        },
        propLoading() {
            this.setLoading()
        }
    },
    computed: {
        canModify(): boolean {
            return (this.$store.state as any).user.functionalities.includes('SaveMetadataFunctionality')
        }
    },
    created() {
        this.setLoading()
        this.loadDocument()
        this.loadMetadata()
    },
    methods: {
        loadDocument() {
            this.document = this.propDocument
        },
        loadMetadata() {
            if (this.propMetadata) {
                this.metadata = {
                    generalMetadata: this.propMetadata.GENERAL_META ? [...this.propMetadata.GENERAL_META] : [],
                    shortText: this.propMetadata.SHORT_TEXT ? [...this.propMetadata.SHORT_TEXT] : [],
                    longText: this.propMetadata.LONG_TEXT ? [...this.propMetadata.LONG_TEXT] : [],
                    file: this.propMetadata.FILE ? [...this.propMetadata.FILE] : []
                }
            }
        },
        setLoading() {
            this.loading = this.propLoading
        },
        uploadFile(event: any) {
            this.uploadedFiles[event.target.id] = event.target.files[0]
        },
        async uploadMetaFile(meta: any) {
            if (!this.uploadedFiles[meta.id]) {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.$t('documentExecution.main.selectFileError')
                })
            } else {
                this.loading = true

                const formData = new FormData()
                formData.append('file', this.uploadedFiles[meta.id])
                formData.append('fileName', this.uploadedFiles[meta.id].name)
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/uploadfilemetadata`, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
                    .then(() => {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.uploadFile'),
                            msg: this.$t('common.uploadFileSuccess')
                        })
                    })
                    .catch((error: any) =>
                        this.$store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: error
                        })
                    )
                this.loading = false
            }
        },
        cleanFile(meta: any) {
            const temp = this.$refs[meta.id] as any
            if (temp) {
                temp.resetInput()
            }
        },
        closeDialog() {
            this.metadata = null
            this.$emit('close')
        },
        save() {
            this.$emit('saveMetadata', this.metadata)
        }
    }
})
</script>

<style lang="scss">
.pi-upload {
    display: none;
}

.document-execution-metadata-dialog-upload-button {
    width: auto !important;
}
</style>
