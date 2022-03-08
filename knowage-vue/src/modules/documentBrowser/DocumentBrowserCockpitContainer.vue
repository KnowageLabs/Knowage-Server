<template>
    <DocumentExecution :id="id" v-if="mode === 'document-execution'"></DocumentExecution>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentExecution from '@/modules/documentExecution/main/DocumentExecution.vue'

// const crypto = require('crypto')

export default defineComponent({
    name: 'document-browser-cockpit-container',
    components: {
        DocumentExecution
    },
    props: { id: { type: String }, functionalityId: { type: String }, item: { type: Object } },
    emits: ['iframeCreated'],
    data() {
        return {
            url: '',
            mode: '',
            testIFrame: null as any
        }
    },
    created() {
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
            // this.url =
            //     process.env.VUE_APP_HOST_URL +
            //     `/knowagecockpitengine/api/1.0/pages/edit?NEW_SESSION=TRUE&SBI_LANGUAGE=en&user_id=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZGVtb19hZG1pbiIsImV4cCI6MTY0NjcwMTg1OH0.eRLi6IXLSUO2UeEMydJGKMUnxwicytraOmrRUJIPfCI&SBI_COUNTRY=US&SBI_EXECUTION_ID=d33471d69e2911ecad1c934e91b00ab4&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=true&documentMode=EDIT&FUNCTIONALITY_ID=601`

            console.log('functionalityId: ', this.functionalityId)
            console.log('NEW URL: ', this.url)
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
