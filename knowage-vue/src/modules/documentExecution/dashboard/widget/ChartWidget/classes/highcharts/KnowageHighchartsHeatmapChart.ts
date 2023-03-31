import { updateHeatmapChartModel } from './updater/KnowageHighchartsHeatmapChartUpdater';
import { IHighchartsHeatmapSerie, IHighchartsHeatmapAxis, IHighchartsHeatmapSerieData } from './../../../../interfaces/highcharts/DashboardHighchartsHeatmapWidget.d';
import { KnowageHighcharts } from './KnowageHighcharts'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
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
        updateHeatmapChartModel(oldModel, this.model)
    }

    setSpecificOptionsDefaultValues() {
        this.setHeatmapPlotOptions()
        this.setHeatmapXAxis()
        this.setHeatmapYAxis()
    }


    setHeatmapPlotOptions() {
        this.model.plotOptions.heatmap = highchartsDefaultValues.getDafaultHeatmapPlotOptions()
    }

    setData(data: any, widgetModel: IWidget) {
        if (!data || !data.rows) return
        if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)

        const categoryValuesMap = {}
        const xAxisCategoriesSet = new Set() as Set<string>
        const yAxisCategoriesSet = new Set() as Set<string>
        this.populateCategoryValuesMap(data, categoryValuesMap, xAxisCategoriesSet, yAxisCategoriesSet)

        const xAxisCategories = this.setXAxisCategories(xAxisCategoriesSet)
        const yAxisCategories = this.setYAxisCategories(yAxisCategoriesSet)

        this.setDataInModelSerie(xAxisCategories, yAxisCategories, categoryValuesMap)
        return this.model.series
    }

    populateCategoryValuesMap(data: any, categoryValuesMap: any, xAxisCategoriesSet: Set<string>, yAxisCategoriesSet: Set<string>) {
        data.rows.forEach((row: any) => {
            const xCategoryValue = row['column_1']
            const yCategoryValue = row['column_2']
            if (!categoryValuesMap[xCategoryValue]) categoryValuesMap[xCategoryValue] = {}
            categoryValuesMap[xCategoryValue][yCategoryValue] = row['column_3'] ?? null

            xAxisCategoriesSet.add(xCategoryValue)
            yAxisCategoriesSet.add(yCategoryValue)
        })
    }

    setXAxisCategories(xAxisCategoriesSet: Set<string>) {
        if (this.model.xAxis?.categories) {
            this.model.xAxis.categories = Array.from(xAxisCategoriesSet) as string[]
            this.model.xAxis.categories.sort()
            return this.model.xAxis.categories
        } else return []
    }


    setYAxisCategories(yAxisCategoriesSet: Set<string>) {
        if ((this.model.yAxis as IHighchartsHeatmapAxis)?.categories) {
            (this.model.yAxis as IHighchartsHeatmapAxis).categories = Array.from(yAxisCategoriesSet) as string[]
            (this.model.yAxis as IHighchartsHeatmapAxis).categories.sort()
            return (this.model.yAxis as IHighchartsHeatmapAxis).categories
        } else return []
    }

    setDataInModelSerie(xAxisCategories: string[], yAxisCategories: string[], categoryValuesMap: any) {
        const modelSerie = this.model.series ? this.model.series[0] : null
        if (modelSerie && xAxisCategories && yAxisCategories) {
            modelSerie.data = [] as IHighchartsHeatmapSerieData[]
            for (let i = 0; i < xAxisCategories.length; i++) {
                for (let j = yAxisCategories.length - 1; j >= 0; j--) {
                    modelSerie.data.push({
                        id: xAxisCategories[i] + ' | ' + yAxisCategories[j],
                        value: categoryValuesMap[xAxisCategories[i]][yAxisCategories[j]] ?? null,
                        x: i,
                        y: j,
                        dataLabels: {}  // TODO
                    })
                }
            }
        }

    }

    getSeriesColumnKey(data: any, widgetModel: IWidget) {
        const measureColumn = widgetModel.columns.find((column: IWidgetColumn) => column.fieldType === 'MEASURE')
        if (measureColumn && data.metaData?.fields) {
            const index = data.metaData.fields.findIndex((field: any) => field.header?.startsWith(measureColumn.columnName))
            return index !== -1 ? data.metaData.fields[index].name : ''
        }
        return ''
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
        (this.model.series as IHighchartsHeatmapSerie[]).forEach((serie: IHighchartsHeatmapSerie) => {
            serie.data.forEach((data: IHighchartsHeatmapSerieData) => {
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
