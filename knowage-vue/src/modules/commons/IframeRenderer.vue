<template>
    <iframe :src="`${completeUrl}`"></iframe>
</template>

<script>
    export default {
        name: 'IframeRenderer',
        props: {
            url: String,
            externalLink: Boolean
        },
        data() {
            return {
                completeUrl: ''
            }
        },
        created() {
            this.createBaseUrl()
        },

        updated() {
            this.createBaseUrl()
        },
        methods: {
            createBaseUrl() {
                if (!this.url) {
                    this.$router.push('/')
                } else {
                    this.completeUrl = (this.externalLink ? '' : process.env.VUE_APP_HOST_URL || window.location.origin) + this.url
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
