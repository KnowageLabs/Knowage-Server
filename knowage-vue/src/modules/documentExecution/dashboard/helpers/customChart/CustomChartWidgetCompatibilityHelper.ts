import { IWidget, IWidgetExports, IWidgetInteractions, IWidgetResponsive } from "../../Dashboard"
import { ICustomChartStyle, ICustomChartWidgetConfiguration, ICustomChartWidgetSettings } from "../../interfaces/customChart/DashboardCustomChartWidget"
import { getFormattedWidgetColumns } from "../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../common/WidgetInteractionsHelper"
import { getFormattedStyle } from "./CustomChartWidgetStyleHelper"
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFiltersForColumns } from "../DashboardBackwardCompatibilityHelper"

const columnNameIdMap = {}

export const formatCustomChartWidget = (widget: any) => {
    console.log('>>>>>>>>>>> OLD WIDGET MODEL: ', widget)

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

    console.log('>>>>>>>>>>> FORMATTED WIDGET MODEL: ', formattedWidget)

    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
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