<template>
    <div class="pivot-widget-container p-d-flex p-d-row kn-flex">
        <DxButton text="Apply" type="default" @click="doStuff()" />
        <DxPivotGrid id="pivotgrid" ref="grid" :data-source="dataSource" v-bind="pivotConfig">
            <DxFieldChooser :enabled="true" :height="400" />
        </DxPivotGrid>
    </div>
</template>

<script lang="ts">
import { DxPivotGrid, DxFieldChooser } from 'devextreme-vue/pivot-grid'
import { DxButton } from 'devextreme-vue/button'
import PivotGridDataSource from 'devextreme/ui/pivot_grid/data_source'

import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'

// import { sales } from './MockData.js'

export default defineComponent({
    name: 'table-widget',
    components: { DxPivotGrid, DxFieldChooser, DxButton },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        editorMode: { type: Boolean, required: false },
        datasets: { type: Array as PropType<IDashboardDataset[]>, required: true },
        dataToShow: { type: Object as any, required: true },
        propActiveSelections: { type: Array as PropType<ISelection[]>, required: true },
        dashboardId: { type: String, required: true }
    },
    emits: ['pageChanged', 'sortingChanged', 'launchSelection'],
    setup() {
        const store = dashboardStore()
        const appStore = mainStore()
        return { store, appStore }
    },
    data() {
        const dataSource = new PivotGridDataSource({
            fields: this.getFormattedFieldsFromModel(),
            store: this.getPivotData()
        })
        return {
            dataSource,
            tableData: [] as any,
            pivotConfig: {} as any
        }
    },
    watch: {
        propWidget: {
            handler() {
                console.log('PROP WIDGET CHANGED', this.propWidget)
            },
            deep: true
        },
        dataToShow: {
            handler() {
                this.tableData = this.dataToShow
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    beforeMount() {},
    created() {
        this.pivotConfig.onCellPrepared = this.setCellConfiguration
    },
    unmounted() {},
    mounted() {},

    methods: {
        createPivotConfiguration() {
            this.pivotConfig = {
                // PROPS
                allowSorting: true,
                allowSortingBySummary: true,
                allowFiltering: true,
                showBorders: true,
                showColumnGrandTotals: true,
                showColumnTotals: true,
                showRowGrandTotals: true,
                showRowTotals: true,
                texts: {
                    grandTotal: 'TEST 123',
                    total: 'TOTAL TEST'
                },

                // EVENTS
                contentReady: this.onContentReady,
                cellPrepared: this.setCellConfiguration
            }
        },
        onContentReady() {
            // console.log('CONTENT READY \n', this.dataSource.state())
        },
        doStuff() {
            console.groupCollapsed('DO STUFF ------------')
            console.log('propWidget', this.propWidget)
            console.groupEnd()
            // console.log('dataToShow', this.dataToShow)
            // console.log('this.dataSource.fields()', this.dataSource.fields())
        },
        getFields() {
            return [
                {
                    caption: 'Region',
                    width: 120,
                    dataField: 'region',
                    area: 'row',
                    headerFilter: {
                        allowSearch: true
                    }
                },
                {
                    caption: 'City',
                    dataField: 'city',
                    width: 150,
                    area: 'row',
                    headerFilter: {
                        allowSearch: true
                    },
                    selector(data) {
                        return `${data.city} (${data.country})`
                    }
                },
                {
                    dataField: 'date',
                    dataType: 'date',
                    area: 'column'
                },
                {
                    caption: 'Sales',
                    dataField: 'amount',
                    dataType: 'number',
                    summaryType: 'sum',
                    format: 'currency',
                    area: 'data'
                }
            ]
        },
        getFormattedFieldsFromModel() {
            const formattedFields = [] as any
            const responseMetadataFields = this.dataToShow?.metaData?.fields

            if (this.getPivotData().length > 0) {
                for (const fieldsName in this.propWidget.fields) {
                    const modelFields = this.propWidget.fields[fieldsName]
                    modelFields.forEach((modelField) => {
                        const tempField = {} as any
                        // console.log('FIELD', modelField)

                        const index = responseMetadataFields.findIndex((metaField: any) => {
                            if (typeof metaField == 'object') return metaField.header.toLowerCase() === modelField.alias.toLowerCase()
                        })

                        //TODO: split tempField props to methods
                        tempField.caption = modelField.alias
                        tempField.dataField = `column_${index}`
                        tempField.area = this.getDataField(fieldsName)
                        if (modelField.sort) tempField.sortOrder = modelField.sort.toLowerCase()

                        // console.log('INDEX FOUND', index, tempField)

                        formattedFields.push(tempField)
                    })
                }
            }

            // console.log('formattedFields', formattedFields)
            return formattedFields
        },
        getDataField(fieldsName) {
            switch (fieldsName) {
                case 'columns':
                    return 'column'
                case 'rows':
                    return 'row'
                default:
                    return fieldsName
            }
        },
        getPivotData() {
            if (this.dataToShow && this.dataToShow.rows) return this.dataToShow.rows
            else return []
        },
        setCellConfiguration(event) {
            // console.log('cell prep,', event)
            if (event.area === 'row' && event.cell.text === 'Grand Total') {
                event.cellElement.innerHTML = this.getGrandTotalLabel('rows')
            }
            if (event.area === 'column' && event.cell.text === 'Grand Total') {
                event.cellElement.innerHTML = this.getGrandTotalLabel('columns')
            }
        },
        getGrandTotalLabel(totalType) {
            if (totalType === 'rows' || totalType === 'columns') {
                const grandTotalLabel = this.propWidget.settings.configuration[totalType].grandTotalLabel
                return grandTotalLabel
            } else return 'Grand Total'
        }
    }
})
</script>
<style lang="scss">
.pivot-widget-container {
    overflow: auto;
}
</style>
