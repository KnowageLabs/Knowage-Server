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

export const deleteWidgetHelper = (dashboardId: string, widget: IWidget, dashboards: any) => {
    if (!dashboards[dashboardId]) return
    const index = dashboards[dashboardId].widgets.findIndex((tempWidget: IWidget) => tempWidget.id === widget.id)
    if (index !== -1) {
        dashboards[dashboardId].widgets.splice(index, 1)
        deleteWidgetFromSheets(dashboards[dashboardId], widget.id as string)
    }

}

const deleteWidgetFromSheets = (dashboard: IDashboard, widgetId: string) => {
    const sheets = dashboard.sheets as any
    for (let i = 0; i < sheets.length; i++) {
        const widgets = sheets[i].widgets.lg
        for (let j = widgets.length - 1; j >= 0; j--) {
            if (widgets[j].id === widgetId) {
                widgets.splice(widgets[j], 1)
            }
        }
    }
}

