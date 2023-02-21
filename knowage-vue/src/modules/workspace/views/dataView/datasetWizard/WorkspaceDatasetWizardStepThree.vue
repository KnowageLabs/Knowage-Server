<template>
    <Card class="p-mt-4">
        <template #content>
            <DataTable :value="rows" class="p-datatable-sm kn-table" responsive-layout="scroll" :loading="loading" striped-rows row-hover>
                <Column v-for="col of columns" :key="col.dataIndex" :field="col.name" :header="col.header" class="kn-truncated" :sortable="true" />
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { DataTable, Column, Card },
    props: { gridForPreview: { type: Object as any } },
    emits: ['close'],
    data() {
        return {
            columns: [] as any,
            rows: [] as any,
            loading: false
        }
    },
    computed: {},
    watch: {
        selectedDataset() {
            this.getPreviewData()
        }
    },
    created() {
        this.getPreviewData()
    },
    methods: {
        getPreviewData() {
            this.loading = true
            const previewColumns = this.gridForPreview.metaData.fields
            previewColumns.forEach((el: any) => {
                typeof el != 'object' ? '' : this.columns.push(el)
            })
            this.rows = this.gridForPreview.rows
            this.loading = false
        }
    }
})
</script>
