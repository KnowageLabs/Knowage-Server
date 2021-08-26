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
            {{ associatedWords }}
            <DataTable
                :value="items"
                class="p-datatable-sm kn-table"
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
                @rowSelect="onRowSelect($event.data)"
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
                        <Chip class="p-m-2" v-for="word in associatedWords[slotProps.data.id]" :key="word.WORD_ID" :label="word.WORD" @click="deleteWordConfirm(word.WORD_ID, slotProps.data.id)" />
                    </div>
                </template>
                <Column :expander="true" :headerStyle="glossaryUsageLinkCardDescriptor.expanderHeaderStyle" />
                <Column class="kn-truncated" v-for="col of glossaryUsageLinkCardDescriptor.columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true"></Column>
            </DataTable>
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

export default defineComponent({
    name: 'glossary-usage-link-card',
    components: { Card, Chip, Column, DataTable },
    props: { title: { type: String }, items: { type: Array }, words: { type: Object } },
    emits: ['selected'],
    data() {
        return {
            glossaryUsageLinkCardDescriptor,
            filters: { global: [filterDefault] } as Object,
            selectedItem: null,
            associatedWords: {} as any,
            expandedRows: [] as any[],
            loading: false
        }
    },
    watch: {
        words: {
            handler() {
                this.loadAssociatedWords()
            },
            deep: true
        }
    },
    created() {
        this.loadAssociatedWords()
    },
    methods: {
        loadAssociatedWords() {
            this.associatedWords = { ...this.words } as any
        },
        async onDragDrop(event: any, item: any) {
            console.log('ON DRAG DROP: ', JSON.parse(event.dataTransfer.getData('text/plain')))
            console.log('ON DRAG DROP LINK ITEM: ', item)
            switch (item.itemType) {
                case 'document':
                    await this.addAssociatedWord(item.id, JSON.parse(event.dataTransfer.getData('text/plain')))
                    break
            }
        },
        onRowExpand(item: any) {
            this.selectedItem = item.data
            this.$emit('selected', item.data)
        },
        async addAssociatedWord(documentId: number, word: any) {
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
        deleteWordConfirm(wordId: number, documentId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteWord(wordId, documentId)
            })
        },
        async deleteWord(wordId: number, documentId: number) {
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
        removeWordFromAssociatedWords(wordId: number, documentId: number) {
            // console.log('ASSOS WORDS', this.associatedWords)
            // console.log('ASSOS WORDS DOC ID', documentId)
            // console.log('TEST', this.associatedWords[documentId])
            const index = this.associatedWords[documentId].findIndex((el: any) => el.WORD_ID === wordId)
            this.associatedWords[documentId].splice(index, 1)
        }
    }
})
</script>
