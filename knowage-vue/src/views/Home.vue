<template>
<h1>{{ $t("home.test") }} - {{getDate()}}</h1>
  <div class="home">
    <HelloWorld msg="Welcome to Your Vue.js + TypeScript App"/>
  </div>
  <button @click="setLocale()">asd</button>
  <button @click="errorTest">error</button>
</template>

<script lang="ts">

import { defineComponent } from 'vue'
import { formatDate } from '@/helpers/dateHelper'
import HelloWorld from '@/components/HelloWorld.vue'

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
  methods: {
    getDate(){
      return formatDate(undefined,'LLL')
    },
    setLocale(){
      this.$store.commit('setLocale', 'en_GB')
      // eslint-disable-next-line
      // @ts-ignore
      this.$i18n.locale = 'en_GB'
    },
    errorTest(){
      this.$toast.add({severity:'error', summary: 'Error Message', detail:'Message Content', life: 3000});
    }
  }
});
</script>
