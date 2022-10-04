import { defineStore } from 'pinia'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
            selectedSheetIndex: 0,
            crossNavigations: [],
            outputParameters: []
        }
    },
    actions: {
        removeDashboard(dashboard) {
            delete this.dashboards[dashboard.id]
        },
        getDashboard(dashboardId) {
            return this.dashboards[dashboardId]
        },
        setDashboard(id, dashboard) {
            this.dashboards[id] = dashboard
        },
        setDashboardSheet(dashboard) {
            this.dashboardModel[dashboard.id].sheet = dashboard.sheet
        },
        createNewWidget(dashboardId, widget) {
            // TODO - dashboardId
            this.dashboards[dashboardId].widgets.push(widget)
            this.dashboards[dashboardId].sheets[this.selectedSheetIndex].widgets.lg.push({ id: widget.id, h: 10, i: cryptoRandomString({ length: 16, type: 'base64' }), w: 10, x: 0, y: 0, moved: false })
        },
        updateWidget(dashboardId, widget) {
            // TODO - dashboardId
            for (let i = 0; i < this.dashboards[dashboardId].widgets.length; i++) {
                console.log(widget.id + ' === ' + this.dashboards[dashboardId].widgets[i].id)
                if (widget.id === this.dashboards[dashboardId].widgets[i].id) {
                    this.dashboards[dashboardId].widgets[i] = deepcopy(widget)
                }
            }
        },
        setSelectedSheetIndex(index) {
            this.selectedSheetIndex = index
        },
        getDashboardSelectedDatastes(index) {
            const temp = this.dashboards[index]?.configuration?.datasets
            return temp ?? []
        },
        getCrossNavigations() {
            return this.crossNavigations
        },
        setCrosssNavigations(crossNavigations) {
            this.crossNavigations = crossNavigations
        },
        getOutputParameters() {
            return this.outputParameters
        },
        setOutputParameters(outputParameters) {
            this.outputParameters = outputParameters
        }
    }
})

export default store
