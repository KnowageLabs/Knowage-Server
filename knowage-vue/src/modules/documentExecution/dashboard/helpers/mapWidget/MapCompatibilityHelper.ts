import { IWidget, IWidgetResponsive, IWidgetExports, IWidgetInteractions, IDashboard, IDashboardDriver } from './../../Dashboard.d'
import { IMapWidgetConditionalStyles, IMapWidgetSettings, IMapWidgetStyle } from './../../interfaces/mapWidget/DashboardMapWidget.d'
import { getFormattedStyle } from './MapStyleHelper'
import { hexToRgba } from '../FormattingHelpers'
import { getFormattedInteractions } from '../common/WidgetInteractionsHelper'
import { getFormattedSettingsFromLayers } from './MapLayersCompatibilityHelper'
import * as mapWidgetDefaultValues from '../../widget/WidgetEditor/helpers/mapWidget/MapWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

export const formatMapWidget = (widget: any, formattedDashboardModel: IDashboard, drivers: IDashboardDriver[]) => {
    console.log('--------- ORIGINAL WIDGET: ', widget)

    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: [],
        layers: getFormattedLayers(widget),
        theme: '',
        style: {},
        settings: {} as IMapWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget)
    getFormattedSettingsFromLayers(widget, formattedWidget, formattedDashboardModel, drivers)

    console.log('--------- FORMATTED WIDGET: ', formattedWidget)
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        visualization: getFormattedVisualization(),
        conditionalStyles: getFormattedConditionalStyles(),
        legend: getFormattedLegend(widget),
        dialog: getFormattedDialogSettings(widget),
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget) as IMapWidgetStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive,
        tooltips: getFormattedTooltipsSettings()
    } as IMapWidgetSettings
    return formattedSettings
}

// TODO - formatirati layere kad zavrsimo sprint, pitaj boga sta nam sve treba odavde a sta ne......
const getFormattedLayers = (widget: any) => {
    return widget.content.layers
}

const getFormattedConfiguration = (widget: any) => {
    return {
        baseLayer: getFormattedBaseLayer(widget),
        controlPanel: getFormattedControlPanel(widget),
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    }
}

const getFormattedBaseLayer = (widget: any) => {
    const formattedBaseLayer = mapWidgetDefaultValues.getDefaultBaseLayerSettings()
    if (!widget.content) return formattedBaseLayer
    formattedBaseLayer.backgroundLayerId = widget.content.backgroundLayerId
    formattedBaseLayer.zoomFactor = widget.content.zoomFactor
    formattedBaseLayer.showScale = widget.content.showScale
    formattedBaseLayer.autoCentering = widget.content.autoCentering
    return formattedBaseLayer
}

const getFormattedControlPanel = (widget: any) => {
    const formattedControlPanel = mapWidgetDefaultValues.getDefaultControlPanelSettings()
    formattedControlPanel.alwaysShow = widget.controlPanelAlwaysOpen
    if (widget.style?.controlPanel) formattedControlPanel.dimension = widget.style.controlPanel.width
    return formattedControlPanel
}

const getFormattedVisualization = () => {
    const formattedVisualizationSettings = mapWidgetDefaultValues.getDefaultVisualizationSettings()
    return formattedVisualizationSettings
}

const getFormattedConditionalStyles = () => {
    const formattedStyles = { enabled: false, conditions: [] } as IMapWidgetConditionalStyles
    return formattedStyles
}

const getFormattedLegend = (widget: any) => {
    const formattedLegendSettings = mapWidgetDefaultValues.getDefaultLegendSettings()
    if (!widget.style || !widget.style.legend) return formattedLegendSettings
    formattedLegendSettings.alignment = widget.style.legend.alignment
    formattedLegendSettings.visualizationType = widget.style.legend.visualizationType
    if (widget.style.legend.format) {
        formattedLegendSettings.precision = widget.style.legend.format.precision
        formattedLegendSettings.prefix = widget.style.legend.format.prefix
        formattedLegendSettings.suffix = widget.style.legend.format.suffix
    }
    return formattedLegendSettings
}

const getFormattedDialogSettings = (widget: any) => {
    const formattedDialogSettings = mapWidgetDefaultValues.getDefaultDialogSettings()
    if (!widget.style || !widget.style.tooltip) return formattedDialogSettings
    if (widget.style.tooltip.box) {
        formattedDialogSettings.height = widget.style.tooltip.box.height
        formattedDialogSettings.width = widget.style.tooltip.box.width
    }
    if (widget.style.tooltip.text) {
        formattedDialogSettings.style['font-size'] = widget.style.tooltip.text['font-size']
        formattedDialogSettings.style.color = widget.style.tooltip.text.color ? hexToRgba(widget.style.tooltip.text.color) : ''
    }
    return formattedDialogSettings
}

const getFormattedTooltipsSettings = () => {
    const formattedTooltips = mapWidgetDefaultValues.getDefaultMapTooltips()
    return formattedTooltips
}

