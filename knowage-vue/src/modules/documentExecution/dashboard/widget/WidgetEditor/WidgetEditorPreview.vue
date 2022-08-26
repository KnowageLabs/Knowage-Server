<template>
    <!-- <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview"> -->
    <div :style="descriptor.style.preview">
        <pre>{{ propWidget }}</pre>
        <!-- <DataTable :value="rows" class="p-datatable-sm kn-table" :style="descriptor.style.previewTable" stripedRows rowHover>
            <Column v-for="col of columns" :field="col.name" :header="col.header" :key="col.dataIndex" class="kn-truncated" />
        </DataTable> -->
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent } from 'vue'
import { emitter } from '../../DashboardHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { Column, DataTable },
    props: {
        propWidget: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            descriptor,
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
        },
        setEventListeners() {}
    }
})
</script>
