<template>
    <iframe :src="`${baseUrl}${url}`"></iframe>
</template>

<script>

export default {
    name: 'IframeRenderer',
    props: {
        url: String
    },
    data(){
        return{
            baseUrl: process.env.VUE_APP_API_URL
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
