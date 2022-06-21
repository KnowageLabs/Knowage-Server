<template>
    <div>
        <Message v-if="notes.length === 0" class="p-m-2" severity="info" :closable="false" :style="documentExecutionNotesDialogDescriptor.messageStyle">
            {{ $t('common.info.noDataFound') }}
        </Message>
        <template v-else>
            <div v-for="note in notes" :key="note.id">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        <span class="p-ml-4">{{ $t('common.owner') + ' ' + note.owner }}</span>
                    </template>
                    <template #end>
                        <span class="p-mr-4">{{ $t('common.creationDate') + ': ' + getFormattedDate(note.creationDate) }}</span>
                        <span class="p-mr-4">{{ $t('common.lastChangeDate') + ': ' + getFormattedDate(note.lastChangeDate) }}</span>
                    </template>
                </Toolbar>

                <div class="p-m-2">
                    <Editor v-model="note.content" :readonly="true"></Editor>
                </div>

                <div class="p-d-flex p-flex-row p-jc-end p-m-2">
                    <Button icon="pi pi-pencil" class="p-button-link" @click="onNoteEdit(note)" />
                    <Button icon="pi pi-trash" class="p-button-link" @click="deleteNoteConfirm(note)" />
                </div>
            </div>
        </template>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/commons/localeHelper'
import { iNote } from '../../DocumentExecution'
import documentExecutionNotesDialogDescriptor from './DocumentExecutionNotesDialogDescriptor.json'
import Editor from 'primevue/editor'
import Message from 'primevue/message'
import moment from 'moment'

import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'document-execution-notes-list',
    components: { Editor, Message },
    props: { propNotes: { type: Array }, document: { type: Object } },
    emits: ['editNote', 'deleteNote'],
    data() {
        return {
            documentExecutionNotesDialogDescriptor,
            notes: [] as any[]
        }
    },
    watch: {
        propNotes() {
            this.loadNotes()
        }
    },
    created() {
        this.loadNotes()
    },
    methods: {
        loadNotes() {
            this.notes = this.propNotes as any[]
        },
        deleteNoteConfirm(note: iNote) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteNote', note)
            })
        },
        getFormattedDate(date: number) {
            const tempDate = moment(date).format('DD/MM/YYYY')
            return formatDate(tempDate, '', 'DD/MM/YYYY')
        },
        onNoteEdit(note: iNote) {
            this.$emit('editNote', deepcopy(note))
        }
    }
})
</script>
