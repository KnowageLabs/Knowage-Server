<template>
    <div ref="discoveryContainer" class="discovery-container" v-resize="onWidgetSizeChange">
        <ProgressSpinner v-if="widgetLoading" class="kn-progress-spinner" />
        <div class="kn-width-full p-d-flex p-mb-2">
            <!-- <InputText class="kn-material-input p-m-3 model-search"  v-model="searchInput" type="text" :placeholder="$t('common.search')" @input="searchItems"  /> -->
            <Button v-if="widgetWidth < 600" :icon="burgerIcon" class="p-button-text p-button-rounded p-button-plain p-as-center" @click="toggleFacets" />
            <InputText class="kn-material-input kn-flex" v-model="searchInput" type="text" :placeholder="$t('common.search')" @input="" />
        </div>
        <div class="discovery-content">
            <div ref="facetsContainer" :class="[widgetWidth < 600 ? 'sidenav' : '']" class="facets-container dashboard-scrollbar" :style="getFacetWidth()">
                <div v-for="(facet, facetName) in facetsToDisplay" :key="facetName" class="facet-accordion">
                    <Toolbar class="kn-toolbar kn-toolbar--primary facet-accordion-header">
                        <template #start> {{ facetName }}</template>
                        <template #end>
                            <Button v-if="facet.closed" class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-down" style="color: white" @click="facet.closed = false" />
                            <Button v-else class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-up" style="color: white" @click="facet.closed = true" />
                        </template>
                    </Toolbar>
                    <div v-if="!facet.closed">
                        <div v-for="row in facet.rows.slice(0, propWidget.settings.facets.limit)" :class="{ selected: isFacetSelected(facetName, row) }" class="facet-accordion-content selectable" v-tooltip.top="facet.column_1" @click="selectFacet(facetName, row)">
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
            <div class="table-container p-ml-2">
                <ag-grid-vue v-if="!gridLoading" class="discovery-grid ag-theme-alpine kn-flex discovery-grid-scrollbar" :gridOptions="gridOptions"></ag-grid-vue>
                <PaginationRenderer class="discovery-pagination" :propWidgetPagination="propWidget.settings.pagination" @pageChanged="$emit('pageChanged')" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { emitter } from '../../DashboardHelpers'
import { mapActions } from 'pinia'
import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import { AgGridVue } from 'ag-grid-vue3' // the AG Grid Vue Component
import 'ag-grid-community/styles/ag-grid.css' // Core grid CSS, always needed
import 'ag-grid-community/styles/ag-theme-alpine.css' // Optional theme CSS
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'
import PaginationRenderer from '../TableWidget/PaginatorRenderer.vue'
import ProgressSpinner from 'primevue/progressspinner'

export default defineComponent({
    name: 'table-widget',
    emits: ['pageChanged', 'facetsChanged', 'launchSelection'],
    components: { AgGridVue, PaginationRenderer, ProgressSpinner },
    props: {
        widgetLoading: { type: Boolean, required: true },
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
                console.log('WIDGET DATA TO SHOW', this.dataToShow)
                this.tableData = this.dataToShow
                this.gridApi?.setRowData(this.tableData.rows)
                this.setFacetAccordionState()
                // this.loadActiveSelectionValue()
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    computed: {
        burgerIcon(): string {
            if (this.facetSidenavShown) return 'fas fa-x'
            else return 'fas fa-bars'
        }
    },
    data() {
        return {
            tableData: [] as any,
            activeSelections: [] as ISelection[],
            searchInput: '',
            widgetWidth: 0 as number,
            facetSidenavShown: false,
            facetsToDisplay: {} as any,
            //ag-grid-stuff
            gridColumns: [] as any[],
            gridRows: [] as any[],
            gridOptions: null as any,
            gridApi: null as any,
            columnApi: null as any,
            gridLoading: false
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    created() {
        this.tableData = this.dataToShow
        this.setEventListeners()
        this.setFacetData()
        this.loadActiveSelections()
        this.setupDatatableOptions()
        this.createColumnDefinitions(this.tableData?.metaData?.fields)
        // this.loadActiveSelectionValue()
    },
    unmounted() {
        this.removeEventListeners()
    },
    mounted() {
        this.setInitialWidgetWidth()
    },

    methods: {
        setEventListeners() {
            // emitter.on('refreshTable', this.refreshGridConfigurationWithoutData)
            // emitter.on('selectionsDeleted', this.onSelectionsDeleted)
        },
        removeEventListeners() {
            // emitter.off('refreshTable', this.refreshGridConfigurationWithoutData)
            // emitter.off('selectionsDeleted', this.onSelectionsDeleted)
        },
        setInitialWidgetWidth() {
            const temp = this.$refs['discoveryContainer'] as any
            this.widgetWidth = temp.clientHeight
        },
        loadActiveSelections() {
            this.activeSelections = this.propActiveSelections
        },
        onWidgetSizeChange({ width, height, offsetWidth, offsetHeight }) {
            this.widgetWidth = width
            if (width > 600) this.facetSidenavShown = false
        },
        //#region ===================== Facet Logic ====================================================
        setFacetData() {
            let facetSettings = this.propWidget.settings.facets
            if (facetSettings.enabled) {
                var facetKeys = this.tableData.facets ? Object.keys(this.tableData.facets) : []
                facetKeys.forEach((facetName) => {
                    if (facetSettings.columns.includes(facetName)) this.facetsToDisplay[facetName] = { ...this.tableData.facets[facetName] }
                })
                this.setFacetAccordionState()
            }
        },
        toggleFacets() {
            const temp = this.$refs['facetsContainer'] as any
            this.facetSidenavShown = !this.facetSidenavShown
            this.facetSidenavShown ? temp.classList.add('open') : temp.classList.remove('open')
        },
        isFacetSelected(facetName, row) {
            let facetSearchParams = this.propWidget.settings.search.facetSearchParams
            if (facetSearchParams[facetName] && facetSearchParams[facetName].includes(row.column_1)) return true
            else return false
        },
        getFacetWidth() {
            let facetSettings = this.propWidget.settings.facets
            if (this.widgetWidth >= 600) return { width: facetSettings.width }
            else return {}
        },
        setFacetAccordionState() {
            if (this.editorMode) return
            let facetSettings = this.propWidget.settings.facets
            let facetSearchParams = this.propWidget.settings.search.facetSearchParams

            if (facetSettings.closedByDefault) {
                Object.keys(this.facetsToDisplay).forEach((facet) => {
                    //check if facet parameter is used in filters/selection, if it is, remain opened
                    if (facetSearchParams[facet] && facetSearchParams[facet].length > 0) this.facetsToDisplay[facet].closed = false
                    else this.facetsToDisplay[facet].closed = true
                })
            }
        },
        selectFacet(facetName, row) {
            console.group('facet selected ------------------------------------')
            console.log('facetName ', facetName)
            console.log('row ', row)
            console.groupEnd()
            //if there are no values for that facet, dont even call BE
            if (row.column_2 == 0) return
            let facetSettings = this.propWidget.settings.facets
            if (facetSettings.selection) {
                //TODO: Selection logic
                //if there are any search params, empty them, now we are doing selection not search
                this.propWidget.settings.search.facetSearchParams = {}
            } else {
                let facetSearchParams = this.propWidget.settings.search.facetSearchParams
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
                // tooltipShowDelay: 100,
                // tooltipMouseTrack: true,
                suppressScrollOnNewData: true,
                animateRows: true,
                headerHeight: 35,
                rowHeight: 30,
                defaultColDef: {
                    editable: false,
                    sortable: true,
                    resizable: true,
                    width: 100
                    // tooltipComponent: TooltipRenderer,
                    // cellClassRules: {
                    //     'edited-cell-color-class': (params) => {
                    //         if (params.data.isEdited) return params.data.isEdited.includes(params.colDef.field)
                    //     }
                    // }
                },
                // CALLBACKS
                onGridReady: this.onGridReady
            }
        },
        onGridReady(params) {
            this.gridApi = params.api
            this.columnApi = params.columnApi

            this.refreshGridConfiguration()
        },
        async refreshGridConfiguration() {
            this.gridApi.setColumnDefs(this.gridColumns)
            this.gridApi.setRowData(this.tableData.rows)
        },
        async createColumnDefinitions(responseFields) {
            this.gridLoading = true
            var columns = [] as any
            var dataset = { type: 'SbiFileDataSet' }

            for (var datasetColumn in this.propWidget.columns) {
                for (var responseField in responseFields) {
                    let modelColumn = this.propWidget.columns[datasetColumn]
                    let responseColumn = responseFields[responseField]

                    if (typeof responseFields[responseField] == 'object' && ((dataset.type == 'SbiSolrDataSet' && modelColumn.alias.toLowerCase() === responseFields[responseField].header) || modelColumn.alias.toLowerCase() === responseFields[responseField].header.toLowerCase())) {
                        // console.log('responseColumn', responseColumn)
                        // console.log('modelColumn HIDE : ', modelColumn)

                        var tempCol = {
                            hide: false, //TODO: implement hide condition
                            colId: modelColumn.id,
                            headerName: modelColumn.alias,
                            columnName: modelColumn.columnName,
                            field: responseFields[responseField].name,
                            measure: modelColumn.fieldType
                        } as any

                        columns.push(tempCol)
                    }
                }
            }
            this.gridColumns = columns

            console.log(columns)
            this.gridLoading = false
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
    .discovery-search-container {
        display: flex;
        box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
        border-radius: 4px;
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
            .discovery-grid .ag-root-wrapper {
                border-bottom: none;
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
