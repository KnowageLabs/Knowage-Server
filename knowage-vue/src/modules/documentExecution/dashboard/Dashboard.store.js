import { defineStore } from 'pinia'

const store = defineStore('dashboardStore', {
    state() {
        return {
            dashboards: {},
        }
    },
    actions: {
        removeDashboard(state, dashboard) {
            delete state.dashboards[dashboard.id]
        },
        setDashboardSheet(state, dashboard) {
            state.dashboards[dashboard.id].sheet = dashboard.sheet
        }
    }
})

export default store
