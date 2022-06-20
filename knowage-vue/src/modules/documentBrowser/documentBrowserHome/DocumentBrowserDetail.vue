<template>
    <div class="p-d-flex p-flex-row kn-flex">
        <div class="p-d-flex p-flex-column kn-flex">
            <div class="document-table-container p-d-flex p-flex-column kn-flex">
                <div v-if="selectedDocument" id="document-detail-backdrop" @click="selectedDocument = null"></div>
                <DocumentBrowserBreadcrumb v-if="!searchMode" :breadcrumbs="breadcrumbs" @breadcrumbClicked="$emit('breadcrumbClicked', $event)"></DocumentBrowserBreadcrumb>
                <DocumentBrowserTable :propDocuments="documents" :searchMode="searchMode" @selected="setSelectedDocument" @itemSelected="$emit('itemSelected', $event)"></DocumentBrowserTable>
            </div>
        </div>
        <div v-if="selectedDocument" id="document-browser-sidebar-container" data-test="document-browser-sidebar">
            <DocumentBrowserSidebar
                :selectedDocument="selectedDocument"
                @documentCloneClick="cloneDocument"
                @documentDeleteClick="deleteDocument"
                @itemSelected="$emit('itemSelected', $event)"
                @documentChangeStateClicked="changeDocumentState"
                @showDocumentDetails="$emit('showDocumentDetails', $event)"
            ></DocumentBrowserSidebar>
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
    emits: ['breadcrumbClicked', 'loading', 'documentCloned', 'itemSelected', 'documentStateChanged', 'showDocumentDetails'],
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
        setSelectedDocument(document: any) {
            this.selectedDocument = document
        },
        async cloneDocument(document: any) {
            this.$emit('loading', true)
            await this.$http
                .post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `documents/clone?docId=${document.id}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$emit('documentCloned')
                })
                .catch(() => {})
            this.$emit('loading', false)
        },
        async deleteDocument(document: any) {
            this.$emit('loading', true)
            await this.$http
                .delete(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documents/${document.label}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.selectedDocument = null
                    this.documents = this.documents.filter((el: any) => el.id !== document.id)
                })
                .catch(() => {})
            this.$emit('loading', false)
        },
        async changeDocumentState(event: any) {
            this.$emit('loading', true)
            await this.$http
                .post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `documents/changeStateDocument?docId=${event.document.id}&direction=${event.direction}`)
                .then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$emit('documentStateChanged')
                })
                .catch(() => {})
            this.$emit('loading', false)
        }
    }
})
</script>

<style lang="scss" scoped>
#document-browser-sidebar-container {
    flex: 0.3;
    z-index: 100;
}

.document-table-container {
    width: 100%;
    height: 100%;
    position: relative;
}

#document-detail-backdrop {
    background-color: rgba(33, 33, 33, 1);
    opacity: 0.48;
    z-index: 50;
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}
</style>
