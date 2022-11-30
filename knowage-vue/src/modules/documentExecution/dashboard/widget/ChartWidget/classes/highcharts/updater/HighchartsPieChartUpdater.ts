import { HighchartsPieChartModel } from "@/modules/documentExecution/dashboard/interfaces/highcharts/DashboardHighchartsPieChartWidget"

export const updatePieChartModel = (oldModel: any, newModel: HighchartsPieChartModel) => {
    newModel.chart.type = "pie"
    newModel.title = oldModel.CHART.TITLE
    newModel.plotOptions.pie = {
        cursor: 'pointer'
    }
    newModel.settings.colorPalette = oldModel.CHART.COLORPALETTE

    // CATEGORIES
    newModel.settings.categories = [oldModel.CHART.VALUES.CATEGORY.name]
    if (oldModel.CHART.VALUES.CATEGORY.groupby) {
        let categoriesArray = oldModel.CHART.VALUES.CATEGORY.groupby.split(',')
        categoriesArray = categoriesArray.map(element => element.trim())
        newModel.settings.categories.push(...categoriesArray)
    }

    //3D
    if (oldModel.CHART.show3D) {
        newModel.plotOptions.pie.depth = oldModel.CHART.depth
        newModel.chart.options3d = {
            enabled: oldModel.CHART.show3D,
            alpha: oldModel.CHART.alpha,
            beta: oldModel.CHART.beta,
            viewDistance: oldModel.CHART.viewDistance
        }
    }

    // LEGEND
    if (oldModel.CHART.LEGEND?.show) {
        newModel.plotOptions.pie.showInLegend = true
        newModel.legend = {
            enabled: true,
            align: 'center',
            verticalAlign: 'bottom',
            layout: 'horizontal',
        }
    }

    // DRILLDOWN
    if (oldModel.CHART.drillable && newModel.settings.categories.length > 1) {
        newModel.settings.drilldown = true
    }

    // SERIE
    oldModel.CHART.VALUES.SERIE.forEach((item) => {
        newModel.series.push({
            "name": item.name,
            "groupingFunction": item.groupingFunction,
            "turboThreshold": 15000
        })
    })

    return newModel
}

