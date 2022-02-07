<template>
    <iframe :src="`${baseUrl}${url}`"></iframe>
</template>

<script>
    import authHelper from '@/helpers/commons/authHelper'
    export default {
        name: 'IframeRenderer',
        props: {
            url: String
        },
        data() {
            return {
                baseUrl: process.env.VUE_APP_HOST_URL || window.location.origin
            }
        },
        created() {
            window.addEventListener('message', this.receiveMessage)
        },
        methods: {
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
