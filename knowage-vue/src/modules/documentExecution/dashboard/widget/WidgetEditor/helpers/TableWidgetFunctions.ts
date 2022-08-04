import { IWidget, IWidgetColumn, IIcon } from "../../../Dashboard"
import { formatRGBColor } from './WidgetEditorHelpers'
import { emitter } from '../../../DashboardHelpers'
import descriptor from '../WidgetEditorDescriptor.json'

const tableWidgetFunctions = {
    itemsPerPageIsDisabled: (model: IWidget) => {
        return !model.settings?.pagination?.enabled
    },
    getColumnIcons: (column: any) => {
        return column?.fieldType === 'ATTRIBUTE' ? 'fas fa-font' : 'fas fa-hashtag'
    },
    onColumnDrop: (event: any, model: IWidget) => {
        if (event.dataTransfer.getData('text/plain') === 'b') return
        const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
        const tempColumn = createNewWidgetColumn(eventData)
        model.columns.push(tempColumn)
        emitter.emit('collumnAdded', eventData)
    },
    updateColumnVisibility: (column: IWidgetColumn, model: IWidget) => {
        const index = model.columns?.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
        if (index !== -1 && model.columns[index] && model.columns[index].style) model.columns[index].style.hiddenColumn = !model.columns[index].style.hiddenColumn
    },
    removeColumn: (column: IWidgetColumn, model: IWidget) => {
        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
        if (index !== -1) {
            if (column.name === model.temp.selectedColumn?.name) model.temp.selectedColumn = null
            model.columns.splice(index, 1)
            emitter.emit('collumnRemoved', column)
        }
    },
    updateColumnValue: (column: IWidgetColumn, model: IWidget, field: string) => {
        if (!model || !model.columns) return
        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === column.name)
        if (index !== -1) {
            model.columns[index][field] = column[field]
            if (model.columns[index][field].fieldType === 'ATTRIBUTE') model.columns[index][field].aggregation = 'NONE'
            if (model.temp.selectedColumn.name === model.columns[index].name) model.temp.selectedColumn = { ...model.columns[index] }
        }
    },
    getColumnAggregationOptions: () => {
        return descriptor.columnAggregationOptions
    },
    showAggregationDropdown: (column: IWidgetColumn) => {
        return column.fieldType === 'MEASURE'
    },
    setSelectedColumn: (column: IWidgetColumn, model: IWidget) => {
        if (!model || !model.temp) return
        model.temp.selectedColumn = { ...column }
    },
    columnIsSelected: (model: IWidget) => {
        return model && model.temp.selectedColumn
    },
    updateSelectedColumn: (model: IWidget) => {
        const index = model.columns.findIndex((tempColumn: IWidgetColumn) => tempColumn.name === model.temp.selectedColumn.name)
        if (index !== -1) model.columns[index] = { ...model.temp.selectedColumn }
    },
    tooltipIsDisabled: (model: IWidget) => {
        return !model?.temp.selectedColumn?.enableTooltip
    },
    selectedColumnDropdownIsVisible: (model: IWidget) => {
        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
    },
    getVisualizationTypeOptions: () => {
        return descriptor.visualizationTypeOptions
    },
    visualizationTypeDropdownIsVisible: (model: IWidget) => {
        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
    },
    tooltipPrecisionIsVisible: (model: IWidget) => {
        return model?.temp.selectedColumn?.fieldType === 'MEASURE'
    },
    tooltipCustomHeaderTextIsDisabled: (model: IWidget) => {
        return !model?.temp.selectedColumn?.style.enableCustomHeaderTooltip || model.functions.tooltipIsDisabled(model)
    },
    headerIsDisabled: (model: IWidget) => {
        return !model?.styles.th.enabled
    },
    updateFontWeight: (model: IWidget) => {
        if (!model) return
        model.styles.th['font-weight'] = model.styles.th['font-weight'] === 'bold' ? '' : 'bold'
    },
    boldIconIsActive: (model: IWidget) => {
        return model?.styles.th['font-weight'] === 'bold'
    },
    updateFontStyle: (model: IWidget) => {
        if (!model) return
        model.styles.th['font-style'] = model.styles.th['font-style'] === 'italic' ? '' : 'italic'
    },
    fontStyleIconIsActive: (model: IWidget) => {
        return model?.styles.th['font-style'] === 'italic'
    },
    getFontSizeOptions: () => {
        return descriptor.fontSizeOptions
    },
    updateFontSize: (newValue: string, model: IWidget) => {
        if (!model) return
        model.styles.th['font-size'] = newValue
    },
    getCellAlignmentOptions: () => {
        return descriptor.cellAlignmentOptions
    },
    updateCellAlignment: (newValue: string, model: IWidget) => {
        if (!model) return
        model.styles.th['justify-content'] = newValue
    },
    getFontFamilyOptions: () => {
        return descriptor.fontFamilyOptions
    },
    updateFontFamily: (newValue: string, model: IWidget) => {
        if (!model) return
        model.styles.th['font-family'] = newValue
    },
    getFontSize: (model: IWidget) => {
        return model.styles.th['font-size']
    },
    getFontColor: (model: IWidget) => {
        return model.styles.th.color
    },
    setFontColor: (newValue: string, model: IWidget) => {
        if (!model) return
        model.styles.th.color = newValue
    },
    getBackgroundColor: (model: IWidget) => {
        return model.styles.th['background-color']
    },
    setBackgroundColor: (newValue: string, model: IWidget) => {
        if (!model) return
        model.styles.th['background-color'] = newValue
    },
    getRowThresholdsList: (model: IWidget) => {
        if (!model) return
        return model.settings.rowThresholds?.list
    },
    createRowThresholdListItem: (model: IWidget) => {
        if (!model || !model.settings.rowThresholds?.list || !model.settings.rowThresholds.enabled) return
        // TODO - CHANGE DEFAULT?
        model.settings.rowThresholds.list.push({
            column: '',
            condition: '',
            compareValueType: '',
            compareValue: '',
            'background-color': '',
            'justify-content': '',
            'font-size': '',
            'font-style': '',
            'font-weight': '',
            'font-family': '',
            color: ''
        })
    },
    deleteRowThresholdListItem: (model: IWidget, itemIndex: number) => {
        if (model.settings.rowThresholds.enabled) model.settings.rowThresholds.list.splice(itemIndex, 1)
    },
    onRowThresholdsEnabled: (model: IWidget) => {
        if (!model || !model.settings.rowThresholds?.list) return
        if (model.settings.rowThresholds.list.length === 0) {
            model.functions.createRowThresholdListItem(model)
        }
    },
    getDatasetColumns: () => {
        // TODO - REMOVE MOCK
        return [
            { value: 'Dataset 1', label: 'Dataset 1' },
            { value: 'Dataset 2', label: 'Dataset 2' },
            { value: 'Dataset 3', label: 'Dataset 3' }
        ]
    },
    updateThresholdListItem: (model: IWidget, item: any, index: number) => {
        if (!model || !model.settings.rowThresholds?.list) return
        if (index !== -1) {
            if (model.settings.rowThresholds.list[index].column !== item.column && item.compareValueType !== 'static') {
                item.compareValue = ''
            }
            if (model.settings.rowThresholds.list[index].compareValueType !== item.compareValueType) item.compareValue = ''
            model.settings.rowThresholds.list[index] = { ...item }
        }
    },
    getColumnConditionOptions: () => {
        return descriptor.columnConditionOptions
    },
    getRowStyleCompareValueTypes: () => {
        return descriptor.rowStyleCompareValueTypes
    },
    compareValueInputTextIsVisible: (model: IWidget, item: any) => {
        if (!item) return
        return item.compareValueType === 'static'
    },
    getColumnVariables: (model: IWidget) => {
        // TODO - remove mock
        return [
            { value: 'Variable 1', label: 'Varibale 1' },
            { value: 'Variable 2', label: 'Varibale 2' }
        ]
    },
    compareValueVariablesDropdownIsVisible: (model: IWidget, item: any) => {
        if (!item) return
        return item.compareValueType === 'variable'
    },
    getColumnVariableOptions: (item: any) => {
        // TODO - remove mock
        return [
            { value: 'Variable OPTION 1', label: 'Varibale OPTION 1' },
            { value: 'Variable OPTION 2', label: 'Varibale OPTION 2' }
        ]
    },
    getColumnParameters: (model: IWidget) => {
        // TODO - remove mock
        return [
            { value: 'Parameter 1', label: 'Parameter 1' },
            { value: 'Parameter 2', label: 'Parameter 2' }
        ]
    },
    compareValueParameterDropdownIsVisible: (model: IWidget, item: any) => {
        if (!item) return
        return item.compareValueType === 'parameter'
    },
    updateThresholdListItemFontItemWeight: (model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex]['font-weight'] = model.settings.rowThresholds.list[itemIndex]['font-weight'] === 'bold' ? '' : 'bold'
    },
    thresholdItemBoldIconIsActive: (model: IWidget, item: any, itemIndex: number) => {
        if (!model.settings.rowThresholds.list[itemIndex]) return false
        return model.settings.rowThresholds.list[itemIndex]['font-weight'] === 'bold'
    },
    updateThresholdListItemFontStyle: (model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex]['font-style'] = model.settings.rowThresholds.list[itemIndex]['font-style'] === 'italic' ? '' : 'italic'
    },
    thresholdListItemFontStyleIconIsActive: (model: IWidget, item: any, itemIndex: number) => {
        if (!model.settings.rowThresholds.list[itemIndex]) return false
        return model.settings.rowThresholds.list[itemIndex]['font-style'] === 'italic'
    },
    updateThresholdListItemFontSize: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex]['font-size'] = newValue
    },
    getThresholdListItemFontSize: (model: IWidget, item: any, itemIndex: number) => {
        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex]['font-size'] : ''
    },

    updateThresholdListItemCellAlignment: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex]['justify-content'] = newValue
    },
    updateThresholdListItemFontFamily: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex]['font-family'] = newValue
    },
    getThresholdListItemFontColor: (model: IWidget, item: any, itemIndex: number) => {
        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex].color : ''
    },
    setThresholdListItemFontColor: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
        if (!model.settings.rowThresholds.list[itemIndex]) return
        model.settings.rowThresholds.list[itemIndex].color = newValue
    },
    getThresholdListItemBackgroundColor: (model: IWidget, item: any, itemIndex: number) => {
        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex]['background-color'] : ''
    },
    setThresholdListItemBackgroundColor: (newValue: string, model: IWidget, item: any, itemIndex: number) => {
        if (!model.settings.rowThresholds.list[itemIndex]) return
        model.settings.rowThresholds.list[itemIndex]['background-color'] = newValue
    },
    rowThresholdsIsDisabled: (model: IWidget) => {
        return !model?.settings.rowThresholds?.enabled
    },
    setThresholdListItemIcon: (icon: IIcon, model: IWidget, item: any, itemIndex: number) => {
        model.settings.rowThresholds.list[itemIndex].icon = icon?.value
    },
    getThresholdListItemIcon: (model: IWidget, item: any, itemIndex: number) => {
        return model.settings.rowThresholds.list[itemIndex] ? model.settings.rowThresholds.list[itemIndex].icon : ''
    },
    updateMultiSelectableColor: (model: IWidget, newColor: string) => {
        model.settings.multiselectablecolor = newColor
    },
    updateEvenRowsColor: (model: IWidget, newColor: string) => {
        if (!model.settings.alternateRows) return
        model.settings.alternateRows.evenRowsColor = newColor
    },
    updateOddRowsColor: (model: IWidget, newColor: string) => {
        if (!model.settings.alternateRows) return
        model.settings.alternateRows.oddRowsColor = newColor
    },
    alternatedRowsDisabled: (model: IWidget) => {
        return !model?.settings.alternateRows?.enabled
    },
    customEmptyRowsMessageDisabled: (model: IWidget) => {
        return model?.settings.norows?.hide
    },
    multiselectableColorIsDisabled: (model: IWidget) => {
        return !model?.settings.multiselectable
    },
    getBordersStyleOptions: () => {
        return descriptor.bordersStyleOptions
    },
    bordersAreDisabled: (model: IWidget) => {
        return !model?.styles.borders
    },
    updateBordersColor: (model: IWidget, newColor: string) => {
        if (!model.styles.border) return
        model.styles.border['border-color'] = newColor
    },
}

function createNewWidgetColumn(eventData: any) {
    const tempColumn = {
        dataset: eventData.dataset,
        name: '(' + eventData.name + ')',
        alias: eventData.alias,
        type: eventData.type,
        fieldType: eventData.fieldType,
        aggregation: eventData.aggregation,
        style: {
            hiddenColumn: false,
            'white-space': 'nowrap',
            tooltip: { prefix: '', suffix: '', precision: 0 },
            enableCustomHeaderTooltip: false,
            customHeaderTooltip: ''
        },
        enableTooltip: false,
        visType: ''
    }
    tempColumn.aggregation = 'NONE'
    return tempColumn
}

export function formatTableWidgetForSave(widget: IWidget) {
    if (!widget) return

    formatTablePagination(widget.settings.pagination)
    formatTableSelectedColumns(widget.columns)
    formatWidgetDatasetKeysArray(widget)
    formatRowHeaderSettings(widget)
    formatRowStyleSettings(widget)
    formatBorderSettings(widget)
}

function formatTablePagination(pagination: { enabled: boolean, itemsNumber: string | number }) {
    if (!pagination) return
    pagination.itemsNumber = pagination.enabled ? +pagination.itemsNumber : 0
}

function formatTableSelectedColumns(columns: IWidgetColumn[]) {
    if (!columns) return
    columns.forEach((column: IWidgetColumn) => {
        if (column.name.startsWith('(')) column.name = column.name.slice(1, -1)
        formatColumnTooltipSettings(column)
    })
}

function formatWidgetDatasetKeysArray(widget: IWidget) {
    if (!widget.columns || !widget.datasets) return
    for (let i = 0; i < widget.columns.length; i++) {
        const index = widget.datasets.findIndex((id: number) => id === widget.columns[i].dataset)
        if (index == -1) widget.datasets.push(widget.columns[i].dataset)
    }
}

function formatColumnTooltipSettings(column: IWidgetColumn) {
    if (column.enableTooltip) {
        column.style.tooltip.precision = +column.style.tooltip.precision
    } else {
        column.style.tooltip = { prefix: '', suffix: '', precision: 0 }
        column.style.enableCustomHeaderTooltip = false
        column.style.customHeaderTooltip = ''
    }
}

function formatRowHeaderSettings(widget: IWidget) {
    if (!widget.styles.th.enbaled) {
        widget.styles.th = {
            'background-color': '',
            color: '',
            'justify-content': '',
            'font-size': '',
            multiline: false,
            height: 0,
            'font-style': '',
            'font-weight': '',
            'font-family': ''
        }
    }
}

function formatRowStyleSettings(widget: IWidget) {
    if (widget.styles.tr.height) widget.styles.tr.height = +widget.styles.tr.height
    if (!widget.settings.multiselectable) widget.settings.multiselectablecolor = ''
    if (widget.settings.multiselectablecolor && typeof widget.settings.multiselectablecolor !== 'string') widget.settings.multiselectablecolor = formatRGBColor(widget.settings.multiselectablecolor)
    if (!widget.settings.alternateRows.enabled) {
        widget.settings.alternateRows.evenRowsColor = ''
        widget.settings.alternateRows.oddRowsColor = ''
    }
    if (widget.settings.alternateRows.evenRowsColor && typeof widget.settings.alternateRows.evenRowsColorr !== 'string') widget.settings.alternateRows.evenRowsColor = formatRGBColor(widget.settings.alternateRows.evenRowsColor)
    if (widget.settings.alternateRows.oddRowsColor && typeof widget.settings.alternateRows.oddRowsColor !== 'string') widget.settings.alternateRows.oddRowsColor = formatRGBColor(widget.settings.alternateRows.oddRowsColor)
    if (widget.settings.norows.hide) widget.settings.norows.message = ''
}

function formatBorderSettings(widget: IWidget) {
    if (!widget.styles.borders) {
        widget.styles.border = {
            "border-top-left-radius": "",
            "border-top-right-radius": "",
            "border-bottom-left-radius": "",
            "border-bottom-right-radius": "",
            "border-color": "",
            "border-width": "",
            "border-style": ""
        }
    }
    if (widget.styles.border['border-color'] && typeof widget.styles.border['border-color'] !== 'string') widget.styles.border['border-color'] = formatRGBColor(widget.styles.border['border-color'])
}

export default tableWidgetFunctions