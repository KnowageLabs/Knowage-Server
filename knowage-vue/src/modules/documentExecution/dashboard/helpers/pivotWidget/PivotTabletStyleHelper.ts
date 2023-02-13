import { IPivotTableStyle } from "../../interfaces/pivotTable/DashboardPivotTableWidget"
import { getFormattedBorderStyle, getFormattedPaddingStyle, getFormattedShadowsStyle, getFormattedTitleStyle, getFormattedBackgroundStyle } from "../common/WidgetStyleHelper"
import * as pivotTalbeDefaultValues from '../../widget/WidgetEditor/helpers/pivotTableWidget/PivotTableDefaultValues'

export const getFormattedStyle = (widget: any) => {
    return {
        title: getFormattedTitleStyle(widget),
        borders: getFormattedBorderStyle(widget),
        columns: pivotTalbeDefaultValues.getDefaultColumnStyles(),
        headers: getFormattedHeadersStyle(widget),
        padding: getFormattedPaddingStyle(widget),
        rows: getFormattedRowsStyle(widget),
        shadows: getFormattedShadowsStyle(widget),
        background: getFormattedBackgroundStyle(widget)
    } as IPivotTableStyle
}


// TODO - Darko
const getFormattedHeadersStyle = (widget: any) => {

}

const getFormattedRowsStyle = (widget: any) => {

}