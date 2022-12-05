import { hexToRgb } from "@/modules/documentExecution/dashboard/helpers/FormattingHelpers"
import { HighchartsPieChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget"
import { HighchartsOptions3D } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsWidget"

export const updatePieChartModel = (oldModel: any, newModel: HighchartsPieChartModel) => {
    console.log("----------------------------------- OLD MODEL: ", oldModel)
    console.log("----------------------------------- NEW MODEL: ", newModel)
    newModel.chart.type = "pie"
    newModel.title = oldModel.CHART.TITLE

    getFormatted3DConfiguration(oldModel, newModel)
    getFormattedNoDataConfiguration(oldModel, newModel)











    // newModel.plotOptions.pie.cursor = 'pointer'
    // newModel.settings.colorPalette = oldModel.CHART.COLORPALETTE



    // // CATEGORIES
    // newModel.settings.categories = [oldModel.CHART.VALUES.CATEGORY.name]
    // if (oldModel.CHART.VALUES.CATEGORY.groupby) {
    //     let categoriesArray = oldModel.CHART.VALUES.CATEGORY.groupby.split(',')
    //     categoriesArray = categoriesArray.map(element => element.trim())
    //     newModel.settings.categories.push(...categoriesArray)
    // }



    // // LEGEND
    // if (oldModel.CHART.LEGEND?.show) {
    //     newModel.plotOptions.pie.showInLegend = true
    //     newModel.legend = {
    //         enabled: true,
    //         align: 'center',
    //         verticalAlign: 'bottom',
    //         layout: 'horizontal',
    //     }
    // }

    // // DRILLDOWN
    // if (oldModel.CHART.drillable && newModel.settings.categories.length > 1) {
    //     newModel.settings.drilldown = true
    // }

    // // SERIE
    // oldModel.CHART.VALUES.SERIE.forEach((item) => {
    //     newModel.series.push({
    //         "name": item.name,
    //         "groupingFunction": item.groupingFunction,
    //         "turboThreshold": 15000
    //     })
    // })

    // return newModel
}

const getFormatted3DConfiguration = (oldModel: any, newModel: HighchartsPieChartModel) => {
    if (oldModel.CHART.show3D) {
        newModel.plotOptions.pie.depth = oldModel.CHART.depth
        newModel.chart.options3d = {
            enabled: oldModel.CHART.show3D,
            alpha: oldModel.CHART.alpha,
            beta: oldModel.CHART.beta,
            viewDistance: oldModel.CHART.viewDistance
        } as HighchartsOptions3D
    }
}


const getFormattedNoDataConfiguration = (oldModel: any, newModel: HighchartsPieChartModel) => {
    if (oldModel.CHART.EMPTYMESSAGE) {
        newModel.lang.noData = oldModel.CHART.EMPTYMESSAGE.text
        newModel.noData.position = oldModel.CHART.EMPTYMESSAGE.position ? { align: oldModel.CHART.EMPTYMESSAGE.position.align, verticalAlign: oldModel.CHART.EMPTYMESSAGE.position.verticalAlign } : { align: '', verticalAlign: '' }

        if (oldModel.CHART.EMPTYMESSAGE.style) {
            newModel.noData.style = {
                fontFamily: oldModel.CHART.EMPTYMESSAGE.style.fontFamily ?? '',
                fontSize: oldModel.CHART.EMPTYMESSAGE.style.fontSize ?? '',
                fontWeight: oldModel.CHART.EMPTYMESSAGE.style.fontWeight,
                color: oldModel.CHART.EMPTYMESSAGE.style.color ? hexToRgb(oldModel.CHART.EMPTYMESSAGE.style.color) : '',
                backgroundColor: ''
            }
        }
    }
}

