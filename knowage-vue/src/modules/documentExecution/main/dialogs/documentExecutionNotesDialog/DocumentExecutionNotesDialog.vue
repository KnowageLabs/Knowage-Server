<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionNotesDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('common.notes') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <TabView v-model:activeIndex="activeTab">
            <TabPanel>
                <template #header>
                    <span>{{ $t('common.note') }}</span>
                </template>

                <DocumentExecutionNotesForm :selectedNote="selectedNote"></DocumentExecutionNotesForm>
            </TabPanel>
            <!-- TODO: Notes list will be implemented when backend services are fixed. -->
            <!-- <TabPanel>
                <template #header>
                    <span>{{ $t('common.notesList') }}</span>
                </template>

                <DocumentExecutionNotesList :propNotes="notes" :document="document" @editNote="onEditNote" @deleteNote="onDeleteNote"></DocumentExecutionNotesList>
            </TabPanel> -->
        </TabView>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button v-if="activeTab === 1" class="kn-button kn-button--primary" @click="exportNotes('pdf')"> {{ $t('documentExecution.main.exportInPDF') }}</Button>
                <Button v-if="activeTab === 1" class="kn-button kn-button--primary" @click="exportNotes('rtf')"> {{ $t('documentExecution.main.exportInRTF') }}</Button>

                <Button class="kn-button kn-button--primary p-ml-auto" @click="closeDialog"> {{ $t('common.close') }}</Button>
                <Button v-if="activeTab === 0" class="kn-button kn-button--primary" @click="saveNote" :disabled="saveButtonDisabled"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { downloadDirect } from '@/helpers/commons/fileHelper'
import Dialog from 'primevue/dialog'
import documentExecutionNotesDialogDescriptor from './DocumentExecutionNotesDialogDescriptor.json'
import DocumentExecutionNotesForm from './DocumentExecutionNotesForm.vue'
// TODO: Notes list will be implemented when backend services are fixed.
// import DocumentExecutionNotesList from './DocumentExecutionNotesList.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'document-execution-notes-dialog',
    components: { Dialog, DocumentExecutionNotesForm, TabView, TabPanel },
    props: { visible: { type: Boolean }, propDocument: { type: Object } },
    emits: ['close'],
    data() {
        return {
            documentExecutionNotesDialogDescriptor,
            document: null as any,
            notes: [] as any,
            selectedNote: {} as any,
            activeTab: 0,
            loading: false
        }
    },
    watch: {
        async visible() {
            if (this.visible) {
                await this.loadDocument()
            }
        },
        async propDocument() {
            await this.loadDocument()
        }
    },
    computed: {
        saveButtonDisabled(): boolean {
            return !this.selectedNote.type
        }
    },
    async created() {
        await this.loadDocument()
    },
    methods: {
        async loadDocument() {
            this.document = this.propDocument

            if (this.document) {
                await this.loadNotes()
            }
        },
        closeDialog() {
            this.selectedNote = {}
            this.$emit('close')
        },
        async loadNotes() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentnotes/getListNotes`, { idObj: this.document.id })
                .then((response: AxiosResponse<any>) => (this.notes = response.data))
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false

            if (this.notes.length > 0) {
                this.selectedNote = this.notes[0]
            }
            console.log('LOADED NOTES: ', this.notes)
        },
        async saveNote() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentnotes/saveNote`, { type: this.selectedNote.type, content: this.selectedNote.content, idObj: this.document.id })
                .then(async () => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    await this.loadNotes()
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        },
        onEditNote(note: any) {
            this.selectedNote = { ...note }
            this.activeTab = 0
        },
        async onDeleteNote(note: any) {
            this.loading = true
            console.log('NOTE FOR DELETE: ', note)
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentnotes/deleteNote`, { id: this.document.id, execReq: note.execReq, owner: note.owner })
                .then(async () => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    await this.loadNotes()
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        },
        async exportNotes(type: string) {
            this.loading = true
            await this.$http
                .post(
                    process.env.VUE_APP_RESTFUL_SERVICES_PATH + `documentnotes/getDownalNote`,
                    { idObj: this.document.id, type: type },
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*'
                        }
                    }
                )
                .then((response: AxiosResponse<any>) => {
                    const byteArray = new Uint8Array(response.data.file)
                    console.log('RESPONSE HEADERS: ', response.headers)
                    // TODO - ask for rtf
                    downloadDirect(byteArray, this.document.label, type === 'pdf' ? 'application/pdf' : 'application/rtf')
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        }
    }
})
</script>
