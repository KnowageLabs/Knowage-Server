import { IVegaChartsModel } from '../../../../interfaces/vega/VegaChartsWidget';
import * as vegaChartsDefaultValues from '../../../WidgetEditor/helpers/chartWidget/vega/VegaDefaultValues'

export class VegaChart {
    model: IVegaChartsModel
    cardinality: any[]
    range: any[]

    constructor() {
        this.model = this.createNewChartModel()
            ; (this.cardinality = []), (this.range = [])
    }

    // TODO
    async updateCardinality(data: any) {
        const cardinalityObj = {}
        this.model.settings.categories.forEach((category) => {
            const tempCategory = data.metaData.fields.filter((i) => i.header === category)
            if (tempCategory.length > 0) {
                cardinalityObj[tempCategory[0].name] = {
                    category: category,
                    set: new Set()
                }
            }
        })
        await data.rows.forEach((row: any) => {
            for (const k in cardinalityObj) {
                if (row[k]) cardinalityObj[k].set.add(row[k])
            }
        })
        this.cardinality = []
        for (const i in cardinalityObj) {
            this.cardinality.push({ [cardinalityObj[i].category]: cardinalityObj[i].set.size })
        }
        return this.cardinality
    }

    getModel() {
        return this.model
    }


    getCardinality() {
        return this.range
    }

    getRange() {
        return this.range
    }

    createNewChartModel() {
        return {
        }
    }


}
