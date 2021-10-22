<template>
    <div class="p-d-flex p-flex-row">
        <div class="kn-flex">
            <DocumentBrowserBreadcrumb v-if="!searchMode" :breadcrumbs="breadcrumbs" @breadcrumbClicked="$emit('breadcrumbClicked', $event)"></DocumentBrowserBreadcrumb>
            <DocumentBrowserTable class="p-m-2" :propDocuments="documents" @executeDocumentClick="executeDocument" @selected="setSelectedDocument"></DocumentBrowserTable>
        </div>
        <div v-if="selectedDocument" id="document-browser-sidebar-container">
            <DocumentBrowserSidebar :selectedDocument="selectedDocument" @documentCloneClick="cloneDocument"></DocumentBrowserSidebar>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

import DocumentBrowserBreadcrumb from './breadcrumbs/DocumentBrowserBreadcrumb.vue'
import DocumentBrowserTable from './tables/DocumentBrowserTable.vue'
import DocumentBrowserSidebar from './sidebar/DocumentBrowserSidebar.vue'

export default defineComponent({
    name: 'document-browser-detail',
    components: { DocumentBrowserBreadcrumb, DocumentBrowserTable, DocumentBrowserSidebar },
    props: { propDocuments: { type: Array }, breadcrumbs: { type: Array }, searchMode: { type: Boolean } },
    emits: ['breadcrumbClicked', 'loading'],
    data() {
        return {
            documents: [] as any[],
            selectedDocument: null as any
        }
    },
    watch: {
        propDocuments() {
            this.loadDocuments()
            this.selectedDocument = null
        }
    },
    created() {
        this.loadDocuments()
    },
    methods: {
        loadDocuments() {
            this.documents = this.propDocuments as any[]
        },
        executeDocument(document: any) {
            console.log('DOCUMENT FOR EXECUTION: ', document)
        },
        setSelectedDocument(document: any) {
            this.selectedDocument = document
            console.log('SELECTED DOCUMENT: ', this.selectedDocument)
        },
        cloneDocument(document: any) {
            console.log('DOCUMENT FOR CLONE: ', document)
            this.$emit('loading', true)
            this.$emit('loading', false)
        }
    }
})
</script>

<style lang="scss" scoped>
#document-browser-sidebar-container {
    flex: 0.3;
}
</style>
