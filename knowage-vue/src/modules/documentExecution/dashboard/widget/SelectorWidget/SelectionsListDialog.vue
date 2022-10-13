<template>
    <Dialog class="kn-dialog--toolbar--secondary selectionsDialog" :visible="visible" style="width: 60%" :header="$t('dashboard.datasetEditor.selectDatasets')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine selectionGrid p-m-2" :gridOptions="gridOptions"></ag-grid-vue>
        <template #footer>
            <Button class="kn-button kn-button--secondary p-mb-2" :label="$t('common.close')" @click="$emit('close')" />
            <Button class="kn-button kn-button p-mb-2" v-t="'common.save'" @click="addSelectedDatasets" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Dialog from 'primevue/dialog'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import buttonRenderer from './SelectionsListDialogCellRenderer.vue'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Dialog, AgGridVue },
    props: { visible: { type: Boolean }, selectedDatasetsProp: { required: true, type: Array as any }, availableDatasetsProp: { required: true, type: Array as any } },
    emits: ['close'],
    data() {
        return {
            gridApi: null as any,
            gridColumnApi: null as any,
            gridOptions: {
                rowData: [],
                columnDefs: [
                    { headerName: 'Dataset', field: 'ds' },
                    { headerName: 'Column Name', field: 'columnName' },
                    { headerName: 'Values', field: 'value' },
                    { headerName: '', cellRenderer: buttonRenderer, field: 'id', cellStyle: { 'text-align': 'right', display: 'inline-flex', 'justify-content': 'flex-end', border: 'none' }, width: 50, suppressSizeToFit: true, tooltip: false }
                ],
                enableColResize: false,
                enableFilter: false,
                enableSorting: false,
                pagination: false,
                suppressRowTransform: true,
                suppressMovableColumns: true,
                suppressDragLeaveHidesColumns: true,
                suppressRowGroupHidesColumns: true,
                rowHeight: 25,
                headerHeight: 30,
                onGridReady: this.onGridReady
            }
        }
    },
    setup() {},
    async created() {},
    updated() {},
    methods: {
        onGridReady(params) {
            this.gridApi = params.api
            this.gridColumnApi = params.columnApi

            params.api.sizeColumnsToFit()
            window.addEventListener('resize', function() {
                setTimeout(function() {
                    params.api.sizeColumnsToFit()
                })
            })

            const updateData = (data) => params.api.setRowData(data)

            updateData([
                { ds: 'AUDIT_02', columnName: 'data', column: 'data', value: '2021 11 22', aggregated: false },
                { ds: 'AUDIT_03', columnName: 'data', column: 'data', value: '2021 11 22', aggregated: false },
                { ds: 'AUDIT_04', columnName: 'data', column: 'data', value: '2021 11 22', aggregated: false },
                { ds: 'AUDIT_05', columnName: 'data', column: 'data', value: '2021 11 22', aggregated: false }
            ])
        }
    }
})
</script>

<style lang="scss">
.selectionsDialog {
    .selectionGrid {
        height: 250px;
        .ag-root-wrapper {
            border: none;
        }
    }
    .p-dialog-content,
    .p-dialog-footer {
        padding: 0;
    }
}
</style>
