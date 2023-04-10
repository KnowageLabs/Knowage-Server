import { updateHeatmapChartModel } from './updater/KnowageHighchartsHeatmapChartUpdater';
import { KnowageHighcharts } from './KnowageHighcharts'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { createHeatMapSerie } from './updater/KnowageHighchartsCommonUpdater'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'
import deepcopy from 'deepcopy'
import moment from 'moment';

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
        this.setHeatmapLegend()
        this.setHeatmapXAxis()
        this.setHeatmapYAxis()
    }

    setHeatmapPlotOptions() {
        this.model.plotOptions.heatmap = highchartsDefaultValues.getDafaultHeatmapPlotOptions()
    }

    setHeatmapLegend() {
        this.model.legend = highchartsDefaultValues.getDefaultHeatmapLegendSettings()
    }

    setData(data: any, widgetModel: IWidget) {
        // TODO - see about sorting
        console.log('------------ DATA: ', data)
        if (!data || !data.rows) return
        if (this.model.series.length === 0) this.getSeriesFromWidgetModel(widgetModel)

        const categoryValuesMap = {}
        const xAxisCategoriesSet = new Set() as Set<string>
        const yAxisCategoriesSet = new Set() as Set<string>
        const firstAttributeIsDate = data.metaData.fields[1] && ['date', 'timestamp'].includes(data.metaData.fields[1].type)
        const secondAttributeIsDate = data.metaData.fields[2] && ['date', 'timestamp'].includes(data.metaData.fields[2].type)
        const attrbiuteColumnsFromWidgetModel = widgetModel.columns.filter((column: IWidgetColumn) => column.fieldType === 'ATTRIBUTE')
        const dateFormat = widgetModel.settings?.configuration?.datetypeSettings?.format
        this.populateCategoryValuesMap(data, categoryValuesMap, xAxisCategoriesSet, yAxisCategoriesSet, widgetModel, firstAttributeIsDate, secondAttributeIsDate, dateFormat)

        const xAxisCategories = this.setXAxisCategories(xAxisCategoriesSet, firstAttributeIsDate ? dateFormat : '', attrbiuteColumnsFromWidgetModel[0])
        const yAxisCategories = this.setYAxisCategories(yAxisCategoriesSet, secondAttributeIsDate ? dateFormat : '', attrbiuteColumnsFromWidgetModel[1])

        this.setDataInModelSerie(xAxisCategories, yAxisCategories, categoryValuesMap)
        return this.model.series
    }

    populateCategoryValuesMap(data: any, categoryValuesMap: any, xAxisCategoriesSet: Set<string>, yAxisCategoriesSet: Set<string>, widgetModel: IWidget, firstAttributeIsDate: boolean, secondAttributeIsDate: boolean, dateFormat: string) {
        data.rows.forEach((row: any) => {
            const xCategoryValue = firstAttributeIsDate ? this.getFormattedDateCategoryValue(row['column_1'], dateFormat, data.metaData.fields[1].type) : row['column_1']
            const yCategoryValue = secondAttributeIsDate ? this.getFormattedDateCategoryValue(row['column_2'], dateFormat, data.metaData.fields[2].type) : row['column_2']
            if (!categoryValuesMap[xCategoryValue]) categoryValuesMap[xCategoryValue] = {}
            if (categoryValuesMap[xCategoryValue][yCategoryValue]) categoryValuesMap[xCategoryValue][yCategoryValue] += row['column_3'] ?? 0
            else categoryValuesMap[xCategoryValue][yCategoryValue] = row['column_3'] ?? null

            xAxisCategoriesSet.add(xCategoryValue)
            yAxisCategoriesSet.add(yCategoryValue)
        })
    }

    getFormattedDateCategoryValue(dateString: string, dateFormat: string, type: 'date' | 'timestamp') {
        if (!dateFormat) return dateString
        const date = moment(dateString, type === 'date' ? 'DD/MM/YYYY' : 'DD/MM/YYYY HH:mm:ss.SSS')
        return date.isValid() ? date.format(dateFormat) : dateString
    }

    // TODO - Put in same method?
    setXAxisCategories(xAxisCategoriesSet: Set<string>, dateFormat: string, modelAttributeColumn: IWidgetColumn | null) {
        const sortType = modelAttributeColumn?.orderType ? modelAttributeColumn.orderType : 'asc'
        console.log('------- SORT TYPE: ', sortType)
        if (this.model.xAxis?.categories) {
            this.model.xAxis.categories = Array.from(xAxisCategoriesSet) as string[]
            this.sortCategories(this.model.xAxis.categories, dateFormat, sortType)
            return this.model.xAxis.categories
        } else return []
    }

    sortCategories(categories: string[], dateFormat: string, sortType: string) {
        if (dateFormat) {
            categories.sort((a: string, b: string) => sortType === 'desc' ? moment(b, dateFormat).diff(moment(a, dateFormat)) : moment(a, dateFormat).diff(moment(b, dateFormat)))
        } else {
            sortType === 'desc' ? categories.reverse() : categories.sort()
        }
        console.log('categories: ', categories)
    }


    setYAxisCategories(yAxisCategoriesSet: Set<string>, dateFormat: '', modelAttributeColumn: IWidgetColumn | null) {
        const sortType = modelAttributeColumn?.orderType ? modelAttributeColumn.orderType : 'asc'
        console.log('------- SORT TYPE: ', sortType)
        if (this.model.yAxis?.categories) {
            this.model.yAxis.categories = Array.from(yAxisCategoriesSet) as string[]
            this.sortCategories(this.model.yAxis.categories, dateFormat, sortType)
            return this.model.yAxis.categories
        } else return []
    }

    setDataInModelSerie(xAxisCategories: string[], yAxisCategories: string[], categoryValuesMap: any) {
        const modelSerie = this.model.series ? this.model.series[0] : null
        if (modelSerie && xAxisCategories && yAxisCategories) {
            modelSerie.data = [] as any[]
            for (let i = 0; i < xAxisCategories.length; i++) {
                for (let j = yAxisCategories.length - 1; j >= 0; j--) {
                    modelSerie.data.push({
                        id: xAxisCategories[i] + ' | ' + yAxisCategories[j],
                        value: categoryValuesMap[xAxisCategories[i]][yAxisCategories[j]] ?? null,
                        x: i,
                        y: j,
                        name: xAxisCategories[i],
                        groupingValue: yAxisCategories[j],
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


    formatSeriesFromOtherChartTypeSeries() {
        this.model.series = this.model.series.map((serie: any) => { return this.getFormattedSerieFromOtherChartTypeSerie(serie) })
    }

    getFormattedSerieFromOtherChartTypeSerie(otherChartSerie: any) {
        const formattedSerie = { name: otherChartSerie.name, data: [], accessibility: {} }
        if (otherChartSerie.accessibility) formattedSerie.accessibility
        return formattedSerie
    }
}
