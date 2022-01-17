<template>
    <DataTable class="p-datatable-sm kn-table p-m-2" :value="rows" editMode="cell" responsiveLayout="stack" breakpoint="960px">
        <Column v-for="column in QBESimpleTableDescriptor.columns" :key="column.header" :header="column.header" :field="column.field"></Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iQuery, iField } from '../../QBE'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import QBESimpleTableDescriptor from './QBESimpleTableDescriptor.json'

export default defineComponent({
    name: 'qbe-simple-table',
    props: { query: { type: Object as PropType<iQuery> } },
    components: { Column, DataTable },
    data() {
        return {
            QBESimpleTableDescriptor,
            rows: [] as iField[]
        }
    },
    watch: {
        queryResult() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            console.log('QBE  QUERY INSIDE TABLE: ', this.query)
            if (!this.query) return

            this.rows = this.query.fields as iField[]
        }
    }
})
</script>
