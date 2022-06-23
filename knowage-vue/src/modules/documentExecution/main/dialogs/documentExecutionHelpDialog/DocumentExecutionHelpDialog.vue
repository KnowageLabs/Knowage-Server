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

        <div class="p-d-flex p-flex-row kn-height-full">
            <Listbox class="kn-list--column kn-flex kn-height-full" :options="words" :filter="true" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="documentExecutionHelpDialogDescriptor.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" @click="loadWordDetail(slotProps.option)">
                        <span>{{ slotProps.option.WORD }}</span>
                    </div>
                </template>
            </Listbox>
            <DocumentExecutionWordDetail id="document-execution-word-detail" :wordDetail="wordDetail" :selectedWordName="selectedWordName"></DocumentExecutionWordDetail>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import documentExecutionHelpDialogDescriptor from './DocumentExecutionHelpDialogDescriptor.json'
import DocumentExecutionWordDetail from './DocumentExecutionWordDetail.vue'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'document-execution-help-dialog',
    components: { Dialog, DocumentExecutionWordDetail, Listbox },
    props: { visible: { type: Boolean }, propDocument: { type: Object } },
    emits: ['close'],
    data() {
        return {
            documentExecutionHelpDialogDescriptor,
            document: null as any,
            words: [] as { WORD_ID: number; WORD: string }[],
            wordDetail: null as any,
            selectedWordName: '' as string
        }
    },
    computed: {},
    watch: {
        async propDocument() {
            await this.loadDocument()
        }
    },
    async created() {
        await this.loadDocument()
    },
    methods: {
        async loadDocument() {
            this.document = this.propDocument ? { ...this.propDocument } : {}
            if (this.document.id) await this.loadDocumentWords()
        },
        async loadDocumentWords() {
            this.$store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getDocumentInfo?DOCUMENT_ID=${this.document.id}`)
                .then((response: AxiosResponse<any>) => (this.words = response.data.word))
                .finally(() => this.$store.setLoading(false))
        },
        async loadWordDetail(word: { WORD_ID: number; WORD: string }) {
            this.$store.setLoading(true)
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getWord?WORD_ID=${word?.WORD_ID}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedWordName = word.WORD
                    this.wordDetail = response.data
                })
                .finally(() => this.$store.setLoading(false))
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>

<style lang="scss" scoped>
#document-execution-word-detail {
    flex: 2;
}
</style>
