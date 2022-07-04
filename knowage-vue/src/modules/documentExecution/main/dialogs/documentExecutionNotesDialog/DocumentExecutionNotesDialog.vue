<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="documentExecutionNotesDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.notes') }}
                </template>
                <template #end>
                    <FabButton icon="fas fa-plus" @click="createNewNote" />
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

            <TabPanel>
                <template #header>
                    <span>{{ $t('common.notesList') }}</span>
                </template>

                <DocumentExecutionNotesList :propNotes="notes" :document="document" @editNote="onEditNote" @deleteNote="onDeleteNote"></DocumentExecutionNotesList>
            </TabPanel>
        </TabView>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button v-if="activeTab === 1" class="kn-button kn-button--primary" @click="exportNotes('pdf')" :disabled="exportButtonDisabled"> {{ $t('documentExecution.main.exportInPDF') }}</Button>
                <Button v-if="activeTab === 1" class="kn-button kn-button--primary" @click="exportNotes('rtf')" :disabled="exportButtonDisabled"> {{ $t('documentExecution.main.exportInRTF') }}</Button>

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
import { iNote } from '../../DocumentExecution'
import Dialog from 'primevue/dialog'
import documentExecutionNotesDialogDescriptor from './DocumentExecutionNotesDialogDescriptor.json'
import DocumentExecutionNotesForm from './DocumentExecutionNotesForm.vue'
import DocumentExecutionNotesList from './DocumentExecutionNotesList.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import { mapState } from 'vuex'

export default defineComponent({
    name: 'document-execution-notes-dialog',
    components: { Dialog, DocumentExecutionNotesForm, DocumentExecutionNotesList, FabButton, TabView, TabPanel },
    props: { visible: { type: Boolean }, propDocument: { type: Object } },
    emits: ['close'],
    data() {
        return {
            documentExecutionNotesDialogDescriptor,
            document: null as any,
            notes: [] as iNote[],
            selectedNote: {} as iNote,
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
        },
        exportButtonDisabled(): boolean {
            return this.notes.length === 0
        },
        ...mapState({
            isEnterprise: 'isEnterprise'
        })
    },
    async mounted() {
        await this.loadDocument()
    },
    methods: {
        async loadDocument() {
            this.document = this.propDocument

            if (this.isEnterprise && this.document && this.document.id) {
                await this.loadNotes()
            }
        },
        closeDialog() {
            this.selectedNote = {} as iNote
            this.$emit('close')
        },
        async loadNotes() {
            this.loading = true
            this.notes = []
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `document-notes/${this.document.id}`)
                .then((response: AxiosResponse<any>) => {
                    this.notes = response.data?.map((note: iNote) => {
                        return { ...note, type: note.public ? 'Public' : 'Private' }
                    })
                })
                .catch((error: any) =>
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: error
                    })
                )
            this.loading = false
        },
        async saveNote() {
            this.loading = true
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `document-notes/${this.document.id}`, { public: this.selectedNote.type === 'Public', content: this.selectedNote.content, id: this.selectedNote.id }, { headers: { 'X-Disable-Errors': 'true' } })
                .then(async (response: AxiosResponse<any>) => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.selectedNote = { ...response.data, type: response.data.public ? 'Public' : 'Private' }
                    await this.loadNotes()
                })
                .catch((error: any) => {
                    const errorMessage = this.createSeaveErrorMessage(error.response.data)
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: errorMessage
                    })
                })
            this.loading = false
        },
        createSeaveErrorMessage(error: any) {
            const properties = ['classViolations', 'fieldViolations', 'parameterViolations', 'propertyViolations', 'returnValueViolations']
            let errorMessage = ''
            properties.forEach((prop: string) => {
                for (let i = 0; i < error[prop].length; i++) {
                    if (error[prop][i].message) errorMessage += error[prop][i].message + '\n\n'
                }
            })
            return errorMessage
        },
        onEditNote(note: iNote) {
            this.selectedNote = { ...note }
            this.activeTab = 0
        },
        async onDeleteNote(note: iNote) {
            this.loading = true
            await this.$http
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `document-notes/${this.document.id}/${note.id}`)
                .then(async () => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })

                    if (this.selectedNote.id === note.id) this.selectedNote = {} as iNote
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
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `document-notes/${this.document.id}/download/${type}`, {
                    headers: {
                        Accept: 'application/json, text/plain, */*'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    const byteArray = new Uint8Array(response.data.file)
                    downloadDirect(byteArray, this.document.label, type === 'pdf' ? 'application/pdf' : 'application/rtf')
                })
                .catch(() => {})
            this.loading = false
        },
        createNewNote() {
            this.selectedNote = {} as iNote
        }
    }
})
</script>
