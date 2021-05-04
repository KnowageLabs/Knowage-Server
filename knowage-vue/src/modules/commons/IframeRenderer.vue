<template>
    <iframe :src="`${baseUrl}${url}`"></iframe>
</template>

<script>
export default {
    name: 'IframeRenderer',
    props: {
        url: String
    },
    data() {
        return {
            baseUrl: process.env.VUE_APP_HOST_URL || window.location.origin
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
