<template>
    <div>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <div class="p-grid p-m-0" v-if="!linkTableVisible">
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :type="'document'" :items="documents" :glossaryChanged="glossaryChanged" @infoClicked="showDocumentInfo($event)" @linkClicked="onLinkClicked($event)" @selected="onDocumentsSelected"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :type="'dataset'" :items="datasets" :glossaryChanged="glossaryChanged" @infoClicked="showDatasetInfo($event)" @linkClicked="onLinkClicked($event)" @selected="onDatasetsSelected"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :type="'businessClass'" :items="businessClasses" :glossaryChanged="glossaryChanged" @infoClicked="showBusinessClassInfo($event)" @linkClicked="onLinkClicked($event)" @selected="onBusinessClassesSelected"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :type="'table'" :items="tables" :glossaryChanged="glossaryChanged" @infoClicked="showTableInfo($event)" @linkClicked="onLinkClicked($event)" @selected="onTablesSelected"></GlossaryUsageNavigationCard>
            </div>
        </div>
        <GlossaryUsageLinkCard v-else :title="linkTableTitle" class="p-m-2" :items="linkTableItems" :words="selectedLinkItemWords" :treeWords="selectedLinkItemTree" :showModelColumn="showModelColumn" @close="onLinkTableClose" @selected="onLinkItemSelect"></GlossaryUsageLinkCard>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode, iLinkTableItem, iNavigationTableItem } from './GlossaryUsage'
import { AxiosResponse } from 'axios'
import GlossaryUsageNavigationCard from './card/GlossaryUsageNavigationCard.vue'
import GlossaryUsageLinkCard from './card/GlossaryUsageLinkCard.vue'
import glossaryUsageDescriptor from './GlossaryUsageDescriptor.json'

export default defineComponent({
    name: 'glossary-usage-detail',
    components: { GlossaryUsageNavigationCard, GlossaryUsageLinkCard },
    props: { glossaryId: { type: Number }, selectedWords: { type: Array } },
    emits: ['infoClicked', 'linkClicked', 'wordsFiltered', 'loading'],
    data() {
        return {
            glossaryUsageDescriptor,
            documents: [] as iNavigationTableItem[],
            selectedDocuments: [] as any[],
            datasets: [] as iNavigationTableItem[],
            selectedDatasets: [] as any[],
            businessClasses: [] as iNavigationTableItem[],
            selectedBusinessClasses: [] as any[],
            tables: [] as iNavigationTableItem[],
            selectedTables: [] as any[],
            linkTableVisible: false,
            linkTableTitle: '',
            linkTableItems: [] as iLinkTableItem[],
            selectedLinkItemWords: {} as any,
            selectedLinkItemTree: {} as any,
            showModelColumn: false,
            glossaryChanged: false,
            loading: false
        }
    },
    watch: {
        async glossaryId() {
            this.linkTableVisible = false
            this.resetSelected()
            await this.loadNavigationItems('all', 'word')
        },
        selectedWords: {
            async handler() {
                await this.loadNavigationItems('all', 'word')
            },
            deep: true
        }
    },
    async created() {
        await this.loadNavigationItems('all', 'word')
    },
    methods: {
        async loadNavigationItems(type: string, item: string) {
            this.loading = true
            this.$emit('loading', true)
            const postData = {
                type: type,
                item: item,
                word: {
                    selected: this.selectedWords,
                    search: '',
                    item_number: 9223372036854775807,
                    page: 1,
                    GLOSSARY_ID: this.glossaryId
                },
                document: { selected: this.selectedDocuments, search: '', item_number: 9223372036854775807, page: 1, GLOSSARY_ID: this.glossaryId },
                dataset: { selected: this.selectedDatasets, search: '', item_number: 9223372036854775807, page: 1, GLOSSARY_ID: this.glossaryId },
                table: { selected: this.selectedTables, search: '', item_number: 9223372036854775807, page: 1, GLOSSARY_ID: this.glossaryId },
                bness_cls: { selected: this.selectedBusinessClasses, search: '', item_number: 9223372036854775807, page: 1, GLOSSARY_ID: this.glossaryId }
            }
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/loadNavigationItem', postData)
                .then((response: AxiosResponse<any>) => {
                    this.formatNavigationItems(response.data)
                    if (response.data.word) {
                        this.$emit('wordsFiltered', response.data.word)
                    }
                })
                .catch((response: AxiosResponse<any>) => {
                    this.store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => {
                    this.loading = false
                    this.$emit('loading', false)
                })
        },
        formatNavigationItems(data: any) {
            if ('document' in data) {
                this.documents = []
                data.document.forEach((el: any) => this.documents.push({ id: el.DOCUMENT_ID, label: el.DOCUMENT_LABEL, type: 'document' }))
            }
            if ('dataset' in data) {
                this.datasets = []
                data.dataset.forEach((el: any) =>
                    this.datasets.push({
                        id: el.DATASET_ID,
                        label: el.DATASET_NM,
                        organization: el.DATASET_ORG,
                        type: 'dataset'
                    })
                )
            }
            if ('bness_cls' in data) {
                this.businessClasses = []
                data.bness_cls.forEach((el: any) =>
                    this.businessClasses.push({
                        id: el.BC_ID,
                        label: el.META_MODEL_NAME + '.' + el.BC_NAME,
                        type: 'businessClass'
                    })
                )
            }
            if ('table' in data) {
                this.tables = []
                data.table.forEach((el: any) =>
                    this.tables.push({
                        id: el.TABLE_ID,
                        label: el.META_SOURCE_NAME + '.' + el.TABLE_NM,
                        type: 'table'
                    })
                )
            }
        },
        async showDocumentInfo(document: iNavigationTableItem) {
            this.loading = true
            let tempDocument = null as any
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/documents/${document.label}`).then((response: AxiosResponse<any>) => (tempDocument = response.data))

            if (tempDocument) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/documents/${tempDocument.id}/roles`).then((response: AxiosResponse<any>) => (tempDocument.access = response.data))
                this.$emit('infoClicked', { data: tempDocument, type: 'document' })
            }
            this.loading = false
        },
        async showDatasetInfo(dataset: iNavigationTableItem) {
            this.loading = true
            await this.loadDatasetInfo(dataset)
                .then((response: AxiosResponse<any>) => this.$emit('infoClicked', { data: response.data, type: 'dataset' }))
                .finally(() => (this.loading = false))
        },
        async showBusinessClassInfo(businessClass: iNavigationTableItem) {
            this.loading = true
            await this.loadBusinessClassInfo(businessClass)
                .then((response: AxiosResponse<any>) => this.$emit('infoClicked', { data: response.data, type: 'businessClass' }))
                .finally(() => (this.loading = false))
        },
        async showTableInfo(table: any) {
            this.loading = true
            await this.loadTableInfo(table)
                .then((response: AxiosResponse<any>) => this.$emit('infoClicked', { data: response.data, type: 'table' }))
                .finally(() => (this.loading = false))
        },
        async onLinkClicked(type: string) {
            switch (type) {
                case 'document':
                    await this.loadDocuments()
                    break
                case 'dataset':
                    await this.loadDatasets()
                    break
                case 'businessClass':
                    await this.loadBusinessClasses()
                    break
                case 'table':
                    await this.loadTables()
            }
        },
        async loadDocuments() {
            this.linkTableItems = []
            this.showModelColumn = false
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument?Page=1&ItemPerPage=&label=&scope=GLOSSARY')
                .then((response: AxiosResponse<any>) => {
                    response.data.item.forEach((el: any) =>
                        this.linkTableItems.push({
                            id: el.DOCUMENT_ID,
                            name: el.DOCUMENT_LABEL,
                            description: el.DOCUMENT_DESCR,
                            type: '',
                            author: el.DOCUMENT_AUTH,
                            itemType: 'document'
                        })
                    )
                    this.showLinkTable(this.$t('managers.glossary.glossaryUsage.documents'))
                })
                .finally(() => (this.loading = false))
        },
        async loadDatasets() {
            this.linkTableItems = []
            this.showModelColumn = false
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/datasets/?asPagedList=true&Page=1&ItemPerPage=&label=')
                .then((response: AxiosResponse<any>) => {
                    response.data.item.forEach((el: any) => {
                        this.linkTableItems.push({
                            id: el.id.dsId,
                            datasetId: el.id.dsId,
                            name: el.label,
                            description: el.description,
                            type: el.type,
                            author: el.owner,
                            organization: el.id.organization,
                            itemType: 'dataset'
                        })
                    })
                    this.showLinkTable(this.$t('managers.glossary.glossaryUsage.dataset'))
                })
                .finally(() => (this.loading = false))
        },
        async loadBusinessClasses() {
            this.linkTableItems = []
            this.showModelColumn = true
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/metaBC/listMetaBC?Page=1&ItemPerPage=&label=')
                .then((response: AxiosResponse<any>) => {
                    response.data.forEach((el: any) =>
                        this.linkTableItems.push({
                            id: el.id,
                            name: el.name,
                            description: '',
                            type: '',
                            author: '',
                            itemType: 'businessClass',
                            model: el.model
                        })
                    )
                    this.showLinkTable(this.$t('managers.glossary.glossaryUsage.businessClass'))
                })
                .finally(() => (this.loading = false))
        },
        async loadTables() {
            this.linkTableItems = []
            this.showModelColumn = false
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/metaTable/listMetaTable?Page=1&ItemPerPage=&label=')
                .then((response: AxiosResponse<any>) => {
                    response.data.forEach((el: any) =>
                        this.linkTableItems.push({
                            id: el.tableId,
                            name: el.name,
                            description: '',
                            type: '',
                            author: '',
                            itemType: 'table'
                        })
                    )
                    this.showLinkTable(this.$t('managers.glossary.glossaryUsage.tables'))
                })
                .finally(() => (this.loading = false))
        },
        showLinkTable(title: string) {
            this.linkTableItems.sort((a: iLinkTableItem, b: iLinkTableItem) => (a.name > b.name ? 1 : -1))
            this.linkTableTitle = title
            this.linkTableVisible = true
        },
        onDocumentsSelected(documents: iNavigationTableItem[]) {
            this.selectedDocuments = []
            documents.forEach((el: iNavigationTableItem) => this.selectedDocuments.push({ DOCUMENT_ID: el.id, DOCUMENT_LABEL: el.label }))
            this.loadNavigationItems('all', 'word')
        },
        onDatasetsSelected(datasets: iNavigationTableItem[]) {
            this.selectedDatasets = []
            datasets.forEach((el: iNavigationTableItem) => this.selectedDatasets.push({ DATASET_ID: el.id, DATASET_NM: el.label, DATASET_ORG: el.organization }))
            this.loadNavigationItems('all', 'word')
        },
        onBusinessClassesSelected(businessClasses: iNavigationTableItem[]) {
            this.selectedBusinessClasses = []
            businessClasses.forEach((el: iNavigationTableItem) => {
                const label = el.label.split('.')
                this.selectedBusinessClasses.push({ BC_ID: el.id, META_MODEL_NAME: label[0], BC_NAME: label[1] })
            })
            this.loadNavigationItems('all', 'word')
        },
        onTablesSelected(tables: iNavigationTableItem[]) {
            this.selectedTables = []
            tables.forEach((el: iNavigationTableItem) => {
                const label = el.label.split('.')
                this.selectedTables.push({ TABLE_ID: el.id, META_SOURCE_NAME: label[0], TABLE_NM: label[1] })
            })
            this.loadNavigationItems('all', 'word')
        },
        async onLinkItemSelect(item: iLinkTableItem) {
            switch (item.itemType) {
                case 'document':
                    await this.loadDocumentWords(item)
                    break
                case 'dataset':
                    await this.loadDatasetWords(item)
                    break
                case 'businessClass':
                    await this.loadBusinessClassWords(item)
                    break
                case 'table':
                    await this.loadTableWords(item)
            }
        },
        async loadDocumentWords(document: iLinkTableItem) {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getDocumentInfo?DOCUMENT_ID=${document.id}`)
                .then((response: AxiosResponse<any>) => (this.selectedLinkItemWords[document.id] = response.data.word))
                .finally(() => (this.loading = false))
        },
        async loadDatasetWords(dataset: iLinkTableItem) {
            this.loading = true
            await this.loadDatasetInfo(dataset)
                .then((response: AxiosResponse<any>) => {
                    this.selectedLinkItemWords[dataset.id] = response.data.Word
                    this.selectedLinkItemTree[dataset.id] = []
                    response.data.SbiGlDataSetWlist.forEach((el: any) => {
                        const organization = el.organization
                        const datasetId = el.datasetId
                        const tempNode = { key: el.alias, id: datasetId, label: el.alias, children: [] as iNode[], data: el, leaf: false, style: '', organization: organization, itemType: 'datasetTree' }
                        el.word.forEach((el: any) =>
                            tempNode.children.push({ key: el.WORD_ID, id: el.WORD_ID, label: el.WORD, children: [] as iNode[], data: el, style: this.glossaryUsageDescriptor.node.style, leaf: true, parent: tempNode, organization: organization, datasetId: datasetId, itemType: 'datasetTree' } as any)
                        )
                        this.selectedLinkItemTree[dataset.id].push(tempNode)
                    })
                })
                .finally(() => (this.loading = false))
        },
        async loadBusinessClassWords(businessClass: iLinkTableItem) {
            this.loading = true
            await this.loadBusinessClassInfo(businessClass)
                .then((response: AxiosResponse<any>) => {
                    this.selectedLinkItemWords[businessClass.id] = response.data.words
                    this.selectedLinkItemTree[businessClass.id] = []
                    response.data.sbiGlBnessClsWlist.forEach((el: any) => {
                        const tempNode = { key: el.columnId, id: el.columnId, label: el.name, children: [] as iNode[], data: el, leaf: false, style: '', businessClassId: response.data.metaBc.bcId, itemType: 'businessClassTree' }
                        el.word.forEach((el: any) => tempNode.children.push({ key: el.WORD_ID, id: el.WORD_ID, label: el.WORD, children: [] as iNode[], data: el, style: this.glossaryUsageDescriptor.node.style, leaf: true, parent: tempNode, itemType: 'businessClassTree' }))
                        this.selectedLinkItemTree[businessClass.id].push(tempNode)
                    })
                    this.selectedLinkItemTree[businessClass.id].sort((a: any, b: any) => (a.label.toUpperCase() > b.label.toUpperCase() ? 1 : -1))
                })
                .finally(() => (this.loading = false))
        },
        async loadTableWords(table: iLinkTableItem) {
            this.loading = true
            await this.loadTableInfo(table)
                .then((response: AxiosResponse<any>) => {
                    this.selectedLinkItemWords[table.id] = response.data.words
                    this.selectedLinkItemTree[table.id] = []
                    response.data.sbiGlTableWlist.forEach((el: any) => {
                        const tempNode = { key: el.columnId, id: el.columnId, label: el.name, children: [] as iNode[], data: el, leaf: false, style: '', metasourceId: response.data.metaTable.tableId, itemType: 'tableTree' }
                        el.word.forEach((el: any) => tempNode.children.push({ key: el.WORD_ID, id: el.WORD_ID, label: el.WORD, children: [] as iNode[], data: el, style: this.glossaryUsageDescriptor.node.style, leaf: true, parent: tempNode, itemType: 'tableTree' }))
                        this.selectedLinkItemTree[table.id].push(tempNode)
                    })
                    this.selectedLinkItemTree[table.id].sort((a: any, b: any) => (a.label.toUpperCase() > b.label.toUpperCase() ? 1 : -1))
                })
                .finally(() => (this.loading = false))
        },
        loadDatasetInfo(dataset: any) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getDataSetInfo?DATASET_ID=${dataset.id}&ORGANIZATION=${dataset.organization}`)
        },
        loadBusinessClassInfo(businessClass: any) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getMetaBcInfo?META_BC_ID=${businessClass.id}`)
        },
        loadTableInfo(table: any) {
            return this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getMetaTableInfo?META_TABLE_ID=${table.id}`)
        },
        async onLinkTableClose() {
            this.linkTableVisible = false
            await this.loadNavigationItems('all', 'word')
        },
        resetSelected() {
            this.selectedDocuments = []
            this.selectedDatasets = []
            this.selectedBusinessClasses = []
            this.selectedTables = []
            this.glossaryChanged = !this.glossaryChanged
        }
    }
})
</script>
