import { IHighchartsHeatmapSerie, IHighchartsHeatmapAxis } from './../../../../interfaces/highcharts/DashboardHighchartsHeatmapWidget.d';
import { KnowageHighcharts } from './KnowageHighcharts'
import { updatePieChartModel } from './updater/KnowageHighchartsPieChartUpdater'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartSerie, IHighchartsChartSerieData } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import { createHeatMapSerie } from './updater/KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import deepcopy from 'deepcopy'

export class KnowageHighchartsHeatmapChart extends KnowageHighcharts {
    constructor(model: any) {
        super()
        this.setSpecificOptionsDefaultValues()
        if (model && model.CHART) this.updateModel(deepcopy(model))
        else if (model) {
            this.model = deepcopy(model)
            if (model.chart.type !== 'heatmap') {
                this.formatSeriesFromOtherChartTypeSeries()
                this.setSpecificOptionsDefaultValues()
            }
        }
        this.model.chart.type = 'heatmap'
    }

    updateModel(oldModel: any) {
        updatePieChartModel(oldModel, this.model)
    }

    setSpecificOptionsDefaultValues() {
        // TODO
        this.setHeatmapXAxis()
        this.setHeatmapYAxis()
    }

    setData(data: any, widgetModel: IWidget) {
        console.log('---------- DATA: ', data)
        console.log('---------- widgetModel: ', widgetModel)

        // TODO
        if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)
        const seriesColumnKey = this.getSeriesColumnKey(data, widgetModel)
        console.log('getSeriesColumnKey: ', seriesColumnKey)

        const categoryValuesMap = {}
        this.setAxisCategoriesData(data, widgetModel)
        this.setCategoriesValuesMap = {}

        const modelSeries = this.model.series as IHighchartsHeatmapSerie[]
        modelSeries.map((item, serieIndex) => {
            this.range[serieIndex] = { serie: item.name }
            item.data = []

            data?.rows?.forEach((row: any) => {
                const serieElement = {
                    id: row.id,
                    value: row[seriesColumnKey],
                    x: 0,
                    y: 0
                }
                item.data.push(serieElement)
            })
        })
        return this.model.series
    }

    getSeriesColumnKey(data: any, widgetModel: IWidget) {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (measureColumn && data.metaData?.fields) {
            const index = data.metaData.fields.findIndex((field: any) => field.header?.startsWith(measureColumn.columnName))
            return index !== -1 ? data.metaData.fields[index].name : ''
        }
        return ''
    }

    setAxisCategoriesData(data: any, widgetModel: IWidget) {
        const filteredAttributes = widgetModel.columns.filter((column: IWidgetColumn) => column.fieldType === 'ATTRIBUTE')
        const firstTwoAttributes = filteredAttributes.slice(0, 2)
        console.log('firstTwo: ', firstTwoAttributes)
        this.setSpecificAxisCategoriesData(data, firstTwoAttributes[0], 'x')
        this.setSpecificAxisCategoriesData(data, firstTwoAttributes[1], 'y')
    }

    setSpecificAxisCategoriesData(data: any, column: IWidgetColumn, axisType: 'x' | 'y') {
        const categoriesSet = new Set()
        const axisCategories = axisType === 'x' ? this.model.xAxis?.categories : (this.model.yAxis as IHighchartsHeatmapAxis)?.categories
        if (column && data?.metaData?.fields && axisCategories) {
            const index = data.metaData.fields.findIndex((field: any) => field.header?.startsWith(column.columnName))
            const attibuteColumnName = index !== -1 ? data.metaData.fields[index].name : ''
            data.rows?.forEach((row: any) => categoriesSet.add(row[attibuteColumnName]))
            const setValues = Array.from(categoriesSet)
            setValues.forEach((value: any) => axisCategories.push(value))
        }
        console.log('------ axisCategories: ', axisCategories)
    }

    getSeriesFromWidgetModel(widgetModel: IWidget) {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (!measureColumn) return
        this.model.series = [createHeatMapSerie(measureColumn.columnName)]
    }

    setHeatmapXAxis() {
        this.model.xAxis = highchartsDefaultValues.getDefaultHeatmapXAxis()
    }

    setHeatmapYAxis() {
        this.model.yAxis = highchartsDefaultValues.getDefaultHeatmapYAxis()
    }


    updateSeriesLabelSettings(widgetModel: IWidget) {
        // TODO
        if (!widgetModel || !widgetModel.settings.series || !widgetModel.settings.series.seriesLabelsSettings || !widgetModel.settings.series.seriesLabelsSettings[0]) return
        const seriesLabelSetting = widgetModel.settings.series.seriesLabelsSettings[0]
        if (!seriesLabelSetting.label.enabled) return
        this.model.series.forEach((serie: IHighchartsChartSerie) => {
            serie.data.forEach((data: IHighchartsChartSerieData) => {
                data.dataLabels = {
                    backgroundColor: seriesLabelSetting.label.backgroundColor ?? '',
                    distance: 30,
                    enabled: true,
                    position: '',
                    style: {
                        fontFamily: seriesLabelSetting.label.style.fontFamily,
                        fontSize: seriesLabelSetting.label.style.fontSize,
                        fontWeight: seriesLabelSetting.label.style.fontWeight,
                        color: seriesLabelSetting.label.style.color ?? ''
                    },
                    formatter: function () {
                        return KnowageHighchartsHeatmapChart.prototype.handleFormatter(this, seriesLabelSetting.label)
                    }
                }
            })
        })
    }

    formatSeriesFromOtherChartTypeSeries() {
        // TODO
        //this.model.series = this.model.series.map((serie: IHighchartsGaugeSerie) => { return this.getFormattedSerieFromOtherChartTypeSerie(serie) })
    }

    getFormattedSerieFromOtherChartTypeSerie(otherChartSerie: IHighchartsGaugeSerie) {
        // TODO
        // const formattedSerie = { name: otherChartSerie.name, data: [], colorByPoint: true } as IHighchartsChartSerie
        // if (otherChartSerie.accessibility) formattedSerie.accessibility
        // return formattedSerie
    }
}
