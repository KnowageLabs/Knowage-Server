import { ITableWidgetCrossNavigation, ITableWidgetLink, ITableWidgetLinks, ITableWidgetParameter, ITableWidgetPreview, ITableWidgetSelection, IWidget } from "../../Dashboard"
import { getColumnId } from './TableWidgetCompatibilityHelper'

export const getFormattedInteractions = (formattedWidget: IWidget, widget: any) => {
    return {
        crosssNavigation: getFormattedCrossNavigation(formattedWidget, widget) as ITableWidgetCrossNavigation,
        link: getFormattedLinkInteraction(widget) as ITableWidgetLinks,
        preview: getFormattedPreview(formattedWidget, widget) as ITableWidgetPreview,
        selection: getFormattedSelection(widget) as ITableWidgetSelection
    }
}

const getFormattedCrossNavigation = (formattedWidget: IWidget, widget: any) => {
    if (!widget.cross || !widget.cross.cross) return {
        enabled: false,
        type: '',
        column: '',
        name: '',
        parameters: []
    }

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
    const formattedParameters = [] as ITableWidgetParameter[]
    if (outputParameterList) {
        Object.keys(outputParameterList).forEach((key: string) => {
            const tempParameter = outputParameterList[key]
            const formattedParameter = {
                enabled: tempParameter.enabled,
                name: key,
                type: tempParameter.type,
                value: tempParameter.value
            } as ITableWidgetParameter
            if (tempParameter) formattedParameter.column = tempParameter.column
            if (tempParameter) formattedParameter.dataset = tempParameter.dataset
            formattedParameters.push(formattedParameter)
        })
    }
    return formattedParameters
}

const getFormattedLinkInteraction = (widget: any) => {
    if (!widget.cross || !widget.cross.link) return {
        enabled: false,
        links: []
    }

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
    const formattedParameters = [] as ITableWidgetParameter[]
    linkParameters.forEach((linkParameter: any) => {
        const formattedParameter = {
            enabled: true,
            name: linkParameter.name,
            type: linkParameter.bindType,
            value: linkParameter.value ?? ''
        } as ITableWidgetParameter

        if (linkParameter.column) formattedParameter.column = linkParameter.column
        if (linkParameter.dataset) formattedParameter.dataset = linkParameter.dataset
        if (linkParameter.driver) formattedParameter.driver = linkParameter.driver
        if (linkParameter.json) formattedParameter.json = linkParameter.json

        formattedParameters.push(formattedParameter)
    })
    return formattedParameters
}

const getFormattedPreview = (formattedWidget: IWidget, widget: any) => {
    if (!widget.cross || !widget.cross.preview) return {
        enabled: false,
        type: '',
        dataset: '',
        parameters: [],
        directDownload: false
    }

    const formattedPreview = {
        enabled: widget.cross.preview.enable,
        type: widget.cross.preview.previewType,
        icon: widget.cross.preview.icon ? widget.cross.preview.icon.trim() : '',
        dataset: widget.cross.preview.dataset,
        directDownload: widget.cross.preview.background,
        parameters: widget.cross.preview.parameters ? getFormattedPreviewParameters(formattedWidget, widget.cross.preview.parameters) : []
    } as ITableWidgetPreview

    if (widget.cross.preview.column) formattedPreview.column = widget.cross.preview.column

    return formattedPreview
}



const getFormattedPreviewParameters = (formattedWidget: IWidget, previewParameters: any) => {
    const formattedParameters = [] as ITableWidgetParameter[]

    previewParameters?.forEach((previewParameter: any) => {
        const formattedParameter = {
            enabled: true,
            name: previewParameter.name,
            type: previewParameter.bindType,
            value: previewParameter.value ?? ''
        } as ITableWidgetParameter

        if (previewParameter.driver) formattedParameter.driver = previewParameter.driver
        if (previewParameter.column) formattedParameter.column = previewParameter.column
        if (previewParameter.dataset) formattedParameter.dataset = previewParameter.dataset

        formattedParameters.push(formattedParameter)
    })


    return formattedParameters
}


const getFormattedSelection = (widget: any) => {
    return {
        enabled: true,
        modalColumn: '',
        multiselection: {
            enabled: widget.settings.multiselectable ?? false,
            properties: {
                "background-color": '',
                color: widget.settings.multiselectablecolor ?? ''
            }
        }
    }
}