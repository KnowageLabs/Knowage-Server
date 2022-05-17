<template>
    <DocumentExecution :id="name" v-if="mode === 'document-execution'" :parameterValuesMap="parameterValuesMap" :tabKey="tabKey" @parametersChanged="$emit('parametersChanged', $event)"></DocumentExecution>
    <DocumentDetails v-else-if="mode === 'document-detail'" :docId="id" :folderId="functionalityId" @closeDetails="$emit('closeDetails', item)" @documentSaved="onDocumentsSaved"></DocumentDetails>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentExecution from '@/modules/documentExecution/main/DocumentExecution.vue'
import DocumentDetails from '@/modules/documentExecution/documentDetails/DocumentDetails.vue'

export default defineComponent({
    name: 'document-browser-cockpit-container',
    components: {
        DocumentExecution,
        DocumentDetails
    },
    props: { id: { type: String }, functionalityId: { type: String }, item: { type: Object }, parameterValuesMap: { type: Object }, tabKey: { type: String } },
    emits: ['iframeCreated', 'closeIframe', 'parametersChanged', 'closeDetails', 'documentSaved'],
    data() {
        return {
            url: '',
            mode: '',
            testIFrame: null as any,
            name: '' as string
        }
    },
    watch: {
        id() {
            this.name = this.id as string
            this.setMode()
        }
    },
    created() {
        this.name = this.id as string
        this.createUrl()
        this.setMode()
        console.log(' >>> ROUTE: ', this.$route)
        console.log(' >>> ID: ', this.id)
        console.log(' >>> functionalityId: ', this.functionalityId)
        console.log(' >>> item: ', this.item)
        console.log(' >>> mode: ', this.mode)
    },
    activated() {
        this.setMode()
    },
    deactivated() {
        this.mode = ''
    },
    methods: {
        createUrl() {
            const user = (this.$store.state as any).user
            const language = user.locale.split('_')[0]
            const uniqueID = user.userUniqueIdentifier
            const country = user.locale.split('_')[1]

            this.url = process.env.VUE_APP_HOST_URL + `/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=${language}&user_id=${uniqueID}&SBI_COUNTRY=${country}&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=true&documentMode=EDIT&FUNCTIONALITY_ID=${this.functionalityId}`
        },
        setMode() {
            console.log('ITEM: ', this.item)
            if (this.$route.name === 'document-browser-document-details-edit' || this.$route.name === 'document-browser-document-details-new') {
                this.mode = 'document-detail'
            } else if (this.item?.name) {
                this.mode = 'document-execution'
            } else {
                this.mode = 'cockpit'
                this.$emit('iframeCreated', { iframe: this.url, item: this.item })
            }
        },
        onDocumentsSaved(document: any) {
            console.log('>>> >>> EVENT: ', document)
            this.$emit('documentSaved', document)
        }
    }
})
</script>
