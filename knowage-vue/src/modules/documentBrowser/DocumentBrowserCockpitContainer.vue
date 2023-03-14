<template>
    <div id="cockpit-container" class="kn-height-full">
        <DocumentExecution
            v-show="mode === 'document-execution'"
            :id="name"
            :prop-mode="mode"
            :style="[mode === 'document-execution' ? '' : 'display: none !important; ']"
            :parameter-values-map="parameterValuesMap"
            :tab-key="tabKey"
            @parametersChanged="$emit('parametersChanged', $event)"
            @close="$emit('close')"
        ></DocumentExecution>
        <DocumentDetails
            v-show="mode === 'document-detail'"
            :style="[mode === 'document-detail' ? '' : 'display: none !important;']"
            :prop-mode="'execution'"
            :view-mode="mode"
            :prop-doc-id="item?.id"
            :whole-item="item"
            :prop-folder-id="functionalityId"
            @closeDetails="$emit('closeDetails', item)"
            @documentSaved="onDocumentsSaved"
        ></DocumentDetails>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentExecution from '@/modules/documentExecution/main/DocumentExecution.vue'
import DocumentDetails from '@/modules/documentExecution/documentDetails/DocumentDetails.vue'
import mainStore from '../../App.store'

export default defineComponent({
    name: 'document-browser-cockpit-container',
    components: {
        DocumentExecution,
        DocumentDetails
    },
    props: { id: { type: String }, functionalityId: { type: String }, item: { type: Object }, parameterValuesMap: { type: Object }, tabKey: { type: String } },
    emits: ['iframeCreated', 'closeIframe', 'parametersChanged', 'closeDetails', 'documentSaved', 'close'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            url: '',
            mode: '',
            testIFrame: null as any,
            name: '' as string,
            loadedItem: null as any
        }
    },
    watch: {
        id() {
            this.name = this.id as string
            this.loadItem()
            this.setMode()
        },
        item() {
            this.loadItem()
            this.setMode()
        }
    },
    created() {
        this.name = this.id as string
        this.createUrl()
        this.loadItem()
        this.setMode()
    },
    activated() {
        this.loadItem()
        this.setMode()
    },
    deactivated() {
        this.mode = ''
    },
    methods: {
        createUrl() {
            const user = (this.store.$state as any).user
            const locale = this.store.getLocale()
            const language = locale.split('_')[0]
            const uniqueID = user.userUniqueIdentifier
            const country = locale.split('_')[1]

            this.url = import.meta.env.VITE_HOST_URL + `/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=${language}&user_id=${uniqueID}&SBI_COUNTRY=${country}&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=true&documentMode=EDIT&FUNCTIONALITY_ID=${this.functionalityId}`
        },
        setMode() {
            if (!this.loadedItem) return

            if (this.loadedItem.showMode === 'documentDetail') {
                this.mode = 'document-detail'
            } else if (this.loadedItem.showMode === 'execute' || this.loadedItem.showMode === 'createDashboard') {
                this.mode = 'document-execution'
            } else if (this.loadedItem.showMode === 'createCockpit') {
                this.mode = 'cockpit'
                this.$emit('iframeCreated', { iframe: this.url, item: this.item })
            }
        },
        onDocumentsSaved(document: any) {
            this.$emit('documentSaved', document)
        },
        loadItem() {
            this.loadedItem = this.item
        }
    }
})
</script>
