import { IWidget, IWidgetInteractions, IWidgetResponsive } from '../../Dashboard'
// import * as pivotTalbeDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'
import * as widgetCommonDefaultValues from '../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
// import { getFiltersForColumns } from '../DashboardBackwardCompatibilityHelper'
import { getFormattedInteractions } from '../common/WidgetInteractionsHelper'
import { getFormattedPivotFields } from './PivotTableColumnHelper'
import { IPivotTableConfiguration, IPivotTableSettings, IPivotTableStyle, IPivotTableWidgetConditionalStyles, IPivotTableWidgetVisualization, IPivotTooltips } from '../../interfaces/pivotTable/DashboardPivotTableWidget'
import { getSettingsFromPivotTableWidgetColumns } from './PivotTableColumnSettingsHelper'
import { getFormattedConfiguration } from './PivotTableConfigurationHelper'
import { getFormattedStyle } from './PivotTabletStyleHelper'
import * as pivotTableDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'

const columnNameIdMap = {}

export const formatPivotTabletWidget = (widget: any) => {
    console.log('----------- ORIGINAL WIDGET: ', widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId,
        type: widget.type,
        fields: getFormattedPivotFields(widget, columnNameIdMap),
        columns: [], //Not used for pivot :/
        theme: '',
        style: {},
        settings: {} as IPivotTableSettings
    } as IWidget
    formattedWidget.settings = getFormattedWidgetSettings(widget)

    //TODO: Rework this method
    // getFiltersForColumns(formattedWidget, widget)

    getSettingsFromPivotTableWidgetColumns(formattedWidget, widget)

    console.log('----------- FORMATTED WIDGET: ', formattedWidget)
    return formattedWidget
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        conditionalStyles: pivotTableDefaultValues.getDefaultConditionalStyles() as IPivotTableWidgetConditionalStyles,
        visualization: pivotTableDefaultValues.getDefaultVisualisationSettings() as IPivotTableWidgetVisualization,
        configuration: getFormattedConfiguration(widget) as IPivotTableConfiguration,
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget) as IPivotTableStyle,
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes() as IWidgetResponsive,
        tooltips: pivotTableDefaultValues.getDefaultTooltips() as IPivotTooltips[]
    } as IPivotTableSettings
    return formattedSettings
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

