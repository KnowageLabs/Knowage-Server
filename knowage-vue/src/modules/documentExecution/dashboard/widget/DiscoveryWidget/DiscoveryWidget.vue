<template>
    <div>
        {{ tableData }}
    </div>
</template>

<script lang="ts">
import { emitter } from '../../DashboardHelpers'
import { mapActions } from 'pinia'
import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'

export default defineComponent({
    name: 'table-widget',
    emits: ['pageChanged', 'sortingChanged', 'launchSelection'],
    components: {},
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    watch: {
        propWidget: {
            handler() {
                // if (!this.editorMode) this.refreshGridConfiguration(true)
            },
            deep: true
        },
        dataToShow: {
            handler() {
                this.tableData = this.dataToShow
                console.log('WIDGET DATA TO SHOW', this.dataToShow)
                // this.refreshGridConfiguration(true)
                // this.loadActiveSelectionValue()
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    data() {
        return {
            gridOptions: null as any,
            columnsNameArray: [] as any,
            rowData: [] as any,
            columnDefs: [] as any,
            gridApi: null as any,
            columnApi: null as any,
            overlayNoRowsTemplateTest: null as any,
            tableData: [] as any,
            showPaginator: false,
            activeSelections: [] as ISelection[],
            multiSelectedCells: [] as any,
            selectedColumn: false as any,
            selectedColumnArray: [] as any,
            context: null as any
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    beforeMount() {
        this.context = { componentParent: this }
    },
    created() {
        this.setEventListeners()
        this.loadActiveSelections()
        // this.setupDatatableOptions()
        // this.loadActiveSelectionValue()
        this.tableData = this.dataToShow
    },
    unmounted() {
        this.removeEventListeners()
    },
    mounted() {},

    methods: {
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        setEventListeners() {
            // emitter.on('refreshTable', this.refreshGridConfigurationWithoutData)
            // emitter.on('selectionsDeleted', this.onSelectionsDeleted)
        },
        removeEventListeners() {
            // emitter.off('refreshTable', this.refreshGridConfigurationWithoutData)
            // emitter.off('selectionsDeleted', this.onSelectionsDeleted)
        }
    }
})
</script>
<style lang="scss"></style>
