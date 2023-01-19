<template>
    <div class="discovery-container p-m-2">
        <span class="discovery-search-container p-m-2">
            <!-- <InputText class="kn-material-input p-m-3 model-search"  v-model="test" type="text" :placeholder="$t('common.search')" @input="searchItems"  /> -->
            <InputText class="kn-material-input p-mx-2 p-my-1 kn-flex" v-model="test" type="text" :placeholder="$t('common.search')" @input="" />
        </span>
        <div class="discovery-content">
            <div class="facets-container dashboard-scrollbar p-m-2">
                <div v-for="(facet, index) in tableData.facets" :key="index" class="facet-accordion">
                    <Toolbar class="kn-toolbar kn-toolbar--primary facet-accordion-header">
                        <template #start> {{ index }}</template>
                        <template #end>
                            <Button v-if="facet.closed" class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-down" style="color: white" @click="facet.closed = false" />
                            <Button v-else class="p-button-text p-button-rounded p-button-plain" icon="fas fa-chevron-up" style="color: white" @click="facet.closed = true" />
                        </template>
                    </Toolbar>
                    <div v-if="!facet.closed">
                        <div v-for="(row, index) in facet.rows" class="facet-accordion-content selectable" v-tooltip.top="facet.column_1">
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
            <div class="table-container p-m-2">tableTest</div>
        </div>
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
            context: null as any,
            test: ''
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
        },
        getFacetAlias(facetIndex) {
            var facetKeys = Object.keys(this.tableData.facets)
            return facetKeys[facetIndex]
        }
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
        flex: 1;
        display: flex;
        flex-direction: row;
        overflow: auto;
        .table-container {
            flex: 2;
        }
        .facets-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            overflow: auto;
            background-color: #fff;
            color: black;
            box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
            border-radius: 4px;
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
                        .chip {
                        }
                    }
                }
            }
        }
    }
}
</style>
