<template>
    <h1>IT WORKS</h1>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'

export default defineComponent({
    name: 'functions-catalog',
    components: {},
    data() {
        return {
            user: null as any
        }
    },
    async created() {
        await this.loadUser()
    },
    methods: {
        async loadUser() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/currentuser`).then((response) => (this.user = response.data))
            console.log('loadUser() LOADED USER: ', this.user)
        }
    }
})
</script>
