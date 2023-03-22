import { getFormattedStyle } from './MapStyleHelper';
import { IWidget, IWidgetResponsive, IWidgetExports } from './../../Dashboard.d';
import { IMapWidgetSettings, IMapWidgetStyle } from './../../interfaces/mapWidget/DashboardMapWidget.d';
import * as mapWidgetDefaultValues from '../../widget/WidgetEditor/helpers/mapWidget/MapWidgetDefaultValues'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'

const columnNameIdMap = {}

export const formatMapWidget = (widget: any) => {

    console.log('--------- ORIGINAL WIDGET: ', widget)

    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: [],
        theme: '',
        style: {},
        settings: {} as IMapWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget)

    console.log('--------- FORMATTED WIDGET: ', widget)

    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        configuration: getFormattedConfiguration(widget),
        visualization: getFormattedVisualization(widget),
        conditionalStyles: getFormattedConditionalStyles(widget),
        legend: getFormattedLegend(widget),
        dialog: getFormattedDialogSettings(widget),
        style: getFormattedStyle(widget) as IMapWidgetStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive,
        tooltips: getFormattedTooltipsSettings(widget)
    } as IMapWidgetSettings
    return formattedSettings
}

// TODO
const getFormattedConfiguration = (widget: any) => {
    return { exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports }
}

// TODO
const getFormattedVisualization = (widget: any) => {
    return {}
}

// TODO
const getFormattedConditionalStyles = (widget: any) => {
    return {}
}

// TODO
const getFormattedLegend = (widget: any) => {
    return {}
}

// TODO
const getFormattedDialogSettings = (widget: any) => {
    return {}
}

// TODO
const getFormattedTooltipsSettings = (widget: any) => {
    return {}
}