<template>
    <div ref="discoveryContainer" v-resize="onWidgetSizeChange" class="discovery-container">
        <ProgressSpinner v-if="widgetLoading" class="kn-progress-spinner" />
        <div class="kn-width-full p-d-flex">
            <Button v-if="widgetWidth < 600 && displayFacets" :icon="burgerIcon" class="p-button-text p-button-rounded p-button-plain p-as-center" @click="toggleFacets" />
            <InputText v-if="displaySearch" v-model="searchWord" class="discovery-search kn-material-input kn-flex p-mb-2" type="text" :placeholder="$t('common.search')" @keyup.enter="searchItems" />
        </div>
        <div class="discovery-content">
            <div v-if="displayFacets" ref="facetsContainer" :class="{ sidenav: widgetWidth < 600 }" class="facets-container dashboard-scrollbar p-mr-2" :style="getFacetWidth()">
                <div v-for="(facet, facetName) in facetsToDisplay" :key="facetName" class="facet-accordion">
                    <Toolbar class="kn-toolbar kn-toolbar--primary facet-accordion-header">
                        <template #start> {{ facetName }}</template>
                        <template #end>
                            <Button v-if="facet.closed" class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-down" style="color: white" @click="facet.closed = false" />
                            <Button v-else class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-up" style="color: white" @click="facet.closed = true" />
                        </template>
                    </Toolbar>
                    <div v-if="!facet.closed">
                        <div
                            v-for="(row, index) in facet.rows.slice(0, propWidget.settings.facets.limit)"
                            :key="index"
                            v-tooltip.top="facet.column_1"
                            :class="{ selected: isFacetSelected(facetName, row), blocked: isFacetBlocked(facetName, row) }"
                            class="facet-accordion-content selectable"
                            @click="selectFacet(facetName, row)"
                        >
                            <!-- <span v-if="facet.metaData.type == 'date'" class="kn-truncated">
                                    TODO: Set Date Format
                                    {{ setTimeFormat(item.column_1, facet.metaData.dateFormat) }}
                                </span> -->
                            <span class="kn-truncated">
                                {{ row.column_1 }}
                            </span>
                            <div class="facet-chip p-ml-auto">
                                {{ row.column_2 }}
                                <!-- {{facet.column_2 | number:getDecimalPlaces("column_2", item.column_2, facet.metaData.fields)}} -->
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="table-container">
                <ag-grid-vue v-if="!gridLoading" class="discovery-grid ag-theme-alpine kn-flex discovery-grid-scrollbar" :grid-options="gridOptions"></ag-grid-vue>
                <PaginationRenderer class="discovery-pagination" :prop-widget-pagination="propWidget.settings.pagination" @page-changed="$emit('pageChanged')" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { emitter } from '../../DashboardHelpers'
import { mapActions } from 'pinia'
import { IDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import { updateStoreSelections } from '../interactionsHelpers/InteractionHelper'
import { createNewTableSelection } from '../TableWidget/TableWidgetHelper'
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'
import PaginationRenderer from '../TableWidget/PaginatorRenderer.vue'
import HeaderRenderer from '../TableWidget/HeaderRenderer.vue'
import TooltipRenderer from '../TableWidget/TooltipRenderer.vue'
import ProgressSpinner from 'primevue/progressspinner'

export default defineComponent({
    name: 'table-widget',
    components: { AgGridVue, PaginationRenderer, ProgressSpinner },
    props: {
        widgetLoading: { type: Boolean, required: true },
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as PropType<IDataset[]>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['pageChanged', 'facetsChanged', 'searchWordChanged', 'launchSelection'],
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    data() {
        return {
            tableData: [] as any,
            activeSelections: [] as ISelection[],
            searchWord: '',
            widgetWidth: 0 as number,
            facetSidenavShown: false,
            facetsToDisplay: {} as any,
            gridColumns: [] as any[],
            gridRows: [] as any[],
            gridOptions: null as any,
            gridApi: null as any,
            columnApi: null as any,
            gridLoading: false,
            selectedColumn: false as any,
            getRowId: null as any
        }
    },
    computed: {
        burgerIcon(): string {
            if (this.facetSidenavShown) return 'fas fa-x'
            else return 'fas fa-bars'
        },
        displayFacets(): boolean {
            const facetSettings = this.propWidget.settings.facets
            if (facetSettings.enabled) return true
            else return false
        },
        displaySearch(): boolean {
            const searchSettings = this.propWidget.settings.search
            if (searchSettings.enabled) return true
            else return false
        }
    },
    watch: {
        propWidget: {
            handler() {
                // console.group('PROP WIDGET PROP HANDLER ------------------------------------')
                // console.log('propWidget ', this.propWidget)
                // console.groupEnd()
                // if (!this.editorMode) this.setGridData()
            },
            deep: true
        },
        dataToShow: {
            handler() {
                console.group('DATA TO SHOW PROP HANDLER ------------------------------------')
                console.log('dataToShow ', this.dataToShow)
                console.groupEnd()

                this.reloadWidgetData()
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    created() {
        console.group('CREATED HOOK ------------------------------------')
        console.log('propWidget ', this.propWidget)
        console.log('dataToShow ', this.dataToShow)
        console.groupEnd()
        this.prepareWidget()
    },
    mounted() {
        this.setInitialWidgetWidth()
    },
    unmounted() {
        this.removeEventListeners()
    },

    methods: {
        ...mapActions(dashboardStore, ['setSelections']),
        setEventListeners() {
            if (this.editorMode) emitter.on('refreshTable', this.reloadWidgetWithoutData)
            // emitter.on('selectionsDeleted', this.onSelectionsDeleted)
        },
        removeEventListeners() {
            if (this.editorMode) emitter.off('refreshTable', this.reloadWidgetWithoutData)
            // emitter.off('selectionsDeleted', this.onSelectionsDeleted)
        },
        prepareWidget() {
            this.getRowId = (params) => params.data.id
            this.loadResponseData()
            this.createColumnDefinitions()
            this.setEventListeners()
            this.setupDatatableOptions()
            this.loadSearchValue()
            this.loadActiveSelections()
        },
        reloadWidgetData() {
            this.loadResponseData()
            this.createColumnDefinitions()
            this.setGridData()
        },
        reloadWidgetWithoutData() {
            console.group('reloadWidgetWithoutData ------------------------------------')
            console.log('propWidget ', this.propWidget)
            console.log('dataToShow ', this.dataToShow)
            console.groupEnd()

            this.createColumnDefinitions()
            this.setFacetData()
            this.gridApi.setColumnDefs(this.gridColumns)
            this.setHeaderHeight()
            this.gridApi.redrawRows()
        },
        loadResponseData() {
            this.tableData = this.dataToShow
            this.setFacetData()
        },
        loadSearchValue() {
            this.searchWord = this.propWidget.settings.search.defaultValue ?? ''
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        setInitialWidgetWidth() {
            const temp = this.$refs['discoveryContainer'] as any
            this.widgetWidth = temp.clientHeight
        },
        onWidgetSizeChange({ width }) {
            this.widgetWidth = width
            if (width > 600) this.facetSidenavShown = false
        },
        searchItems() {
            if (this.editorMode) return
            const searchSettings = this.propWidget.settings.search
            if (searchSettings.columns.length > 0) {
                searchSettings.searchWord = this.searchWord
                this.$emit('searchWordChanged')
            }
        },
        //#region ===================== Facet Logic ====================================================
        getFacetWidth() {
            const facetSettings = this.propWidget.settings.facets
            if (this.widgetWidth >= 600) return { width: facetSettings.width }
            else return {}
        },
        setFacetData() {
            if (this.displayFacets && this.tableData.facets) {
                const facetSettings = this.propWidget.settings.facets

                const facetKeys = Object.keys(this.tableData.facets)
                if (facetKeys) {
                    facetKeys.forEach((facetName) => {
                        if (facetSettings.columns.includes(facetName)) {
                            this.facetsToDisplay[facetName] = { ...this.tableData.facets[facetName] }
                        } else delete this.facetsToDisplay[facetName]
                    })
                    this.setFacetAccordionState()
                }
            }
        },
        setFacetAccordionState() {
            const facetSettings = this.propWidget.settings.facets
            const facetSearchParams = this.propWidget.settings.search.facetSearchParams

            if (facetSettings.closedByDefault) {
                Object.keys(this.facetsToDisplay).forEach((facet) => {
                    if (this.editorMode) {
                        this.facetsToDisplay[facet].closed = true
                        return
                    }
                    if (facetSearchParams[facet] && facetSearchParams[facet].length > 0) this.facetsToDisplay[facet].closed = false
                    else this.facetsToDisplay[facet].closed = true
                })
            }
        },
        isFacetSelected(facetName, row) {
            const facetSearchParams = this.propWidget.settings.search.facetSearchParams
            if (facetSearchParams[facetName] && facetSearchParams[facetName].includes(row.column_1)) return true
            else return false
        },
        isFacetBlocked(facetName, row) {
            const facetSearchParams = this.propWidget.settings.search.facetSearchParams
            const facetBrotherSelected = facetSearchParams[facetName] && facetSearchParams[facetName].length > 0 && !facetSearchParams[facetName].includes(row.column_1)

            if (row.column_2 == 0 || facetBrotherSelected) return true
            else return false
        },
        toggleFacets() {
            const temp = this.$refs['facetsContainer'] as any
            this.facetSidenavShown = !this.facetSidenavShown
            this.facetSidenavShown ? temp.classList.add('open') : temp.classList.remove('open')
        },
        selectFacet(facetName, row) {
            if (this.isFacetBlocked(facetName, row)) return
            const facetSettings = this.propWidget.settings.facets
            if (facetSettings.selection) {
                //if there are any search params, empty them, now we are doing selection not search
                // eslint-disable-next-line vue/no-mutating-props
                this.propWidget.settings.search.facetSearchParams = {}
                //TODO: Selection logic
                updateStoreSelections(createNewTableSelection([row['column_1']], facetName, this.propWidget, this.datasets), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
            } else {
                const facetSearchParams = this.propWidget.settings.search.facetSearchParams
                if (facetSearchParams[facetName] && !facetSearchParams[facetName].includes(row.column_1)) {
                    facetSearchParams[facetName].push(row.column_1)
                } else if (facetSearchParams[facetName] && facetSearchParams[facetName].includes(row.column_1)) {
                    const index = facetSearchParams[facetName].indexOf(row.column_1)
                    facetSearchParams[facetName].splice(index, 1)
                } else {
                    facetSearchParams[facetName] = [row.column_1]
                }

                this.$emit('facetsChanged')
            }
        },
        //#endregion ================================================================================================

        //#region ===================== Ag Grid Logic ====================================================
        setupDatatableOptions() {
            this.gridOptions = {
                // PROPERTIES
                tooltipShowDelay: 100,
                tooltipMouseTrack: true,
                suppressScrollOnNewData: true,
                animateRows: true,
                headerHeight: 35,
                rowHeight: 30,
                defaultColDef: {
                    width: 150
                },
                // EVENTS
                onCellClicked: this.onCellClicked,
                // CALLBACKS
                onGridReady: this.onGridReady,
                getRowHeight: this.getRowHeight,
                getRowStyle: this.getRowStyle,
                getRowId: this.getRowId
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            console.group('ON GRID READY HOOK ------------------------------------')
            console.log('propWidget ', this.propWidget)
            console.log('dataToShow ', this.dataToShow)
            console.groupEnd()

            this.setGridData()
            this.setHeaderHeight()
        },
        setHeaderHeight() {
            const headerConfig = this.propWidget.settings.style.headers
            this.gridApi?.setHeaderHeight(headerConfig.height)
        },
        getRowHeight() {
            const rowsConfiguration = this.propWidget.settings.style.rows
            if (rowsConfiguration.height && rowsConfiguration.height != 0) return rowsConfiguration.height
            else return 25
        },
        getRowStyle(params) {
            const rowStyles = this.propWidget.settings.style.rows

            if (rowStyles.alternatedRows && rowStyles.alternatedRows.enabled) {
                if (rowStyles.alternatedRows.oddBackgroundColor && params.node.rowIndex % 2 === 0) {
                    return { background: rowStyles.alternatedRows.oddBackgroundColor }
                }
                if (rowStyles.alternatedRows.evenBackgroundColor && params.node.rowIndex % 2 != 0) {
                    return { background: rowStyles.alternatedRows.evenBackgroundColor }
                }
            }
        },
        async setGridData() {
            this.gridApi.setRowData(this.tableData.rows)
            this.gridApi.setColumnDefs(this.gridColumns)
        },
        async createColumnDefinitions() {
            this.gridLoading = true
            const columns = [] as any
            const dataset = { type: 'SbiFileDataSet' }
            const responseFields = this.tableData?.metaData?.fields

            for (const datasetColumn in this.propWidget.columns) {
                for (const responseField in responseFields) {
                    const modelColumn = this.propWidget.columns[datasetColumn]
                    // const responseColumn = responseFields[responseField]

                    if (typeof responseFields[responseField] == 'object' && ((dataset.type == 'SbiSolrDataSet' && modelColumn.alias.toLowerCase() === responseFields[responseField].header) || modelColumn.alias.toLowerCase() === responseFields[responseField].header.toLowerCase())) {
                        // console.log('responseColumn', responseColumn)
                        // console.log('modelColumn HIDE : ', modelColumn)

                        const tempCol = {
                            hide: false, //TODO: implement hide condition
                            colId: modelColumn.id,
                            headerName: modelColumn.alias,
                            columnName: modelColumn.columnName,
                            field: responseFields[responseField].name,
                            measure: modelColumn.fieldType,
                            headerComponent: HeaderRenderer,
                            headerComponentParams: { propWidget: this.propWidget },
                            suppressMovable: true
                        } as any

                        // COLUMN STYLE ---------------------------------------------------------------------------
                        const columnStyles = this.propWidget.settings.style.columns

                        if (columnStyles.enabled) {
                            let columnStyleString = null as any
                            // eslint-disable-next-line @typescript-eslint/no-unused-vars
                            columnStyleString = Object.entries(columnStyles.styles[0].properties)
                                .map(([k, v]) => `${k}:${v}`)
                                .join(';')

                            columnStyles.styles.forEach((group) => {
                                if (group.target.includes(tempCol.colId)) {
                                    columnStyleString = Object.entries(group.properties)
                                        .map(([k, v]) => `${k}:${v}`)
                                        .join(';')
                                }
                            })

                            tempCol.cellStyle = () => {
                                return columnStyles.styles[0].properties
                            }
                        }

                        // TOOLTIP CONFIGURATION  -----------------------------------------------------------------
                        const tooltipConfig = this.getColumnTooltipConfig(tempCol.colId)
                        if (tooltipConfig !== null) {
                            tempCol.tooltipComponent = TooltipRenderer
                            tempCol.tooltipField = tempCol.field
                            tempCol.headerTooltip = tooltipConfig.header.enabled ? tooltipConfig.header.text : null
                            tempCol.tooltipComponentParams = { tooltipConfig: tooltipConfig }
                        } else {
                            tempCol.headerTooltip = null
                        }

                        columns.push(tempCol)
                    }
                }
            }
            this.gridColumns = columns

            this.gridLoading = false
        },
        getColumnTooltipConfig(colId) {
            const tooltipConfig = this.propWidget.settings.tooltips
            let columntooltipConfig = null as any
            tooltipConfig[0].enabled ? (columntooltipConfig = tooltipConfig[0]) : ''
            tooltipConfig.forEach((config) => {
                config.target.includes(colId) ? (columntooltipConfig = config) : ''
            })

            return columntooltipConfig
        },
        onCellClicked(node) {
            if (!this.editorMode) {
                if (node.colDef.measure == 'MEASURE' || node.colDef.pinned || node.value === '' || node.value == undefined) return

                updateStoreSelections(createNewTableSelection([node.value], node.colDef.columnName, this.propWidget, this.datasets), this.activeSelections, this.dashboardId, this.setSelections, this.$http)
                this.gridApi?.refreshCells({ force: true })
            }
        }

        //#endregion ================================================================================================
    }
})
</script>
<style lang="scss">
.discovery-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: auto;
    .discovery-search {
        border: 1px solid var(--kn-color-borders);
        border-radius: 2px;
    }
    .discovery-content {
        position: relative;
        flex: 1;
        display: flex;
        flex-direction: row;
        overflow: auto;
        .table-container {
            flex: 2;
            display: flex;
            flex-direction: column;
            .discovery-grid {
                .ag-root-wrapper {
                    border-bottom: none;
                }
                .ag-header-cell-comp-wrapper {
                    height: 100%;
                    display: flex;
                }
                .ag-row,
                .ag-header-cell,
                .ag-cell-value,
                .ag-header-group-cell,
                .ag-floating-bottom-container .ag-cell {
                    padding: 0 !important;
                    border: none;
                }
                .custom-header-container,
                .custom-header-group-container {
                    display: flex;
                    align-items: center;
                    width: 100%;
                    height: 100%;
                }
            }
            .discovery-pagination {
                border-left: 1px solid #babfc7;
                border-right: 1px solid #babfc7;
                border-bottom: 1px solid #babfc7;
                padding: 0 !important;
                border-radius: 0;
            }
        }
        .facets-container {
            display: flex;
            flex-direction: column;
            overflow: auto;
            border: 1px solid #babfc7;
            background-color: #fff;
            color: black;
            transition: flex 0.3s linear;
            &.sidenav {
                border: none;
                width: 0px;
                height: 100%;
                position: absolute;
                margin-left: 0;
                margin-right: 0;
                transition: width 0.25s linear;
                z-index: 999;
                &.open {
                    width: 50%;
                    border: 1px solid rgba(172, 172, 172, 0.8);
                }
            }
            .facet-accordion {
                margin-bottom: 1px;
                .facet-accordion-header {
                    text-transform: none;
                }
                .facet-accordion-content {
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #ccc;
                    padding: 0 8px;
                    min-height: 24px;
                    height: 24px;
                    .facet-chip {
                        background-color: rgb(238, 238, 238);
                        border: 1px solid #ccc;
                        border-radius: 15px;
                        min-width: 30px;
                        padding: 0 4px;
                        text-align: center;
                        font-size: 0.8rem;
                    }
                }
                .selectable {
                    &:hover {
                        background-color: #eceff1;
                    }
                    &.selected {
                        background-color: #ced1d3;
                    }
                    &.blocked {
                        cursor: not-allowed;
                    }
                }
            }
        }
    }
    // has to be here, dashboard-scrollbar class doesnt work for some reason
    .discovery-grid-scrollbar {
        ::-webkit-scrollbar {
            width: 5px;
            height: 5px;
        }
        ::-webkit-scrollbar-track {
            background: #f1f1f1;
        }
        ::-webkit-scrollbar-thumb {
            background: #888;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: #555;
        }
    }
}
</style>
