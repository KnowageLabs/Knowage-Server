import { IWidget } from './../../../../../Dashboard.d';
import { KnowageVegaChartWordcloud } from './../../../../ChartWidget/classes/vega/KnowageVegaChartWordcloud';

export const formatVegaWidget = (widget: IWidget) => {
    widget.settings.chartModel = new KnowageVegaChartWordcloud(widget.settings.chartModel.model ?? widget.settings.chartModel)
}
