<template>
    <DataTable class="p-datatable-sm kn-table" :value="rows" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px">
        <template v-for="col of columns" :key="col._field">
            <Column class="kn-truncated" :field="columnMap[col._field]" :header="col._title">
                <template #editor="slotProps">
                    <InputText v-if="col && col._editable === 'true'" v-model="slotProps.data[slotProps.column.props.field]" />
                    <span v-else>{{ slotProps.data[columnMap[col._field]] }}</span>
                </template>
            </Column>
        </template>

        <Column :style="registryDatatableDescriptor.iconColumn.style">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="deleteRecord(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import registryDatatableDescriptor from './RegistryDatatableDescriptor.json'

export default defineComponent({
    name: 'registry-datatable',
    components: { Column, DataTable },
    props: { propColumns: { type: Array }, propRows: { type: Array }, columnMap: { type: Object } },
    data() {
        return {
            registryDatatableDescriptor,
            columns: [] as any[],
            rows: [] as any[]
        }
    },
    watch: {
        propColumns() {
            this.loadColumns()
        },
        propRows() {
            this.loadRows()
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
    },
    methods: {
        loadColumns() {
            this.columns = [{ _field: 'id', _title: '', _size: '', _visible: 'true', _editable: 'false' }]
            this.propColumns?.forEach((el: any) => this.columns.push(el))
            console.log('COLUMN: ', this.columns)
        },
        loadRows() {
            this.rows = [...(this.propRows as any[])]
            console.log('ROWS: ', this.rows)
        },
        deleteRecord(record: any) {
            console.log('RECORD FOR DELETE: ', record)
        }
    }
})
</script>
