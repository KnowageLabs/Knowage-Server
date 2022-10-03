import { IWidget } from '../../../Dashboard'
import { formatTableWidgetForSave } from './tableWidget/TableWidgetBackendSaveHelper'
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'
import * as  tableWidgetDefaultValues from './tableWidget/TableWidgetDefaultValues'

export function createNewWidget() {

    const widget = {
        id: cryptoRandomString({ length: 16, type: 'base64' }),
        new: true,
        type: 'table',
        dataset: null,
        columns: [],
        settings: {
            sortingColumn: '',
            sortingOrder: '',
            updatable: true,
            clickable: true,
            conditionalStyles: tableWidgetDefaultValues.getDefaultConditionalStyles(),
            configuration: {
                columnGroups: tableWidgetDefaultValues.getDefaultColumnGroups(),
                exports: tableWidgetDefaultValues.getDefaultExportsConfiguration(),
                headers: tableWidgetDefaultValues.getDefaultHeadersConfiguration(),
                rows: tableWidgetDefaultValues.getDefaultRowsConfiguration(),
                summaryRows: tableWidgetDefaultValues.getDefaultSummaryRowsConfiguration(),
                customMessages: tableWidgetDefaultValues.getDefaultCustomMessages()
            },
            interactions: {
                crosssNavigation: tableWidgetDefaultValues.getDefaultCrossNavigation(),
                link: tableWidgetDefaultValues.getDefaultLinks(),
                preview: tableWidgetDefaultValues.getDefaultPreview(),
                selection: tableWidgetDefaultValues.getDefaultSelection()
            },
            pagination: tableWidgetDefaultValues.getDefaultPagination(),
            style: {
                borders: tableWidgetDefaultValues.getDefaultBordersStyle(),
                columns: tableWidgetDefaultValues.getDefaultColumnStyles(),
                columnGroups: tableWidgetDefaultValues.getDefaultColumnStyles(),
                headers: tableWidgetDefaultValues.getDefaultHeadersStyle(),
                padding: tableWidgetDefaultValues.getDefaultPaddingStyle(),
                rows: tableWidgetDefaultValues.getDefaultRowsStyle(),
                shadows: tableWidgetDefaultValues.getDefaultShadowsStyle(),
                summary: tableWidgetDefaultValues.getDefualtSummryStyle()
            },
            tooltips: tableWidgetDefaultValues.getDefaultTooltips(),
            visualization: tableWidgetDefaultValues.getDefaultVisualizations(),
            responsive: tableWidgetDefaultValues.getDefaultResponsivnes()

        }

    } as IWidget

    console.log("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA : ", tableWidgetDefaultValues.getDefaultTooltips())

    return widget
}


export function formatWidgetForSave(tempWidget: IWidget) {
    if (!tempWidget) return

    const widget = deepcopy(tempWidget)

    switch (widget.type) {
        case 'table': formatTableWidgetForSave(widget)
    }

    return widget
}

export function getRGBColorFromString(color: string) {
    const temp = color
        ?.trim()
        ?.substring(4, color.length - 1)
        ?.split(',')

    if (temp) {
        return { r: +temp[0], g: +temp[1], b: +temp[2] }
    } else return { r: 0, g: 0, b: 0 }
}