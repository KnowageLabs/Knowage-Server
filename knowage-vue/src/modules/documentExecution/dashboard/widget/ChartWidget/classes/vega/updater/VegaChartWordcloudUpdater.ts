import { IVegaChartsModel } from './../../../../../interfaces/vega/VegaChartsWidget.d';

export const updateWordcloudChartModel = (oldModel: any, newModel: IVegaChartsModel) => {
    console.log('------------ OLD MODEL: ', oldModel)
    // getFormattedNoDataConfiguration(oldModel, newModel)

    console.log('------------ NEW MODEL: ', newModel)
    return newModel
}
