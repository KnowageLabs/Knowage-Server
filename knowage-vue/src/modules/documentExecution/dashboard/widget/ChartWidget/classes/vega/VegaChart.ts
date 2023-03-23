import { IVegaChartsModel } from '../../../../interfaces/vega/VegaChartsWidget';
import * as vegaChartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/vega/VegaDefaultValues'

export class VegaChart {
    model: IVegaChartsModel

    constructor() {
        this.model = this.createNewChartModel()
    }

    getModel() {
        return this.model
    }

    createNewChartModel() {
        return {
            "$schema": vegaChartsDefaultValues.getDefaultVegaSchema(),
            chart: { type: '' },
            description: vegaChartsDefaultValues.getDefaultVegaDescription(),
            padding: vegaChartsDefaultValues.getDefaultVegaPadding(),
            autosize: vegaChartsDefaultValues.getDefaultVegaAutosize(),
            signals: vegaChartsDefaultValues.getDefaultVegaSignals(),
            data: vegaChartsDefaultValues.getDefaultVegaData(),
            scales: vegaChartsDefaultValues.getDefaultVegaScales(),
            marks: vegaChartsDefaultValues.getDefaultVegaMarks()
        }
    }


}
