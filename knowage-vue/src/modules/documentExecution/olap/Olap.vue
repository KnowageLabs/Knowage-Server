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
            olap: null as any,
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
            await this.loadOlapModel()
            this.loading = false
        },
        async loadOlapModel() {
            this.loading = true
            await this.$http
                .post(
                    process.env.VUE_APP_OLAP_PATH + `/1.0/model/?SBI_EXECUTION_ID=${this.id}`,
                    {},
                    {
                        headers: {
                            Accept: 'application/json, text/plain, */*',
                            'Content-Type': 'application/x-www-form-urlencoded'
                        }
                    }
                )
                .then((response: AxiosResponse<any>) => (this.olap = response.data))
                .catch(() => {})
            this.loading = false

            console.log('LOADED OLAP: ', this.olap)
        }
    }
})
</script>
