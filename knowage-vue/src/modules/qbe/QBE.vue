<template>
    <h1>QBE WORKS: {{ id }}</h1>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { iQBE } from './QBE'

export default defineComponent({
    name: 'qbe',
    components: {},
    props: { id: { type: String } },
    data() {
        return {
            qbe: null as iQBE | null,
            customizedDatasetFunctions: {} as any,
            exportLimit: null as number | null,
            entities: [] as any[],
            //qbe: {} as any,
            loading: false
        }
    },
    watch: {
        async id() {
            await this.loadPage()
        }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadDataset()
            await this.loadCustomizedDatasetFunctions()
            await this.loadExportLimit()
            await this.loadEntities()
            await this.executeQBEQuery()
            this.loading = false
        },
        async loadDataset() {
            // HARDCODED Dataset label/name
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/Bojan%20QBE%20TEST`).then((response: AxiosResponse<any>) => {
                this.qbe = response.data[0]
                if (this.qbe) this.qbe.qbeJSONQuery = JSON.parse(this.qbe.qbeJSONQuery)
            })
            console.log('LOADED QBE: ', this.qbe)
        },
        async loadCustomizedDatasetFunctions() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${this.qbe?.qbeDataSourceId}`).then((response: AxiosResponse<any>) => (this.customizedDatasetFunctions = response.data))
            console.log('LOADED CUSTOMIZED DATASET FUNCTIONS: ', this.customizedDatasetFunctions)
        },
        async loadExportLimit() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/EXPORT.LIMITATION`).then((response: AxiosResponse<any>) => (this.exportLimit = response.data))
            console.log('LOADED EXPORT LIMIT: ', this.exportLimit)
        },
        async loadEntities() {
            // HARDCODED SBI_EXECUTION_ID
            await this.$http.get(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_TREE_ACTION&SBI_EXECUTION_ID=bbb69a67777811ecb185855116b7fb4d&datamartName=null`).then((response: AxiosResponse<any>) => (this.entities = response.data))
            console.log('LOADED ENTITIES: ', this.entities)
        },
        async executeQBEQuery() {
            // HARDCODED
            const postData = { catalogue: [], meta: this.formatQbeMeta(), pars: [], qbeJSONQuery: {}, schedulingCronLine: '0 * * * * ?' }
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `qbequery/executeQuery/?SBI_EXECUTION_ID=bbb69a67777811ecb185855116b7fb4d&currentQueryId=q1&start=0&limit=25`, postData).then((response: AxiosResponse<any>) => (this.qbe = response.data))
            console.log('LOADED QBE: ', this.qbe)
        },
        formatQbeMeta() {}
    }
})
</script>
