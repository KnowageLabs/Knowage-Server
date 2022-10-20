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

export const updateWidgetHelper = (dashboardId: string, widget: IWidget, dashboards: any) => {
    for (let i = 0; i < dashboards[dashboardId].widgets.length; i++) {
        if (widget.id === dashboards[dashboardId].widgets[i].id) {
            dashboards[dashboardId].widgets[i] = deepcopy(widget)
            emitter.emit("widgetUpdatedFromStore", widget)
        }
    }
}
