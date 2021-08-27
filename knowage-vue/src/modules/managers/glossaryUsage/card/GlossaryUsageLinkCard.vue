<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ title }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" @click="$emit('close')">{{ $t('common.close') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div class="p-d-flex p-flex-row">
                <DataTable
                    :value="items"
                    class="p-datatable-sm kn-table p-col-9"
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
                            <Chip class="p-m-2" v-for="word in associatedWords[slotProps.data.id]" :key="word.WORD_ID" :label="word.WORD" @click="deleteWordConfirm(word.WORD_ID, slotProps.data)" />
                        </div>
                    </template>
                    <Column :expander="true" :headerStyle="glossaryUsageLinkCardDescriptor.expanderHeaderStyle" />
                    <Column class="kn-truncated" v-for="col of glossaryUsageLinkCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
                </DataTable>
                <!-- <div v-if="selectedItem && selectedItem.id">{{ treeWords[selectedItem.id] }}</div> -->
                <GlossaryUsageLinkTree v-if="selectedItem && selectedItem.id" class="p-col-3" :treeWords="associatedWordsTree[selectedItem.id]" @delete="deleteTreeWord" @wordDropped="onDragDrop($event.event, $event.item)"></GlossaryUsageLinkTree>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import axios from 'axios'
import Card from 'primevue/card'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import glossaryUsageLinkCardDescriptor from './GlossaryUsageLinkCardDescriptor.json'
import GlossaryUsageLinkTree from './GlossaryUsageLinkTree.vue'

// TODO dodati style kod dodatih nodova

export default defineComponent({
    name: 'glossary-usage-link-card',
    components: { Card, Chip, Column, DataTable, GlossaryUsageLinkTree },
    props: {
        title: { type: String },
        items: { type: Array },
        words: { type: Object },
        treeWords: { type: Object }
    },
    emits: ['selected'],
    data() {
        return {
            glossaryUsageLinkCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedItem: null,
            associatedWords: {} as any,
            expandedRows: [] as any[],
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
            console.log('ON DRAG DROP: ', JSON.parse(event.dataTransfer.getData('text/plain')))
            console.log('ON DRAG DROP LINK ITEM: ', item)
            switch (item.itemType) {
                case 'document':
                    await this.addAssociatedWordDocument(item.id, JSON.parse(event.dataTransfer.getData('text/plain')))
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
                    await this.addAssociatedWordTables(item.id, JSON.parse(event.dataTransfer.getData('text/plain')))
                    break
            }
        },
        onRowExpand(item: any) {
            this.selectedItem = item.data
            this.$emit('selected', item.data)
        },
        async addAssociatedWordDocument(documentId: number, word: any) {
            this.loading = true
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/addDocWlist', { DOCUMENT_ID: documentId, WORD_ID: word.WORD_ID })
                .then((response) => {
                    if (response.data.Status !== 'NON OK') {
                        this.associatedWords[documentId].push(word)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async addAssociatedWordDataset(dataset: any, word: any, column: string, type: string) {
            console.log('DATASET FOR ADD WORD: ', dataset)
            console.log('WORD FOR ADD WORD: ', word)
            this.loading = true
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/addDataSetWlist', {
                    COLUMN_NAME: column,
                    DATASET_ID: dataset.id,
                    ORGANIZATION: dataset.organization,
                    WORD_ID: word.WORD_ID
                })
                .then((response) => {
                    if (!response.data.errors) {
                        type === 'tree'
                            ? dataset.children.push({
                                  key: word.WORD_ID,
                                  id: word.WORD_ID,
                                  label: word.WORD,
                                  children: [] as any[],
                                  data: word,
                                  style: '',
                                  leaf: true,
                                  parent: dataset,
                                  itemType: 'datasetTree'
                              })
                            : this.associatedWords[dataset.id].push(word)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async addAssociatedWordBusinessClass(businessClass: any, word: any, column: string, type: string) {
            console.log('BUSINESS CLASS FOR ADD WORD: ', businessClass)
            console.log('WORD FOR ADD WORD: ', word)
            this.loading = true
            const id = type === 'tree' ? businessClass.businessClassId : businessClass.id
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/addMetaBcWlist', {
                    COLUMN_NAME: column,
                    META_BC_ID: id,
                    WORD_ID: word.WORD_ID
                })
                .then((response) => {
                    if (!response.data.errors) {
                        type === 'tree'
                            ? businessClass.children.push({
                                  key: word.WORD_ID,
                                  id: word.WORD_ID,
                                  label: word.WORD,
                                  children: [] as any[],
                                  data: word,
                                  style: '',
                                  leaf: true,
                                  parent: businessClass,
                                  itemType: 'businessClassTree'
                              })
                            : this.associatedWords[businessClass.id].push(word)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async addAssociatedWordTables(tableId: number, word: any) {
            console.log('TABLE FOR ADD WORD: ', tableId)
            console.log('WORD FOR ADD WORD: ', word)
            this.loading = true
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/addMetaTableWlist', {
                    COLUMN_NAME: '.SELF',
                    META_TABLE_ID: tableId,
                    WORD_ID: word.WORD_ID
                })
                .then((response) => {
                    if (!response.data.errors) {
                        this.associatedWords[tableId].push(word)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
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
            console.log('ITEM FOR DELETE: ', item)
            switch (item.itemType) {
                case 'document':
                    await this.deleteDocumentWord(wordId, item.id)
                    break
                case 'dataset':
                    await this.deleteDatasetWord(wordId, item, '.SELF', 'array')
                    break
                case 'datasetTree':
                    await this.deleteDatasetWord(wordId, item.parent, item.parent.data.alias, 'tree')
                    break
                case 'businessClass':
                    await this.deleteBusinessClassWord(wordId, item, '.SELF', 'array')
                    break
                case 'businessClassTree':
                    await this.deleteBusinessClassWord(wordId, item, item.parent.label, 'tree')
                    break
                case 'table':
                    await this.deleteTablesWord(wordId, item.id)
            }
        },
        async deleteDocumentWord(wordId: number, documentId: number) {
            console.log('WORD ID FOR DELETE: ', wordId)
            console.log('DOCUMENT ID FOR DELETE: ', documentId)
            this.loading = true
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/deleteDocWlist?WORD_ID=${wordId}&DOCUMENT_ID=${documentId}`, {})
                .then((response) => {
                    if (response.data.Status === 'OK') {
                        this.removeWordFromAssociatedWords(wordId, documentId)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async deleteDatasetWord(wordId: number, dataset: any, column: string, type: string) {
            console.log('WORD ID FOR DELETE: ', wordId)
            console.log('DATASET FOR DELETE: ', dataset)
            this.loading = true
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/deleteDatasetWlist?WORD_ID=${wordId}&DATASET_ID=${dataset.id}&ORGANIZATION=${dataset.organization}&COLUMN=${column}`, {})
                .then((response) => {
                    if (response.data.Status === 'OK') {
                        type === 'tree' ? this.removeWordFromTreeWords(wordId, dataset) : this.removeWordFromAssociatedWords(wordId, dataset.id)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async deleteBusinessClassWord(wordId: number, businessClass: any, column: string, type: string) {
            console.log('WORD ID FOR DELETE: ', wordId)

            this.loading = true
            const id = type === 'tree' ? businessClass.parent.businessClassId : businessClass.id
            console.log('BUSINESS CLASS ID FOR DELETE: ', businessClass.id)
            await axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/deleteMetaBcWlist?WORD_ID=${wordId}&BC_ID=${id}&COLUMN=${column}`)
                .then((response) => {
                    if (!response.data.errors) {
                        type === 'tree' ? this.removeWordFromTreeWords(wordId, businessClass.parent) : this.removeWordFromAssociatedWords(wordId, businessClass.id)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async deleteTablesWord(wordId: number, tableId: number) {
            console.log('WORD ID FOR DELETE: ', wordId)
            console.log('TABLE ID FOR DELETE: ', tableId)
            this.loading = true
            await axios
                .delete(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/deleteMetaTableWlist?WORD_ID=${wordId}&TABLE_ID=${tableId}&COLUMN=.SELF`)
                .then((response) => {
                    if (!response.data.errors) {
                        this.removeWordFromAssociatedWords(wordId, tableId)
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        removeWordFromAssociatedWords(wordId: number, documentId: number) {
            // console.log('ASSOS WORDS', this.associatedWords)
            // console.log('ASSOS WORDS DOC ID', documentId)
            // console.log('TEST', this.associatedWords[documentId])
            const index = this.associatedWords[documentId].findIndex((el: any) => el.WORD_ID === wordId)
            this.associatedWords[documentId].splice(index, 1)
        },
        removeWordFromTreeWords(wordId: number, parent: any) {
            console.log('TREE DELETE - WORD ID: ', wordId)
            console.log('TREE DELETE - PARENT: ', parent)
            const index = parent.children.findIndex((el: any) => el.id === wordId)
            parent.children.splice(index, 1)
        },
        deleteTreeWord(word: any) {
            this.handleDelete(word.id, word)
        }
    }
})
</script>
