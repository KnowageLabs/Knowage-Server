<template>
    <h3>IT WOOOOOOOOOOOOOOORKS</h3>
    <h3>{{ id }}</h3>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'document-browser-tab',
    components: {},
    props: { id: { type: String } },
    data() {
        return {}
    },
    created() {
        console.log('CONTAINER CREATED!')
    },
    mounted() {
        console.log('CONTAINER MOUNTED!')
    },
    methods: {}
})
</script>
