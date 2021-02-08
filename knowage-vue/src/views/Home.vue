<template>
<h1>{{ $t("test") }}</h1>
  <div class="home">
    <HelloWorld msg="Welcome to Your Vue.js + TypeScript App"/>
  </div>
</template>

<script lang="ts">

import { defineComponent } from 'vue';
import HelloWorld from '@/components/HelloWorld.vue';

export default defineComponent({
  name: 'Home',
  components: {
    HelloWorld
  },
  props: {
    msg: {
      type: String,
      required: false,
    },
  },
});
</script>
