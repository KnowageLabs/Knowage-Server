<template>
    <div ref="discoveryContainer" class="discovery-container p-m-2" v-resize="onWidgetSizeChange">
        <div class="kn-width-full p-d-flex">
            <!-- <InputText class="kn-material-input p-m-3 model-search"  v-model="searchInput" type="text" :placeholder="$t('common.search')" @input="searchItems"  /> -->
            <Button v-if="widgetWidth < 600" :icon="burgerIcon" class="p-button-text p-button-rounded p-button-plain p-as-center" @click="toggleFacets" />
            <InputText class="kn-material-input p-mx-2 p-my-1 kn-flex" v-model="searchInput" type="text" :placeholder="$t('common.search')" @input="" />
        </div>
        <div class="discovery-content">
            <div ref="facetsContainer" :class="[widgetWidth < 600 ? 'sidenav' : '']" class="facets-container dashboard-scrollbar p-m-2" :style="getFacetWidth()">
                <div v-for="(facet, facetName) in tableData.facets" :key="facetName" class="facet-accordion">
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
            <div class="table-container p-m-2">tableTest: {{ widgetWidth }}</div>
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
            facetsToDisplay: []
        }
    },
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    beforeMount() {},
    created() {
        this.tableData = this.dataToShow
        this.setEventListeners()
        this.loadActiveSelections()
        // this.setupDatatableOptions()
        // this.loadActiveSelectionValue()
        this.setFacetAccordionState()
    },
    unmounted() {
        this.removeEventListeners()
    },
    mounted() {
        this.setInitialWidgetWidth()
    },

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
        setInitialWidgetWidth() {
            const temp = this.$refs['discoveryContainer'] as any
            this.widgetWidth = temp.clientHeight
        },
        onWidgetSizeChange({ width, height, offsetWidth, offsetHeight }) {
            this.widgetWidth = width
            if (width > 600) this.facetSidenavShown = false
        },
        toggleFacets() {
            const temp = this.$refs['facetsContainer'] as any
            this.facetSidenavShown = !this.facetSidenavShown
            this.facetSidenavShown ? temp.classList.add('open') : temp.classList.remove('open')
        },

        //#region ===================== Facet Logic ====================================================
        isFacetSelected(facetName, row) {
            let facetSearchParams = this.propWidget.settings.search.facetSearchParams
            if (facetSearchParams[facetName] && facetSearchParams[facetName].includes(row.column_1)) return true
            else return false
        },
        getFacetAlias(facetIndex) {
            var facetKeys = Object.keys(this.tableData.facets)
            return facetKeys[facetIndex]
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
                Object.keys(this.tableData.facets).forEach((facet) => {
                    //check if facet parameter is used in filters/selection, if it is, remain opened
                    if (facetSearchParams[facet]) this.tableData.facets[facet].closed = false
                    else this.tableData.facets[facet].closed = true
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
                //if there are any search params, empty them, now we are doing selection not search
                this.propWidget.settings.search.facetSearchParams = {}
                //TODO: Selection logic
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
            // flex: 1;
            &.sidenav {
                // max-width: 0;
                transition: width 0.25s linear;
                position: absolute;
                margin-left: 0;
                margin-right: 0;
                width: 0px;
                height: calc(100% - 75px);
                &.open {
                    width: 50%;
                    border: 1px solid rgba(172, 172, 172, 0.8);
                }
            }
            display: flex;
            flex-direction: column;
            overflow: auto;
            background-color: #fff;
            color: black;
            box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 1px 3px 0 rgb(0 0 0 / 12%);
            // border-radius: 4px;
            transition: flex 0.3s linear;
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
}
</style>
