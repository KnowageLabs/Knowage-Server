<template>
    <div>
        <div v-for="note in notes" :key="note.id">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left
                    ><span>{{ $t('common.owner') + ' ' + document.owner }}</span>
                    <span>{{ $t('common.creationDate') + ' ' + note.creationDate }}</span>
                    <span>{{ $t('common.lastChangeDate') + ' ' + note.lastChangeDate }}</span>
                </template>
            </Toolbar>

            <div class="p-m-2">
                <Editor v-model="note.content" :readonly="true"></Editor>
            </div>

            <div class="p-d-flex p-flex-row p-jc-end p-m-2">
                <Button icon="pi pi-pencil" class="p-button-link" @click="$emit('editNote', note)" />
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteNoteConfirm(note)" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Editor from 'primevue/editor'

export default defineComponent({
    name: 'document-execution-notes-list',
    components: { Editor },
    props: { propNotes: { type: Array }, document: { type: Object } },
    emits: ['editNote', 'deleteNote'],
    data() {
        return {
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
        deleteNoteConfirm(note: any) {
            this.$confirm.require({
                message: this.$t('documentExecution.dossier.deleteConfirm'),
                header: this.$t('documentExecution.dossier.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleteNote', note)
            })
        }
    }
})
</script>
