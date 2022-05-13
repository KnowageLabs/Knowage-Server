const dashboardStore = {
    namespaced: true,
    state() {
        return {
            dashboards: {}
        }
    },
    mutations: {
        setDashboardSheet(state, dashboard) {
            state.dashboards[dashboard.id] = dashboard.sheet
        }
    }
}

export default dashboardStore
