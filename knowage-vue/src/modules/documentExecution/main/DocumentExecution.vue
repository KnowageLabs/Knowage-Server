<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary">
        <template #left> </template>

        <template #right> </template>
    </Toolbar>
    <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />

    <div></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'document-execution',
    components: {},
    props: { id: { type: String } },
    data() {
        return {
            filtersData: null as any,
            urlData: null as any,
            exporters: null as any,
            user: null as any,
            loading: false
        }
    },
    async created() {
        console.log('ID: ', this.id)

        this.user = (this.$store.state as any).user

        console.log('LOADED USER: ', this.user)

        await this.loadFilters()
        await this.loadURL()
        await this.loadExporters()
    },
    methods: {
        async loadFilters() {
            this.loading = true
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/filters`, { label: this.id, role: this.user.defaultRole, parameters: {} }).then((response: AxiosResponse<any>) => (this.filtersData = response.data))
            this.loading = false
            console.log('LOADED FILTERS DATA: ', this.filtersData)
        },
        async loadURL() {
            this.loading = true
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/url`, { label: this.id, role: this.user.defaultRole, parameters: {}, EDIT_MODE: 'null', IS_FOR_EXPORT: true }).then((response: AxiosResponse<any>) => (this.urlData = response.data))
            this.loading = false
            console.log('LOADED URL DATA: ', this.urlData)
        },
        async loadExporters() {
            this.loading = true
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/exporters/${this.urlData.engineLabel}`).then((response: AxiosResponse<any>) => (this.exporters = response.data.exporters))
            this.loading = false
            console.log('LOADED EXPORTERS: ', this.exporters)
        }
    }
})
</script>
