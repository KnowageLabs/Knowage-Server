import { IWidget, IWidgetColumn, IWidgetExports, IWidgetInteractions } from "../../../Dashboard"
import { IHighchartColor, IHighchartsSeriesLabelsSetting, IHighchartsWidgetConfiguration, IHighchartsWidgetSettings } from "../../../interfaces/highcharts/DashboardHighchartsWidget"
import { HighchartsPieChart } from "../../../widget/ChartWidget/classes/highcharts/KnowageHighchartsPieChart"
import * as widgetCommonDefaultValues from '../../../widget/WidgetEditor/helpers/common/WidgetCommonDefaultValues'
import { getFormattedWidgetColumn } from "../../common/WidgetColumnHelper"
import { getFormattedInteractions } from "../../common/WidgetInteractionsHelper"
import { getFiltersForColumns } from "../../DashboardBackwardCompatibilityHelper"
import { hexToRgb } from "../../FormattingHelpers"
import { addCategoryColumns, addSerieColumn } from "../CommonChartCompatibilityHelper"
import { getFormattedStyle } from "./HighchartsWidgetStyleHelper"

const columnNameIdMap = {}

export const formatHighchartsWidget = (widget: any) => {
    console.log(">>>>>>>>>>> OLD WIDGET: ", widget)
    const formattedWidget = {
        id: widget.id,
        dataset: widget.dataset.dsId ?? null,
        type: 'highcharts',
        columns: getFormattedWidgetColumns(widget),
        theme: '',
        settings: {} as IHighchartsWidgetSettings
    } as IWidget

    formattedWidget.settings = getFormattedWidgetSettings(widget) as IHighchartsWidgetSettings
    getFiltersForColumns(formattedWidget, widget)
    formattedWidget.settings.chartModel = createChartModel(widget, formattedWidget)
    console.log(">>>>>>>>>>> FORMATTED WIDGET: ", widget)
    return formattedWidget
}

// TODO - add condition for pie widget, see about the property columnSelectedOfDatasetAggregations
export const getFormattedWidgetColumns = (widget: any) => {
    if (!widget.content || !widget.content.columnSelectedOfDatasetAggregations || !widget.content.chartTemplate || !widget.content.chartTemplate.CHART || !widget.content.chartTemplate.CHART.VALUES) return []
    const widgetColumNameMap = {}
    for (let i = 0; i < widget.content.columnSelectedOfDatasetAggregations.length; i++) {
        if (!widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name]) widgetColumNameMap[widget.content.columnSelectedOfDatasetAggregations[i].name] = getFormattedWidgetColumn(widget.content.columnSelectedOfDatasetAggregations[i], columnNameIdMap)
    }

    const formattedColumns = [] as IWidgetColumn[]
    const category = widget.content.chartTemplate.CHART.VALUES.CATEGORY
    const serie = widget.content.chartTemplate.CHART.VALUES.SERIE ? widget.content.chartTemplate.CHART.VALUES.SERIE[0] : null
    if (category) addCategoryColumns(category, formattedColumns, widgetColumNameMap)
    if (serie) addSerieColumn(serie, widgetColumNameMap, formattedColumns)
    return formattedColumns
}

const getFormattedWidgetSettings = (widget: any) => {
    const formattedSettings = {
        updatable: widget.updateble,
        clickable: widget.cliccable,
        chartModel: null, // TODO - see about this
        configuration: getFormattedConfiguration(widget),
        accesssibility: {
            seriesAccesibilitySettings: getFormattedSeriesAccesibilitySettings(widget)
        },  // TODO - move to some default helper 
        series: {
            seriesLabelsSettings: getFormattedSerieLabelsSettings(widget) // TODO - move to some default helper 
        },
        interactions: getFormattedInteractions(widget) as IWidgetInteractions,
        style: getFormattedStyle(widget),
        chart: {
            colors: getFormattedColorSettings(widget)
        },
        responsive: widgetCommonDefaultValues.getDefaultResponsivnes()
    } as IHighchartsWidgetSettings
    return formattedSettings
}

const getFormattedConfiguration = (widget: any) => {
    return {
        exports: { showExcelExport: widget.style?.showExcelExport ?? false, showScreenshot: widget.style?.showScreenshot ?? false } as IWidgetExports
    } as IHighchartsWidgetConfiguration
}

const getFormattedSeriesAccesibilitySettings = (widget: any) => {
    return widget.content.chartTemplate.CHART.type !== 'PIE' ? [
        {
            names: ['all'],
            accessibility: {
                enabled: false,
                description: '',
                exposeAsGroupOnly: false,
                keyboardNavigation: { enabled: false }
            }
        }
    ] : []
}

export const getColumnId = (widgetColumnName: string) => {
    return columnNameIdMap[widgetColumnName]
}

const createChartModel = (widget: any, formattedWidget: IWidget) => {
    switch (widget.content.chartTemplate.CHART.type) {
        case 'PIE':
            return new HighchartsPieChart(widget.content.chartTemplate, formattedWidget)
        default:
            return null
    }
}

const getFormattedSerieLabelsSettings = (widget: any) => {
    const formattedSerieSettings = widget.content.chartTemplate.CHART.type !== 'PIE' ? [{
        names: ['all'],
        label: {
            enabled: false,
            style: {
                fontFamily: '',
                fontSize: '',
                fontWeight: '',
                color: '',
                backgroundColor: ''
            },
            prefix: '',
            suffix: '',
            scale: 'empty', // TODO
            precision: 2,
            absolute: false,
            percentage: false
        }
    }] : [] as IHighchartsSeriesLabelsSetting[]
    if (widget.content.chartTemplate.CHART.VALUES.SERIE && widget.content.chartTemplate.CHART.VALUES.SERIE[0]) {
        const oldModelSerie = widget.content.chartTemplate.CHART.VALUES.SERIE[0]
        formattedSerieSettings.push({
            names: [oldModelSerie.name],
            label: {
                enabled: true,
                style: {
                    fontFamily: oldModelSerie.dataLabels.style.fontFamily,
                    fontSize: oldModelSerie.dataLabels.style.fontSize,
                    fontWeight: oldModelSerie.dataLabels.style.fontWeight,
                    color: oldModelSerie.dataLabels.style.color ? hexToRgb(oldModelSerie.dataLabels.style.color) : '',
                    backgroundColor: ''
                },
                prefix: oldModelSerie.prefixChar ?? '',
                suffix: oldModelSerie.postfixChar ?? '',
                scale: oldModelSerie.scaleFactor ?? 'empty', // TODO
                precision: oldModelSerie.precision ?? 2,
                absolute: oldModelSerie.showAbsValue,
                percentage: oldModelSerie.showPercentage
            }
        })

    }
    return formattedSerieSettings
}

const getFormattedColorSettings = (widget: any) => {
    let formattedColors = [] as IHighchartColor[]
    if (widget.content.chartTemplate.CHART.COLORPALETTE.COLOR) {
        formattedColors = { ...widget.content.chartTemplate.CHART.COLORPALETTE.COLOR } // TODO - CHECK THIS
    }
    return formattedColors
}