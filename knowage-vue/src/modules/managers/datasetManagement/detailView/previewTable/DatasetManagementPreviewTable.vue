<template>
    <DataTable :value="rows" class="p-datatable-sm kn-table" responsiveLayout="scroll" :scrollable="true" :reorderableColumns="true" :loading="loading" scrollDirection="both" scrollHeight="800px" stripedRows rowHover resizableColumns style="previewDescriptor">
        <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" style="width:250px" />
    </DataTable>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { DataTable, Column },
    props: { selectedDataset: { type: Object as any } },
    computed: {},
    emits: ['close'],
    data() {
        return {
            dataset: {} as any,
            columns: [] as any,
            rows: [] as any,
            loading: false
        }
    },
    created() {
        this.dataset = { ...this.selectedDataset }
        this.getPreviewData()
    },
    watch: {
        selectedDataset() {
            this.dataset = { ...this.selectedDataset }
            this.getPreviewData()
        }
    },
    methods: {
        async getPreviewData() {
            this.loading = true
            this.dataset.limit = 10000000
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/preview`, this.dataset, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8'
                    }
                })
                .then((response) => {
                    let previewColumns = response.data.metaData.fields
                    previewColumns.forEach((el: any) => {
                        typeof el != 'object' ? '' : this.columns.push(el)
                    })

                    this.rows = response.data.rows
                })
                .catch(() => this.$emit('close'))
                .finally(() => (this.loading = false))
        }
    }
})
</script>
