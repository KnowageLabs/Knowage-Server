import { defineStore } from 'pinia'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
            selectedSheetIndex: 0
        }
    },
    actions: {
        removeDashboard(state, dashboard) {
            delete state.dashboards[dashboard.id]
        },
        setDashboard(dashboard) {
            this.dashboards[dashboard.id] = dashboard
        },

        setDashboardSheet(dashboard) {
            this.dashboardModel[dashboard.id].sheet = dashboard.sheet
        },
        createNewWidget(widget) {
            delete widget.new
            // TODO - hardcoded 1 for dashboard
            this.dashboards[1].widgets.push(widget)
            this.dashboards[1].sheets[this.selectedSheetIndex].widgets.lg.push({ id: widget.id, h: 5, i: 0, w: 10, x: 10, y: 10, moved: false })
        },
        updateWidget(widget) {
            // TODO - hardcoded 1 for dashboard
            for (let i = 0; i < this.dashboards[1].sheets.length; i++) {}
        },
        setSelectedSheetIndex(index) {
            this.selectedSheetIndex = index
        },
        getDashboardSelectedDatastes(index) {
            const temp = this.dashboards[index]?.configuration?.datasets
            return temp ?? []
        }
    }
})

export default store
