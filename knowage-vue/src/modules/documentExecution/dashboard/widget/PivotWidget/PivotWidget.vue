<template>
    <div class="pivot-widget-container p-d-flex p-d-row kn-flex">
        <DxPivotGrid id="pivotgrid" ref="grid" :data-source="dataSource" v-bind="pivotConfig" @initialized="onGridInitialization">
            <DxFieldChooser v-bind="fieldPickerConfig" />
            <DxFieldPanel v-bind="fieldPanelConfig" />
        </DxPivotGrid>
    </div>
</template>

<script lang="ts">
import { DxPivotGrid, DxFieldChooser, DxFieldPanel } from 'devextreme-vue/pivot-grid'
import Tooltip from 'devextreme/ui/tooltip'
import PivotGridDataSource from 'devextreme/ui/pivot_grid/data_source'
import { IDashboardDataset, ISelection, IWidget, ITableWidgetColumnStyles, ITableWidgetConditionalStyles } from '../../Dashboard'
import { defineComponent, PropType } from 'vue'
import mainStore from '../../../../../App.store'
import dashboardStore from '../../Dashboard.store'
import { getColumnConditionalStyles } from '../TableWidget/TableWidgetHelper'

import { getWidgetStyleByType, stringifyStyleProperties } from '../TableWidget/TableWidgetHelper'
import { IPivotTooltips } from '../../interfaces/pivotTable/DashboardPivotTableWidget'

export default defineComponent({
    name: 'table-widget',
    components: { DxPivotGrid, DxFieldChooser, DxFieldPanel },
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
            fieldPanelConfig: {} as any,
            gridInstance: null as any
        }
    },
    computed: {
        dataFields() {
            return this.dataSource.fields().filter((field) => field.area == 'data')
        },
        pivotFields() {
            return this.gridInstance.getDataSource()._descriptions
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
        setPivotConfiguration() {
            const widgetConfig = this.propWidget.settings.configuration
            this.pivotConfig = {
                // PROPS
                allowExpandAll: true,
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
                onCellPrepared: this.setCellConfiguration,
                onCellClick: this.onCellClicked
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
        onGridInitialization(event) {
            console.log('INIT EVENT', event.component)
            this.gridInstance = event.component
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
                        const index = responseMetadataFields.findIndex((metaField: any) => {
                            if (typeof metaField == 'object') return metaField.header.toLowerCase() === modelField.alias.toLowerCase()
                        })

                        tempField.id = modelField.id //ID FROM MODEL PASSED AS A PROPERTY
                        //TODO: split tempField props to methods
                        tempField.caption = modelField.alias
                        tempField.dataField = `column_${index}`
                        tempField.area = this.getDataField(fieldsName)
                        if (modelField.sort) tempField.sortOrder = modelField.sort.toLowerCase()

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
                case 'filters':
                    return 'filter'
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
            // if (event.cell.text == 'UNITS_ORDERED') {
            //     console.group('cellPrep ---------------------', event.cellElement)
            //     console.log('CELL EVENT', event)
            //     console.log('CELL EVENT', pivotFields[event.cell.dataSourceIndex])
            //     console.groupEnd()
            // }

            // if ((event.area == 'row' || event.area == 'data') && event.rowIndex % 2 === 0) {
            //     event.cellElement.style = 'background-color: grey; color: orange'
            // }

            // if (event.area == 'row' || event.area == 'column' || !this.isTotalCell) {
            //     event.cellElement.style = 'background-color: grey; color: orange'
            // }

            this.setTotals(event)
            this.setTooltips(event)
            this.setFieldStyles(event)
            this.setHeaderStyles(event) //TODO: Does it need to exist now that we can target specific fields?
        },
        //#endregion ===============================================================================================

        //#region ===================== Totals Config (Sub, Grand, Style) ====================================================
        setTotals(cellEvent) {
            this.setTotalStyles(cellEvent)

            if (cellEvent.area === 'data') return

            const fieldType = cellEvent.area === 'column' ? 'columns' : 'rows'
            this.setTotalLabels(cellEvent, fieldType)
        },
        setTotalLabels(cellEvent, fieldType) {
            const columnConfig = this.propWidget.settings.configuration[fieldType]

            if (cellEvent.cell.type === 'GT') cellEvent.cellElement.innerHTML = columnConfig.grandTotalLabel
            else if (cellEvent.cell.type === 'T') cellEvent.cellElement.innerHTML = columnConfig.subTotalLabel
        },
        setTotalStyles(cellEvent) {
            let totalType = null as any
            const cell = cellEvent.cell

            if (cell.type === 'GT' || cell.rowType === 'GT' || cell.columnType === 'GT') totalType = 'totals'
            else if (cell.type === 'T' || cell.rowType === 'T' || cell.columnType === 'T') totalType = 'subTotals'

            const styleConfig = getWidgetStyleByType(this.propWidget, totalType)
            cellEvent.cellElement.style = styleConfig
        },
        //#endregion ===============================================================================================

        //#region ===================== Tooltips Config  ====================================================
        setTooltips(cellEvent) {
            const tooltipsConfig = this.propWidget.settings.tooltips as IPivotTooltips[]
            const parentField = this.getCellParent(cellEvent)

            let cellTooltipConfig = null as unknown as IPivotTooltips
            if (parentField?.id && tooltipsConfig.length > 1) cellTooltipConfig = tooltipsConfig.find((tooltipConfig) => tooltipConfig.target.includes(parentField.id)) as IPivotTooltips
            else if (tooltipsConfig[0].enabled) cellTooltipConfig = tooltipsConfig[0] as IPivotTooltips

            if (cellTooltipConfig) this.createFieldTooltips(cellEvent, cellTooltipConfig)
        },
        createFieldTooltips(cellEvent, tooltipConfig: IPivotTooltips) {
            const container = document.createElement('div')
            cellEvent.cellElement.appendChild(container)
            new Tooltip(container, {
                target: cellEvent.cellElement,
                visible: false,
                showEvent: 'mouseenter',
                hideEvent: 'mouseleave click',
                contentTemplate: function (content) {
                    const label = document.createElement('div')
                    if (cellEvent.area == 'data') {
                        label.innerHTML = `<b>${tooltipConfig.prefix} ${cellEvent.cell.text} ${tooltipConfig.suffix}</b>`
                        content.appendChild(label)
                    } else {
                        label.innerHTML = `<b>${tooltipConfig.header.enabled ? tooltipConfig.header.text : cellEvent.cell.text}</b>`
                        content.appendChild(label)
                    }
                }
            })
        },
        //#endregion ===============================================================================================

        //#region ===================== Field Styles: TODO: Possibly split methods? Maybe no need.  ====================================================
        setFieldStyles(cellEvent) {
            if (this.isTotalCell(cellEvent)) return

            const parentField = this.getCellParent(cellEvent)
            const conditionalStyles = this.propWidget.settings.conditionalStyles as ITableWidgetConditionalStyles
            let fieldStyles = null as unknown as ITableWidgetColumnStyles
            let fieldStyleString = null as any

            if (cellEvent.area == 'data') fieldStyles = this.propWidget.settings.style.fields
            else fieldStyles = this.propWidget.settings.style.fieldHeaders

            if (!fieldStyles.enabled || !parentField) return

            //All Field Styles
            fieldStyleString = stringifyStyleProperties(fieldStyles.styles[0].properties)

            //Specific Field Styles
            const fieldStyle = fieldStyles.styles.find((fieldStyle) => fieldStyle.target.includes(parentField.id))
            if (fieldStyle) fieldStyleString = stringifyStyleProperties(fieldStyle.properties)

            //Conditional Styles
            if (cellEvent.area == 'data' && conditionalStyles.enabled) {
                const cellConditionalStyle = getColumnConditionalStyles(this.propWidget, parentField?.id, cellEvent.cell.text)
                if (cellConditionalStyle) fieldStyleString = stringifyStyleProperties(cellConditionalStyle)
                if (cellConditionalStyle && cellConditionalStyle.icon) cellEvent.cellElement.innerHTML += `<i class="${cellConditionalStyle.icon} p-ml-1"/>`
            }

            cellEvent.cellElement.style = fieldStyleString
        },
        getCellParent(cellEvent) {
            if (this.isTotalCell(cellEvent)) return undefined
            if (cellEvent.area == 'data' || (cellEvent.area == 'column' && cellEvent.cell.dataIndex >= 0)) {
                return this.pivotFields.values[cellEvent.cell.dataIndex]
            }
            if (cellEvent.area == 'column' && !cellEvent.cell.dataIndex) {
                const fieldIndex = cellEvent.cell.path.findIndex((pathElement) => pathElement == cellEvent.cell.text)
                return this.pivotFields.columns[fieldIndex]
            }
            if (cellEvent.area == 'row' && !cellEvent.cell.dataIndex) {
                const fieldIndex = cellEvent.cell.path.findIndex((pathElement) => pathElement == cellEvent.cell.text)
                return this.pivotFields.rows[fieldIndex]
            }
        },
        isTotalCell(cellEvent) {
            return cellEvent.cell.type === 'GT' || cellEvent.cell.rowType === 'GT' || cellEvent.cell.columnType === 'GT' || cellEvent.cell.type === 'T' || cellEvent.cell.rowType === 'T' || cellEvent.cell.columnType === 'T'
        },
        //#endregion ===============================================================================================

        //#region ===================== Header Styles  ====================================================
        setHeaderStyles(cellEvent) {
            // let headerStyles = null as unknown as IPivotTableColumnHeadersStyle
            let headerStyles = null as any

            // const parentField = this.dataFields[cellEvent.cell.dataIndex]
            const isDataColumn = cellEvent.area == 'data' || (cellEvent.area == 'column' && cellEvent.cell.dataIndex >= 0)
            let headerStylestring = null as any

            if (cellEvent.area == 'column') headerStyles = this.propWidget.settings.style.columnHeaders
            else if (cellEvent.area == 'row') headerStyles = this.propWidget.settings.style.rowHeaders

            if (cellEvent.area == 'data' || this.isTotalCell(cellEvent) || isDataColumn || !headerStyles.enabled) return

            headerStylestring = stringifyStyleProperties(headerStyles.properties)
            cellEvent.cellElement.style = headerStylestring
        },
        //#endregion ===============================================================================================

        //#region ===================== Cell Click Events  ====================================================
        onCellClicked(cellEvent) {
            console.group('CELL CLICKED ---------------------', cellEvent.cellElement)
            console.log('event', cellEvent)
            console.log('pivotFields', this.pivotFields)
            console.log('this.dataFields[cellEvent.cell.dataIndex]', this.dataFields[cellEvent.cell.dataIndex])
            console.log('this.gridInstance', this.gridInstance.getDataSource())
            console.groupEnd()
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
