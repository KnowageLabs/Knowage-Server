<template>
    <DocumentExecution :id="name" v-if="mode === 'document-execution'" :parameterValuesMap="parameterValuesMap" :tabKey="tabKey" @parametersChanged="$emit('parametersChanged', $event)"></DocumentExecution>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentExecution from '@/modules/documentExecution/main/DocumentExecution.vue'

export default defineComponent({
    name: 'document-browser-cockpit-container',
    components: {
        DocumentExecution
    },
    props: { id: { type: String }, functionalityId: { type: String }, item: { type: Object }, parameterValuesMap: { type: Object }, tabKey: { type: String } },
    emits: ['iframeCreated', 'closeIframe', 'parametersChanged'],
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
            if (this.item?.name) {
                this.mode = 'document-execution'
            } else {
                this.mode = 'cockpit'
                this.$emit('iframeCreated', { iframe: this.url, item: this.item })
            }
        }
    }
})
</script>
