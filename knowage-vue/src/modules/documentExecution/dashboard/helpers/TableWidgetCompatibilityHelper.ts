import { IWidget, IWidgetColumn } from '../Dashboard'
import cryptoRandomString from 'crypto-random-string'

export const formatTableWidget = (widget: any) => {
    console.log("TableWidgetCompatibilityHelper - formatTableWidget called for: ", widget)
    const formattedWidget = {
        id: widget.id, dataset: widget.dataset.dsId, type: widget.type, columns: getFormattedWidgetColumns(widget), conditionalStyles: [], interactions: [], theme: '', styles: {}, settings: getFormattedWidgetSettings(widget)
    } as IWidget

    console.log("TableWidgetCompatibilityHelper - FORMATTED WIDGET: ", formattedWidget)
    return formattedWidget
}

const getFormattedWidgetColumns = (widget: any) => {
    if (!widget?.content?.columnSelectedOfDataset) return
    const formattedColumns = [] as IWidgetColumn[]
    for (let i = 0; i < widget.content.columnSelectedOfDataset.length; i++) {
        formattedColumns.push(getFormattedWidgetColumn(widget.content.columnSelectedOfDataset[i]))
    }
    return formattedColumns
}

const getFormattedWidgetColumn = (widgetColumn: any) => {
    const formattedColumn = { id: cryptoRandomString({ length: 16, type: 'base64' }), columnName: widgetColumn.name, alias: widgetColumn.alias, type: widgetColumn.type, fieldType: widgetColumn.fieldType, multiValue: widgetColumn.multiValue } as IWidgetColumn
    if (widgetColumn.aggregationSelected) formattedColumn.aggregation = widgetColumn.aggregationSelected
    return formattedColumn
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = { sortingColumn: widget.settings?.sortingColumn, sortingOrder: widget.settings?.sortingOrder, updatable: widget.updateble, clickable: widget.cliccable, conditionalStyle: getFormattedConditionalStyles(widget), getFormattedConfiguration: getFormattedConfiguration(widget), interactions: getFormattedInteractions(widget), pagination: getFormattedPaginations(widget), style: getFormattedStyle(widget), tooltips: getFormattedTooltips(widget), visualization: getFormattedVisualizations(widget), responsive: getFormattedResponsivnes(widget) }
    return formattedSettings
}

// TODO
const getFormattedConditionalStyles = (widget: any) => {
    return {}
}

// TODO
const getFormattedConfiguration = (widget: any) => {
    return {}
}

// TODO
const getFormattedInteractions = (widget: any) => {
    return {}
}

// TODO
const getFormattedPaginations = (widget: any) => {
    if (!widget.settings?.pagination) return {}
    return { enabled: widget.settings.pagination.enabled, itemsNumber: widget.settings.pagination.itemsNumber }
}


// TODO
const getFormattedStyle = (widget: any) => {
    return {}
}


// TODO
const getFormattedTooltips = (widget: any) => {
    return []
}


// TODO
const getFormattedVisualizations = (widget: any) => {
    return {}
}

// TODO
const getFormattedResponsivnes = (widget: any) => {
    return {}
}