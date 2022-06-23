<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ title }}
                </template>
                <template #end>
                    <Button class="kn-button p-button-text" @click="$emit('close')">{{ $t('common.close') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row">
                <DataTable
                    :value="items"
                    id="link-table"
                    class="p-datatable-sm kn-table p-mr-3"
                    v-model:selection="selectedItem"
                    selectionMode="single"
                    v-model:expandedRows="expandedRows"
                    :loading="loading"
                    dataKey="id"
                    v-model:filters="filters"
                    :globalFilterFields="glossaryUsageLinkCardDescriptor.globalFilterFields"
                    :paginator="true"
                    :rows="20"
                    responsiveLayout="stack"
                    breakpoint="960px"
                    @rowSelect="onRowExpand"
                    @rowExpand="onRowExpand"
                >
                    <template #header>
                        <div class="table-header p-d-flex p-ai-center">
                            <span id="search-container" class="p-input-icon-left p-mr-3">
                                <i class="pi pi-search" />
                                <InputText class="kn-material-input" v-model="filters['global'].value" type="text" :placeholder="$t('common.search')" />
                            </span>
                        </div>
                    </template>
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #loading> {{ $t('common.info.dataLoading') }}</template>
                    <template #expansion="slotProps">
                        <div :style="glossaryUsageLinkCardDescriptor.dropZoneStyle" @drop="onDragDrop($event, slotProps.data)" @dragover.prevent @dragenter.prevent>
                            <Chip class="p-m-2" v-for="word in associatedWords[slotProps.data.id]" :key="word.WORD_ID" :label="word.WORD">
                                <span>{{ word.WORD }}</span>
                                <i class="pi pi-times-circle chip-icon p-ml-3" @click="deleteWordConfirm(word.WORD_ID, slotProps.data)" />
                            </Chip>
                        </div>
                    </template>
                    <Column :expander="true" :headerStyle="glossaryUsageLinkCardDescriptor.expanderHeaderStyle" />
                    <Column class="kn-truncated" v-for="col of glossaryUsageLinkCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
                    <Column v-if="showModelColumn" class="kn-truncated" field="model" :header="'model'" :sortable="true"></Column>
                </DataTable>
                <div class="kn-flex" v-if="selectedItem && selectedItem.id && selectedItem.itemType !== 'document'">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t('managers.glossary.glossaryUsage.column') }}
                        </template>
                    </Toolbar>
                    <GlossaryUsageLinkTree :treeWords="associatedWordsTree[selectedItem.id]" @delete="deleteTreeWord" @wordDropped="onDragDrop($event.event, $event.item)"></GlossaryUsageLinkTree>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { iLinkTableItem, iWord } from '../GlossaryUsage'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import glossaryUsageLinkCardDescriptor from './GlossaryUsageLinkCardDescriptor.json'
import GlossaryUsageLinkTree from './GlossaryUsageLinkTree.vue'

export default defineComponent({
    name: 'glossary-usage-link-card',
    components: { Card, Chip, Column, DataTable, GlossaryUsageLinkTree },
    props: {
        title: { type: String },
        items: { type: Array },
        showModelColumn: { type: Boolean },
        words: { type: Object },
        treeWords: { type: Object }
    },
    emits: ['selected'],
    data() {
        return {
            glossaryUsageLinkCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedItem: null as iLinkTableItem | null,
            associatedWords: {} as any,
            expandedRows: [] as iLinkTableItem[],
            associatedWordsTree: {} as any,
            loading: false
        }
    },
    watch: {
        words: {
            handler() {
                this.loadAssociatedWords()
            },
            deep: true
        },
        treeWords: {
            handler() {
                this.loadAssociatedWordsTree()
            },
            deep: true
        }
    },
    created() {
        this.loadAssociatedWords()
        this.loadAssociatedWordsTree()
    },
    methods: {
        loadAssociatedWords() {
            this.associatedWords = { ...this.words } as any
        },
        loadAssociatedWordsTree() {
            this.associatedWordsTree = { ...this.treeWords } as any
        },
        async onDragDrop(event: any, item: any) {
            switch (item.itemType) {
                case 'document':
                    await this.addAssociatedWordDocument(item, JSON.parse(event.dataTransfer.getData('text/plain')))
                    break
                case 'dataset':
                    await this.addAssociatedWordDataset(item, JSON.parse(event.dataTransfer.getData('text/plain')), '.SELF', 'array')
                    break
                case 'datasetTree':
                    await this.addAssociatedWordDataset(item, JSON.parse(event.dataTransfer.getData('text/plain')), item.data.alias, 'tree')
                    break
                case 'businessClass':
                    await this.addAssociatedWordBusinessClass(item, JSON.parse(event.dataTransfer.getData('text/plain')), '.SELF', 'array')
                    break
                case 'businessClassTree':
                    await this.addAssociatedWordBusinessClass(item, JSON.parse(event.dataTransfer.getData('text/plain')), item.label, 'tree')
                    break
                case 'table':
                    await this.addAssociatedWordTables(item, JSON.parse(event.dataTransfer.getData('text/plain')), '.SELF', 'array')
                    break
                case 'tableTree':
                    await this.addAssociatedWordTables(item, JSON.parse(event.dataTransfer.getData('text/plain')), item.label, 'tree')
            }
        },
        onRowExpand(item: any) {
            this.selectedItem = item.data
            this.$emit('selected', item.data)
        },
        async addAssociatedWord(linkItem: any, word: iWord, type: string, url: string, postData: any, itemType: string) {
            this.loading = true
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.Status !== 'NON OK') {
                        type === 'tree'
                            ? linkItem.children.push({
                                  key: word.WORD_ID,
                                  id: word.WORD_ID,
                                  label: word.WORD,
                                  children: [] as any[],
                                  data: word,
                                  style: glossaryUsageLinkCardDescriptor.node.style,
                                  leaf: true,
                                  parent: linkItem,
                                  itemType: itemType
                              })
                            : this.associatedWords[linkItem.id].push(word)
                        this.store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    } else {
                        this.store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: response.data.Message === 'sbi.glossary.word.new.name.duplicate' ? this.$t('managers.glossary.glossaryUsage.duplicateWord') : response.data.Message
                        })
                    }
                })
                .catch(() => {})
                .finally(() => (this.loading = false))
        },
        async addAssociatedWordDocument(document: any, word: iWord) {
            const postData = { DOCUMENT_ID: document.id, WORD_ID: word.WORD_ID }
            await this.addAssociatedWord(document, word, 'array', '1.0/glossary/addDocWlist', postData, '')
        },
        async addAssociatedWordDataset(dataset: any, word: iWord, column: string, type: string) {
            const postData = { COLUMN_NAME: column, DATASET_ID: dataset.id, ORGANIZATION: dataset.organization, WORD_ID: word.WORD_ID }
            await this.addAssociatedWord(dataset, word, type, '1.0/glossary/addDataSetWlist', postData, 'datasetTree')
        },
        async addAssociatedWordBusinessClass(businessClass: any, word: iWord, column: string, type: string) {
            const id = type === 'tree' ? businessClass.businessClassId : businessClass.id
            const postData = { COLUMN_NAME: column, META_BC_ID: id, WORD_ID: word.WORD_ID }
            await this.addAssociatedWord(businessClass, word, type, '1.0/glossary/addMetaBcWlist', postData, 'businessClassTree')
        },
        async addAssociatedWordTables(table: any, word: iWord, column: string, type: string) {
            const id = type === 'tree' ? table.metasourceId : table.id
            const postData = { COLUMN_NAME: column, META_TABLE_ID: id, WORD_ID: word.WORD_ID }
            await this.addAssociatedWord(table, word, type, '1.0/glossary/addMetaTableWlist', postData, 'tableTree')
        },
        deleteWordConfirm(wordId: number, item: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.handleDelete(wordId, item)
            })
        },
        async handleDelete(wordId: number, item: any) {
            switch (item.itemType) {
                case 'document':
                    await this.deleteDocumentWord(wordId, item)
                    break
                case 'dataset':
                    await this.deleteDatasetWord(wordId, item, '.SELF', 'array')
                    break
                case 'datasetTree':
                    await this.deleteDatasetWord(wordId, item, item.parent.data.alias, 'tree')
                    break
                case 'businessClass':
                    await this.deleteBusinessClassWord(wordId, item, '.SELF', 'array')
                    break
                case 'businessClassTree':
                    await this.deleteBusinessClassWord(wordId, item, item.parent.label, 'tree')
                    break
                case 'table':
                    await this.deleteTablesWord(wordId, item, '.SELF', 'array')
                    break
                case 'tableTree':
                    await this.deleteTablesWord(wordId, item, item.parent.label, 'tree')
            }
        },
        async deleteWord(linkItem: any, wordId: number, type: string, url: string, method: string) {
            this.loading = true
            await this.$http[method](import.meta.env.VITE_RESTFUL_SERVICES_PATH + url)
                .then(() => {
                    type === 'tree' ? this.removeWordFromTreeWords(wordId, linkItem.parent) : this.removeWordFromAssociatedWords(wordId, linkItem.id)
                    this.store.setInfo({
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                })
                .catch((response: AxiosResponse<any>) => {
                    this.store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async deleteTablesWord(wordId: number, table: any, column: string, type: string) {
            const id = type === 'tree' ? table.parent.metasourceId : table.id
            const url = `1.0/glossary/deleteMetaTableWlist?WORD_ID=${wordId}&TABLE_ID=${id}&COLUMN=${column}`
            await this.deleteWord(table, wordId, type, url, 'delete')
        },
        async deleteBusinessClassWord(wordId: number, businessClass: any, column: string, type: string) {
            const id = type === 'tree' ? businessClass.parent.businessClassId : businessClass.id
            const url = `1.0/glossary/deleteMetaBcWlist?WORD_ID=${wordId}&BC_ID=${id}&COLUMN=${column}`
            await this.deleteWord(businessClass, wordId, type, url, 'delete')
        },
        async deleteDatasetWord(wordId: number, dataset: any, column: string, type: string) {
            const url = `1.0/glossary/deleteDatasetWlist?WORD_ID=${wordId}&DATASET_ID=${dataset.datasetId}&ORGANIZATION=${dataset.organization}&COLUMN=${column}`
            await this.deleteWord(dataset, wordId, type, url, 'post')
        },
        async deleteDocumentWord(wordId: number, document: any) {
            const url = `1.0/glossary/deleteDocWlist?WORD_ID=${wordId}&DOCUMENT_ID=${document.id}`
            await this.deleteWord(document, wordId, 'array', url, 'post')
        },
        removeWordFromAssociatedWords(wordId: number, documentId: number) {
            const index = this.associatedWords[documentId].findIndex((el: any) => el.WORD_ID === wordId)
            this.associatedWords[documentId].splice(index, 1)
        },
        removeWordFromTreeWords(wordId: number, parent: any) {
            const index = parent.children.findIndex((el: any) => el.id === wordId)
            parent.children.splice(index, 1)
        },
        deleteTreeWord(word: any) {
            this.handleDelete(word.id, word)
        }
    }
})
</script>

<style lang="scss" scoped>
#link-table {
    flex: 2;
}

.chip-icon {
    font-size: 0.9rem;
}
</style>
