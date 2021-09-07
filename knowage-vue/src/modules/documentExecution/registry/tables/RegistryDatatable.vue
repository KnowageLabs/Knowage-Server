<template>
    <DataTable class="p-datatable-sm kn-table" :value="rows" editMode="cell" dataKey="id" responsiveLayout="stack" breakpoint="960px">
        <template v-for="col of columns" :key="col._field">
            <Column class="kn-truncated" :field="columnMap[col._field]" :header="col._title">
                <template #editor="slotProps">
                    <span v-if="col._editable === 'false' && col.columnInfo.type !== 'boolean'">{{ slotProps.data[columnMap[col._field]] }}</span>
                    <InputText v-else-if="col._editable === 'true'" v-model="slotProps.data[slotProps.column.props.field]" />
                    <span v-else>TODO</span>
                </template>
                <template #body="slotProps">
                    {{ slotProps.data.label }}
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
    props: { propColumns: { type: Array }, propRows: { type: Array }, columnMap: { type: Object }, propConfiguration: { type: Object } },
    data() {
        return {
            registryDatatableDescriptor,
            columns: [] as any[],
            rows: [] as any[],
            configuration: {} as any,
            buttons: {
                enableButtons: false,
                enableDeleteRecords: false,
                enableAddRecords: false
            }
        }
    },
    watch: {
        propColumns() {
            this.loadColumns()
        },
        propRows() {
            this.loadRows()
        },
        propConfiguration() {
            this.loadConfiguration()
        }
    },
    created() {
        this.loadColumns()
        this.loadRows()
        this.loadConfiguration()
    },
    methods: {
        loadColumns() {
            this.columns = [{ _field: 'id', _title: '', _size: '', _visible: 'true', _editable: 'false' }]
            this.propColumns?.forEach((el: any) => {
                if (el._visible === 'true') this.columns.push(el)
            })
            console.log('COLUMN: ', this.columns)
        },
        loadRows() {
            this.rows = [...(this.propRows as any[])]
            console.log('ROWS: ', this.rows)
        },
        deleteRecord(record: any) {
            console.log('RECORD FOR DELETE: ', record)
        },
        loadConfiguration() {
            this.configuration = this.propConfiguration

            for (let i = 0; i < this.configuration.length; i++) {
                if (this.configuration[i]._name === 'enableButtons') {
                    this.buttons.enableButtons = this.configuration[i]._value === 'true'
                } else {
                    if (this.configuration[i]._name === 'enableDeleteRecords') {
                        this.buttons.enableDeleteRecords = this.configuration[i]._value === 'true'
                    }
                    if (this.configuration[i]._name === 'enableAddRecords') {
                        this.buttons.enableAddRecords = this.configuration[i]._value === 'true'
                    }
                }
                // console.log('CONFIGURATION: ', this.configuration)
                // console.log('BUTTONS: ', this.configuration)
            }
        }
    }
})
</script>
