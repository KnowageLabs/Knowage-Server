<template>
    <!-- <DataTable v-if="previewData != null" :value="rows" stripedRows :scrollable="true" scrollHeight="400px" :loading="loading" scrollDirection="horizontal"> -->
    <DataTable v-if="previewData != null" :value="rows" stripedRows responsiveLayout="scroll" breakpoint="flex">
        <template #empty>
            <div id="noFunctionsFound">
                {{ $t('common.info.noDataFound') }}
            </div>
        </template>
        <Column v-for="col of columns" :field="col.field" :key="col.field" style="flex-grow:1; flex-basis:200px">
            <template #header>
                <div class="dropdown">
                    <div clas="p-d-flex p-flex-column">
                        <p class="p-m-0">{{ col.header }}</p>
                    </div>
                </div>
            </template>
        </Column>
    </DataTable>
    <span v-else>nothing to preview</span>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
// import Menu from 'primevue/menu'
// import Checkbox from 'primevue/checkbox'
import QBESimpleTableDescriptor from './QBESmartTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { previewData: { type: Object, required: true } },
    components: { Column, DataTable },
    emits: [],
    data() {
        return {
            QBESimpleTableDescriptor,
            columns: [] as any,
            rows: [] as any[]
        }
    },
    computed: {},
    watch: {
        previewData() {
            this.previewData != null ? this.setData() : ''
        }
    },
    created() {
        this.previewData != null ? this.setData() : ''
    },
    methods: {
        setData() {
            this.rows = this.previewData.rows
            this.columns = this.setPreviewColumns(this.previewData)
        },
        setPreviewColumns(data: any) {
            let columns = [] as any
            for (let i = 1; i < data.metaData.fields.length; i++) {
                columns.push({ header: data.metaData.fields[i].header, field: data.metaData.fields[i].name, type: data.metaData.fields[i].type })
            }
            return columns
        }
    }
})
</script>
