const dashboardStore = {
    namespaced: true,
    state() {
        return {
            dashboards: {}
        }
    },
    mutations: {
        removeDashboard(state, dashboard) {
            delete state.dashboards[dashboard.id]
        },
        setDashboardSheet(state, dashboard) {
            state.dashboards[dashboard.id] = dashboard.sheet
        }
    }
}

export default dashboardStore
