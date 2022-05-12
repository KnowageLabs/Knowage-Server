<template>
    <Card class="p-mt-4">
        <template #content>
            <DataTable :value="rows" class="p-datatable-sm kn-table" responsiveLayout="scroll" :loading="loading" stripedRows rowHover>
                <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" :sortable="true" />
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
    computed: {},
    emits: ['close'],
    data() {
        return {
            columns: [] as any,
            rows: [] as any,
            loading: false
        }
    },
    created() {
        this.getPreviewData()
    },
    watch: {
        selectedDataset() {
            this.getPreviewData()
        }
    },
    methods: {
        getPreviewData() {
            this.loading = true
            let previewColumns = this.gridForPreview.metaData.fields
            previewColumns.forEach((el: any) => {
                typeof el != 'object' ? '' : this.columns.push(el)
            })
            this.rows = this.gridForPreview.rows
            this.loading = false
        }
    }
})
</script>
