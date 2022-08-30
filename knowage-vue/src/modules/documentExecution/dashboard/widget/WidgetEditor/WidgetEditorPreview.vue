<template>
    <div class="p-d-flex p-flex-column p-ai-stretch p-jc-center kn-overflow" :style="descriptor.style.preview">
        <!-- <div :style="descriptor.style.preview" class="kn-overflow"> -->
        <div style="overflow: auto; height: 500px; width: 400px">{{ propWidget }}</div>
        <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="populate" />

        <div ref="tabulator"></div>
    </div>
</template>

<script lang="ts">
/**
 * ! this component will be in charge of managing the widget editing preview.
 */
import { defineComponent } from 'vue'
import { IWidgetColumn } from '../../Dashboard'
import { emitter } from '../../DashboardHelpers'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import mock from '../../dataset/DatasetEditorTestMocks.json'
import descriptor from '../../dataset/DatasetEditorDescriptor.json'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator.min.css'

export default defineComponent({
    name: 'widget-editor-preview',
    components: { Column, DataTable, Tabulator },
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
            rows: [] as any,
            tabulator: null as any,
            columns: [] as any,
            tableData: [] as any
        }
    },
    mounted() {
        console.log('MOUNDETE _------------------')
        this.tabulator = new Tabulator(this.$refs.tabulator, {
            data: [],
            columns: [],
            layout: 'fitDataTable'
        })
    },
    created() {
        this.setDatatableData()
        this.setEventListeners()
    },
    methods: {
        populate() {
            this.tabulator.addColumn({ title: 'Name', field: 'name', width: 150 })
            // this.tableData = [
            //     { id: 1, name: 'Oli Bob', age: '12', col: 'red', dob: '' },
            //     { id: 2, name: 'Mary May', age: '1', col: 'blue', dob: '14/05/1982' },
            //     { id: 3, name: 'Christine Lobowski', age: '42', col: 'green', dob: '22/05/1982' },
            //     { id: 4, name: 'Brendon Philips', age: '125', col: 'orange', dob: '01/08/1980' },
            //     { id: 5, name: 'Margret Marmajuke', age: '16', col: 'yellow', dob: '31/01/1999' }
            // ]
            // this.columns = [
            //     { title: 'Name', field: 'name', width: 150 },
            //     { title: 'Age', field: 'age', hozAlign: 'left', formatter: 'progress' },
            //     { title: 'Favourite Color', field: 'col' },
            //     { title: 'Date Of Birth', field: 'dob', sorter: 'date', hozAlign: 'center' }
            // ]
        },
        setDatatableData() {
            this.mock.previewMock.metaData.fields.forEach((el: any) => {
                typeof el != 'object' ? '' : this.columns.push(el)
            })
            this.rows = this.mock.previewMock.rows
        },
        setEventListeners() {
            emitter.on('paginationChanged', () => console.log('WidgetEditorPreview - PAGINATION CHANGED!'))
            emitter.on('sortingChanged', () => console.log('WidgetEditorPreview  - SORTING CHANGED!'))
            emitter.on('collumnAdded', (column) => this.onColumnAdd(column))
            emitter.on('collumnRemoved', (column) => console.log('WidgetEditorPreview  - collumnRemoved!', column, this.propWidget))
            emitter.on('collumnUpdated', (column) => this.onColumnUpdate(column))
            emitter.on('columnsReordered', () => console.log('WidgetEditorPreview  - columnsReordered!'))
            emitter.on('indexColumnChanged', () => console.log('WidgetEditorPreview  - indexColumnChanged!'))
            emitter.on('rowSpanChanged', () => console.log('WidgetEditorPreview  - rowSpanChanged!'))
            emitter.on('summaryRowsChanged', () => console.log('WidgetEditorPreview  - summaryRowsChanged!'))
        },
        onColumnAdd(column) {
            // console.log('WidgetEditorPreview  - collumnAdded!', this.propWidget)
            let test = {
                alias: 'FID',
                dataset: 166,
                decript: false,
                fieldType: 'ATTRIBUTE',
                multiValue: false,
                name: 'FID',
                personal: false,
                precision: 0,
                properties: {},
                scale: 0,
                subjectId: false,
                type: 'java.lang.String'
            } as any
            console.log('WidgetEditorPreview  - collumnAdded!', column)
            this.tabulator.addColumn({ title: column.alias, field: column.name, width: 150 })
        },
        onColumnUpdate(column) {
            // console.log('WidgetEditorPreview  - collumnAdded!', this.propWidget)
            console.log('WidgetEditorPreview  - columnEdited!', column)
            console.log('GET TABULATOR COLUMNS ', column.columnName, this.tabulator.updateColumnDefinition('FID', { title: column.alias }))
            // this.tabulator.updateColumnDefinition(column.name, { title: column.alias })
        }
    }
})
</script>
