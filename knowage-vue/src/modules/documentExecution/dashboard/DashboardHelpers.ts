import { IDashboard, IWidget } from './Dashboard'
import mitt from 'mitt'
export const emitter = mitt()
import cryptoRandomString from 'crypto-random-string'
import deepcopy from 'deepcopy'

export const createNewDashboardModel = () => {
    const dashboardModel = {
        sheets: [],
        widgets: [],
        configuration: {
            id: cryptoRandomString({ length: 16, type: 'base64' }),
            name: '',
            label: '',
            description: '',
            associations: [],
            datasets: [],
            variables: [],
            themes: {},
            selections: []
        },
        version: "8.2.0"
    } as IDashboard

    return dashboardModel
}

export const updateWidgetHelper = (dashboardId: string, widget: IWidget, dashboards: any, removeSelection: Function) => {
    for (let i = 0; i < dashboards[dashboardId].widgets.length; i++) {
        console.log(widget.id + ' === ' + dashboards[dashboardId].widgets[i].id)
        if (widget.id === dashboards[dashboardId].widgets[i].id) {
            if (widget.type === 'selector') updateSelectionsOnWidgetEdit(dashboards[dashboardId].widgets[i], widget, dashboardId, removeSelection)
            dashboards[dashboardId].widgets[i] = deepcopy(widget)
        }
    }
}

const updateSelectionsOnWidgetEdit = (oldWidget: IWidget, newWidget: IWidget, dashboardId: string, removeSelection: Function) => {
    if (oldWidget.dataset !== newWidget.dataset || (oldWidget.dataset === newWidget.dataset && oldWidget.columns[0]?.columnName !== newWidget.columns[0].columnName)) {
        removeSelection({ datasetId: oldWidget.dataset, columnName: oldWidget.columns[0]?.columnName ?? '' }, dashboardId)
    }

}