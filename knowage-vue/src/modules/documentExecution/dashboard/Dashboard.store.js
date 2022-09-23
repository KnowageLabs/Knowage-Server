import { defineStore } from 'pinia'
import deepcopy from 'deepcopy'

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
        removeDashboard(state, dashboard) {
            delete state.dashboards[dashboard.id]
        },
        getDashboard(dashboardId) {
            return this.dashboards[dashboardId]
        },
        setDashboard(dashboard) {
            this.dashboards[dashboard.id] = dashboard
        },
        setDashboardSheet(dashboard) {
            this.dashboardModel[dashboard.id].sheet = dashboard.sheet
        },
        createNewWidget(widget) {
            // TODO - hardcoded 1 for dashboard
            this.dashboards[1].widgets.push(widget)
            this.dashboards[1].sheets[this.selectedSheetIndex].widgets.lg.push({ id: widget.id, h: 5, i: 0, w: 10, x: 10, y: 10, moved: false })
        },
        updateWidget(widget) {
            // TODO - hardcoded 1 for dashboard
            for (let i = 0; i < this.dashboards[1].widgets.length; i++) {
                console.log(widget.id + ' === ' + this.dashboards[1].widgets[i].id)
                if (widget.id === this.dashboards[1].widgets[i].id) {
                    this.dashboards[1].widgets[i] = deepcopy(widget)
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
