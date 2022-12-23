import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import { IHighchartsChartModel, IHighchartsChartSerie, IHighchartsSerieAccessibility, ISerieAccessibilitySetting } from '@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget'
import * as highchartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/highcharts/HighchartsDefaultValues'

export class KnowageHighcharts {
    model: IHighchartsChartModel
    cardinality: any[]
    range: any[]

    constructor() {
        this.model = this.createNewChartModel()
        ;(this.cardinality = []), (this.range = [])
    }

    updateCardinality = async (data: any) => {
        let cardinalityObj = {}
        this.model.settings.categories.forEach((category) => {
            let tempCategory = data.metaData.fields.filter((i) => i.header === category)
            if (tempCategory.length > 0) {
                cardinalityObj[tempCategory[0].name] = {
                    category: category,
                    set: new Set()
                }
            }
        })
        await data.rows.forEach((row: any, index: number) => {
            for (let k in cardinalityObj) {
                if (row[k]) cardinalityObj[k].set.add(row[k])
            }
        })
        this.cardinality = []
        for (let i in cardinalityObj) {
            this.cardinality.push({ [cardinalityObj[i].category]: cardinalityObj[i].set.size })
        }
        return this.cardinality
    }

    getModel = () => {
        return this.model
    }

    getCardinality = () => {
        return this.range
    }

    getRange = () => {
        return this.range
    }

    dispatchEvent = (e: any) => {
        var myCustomEvent = new CustomEvent(e.type, { detail: e })
        document.dispatchEvent(myCustomEvent)
    }

    createNewChartModel = () => {
        return {
            title: '',
            lang: { noData: '' },
            chart: {
                options3d: highchartsDefaultValues.getDefault3DOptions(),
                type: ''
            },
            noData: highchartsDefaultValues.getDefaultNoDataConfiguration(),
            accessibility: highchartsDefaultValues.getDefaultAccessibilitySettings(),
            series: [],
            settings: {
                drilldown: {}, // TODO
                categories: [] // TODO
            },
            plotOptions: {
                series: { events: {} }
            },
            legend: highchartsDefaultValues.getDefaultLegendSettings(),
            tooltip: highchartsDefaultValues.getDefaultTooltipSettings(),
            credits: { enabled: false }
        }
    }

    updateSeriesAccessibilitySettings = (widgetModel: IWidget, model: any) => {
        if (!widgetModel || !widgetModel.settings.accesssibility || !widgetModel.settings.accesssibility.seriesAccesibilitySettings) return
        this.setAllSeriesAccessibilitySettings(widgetModel)
        this.setSpecificAccessibilitySettings(widgetModel, model)
    }

    setAllSeriesAccessibilitySettings = (widgetModel: IWidget) => {
        this.model.series.forEach((serie: IHighchartsChartSerie) => {
            if (this.model.chart.type !== 'pie' && widgetModel.settings.accesssibility.seriesAccesibilitySettings[0] && widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility.enabled) {
                serie.accessibility = {
                    ...widgetModel.settings.accesssibility.seriesAccesibilitySettings[0].accessibility
                }
            } else {
                serie.accessibility = {
                    enabled: false,
                    description: '',
                    exposeAsGroupOnly: false,
                    keyboardNavigation: { enabled: false }
                }
            }
        })
    }

    setSpecificAccessibilitySettings = (widgetModel: IWidget, model: any) => {
        const index = this.model.chart.type !== 'pie' ? 1 : 0
        for (let i = index; i < widgetModel.settings.accesssibility.seriesAccesibilitySettings.length; i++) {
            const seriesAccesibilitySetting = widgetModel.settings.accesssibility.seriesAccesibilitySettings[i] as ISerieAccessibilitySetting
            if (seriesAccesibilitySetting.accessibility.enabled) seriesAccesibilitySetting.names.forEach((serieName: string) => this.updateSerieAccessibilitySettings(serieName, seriesAccesibilitySetting.accessibility, model))
        }
    }

    updateSerieAccessibilitySettings = (serieName: string, accessibility: IHighchartsSerieAccessibility, model: any) => {
        const index = model.series.findIndex((serie: IHighchartsChartSerie) => serie.name === serieName)
        if (index !== -1) model.series[index].accessibility = { ...accessibility }
    }

    updateFormatterSettings = (object: any, formatProperty: string | null, formatterProperty: string, formatterTextProperty: string, formatterErrorProperty: string) => {
        let hasError = false
        if (formatProperty && object[formatProperty]?.trim() === '') delete object[formatProperty]
        if (!object[formatterTextProperty] || !object[formatterTextProperty].trim()) {
            delete object[formatterProperty]
            object[formatterErrorProperty] = ''
            return hasError
        } else {
            try {
                const fn = eval(`(${object[formatterTextProperty]})`)
                if (typeof fn === 'function') object[formatterProperty] = fn
                object[formatterErrorProperty] = ''
            } catch (error) {
                object[formatterErrorProperty] = (error as any).message
                hasError = true
            }
        }

        return hasError
    }

    updateLegendSettings = () => {
        if (this.model.plotOptions.pie) this.model.plotOptions.pie.showInLegend = true
        return this.updateFormatterSettings(this.model.legend, 'labelFormat', 'labelFormatter', 'labelFormatterText', 'labelFormatterError')
    }

    updateTooltipSettings = () => {
        let hasError = this.updateFormatterSettings(this.model.tooltip, null, 'formatter', 'formatterText', 'formatterError')
        if (hasError) return hasError
        hasError = this.updateFormatterSettings(this.model.tooltip, null, 'pointFormatter', 'pointFormatterText', 'pointFormatterError')
        return hasError
    }

    updateChartColorSettings = (widgetModel: IWidget, model: any) => {
        if (!model.plotOptions.pie) return
        model.plotOptions.pie.colors = widgetModel.settings.chart.colors
    }
}
