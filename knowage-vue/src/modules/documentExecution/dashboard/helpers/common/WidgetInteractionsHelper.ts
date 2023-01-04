import { ITableWidgetLink, IWidgetCrossNavigation, IWidgetInteractionParameter, IWidgetInteractions, IWidgetLinks, IWidgetPreview, IWidgetSelection } from '../../Dashboard'
import { getColumnId } from '../tableWidget/TableWidgetCompatibilityHelper'
import { IHighchartsDrilldown } from '../../interfaces/highcharts/DashboardHighchartsWidget'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import * as tableWidgetDefaultValues from '../../widget/WidgetEditor/helpers/tableWidget/TableWidgetDefaultValues'
import * as chartJSDefaultValues from '../../widget/WidgetEditor/helpers/chartWidget/chartJS/ChartJSDefaultValues'
import * as highchartsDefaultValues from '../../widget/WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import mainStore from '@/App.store'

export const getFormattedInteractions = (widget: any) => {
    const interactions = {} as IWidgetInteractions
    let chartType = getChartType(widget)
    if (['table', 'chart'].includes(widget.type) && chartType !== 'GAUGE') interactions.selection = getFormattedSelection(widget) as IWidgetSelection
    if (['table', 'html', 'text', 'chart'].includes(widget.type)) interactions.crossNavigation = getFormattedCrossNavigation(widget) as IWidgetCrossNavigation
    if (['table', 'chart'].includes(widget.type)) interactions.link = getFormattedLinkInteraction(widget) as IWidgetLinks
    if (['table', 'html', 'text', 'chart'].includes(widget.type)) interactions.preview = getFormattedPreview(widget) as IWidgetPreview
    if (['chart'].includes(widget.type)) interactions.drilldown = { enabled: false } as IHighchartsDrilldown
    return interactions
}

const getChartType = (widget: any) => {

    return widget.content?.chartTemplate?.CHART?.type ?? null
}

const getFormattedSelection = (widget: any) => {
    if (widget.type === 'table') {
        return getFormattedTableSelection(widget)
    } else if (widget.type === 'chart') {
        return getFormattedChartSelection()
    }
}

const getFormattedTableSelection = (widget: any) => {
    if (!widget.settings.multiselectable && !widget.settings.multiselectablecolor && !widget.settings.modalSelectionColumn) return tableWidgetDefaultValues.getDefaultSelection() as IWidgetSelection
    const formattedSelection = {
        enabled: true,
        modalColumn: widget.settings.modalSelectionColumn ? getColumnId(widget.settings.modalSelectionColumn) : '',
        multiselection: {
            enabled: widget.settings.multiselectable,
            properties: {
                'background-color': widget.settings.multiselectablecolor ? widget.settings.multiselectablecolor : '',
                color: ''
            }
        }
    } as IWidgetSelection

    return formattedSelection
}

const getFormattedChartSelection = () => {
    const store = mainStore()
    const user = store.getUser()
    // TODO widgetChange
    // return user?.enterprise ? highchartsDefaultValues.getDefaultHighchartsSelections() : chartJSDefaultValues.getDefaultChartJSSelections
    return false ? highchartsDefaultValues.getDefaultHighchartsSelections() : chartJSDefaultValues.getDefaultChartJSSelections()
}

export const getFormattedCrossNavigation = (widget: any) => {
    if (!widget.cross || !widget.cross.cross) return widgetCommonDefaultValues.getDefaultCrossNavigation()

    return {
        enabled: widget.cross.cross.enable,
        type: widget.cross.cross.crossType,
        icon: widget.cross.cross.icon ? widget.cross.cross.icon.trim() : '',
        column: getColumnId(widget.cross.cross.column),
        name: widget.cross.cross.crossName,
        parameters: widget.cross.cross.outputParametersList ? getFormattedCrossNavigationParameters(widget.cross.cross.outputParametersList) : []
    }
}

const getFormattedCrossNavigationParameters = (outputParameterList: any) => {
    const formattedParameters = [] as IWidgetInteractionParameter[]
    if (outputParameterList) {
        Object.keys(outputParameterList).forEach((key: string) => {
            const tempParameter = outputParameterList[key]
            const formattedParameter = {
                enabled: tempParameter.enabled,
                name: key,
                type: tempParameter.type,
                value: tempParameter.value
            } as IWidgetInteractionParameter
            if (tempParameter) formattedParameter.column = tempParameter.column
            if (tempParameter) formattedParameter.dataset = tempParameter.dataset
            formattedParameters.push(formattedParameter)
        })
    }
    return formattedParameters
}

export const getFormattedLinkInteraction = (widget: any) => {
    if (!widget.cross || !widget.cross.link) return widgetCommonDefaultValues.getDefaultLinks()

    return {
        enabled: widget.cross.link.enable,
        links: getFormattededLinks(widget.cross.link.links)
    }
}

const getFormattededLinks = (links: any) => {
    const formattedLinks = [] as ITableWidgetLink[]
    links.forEach((link: any) => {
        const formattedLink = {
            type: link.interactionType,
            baseurl: link.baseurl,
            action: link.type,
            parameters: getFormattedLinkParameters(link.parameters)
        } as ITableWidgetLink

        if (link.icon) formattedLink.icon = link.icon
        if (link.column) formattedLink.column = link.column

        formattedLinks.push(formattedLink)
    })

    return formattedLinks
}

const getFormattedLinkParameters = (linkParameters: any[]) => {
    if (!linkParameters || linkParameters.length === 0) return []
    const formattedParameters = [] as IWidgetInteractionParameter[]
    linkParameters.forEach((linkParameter: any) => {
        const formattedParameter = {
            enabled: true,
            name: linkParameter.name,
            type: linkParameter.bindType,
            value: linkParameter.value ?? ''
        } as IWidgetInteractionParameter

        if (linkParameter.column) formattedParameter.column = linkParameter.column
        if (linkParameter.dataset) formattedParameter.dataset = linkParameter.dataset
        if (linkParameter.driver) formattedParameter.driver = linkParameter.driver
        if (linkParameter.json) formattedParameter.json = linkParameter.json

        formattedParameters.push(formattedParameter)
    })
    return formattedParameters
}

export const getFormattedPreview = (widget: any) => {
    if (!widget.cross || !widget.cross.preview) return widgetCommonDefaultValues.getDefaultPreview()

    const formattedPreview = {
        enabled: widget.cross.preview.enable,
        type: widget.cross.preview.previewType,
        icon: widget.cross.preview.icon ? widget.cross.preview.icon.trim() : '',
        dataset: widget.cross.preview.dataset,
        directDownload: widget.cross.preview.background,
        parameters: widget.cross.preview.parameters ? getFormattedPreviewParameters(widget.cross.preview.parameters) : []
    } as IWidgetPreview

    if (widget.cross.preview.column) formattedPreview.column = widget.cross.preview.column

    return formattedPreview
}

const getFormattedPreviewParameters = (previewParameters: any) => {
    const formattedParameters = [] as IWidgetInteractionParameter[]

    previewParameters?.forEach((previewParameter: any) => {
        const formattedParameter = {
            enabled: true,
            name: previewParameter.name,
            type: previewParameter.bindType,
            value: previewParameter.value ?? ''
        } as IWidgetInteractionParameter

        if (previewParameter.driver) formattedParameter.driver = previewParameter.driver
        if (previewParameter.column) formattedParameter.column = previewParameter.column
        if (previewParameter.dataset) formattedParameter.dataset = previewParameter.dataset

        formattedParameters.push(formattedParameter)
    })

    return formattedParameters
}
