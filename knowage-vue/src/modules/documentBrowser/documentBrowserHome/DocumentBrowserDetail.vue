<template>
    <div class="p-d-flex p-flex-row">
        <div class="kn-flex">
            <DocumentBrowserBreadcrumb v-if="!searchMode" :breadcrumbs="breadcrumbs" @breadcrumbClicked="$emit('breadcrumbClicked', $event)"></DocumentBrowserBreadcrumb>
            <DocumentBrowserTable class="p-m-2" :propDocuments="documents" @executeDocumentClick="executeDocument"></DocumentBrowserTable>
        </div>
        <div id="document-browser-sidebar-container">
            <DocumentBrowserSidebar></DocumentBrowserSidebar>
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
    emits: ['breadcrumbClicked'],
    data() {
        return {
            documents: [] as any[]
        }
    },
    watch: {
        propDocuments() {
            this.loadDocuments()
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
        }
    }
})
</script>

<style lang="scss" scoped>
#document-browser-sidebar-container {
    flex: 0.3;
}
</style>
