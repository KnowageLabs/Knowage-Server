import { IWidget, IWidgetExports, IWidgetInteractions } from "../../Dashboard"
import { ITextWidgetConfiguration, ITextWidgetEditor, ITextWidgetSettings } from "../../interfaces/DashboardTextWidget"
import { getFormattedStyle } from "./TextWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { columnTextCompatibilityRegex, crossNavigationTextCompatibilityRegex, parameterTextCompatibilityRegex, variableTextCompatibilityRegex } from "../common/DashboardRegexHelper"

const columnNameIdMap = {}

export const formatTextWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ? widget.dataset.dsId[0] : null,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap, false),
        theme: '',
        settings: {} as ITextWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget) as ITextWidgetSettings
    return formattedWidget
}


const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        editor: getFormattedEditor(widget),
        configuration: getFormattedConfiguration(widget),
        style: getFormattedStyle(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as ITextWidgetSettings
    return formattedSettings
}

const getFormattedEditor = (widget: any) => {
    return { text: widget.content?.text ? getFormattedText(widget, widget.content.text) : '' } as ITextWidgetEditor
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as ITextWidgetConfiguration
}

const getFormattedText = (widget: any, originalText: string) => {
    if ((originalText.indexOf("$F{") < 0 && originalText.indexOf("$P{") < 0 && originalText.indexOf("$V{") < 0)) return originalText
    let formattedText = replaceParameters(originalText)
    formattedText = replaceVariables(formattedText)
    formattedText = replaceColumns(widget, formattedText)
    formattedText = replaceCrossNavigation(formattedText)
    return formattedText
}

const replaceParameters = (text: string) => {
    return text.replace(parameterTextCompatibilityRegex, parametersReplacer)
}

const parametersReplacer = (match: string, parameterUrlName: string) => {
    return `[kn-parameter='${parameterUrlName}']`
}

const replaceVariables = (text: string) => {
    return text.replace(variableTextCompatibilityRegex, variablesReplacer)
}

const variablesReplacer = (match: string, variableName: string) => {
    return `[kn-variable='${variableName}']`
}

const replaceColumns = (widget: any, text: string) => {
    const numberFormatting = widget.numbers
    return text.replace(columnTextCompatibilityRegex, (match: string, aggregation: string, datasetAndColumnName: any) => {
        const formattedAggregation = aggregation ? aggregation.substring(0, aggregation.length - 1) : ''
        const columnName = datasetAndColumnName && datasetAndColumnName.split('.') ? datasetAndColumnName.split('.')[1] : ''
        let result = `[kn-column='${columnName}' row='0'`
        if (numberFormatting) {
            if (formattedAggregation) result += ` aggregation='${formattedAggregation}'`
            if (numberFormatting.precision) result += ` precision='${numberFormatting.precision}'`
            if (numberFormatting.format) result += ` format`
            if (numberFormatting.prefix) result += ` prefix='${numberFormatting.prefix}'`
            if (numberFormatting.suffix) result += ` suffix='${numberFormatting.suffix}'`
        }
        result += ']'
        return result
    })
}

const replaceCrossNavigation = (text: string) => {
    return text.replace(crossNavigationTextCompatibilityRegex, 'kn-cross')
}