import { IWidget, IWidgetExports, IWidgetInteractions, IWidgetResponsive } from "../../Dashboard"
import { ICustomChartStyle, ICustomChartWidgetConfiguration, ICustomChartWidgetEditor, ICustomChartWidgetSettings } from "../../interfaces/customChart/DashboardCustomChartWidget"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./CustomChartWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFiltersForColumns } from "../DashboardBackwardCompatibilityHelper"

const columnNameIdMap = {}

export const formatCustomChartWidget = (widget: any) => {
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        columns: getFormattedWidgetColumns(widget, columnNameIdMap),
        theme: '',
        settings: {} as ICustomChartWidgetSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget)
    getFiltersForColumns(formattedWidget, widget)
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        editor: getFormattedEditor(widget),
        configuration: getFormattedConfiguration(widget) as ICustomChartWidgetConfiguration,
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget) as ICustomChartStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive
    } as ICustomChartWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return { exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports }
}

const getFormattedEditor = (widget: any) => {
    return { css: widget.css.code, html: widget.html.code, js: widget.js.code } as ICustomChartWidgetEditor
}