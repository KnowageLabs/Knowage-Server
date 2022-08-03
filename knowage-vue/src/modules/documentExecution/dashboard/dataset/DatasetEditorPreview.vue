<template>
    <div class="datasetEditor-preview">
        <DataTable :value="rows" class="p-datatable-sm kn-table" style="height: unset !important" stripedRows rowHover>
            <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" />
        </DataTable>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the dataset editing preview.
 */
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mock from './DatasetEditorTestMocks.json'

export default defineComponent({
    name: 'dataset-editor-preview',
    components: { Column, DataTable },
    props: {
        selectedDatasetProp: { required: true, type: Object }
    },
    data() {
        return {
            mock,
            columns: [] as any,
            rows: [] as any
        }
    },
    created() {
        this.setDatatableData()
    },
    methods: {
        setDatatableData() {
            this.mock.previewMock.metaData.fields.forEach((el: any) => {
                typeof el != 'object' ? '' : this.columns.push(el)
            })
            this.rows = this.mock.previewMock.rows
        }
    }
})
</script>
<style lang="scss">
.datasetEditor-preview {
    border-left: 1px solid #ccc;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: stretch;
    overflow: auto;
}
</style>
