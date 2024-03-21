<template>
    <iframe :src="`${completeUrl}`"></iframe>
</template>

<script>
    import authHelper from '@/helpers/commons/authHelper'
    export default {
        name: 'IframeRenderer',
        emits: ['click'],
        props: {
            url: String,
            externalLink: Boolean
        },
        data() {
            return {
                completeUrl: '',
                clickMonitor: null
            }
        },
        created() {
            window.addEventListener('message', this.receiveMessage)
            this.createBaseUrl()
            this.clickMonitor = setInterval(() => {
            const elem = document.activeElement
            if (elem && elem.tagName == 'IFRAME') {
                this.$emit('click')
            }
        }, 200)
        },
        updated() {
            this.createBaseUrl()
        },
        unmounted() {
            clearInterval(this.clickMonitor)
        },
        methods: {
            createBaseUrl() {
                if (!this.url) {
                    this.$router.push('/')
                } else {
                    this.completeUrl = (this.externalLink ? '' : process.env.VUE_APP_HOST_URL || window.location.origin) + this.url
                }
            },
            
            receiveMessage(event) {
                if (event && event.data && event.data.status === 401) {
                    authHelper.handleUnauthorized()
                }
            }
        }
    }
</script>

<style lang="scss" scoped>
    iframe {
        border: 0;
        width: 100%;
        height: 100%;
    }
</style>
