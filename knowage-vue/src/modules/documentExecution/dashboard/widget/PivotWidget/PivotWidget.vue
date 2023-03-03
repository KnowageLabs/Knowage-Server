<template>
    <div class="pivot-widget-container p-d-flex p-d-row kn-flex">
        <DxButton text="Apply" type="default" @click="logButton()" />
        <DxPivotGrid id="pivotgrid" ref="grid" :data-source="dataSource" v-bind="pivotConfig">
            <DxFieldChooser v-bind="fieldPickerConfig" />
            <DxFieldPanel v-bind="fieldPanelConfig" />
        </DxPivotGrid>
    </div>
</template>

<script lang="ts">
import { DxPivotGrid, DxFieldChooser, DxFieldPanel } from 'devextreme-vue/pivot-grid'
import { DxButton } from 'devextreme-vue/button'
import Tooltip from 'devextreme/ui/tooltip'
import PivotGridDataSource from 'devextreme/ui/pivot_grid/data_source'

import { IDashboardDataset, ISelection, IWidget } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'

// import { sales } from './MockData.js'

export default defineComponent({
    name: 'table-widget',
    components: { DxPivotGrid, DxFieldChooser, DxFieldPanel, DxButton },
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
            pivotConfig: {} as any,
            fieldPickerConfig: {} as any,
            fieldPanelConfig: {} as any
        }
    },
    computed: {},
    watch: {
        propWidget: {
            handler() {
                console.log('PROP WIDGET CHANGED', this.propWidget)
            },
            deep: true
        },
        dataToShow: {
            handler() {
                // this.tableData = this.dataToShow
            },
            deep: true
        },
        propActiveSelections() {
            // this.loadActiveSelections()
        }
    },
    beforeMount() {},
    created() {
        this.setPivotConfiguration()
        this.setFieldPickerConfiguration()
        this.setFieldPanelConfiguration()
    },
    unmounted() {},
    mounted() {},

    methods: {
        logButton() {
            console.groupCollapsed('DO STUFF ------------')
            console.log('propWidget', this.propWidget)
            console.groupEnd()

            // console.log('dataToShow', this.dataToShow)
            // console.log('this.dataSource.fields()', this.dataSource.fields())
        },
        setPivotConfiguration() {
            const widgetConfig = this.propWidget.settings.configuration
            this.pivotConfig = {
                // PROPS
                allowSorting: true,
                allowSortingBySummary: true,
                allowFiltering: true,
                showBorders: true,
                showColumnGrandTotals: widgetConfig.columns.grandTotal,
                showColumnTotals: widgetConfig.columns.subTotal,
                showRowGrandTotals: widgetConfig.rows.grandTotal,
                showRowTotals: widgetConfig.rows.subTotal,
                texts: {},

                // EVENTS
                contentReady: this.onContentReady,
                onCellPrepared: this.setCellConfiguration
            }
        },

        setFieldPickerConfiguration() {
            const fieldPickerConfig = this.propWidget.settings.configuration.fieldPicker
            this.fieldPickerConfig = {
                enabled: fieldPickerConfig.enabled,
                width: fieldPickerConfig.width,
                height: fieldPickerConfig.height
            }
        },

        setFieldPanelConfiguration() {
            const fieldPanelConfig = this.propWidget.settings.configuration.fieldPanel
            this.fieldPanelConfig = {
                visible: fieldPanelConfig.enabled
            }
        },

        onContentReady() {
            // console.log('CONTENT READY \n', this.dataSource.state())
        },

        //#region ===================== Pivot Datasource Config (Fields & Data) ====================================================
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

                        tempField.id = modelField.id //ID FROM MODEL PASSED AS A PROPERTY
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
        //#endregion ===============================================================================================

        //#region ===================== Cell Config (Totals, Stlye, Conditionals) ====================================================
        setCellConfiguration(event) {
            if (event.area && event.cell.value == 14) {
                const pivotFields = this.dataSource.fields()
                const parentField = pivotFields[pivotFields.findIndex((field: any) => field.area === 'data' && field.areaIndex === event.cell.dataIndex)]

                console.group('cellPrep ---------------------', event.cellElement)
                console.log('CELL EVENT', event)
                console.log('PARENT FIELD', parentField)
                // console.log('FIELDS', this.dataSource.fields())
                console.groupEnd()
            }

            this.setTotals(event)
            // this.createFieldTooltips(event)
        },

        //#region ===================== Totals Config (Sub, Grand, Style) ====================================================
        setTotals(cellEvent) {
            if (cellEvent.area === 'column') this.setTotalLabels(cellEvent, 'columns')
            else if (cellEvent.area === 'row') this.setTotalLabels(cellEvent, 'rows')
        },
        setTotalLabels(cellEvent, fieldType) {
            const columnConfig = this.propWidget.settings.configuration[fieldType]

            if (cellEvent.cell.type === 'GT') cellEvent.cellElement.innerHTML = columnConfig.grandTotalLabel
            else if (cellEvent.cell.type === 'T') cellEvent.cellElement.innerHTML = columnConfig.subTotalLabel
        },
        //#endregion ===============================================================================================

        createFieldTooltips(cellEvent) {
            //TODO DARKO: add correct cell values from cellEvent
            const container = document.createElement('div')
            cellEvent.cellElement.appendChild(container)
            new Tooltip(container, {
                target: cellEvent.cellElement,
                visible: false,
                showEvent: 'mouseenter',
                hideEvent: 'mouseleave click',
                contentTemplate: function (content) {
                    const label = document.createElement('div')
                    label.innerHTML = '<b>Test</b>'
                    content.appendChild(label)
                }
            })
        }
        //#endregion ===============================================================================================
    }
})
</script>
<style lang="scss">
.pivot-widget-container {
    overflow: auto;
}
</style>
