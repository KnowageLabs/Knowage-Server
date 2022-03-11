<template>
    <DocumentExecution :id="name" v-if="mode === 'document-execution'"></DocumentExecution>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DocumentExecution from '@/modules/documentExecution/main/DocumentExecution.vue'

export default defineComponent({
    name: 'document-browser-cockpit-container',
    components: {
        DocumentExecution
    },
    props: { id: { type: String }, functionalityId: { type: String }, item: { type: Object } },
    emits: ['iframeCreated', 'closeIframe'],
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
        }
    },
    created() {
        window.addEventListener('message', (event) => {
            if (event.data.type === 'crossNavigation') {
                console.log('EVENT FROM ANGULAR: ', event)
            }
        })

        this.name = this.id as string
        this.createUrl()
        this.setMode()

        // setTimeout(() => this.loadCockpit('TC_1581'), 5000)
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
        },
        loadCockpit(name: string) {
            // TC_1581
            console.log('NAME: ', name)
            this.name = name
            this.mode = 'document-execution'
            this.$router.push(`/document-browser/document-composite/${name}`)
            this.$emit('closeIframe')
        }
    }
})
</script>
