<template>
    <h1>IT WORKS. ID {{ id }}</h1>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import olapDescriptor from './OlapDescriptor.json'

export default defineComponent({
    name: 'olap',
    components: {},
    props: { id: { type: String }, reloadTrigger: { type: Boolean } },
    data() {
        return {
            olapDescriptor,
            loading: false
        }
    },
    async created() {
        await this.loadPage()
    },
    watch: {
        async id() {
            await this.loadPage()
        },
        async reloadTrigger() {
            await this.loadPage()
        }
    },
    methods: {
        async loadPage() {
            this.loading = true
            this.loading = false
        },
        async loadOlapModel() {
            this.loading = true
            await this.$http
                .post(`/knowagewhatifengine/restful-services/1.0/model/?SBI_EXECUTION_ID=${this.id}`, {})
                .then((response: AxiosResponse<any>) => {
                    console.log('RESPONSE: ', response)
                })
                .catch(() => {})
            this.loading = false
        }
    }
})
</script>
