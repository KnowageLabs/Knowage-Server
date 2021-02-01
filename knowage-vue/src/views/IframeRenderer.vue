<template>
    <iframe :src="`http://localhost:8080${url}`"></iframe>
</template>

<script>

export default {
  name: 'IframeRenderer',
  props: {
      url: String
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
