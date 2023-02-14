<template>
    <Dialog class="kn-dialog--toolbar--secondary selectionsDialog" :visible="visible" style="width: 60%" :header="$t('dashboard.selectionsList')" :closable="false" modal :breakpoints="{ '960px': '75vw', '640px': '100vw' }">
        <ag-grid-vue class="kn-table-widget-grid ag-theme-alpine selectionGrid p-m-2" :gridOptions="gridOptions" :context="context"></ag-grid-vue>
        <template #footer>
            <Button class="kn-button kn-button--secondary p-mb-2" :label="$t('common.close')" @click="closeDialog" />
            <Button class="kn-button kn-button p-mb-2" v-t="'common.save'" @click="onSave" />
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
import { mapState, mapActions } from 'pinia'
import store from '../../Dashboard.store'
import deepcopy from 'deepcopy'
import { ISelection } from '../../Dashboard'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { Dialog, AgGridVue },
    props: {
        visible: { type: Boolean },
        dashboardId: { type: String, required: true }
    },
    emits: ['close', 'save'],
    data() {
        return {
            gridApi: null as any,
            gridColumnApi: null as any,
            gridOptions: {
                rowData: [],
                columnDefs: [
                    { headerName: 'Dataset', field: 'datasetLabel' },
                    { headerName: 'Column Name', field: 'columnName' },
                    { headerName: 'Values', field: 'value' },
                    {
                        headerName: '',
                        cellRenderer: buttonRenderer,
                        field: 'id',
                        cellStyle: {
                            'text-align': 'right',
                            display: 'inline-flex',
                            'justify-content': 'flex-end',
                            border: 'none'
                        },
                        width: 50,
                        suppressSizeToFit: true,
                        tooltip: false
                    }
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
            } as any,
            context: null as any,
            activeSelections: [] as ISelection[],
            selectionsToRemove: [] as ISelection[]
        }
    },
    computed: {
        ...mapState(store, ['dashboards'])
    },
    beforeMount() {
        this.context = { componentParent: this }
    },
    methods: {
        ...mapActions(store, ['getDashboard', 'getSelections']),
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
            this.activeSelections = deepcopy(this.getSelections(this.dashboardId))
            updateData(this.activeSelections)
        },
        deleteSelection(selection: ISelection) {
            const index = this.activeSelections.findIndex((tempSelection: ISelection) => tempSelection.datasetId === selection.datasetId && tempSelection.columnName === selection.columnName)
            if (index !== -1) {
                this.selectionsToRemove.push(this.activeSelections[index])
                this.activeSelections.splice(index, 1)
                this.gridApi.setRowData(this.activeSelections)
            }
        },
        closeDialog() {
            this.activeSelections = []
            this.selectionsToRemove = []
            this.$emit('close')
        },
        onSave() {
            this.$emit('save', this.selectionsToRemove)
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
