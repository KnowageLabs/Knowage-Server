<template>
    <div>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <div class="p-grid" v-if="!linkTableVisible">
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :title="$t('managers.glossaryUsage.documents')" :items="documents" @infoClicked="showDocumentInfo($event)" @linkClicked="onLinkClicked($event)"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :title="$t('managers.glossaryUsage.dataset')" :items="datasets" @infoClicked="showDatasetInfo($event)" @linkClicked="onLinkClicked($event)"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :title="$t('managers.glossaryUsage.businessClass')" :items="businessClasses" @infoClicked="showBusinessClassInfo($event)" @linkClicked="onLinkClicked($event)"></GlossaryUsageNavigationCard>
            </div>
            <div class="p-col-6">
                <GlossaryUsageNavigationCard class="p-m-2" :title="$t('managers.glossaryUsage.tables')" :items="tables" @infoClicked="showTableInfo($event)" @linkClicked="onLinkClicked($event)"></GlossaryUsageNavigationCard>
            </div>
        </div>
        <GlossaryUsageLinkCard v-else :title="linkTableTitle" class="p-m-2" :items="linkTableItems" @close="linkTableVisible = false"></GlossaryUsageLinkCard>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import GlossaryUsageNavigationCard from './card/GlossaryUsageNavigationCard.vue'
import GlossaryUsageLinkCard from './card/GlossaryUsageLinkCard.vue'

export default defineComponent({
    name: 'glossary-usage-detail',
    components: { GlossaryUsageNavigationCard, GlossaryUsageLinkCard },
    props: { glossaryId: { type: Number }, selectedWords: { type: Array } },
    emits: ['infoClicked', 'linkClicked'],
    data() {
        return {
            documents: [] as any[],
            selectedDocuments: [] as any[],
            datasets: [] as any[],
            selectedDatasets: [] as any[],
            businessClasses: [] as any[],
            selectedBusinessClasses: [] as any[],
            tables: [] as any[],
            selectedTables: [] as any[],
            linkTableVisible: false,
            linkTableTitle: '',
            linkTableItems: [] as any[],
            loading: false
        }
    },
    watch: {
        async glossaryId() {
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
            const postData = {
                type: type,
                item: item,
                word: { selected: this.selectedWords, search: '', item_number: 9000, page: 1, GLOSSARY_ID: this.glossaryId },
                document: { selected: [], search: '', item_number: 9000, page: 1 },
                dataset: { selected: [], search: '', item_number: 9000, page: 1 },
                table: { selected: [], search: '', item_number: 9000, page: 1 },
                bness_cls: { selected: [], search: '', item_number: 9000, page: 1 }
            }
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/loadNavigationItem', postData)
                .then((response) => this.formatNavigationItems(response.data))
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        formatNavigationItems(data: any) {
            if ('document' in data) {
                this.documents = []
                data.document.forEach((el: any) => this.documents.push({ id: el.DOCUMENT_ID, label: el.DOCUMENT_LABEL }))
            }
            if ('dataset' in data) {
                this.datasets = []
                data.dataset.forEach((el: any) => this.datasets.push({ id: el.DATASET_ID, label: el.DATASET_NM, organization: el.DATASET_ORG, type: 'dataset' }))
            }
            if ('bness_cls' in data) {
                this.businessClasses = []
                data.bness_cls.forEach((el: any) => this.businessClasses.push({ id: el.BC_ID, label: el.META_MODEL_NAME + '.' + el.BC_NAME, type: 'businessClass' }))
            }
            if ('table' in data) {
                this.tables = []
                data.table.forEach((el: any) => this.tables.push({ id: el.TABLE_ID, label: el.META_SOURCE_NAME + '.' + el.TABLE_NM, type: 'table' }))
            }
            // console.log('DOCUMENTS LOADED: ', this.documents)
            // console.log('BUSINESS CLASSES LOADED: ', this.businessClasses)
            // console.log('DATASETS LOADED: ', this.datasets)
            // console.log('TABLES LOADED: ', this.tables)
        },
        async showDocumentInfo(document: any) {
            // console.log('DOCUMENT FOR INFO: ', document)
            this.loading = true
            let tempDocument = null as any
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${document.label}`).then((response) => (tempDocument = response.data))

            if (tempDocument) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documents/${tempDocument.id}/roles`).then((response) => (tempDocument.access = response.data))
                this.$emit('infoClicked', { data: tempDocument, type: 'document' })
            }

            this.loading = false
        },
        async showDatasetInfo(dataset: any) {
            // console.log('DATASET FOR INFO: ', dataset)
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getDataSetInfo?DATASET_ID=${dataset.id}&ORGANIZATION=${dataset.organization}`)
                .then((response) => this.$emit('infoClicked', { data: response.data, type: 'dataset' }))
                .finally(() => (this.loading = false))
        },
        async showBusinessClassInfo(businessClass: any) {
            // console.log('BUSINESS CLASS FOR INFO: ', businessClass)
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getMetaBcInfo?META_BC_ID=${businessClass.id}`)
                .then((response) => this.$emit('infoClicked', { data: response.data, type: 'businessClass' }))
                .finally(() => (this.loading = false))
        },
        async showTableInfo(table: any) {
            // console.log('TABLE FOR INFO: ', table)
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getMetaTableInfo?META_TABLE_ID=${table.id}`)
                .then((response) => this.$emit('infoClicked', { data: response.data, type: 'table' }))
                .finally(() => (this.loading = false))
        },
        async onLinkClicked(type: string) {
            console.log('LINK CLICKED!', type)
            switch (type) {
                case 'Documents':
                    await this.loadDocuments()
                    break
                case 'Dataset':
                    await this.loadDatasets()
                    break
                case 'Business Class':
                    await this.loadBusinessClasses()
                    break
                case 'Tables':
                    await this.loadTables()
            }
        },
        async loadDocuments() {
            this.linkTableItems = []
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/documents/listDocument?Page=1&ItemPerPage=&label=&scope=GLOSSARY')
                .then((response) => {
                    response.data.item.forEach((el: any) => this.linkTableItems.push({ id: el.DOCUMENT_ID, name: el.DOCUMENT_LABEL, description: el.DOCUMENT_DESCR, type: '', author: el.DOCUMENT_AUTH }))
                    this.linkTableTitle = this.$t('managers.glossaryUsage.documents')
                    this.linkTableVisible = true
                })
                .finally(() => (this.loading = false))
        },
        async loadDatasets() {
            this.linkTableItems = []
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/datasets/?asPagedList=true&Page=1&ItemPerPage=&label=')
                .then((response) => {
                    response.data.item.forEach((el: any) => this.linkTableItems.push({ id: el.id.dsId, name: el.name, description: el.description, type: el.type, author: el.owner }))
                    this.linkTableTitle = this.$t('managers.glossaryUsage.dataset')
                    this.linkTableVisible = true
                })
                .finally(() => (this.loading = false))
        },
        async loadBusinessClasses() {
            this.linkTableItems = []
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/metaBC/listMetaBC?Page=1&ItemPerPage=&label=')
                .then((response) => {
                    response.data.forEach((el: any) => this.linkTableItems.push({ id: el.id, name: el.name, description: '', type: '', author: '' }))
                    this.linkTableTitle = this.$t('managers.glossaryUsage.businessClass')
                    this.linkTableVisible = true
                })
                .finally(() => (this.loading = false))
        },
        async loadTables() {
            this.linkTableItems = []
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/metaTable/listMetaTable?Page=1&ItemPerPage=&label=')
                .then((response) => {
                    response.data.forEach((el: any) => this.linkTableItems.push({ id: el.id, name: el.name, description: '', type: '', author: '' }))
                    this.linkTableTitle = this.$t('managers.glossaryUsage.tables')
                    this.linkTableVisible = true
                })
                .finally(() => (this.loading = false))
        }
    }
})
</script>
